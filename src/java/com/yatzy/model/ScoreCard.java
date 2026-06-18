package com.yatzy.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kartu skor pemain, nyimpen skor buat 13 kategori di Yatzy.
 * Pake Map buat nyimpen kategori → skor. Kalo nilainya null, berarti belom diisi.
 */
public class ScoreCard {
    
    private Map<String, Integer> scores;
    
    /**
     * Bikin kartu skor baru yang masih kosong.
     */
    public ScoreCard() {
        this.scores = new LinkedHashMap<>();
        for (String category : RuleEngine.ALL_CATEGORIES) {
            scores.put(category, null); // null = belom dipilih
        }
    }
    
    /**
     * Ngitung kira-kira dapet skor berapa buat kategori tertentu tanpa nge-lock skornya.
     * @param category kategori skor
     * @param dices posisi dadu sekarang
     * @return potensi skor yang didapet
     */
    public int calculateScore(String category, List<Dice> dices) {
        return RuleEngine.calculateScore(category, dices);
    }
    
    /**
     * Ngunci skor di kategori tertentu pake dadu yang ada sekarang.
     * @param category kategori skor
     * @param dices dadu yang dipake
     * @return true kalo berhasil diset, false kalo kategorinya udah keisi
     */
    public boolean setScore(String category, List<Dice> dices) {
        if (scores.get(category) != null) {
            return false; // Udah diisi sebelumnya
        }
        int score = RuleEngine.calculateScore(category, dices);
        scores.put(category, score);
        return true;
    }
    
    /**
     * Ngitung total skor dari semua kategori yang udah diisi.
     * Sekalian nambahin bonus 35 poin kalo total bagian atas >= 63.
     * @return skor total keseluruhan
     */
    public int getTotal() {
        int total = 0;
        for (Integer score : scores.values()) {
            if (score != null) {
                total += score;
            }
        }
        // Tambahin bonus bagian atas
        total += getUpperBonus();
        return total;
    }
    
    /**
     * Notalin skor bagian atas doang (dari Ones sampe Sixes).
     * @return total skor bagian atas
     */
    public int getUpperSum() {
        int sum = 0;
        for (String cat : RuleEngine.UPPER_CATEGORIES) {
            Integer score = scores.get(cat);
            if (score != null) {
                sum += score;
            }
        }
        return sum;
    }
    
    /**
     * Ngitung bonus bagian atas (dapet 35 kalo skor bagian atas >= 63).
     * @return poin bonusnya
     */
    public int getUpperBonus() {
        return getUpperSum() >= 63 ? 35 : 0;
    }
    
    /**
     * Ngecek apa ke-13 kategori udah keisi semua.
     * @return true kalo kartu skor udah penuh
     */
    public boolean isFull() {
        for (Integer score : scores.values()) {
            if (score == null) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Ngecek apa kategori tertentu masih bisa dipilih.
     * @param category kategori yang mau dicek
     * @return true kalo kategorinya belom ada skornya
     */
    public boolean isCategoryAvailable(String category) {
        return scores.containsKey(category) && scores.get(category) == null;
    }
    
    /**
     * Ngambil skor buat kategori tertentu.
     * @param category nama kategorinya
     * @return poin skornya, atau null kalo belom diisi
     */
    public Integer getScore(String category) {
        return scores.get(category);
    }
    
    /**
     * Ngambil semua skor sekaligus.
     * @return map kategori → skor (null kalo belom dipilih)
     */
    public Map<String, Integer> getScores() {
        return scores;
    }
}
