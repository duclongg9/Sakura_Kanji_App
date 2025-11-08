package com.example.kanji_learning_sakura.dao;

import com.example.kanji_learning_sakura.model.JLPTLevel;
import java.sql.*;
import java.util.*;

public class JLPTLevelDAO extends BaseDAO {

    public List<JLPTLevel> findAll() throws SQLException {
        String sql = "SELECT id, nameLevel FROM JLPTLevel ORDER BY id";
        try (var c = getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            List<JLPTLevel> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new JLPTLevel(
                        rs.getInt("id"),
                        rs.getString("nameLevel")
                ));
            }
            return list;
        }
    }

    public Optional<JLPTLevel> findById(int id) throws SQLException {
        String sql = "SELECT id, nameLevel FROM JLPTLevel WHERE id = ?";
        try (var c = getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new JLPTLevel(
                            rs.getInt("id"), rs.getString("nameLevel")));
                }
                return Optional.empty();
            }
        }
    }
}
