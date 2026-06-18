package com.yatzy.model;

/**
 * Kelas dasar abstract yang ngewakilin user di dalem sistem.
 * Nyediain properti umum yang dipake sama semua jenis user (Player, AIPlayer).
 * Nunjukin penerapan abstract class buat spesifikasi OOP.
 */
public abstract class User {
    
    private int id;
    private String username;
    private String profileImage;
    
    /**
     * Bikin User pake data yang disediain.
     * @param id ID uniknya
     * @param username nama yang mau ditampilin
     * @param profileImage path atau URL ke foto profil
     */
    public User(int id, String username, String profileImage) {
        this.id = id;
        this.username = username;
        this.profileImage = profileImage;
    }
    
    // --- Method Getters sama Setters ---
    
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
