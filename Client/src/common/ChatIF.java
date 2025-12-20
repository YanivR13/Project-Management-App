package common;

/**
 * Interface used by client-side UIs to receive messages from the server.
 */
public interface ChatIF {
    /**
     * Displays a message sent from the server.
     * We use Object to allow flexibility in the types of messages received.
     */
    void display(Object message);
}