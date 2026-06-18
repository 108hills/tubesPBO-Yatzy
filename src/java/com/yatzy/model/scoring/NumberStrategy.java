package com.yatzy.model.scoring;

import com.yatzy.model.Dice;
import com.yatzy.model.ScoringStrategy;
import java.util.List;

/**
 * Strategy buat ngitung skor bagian atas (Ones sampe Sixes).
 * Jumlahin semua dadu yang nilainya sama kayak targetNumber.
 * Satu kelas bisa dipake buat semua 6 kategori atas cuma beda targetnya.
 */
public class NumberStrategy implements ScoringStrategy {
    
    private int targetNumber;
    
    /**
     * Bikin NumberStrategy buat angka target tertentu.
     * @param targetNumber angka dadu yang mau dijumlahin (1-6)
     */
    public NumberStrategy(int targetNumber) {
        this.targetNumber = targetNumber;
    }
    
    @Override
    public int calculate(List<Dice> dices) {
        int total = 0;
        for (Dice d : dices) {
            if (d.getValue() == targetNumber) {
                total += targetNumber;
            }
        }
        return total;
    }
}
