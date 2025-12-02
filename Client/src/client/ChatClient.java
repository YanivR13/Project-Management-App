package client;

import java.io.IOException;
import common.ChatIF;
import ocsf.client.AbstractClient;
import java.util.ArrayList;



/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient {
	
	  /**
	   * The interface type variable.  It allows the implementation of 
	   * the display method in the client.
	   */
	  ChatIF clientUI; 
	  
	  
	  /**
	   * Constructs an instance of the chat client.
	   *
	   * @param host The server to connect to.
	   * @param port The port number to connect on.
	   * @param clientUI The interface type variable.
	   */
	  public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
	        super(host, port); // מעביר את הכתובת והפורט ל-OCSF
	        this.clientUI = clientUI; // שומר את הרפרנס למסך
	        openConnection(); // מנסה להתחבר לשרת מיד
	  }
	  
	  
	  /**
	   * This method handles all data that comes in from the server.
	   *
	   * @param msg The message from the server.
	   */
	  @Override
	  protected void handleMessageFromServer(Object msg) {
		    if (msg instanceof java.util.ArrayList) {

		    	System.out.println("");
		        System.out.println("----- ORDERS -----");

		        ArrayList<ArrayList<String>> orders = (ArrayList<ArrayList<String>>) msg;

		        for (ArrayList<String>  row : orders) {
		            //System.out.println(row);
		            clientUI.display(row.toString());
		        }

		        System.out.println("------------------------");
		    }
		    else {
		        clientUI.display(msg.toString());
		    }
	  }
	
	  
	  /**
	  * This method handles all data coming from the UI            
	  *
	  * @param message The message from the UI.    
	  */
	  public void handleMessageFromClientUI(Object message) {
	      try {
           sendToServer(message); // שליחה פיזית לשרת דרך OCSF
	      } catch (IOException e) {
	        clientUI.display("Could not send message to server. Terminating client.");
	        quit();
	      }
	  }
	
	  
	  /**
	   * This method terminates the client.
	   */
	  public void quit() {
	      try {
	          closeConnection();
	      } catch(IOException e) {}
	      System.exit(0);
	  }

}
