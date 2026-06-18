package com.yatzy.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kelas utama yang ngatur jalannya game. Ngurusin pemain, giliran, dadu, sama alur mainnya.
 * Nyimpen semua status game dan nyediain fungsi buat ngendaliin gamenya.
 */
public class Game {
    
    private List<Player> players;
    private int currentTurn; // index pemain yang lagi jalan
    private DiceSet diceSet;
    private Notification notification;
    private boolean gameStarted;
    private boolean gameOver;
    private String mode; // "single" atau "multi"
    
    /**
     * Bikin objek Game baru.
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
     * Mulai game baru sesuai mode sama data pemainnya.
     * @param mode "single" buat singleplayer (lawan AI) atau "multi" buat multiplayer
     * @param p1Name Nama pemain 1
     * @param p1Image Foto profil pemain 1
     * @param p2Name Nama pemain 2 (dicuekin kalo mode single)
     * @param p2Image Foto profil pemain 2 (dicuekin kalo mode single)
     */
    public void startGame(String mode, String p1Name, String p1Image, String p2Name, String p2Image) {
        this.mode = mode;
        this.players.clear();
        
        // Kasih nama default kalo kosong
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
     * Lanjut ke giliran pemain berikutnya.
     * Sekalian ngereset dadu buat giliran baru.
     */
    public void nextTurn() {
        currentTurn = (currentTurn + 1) % players.size();
        diceSet.resetTurn();
        
        // Cek kalo gamenya udah kelar
        if (checkGameOver()) {
            this.gameOver = true;
            notification.showMessage("Game Over!");
        } else {
            notification.showMessage(getCurrentPlayer().getName() + "'s turn!");
        }
    }
    
    /**
     * Nentuin siapa pemenangnya dengan bandingin total skor.
     * @return Pemain yang menang, atau null kalo seri
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
     * Ngecek apa semua pemain udah ngisi penuh scorecard-nya.
     * @return true kalo game udah beres
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
     * Ngambil data pemain yang sekarang lagi jalan.
     * @return objek Player yang lagi aktif
     */
    public Player getCurrentPlayer() {
        return players.get(currentTurn);
    }
    
    /**
     * Ngecek apa pemain yang sekarang lagi jalan itu AI.
     * @return true kalo pemainnya AI
     */
    public boolean isCurrentPlayerAI() {
        return getCurrentPlayer() instanceof AIPlayer;
    }
    
    /**
     * Ngebungkus semua status game ke dalem Map biar gampang dijadiin JSON.
     * @return map yang isinya status game lengkap
     */
    public Map<String, Object> toMap() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("mode", mode);
        state.put("currentTurn", currentTurn);
        state.put("gameStarted", gameStarted);
        state.put("gameOver", gameOver);
        state.put("rollsLeft", diceSet.getRollsLeft());
        state.put("canRoll", diceSet.canRoll());
        
        // Data dadu sama status ditahan apa nggaknya
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
        
        // Data pemain sama scorecard mereka
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
            
            // Skor per kategori
            Map<String, Object> scoresMap = new LinkedHashMap<>();
            for (String category : ScoreCard.ALL_CATEGORIES) {
                Integer score = p.getScoreCard().getScore(category);
                scoresMap.put(category, score); // null kalo belom dipilih
            }
            playerMap.put("scores", scoresMap);
            
            // Potensi skor (cuma buat pemain yang lagi jalan)
            if (i == currentTurn && diceSet.getRollsLeft() < 3) {
                Map<String, Object> potentialMap = new LinkedHashMap<>();
                for (String category : ScoreCard.ALL_CATEGORIES) {
                    if (p.getScoreCard().isCategoryAvailable(category)) {
                        int potential = p.getScoreCard().calculateScore(category, diceSet.getDices());
                        potentialMap.put(category, potential);
                    }
                }
                playerMap.put("potentialScores", potentialMap);
            }
            
            playerList.add(playerMap);
        }
        state.put("players", playerList);
        
        // Data pemenang (kalo game udah kelar)
        if (gameOver) {
            Player winner = checkWinner();
            if (winner != null) {
                state.put("winnerIndex", players.indexOf(winner));
                state.put("winnerName", winner.getName());
            }
        }
        
        // Notifikasi game
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
