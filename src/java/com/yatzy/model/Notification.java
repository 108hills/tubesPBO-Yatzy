package com.yatzy.model;

/**
 * Handles game notifications and messages displayed to players.
 */
public class Notification {
    
    private String message;
    private String type; // "info", "success", "warning", "error"
    
    /**
     * Constructs a default notification.
     */
    public Notification() {
        this.message = "";
        this.type = "info";
    }
    
    /**
     * Creates and returns a notification message.
     * @param message the message to display
     * @return the message string
     */
    public String showMessage(String message) {
        this.message = message;
        this.type = "info";
        return this.message;
    }
    
    /**
     * Creates a notification with a specific type.
     * @param message the message to display
     * @param type the notification type ("info", "success", "warning", "error")
     * @return the message string
     */
    public String showMessage(String message, String type) {
        this.message = message;
        this.type = type;
        return this.message;
    }
    
    // --- Getters ---
    
    public String getMessage() {
        return message;
    }
    
    public String getType() {
        return type;
    }
}
