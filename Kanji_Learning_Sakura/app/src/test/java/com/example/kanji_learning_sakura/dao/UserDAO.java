package com.example.kanji_learning_sakura.dao;

import java.sql.*;

public class UserDAO extends BaseDAO {

    public Integer findRoleIdByUserId(long userId) throws SQLException {
        String sql = "SELECT roleId FROM User WHERE id = ?";
        try (var c = getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("roleId");
                return null;
            }
        }
    }
}
