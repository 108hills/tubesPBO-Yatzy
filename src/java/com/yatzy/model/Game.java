package com.yatzy.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game coordinator class. Manages players, turns, dice, and game flow.
 * Holds all game state and provides methods to control gameplay.
 */
public class Game {
    
    private List<Player> players;
    private int currentTurn; // index of current player
    private DiceSet diceSet;
    private Notification notification;
    private boolean gameStarted;
    private boolean gameOver;
    private String mode; // "single" or "multi"
    
    /**
     * Constructs a new Game instance.
     */
    public Game() {
        this.players = new ArrayList<>();
        this.diceSet = new DiceSet();
        this.notification = new Notification();
        this.currentTurn = 0;
        this.gameStarted = false;
        this.gameOver = false;
    }
    
    /**
     * Starts a new game with the specified mode and player details.
     * @param mode "single" for singleplayer (vs AI) or "multi" for multiplayer (pass-and-play)
     * @param p1Name Player 1 name
     * @param p1Image Player 1 profile image (base64 data URL or identifier)
     * @param p2Name Player 2 name (ignored in single mode)
     * @param p2Image Player 2 profile image (ignored in single mode)
     */
    public void startGame(String mode, String p1Name, String p1Image, String p2Name, String p2Image) {
        this.mode = mode;
        this.players.clear();
        
        // Default names if empty
        if (p1Name == null || p1Name.trim().isEmpty()) p1Name = "Player 1";
        if (p2Name == null || p2Name.trim().isEmpty()) p2Name = "Player 2";
        if (p1Image == null || p1Image.trim().isEmpty()) p1Image = "";
        if (p2Image == null || p2Image.trim().isEmpty()) p2Image = "";
        
        if ("single".equals(mode)) {
            players.add(new Player(1, p1Name.trim(), p1Image));
            players.add(new AIPlayer(2));
        } else {
            players.add(new Player(1, p1Name.trim(), p1Image));
            players.add(new Player(2, p2Name.trim(), p2Image));
        }
        
        this.currentTurn = 0;
        this.diceSet = new DiceSet();
        this.gameStarted = true;
        this.gameOver = false;
        
        notification.showMessage(getCurrentPlayer().getName() + "'s turn!");
    }
    
    /**
     * Advances to the next player's turn.
     * Resets dice for the new turn.
     */
    public void nextTurn() {
        currentTurn = (currentTurn + 1) % players.size();
        diceSet.resetTurn();
        
        // Check if game is over
        if (checkGameOver()) {
            this.gameOver = true;
            notification.showMessage("Game Over!");
        } else {
            notification.showMessage(getCurrentPlayer().getName() + "'s turn!");
        }
    }
    
    /**
     * Determines the winner by comparing total scores.
     * @return the winning Player, or null if tie
     */
    public Player checkWinner() {
        if (!gameOver) return null;
        
        Player winner = null;
        int highestScore = -1;
        
        for (Player player : players) {
            int total = player.getScoreCard().getTotal();
            if (total > highestScore) {
                highestScore = total;
                winner = player;
            }
        }
        
        return winner;
    }
    
    /**
     * Checks if all players have filled their scorecards.
     * @return true if game is over
     */
    private boolean checkGameOver() {
        for (Player player : players) {
            if (!player.getScoreCard().isFull()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets the currently active player.
     * @return the current Player
     */
    public Player getCurrentPlayer() {
        return players.get(currentTurn);
    }
    
    /**
     * Checks if the current player is an AI player.
     * @return true if current player is AI
     */
    public boolean isCurrentPlayerAI() {
        return getCurrentPlayer() instanceof AIPlayer;
    }
    
    /**
     * Serializes the full game state to a Map for JSON conversion.
     * @return map representing the complete game state
     */
    public Map<String, Object> toMap() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("mode", mode);
        state.put("currentTurn", currentTurn);
        state.put("gameStarted", gameStarted);
        state.put("gameOver", gameOver);
        state.put("rollsLeft", diceSet.getRollsLeft());
        state.put("canRoll", diceSet.canRoll());
        
        // Dice values and held states
        List<Map<String, Object>> diceList = new ArrayList<>();
        for (int i = 0; i < diceSet.getDices().size(); i++) {
            Dice d = diceSet.getDices().get(i);
            Map<String, Object> diceMap = new LinkedHashMap<>();
            diceMap.put("index", i);
            diceMap.put("value", d.getValue());
            diceMap.put("held", d.isHeld());
            diceList.add(diceMap);
        }
        state.put("dice", diceList);
        
        // Players and their scorecards
        List<Map<String, Object>> playerList = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Map<String, Object> playerMap = new LinkedHashMap<>();
            playerMap.put("index", i);
            playerMap.put("name", p.getName());
            playerMap.put("isAI", p instanceof AIPlayer);
            playerMap.put("profileImage", p.getProfileImage());
            playerMap.put("total", p.getScoreCard().getTotal());
            playerMap.put("upperSum", p.getScoreCard().getUpperSum());
            playerMap.put("upperBonus", p.getScoreCard().getUpperBonus());
            
            // Scores per category
            Map<String, Object> scoresMap = new LinkedHashMap<>();
            for (String category : RuleEngine.ALL_CATEGORIES) {
                Integer score = p.getScoreCard().getScore(category);
                scoresMap.put(category, score); // null if not yet chosen
            }
            playerMap.put("scores", scoresMap);
            
            // Potential scores (only for current player)
            if (i == currentTurn && diceSet.getRollsLeft() < 3) {
                Map<String, Object> potentialMap = new LinkedHashMap<>();
                for (String category : RuleEngine.ALL_CATEGORIES) {
                    if (p.getScoreCard().isCategoryAvailable(category)) {
                        int potential = RuleEngine.calculateScore(category, diceSet.getDices());
                        potentialMap.put(category, potential);
                    }
                }
                playerMap.put("potentialScores", potentialMap);
            }
            
            playerList.add(playerMap);
        }
        state.put("players", playerList);
        
        // Winner (if game over)
        if (gameOver) {
            Player winner = checkWinner();
            if (winner != null) {
                state.put("winnerIndex", players.indexOf(winner));
                state.put("winnerName", winner.getName());
            }
        }
        
        // Notification
        state.put("notification", notification.getMessage());
        
        return state;
    }
    
    // --- Getters ---
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public int getCurrentTurnIndex() {
        return currentTurn;
    }
    
    public DiceSet getDiceSet() {
        return diceSet;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getMode() {
        return mode;
    }
    
    public Notification getNotification() {
        return notification;
    }
}
