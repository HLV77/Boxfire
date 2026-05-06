package com.boxfire.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VentanaPrincipal extends JFrame {



    private JLabel lblSocios;
    private JLabel lblIngresosMes;
    private JLabel lblIngresosAnio; // <--- Nueva variable



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


        // --- CENTRO: Contador de Socios ---
        JPanel panelCentralContador = new JPanel(new GridBagLayout());
        panelCentralContador.setOpaque(false);

        lblSocios = new JLabel("Socios activos: " + com.boxfire.db.ConexionDB.obtenerTotalSociosActivos());
        lblSocios.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSocios.setForeground(new Color(50, 50, 50));
        panelCentralContador.add(lblSocios);
        header.add(panelCentralContador, BorderLayout.CENTER);

// --- DERECHA: Resumen de Caja (Mes y Año) ---
        JPanel panelDerechoIngresos = new JPanel(new GridLayout(2, 1));
        panelDerechoIngresos.setOpaque(false);
        panelDerechoIngresos.setPreferredSize(new Dimension(250, 70));
        panelDerechoIngresos.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));

        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int mesActual = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);

        lblIngresosMes = new JLabel(meses[mesActual] + ": " + String.format("%.2f €", com.boxfire.db.ConexionDB.obtenerIngresosMesActual()), SwingConstants.RIGHT);
        lblIngresosMes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIngresosMes.setForeground(new Color(39, 174, 96)); // Verde

        lblIngresosAnio = new JLabel("Total Año: " + String.format("%.2f €", com.boxfire.db.ConexionDB.obtenerIngresosAnioActual()), SwingConstants.RIGHT);
        lblIngresosAnio.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblIngresosAnio.setForeground(new Color(41, 128, 185)); // Azul

        panelDerechoIngresos.add(lblIngresosMes);
        panelDerechoIngresos.add(lblIngresosAnio);
        header.add(panelDerechoIngresos, BorderLayout.EAST);


        add(header, BorderLayout.NORTH);

        // 3. Menú Lateral (Sidebar)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(GRIS_FONDO);
        sidebar.setPreferredSize(new Dimension(220, 800));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        String[] opciones = {"Alta de Socio", "Listado de Socios", "Cobros Mensuales", "Control de Impagos" , "Estadisticas"};
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
            } else if (texto.equals("Control de Impagos")) {
                PanelImpagos pi = new PanelImpagos();
                pi.cargarImpagos();
                actualizarPanelCentral(pi);
            } else if (texto.equals("Estadisticas")) { // <--- AÑADE ESTO
                actualizarPanelCentral(new PanelEstadisticas());
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
        // 1. Refrescar Socios
        int total = com.boxfire.db.ConexionDB.obtenerTotalSociosActivos();
        lblSocios.setText("Socios activos: " + total);

        // 2. Refrescar Mes
        double ingresosMes = com.boxfire.db.ConexionDB.obtenerIngresosMesActual();
        int mesIdx = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        lblIngresosMes.setText(meses[mesIdx] + ": " + String.format("%.2f €", ingresosMes));

        // 3. Refrescar Año
        double ingresosAnio = com.boxfire.db.ConexionDB.obtenerIngresosAnioActual();
        lblIngresosAnio.setText("Total Año: " + String.format("%.2f €", ingresosAnio));
    }



}
