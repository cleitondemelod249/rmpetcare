package com.rmpetcare;

import java.sql.*;

public class DBControl {
	
	private static String URL = "jdbc:mysql://localhost:3306/";
	private static String DATABASE = "spacebox";
	private static String LOGIN = "root";
	private static String PASSWD = "";
	private static Connection CONN;
	private static Statement STMT;
	private static ResultSet RS;
	
	public static Boolean startConnection() {
		Boolean isConnect = false;
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			CONN = DriverManager.getConnection(URL + DATABASE, LOGIN, PASSWD);
			STMT = CONN.createStatement();
			isConnect = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return isConnect;
	}
	
	public static Boolean executeQuery(String strQuery) {
		Boolean isExecuted = false;
		try {
			STMT.executeQuery(strQuery);
			isExecuted = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isExecuted;
	}
	
	public static Boolean selectOnQuery(String strQuery) {
		
		Boolean isExecuted = false;
		try {
			RS = STMT.executeQuery(strQuery);
			while(RS.next()) {
				//Do something...
			}
			isExecuted = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isExecuted;
	}

}
