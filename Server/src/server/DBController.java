package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBController {
	public static Connection conn;
	
	
	  /**
	   * Connects the server to the MySQL database.
	   * Creates a single shared connection stored in 'conn'.
	   * Prints a message on success or detailed SQL errors on failure.
	   */
	  public static void connectToDB() {
			 try {
				  conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/prototypedb?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Eden2701@");
				 System.out.println("SQL connection succeed");
			 }
			 catch(SQLException ex){
		         System.out.println("SQLException: " + ex.getMessage());
		         System.out.println("SQLState: " + ex.getSQLState());
		         System.out.println("VendorError: " + ex.getErrorCode());
			 }
	  }
	  
	  
	  /**
	   * Inserts a new order into the 'orders' table.
	   *
	   * @param msg An ArrayList<String> containing 6 fields of the order:
	   *            [order_number, order_date, number_of_guests,
	   *             confirmation_code, subscriber_id, date_of_placing_order].
	   *
	   * Uses PreparedStatement to safely insert the data into the database.
	   */
	  public static void insertOrderToDB(Object msg) {
		  ArrayList<String> list = (ArrayList<String>) msg;
		  try {
		  PreparedStatement ps = conn.prepareStatement("insert into orders values(?,?,?,?,?,?)");
	      ps.setInt(1, Integer.parseInt(list.get(0))); // order_number (PK)
	      ps.setDate(2, java.sql.Date.valueOf(list.get(1)));                // order_date
	      ps.setInt(3, Integer.parseInt(list.get(2))); // number_of_guests
	      ps.setInt(4, Integer.parseInt(list.get(3))); // confirmation_code
	      ps.setInt(5, Integer.parseInt(list.get(4))); // subscriber_id (FK)
	      ps.setDate(6, java.sql.Date.valueOf(list.get(5)));;                // date_of_placing_order
		  
		  ps.executeUpdate();
		  System.out.println("Order inserted.");
		  }
		  catch(SQLException e) {
			  e.printStackTrace();
		  }
	  }
	  
	  public static ArrayList<ArrayList<String>> getOrdersFromDB() {
		  
		  ArrayList<ArrayList<String>> orders = new ArrayList<>();
		  
		  try {
		        Statement stmt = conn.createStatement();
		        ResultSet rs = stmt.executeQuery("SELECT * FROM orders");
		        
		        while(rs.next()) {
		            ArrayList<String> row = new ArrayList<>();
		            row.add(String.valueOf(rs.getInt("order_number")));
		            row.add(String.valueOf(rs.getDate("order_date")));
		            row.add(String.valueOf(rs.getInt("number_of_guests")));
		            row.add(String.valueOf(rs.getInt("confirmation_code")));
		            row.add(String.valueOf(rs.getInt("subscriber_id")));
		            row.add(String.valueOf(rs.getDate("date_of_placing_order")));

		            orders.add(row);
		        }
		        rs.close();
		        stmt.close();
		        
		  }
		  catch(SQLException e) {
			  e.printStackTrace();
		  }
		  
		  return orders;
		  
	  }

}
