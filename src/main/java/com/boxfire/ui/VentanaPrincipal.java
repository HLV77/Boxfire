package com.boxfire.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VentanaPrincipal extends JFrame {


        private JLabel lblSocios; // <--- Ponla aquí arriba
        // ... el resto de tus variables

        private JButton botonSeleccionado = null;
    private final Color AMARILLO_BOXFIRE = new Color(221, 216, 60);
    private final Color GRIS_FONDO = new Color(242, 242, 242);



    public VentanaPrincipal() {
        // 1. Configuración de la Ventana
        setTitle("BOXFIRE - Gestión de Gimnasio");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());

        // 2. Barra Superior (Header)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GRIS_FONDO);
        header.setPreferredSize(new Dimension(1200, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // --- IZQUIERDA: Usuario ---
        JPanel panelIzquierdo = new JPanel(new GridLayout(2, 1));
        panelIzquierdo.setOpaque(false);
        panelIzquierdo.setPreferredSize(new Dimension(300, 70));

        JLabel tituloHeader = new JLabel("  PANEL DE CONTROL BOXFIRE");
        tituloHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel usuario = new JLabel("  Usuario: José Andrés Luján Beas");
        usuario.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        usuario.setForeground(new Color(100, 100, 100));

        panelIzquierdo.add(tituloHeader);
        panelIzquierdo.add(usuario);
        header.add(panelIzquierdo, BorderLayout.WEST);


        // --- CENTRO: Marcador de Socios ---
        JPanel panelCentralContador = new JPanel(new GridBagLayout());
        panelCentralContador.setOpaque(false);

        // Llamamos a la base de datos para que nos de el número real al empezar
        int total = com.boxfire.db.ConexionDB.obtenerTotalSociosActivos();
        lblSocios = new JLabel("Nº de socios activos (" + total + ")");

        lblSocios.setFont(new Font("Segoe UI", Font.BOLD, 17));
        panelCentralContador.add(lblSocios);
        header.add(panelCentralContador, BorderLayout.CENTER);

        // --- DERECHA: Espacio ---
        JPanel panelDerecho = new JPanel();
        panelDerecho.setOpaque(false);
        panelDerecho.setPreferredSize(new Dimension(300, 70));
        header.add(panelDerecho, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // 3. Menú Lateral (Sidebar)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(GRIS_FONDO);
        sidebar.setPreferredSize(new Dimension(220, 800));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        String[] opciones = {"Alta de Socio", "Listado de Socios", "Cobros Mensuales", "Control de Impagos"};
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        for (String opcion : opciones) {
            sidebar.add(crearBotonPersonalizado(opcion));
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        add(sidebar, BorderLayout.WEST);

        // 4. Área Central (Mensaje de bienvenida)
        JPanel contenidoInicial = new JPanel(new GridBagLayout()); // Usamos GridBagLayout para centrar
        contenidoInicial.setBackground(Color.WHITE);

        JLabel mensajeBienvenida = new JLabel("Bienvenido al Sistema de Gestión BOXFIRE");
        mensajeBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mensajeBienvenida.setForeground(new Color(80, 80, 80));

        contenidoInicial.add(mensajeBienvenida);
        add(contenidoInicial, BorderLayout.CENTER);

        add(contenidoInicial, BorderLayout.CENTER);
    }

    private JButton crearBotonPersonalizado(String texto) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBackground(GRIS_FONDO);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160), 1));

        Color colorHover = new Color(240, 238, 170);



        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Quitamos el 'if' para que cambie SIEMPRE al pasar el ratón
                btn.setBackground(colorHover);
            } public void mouseExited(java.awt.event.MouseEvent e) {
                // Al salir, comprobamos: si es el seleccionado vuelve al amarillo,
                // si no, vuelve al gris de fondo.
                if (btn == botonSeleccionado) { btn.setBackground(AMARILLO_BOXFIRE);
                } else { btn.setBackground(GRIS_FONDO);
                }
            }
        });

        btn.addActionListener(e -> {
            if (botonSeleccionado != null)
                botonSeleccionado.setBackground(GRIS_FONDO);
            botonSeleccionado = btn;
            btn.setBackground(AMARILLO_BOXFIRE);

            // Gestión de navegación
            if (texto.equals("Alta de Socio")) {
                actualizarPanelCentral(new PanelSocio());
            } else if (texto.equals("Listado de Socios")) {
                actualizarPanelCentral(new PanelListadoSocios());
            } else if (texto.equals("Cobros Mensuales")) {
                actualizarPanelCentral(new PanelCobros());
            }
        });
        return btn;
    }




    private void actualizarPanelCentral(JPanel nuevoPanel) {
        BorderLayout layout = (BorderLayout) getContentPane().getLayout();
        Component centroActual = layout.getLayoutComponent(BorderLayout.CENTER);

        if (centroActual != null) {
            remove(centroActual);
        }

        // Ponemos el panel dentro de un JScrollPane
        JScrollPane scrollPane = new JScrollPane(nuevoPanel);

        // Configuramos para que el scroll solo aparezca si es necesario
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Quitamos el borde al scroll para que no afee el diseño
        scrollPane.setBorder(null);

        // Ajustamos la velocidad del scroll (por defecto es muy lenta)
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();

        if (nuevoPanel instanceof PanelSocio) {
            SwingUtilities.invokeLater(() -> ((PanelSocio) nuevoPanel).activarCursorNombre());
        }
    }

    public void actualizarContador() {
        int total = com.boxfire.db.ConexionDB.obtenerTotalSociosActivos();
        lblSocios.setText("Nº de socios activos (" + total + ")");
    }


}
