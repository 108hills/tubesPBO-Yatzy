package com.yatzy.model.scoring;

import com.yatzy.model.Dice;
import com.yatzy.model.ScoringStrategy;
import java.util.List;

/**
 * Strategy buat kategori Full House.
 * Dapet 25 poin kalo ada 3 dadu angka sama + 2 dadu angka sama (beda dari yang 3).
 * Kalo nggak memenuhi, skornya 0.
 */
public class FullHouseStrategy implements ScoringStrategy {
    
    @Override
    public int calculate(List<Dice> dices) {
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        boolean hasThree = false;
        boolean hasTwo = false;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 3) hasThree = true;
            if (counts[i] == 2) hasTwo = true;
        }
        return (hasThree && hasTwo) ? 25 : 0;
    }
}
