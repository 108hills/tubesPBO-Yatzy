package com.yatzy.model;

/**
 * Ngurusin notifikasi sama pesen-pesen yang bakal ditampilin ke pemain.
 */
public class Notification {
    
    private String message;
    private String type; // "info", "success", "warning", "error"
    
    /**
     * Bikin objek notifikasi default.
     */
    public Notification() {
        this.message = "";
        this.type = "info";
    }
    
    /**
     * Bikin dan ngembaliin pesen notifikasi.
     * @param message pesen yang mau ditampilin
     * @return string pesennya
     */
    public String showMessage(String message) {
        this.message = message;
        this.type = "info";
        return this.message;
    }
    
    /**
     * Bikin notifikasi pake tipe tertentu.
     * @param message pesen yang mau ditampilin
     * @param type tipe notifnya ("info", "success", "warning", "error")
     * @return string pesennya
     */
    public String showMessage(String message, String type) {
        this.message = message;
        this.type = type;
        return this.message;
    }
    
    // --- Method Getters ---
    
    public String getMessage() {
        return message;
    }
    
    public String getType() {
        return type;
    }
}
