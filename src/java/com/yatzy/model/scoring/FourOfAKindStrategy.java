package com.yatzy.model.scoring;

import com.yatzy.model.Dice;
import com.yatzy.model.ScoringStrategy;
import java.util.List;

/**
 * Strategy buat kategori Four of a Kind.
 * Kalo ada minimal 4 dadu yang angkanya sama, jumlahin semua dadu.
 * Kalo nggak ada, skornya 0.
 */
public class FourOfAKindStrategy implements ScoringStrategy {
    
    @Override
    public int calculate(List<Dice> dices) {
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 4) {
                int total = 0;
                for (Dice d : dices) {
                    total += d.getValue();
                }
                return total;
            }
        }
        return 0;
    }
}
