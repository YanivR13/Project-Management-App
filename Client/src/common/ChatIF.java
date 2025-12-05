package common;

/**
 * A simple interface used by client-side UIs (GUI or console)
 * to receive messages sent from the server.
 *
 * Any class implementing this interface must define how to
 * display the incoming message to the user.
 */
public interface ChatIF {

    /**
     * Displays a message sent from the server.
     *
     * @param message The message object to display.
     */
    public abstract void display(Object message);
}
