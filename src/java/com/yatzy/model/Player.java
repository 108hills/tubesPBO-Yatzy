package com.yatzy.model;

/**
 * Represents a human player in the Yatzy game.
 * Extends User (abstract class) and implements GameEntity (interface).
 * Demonstrates inheritance and interface implementation for OOP.
 */
public class Player extends User implements GameEntity {
    
    private ScoreCard scoreCard;
    private String name;
    
    /**
     * Constructs a Player with the given properties.
     * @param id unique identifier
     * @param username display name
     * @param profileImage profile image path
     */
    public Player(int id, String username, String profileImage) {
        super(id, username, profileImage);
        this.scoreCard = new ScoreCard();
        this.name = username;
    }
    
    /**
     * Rolls the dice using the provided dice set.
     * @param diceSet the dice set to roll
     * @return true if roll was performed successfully
     */
    public boolean rollDice(DiceSet diceSet) {
        return diceSet.rollAll();
    }
    
    /**
     * Chooses a scoring category and locks in the score.
     * @param category the scoring category to choose
     * @param diceSet the current dice set
     * @return true if score was successfully set
     */
    public boolean chooseScore(String category, DiceSet diceSet) {
        return scoreCard.setScore(category, diceSet.getDices());
    }
    
    // --- GameEntity interface implementation ---
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void display() {
        System.out.println("Player: " + getName() + " | Score: " + scoreCard.getTotal());
    }
    
    // --- Getters ---
    
    public ScoreCard getScoreCard() {
        return scoreCard;
    }
}
