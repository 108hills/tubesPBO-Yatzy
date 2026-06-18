package com.yatzy.controller;

import com.yatzy.model.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet controller utama buat game Yatzy.
 * Ngatur semua aksi di game lewat parameter query "action".
 * Status gamenya disimpen di HttpSession.
 * 
 * Daftar Endpoint:
 *   GET  ?action=state              - Ngambil status game sekarang (format JSON)
 *   POST ?action=start&mode=X       - Mulai game baru (single/multi)
 *   POST ?action=roll               - Ngocok semua dadu yang nggak ditahan
 *   POST ?action=hold&index=N       - Nahan/ngelepas dadu di index tertentu
 *   POST ?action=score&category=X   - Ngunci skor buat kategori yang dipilih
 *   POST ?action=aiturn             - Ngeksekusi giliran AI secara penuh
 */
@WebServlet(name = "GameServlet", urlPatterns = {"/api/game"})
public class GameServlet extends HttpServlet {
    
    private static final String GAME_SESSION_KEY = "yatzyGame";
    
    /**
     * Ngambil atau bikin objek Game baru dari session.
     */
    private Game getGame(HttpSession session) {
        Game game = (Game) session.getAttribute(GAME_SESSION_KEY);
        if (game == null) {
            game = new Game();
            session.setAttribute(GAME_SESSION_KEY, game);
        }
        return game;
    }
    
    /**
     * Ngirim balasan JSON ke client.
     */
    private void sendJsonResponse(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            out.flush();
        }
    }
    
    /**
     * Ngirim status game sekarang dalam bentuk JSON.
     */
    private void sendGameState(HttpServletResponse response, Game game) throws IOException {
        String json = mapToJson(game.toMap());
        sendJsonResponse(response, json);
    }
    
    /**
     * Ngirim pesen error kalo ada yang salah.
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        String json = "{\"error\":\"" + escapeJson(message) + "\"}";
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        sendJsonResponse(response, json);
    }
    
    // --- Handler buat GET ---
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Game game = getGame(session);
        
        if ("state".equals(action)) {
            sendGameState(response, game);
        } else {
            sendError(response, "Unknown GET action: " + action);
        }
    }
    
    // --- Handler buat POST ---
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Game game = getGame(session);
        
        if (action == null) {
            sendError(response, "No action specified");
            return;
        }
        
        switch (action) {
            case "start":
                handleStart(request, response, session, game);
                break;
            case "roll":
                handleRoll(response, game);
                break;
            case "hold":
                handleHold(request, response, game);
                break;
            case "score":
                handleScore(request, response, game, session);
                break;
            case "aiturn":
                handleAITurn(response, game, session);
                break;
            case "airoll":
                handleAIRoll(response, game, session);
                break;
            case "aihold":
                handleAIHold(response, game, session);
                break;
            case "aiscore":
                handleAIScore(response, game, session);
                break;
            default:
                sendError(response, "Unknown action: " + action);
        }
    }
    
    /**
     * Ngurusin proses mulai game baru.
     */
    private void handleStart(HttpServletRequest request, HttpServletResponse response,
                             HttpSession session, Game game) throws IOException {
        String mode = request.getParameter("mode");
        if (mode == null || (!mode.equals("single") && !mode.equals("multi"))) {
            sendError(response, "Invalid mode. Use 'single' or 'multi'.");
            return;
        }
        
        // Ambil detail pemain
        String p1Name = request.getParameter("p1name");
        String p1Image = request.getParameter("p1image");
        String p2Name = request.getParameter("p2name");
        String p2Image = request.getParameter("p2image");
        
        // Bikin game baru yang fresh
        game = new Game();
        game.startGame(mode, p1Name, p1Image, p2Name, p2Image);
        session.setAttribute(GAME_SESSION_KEY, game);
        
        sendGameState(response, game);
    }
    
    /**
     * Ngurusin aksi ngocok dadu.
     */
    private void handleRoll(HttpServletResponse response, Game game) throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        
        DiceSet diceSet = game.getDiceSet();
        if (!diceSet.canRoll()) {
            sendError(response, "No rolls remaining. Choose a category.");
            return;
        }
        
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.rollDice(diceSet);
        
        game.getNotification().showMessage(
            currentPlayer.getName() + " — " + diceSet.getRollsLeft() + " rolls left"
        );
        
        sendGameState(response, game);
    }
    
    /**
     * Ngurusin ganti status dadu (ditahan atau dilepas).
     */
    private void handleHold(HttpServletRequest request, HttpServletResponse response,
                            Game game) throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        
        // Cuma bisa nahan dadu abis kocokan pertama
        if (game.getDiceSet().getRollsLeft() == 3) {
            sendError(response, "Roll the dice first.");
            return;
        }
        
        String indexStr = request.getParameter("index");
        if (indexStr == null) {
            sendError(response, "Missing die index.");
            return;
        }
        
        try {
            int index = Integer.parseInt(indexStr);
            game.getDiceSet().toggleHold(index);
            sendGameState(response, game);
        } catch (NumberFormatException e) {
            sendError(response, "Invalid die index.");
        }
    }
    
    /**
     * Ngurusin pemilihan kategori skor.
     */
    private void handleScore(HttpServletRequest request, HttpServletResponse response,
                             Game game, HttpSession session) throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        
        // Harus ngocok minimal sekali sebelum milih skor
        if (game.getDiceSet().getRollsLeft() == 3) {
            sendError(response, "Roll the dice first.");
            return;
        }
        
        String category = request.getParameter("category");
        if (category == null) {
            sendError(response, "Missing category.");
            return;
        }
        
        Player currentPlayer = game.getCurrentPlayer();
        boolean success = currentPlayer.chooseScore(category, game.getDiceSet());
        
        if (!success) {
            sendError(response, "Category '" + category + "' is already taken.");
            return;
        }
        
        // Lanjut ke giliran berikutnya
        game.nextTurn();
        session.setAttribute(GAME_SESSION_KEY, game);
        
        sendGameState(response, game);
    }
    
    /**
     * Ngurusin satu giliran penuh AI (maksimal 3 kali kocok, terus milih skor).
     */
    private void handleAITurn(HttpServletResponse response, Game game, HttpSession session) 
            throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        
        if (!game.isCurrentPlayerAI()) {
            sendError(response, "Current player is not AI.");
            return;
        }
        
        AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
        DiceSet diceSet = game.getDiceSet();
        
        // AI mulai ngocok (bisa sampe 3 kali)
        // Kocokan pertama
        ai.rollDice(diceSet);
        
        // Kocokan kedua pake strategi nahan dadu
        if (diceSet.canRoll()) {
            ai.decideDiceHolds(diceSet);
            ai.rollDice(diceSet);
        }
        
        // Kocokan ketiga sekalian nge-update dadu yang ditahan
        if (diceSet.canRoll()) {
            ai.decideDiceHolds(diceSet);
            ai.rollDice(diceSet);
        }
        
        // Pilih kategori yang skornya paling gede
        String chosenCategory = ai.chooseScore(diceSet);
        
        game.getNotification().showMessage(
            "AI chose: " + ScoreCard.getCategoryDisplayName(chosenCategory)
        );
        
        // Lanjut ke giliran berikutnya
        game.nextTurn();
        session.setAttribute(GAME_SESSION_KEY, game);
        
        sendGameState(response, game);
    }
    
    /**
     * AI step 1: Ngocok dadu (cuma ngocok doang buat giliran AI).
     */
    private void handleAIRoll(HttpServletResponse response, Game game, HttpSession session) 
            throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        if (!game.isCurrentPlayerAI()) {
            sendError(response, "Current player is not AI.");
            return;
        }
        
        AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
        DiceSet diceSet = game.getDiceSet();
        
        if (!diceSet.canRoll()) {
            sendError(response, "No rolls remaining for AI.");
            return;
        }
        
        ai.rollDice(diceSet);
        game.getNotification().showMessage(
            "AI rolling... " + diceSet.getRollsLeft() + " rolls left"
        );
        session.setAttribute(GAME_SESSION_KEY, game);
        sendGameState(response, game);
    }
    
    /**
     * AI step 2: Mikir mau nahan dadu yang mana.
     */
    private void handleAIHold(HttpServletResponse response, Game game, HttpSession session) 
            throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        if (!game.isCurrentPlayerAI()) {
            sendError(response, "Current player is not AI.");
            return;
        }
        
        AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
        ai.decideDiceHolds(game.getDiceSet());
        game.getNotification().showMessage("AI is deciding which dice to keep...");
        session.setAttribute(GAME_SESSION_KEY, game);
        sendGameState(response, game);
    }
    
    /**
     * AI step 3: Milih skor dan lanjutin ke giliran berikutnya.
     */
    private void handleAIScore(HttpServletResponse response, Game game, HttpSession session) 
            throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        if (!game.isCurrentPlayerAI()) {
            sendError(response, "Current player is not AI.");
            return;
        }
        
        AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
        String chosenCategory = ai.chooseScore(game.getDiceSet());
        
        String displayName = ScoreCard.getCategoryDisplayName(chosenCategory);
        int score = ai.getScoreCard().getScore(chosenCategory);
        game.getNotification().showMessage(
            "AI scored " + score + " on " + displayName + "!"
        );
        
        // Lanjut ke giliran berikutnya
        game.nextTurn();
        session.setAttribute(GAME_SESSION_KEY, game);
        sendGameState(response, game);
    }
    
    // ============================================================
    // Serialisasi JSON simpel (nggak perlu library eksternal)
    // ============================================================
    
    /**
     * Ngubah Map jadi string JSON secara manual.
     * Bisa nanganin Map dalem Map, List, String, Angka, Boolean, sama null.
     */
    @SuppressWarnings("unchecked")
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof Map) {
            return mapToJson((Map<String, Object>) value);
        } else if (value instanceof java.util.List) {
            return listToJson((java.util.List<Object>) value);
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
    }
    
    @SuppressWarnings("unchecked")
    private String listToJson(java.util.List<Object> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append(valueToJson(item));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
