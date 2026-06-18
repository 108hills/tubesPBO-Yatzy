package com.yatzy.model;

import java.util.Arrays;
import java.util.List;

/**
 * Kelas utilitas (static) buat ngitung skor semua kategori Yatzy.
 * Ada satu method buat tiap kategori skor, persis kayak di UML class diagram.
 */
public class RuleEngine {
    
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
    
    /**
     * Ngitung skor buat kategori tertentu pake dadu yang disediain.
     * @param category kategori skornya
     * @param dices list dadu
     * @return hasil skornya
     */
    public static int calculateScore(String category, List<Dice> dices) {
        switch (category) {
            case ONES: return calculateOnes(dices);
            case TWOS: return calculateTwos(dices);
            case THREES: return calculateThrees(dices);
            case FOURS: return calculateFours(dices);
            case FIVES: return calculateFives(dices);
            case SIXES: return calculateSixes(dices);
            case THREE_OF_KIND: return checkThreeOfKind(dices);
            case FOUR_OF_KIND: return checkFourOfKind(dices);
            case FULL_HOUSE: return checkFullHouse(dices);
            case SMALL_STRAIGHT: return checkSmallStraight(dices);
            case LARGE_STRAIGHT: return checkLargeStraight(dices);
            case CHANCE: return calculateChance(dices);
            case YATZY: return checkYatzy(dices);
            default: return 0;
        }
    }
    
    // --- Helper: ngitung berapa kali tiap angka dadu muncul ---
    
    private static int[] getCounts(List<Dice> dices) {
        int[] counts = new int[7]; // index 0 ga dipake, cuma 1-6
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        return counts;
    }
    
    private static int sumAll(List<Dice> dices) {
        int total = 0;
        for (Dice d : dices) {
            total += d.getValue();
        }
        return total;
    }
    
    private static int sumOfValue(List<Dice> dices, int target) {
        int total = 0;
        for (Dice d : dices) {
            if (d.getValue() == target) {
                total += target;
            }
        }
        return total;
    }
    
    // --- Bagian Atas (Upper Section) ---
    
    /** Jumlah dadu angka 1. */
    public static int calculateOnes(List<Dice> dices) {
        return sumOfValue(dices, 1);
    }
    
    /** Jumlah dadu angka 2. */
    public static int calculateTwos(List<Dice> dices) {
        return sumOfValue(dices, 2);
    }
    
    /** Jumlah dadu angka 3. */
    public static int calculateThrees(List<Dice> dices) {
        return sumOfValue(dices, 3);
    }
    
    /** Jumlah dadu angka 4. */
    public static int calculateFours(List<Dice> dices) {
        return sumOfValue(dices, 4);
    }
    
    /** Jumlah dadu angka 5. */
    public static int calculateFives(List<Dice> dices) {
        return sumOfValue(dices, 5);
    }
    
    /** Jumlah dadu angka 6. */
    public static int calculateSixes(List<Dice> dices) {
        return sumOfValue(dices, 6);
    }
    
    // --- Bagian Bawah (Lower Section) ---
    
    /** Jumlah semua dadu kalo ada 3 angka yang sama, kalo nggak dapet 0. */
    public static int checkThreeOfKind(List<Dice> dices) {
        int[] counts = getCounts(dices);
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 3) {
                return sumAll(dices);
            }
        }
        return 0;
    }
    
    /** Jumlah semua dadu kalo ada 4 angka yang sama, kalo nggak dapet 0. */
    public static int checkFourOfKind(List<Dice> dices) {
        int[] counts = getCounts(dices);
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 4) {
                return sumAll(dices);
            }
        }
        return 0;
    }
    
    /** Dapet 25 poin kalo ada 3 angka sama + 2 angka sama (Full House), kalo nggak 0. */
    public static int checkFullHouse(List<Dice> dices) {
        int[] counts = getCounts(dices);
        boolean hasThree = false;
        boolean hasTwo = false;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 3) hasThree = true;
            if (counts[i] == 2) hasTwo = true;
        }
        return (hasThree && hasTwo) ? 25 : 0;
    }
    
    /** Dapet 30 poin kalo dadunya berurutan 4 angka, kalo nggak 0. */
    public static int checkSmallStraight(List<Dice> dices) {
        int[] counts = getCounts(dices);
        // Cek buat urutan: 1-2-3-4, 2-3-4-5, 3-4-5-6
        if (counts[1] >= 1 && counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1) return 30;
        if (counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1) return 30;
        if (counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1 && counts[6] >= 1) return 30;
        return 0;
    }
    
    /** Dapet 40 poin kalo dadunya berurutan 5 angka (1-2-3-4-5 atau 2-3-4-5-6), kalo nggak 0. */
    public static int checkLargeStraight(List<Dice> dices) {
        int[] counts = getCounts(dices);
        if (counts[1] == 1 && counts[2] == 1 && counts[3] == 1 && counts[4] == 1 && counts[5] == 1) return 40;
        if (counts[2] == 1 && counts[3] == 1 && counts[4] == 1 && counts[5] == 1 && counts[6] == 1) return 40;
        return 0;
    }
    
    /** Jumlahin aja semua dadu (nggak ada syarat). */
    public static int calculateChance(List<Dice> dices) {
        return sumAll(dices);
    }
    
    /** Dapet 50 poin kalo kelima dadu angkanya kembar semua, kalo nggak 0. */
    public static int checkYatzy(List<Dice> dices) {
        int[] counts = getCounts(dices);
        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 5) {
                return 50;
            }
        }
        return 0;
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
