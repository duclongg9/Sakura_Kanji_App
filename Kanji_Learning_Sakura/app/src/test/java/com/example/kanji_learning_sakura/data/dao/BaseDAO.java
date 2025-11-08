package com.example.kanji_learning_sakura.data.dao;

import com.example.kanji_learning_sakura.data.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

/** DAO cơ sở: nơi các DAO khác kế thừa để có sẵn getConnection() */
public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
}
