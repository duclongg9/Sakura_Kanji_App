package app.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Cấu hình JDBC tập trung cho backend servlet.
 * <p>
 * Đặt các thông số kết nối MySQL ở đây và không để rải rác trong code khác.
 * Có thể thay đổi bằng biến môi trường nếu cần triển khai thực tế.
 */
public final class DBConfig {

    private static final String JDBC_URL = System.getProperty(
            "KANJI_APP_JDBC_URL",
            "jdbc:mysql://localhost:3306/kanji_app?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC"
    );
    private static final String DB_USER = System.getProperty("KANJI_APP_DB_USER", "root");
    private static final String DB_PASSWORD = System.getProperty("KANJI_APP_DB_PASS", "123456");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConfig() {
    }

    /**
     * Lấy {@link Connection} tới cơ sở dữ liệu MySQL.
     *
     * @return connection đã sẵn sàng sử dụng.
     * @throws SQLException nếu không kết nối được.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
}
