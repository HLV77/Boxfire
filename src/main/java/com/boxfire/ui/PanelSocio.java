package com.boxfire.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import com.boxfire.db.ConexionDB;

public class PanelSocio extends JPanel {
    private JTextField txtNombre, txtDni, txtDomicilio, txtTelefono, txtEmail;
    private JComboBox<String> comboPeriodicidad, comboPago, comboTarifa;
    private JFormattedTextField txtFechaNac;
    private JComboBox<String> comboDescuento;

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

        // Estilo unificado para las etiquetas (Igual que F. Pago)
        Font labelFontGeneral = new JLabel().getFont();
        LineBorder bordeNegro = new LineBorder(Color.BLACK, 1);
        int alturaFija = 20;

        // Campos de texto con etiquetas modificadas
        agregarCampo(card, "Nombre del Socio:", txtNombre = new JTextField(), 0, gbc, labelFontGeneral, bordeNegro, 350, alturaFija);
        agregarCampo(card, "D.N.I/N.I.E:", txtDni = new JTextField(), 1, gbc, labelFontGeneral, bordeNegro, 110, alturaFija);
        configurarLimitadorDNI(txtDni);
        agregarCampo(card, "Domicilio:", txtDomicilio = new JTextField(), 2, gbc, labelFontGeneral, bordeNegro, 350, alturaFija);

        // Fecha de Nacimiento
        try {
            javax.swing.text.MaskFormatter mascaraFecha = new javax.swing.text.MaskFormatter("##/##/####");
            mascaraFecha.setPlaceholderCharacter('_');
            txtFechaNac = new JFormattedTextField(mascaraFecha);
        } catch (Exception e) {
            txtFechaNac = new JFormattedTextField();
        }
        agregarCampo(card, "F. Nacimiento:", txtFechaNac, 3, gbc, labelFontGeneral, bordeNegro, 110, alturaFija);
        agregarCampo(card, "Teléfono:", txtTelefono = new JTextField(), 4, gbc, labelFontGeneral, bordeNegro, 110, alturaFija);
        configurarLimitadorTelefono(txtTelefono);
        agregarCampo(card, "C. Electrónico:", txtEmail = new JTextField(), 5, gbc, labelFontGeneral, bordeNegro, 350, alturaFija);

        // --- SECCIÓN TARIFA ---
        comboTarifa = crearComboBlanco(new String[]{}, (LineBorder) BorderFactory.createLineBorder(Color.GRAY));
        comboTarifa.removeAllItems();
        comboTarifa.addItem("");
        com.boxfire.db.ConexionDB.rellenarComboTarifas(comboTarifa);

        JLabel lblTarifaTexto = new JLabel("Tarifa:");
        lblTarifaTexto.setFont(labelFontGeneral); // Modificado a estilo F. Pago
        gbc.gridy = 6;
        gbc.gridx = 0;
        card.add(lblTarifaTexto, gbc);

        JPanel pnlTarifaWrap = new JPanel(new BorderLayout(5, 0));
        pnlTarifaWrap.setOpaque(false);
        pnlTarifaWrap.setPreferredSize(new Dimension(80, 20));

        JLabel lblEuro = new JLabel(" €");
        lblEuro.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlTarifaWrap.add(comboTarifa, BorderLayout.CENTER);
        pnlTarifaWrap.add(lblEuro, BorderLayout.EAST);

        gbc.gridx = 1;
        card.add(pnlTarifaWrap, gbc);

        // --- FORMA DE PAGO ---
        gbc.gridy = 9;
        gbc.gridx = 0;
        JLabel lblPago = new JLabel("F. Pago:", SwingConstants.LEFT);
        lblPago.setFont(labelFontGeneral);
        card.add(lblPago, gbc);

        comboPago = crearComboBlanco(new String[]{"", "Efectivo/Tarjeta/Bizum", "Domiciliación"}, bordeNegro);
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

        Color colorHoverAlta = new Color(240, 238, 170);
        Color colorOriginalAlta = new Color(221, 216, 60);

        btnConfirmar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { btnConfirmar.setBackground(colorHoverAlta); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { btnConfirmar.setBackground(colorOriginalAlta); }
        });

        btnConfirmar.addActionListener(e -> {
            String soloNumeros = txtFechaNac.getText().replaceAll("[^0-9]", "").trim();
            String fechaParaSQL = null;

            if (!soloNumeros.isEmpty()) {
                String fechaLimpia = txtFechaNac.getText().replace("_", "").trim();
                try {
                    if (fechaLimpia.length() == 10) {
                        java.time.format.DateTimeFormatter v = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.time.LocalDate fechaValidada = java.time.LocalDate.parse(fechaLimpia, v);
                        fechaParaSQL = fechaValidada.toString();
                    } else {
                        JOptionPane.showMessageDialog(this, "⚠️ Por favor, complete la fecha o bórrela totalmente.");
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "⚠️ La fecha de nacimiento no es válida.");
                    return;
                }
            }

            String sql = "INSERT INTO socios (nombre, dni, domicilio, fecha_nacimiento, telefono, email, tarifa, forma_pago, esta_activo, fecha_alta) VALUES (?,?,?,?,?,?,?,?,?,?)";

            try (java.sql.Connection conn = com.boxfire.db.ConexionDB.conectar()) {
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, txtNombre.getText());
                pstmt.setString(2, txtDni.getText());
                pstmt.setString(3, txtDomicilio.getText());
                pstmt.setString(4, fechaParaSQL);
                pstmt.setString(5, txtTelefono.getText());
                pstmt.setString(6, txtEmail.getText());

                String selTarifa = comboTarifa.getSelectedItem().toString();
                pstmt.setDouble(7, selTarifa.isEmpty() ? 0 : Double.parseDouble(selTarifa));
                pstmt.setString(8, comboPago.getSelectedItem().toString());
                pstmt.setInt(9, 1);
                pstmt.setString(10, java.time.LocalDate.now().toString());

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Socio guardado correctamente.");
                actualizarInterfaz();
                limpiarCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar en BD: " + ex.getMessage());
            }
        });

        configurarSaltoEnter();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtDni.setText("");
        txtDomicilio.setText("");
        txtFechaNac.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        comboTarifa.setSelectedIndex(0);
        comboPago.setSelectedIndex(0);
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
        JLabel lbl = new JLabel(label);
        lbl.setFont(f); // Aplicamos la fuente unificada aquí
        p.add(lbl, gbc);
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
                if (nextText.length() > 9) return;
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
