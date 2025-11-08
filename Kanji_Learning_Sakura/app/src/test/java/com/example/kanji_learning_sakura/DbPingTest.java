package com.example.kanji_learning_sakura;

import com.example.kanji_learning_sakura.infra.DBConnection;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DbPingTest {

    @Test
    public void ping_mysql_and_query_sample() throws Exception {
        System.out.println("[DB] URL = " + com.example.kanji_learning_sakura.config.DBConfig.jdbcUrl());
        System.out.println("[DB] Bắt đầu ping MySQL...");
        boolean ok = DBConnection.ping();
        System.out.println(ok ? "[DB] ✅ Kết nối thành công!" : "[DB] ❌ Kết nối thất bại!");
        assertTrue("Ping DB thất bại", ok);

        }
    }

