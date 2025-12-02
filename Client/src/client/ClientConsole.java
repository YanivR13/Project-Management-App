package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import common.ChatIF;


/**
 * This class constructs the UI for a chat client.  It implements the
 * chat interface in order to activate the display() method.
 * Warning: Some of the code here is cloned in ServerConsole
 */
public class ClientConsole implements ChatIF {
	
	  
	//The default port to connect on.
	final public static int DEFAULT_PORT = 5555;
	
	  
	//The instance of the client that created this ConsoleChat.
	ChatClient client;
	
	
	  /**
	   * Constructs an instance of the ClientConsole UI.
	   *
	   * @param host The host to connect to.
	   * @param port The port to connect on.
	   */
	public ClientConsole(String host, int port){
	  try 
	  {
	    client= new ChatClient(host, port, this);
	  } 
	  catch(IOException exception) 
	  {
	    System.out.println("Error: Can't setup connection!" + " Terminating client.");
	    System.exit(1);
	  }
	}
	
	
	  /**
	   * This method waits for input from the console.  Once it is 
	   * received, it sends it to the client's message handler.
	   */
	public void accept() {
	    try
	    {
	      BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
	      String message;

	      while (true) 
	      {
	    	System.out.print("Enter command (insert/display): ");
	        message = fromConsole.readLine();
	        
            if (message.equalsIgnoreCase("display")) {
                client.handleMessageFromClientUI("display");
                continue;
            }
            
            
            else if (message.equalsIgnoreCase("insert")) {

                ArrayList<String> order = new ArrayList<>();

                System.out.print("Enter order number: ");
                order.add(fromConsole.readLine());

                System.out.print("Enter order date: ");
                order.add(fromConsole.readLine());

                System.out.print("Enter number of guests: ");
                order.add(fromConsole.readLine());
                
                System.out.print("Enter confirmation code: ");
                order.add(fromConsole.readLine());
                
                System.out.print("Enter subscriber id: ");
                order.add(fromConsole.readLine());
                
                System.out.print("Enter date of placing order: ");
                order.add(fromConsole.readLine());

                // שליחת ההזמנה לשרת
                client.handleMessageFromClientUI(order);
                continue;
            }
            
            System.out.println("Unknown command.");
	      }
	      
	    } catch (Exception ex){
	      System.out.println
	        ("Unexpected error while reading from console!");
	    }
	}
	
	
	  /**
	   * This method overrides the method in the ChatIF interface.  It
	   * displays a message onto the screen.
	   *
	   * @param message The string to be displayed.
	   */
	public void display(String message) {
		System.out.println("> " + message);
	}
	
	
	  /**
	   * This method is responsible for the creation of the Client UI.
	   *
	   * @param args[0] The host to connect to.
	   */
	public static void main(String[] args) {
	    String host = "";
	    int port = 0;  //The port number

	    try
	    {
	      host = args[0];
	    }
	    catch(ArrayIndexOutOfBoundsException e)
	    {
	      host = "localhost";
	    }
	    ClientConsole chat= new ClientConsole(host, DEFAULT_PORT);
	    chat.accept();  //Wait for console data
	}
}
