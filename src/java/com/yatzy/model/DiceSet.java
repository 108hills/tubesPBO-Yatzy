package com.yatzy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of 5 dice used in a Yatzy game.
 * Manages rolling, holding, and releasing dice.
 */
public class DiceSet {
    
    private List<Dice> dices;
    private int rollsCount;
    
    public static final int MAX_DICE = 5;
    public static final int MAX_ROLLS = 3;
    
    /**
     * Constructs a DiceSet with 5 dice, ready for a new turn.
     */
    public DiceSet() {
        this.dices = new ArrayList<>();
        for (int i = 0; i < MAX_DICE; i++) {
            this.dices.add(new Dice());
        }
        this.rollsCount = 0;
    }
    
    /**
     * Rolls all non-held dice. Increments roll count.
     * @return true if roll was performed (rolls remaining), false otherwise
     */
    public boolean rollAll() {
        if (rollsCount >= MAX_ROLLS) {
            return false;
        }
        for (Dice dice : dices) {
            dice.roll();
        }
        rollsCount++;
        return true;
    }
    
    /**
     * Holds a die at the specified index (prevents it from being rolled).
     * @param index die index (0-4)
     */
    public void holdDice(int index) {
        if (index >= 0 && index < MAX_DICE) {
            dices.get(index).setHeld(true);
        }
    }
    
    /**
     * Releases a held die at the specified index (allows it to be rolled again).
     * @param index die index (0-4)
     */
    public void releaseDice(int index) {
        if (index >= 0 && index < MAX_DICE) {
            dices.get(index).setHeld(false);
        }
    }
    
    /**
     * Toggles the held state of a die at the specified index.
     * @param index die index (0-4)
     */
    public void toggleHold(int index) {
        if (index >= 0 && index < MAX_DICE) {
            Dice dice = dices.get(index);
            dice.setHeld(!dice.isHeld());
        }
    }
    
    /**
     * Resets for a new turn: releases all dice and resets roll count.
     */
    public void resetTurn() {
        for (Dice dice : dices) {
            dice.setHeld(false);
        }
        rollsCount = 0;
    }
    
    /**
     * Gets the list of all dice values.
     * @return list of integer values (1-6)
     */
    public List<Integer> getValues() {
        List<Integer> values = new ArrayList<>();
        for (Dice dice : dices) {
            values.add(dice.getValue());
        }
        return values;
    }
    
    // --- Getters ---
    
    public List<Dice> getDices() {
        return dices;
    }
    
    public int getRollsCount() {
        return rollsCount;
    }
    
    public int getRollsLeft() {
        return MAX_ROLLS - rollsCount;
    }
    
    public boolean canRoll() {
        return rollsCount < MAX_ROLLS;
    }
}
