package com.yatzy.model;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Pemain yang dikendaliin AI buat otomatis milih skor.
 * Ngewarisin kelas Player buat nunjukin konsep inheritance (AIPlayer → Player → User).
 */
public class AIPlayer extends Player {
    
    private static final List<String> AI_NAMES = Arrays.asList("BOT - Imrong", "BOT - Kadhim", "BOT - Rafi", "BOT - Arsha", "BOT - Hamud", "BOT - Pedil");
    private static final Random RANDOM = new Random();

    /**
     * Bikin AIPlayer dengan profil AI bawaan.
     * @param id ID unik buat pemainnya
     */
    public AIPlayer(int id) {
        super(id, AI_NAMES.get(RANDOM.nextInt(AI_NAMES.size())), "ai");
    }
    
    /**
     * Strategi AI buat otomatis milih kategori skor yang paling gede poinnya.
     * Bakal ngecek semua kategori yang masih kosong, terus milih yang paling nguntungin.
     * Kalo semuanya ngasilin 0, dia bakal milih satu buat dikorbanin (diisi 0).
     * 
     * @param diceSet set dadu yang lagi dipake
     * @return nama kategori yang dipilih
     */
    public String chooseScore(DiceSet diceSet) {
        ScoreCard scoreCard = getScoreCard();
        List<Dice> dices = diceSet.getDices();
        
        String bestCategory = null;
        int bestScore = -1;
        
        // Cek satu-satu kategori yang masih bisa diisi
        for (String category : RuleEngine.ALL_CATEGORIES) {
            if (scoreCard.isCategoryAvailable(category)) {
                int score = RuleEngine.calculateScore(category, dices);
                if (score > bestScore) {
                    bestScore = score;
                    bestCategory = category;
                }
            }
        }
        
        // Kalo semua kategori ngasilin skor 0, korbanin kategori yang paling gak berharga
        if (bestScore == 0) {
            bestCategory = chooseSacrificeCategory(scoreCard);
        }
        
        // Kunci skornya
        if (bestCategory != null) {
            scoreCard.setScore(bestCategory, dices);
        }
        
        return bestCategory;
    }
    
    /**
     * Pas semua kategori bakal ngasilin skor 0, milih kategori yang paling gak guna buat dikorbanin.
     * Prioritasnya: ngorbanin kategori atas yang nilainya kecil dulu.
     */
    private String chooseSacrificeCategory(ScoreCard scoreCard) {
        // Urutan ngorbanin (dari yang paling gak penting)
        String[] sacrificeOrder = {
            RuleEngine.ONES, RuleEngine.TWOS, RuleEngine.THREES,
            RuleEngine.CHANCE, RuleEngine.FOURS,
            RuleEngine.SMALL_STRAIGHT, RuleEngine.FIVES,
            RuleEngine.THREE_OF_KIND, RuleEngine.FOUR_OF_KIND,
            RuleEngine.SIXES, RuleEngine.FULL_HOUSE,
            RuleEngine.LARGE_STRAIGHT, RuleEngine.YATZY
        };
        
        for (String category : sacrificeOrder) {
            if (scoreCard.isCategoryAvailable(category)) {
                return category;
            }
        }
        return null;
    }
    
    /**
     * AI mikir dadu mana yang mau ditahan berdasarin strategi dasar.
     * Nahan dadu yang nilainya paling sering muncul biar gampang dapet skor gede.
     * @param diceSet set dadu yang sekarang
     */
    public void decideDiceHolds(DiceSet diceSet) {
        List<Dice> dices = diceSet.getDices();
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        
        // Cari angka dadu mana yang paling sering muncul
        int bestValue = 1;
        int bestCount = 0;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] > bestCount) {
                bestCount = counts[i];
                bestValue = i;
            }
        }
        
        // Tahan dadu yang angkanya paling banyak (strategi simpel aja)
        if (bestCount >= 2) {
            for (int i = 0; i < dices.size(); i++) {
                if (dices.get(i).getValue() == bestValue) {
                    diceSet.holdDice(i);
                } else {
                    diceSet.releaseDice(i);
                }
            }
        }
    }
    
    @Override
    public void display() {
        System.out.println(getName() + " | Score: " + getScoreCard().getTotal());
    }
}
