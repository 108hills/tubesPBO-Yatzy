package com.yatzy.model;

import java.util.Random;

/**
 * Ngewakilin satu dadu yang bisa dikocok.
 * Pake interface Rollable.
 */
public class Dice implements Rollable {
    
    private int value;
    private boolean held;
    private static final Random random = new Random();
    
    /**
     * Bikin objek Dadu dengan nilai awal 0 (belom dikocok).
     */
    public Dice() {
        this.value = 0;
        this.held = false;
    }
    
    /**
     * Ngocok dadu dapet angka random 1 sampe 6.
     * Cuma dikocok kalo dadunya lagi nggak ditahan.
     */
    @Override
    public void roll() {
        if (!held) {
            this.value = random.nextInt(6) + 1;
        }
    }
    
    /**
     * Ngambil angka dadu yang sekarang.
     * @return angka 1-6, atau 0 kalo belom dikocok
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Ngecek apa dadu ini lagi ditahan (gak ikutan dikocok).
     * @return true kalo ditahan
     */
    public boolean isHeld() {
        return held;
    }
    
    /**
     * Ngatur status dadu ini mau ditahan apa nggak.
     * @param held true kalo mau ditahan, false buat dilepas
     */
    public void setHeld(boolean held) {
        this.held = held;
    }
}
