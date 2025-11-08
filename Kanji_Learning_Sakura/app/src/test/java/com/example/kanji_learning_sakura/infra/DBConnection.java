package com.example.kanji_learning_sakura.infra;

import com.example.kanji_learning_sakura.config.DBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Cấp kết nối JDBC dùng DriverManager (đơn giản, đủ cho môn học) */
public final class DBConnection {
    private static volatile boolean driverLoaded = false;

    private DBConnection() {}

    /** Lấy 1 kết nối mới; dùng try-with-resources ở nơi gọi để tự đóng. */
    public static Connection getConnection() throws SQLException {
        loadDriverIfNeeded();
        return DriverManager.getConnection(
                DBConfig.jdbcUrl(),
                DBConfig.DB_USER,
                DBConfig.DB_PASSWORD
        );
    }

    /** Ping nhanh để kiểm tra cấu hình đúng hay chưa. */
    public static boolean ping() {
        try (Connection c = getConnection()) {
            return c.isValid(2);
        } catch (Exception e) {         // SQLException + mọi lỗi khác
            System.err.println("[DB][PING] " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();        // in full stacktrace ra console
            return false;
        }
    }

    private static void loadDriverIfNeeded() {
        if (!driverLoaded) {
            synchronized (DBConnection.class) {
                if (!driverLoaded) {
                    try {
                        Class.forName(DBConfig.DRIVER);
                        driverLoaded = true;
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("MySQL Driver không tìm thấy. Đã thêm dependency chưa?", e);
                    }
                }
            }
        }
    }
}
