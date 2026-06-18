package com.yatzy.model;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Pemain yang dikendaliin AI buat otomatis milih skor.
 * Ngewarisin kelas Player buat nunjukin konsep inheritance (AIPlayer → Player → User).
 */
public class AIPlayer extends Player {

    /**
     * Bikin AIPlayer dengan profil AI bawaan.
     * @param id ID unik buat pemainnya
     */
    public AIPlayer(int id) {
        super(id, generateAIName(), "ai");
    }
    
    /**
     * Generate nama AI secara random dari daftar nama.
     * @return nama AI yang dipilih random
     */
    private static String generateAIName() {
        List<String> aiNames = Arrays.asList("BOT - Imrong", "BOT - Kadhim", "BOT - Rafi", "BOT - Arsha", "BOT - Hamud", "BOT - Pedil");
        return aiNames.get(new Random().nextInt(aiNames.size()));
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
        for (String category : ScoreCard.ALL_CATEGORIES) {
            if (scoreCard.isCategoryAvailable(category)) {
                int score = scoreCard.calculateScore(category, dices);
                if (score > bestScore) {
                    bestScore = score;
                    bestCategory = category;
                }
            }
        }
        
        // Kalo semua kategori ngasilin skor 0, korbanin kategori yang paling gak berharga
        if (bestScore == 0) {
            String[] sacrificeOrder = {
                ScoreCard.ONES, ScoreCard.TWOS, ScoreCard.THREES,
                ScoreCard.CHANCE, ScoreCard.FOURS,
                ScoreCard.SMALL_STRAIGHT, ScoreCard.FIVES,
                ScoreCard.THREE_OF_KIND, ScoreCard.FOUR_OF_KIND,
                ScoreCard.SIXES, ScoreCard.FULL_HOUSE,
                ScoreCard.LARGE_STRAIGHT, ScoreCard.YATZY
            };
            for (String category : sacrificeOrder) {
                if (scoreCard.isCategoryAvailable(category)) {
                    bestCategory = category;
                    break;
                }
            }
        }
        
        // Kunci skornya
        if (bestCategory != null) {
            scoreCard.setScore(bestCategory, dices);
        }
        
        return bestCategory;
    }
    
    /**
     * AI mikir dadu mana yang mau ditahan berdasarin kategori skor yang masih tersedia.
     * Ngecek scoreCard buat tau kategori mana yang paling nguntungin,
     * terus nahan dadu yang cocok buat dapetin skor tertinggi.
     * @param diceSet set dadu yang sekarang
     */
    public void decideDiceHolds(DiceSet diceSet) {
        ScoreCard scoreCard = getScoreCard();
        List<Dice> dices = diceSet.getDices();
        
        // Hitung frekuensi tiap angka dadu
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        
        // Cari kategori yang masih available dan kasih skor paling tinggi
        String bestCategory = null;
        int bestScore = -1;
        for (String category : ScoreCard.ALL_CATEGORIES) {
            if (scoreCard.isCategoryAvailable(category)) {
                int score = scoreCard.calculateScore(category, dices);
                if (score > bestScore) {
                    bestScore = score;
                    bestCategory = category;
                }
            }
        }
        
        // Tentuin angka dadu mana yang mau ditahan berdasarkan kategori terbaik
        int holdValue = -1;
        int holdCount = 0;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] > holdCount) {
                holdCount = counts[i];
                holdValue = i;
            }
        }
        
        // Tahan dadu yang angkanya paling banyak (buat ngejar kategori terbaik)
        if (holdCount >= 2 && bestCategory != null) {
            for (int i = 0; i < dices.size(); i++) {
                if (dices.get(i).getValue() == holdValue) {
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
