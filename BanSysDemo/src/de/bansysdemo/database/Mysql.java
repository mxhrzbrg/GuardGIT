package de.bansysdemo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mysql {
	static String username = "xxxx";
	static String password = "xxxx";
	static String url = "jdbc:mysql://localhost:3306/";
	public static Connection connection;

	public static void connectMysql() {
		if (connection != null) {
			return;
		}
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void disconnectMysql() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createTables() {
		try {
			connection.prepareStatement("CREATE TABLE IF NOT EXISTS playerDB (uuid varchar(64), name varchar(64), ip varchar(64), banstate varchar(15), banexpiration long, reason varchar(64), log varchar(100), UNIQUE (uuid));").executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
