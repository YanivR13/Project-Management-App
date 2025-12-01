package server;

import java.sql.Connection;
import java.sql.SQLException;

import ocsf.server.*;


/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 */
public class ServerController extends AbstractServer{
	
	// Variable for storing the database connection
	private static Connection conn;

	  /**
	   * The default port to listen on.
	   */
	  final public static int DEFAULT_PORT = 5555;
	
	public ServerController(int port) {
		super(port);
	}

	
	  /**
	   * This method handles any messages received from the client.
	   *
	   * @param msg The message received from the client.
	   * @param client The connection from which the message originated.
	   */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
	    System.out.println("Message received: " + msg + " from " + client);
	    DBController.insertOrderToDB(msg);
	    this.sendToAllClients(msg);
	}
	
	  /**
	   * This method overrides the one in the superclass.  Called
	   * when the server starts listening for connections.
	   */
	protected void serverStarted()
	{
	  DBController.connectToDB();
	  System.out.println("Server listening for connections on port " + getPort());
	}
	
	  /**
	   * This method overrides the one in the superclass.  Called
	   * when the server stops listening for connections.
	   */
	protected void serverStopped()
	{
	  System.out.println("Server has stopped listening for connections.");
      try {
          // Close the database connection if it is open
          if (conn != null) {
              conn.close();
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
	}
	
	
	
	  /**
	   * This method is responsible for the creation of 
	   * the server instance (there is no UI in this phase).
	   *
	   * @param args[0] The port number to listen on.  Defaults to 5555 
	   *          if no argument is entered.
	   */
    public static void main(String[] args) {
	  int port = 0; //Port to listen on

	  try
	  {
	    port = Integer.parseInt(args[0]); //Get port from command line
	  }
	  catch(Throwable t)
	  {
	    port = DEFAULT_PORT; //Set port to 5555
	  }
		
	  ServerController sv = new ServerController(port);
	    
	  try 
	  {
	    sv.listen(); //Start listening for connections
	  } 
	  catch (Exception ex) 
	  {
	    System.out.println("ERROR - Could not listen for clients!");
	  }
	}
}
