package com.boxfire.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.*;
import com.boxfire.db.ConexionDB;
import javax.swing.table.TableRowSorter;


public class PanelListadoSocios extends JPanel {
    private JTable tabla;
    private JTextField txtBuscador;
    private TableRowSorter<DefaultTableModel> sorter;

    private DefaultTableModel modelo;

    public PanelListadoSocios() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- PANEL SUPERIOR (TÍTULO + BUSCADOR) ---
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(Color.WHITE);

        // 1. Título
        JLabel titulo = new JLabel("LIBRO DE REGISTRO - SOCIOS", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // 2. Buscador
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(new JLabel(" 🔍 Buscar: "));
        txtBuscador = new JTextField(30);
        panelBusqueda.add(txtBuscador);

        panelSuperior.add(titulo);
        panelSuperior.add(panelBusqueda);
        add(panelSuperior, BorderLayout.NORTH);



        // 2. CONFIGURACIÓN DE COLUMNAS (Con unidades en el título)
        String[] columnas = {
                "Nº", "Nombre", "Estado", "Tarifa (€)", "Periodicidad",
                "Dto (Meses)", "F. Pago", "DNI", "Domicilio", "F. Nacimiento", "Teléfono", "E-mail"
        };


        // 2. CONFIGURACIÓN DE COLUMNAS
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Indicamos que la columna 2 es de tipo Boolean para que salga el Checkbox
                if (columnIndex == 2) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // El Nº de socio (columna 0) NO se toca, el resto SÍ
                return column != 0;
            }
        };

        tabla = new JTable(modelo);

        // Esto hace que en cuanto hagas clic en el check, se guarde el cambio y se repinte la fila
        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);



        // --- RENDERIZADOR ÚNICO PARA TODA LA FILA (FONDO, TEXTO Y CHECK) ---
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            private final JCheckBox checkRenderer = new JCheckBox();
            private final JPanel panelCheck = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            private final JLabel labelCheck = new JLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // 1. Obtener el estado de la fila
                boolean estaActivo = (boolean) table.getModel().getValueAt(table.convertRowIndexToModel(row), 2);

                // Definimos los colores
                Color rojoClarito = new Color(255, 220, 220);
                Color rojoFuerte = new Color(180, 0, 0);

                // 2. Si es la columna del Check (Estado), dibujamos Check + Texto
                if (column == 2) {
                    panelCheck.removeAll();
                    checkRenderer.setSelected(estaActivo);
                    checkRenderer.setOpaque(false);
                    labelCheck.setText(estaActivo ? "ALTA" : "BAJA");
                    labelCheck.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    panelCheck.add(checkRenderer);
                    panelCheck.add(labelCheck);

                    if (!estaActivo) {
                        panelCheck.setBackground(rojoClarito);
                        labelCheck.setForeground(rojoFuerte);
                    } else {
                        panelCheck.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                        labelCheck.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                    }
                    return panelCheck;
                }

                // 3. Para el resto de celdas (Nombre, Tarifa, etc.)
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!estaActivo) {
                    c.setBackground(rojoClarito);
                    c.setForeground(rojoFuerte);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                }
                return c;
            }
        });

        // MUY IMPORTANTE: Aplicar este mismo renderizador a los tipos Boolean
        tabla.setDefaultRenderer(Boolean.class, tabla.getDefaultRenderer(Object.class));






        // 1. Creamos las listas de opciones
        String[] precios = {"45", "50", "55", "60", "65", "70", "75", "80", "85", "90"};
        String[] mesesDesc = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        String[] opcionesPeriodo = {"Mensual", "Trimestral", "Semestral", "Anual"};
        String[] opcionesPago = {"Efectivo", "Tarjeta", "Domiciliación", "Bizum"};

                // 2. Creamos los componentes desplegables
        JComboBox<String> comboTarifaTab = new JComboBox<>(precios);
        JComboBox<String> comboDescuentoTab = new JComboBox<>(mesesDesc);
        JComboBox<String> comboPeriodicidad = new JComboBox<>(opcionesPeriodo);
        JComboBox<String> comboPago = new JComboBox<>(opcionesPago);



        comboTarifaTab.setBackground(Color.WHITE);
        comboDescuentoTab.setBackground(Color.WHITE);
        comboPeriodicidad.setBackground(Color.WHITE);
        comboPago.setBackground(Color.WHITE);


        // 3. Asignamos cada desplegable a su columna
        tabla.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboTarifaTab));    // Tarifa
        tabla.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboPeriodicidad)); // Periodicidad
        tabla.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboDescuentoTab)); // Descuento
        tabla.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboPago));        // F. Pago


        // Columna 4 es Periodicidad
        tabla.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboPeriodicidad));

        // Columna 6 es Forma de Pago
        tabla.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboPago));


        // Detectar doble clic para editar
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tabla.getSelectedRow();
                    if (fila != -1) {
                        int filaModelo = tabla.convertRowIndexToModel(fila);
                        // Permitimos editar directamente en la celda
                        JOptionPane.showMessageDialog(null, "Ya puedes editar la celda. Pulsa ENTER al acabar para guardar.");
                    }
                }
            }
        });

        // Detectar cuando el usuario marca o desmarca el Check de "Estado"
        modelo.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();

                if (columna == 2) {
                    boolean estaActivo = (boolean) modelo.getValueAt(fila, columna);
                    int numSocio = (int) modelo.getValueAt(fila, 0);

                    // 1. Actualizamos la BD
                    actualizarEstadoSocioEnBD(numSocio, estaActivo);

                    // 2. Avisamos a la Ventana Principal
                    Window win = SwingUtilities.getWindowAncestor(this);
                    if (win instanceof VentanaPrincipal) {
                        ((VentanaPrincipal) win).actualizarContador();
                    }

                    // --- AÑADE ESTO AQUÍ PARA EL CAMBIO DE COLOR INSTANTÁNEO ---
                    tabla.repaint();
                }
            }
        });



        // Configuramos el filtro
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        // Evento para filtrar mientras escribes
        txtBuscador.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = txtBuscador.getText();
                if (texto.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
                }
            }
        });


        // --- EVITA QUE LAS COLUMNAS SE APLASTEN Y PERMITE QUE CREZCAN ---
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tabla.setRowHeight(30);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // 3. CARGAR DATOS (Esto llamará internamente al auto-ajuste de columnas)
        cargarDatos();

        // 4. PANEL DE DESPLAZAMIENTO (Scroll)
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));

        // Detectar cuando el usuario marca o desmarca el Check de "Estado"
        modelo.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();

                // La columna 2 es la del Check (Estado)
                if (columna == 2) {
                    boolean estaActivo = (boolean) modelo.getValueAt(fila, columna);
                    int numSocio = (int) modelo.getValueAt(fila, 0); // Sacamos el Nº de socio

                    // 1. Actualizamos la Base de Datos
                    actualizarEstadoSocioEnBD(numSocio, estaActivo);

                    // 2. Avisamos a la Ventana Principal para que refresque el contador
                    Window win = SwingUtilities.getWindowAncestor(this);
                    if (win instanceof VentanaPrincipal) {
                        ((VentanaPrincipal) win).actualizarContador();
                    }
                }
            }
        });


        // Forzamos que siempre pueda haber scroll horizontal si es necesario
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll, BorderLayout.CENTER);

        // --- MENÚ CON BOTÓN DERECHO ---
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemEliminar = new JMenuItem("Eliminar Socio definitivamente");
        itemEliminar.setForeground(Color.RED); // Color rojo para advertir
        popupMenu.add(itemEliminar);

        // Seleccionar la fila automáticamente al hacer clic derecho
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int r = tabla.rowAtPoint(e.getPoint());
                if (r >= 0 && r < tabla.getRowCount()) {
                    tabla.setRowSelectionInterval(r, r);
                } else {
                    tabla.clearSelection();
                }
            }
        });


        // Hacer que el menú aparezca al hacer clic derecho
        tabla.setComponentPopupMenu(popupMenu);

        // Acción del botón Eliminar
        itemEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila != -1) {
                int filaModelo = tabla.convertRowIndexToModel(fila);
                int numSocio = (int) modelo.getValueAt(filaModelo, 0);
                String nombre = (String) modelo.getValueAt(filaModelo, 1);

                // Confirmación antes de borrar
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro de que quieres eliminar a " + nombre + "?\nEsta acción no se puede deshacer.",
                        "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (respuesta == JOptionPane.YES_OPTION) {
                    eliminarSocioDeBD(numSocio);
                    cargarDatos(); // Recargar la tabla

                    // Actualizar el contador de la ventana principal
                    Window win = SwingUtilities.getWindowAncestor(this);
                    if (win instanceof VentanaPrincipal) {
                        ((VentanaPrincipal) win).actualizarContador();
                    }
                }
            }
        });


    }


    private void ajustarAnchoColumnas() {
        TableColumnModel columnModel = tabla.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);  // Nº
        columnModel.getColumn(1).setPreferredWidth(200); // Nombre
        columnModel.getColumn(2).setPreferredWidth(60);  // Estado (Check)
        columnModel.getColumn(3).setPreferredWidth(70);  // Tarifa
        columnModel.getColumn(5).setPreferredWidth(50);  // Dto
        columnModel.getColumn(11).setPreferredWidth(180); // E-mail
    }

    private void cargarDatos() {
        // Limpiamos la tabla por si acaso
        modelo.setRowCount(0);

        String sql = "SELECT * FROM socios ORDER BY num_socio ASC";

        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = new Object[12];
                fila[0] = rs.getInt("num_socio");
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getInt("esta_activo") == 1;



                // --- CAMBIO AQUÍ: Quitamos los símbolos € y % ---
                // Convertimos a entero para que no salgan decimales como .0
                fila[3] = String.valueOf((int)rs.getDouble("tarifa"));
                fila[4] = rs.getString("periodicidad");
                fila[5] = String.valueOf((int)rs.getDouble("descuento"));
                // ------------------------------------------------

                fila[6] = rs.getString("forma_pago");
                fila[7] = rs.getString("dni");
                fila[8] = rs.getString("domicilio");
                fila[9] = rs.getString("fecha_nacimiento");
                fila[10] = rs.getString("telefono");
                fila[11] = rs.getString("email");

                modelo.addRow(fila);
            }


            autoAjustarColumnas();

        } catch (SQLException e) {
            System.out.println("Error al cargar listado: " + e.getMessage());
        }
    }

    private void autoAjustarColumnas() {
        for (int column = 0; column < tabla.getColumnCount(); column++) {
            int anchoMaximo = 70;

            // Medir ancho del título
            int anchoTitulo = (int) tabla.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tabla, tabla.getColumnName(column), false, false, -1, column)
                    .getPreferredSize().getWidth();
            anchoMaximo = Math.max(anchoMaximo, anchoTitulo);

            // Medir cada fila
            for (int row = 0; row < tabla.getRowCount(); row++) {
                int anchoCelda = (int) tabla.getCellRenderer(row, column)
                        .getTableCellRendererComponent(tabla, tabla.getValueAt(row, column), false, false, row, column)
                        .getPreferredSize().getWidth();
                anchoMaximo = Math.max(anchoMaximo, anchoCelda);
            }

            // Aplicar con margen
            tabla.getColumnModel().getColumn(column).setPreferredWidth(anchoMaximo + 20);
        }
    }




    // Método auxiliar para no perder la fecha de alta al dar de baja
    private String obtenerFechaAltaActual(int numSocio) {
        String sql = "SELECT fecha_alta FROM socios WHERE num_socio = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, numSocio);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("fecha_alta");
        } catch (SQLException e) { e.printStackTrace(); }
        return java.time.LocalDate.now().toString();
    }

    private void actualizarDatoSocio(int fila, int col) {
        int numSocio = (int) modelo.getValueAt(fila, 0);
        Object nuevoValor = modelo.getValueAt(fila, col);
        String nombreColumna = "";

        switch(col) {
            case 1: nombreColumna = "nombre"; break;
            case 2:
                nombreColumna = "esta_activo";
                // Ahora recibimos un true/false del Checkbox, no la palabra "ALTA"
                nuevoValor = (boolean) nuevoValor ? 1 : 0;
                break;
            case 3: nombreColumna = "tarifa"; break;
            case 4: nombreColumna = "periodicidad"; break;
            case 5: nombreColumna = "descuento"; break;
            case 6: nombreColumna = "forma_pago"; break;
            case 7: nombreColumna = "dni"; break;
            case 8: nombreColumna = "domicilio"; break;
            case 10: nombreColumna = "telefono"; break;
            case 11: nombreColumna = "email"; break;
        }

        if (!nombreColumna.isEmpty()) {
            String sql = "UPDATE socios SET " + nombreColumna + " = ? WHERE num_socio = ?";
            try (Connection conn = ConexionDB.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setObject(1, nuevoValor);
                pstmt.setInt(2, numSocio);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Error al actualizar " + nombreColumna + ": " + ex.getMessage());
            }
        }
    }


    private void actualizarEstadoSocioEnBD(int numSocio, boolean activo) {
        String sql = "UPDATE socios SET esta_activo = ?, fecha_alta = ?, fecha_baja = ? WHERE num_socio = ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hoy = java.time.LocalDate.now().toString();

            if (activo) {
                // ALTA
                pstmt.setInt(1, 1);
                pstmt.setString(2, hoy);
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            } else {
                // BAJA
                pstmt.setInt(1, 0);
                pstmt.setString(2, obtenerFechaAltaActual(numSocio));
                pstmt.setString(3, hoy);
            }

            pstmt.setInt(4, numSocio);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void eliminarSocioDeBD(int numSocio) {
        String sql = "DELETE FROM socios WHERE num_socio = ?";
        try (Connection conn = com.boxfire.db.ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, numSocio);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Socio eliminado correctamente.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage());
        }
    }

}
