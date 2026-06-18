package com.yatzy.model.scoring;

import com.yatzy.model.Dice;
import com.yatzy.model.ScoringStrategy;
import java.util.List;

/**
 * Strategy buat kategori Yatzy.
 * Dapet 50 poin kalo kelima dadu angkanya kembar semua.
 * Kalo nggak, skornya 0.
 */
public class YatzyStrategy implements ScoringStrategy {
    
    @Override
    public int calculate(List<Dice> dices) {
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 5) {
                return 50;
            }
        }
        return 0;
    }
}
