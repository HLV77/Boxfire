package com.boxfire.db;

import java.sql.*;
import java.time.LocalDate;
import java.io.File;
import javax.swing.JComboBox;

public class ConexionDB {
    // RUTA FIJA: Fuera de la carpeta del proyecto para que no se borren los datos
    private static final String URL = "jdbc:sqlite:C:/Boxfire/boxfire.db";

    public static Connection conectar() {
        // Aseguramos que la carpeta C:/Boxfire existe físicamente
        File carpeta = new File("C:/Boxfire");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conn;
    }

    public static void crearTablas() {
        String sqlSocios = "CREATE TABLE IF NOT EXISTS socios (" +
                "num_socio INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "dni TEXT, " +
                "domicilio TEXT, " +
                "fecha_nacimiento TEXT, " +
                "telefono TEXT, " +
                "email TEXT, " +
                "tarifa REAL, " +
                "periodicidad TEXT, " +
                "descuento REAL, " +
                "forma_pago TEXT, " +
                "esta_activo INTEGER DEFAULT 1, " +
                "fecha_alta TEXT, " +
                "fecha_baja TEXT, " +
                "es_domiciliado INTEGER DEFAULT 0" +
                ");";

        String sqlPagos = "CREATE TABLE IF NOT EXISTS pagos (" +
                "id_pago INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "num_socio INTEGER, " +
                "mes INTEGER, " +
                "anio INTEGER, " +
                "cuota_pagada REAL, " +
                "estado_pago INTEGER, " +
                "metodo_pago TEXT, " +
                "FOREIGN KEY(num_socio) REFERENCES socios(num_socio)" +
                ");";

        String sqlConfigTarifas = "CREATE TABLE IF NOT EXISTS configuracion_tarifas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "valor TEXT NOT NULL);";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlSocios);
            stmt.execute(sqlPagos);
            stmt.execute(sqlConfigTarifas);

            // Insertar tarifas por defecto solo si la tabla está vacía
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM configuracion_tarifas");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO configuracion_tarifas (valor) VALUES ('45'), ('50'), ('55'), ('60')");
            }

            // Asegurar columna metodo_pago por compatibilidad
            try {
                stmt.execute("ALTER TABLE pagos ADD COLUMN metodo_pago TEXT");
            } catch (SQLException e) {
                // Ya existe la columna
            }

            System.out.println("✅ Base de datos en C:/Boxfire lista.");

        } catch (SQLException e) {
            System.out.println("❌ Error al crear tablas: " + e.getMessage());
        }
    }

    public static void procesarDomiciliacionesAuto() {
        LocalDate hoy = LocalDate.now();
        if (hoy.getDayOfMonth() >= 8) {
            int mes = hoy.getMonthValue();
            int anio = hoy.getYear();

            String sql = "INSERT INTO pagos (num_socio, mes, anio, cuota_pagada, estado_pago, metodo_pago) " +
                    "SELECT num_socio, ?, ?, tarifa, 1, 'Domiciliación' FROM socios " +
                    "WHERE esta_activo = 1 AND forma_pago = 'Domiciliación' " +
                    "AND num_socio NOT IN (SELECT num_socio FROM pagos WHERE mes = ? AND anio = ?)";

            try (Connection conn = conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mes);
                pstmt.setInt(2, anio);
                pstmt.setInt(3, mes);
                pstmt.setInt(4, anio);
                int filas = pstmt.executeUpdate();
                if (filas > 0) System.out.println("✅ Se han procesado " + filas + " domiciliaciones.");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static int obtenerTotalSociosActivos() {
        String sql = "SELECT COUNT(*) FROM socios WHERE esta_activo = 1";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static double obtenerIngresosMesActual() {
        int mes = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1;
        int anio = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        return obtenerSuma("SELECT SUM(cuota_pagada) FROM pagos WHERE mes = ? AND anio = ?", mes, anio);
    }

    public static double obtenerIngresosAnioActual() {
        int anio = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        return obtenerSuma("SELECT SUM(cuota_pagada) FROM pagos WHERE anio = ?", anio, -1);
    }

    private static double obtenerSuma(String sql, int p1, int p2) {
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, p1);
            if (p2 != -1) pstmt.setInt(2, p2);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static void rellenarComboTarifas(JComboBox<String> combo) {
        String sql = "SELECT valor FROM configuracion_tarifas ORDER BY CAST(valor AS INTEGER) ASC";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) combo.addItem(rs.getString("valor"));
        } catch (SQLException e) {
            System.out.println("Error al rellenar combo: " + e.getMessage());
            combo.addItem("45");
        }
    }
}
