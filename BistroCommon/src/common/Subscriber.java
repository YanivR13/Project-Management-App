package common;

import java.io.Serializable;

public class Subscriber implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    private int subscriberId;
    private String username;
    private String qrCode;

    public Subscriber(int userId, int subscriberId, String username, String qrCode) {
        this.userId = userId;
        this.subscriberId = subscriberId;
        this.username = username;
        this.qrCode = qrCode;
    }

    // Getters for TableView binding
    public int getUserId() { return userId; }
    public int getSubscriberId() { return subscriberId; }
    public String getUsername() { return username; }
    public String getQrCode() { return qrCode; }
}