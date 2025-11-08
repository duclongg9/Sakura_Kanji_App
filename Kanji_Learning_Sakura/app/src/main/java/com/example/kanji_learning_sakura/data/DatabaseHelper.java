package com.example.kanji_learning_sakura.data;

import com.example.kanji_learning_sakura.config.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Tiện ích mở kết nối JDBC tới MySQL dựa trên {@link DBConfig}.
 */
public final class DatabaseHelper {
    private static volatile boolean driverLoaded = false;

    private DatabaseHelper() {
    }

    /**
     * Đảm bảo driver MySQL chỉ được nạp một lần duy nhất.
     *
     * @throws ClassNotFoundException khi thiếu dependency mysql-connector-j.
     */
    private static void ensureDriverLoaded() throws ClassNotFoundException {
        if (!driverLoaded) {
            synchronized (DatabaseHelper.class) {
                if (!driverLoaded) {
                    Class.forName(DBConfig.DRIVER);
                    driverLoaded = true;
                }
            }
        }
    }

    /**
     * Tạo mới một {@link Connection} tới database cấu hình trong {@link DBConfig}.
     * Caller chịu trách nhiệm đóng kết nối sau khi sử dụng xong.
     *
     * @return {@link Connection} mới tới MySQL.
     * @throws SQLException           nếu không thể kết nối tới database.
     * @throws ClassNotFoundException nếu driver MySQL chưa được thêm vào project.
     */
    public static Connection openConnection() throws SQLException, ClassNotFoundException {
        ensureDriverLoaded();
        return DriverManager.getConnection(DBConfig.jdbcUrl(), DBConfig.DB_USER, DBConfig.DB_PASSWORD);
    }
}
