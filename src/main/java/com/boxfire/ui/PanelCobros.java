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
        String sqlPago = "SELECT cuota_pagada, metodo_pago FROM pagos WHERE num_socio=? AND mes=? AND anio=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlPago)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, anio);
            ResultSet rsP = pstmt.executeQuery();

            if (rsP.next()) {
                double pagado = rsP.getDouble("cuota_pagada");
                String metodo = rsP.getString("metodo_pago");
                String inicial = "";
                if (metodo != null) {
                    if (metodo.equalsIgnoreCase("Efectivo")) inicial = "E";
                    else if (metodo.equalsIgnoreCase("Tarjeta")) inicial = "T";
                    else if (metodo.equalsIgnoreCase("Bizum")) inicial = "B";
                    else if (metodo.equalsIgnoreCase("Domiciliación")) inicial = "D";
                }
                if (pagado == 0) return "REGALO ✔";
                return pagado + "€ [" + inicial + "] ✔";
            }
        } catch (SQLException e) { e.printStackTrace(); }

        String sqlSocio = "SELECT forma_pago, fecha_alta FROM socios WHERE num_socio = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlSocio)) {
            pstmt.setInt(1, id);
            ResultSet rsS = pstmt.executeQuery();
            if (rsS.next()) {
                String fPago = rsS.getString("forma_pago");
                String fAlta = rsS.getString("fecha_alta");
                if (fAlta != null && !fAlta.isEmpty()) {
                    java.time.LocalDate fechaCelda = java.time.LocalDate.of(anio, mes, 1);
                    java.time.LocalDate alta = java.time.LocalDate.parse(fAlta).withDayOfMonth(1);
                    if (fechaCelda.isBefore(alta)) return "";
                }
                if (fPago != null && fPago.equalsIgnoreCase("Domiciliación")) return "D - " + tarifaActual + "€";
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tarifaActual + "€";
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
        if (valorActual.contains("✔")) {
            if (JOptionPane.showConfirmDialog(this, "¿Anular pago?", "Anular", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                eliminarPagoDeBD(id, mesNum, anio);
                cargarDatos();
            }
            return;
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtImp = new JTextField("45.0");
        JComboBox<String> cbMeses = new JComboBox<>(new String[]{"1 Mes", "3 Meses", "6 Meses", "12 Meses"});
        JTextField txtReg = new JTextField("0");
        JComboBox<String> cbMet = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Bizum", "Domiciliación"});

        panel.add(new JLabel("Importe:")); panel.add(txtImp);
        panel.add(new JLabel("Periodo:")); panel.add(cbMeses);
        panel.add(new JLabel("Regalo:")); panel.add(txtReg);
        panel.add(new JLabel("Método:")); panel.add(cbMet);

        if (JOptionPane.showConfirmDialog(this, panel, "Cobro - " + nombre, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection conn = ConexionDB.conectar()) {
                double total = Double.parseDouble(txtImp.getText().replace(",", "."));
                int reg = Integer.parseInt(txtReg.getText().trim());
                String met = (String) cbMet.getSelectedItem();
                int pack = switch (cbMeses.getSelectedIndex()) { case 1 -> 3; case 2 -> 6; case 3 -> 12; default -> 1; };

                java.time.LocalDate date = java.time.LocalDate.of(anio, mesNum, 1);
                for (int i = 0; i < pack; i++) {
                    double cuota = (i == 0) ? total : 0.0;
                    if (i >= (pack - reg)) cuota = 0.0;
                    guardarPagoEnBD(conn, id, date.getMonthValue(), date.getYear(), cuota, met);
                    date = date.plusMonths(1);
                }
                cargarDatos();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error en los datos."); }
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
