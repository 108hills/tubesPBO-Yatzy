package com.yatzy.model;

/**
 * Abstract base class representing a user in the system.
 * Provides common properties shared by all user types (Player, AIPlayer).
 * Demonstrates abstract class usage for OOP specification.
 */
public abstract class User {
    
    private int id;
    private String username;
    private String profileImage;
    
    /**
     * Constructs a User with the given properties.
     * @param id unique identifier
     * @param username display name
     * @param profileImage path or URL to profile image
     */
    public User(int id, String username, String profileImage) {
        this.id = id;
        this.username = username;
        this.profileImage = profileImage;
    }
    
    // --- Getters and Setters ---
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
