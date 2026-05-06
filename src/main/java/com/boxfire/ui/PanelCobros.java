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
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
                tabla.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1));
            }
        });

        tabla.setRowHeight(35);
        tabla.getTableHeader().setReorderingAllowed(false);

        configurarColores();
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
                String valStr = (value != null) ? value.toString() : "";

                if (col > 1 && !valStr.isEmpty()) {
                    if (valStr.contains("✔")) {
                        c.setBackground(new Color(144, 238, 144));
                        c.setForeground(new Color(0, 100, 0));
                    } else if (valStr.startsWith("D -")) {
                        c.setBackground(new Color(230, 240, 255));
                        c.setForeground(Color.BLUE);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        int anio = (int) comboAnio.getSelectedItem();
        String sqlSocios = "SELECT num_socio, nombre, tarifa, esta_activo, fecha_alta, fecha_baja FROM socios ORDER BY nombre ASC";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sqlSocios)) {
            ResultSet rsSocios = pstmt.executeQuery();
            while (rsSocios.next()) {
                int id = rsSocios.getInt("num_socio");
                String nombre = rsSocios.getString("nombre");
                double tarifaActual = rsSocios.getDouble("tarifa");
                int activo = rsSocios.getInt("esta_activo");

                Object[] fila = new Object[14];
                fila[0] = id;
                fila[1] = (activo == 0) ? nombre + " (BAJA)" : nombre;

                for (int mes = 1; mes <= 12; mes++) {
                    fila[mes + 1] = obtenerEstadoPago(conn, id, mes, anio, tarifaActual, activo);
                }
                modelo.addRow(fila);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        ajustarAnchoNombre();
    }

    public static String obtenerEstadoPago(Connection conn, int id, int mes, int anio, double tarifaActual, int estaActivo) {
        // 1. Buscamos si ya existe un pago
        String sqlPago = "SELECT cuota_pagada, metodo_pago FROM pagos WHERE num_socio=? AND mes=? AND anio=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlPago)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            ResultSet rsP = pstmt.executeQuery();

            if (rsP.next()) {
                double pagado = rsP.getDouble("cuota_pagada");
                String metodo = rsP.getString("metodo_pago");

                // Si la cuota es 0, es un regalo
                if (pagado == 0) return "REGALO ✔";

                // Si tiene precio, buscamos la inicial del método (E, T, B, D)
                String inicial = "";
                if (metodo != null) {
                    if (metodo.equalsIgnoreCase("Efectivo")) inicial = "E";
                    else if (metodo.equalsIgnoreCase("Tarjeta")) inicial = "T";
                    else if (metodo.equalsIgnoreCase("Bizum")) inicial = "B";
                    else if (metodo.equalsIgnoreCase("Domiciliación")) inicial = "D";
                }

                // Retorna el precio individual + inicial + check
                return pagado + "€ [" + inicial + "] ✔";
            }

        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Si no hay pago, comprobamos fechas de ALTA y BAJA
        String sqlSocio = "SELECT forma_pago, fecha_alta, fecha_baja FROM socios WHERE num_socio = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlSocio)) {
            pstmt.setInt(1, id);
            ResultSet rsS = pstmt.executeQuery();
            if (rsS.next()) {
                String fPago = rsS.getString("forma_pago");
                String fAlta = rsS.getString("fecha_alta");
                String fBaja = rsS.getString("fecha_baja");

                java.time.LocalDate fechaCelda = java.time.LocalDate.of(anio, mes, 1);

                // REGLA 1: Si la celda es ANTERIOR al alta, vacío
                if (fAlta != null && !fAlta.isEmpty()) {
                    java.time.LocalDate alta = java.time.LocalDate.parse(fAlta).withDayOfMonth(1);
                    if (fechaCelda.isBefore(alta)) return "";
                }

                // REGLA 2: Si el socio está de baja y la celda es POSTERIOR a la baja, vacío
                if (estaActivo == 0 && fBaja != null && !fBaja.isEmpty()) {
                    java.time.LocalDate baja = java.time.LocalDate.parse(fBaja).withDayOfMonth(1);
                    if (fechaCelda.isAfter(baja)) return "";
                }

                // Si pasa los filtros y es Domiciliación, ponemos la D
                if (fPago != null && fPago.equalsIgnoreCase("Domiciliación")) {
                    return "D - " + tarifaActual + "€";
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Si está activo y no hay pago, mostramos la tarifa normal
        return (estaActivo == 1) ? tarifaActual + "€" : "";
    }


    private void configurarDobleClic() {
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.getSelectedRow();
                    int col = tabla.getSelectedColumn();
                    if (col > 1) procesarCobro(fila, col);
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

        // --- CASO A: ANULAR PAGO EXISTENTE ---
        if (valorActual.contains("✔")) {
            int borrar = JOptionPane.showConfirmDialog(this, "¿Anular pago?", "Anular", JOptionPane.YES_NO_OPTION);
            if (borrar == JOptionPane.YES_OPTION) {
                eliminarPagoDeBD(id, mesNum, anio);
                cargarDatos(); // Refresca la tabla del panel

                // ACTUALIZAR HEADER (Suma de arriba)
                Window win = SwingUtilities.getWindowAncestor(this);
                if (win instanceof VentanaPrincipal) {
                    ((VentanaPrincipal) win).actualizarContador();
                }
            }
            return;
        }

        // --- CASO B: REGISTRAR NUEVO PAGO ---
        String tarifaDeCelda = valorActual.replace("D -", "").replace("€", "").trim().replace(",", ".");
        double tarifaSugerida = 0;
        try {
            tarifaSugerida = Double.parseDouble(tarifaDeCelda);
        } catch (NumberFormatException e) {
            tarifaSugerida = 0;
        }

        JPanel panelCobro = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField txtImporte = new JTextField(String.valueOf(tarifaSugerida));
        JComboBox<String> comboMesesPaga = new JComboBox<>(new String[]{"1 Mes", "3 Meses", "6 Meses", "12 Meses"});
        JTextField txtRegalo = new JTextField("0");
        JComboBox<String> comboMetodo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Bizum", "Domiciliación"});

        panelCobro.add(new JLabel("Importe (€):")); panelCobro.add(txtImporte);
        panelCobro.add(new JLabel("Periodo:")); panelCobro.add(comboMesesPaga);
        panelCobro.add(new JLabel("Regalo:")); panelCobro.add(txtRegalo);
        panelCobro.add(new JLabel("Método:")); panelCobro.add(comboMetodo);

        int result = JOptionPane.showConfirmDialog(this, panelCobro, "Cobro - " + nombre, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = ConexionDB.conectar()) {
                // Ahora tratamos este importe como el PRECIO DE UN MES
                double precioMensual = Double.parseDouble(txtImporte.getText().replace(",", "."));
                int mesesRegalo = Integer.parseInt(txtRegalo.getText().trim());
                String met = (String) comboMetodo.getSelectedItem();

                int mesesPack = switch (comboMesesPaga.getSelectedIndex()) {
                    case 1 -> 3;
                    case 2 -> 6;
                    case 3 -> 12;
                    default -> 1;
                };

                java.time.LocalDate fecha = java.time.LocalDate.of(anio, mesNum, 1);

                for (int i = 0; i < mesesPack; i++) {
                    double cuota;

                    // Si el mes actual es de regalo (está al final del pack)
                    // Ejemplo: Pack 3 meses, 1 regalo -> i=0 (paga), i=1 (paga), i=2 (regalo)
                    if (mesesRegalo > 0 && i >= (mesesPack - mesesRegalo)) {
                        cuota = 0.0;
                    } else {
                        // Se guarda el precio mensual íntegro en cada mes de pago
                        cuota = precioMensual;
                    }

                    guardarPagoEnBD(conn, id, fecha.getMonthValue(), fecha.getYear(), cuota, met);
                    fecha = fecha.plusMonths(1);
                }

                cargarDatos();

                Window win = SwingUtilities.getWindowAncestor(this);
                if (win instanceof VentanaPrincipal) {
                    ((VentanaPrincipal) win).actualizarContador();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en los datos introducidos.");
            }

        }

    }



    private void eliminarPagoDeBD(int id, int mes, int anio) {
        String sql = "DELETE FROM pagos WHERE num_socio=? AND mes=? AND anio=?";
        try (Connection conn = ConexionDB.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setInt(2, mes); ps.setInt(3, anio);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void guardarPagoEnBD(Connection conn, int id, int mes, int anio, double cuota, String met) throws SQLException {
        String sql = "INSERT INTO pagos (num_socio, mes, anio, cuota_pagada, estado_pago, metodo_pago) VALUES (?,?,?,?,1,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setInt(2, mes); ps.setInt(3, anio); ps.setDouble(4, cuota); ps.setString(5, met);
            ps.executeUpdate();
        }
    }

    private void ajustarAnchoNombre() {
        if (tabla.getColumnCount() > 1) {
            tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
            for (int i = 2; i < 14; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(85);
        }
    }
}
