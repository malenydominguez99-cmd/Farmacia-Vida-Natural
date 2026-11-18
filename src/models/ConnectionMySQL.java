package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConnectionMySQL {

    private String database_name = "pharmacy_database";
    private String user = "root";
    private String password = "Pa$$w0rd";
    private String url = "jdbc:mysql://localhost:3306/" + database_name;

    Connection conn = null;

    // Cargar las credenciales desde un archivo local
    public ConnectionMySQL() {
        Properties config = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/models/config.properties")) {
            if (input == null) {
                throw new IOException("No se encontró config.properties");
            }

            config.load(input);

            user = config.getProperty("db.user");
            password = config.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la configuración de MySQL", e);
        }
    }

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection(url, user, password);

        } catch (ClassNotFoundException e) {
            System.err.println("Ha ocurrido un ClassNotFoundException: " + e.getMessage());

        } catch (SQLException e) {
            System.err.println("Ha ocurrido un SQLException: " + e.getMessage());
        }

        return conn;
    }
}
