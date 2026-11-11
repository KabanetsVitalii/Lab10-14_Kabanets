package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {
    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/car_registry?useUnicode=true&characterEncoding=utf8");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }
}