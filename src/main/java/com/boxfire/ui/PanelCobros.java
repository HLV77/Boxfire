package com.boxfire.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import com.boxfire.db.ConexionDB;

public class PanelCobros extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private JComboBox<Integer> comboAnio;

    public PanelCobros() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- CABECERA ---
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("CONTROL DE COBROS MENSUALES", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JPanel panelFiltro = new JPanel();
        panelFiltro.setBackground(Color.WHITE);
        panelFiltro.add(new JLabel("Filtrar por Año: "));
        comboAnio = new JComboBox<>();
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = anioActual - 1; i <= anioActual + 5; i++) comboAnio.addItem(i);
        comboAnio.setSelectedItem(anioActual);
        panelFiltro.add(comboAnio);



        panelNorte.add(titulo, BorderLayout.NORTH);
        panelNorte.add(panelFiltro, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {"Nº", "Socio", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modelo);

        // --- BUSCADOR ---
        JTextField txtBuscar = new JTextField(15);
        panelFiltro.add(new JLabel("   Buscar Socio: "));
        panelFiltro.add(txtBuscar);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String texto = txtBuscar.getText().toLowerCase();
                javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter =
                        new javax.swing.table.TableRowSorter<>(modelo);
                tabla.setRowSorter(sorter);
                // Filtra por la columna 1 (Nombre) ignorando mayúsculas/minúsculas
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + texto, 1));
            }
        });

        panelFiltro.revalidate();
        panelFiltro.repaint();



        tabla.setRowHeight(35);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Renderizador para el color VERDE
        configurarColores();

        // Acción: Doble clic para COBRAR
        configurarDobleClic();

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        comboAnio.addActionListener(e -> cargarDatos());
        cargarDatos();
    }

    private void configurarColores() {
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (col > 1 && value != null && value.toString().contains("✔")) {
                    c.setBackground(new Color(144, 238, 144)); // Verde
                    c.setForeground(new Color(0, 100, 0));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        int anio = (int) comboAnio.getSelectedItem();

        String sqlSocios = "SELECT s.num_socio, s.nombre, s.tarifa, s.esta_activo, s.fecha_alta, s.fecha_baja " +
                "FROM socios s " +
                "LEFT JOIN pagos p ON s.num_socio = p.num_socio AND p.anio = ? " +
                "GROUP BY s.num_socio " +
                "ORDER BY s.nombre ASC";



        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sqlSocios)) {

            pstmt.setInt(1, anio);
            ResultSet rsSocios = pstmt.executeQuery();

            while (rsSocios.next()) {
                int id = rsSocios.getInt("num_socio");
                String nombre = rsSocios.getString("nombre");
                double tarifaActual = rsSocios.getDouble("tarifa");
                int activo = rsSocios.getInt("esta_activo");

                Object[] fila = new Object[14];
                fila[0] = id;
                // Si está de baja, le añadimos una marca visual al nombre
                fila[1] = (activo == 0) ? nombre + " (BAJA)" : nombre;

                for (int mes = 1; mes <= 12; mes++) {
                    fila[mes + 1] = obtenerEstadoPago(conn, id, mes, anio, tarifaActual, activo);
                }
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String obtenerEstadoPago(Connection conn, int id, int mes, int anio, double tarifaActual, int estaActivo) {
        // 1. Mirar si ya hay un pago (Check verde)
        String sqlPago = "SELECT cuota_pagada, estado_pago FROM pagos WHERE num_socio=? AND mes=? AND anio=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlPago)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            ResultSet rsP = pstmt.executeQuery();
            if (rsP.next() && rsP.getInt("estado_pago") == 1) {
                return rsP.getDouble("cuota_pagada") + "€ ✔";
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Lógica de ALTA y BAJA
        String sqlFechas = "SELECT fecha_alta, fecha_baja FROM socios WHERE num_socio = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlFechas)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String fAlta = rs.getString("fecha_alta");
                String fBaja = rs.getString("fecha_baja");
                java.time.LocalDate fechaCelda = java.time.LocalDate.of(anio, mes, 1);

                // OCULTAR MESES ANTERIORES AL ALTA
                if (fAlta != null && !fAlta.isEmpty()) {
                    java.time.LocalDate alta = java.time.LocalDate.parse(fAlta).withDayOfMonth(1);
                    if (fechaCelda.isBefore(alta)) return "";
                }

                // OCULTAR MESES POSTERIORES A LA BAJA
                if (estaActivo == 0 && fBaja != null && !fBaja.isEmpty()) {
                    java.time.LocalDate baja = java.time.LocalDate.parse(fBaja).withDayOfMonth(1);
                    if (fechaCelda.isAfter(baja)) return "";
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 3. Si no hay pago y debe estar activo, mostrar la tarifa
        return (estaActivo == 1 || estaActivo == 0) ? tarifaActual + "€" : "";
    }






    private void configurarDobleClic() {
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.getSelectedRow();
                    int col = tabla.getSelectedColumn();
                    if (col > 1) { // Si es un mes
                        procesarCobro(fila, col);
                    }
                }
            }
        });
    }

    private void procesarCobro(int fila, int col) {
        int id = (int) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 1);
        int mesNum = col - 1;
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String nombreMes = meses[mesNum];
        int anio = (int) comboAnio.getSelectedItem();

        String valorActual = modelo.getValueAt(fila, col).toString();

        if (valorActual.isEmpty()) return;

        // Si ya está pagado, preguntar para anular
        if (valorActual.contains("✔")) {
            int borrar = JOptionPane.showConfirmDialog(this, "¿Anular pago de " + nombreMes + "?", "Anular", JOptionPane.YES_NO_OPTION);
            if (borrar == JOptionPane.YES_OPTION) {
                eliminarPagoDeBD(id, mesNum, anio);
                cargarDatos();
            }
            return;
        }

        // --- MODIFICACIÓN MANUAL DEL PRECIO ---
        // Sacamos el precio sugerido (quitando el símbolo €)
        String precioSugerido = valorActual.replace("€", "").trim();

        // Pedimos el importe final con un cuadro de texto
        String input = JOptionPane.showInputDialog(this,
                "Cobro para: " + nombre + " (" + nombreMes + ")\nImporte a cobrar:",
                precioSugerido);

        // Si el usuario no cancela y escribe algo...
        if (input != null && !input.trim().isEmpty()) {
            try {
                // Cambiamos coma por punto por si el usuario escribe "10,50"
                double tarifaFinal = Double.parseDouble(input.replace(",", "."));

                String sql = "INSERT INTO pagos (num_socio, mes, anio, cuota_pagada, estado_pago) VALUES (?,?,?,?,1)";
                try (Connection conn = ConexionDB.conectar();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, mesNum);
                    pstmt.setInt(3, anio);
                    pstmt.setDouble(4, tarifaFinal);
                    pstmt.executeUpdate();
                    cargarDatos(); // Refrescamos para que salga el check verde
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: Introduce un número válido (ejemplo: 25.50)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    // Este es el método que te faltaba y por eso salía en rojo
    private void eliminarPagoDeBD(int idSocio, int mes, int anio) {
        String sql = "DELETE FROM pagos WHERE num_socio=? AND mes=? AND anio=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSocio);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al anular pago: " + e.getMessage());
        }
    }


}
