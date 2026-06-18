package com.yatzy.model;

import com.yatzy.model.scoring.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kartu skor pemain, nyimpen skor buat 13 kategori di Yatzy.
 * Pake Strategy Pattern: tiap kategori punya ScoringStrategy sendiri.
 * Map scores nyimpen kategori → skor. Kalo nilainya null, berarti belom diisi.
 */
public class ScoreCard {
    
    // Konstanta nama kategori
    public static final String ONES = "ones";
    public static final String TWOS = "twos";
    public static final String THREES = "threes";
    public static final String FOURS = "fours";
    public static final String FIVES = "fives";
    public static final String SIXES = "sixes";
    public static final String THREE_OF_KIND = "threeOfKind";
    public static final String FOUR_OF_KIND = "fourOfKind";
    public static final String FULL_HOUSE = "fullHouse";
    public static final String SMALL_STRAIGHT = "smallStraight";
    public static final String LARGE_STRAIGHT = "largeStraight";
    public static final String CHANCE = "chance";
    public static final String YATZY = "yatzy";
    
    /** Semua nama kategori diurutin sesuai tampilan. */
    public static final String[] ALL_CATEGORIES = {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        THREE_OF_KIND, FOUR_OF_KIND, FULL_HOUSE,
        SMALL_STRAIGHT, LARGE_STRAIGHT, CHANCE, YATZY
    };
    
    /** Kategori bagian atas (buat ngitung bonus). */
    public static final String[] UPPER_CATEGORIES = {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES
    };
    
    private Map<String, Integer> scores;
    private Map<String, ScoringStrategy> strategies;
    
    /**
     * Bikin kartu skor baru yang masih kosong.
     * Setiap kategori langsung dipasangin sama strategy-nya masing-masing.
     */
    public ScoreCard() {
        this.scores = new LinkedHashMap<>();
        this.strategies = new LinkedHashMap<>();
        
        // Pasang strategy buat tiap kategori
        strategies.put(ONES, new NumberStrategy(1));
        strategies.put(TWOS, new NumberStrategy(2));
        strategies.put(THREES, new NumberStrategy(3));
        strategies.put(FOURS, new NumberStrategy(4));
        strategies.put(FIVES, new NumberStrategy(5));
        strategies.put(SIXES, new NumberStrategy(6));
        strategies.put(THREE_OF_KIND, new ThreeOfAKindStrategy());
        strategies.put(FOUR_OF_KIND, new FourOfAKindStrategy());
        strategies.put(FULL_HOUSE, new FullHouseStrategy());
        strategies.put(SMALL_STRAIGHT, new SmallStraightStrategy());
        strategies.put(LARGE_STRAIGHT, new LargeStraightStrategy());
        strategies.put(CHANCE, new ChanceStrategy());
        strategies.put(YATZY, new YatzyStrategy());
        
        // Inisialisasi semua skor ke null (belom dipilih)
        for (String category : ALL_CATEGORIES) {
            scores.put(category, null);
        }
    }
    
    /**
     * Ngitung kira-kira dapet skor berapa buat kategori tertentu tanpa nge-lock skornya.
     * Pake strategy yang udah dipasangin buat kategori itu.
     * @param category kategori skor
     * @param dices posisi dadu sekarang
     * @return potensi skor yang didapet
     */
    public int calculateScore(String category, List<Dice> dices) {
        ScoringStrategy strategy = strategies.get(category);
        if (strategy == null) {
            return 0;
        }
        return strategy.calculate(dices);
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
        int score = calculateScore(category, dices);
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
        for (String cat : UPPER_CATEGORIES) {
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
    
    /**
     * Ngembaliin nama kerennya dari kategori biar enak dibaca.
     * @param category kode kategorinya
     * @return nama kategori yang siap tampil
     */
    public static String getCategoryDisplayName(String category) {
        switch (category) {
            case ONES: return "Ones";
            case TWOS: return "Twos";
            case THREES: return "Threes";
            case FOURS: return "Fours";
            case FIVES: return "Fives";
            case SIXES: return "Sixes";
            case THREE_OF_KIND: return "Three of a kind";
            case FOUR_OF_KIND: return "Four of a kind";
            case FULL_HOUSE: return "Full house";
            case SMALL_STRAIGHT: return "Small straight";
            case LARGE_STRAIGHT: return "Large Straight";
            case CHANCE: return "Chance";
            case YATZY: return "Yatzy";
            default: return category;
        }
    }
}
