package common;

/**
 * Interface for server-side UI communication.
 * Allows the server backend to send log messages to the GUI.
 */
public interface ServerIF {

    /**
     * Appends a log message to the server's user interface.
     * @param message the text to display in the log window
     */
    void appendLog(String message);
}
// commit