package app.dao;

import app.infra.DBConfig;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lớp tiện ích dùng chung cho tất cả DAO.
 */
public abstract class BaseDAO {

    /**
     * Lấy {@link Connection} mới từ {@link DBConfig}.
     *
     * @return kết nối JDBC mới.
     * @throws SQLException nếu không kết nối được tới MySQL.
     */
    protected Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }
}
