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
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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

        JScrollPane scroll = new JScrollPane(tabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(scroll, BorderLayout.CENTER);


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

        ajustarAnchoNombre();
    }

    public static String obtenerEstadoPago(Connection conn, int id, int mes, int anio, double tarifaActual, int estaActivo) {
        // 1. Mirar si ya hay un pago (Check verde)
        String sqlPago = "SELECT cuota_pagada, estado_pago FROM pagos WHERE num_socio=? AND mes=? AND anio=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlPago)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            ResultSet rsP = pstmt.executeQuery();
            // Busca este bloque en obtenerEstadoPago y cámbialo
            if (rsP.next() && rsP.getInt("estado_pago") == 1) {
                double pagado = rsP.getDouble("cuota_pagada");

                // Ahora solo dirá REGALO si el precio es 0 de verdad (porque tú lo has regalado)
                if (pagado == 0) {
                    return "REGALO ✔";
                } else {
                    return pagado + "€ ✔";
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        int anio = (int) comboAnio.getSelectedItem();
        String valorActual = modelo.getValueAt(fila, col).toString();

        if (valorActual.isEmpty()) return;

        if (valorActual.contains("✔")) {
            int borrar = JOptionPane.showConfirmDialog(this, "¿Anular pago?", "Anular", JOptionPane.YES_NO_OPTION);
            if (borrar == JOptionPane.YES_OPTION) {
                eliminarPagoDeBD(id, mesNum, anio);
                cargarDatos();
            }
            return;
        }

        double tarifaSugerida = 0;
        int mesesRegaloDefecto = 0;

        // Abrimos conexión para leer datos iniciales
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement("SELECT tarifa, descuento FROM socios WHERE num_socio = ?")) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                tarifaSugerida = rs.getDouble("tarifa");
                mesesRegaloDefecto = rs.getInt("descuento");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        JPanel panelCobro = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField txtImporte = new JTextField(String.valueOf(tarifaSugerida));
        JComboBox<String> comboMesesPaga = new JComboBox<>(new String[]{"1 Mes", "3 Meses (Trimestral)", "6 Meses (Semestral)", "12 Meses (Anual)"});
        JTextField txtRegalo = new JTextField(String.valueOf(mesesRegaloDefecto));

        panelCobro.add(new JLabel("Importe (€):")); panelCobro.add(txtImporte);
        panelCobro.add(new JLabel("Periodo que paga:")); panelCobro.add(comboMesesPaga);
        panelCobro.add(new JLabel("Meses de regalo:")); panelCobro.add(txtRegalo);

        int result = JOptionPane.showConfirmDialog(this, panelCobro, "Registrar Cobro - " + nombre, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = ConexionDB.conectar()) {
                double importeTotal = Double.parseDouble(txtImporte.getText().replace(",", "."));
                int mesesRegalo = Integer.parseInt(txtRegalo.getText());

                // Determinamos los meses del pack según el desplegable
                int mesesPack = switch (comboMesesPaga.getSelectedIndex()) {
                    case 1 -> 3;
                    case 2 -> 6;
                    case 3 -> 12;
                    default -> 1;
                };

                // Calculamos cuántos de esos meses son de pago real
                int mesesDePagoReal = mesesPack - mesesRegalo;
                if (mesesDePagoReal < 1) mesesDePagoReal = 1;

                java.time.LocalDate fecha = java.time.LocalDate.of(anio, mesNum, 1);

                for (int i = 0; i < mesesPack; i++) {
                    double cuotaAImprimir;

                    if (i < mesesDePagoReal) {
                        // Repartimos el importe total entre los meses de pago real
                        cuotaAImprimir = importeTotal;
                    } else {
                        // El resto de meses del pack son GRATIS
                        cuotaAImprimir = 0.0;
                    }

                    guardarPagoEnBD(conn, id, fecha.getMonthValue(), fecha.getYear(), cuotaAImprimir);
                    fecha = fecha.plusMonths(1);
                }

                cargarDatos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "⚠️ Error: Revisa los números introducidos.");
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

    private void ajustarAnchoNombre() {
        int anchoMaximo = 150;

        int anchoTitulo = (int) tabla.getTableHeader().getDefaultRenderer()
                .getTableCellRendererComponent(tabla, "Socio", false, false, -1, 1)
                .getPreferredSize().getWidth();
        anchoMaximo = Math.max(anchoMaximo, anchoTitulo);

        for (int row = 0; row < tabla.getRowCount(); row++) {
            int anchoCelda = (int) tabla.getCellRenderer(row, 1)
                    .getTableCellRendererComponent(tabla, tabla.getValueAt(row, 1), false, false, row, 1)
                    .getPreferredSize().getWidth();
            anchoMaximo = Math.max(anchoMaximo, anchoCelda);
        }

        tabla.getColumnModel().getColumn(1).setPreferredWidth(anchoMaximo + 15);
    }

    private void guardarPagoEnBD(Connection conn, int id, int mes, int anio, double cuota) throws SQLException {
        String sql = "INSERT INTO pagos (num_socio, mes, anio, cuota_pagada, estado_pago) VALUES (?,?,?,?,1)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            pstmt.setDouble(4, cuota);
            pstmt.executeUpdate();
        }
    }

    private int obtenerDescuentoSocio(int idSocio) {
        String sql = "SELECT descuento FROM socios WHERE num_socio = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSocio);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("descuento");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
} // <--- ESTA ES LA ÚNICA LLAVE QUE CIERRA LA CLASE
