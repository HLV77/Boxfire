package com.boxfire.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public SplashScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // --- AQUÍ ESTÁ LA LÍNEA DEL LOGO ---
        URL imgUrl = getClass().getResource("/Logo_Boxfire.JPG");


        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Image img = icon.getImage().getScaledInstance(350, 200, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(img));
            logoLabel.setBounds(125, 40, 350, 200);
            panel.add(logoLabel);
        }

        JLabel titleLabel = new JLabel("BOXFIRE - GESTIÓN DE GIMNASIO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setBounds(0, 260, 600, 40);
        panel.add(titleLabel);

        statusLabel = new JLabel("Iniciando sistema deportivo...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setBounds(0, 330, 600, 20);
        panel.add(statusLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(50, 360, 500, 6);
        progressBar.setForeground(new Color(221, 216, 60));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(240, 240, 240));
        panel.add(progressBar);

        setContentPane(panel);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    public void animar() {
        setVisible(true);
        try {
            for (int i = 0; i <= 100; i++) {
                Thread.sleep(10);
                progressBar.setValue(i);
                if (i == 30) statusLabel.setText("Verificando cuotas de socios...");
                if (i == 60) statusLabel.setText("Cargando base de datos Boxfire...");
                if (i == 100) {
                    Thread.sleep(500);
                    dispose();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
