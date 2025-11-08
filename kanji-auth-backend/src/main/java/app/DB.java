/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
  static {
    try { Class.forName("com.mysql.cj.jdbc.Driver"); }
    catch (ClassNotFoundException e) { throw new RuntimeException(e); }
  }
  public static Connection get() {
    try {
      String url  = "jdbc:mysql://localhost:3306/kanji_app?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
      String user = "root";                // <=== sửa
      String pass = "123456";           // <=== sửa
      return DriverManager.getConnection(url, user, pass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

