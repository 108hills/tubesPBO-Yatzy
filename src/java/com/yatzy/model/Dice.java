package com.yatzy.model;

import java.util.Random;

/**
 * Represents a single die that can be rolled.
 * Implements the Rollable interface.
 */
public class Dice implements Rollable {
    
    private int value;
    private boolean held;
    private static final Random random = new Random();
    
    /**
     * Constructs a Dice with initial value of 0 (not yet rolled).
     */
    public Dice() {
        this.value = 0;
        this.held = false;
    }
    
    /**
     * Rolls the die to produce a random value between 1 and 6.
     * Only rolls if the die is not currently held.
     */
    @Override
    public void roll() {
        if (!held) {
            this.value = random.nextInt(6) + 1;
        }
    }
    
    /**
     * Gets the current face value of the die.
     * @return value between 1-6, or 0 if not yet rolled
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Checks if this die is being held (excluded from rolling).
     * @return true if held
     */
    public boolean isHeld() {
        return held;
    }
    
    /**
     * Sets the held state of this die.
     * @param held true to hold, false to release
     */
    public void setHeld(boolean held) {
        this.held = held;
    }
}
