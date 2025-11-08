package com.example.kanji_learning_sakura.config;

/** Cấu hình DB (điền tên DB, user, password ở đây) */
public final class DBConfig {
    private DBConfig() {}

    // TODO: điền tên DB của bạn, ví dụ "kanji_app"
    public static final String DB_NAME = "kanji_app";         // ví dụ: "kanji_app"
    // TODO: điền user (root hoặc user khác)
    public static final String DB_USER = "root";         // ví dụ: "root"
    // TODO: điền password
    public static final String DB_PASSWORD = "123456";     // ví dụ: "123456"

    // Host/Port có thể sửa khi cần
    public static final String HOST = "localhost";
    public static final int    PORT = 3306;

    /** JDBC URL đã kèm múi giờ & Unicode */
    public static String jdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                HOST, PORT, DB_NAME
        );
    }

    /** Tên driver MySQL 8+ */
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
}
