package com.yatzy.model.scoring;

import com.yatzy.model.Dice;
import com.yatzy.model.ScoringStrategy;
import java.util.List;

/**
 * Strategy buat kategori Large Straight.
 * Dapet 40 poin kalo kelima dadu berurutan (1-2-3-4-5 atau 2-3-4-5-6).
 * Kalo nggak memenuhi, skornya 0.
 */
public class LargeStraightStrategy implements ScoringStrategy {
    
    @Override
    public int calculate(List<Dice> dices) {
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        if (counts[1] == 1 && counts[2] == 1 && counts[3] == 1 && counts[4] == 1 && counts[5] == 1) return 40;
        if (counts[2] == 1 && counts[3] == 1 && counts[4] == 1 && counts[5] == 1 && counts[6] == 1) return 40;
        return 0;
    }
}
