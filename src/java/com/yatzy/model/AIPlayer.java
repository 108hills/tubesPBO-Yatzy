package com.yatzy.model;

import java.util.List;

/**
 * AI-controlled player that automatically chooses scores.
 * Extends Player to demonstrate inheritance (AIPlayer → Player → User).
 */
public class AIPlayer extends Player {
    
    /**
     * Constructs an AIPlayer with a default AI profile.
     * @param id unique identifier
     */
    public AIPlayer(int id) {
        super(id, "AI Player", "ai");
    }
    
    /**
     * AI strategy to automatically choose the best available scoring category.
     * Evaluates all available categories and picks the one with the highest score.
     * If all yield 0, picks one to sacrifice (scores 0).
     * 
     * @param diceSet the current dice set
     * @return the chosen category name
     */
    public String chooseScore(DiceSet diceSet) {
        ScoreCard scoreCard = getScoreCard();
        List<Dice> dices = diceSet.getDices();
        
        String bestCategory = null;
        int bestScore = -1;
        
        // Evaluate each available category
        for (String category : RuleEngine.ALL_CATEGORIES) {
            if (scoreCard.isCategoryAvailable(category)) {
                int score = RuleEngine.calculateScore(category, dices);
                if (score > bestScore) {
                    bestScore = score;
                    bestCategory = category;
                }
            }
        }
        
        // If all available categories score 0, sacrifice the least valuable one
        if (bestScore == 0) {
            bestCategory = chooseSacrificeCategory(scoreCard);
        }
        
        // Lock in the score
        if (bestCategory != null) {
            scoreCard.setScore(bestCategory, dices);
        }
        
        return bestCategory;
    }
    
    /**
     * When all available categories would score 0, choose the least valuable to sacrifice.
     * Priority: sacrifice lower-value upper categories first.
     */
    private String chooseSacrificeCategory(ScoreCard scoreCard) {
        // Sacrifice priority (least valuable first)
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
     * AI decides which dice to hold based on current best strategy.
     * Holds dice that contribute to the most promising scoring category.
     * @param diceSet the current dice set
     */
    public void decideDiceHolds(DiceSet diceSet) {
        List<Dice> dices = diceSet.getDices();
        int[] counts = new int[7];
        for (Dice d : dices) {
            counts[d.getValue()]++;
        }
        
        // Find the most common value
        int bestValue = 1;
        int bestCount = 0;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] > bestCount) {
                bestCount = counts[i];
                bestValue = i;
            }
        }
        
        // Hold dice matching the most common value (basic strategy)
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
        System.out.println("AI Player | Score: " + getScoreCard().getTotal());
    }
}
