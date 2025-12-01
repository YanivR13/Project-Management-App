package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
	  public static void insertOrderToDB( Object msg) {
		  ArrayList<String> list = (ArrayList<String>) msg;
		  try {
		  PreparedStatement ps = conn.prepareStatement("insert into orders values(?,?,?,?,?,?)");
	      ps.setInt(1, Integer.parseInt(list.get(0))); // order_number (PK)
	      ps.setString(2, list.get(1));                // order_date
	      ps.setInt(3, Integer.parseInt(list.get(2))); // number_of_guests
	      ps.setInt(4, Integer.parseInt(list.get(3))); // confirmation_code
	      ps.setInt(5, Integer.parseInt(list.get(4))); // subscriber_id (FK)
	      ps.setString(6, list.get(5));                // date_of_placing_order
//		  ps.setString(1,list.get(0));
//		  ps.setString(2,list.get(1));
//		  ps.setString(3,list.get(2));
//		  ps.setString(4,list.get(3));
//		  ps.setString(5,list.get(4));
//		  ps.setString(6,list.get(5));
		  
		  ps.executeUpdate();
		  System.out.println("Order inserted.");
		  }
		  catch(SQLException e) {
			  e.printStackTrace();
		  }
	  }

}
