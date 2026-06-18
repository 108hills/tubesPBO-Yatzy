package com.yatzy.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a player's scorecard tracking scores for all 13 Yatzy categories.
 * Uses a Map to store category → score mappings. Null value means not yet chosen.
 */
public class ScoreCard {
    
    private Map<String, Integer> scores;
    
    /**
     * Constructs a new empty ScoreCard with all categories unset.
     */
    public ScoreCard() {
        this.scores = new LinkedHashMap<>();
        for (String category : RuleEngine.ALL_CATEGORIES) {
            scores.put(category, null); // null = not yet chosen
        }
    }
    
    /**
     * Calculates what the score would be for a category without locking it in.
     * @param category the scoring category
     * @param dices the current dice
     * @return the potential score
     */
    public int calculateScore(String category, List<Dice> dices) {
        return RuleEngine.calculateScore(category, dices);
    }
    
    /**
     * Locks in a score for the given category based on current dice.
     * @param category the scoring category
     * @param dices the current dice
     * @return true if score was set, false if category already taken
     */
    public boolean setScore(String category, List<Dice> dices) {
        if (scores.get(category) != null) {
            return false; // Already scored
        }
        int score = RuleEngine.calculateScore(category, dices);
        scores.put(category, score);
        return true;
    }
    
    /**
     * Gets the total score across all filled categories.
     * Includes upper section bonus (35 points if upper sum >= 63).
     * @return total score
     */
    public int getTotal() {
        int total = 0;
        for (Integer score : scores.values()) {
            if (score != null) {
                total += score;
            }
        }
        // Upper section bonus
        total += getUpperBonus();
        return total;
    }
    
    /**
     * Gets the sum of the upper section (Ones through Sixes).
     * @return upper section sum
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
     * Gets the upper section bonus (35 if upper sum >= 63, else 0).
     * @return bonus amount
     */
    public int getUpperBonus() {
        return getUpperSum() >= 63 ? 35 : 0;
    }
    
    /**
     * Checks if all 13 categories have been filled.
     * @return true if scorecard is complete
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
     * Checks if a specific category is still available.
     * @param category the category to check
     * @return true if category has not been scored yet
     */
    public boolean isCategoryAvailable(String category) {
        return scores.containsKey(category) && scores.get(category) == null;
    }
    
    /**
     * Gets the score for a specific category.
     * @param category the category
     * @return the score, or null if not yet chosen
     */
    public Integer getScore(String category) {
        return scores.get(category);
    }
    
    /**
     * Gets the full scores map.
     * @return map of category → score (null = not yet chosen)
     */
    public Map<String, Integer> getScores() {
        return scores;
    }
}
