package com.boxfire.ui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // 1. Llamamos al cobrador automático
        com.boxfire.db.ConexionDB.procesarDomiciliacionesAuto();
        // 2. Aquí sigue tu código para abrir el Splash o la VentanaPrincipal...


        // 1. Forzamos el escalado para que se vea igual en todos los monitores
        System.setProperty("sun.java2d.uiScale", "1.0");

        SwingUtilities.invokeLater(() -> {
            // 2. Creamos la pantalla de carga
            SplashScreen splash = new SplashScreen();

            // 3. Iniciamos el proceso en un hilo separado para que no se congele la imagen
            new Thread(() -> {
                // Preparamos la base de datos de Boxfire
                com.boxfire.db.ConexionDB.crearTablas();

                // Mostramos la animación de carga
                splash.animar();

                // 4. Cuando termina el Splash, abrimos la Ventana Principal
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            }).start();
        });
    }
}
