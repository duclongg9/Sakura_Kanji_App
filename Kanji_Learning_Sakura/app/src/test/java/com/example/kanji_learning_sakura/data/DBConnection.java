package com.example.kanji_learning_sakura.data;

import com.example.kanji_learning_sakura.config.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Tiện ích JDBC chỉ sử dụng trong test để thao tác với cơ sở dữ liệu mẫu.
 */
public final class DBConnection {
    private static volatile boolean driverLoaded = false;

    private DBConnection() {}

    /**
     * Mở một kết nối JDBC mới tới cấu hình trong {@link DBConfig}.
     */
    public static Connection getConnection() throws SQLException {
        loadDriverIfNeeded();
        return DriverManager.getConnection(
                DBConfig.jdbcUrl(),
                DBConfig.DB_USER,
                DBConfig.DB_PASSWORD
        );
    }

    /**
     * Kiểm tra nhanh khả năng kết nối đến cơ sở dữ liệu MySQL.
     */
    public static boolean ping() {
        try (Connection c = getConnection()) {
            return c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Đảm bảo driver MySQL được nạp trước khi tạo kết nối.
     */
    private static void loadDriverIfNeeded() {
        if (!driverLoaded) {
            synchronized (DBConnection.class) {
                if (!driverLoaded) {
                    try {
                        Class.forName(DBConfig.DRIVER);
                        driverLoaded = true;
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Không tìm thấy MySQL Driver. Đã thêm dependency chưa?", e);
                    }
                }
            }
        }
    }
}
