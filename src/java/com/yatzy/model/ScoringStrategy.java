package com.yatzy.model;

import java.util.List;

/**
 * Interface Strategy buat ngitung skor di Yatzy.
 * Tiap kategori punya implementasi sendiri dari interface ini.
 * Ini adalah penerapan Strategy Pattern biar gampang nambah/ubah aturan skor.
 */
public interface ScoringStrategy {
    
    /**
     * Ngitung skor berdasarkan dadu yang ada.
     * @param dices list dadu yang mau diitung
     * @return skor yang didapet
     */
    int calculate(List<Dice> dices);
}
