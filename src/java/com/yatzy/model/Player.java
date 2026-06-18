package com.yatzy.model;

/**
 * Ngewakilin pemain manusia di game Yatzy.
 * Ngewarisin User (abstract class) sama implement GameEntity (interface).
 * Nunjukin penerapan inheritance sama interface di OOP.
 */
public class Player extends User implements GameEntity {
    
    private ScoreCard scoreCard;
    private String name;
    
    /**
     * Bikin pemain baru pake data yang disediain.
     * @param id ID unik pemain
     * @param username nama yang bakal ditampilin
     * @param profileImage path atau data URL foto profil
     */
    public Player(int id, String username, String profileImage) {
        super(id, username, profileImage);
        this.scoreCard = new ScoreCard();
        this.name = username;
    }
    
    /**
     * Ngocok dadu pake set dadu yang lagi dimaenin.
     * @param diceSet kumpulan dadunya
     * @return true kalo berhasil dikocok
     */
    public boolean rollDice(DiceSet diceSet) {
        return diceSet.rollAll();
    }
    
    /**
     * Milih kategori skor buat disimpen permanen.
     * @param category kategori yang dipilih
     * @param diceSet set dadu sekarang
     * @return true kalo skornya sukses diset
     */
    public boolean chooseScore(String category, DiceSet diceSet) {
        return scoreCard.setScore(category, diceSet.getDices());
    }
    
    // --- Implementasi interface GameEntity ---
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void display() {
        System.out.println("Player: " + getName() + " | Score: " + scoreCard.getTotal());
    }
    
    // --- Method Getters ---
    
    public ScoreCard getScoreCard() {
        return scoreCard;
    }
}
