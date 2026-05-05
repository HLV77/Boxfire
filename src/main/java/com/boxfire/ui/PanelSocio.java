package com.boxfire.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class PanelSocio extends JPanel {
    private JTextField txtNombre, txtDni, txtDomicilio, txtTelefono, txtEmail;
    private JComboBox<String> comboPeriodicidad, comboPago, comboTarifa;
    private JFormattedTextField txtFechaNac;
    private JComboBox<String> comboDescuento; // Ahora es un combo


    public PanelSocio() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));


        GridBagConstraints gbcPrincipal = new GridBagConstraints();
        gbcPrincipal.gridx = 0;
        gbcPrincipal.anchor = GridBagConstraints.CENTER;

        // 1. TÍTULO
        JLabel titulo = new JLabel("FORMULARIO DE REGISTRO");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        gbcPrincipal.gridy = 0;
        gbcPrincipal.insets = new Insets(5, 0, 5, 0);
        add(titulo, gbcPrincipal);

        // 2. CUADRO BLANCO (CARD)
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        LineBorder bordeNegro = new LineBorder(Color.BLACK, 1);
        int alturaFija = 20;

        // Campos de texto
        agregarCampo(card, "Nombre del Socio:", txtNombre = new JTextField(), 0, gbc, labelFont, bordeNegro, 350, alturaFija);
        agregarCampo(card, "D.N.I/N.I.E:", txtDni = new JTextField(), 1, gbc, labelFont, bordeNegro, 110, alturaFija);
        configurarLimitadorDNI(txtDni);
        agregarCampo(card, "Domicilio:", txtDomicilio = new JTextField(), 2, gbc, labelFont, bordeNegro, 350, alturaFija);


        // Fecha de Nacimiento con Máscara
        try {
            javax.swing.text.MaskFormatter mascaraFecha = new javax.swing.text.MaskFormatter("##/##/####");
            mascaraFecha.setPlaceholderCharacter('_');
            txtFechaNac = new JFormattedTextField(mascaraFecha);
        } catch (Exception e) {
            txtFechaNac = new JFormattedTextField();
        }
        agregarCampo(card, "F. Nacimiento:", txtFechaNac, 3, gbc, labelFont, bordeNegro, 110, alturaFija);
        agregarCampo(card, "Teléfono:", txtTelefono = new JTextField(), 4, gbc, labelFont, bordeNegro, 110, alturaFija);
        configurarLimitadorTelefono(txtTelefono);
        agregarCampo(card, "C. Electrónico:", txtEmail = new JTextField(), 5, gbc, labelFont, bordeNegro, 350, alturaFija);


        // Tarifa alineada con €
        gbc.gridy = 6;
        gbc.gridx = 0;
        card.add(new JLabel("Tarifa:"), gbc);

        // Creamos un panel con ancho fijo para que no se desplace
        JPanel pnlTarifaWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTarifaWrap.setOpaque(false);
        pnlTarifaWrap.setPreferredSize(new Dimension(150, alturaFija)); // Ancho suficiente para combo + €

        String[] precios = {"", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90"};
        comboTarifa = crearComboBlanco(precios, bordeNegro);
        comboTarifa.setPreferredSize(new Dimension(110, alturaFija)); // Mismo ancho que el DNI

        pnlTarifaWrap.add(comboTarifa);
        pnlTarifaWrap.add(new JLabel("  €"));

        gbc.gridx = 1;
        card.add(pnlTarifaWrap, gbc);


        // Periodicidad
        gbc.gridy = 7;
        gbc.gridx = 0;
        card.add(new JLabel("Periodicidad:", SwingConstants.LEFT), gbc);
        comboPeriodicidad = crearComboBlanco(new String[]{"", "Mensual", "Trimestral", "Semestral", "Anual"}, bordeNegro);
        comboPeriodicidad.setPreferredSize(new Dimension(120, alturaFija));
        gbc.gridx = 1;
        card.add(comboPeriodicidad, gbc);

        // Descuento alineado con Meses
        gbc.gridy = 8;
        gbc.gridx = 0;
        card.add(new JLabel("Descuento:"), gbc);

        JPanel pnlDescWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlDescWrap.setOpaque(false);
        pnlDescWrap.setPreferredSize(new Dimension(200, alturaFija)); // Más ancho para la palabra "Meses"

        String[] opcionesDesc = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        comboDescuento = crearComboBlanco(opcionesDesc, bordeNegro);
        comboDescuento.setPreferredSize(new Dimension(110, alturaFija)); // Mismo ancho que el DNI

        pnlDescWrap.add(comboDescuento);
        pnlDescWrap.add(new JLabel("  Meses"));

        gbc.gridx = 1;
        card.add(pnlDescWrap, gbc);


        // Forma de Pago
        gbc.gridy = 9;
        gbc.gridx = 0;
        card.add(new JLabel("F. Pago:", SwingConstants.LEFT), gbc);
        comboPago = crearComboBlanco(new String[]{"", "Efectivo", "Tarjeta", "Domiciliación", "Bizum"}, bordeNegro);
        comboPago.setPreferredSize(new Dimension(170, alturaFija));
        gbc.gridx = 1;
        card.add(comboPago, gbc);

        gbcPrincipal.gridy = 1;
        add(card, gbcPrincipal);

        // 3. BOTÓN CONFIRMAR
        JButton btnConfirmar = new JButton("CONFIRMAR ALTA");
        btnConfirmar.setBackground(new Color(221, 216, 60));
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirmar.setBorder(new LineBorder(Color.BLACK, 1));
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.setPreferredSize(new Dimension(150, 30));

        gbcPrincipal.gridy = 2;
        gbcPrincipal.insets = new Insets(20, 0, 40, 0);
        add(btnConfirmar, gbcPrincipal);

        // --- EFECTO HOVER PARA EL BOTÓN ---
        Color colorHoverAlta = new Color(240, 238, 170);
        Color colorOriginalAlta = new Color(221, 216, 60);

        btnConfirmar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnConfirmar.setBackground(colorHoverAlta);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnConfirmar.setBackground(colorOriginalAlta);
            }
        });


        btnConfirmar.addActionListener(e -> {
            // 1. Extraemos solo los números para saber si el usuario escribió algo real
            String soloNumeros = txtFechaNac.getText().replaceAll("[^0-9]", "").trim();
            String fechaParaSQL = null;

            // 2. Si NO hay números, ignoramos el campo y guardamos (sin avisos)
            if (!soloNumeros.isEmpty()) {
                String fechaLimpia = txtFechaNac.getText().replace("_", "").trim();

                try {
                    // Si tiene algo, tiene que ser una fecha completa (10 caracteres incluyendo /)
                    if (fechaLimpia.length() == 10) {
                        DateTimeFormatter v = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.time.LocalDate fechaValidada = java.time.LocalDate.parse(fechaLimpia, v);
                        fechaParaSQL = fechaValidada.toString();
                    } else {
                        // Si escribió algún número pero no terminó la fecha
                        JOptionPane.showMessageDialog(this, "⚠️ Por favor, complete la fecha o bórrela totalmente.");
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "⚠️ La fecha no es válida.");
                    return;
                }
            }

            // 2. Guardar en BD y generar cobros automáticos
            try (java.sql.Connection conn = com.boxfire.db.ConexionDB.conectar()) {
                // Añadimos RETURN_GENERATED_KEYS para saber qué ID le da la base de datos
                String sql = "INSERT INTO socios (nombre, dni, domicilio, fecha_nacimiento, telefono, email, tarifa, periodicidad, descuento, forma_pago, esta_activo, fecha_alta) VALUES (?,?,?,?,?,?,?,?,?,?,1,?)";
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);

                pstmt.setString(1, txtNombre.getText());
                pstmt.setString(2, txtDni.getText());
                pstmt.setString(3, txtDomicilio.getText());
                pstmt.setString(4, fechaParaSQL);
                pstmt.setString(5, txtTelefono.getText());
                pstmt.setString(6, txtEmail.getText());

                double tarifa = Double.parseDouble(comboTarifa.getSelectedItem().toString().isEmpty() ? "0" : comboTarifa.getSelectedItem().toString());
                pstmt.setDouble(7, tarifa);

                String periodicidad = comboPeriodicidad.getSelectedItem().toString();
                pstmt.setString(8, periodicidad);

                int mesesRegalo = Integer.parseInt(comboDescuento.getSelectedItem().toString());
                pstmt.setDouble(9, mesesRegalo);

                pstmt.setString(10, comboPago.getSelectedItem().toString());
                pstmt.setString(11, java.time.LocalDate.now().toString());

                pstmt.executeUpdate();

                // --- NUEVO: GENERAR PAGOS AUTOMÁTICOS AL ALTA ---
                int idSocio = 0;
                try (java.sql.ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) idSocio = rs.getInt(1);
                }

                // --- BLOQUE DE PAGOS AUTOMÁTICOS ---
                if (idSocio > 0) {
                    // (Asegúrate de que estas variables ya estén declaradas arriba en el método)
                    periodicidad = comboPeriodicidad.getSelectedItem().toString();
                    tarifa = Double.parseDouble(comboTarifa.getSelectedItem().toString().isEmpty() ? "0" : comboTarifa.getSelectedItem().toString());
                    mesesRegalo = Integer.parseInt(comboDescuento.getSelectedItem().toString());

                    int mesesPack = switch (periodicidad) {
                        case "Trimestral" -> 3;
                        case "Semestral" -> 6;
                        case "Anual" -> 12;
                        default -> 1;
                    };

                    int mesesDePagoReal = mesesPack - mesesRegalo;
                    if (mesesDePagoReal < 1) mesesDePagoReal = 1;

                    java.time.LocalDate fechaVence = java.time.LocalDate.now();
                    String sqlPagos = "INSERT INTO pagos (num_socio, mes, anio, cuota_pagada, estado_pago) VALUES (?,?,?,?,1)";

                    try (java.sql.PreparedStatement pstmtPagos = conn.prepareStatement(sqlPagos)) {
                        for (int i = 0; i < mesesPack; i++) {
                            pstmtPagos.setInt(1, idSocio);
                            pstmtPagos.setInt(2, fechaVence.getMonthValue());
                            pstmtPagos.setInt(3, fechaVence.getYear());

                            // Repartimos la tarifa entre los meses que no son de regalo
                            double cuotaMes = (i < mesesDePagoReal) ? (tarifa / mesesDePagoReal) : 0.0;

                            pstmtPagos.setDouble(4, cuotaMes);
                            pstmtPagos.executeUpdate();
                            fechaVence = fechaVence.plusMonths(1);
                        }
                    }
                }



                JOptionPane.showMessageDialog(this, "✅ Socio guardado y pagos iniciales registrados correctamente.");

                actualizarInterfaz();
                limpiarCampos();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }

        });
        configurarSaltoEnter(); // Solo esta línea
    }// Y esta llave cierra el public PanelSocio()


    private void limpiarCampos() {
        txtNombre.setText(""); txtDni.setText(""); txtDomicilio.setText("");
        txtFechaNac.setText(""); txtTelefono.setText(""); txtEmail.setText("");
        comboDescuento.setSelectedIndex(0); comboTarifa.setSelectedIndex(0);
        comboPeriodicidad.setSelectedIndex(0); comboPago.setSelectedIndex(0);
        txtNombre.requestFocus();
    }

    private JComboBox<String> crearComboBlanco(String[] items, LineBorder borde) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(Color.WHITE);
        combo.setBorder(borde);
        combo.setUI(new BasicComboBoxUI());
        return combo;
    }

    private void agregarCampo(JPanel p, String label, JTextField tf, int fila, GridBagConstraints gbc, Font f, LineBorder borde, int ancho, int alto) {
        gbc.gridy = fila; gbc.gridx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        tf.setBorder(borde);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setPreferredSize(new Dimension(ancho, alto));
        p.add(tf, gbc);
    }

    public void activarCursorNombre() { txtNombre.requestFocusInWindow(); }

    private void configurarLimitadorDNI(JTextField campo) {
        ((javax.swing.text.AbstractDocument) campo.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                String next = fb.getDocument().getText(0, fb.getDocument().getLength()).substring(0, offset) + text;
                if (next.length() > 9) return;
                for (int i = 0; i < next.length() && i < 8; i++) if (!Character.isDigit(next.charAt(i))) return;
                if (next.length() == 9) {
                    if (!Character.isLetter(next.charAt(8))) return;
                    text = text.toUpperCase();
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    private void configurarLimitadorTelefono(JTextField campo) {
        ((javax.swing.text.AbstractDocument) campo.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nextText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                // 1. Solo permite máximo 9 caracteres
                if (nextText.length() > 9) return;

                // 2. Solo permite que sean números
                if (!text.matches("\\d*")) return;

                super.replace(fb, offset, length, text, attrs);
            }
        });
    }


    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        java.net.URL imgUrl = getClass().getResource("/Logo_Boxfire.jpg");
        if (imgUrl != null) {
            Image img = new ImageIcon(imgUrl).getImage();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = getWidth() - 160, y = 30;
            java.awt.geom.Ellipse2D.Double clip = new java.awt.geom.Ellipse2D.Double(x, y, 100, 100);
            g2.setClip(clip);
            g2.drawImage(img, x, y, 100, 100, this);
            g2.setClip(null);
            g2.setColor(Color.BLACK);
            g2.draw(clip);
            g2.dispose();
        }
    }

        private void configurarSaltoEnter() {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
                if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED && e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                    if (focusOwner instanceof javax.swing.text.JTextComponent || focusOwner instanceof JComboBox) {
                        // Si no es el combo de pago y está en este panel, salta
                        if (focusOwner != comboPago && SwingUtilities.isDescendingFrom(focusOwner, this)) {
                            focusOwner.transferFocus();
                            return true;
                        }
                    }
                }
                return false;
            });
        }

    private void actualizarInterfaz() {
        SwingUtilities.invokeLater(() -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof VentanaPrincipal) {
                ((VentanaPrincipal) win).actualizarContador();
            }
        });
    }



    }
