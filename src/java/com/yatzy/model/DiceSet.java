package com.yatzy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Kumpulan 5 dadu yang dipake main Yatzy.
 * Ngatur urusan ngocok, nahan, sama ngelepas dadu.
 */
public class DiceSet {
    
    private List<Dice> dices;
    private int rollsCount;
    
    public static final int MAX_DICE = 5;
    public static final int MAX_ROLLS = 3;
    
    /**
     * Bikin set dadu baru (5 biji), siap buat giliran baru.
     */
    public DiceSet() {
        this.dices = new ArrayList<>();
        for (int i = 0; i < MAX_DICE; i++) {
            this.dices.add(new Dice());
        }
        this.rollsCount = 0;
    }
    
    /**
     * Ngocok semua dadu yang nggak ditahan. Jatah ngocok nambah 1.
     * @return true kalo berhasil ngocok (masih ada jatah), false kalo udah abis jatahnya
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
     * Nahan dadu di posisi tertentu (biar nggak ikutan dikocok).
     * @param index posisi dadu (0-4)
     */
    public void holdDice(int index) {
        if (index >= 0 && index < MAX_DICE) {
            dices.get(index).setHeld(true);
        }
    }
    
    /**
     * Ngelepas dadu yang lagi ditahan (biar bisa dikocok lagi).
     * @param index posisi dadu (0-4)
     */
    public void releaseDice(int index) {
        if (index >= 0 && index < MAX_DICE) {
            dices.get(index).setHeld(false);
        }
    }
    
    /**
     * Ganti status dadu, kalo tadinya ditahan jadi dilepas, dan sebaliknya.
     * @param index posisi dadu (0-4)
     */
    public void toggleHold(int index) {
        if (index >= 0 && index < MAX_DICE) {
            Dice dice = dices.get(index);
            dice.setHeld(!dice.isHeld());
        }
    }
    
    /**
     * Reset buat giliran baru: ngelepas semua kuncian dadu sama ngenolin jumlah kocokan.
     */
    public void resetTurn() {
        for (Dice dice : dices) {
            dice.setHeld(false);
        }
        rollsCount = 0;
    }
    
    /**
     * Ngambil list angka dari semua dadu.
     * @return list angka (1-6)
     */
    public List<Integer> getValues() {
        List<Integer> values = new ArrayList<>();
        for (Dice dice : dices) {
            values.add(dice.getValue());
        }
        return values;
    }
    
    // --- Method Getters ---
    
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
