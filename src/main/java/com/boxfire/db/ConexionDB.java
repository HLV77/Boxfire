package com.boxfire.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.time.LocalDate;



public class ConexionDB {
    private static final String URL = "jdbc:sqlite:boxfire.db";

    public static Connection conectar() {
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
                "email TEXT, " +            // <--- Aquí estaba el error
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
                "FOREIGN KEY(num_socio) REFERENCES socios(num_socio)" +
                ");";
        try (Connection conn = conectar()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sqlSocios);
            stmt.execute(sqlPagos);

            // Añade esta línea aquí abajo, justo después de crear las tablas
            try {
                stmt.execute("ALTER TABLE pagos ADD COLUMN metodo_pago TEXT");
            } catch (SQLException e) {
                // Ignoramos el error si la columna ya existe
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }



        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlSocios);
            stmt.execute(sqlPagos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void procesarDomiciliacionesAuto() {
        LocalDate hoy = LocalDate.now();

        // Si es día 8 o más
        if (hoy.getDayOfMonth() >= 8) {
            int mes = hoy.getMonthValue();
            int anio = hoy.getYear();

            // SQL AJUSTADO: Ahora busca por tu columna 'forma_pago'
            String sql = "INSERT INTO pagos (num_socio, mes, anio, cuota_pagada, estado_pago) " +
                    "SELECT num_socio, ?, ?, tarifa, 1 FROM socios " +
                    "WHERE esta_activo = 1 AND forma_pago = 'Domiciliación' " +
                    "AND num_socio NOT IN (SELECT num_socio FROM pagos WHERE mes = ? AND anio = ?)";

            try (Connection conn = conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mes);
                pstmt.setInt(2, anio);
                pstmt.setInt(3, mes);
                pstmt.setInt(4, anio);

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Se han procesado " + filas + " cobros por domiciliación.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    public static int obtenerTotalSociosActivos() {
        String sql = "SELECT COUNT(*) FROM socios WHERE esta_activo = 1";
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error al contar socios: " + e.getMessage());
        }
        return 0;
    }

}
