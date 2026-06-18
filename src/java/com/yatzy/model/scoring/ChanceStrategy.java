package com.yatzy.model.scoring;

import com.yatzy.model.Dice;
import com.yatzy.model.ScoringStrategy;
import java.util.List;

/**
 * Strategy buat kategori Chance.
 * Jumlahin aja semua dadu tanpa syarat apa-apa.
 */
public class ChanceStrategy implements ScoringStrategy {
    
    @Override
    public int calculate(List<Dice> dices) {
        int total = 0;
        for (Dice d : dices) {
            total += d.getValue();
        }
        return total;
    }
}
