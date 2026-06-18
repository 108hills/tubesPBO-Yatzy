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
 * Main servlet controller for the Yatzy game.
 * Handles all game actions via the "action" query parameter.
 * Game state is stored in HttpSession.
 * 
 * Endpoints:
 *   GET  ?action=state              → Returns current game state as JSON
 *   POST ?action=start&mode=X       → Starts a new game (single/multi)
 *   POST ?action=roll               → Rolls all non-held dice
 *   POST ?action=hold&index=N       → Toggles hold on die at index
 *   POST ?action=score&category=X   → Locks in score for category
 *   POST ?action=aiturn             → Executes a full AI turn
 */
@WebServlet(name = "GameServlet", urlPatterns = {"/api/game"})
public class GameServlet extends HttpServlet {
    
    private static final String GAME_SESSION_KEY = "yatzyGame";
    
    /**
     * Gets or creates a Game object from the session.
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
     * Sends a JSON response to the client.
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
     * Sends the current game state as a JSON response.
     */
    private void sendGameState(HttpServletResponse response, Game game) throws IOException {
        String json = mapToJson(game.toMap());
        sendJsonResponse(response, json);
    }
    
    /**
     * Sends an error response.
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        String json = "{\"error\":\"" + escapeJson(message) + "\"}";
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        sendJsonResponse(response, json);
    }
    
    // --- GET Handler ---
    
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
    
    // --- POST Handler ---
    
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
     * Handles starting a new game.
     */
    private void handleStart(HttpServletRequest request, HttpServletResponse response,
                             HttpSession session, Game game) throws IOException {
        String mode = request.getParameter("mode");
        if (mode == null || (!mode.equals("single") && !mode.equals("multi"))) {
            sendError(response, "Invalid mode. Use 'single' or 'multi'.");
            return;
        }
        
        // Extract player details
        String p1Name = request.getParameter("p1name");
        String p1Image = request.getParameter("p1image");
        String p2Name = request.getParameter("p2name");
        String p2Image = request.getParameter("p2image");
        
        // Create a fresh game
        game = new Game();
        game.startGame(mode, p1Name, p1Image, p2Name, p2Image);
        session.setAttribute(GAME_SESSION_KEY, game);
        
        sendGameState(response, game);
    }
    
    /**
     * Handles rolling the dice.
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
     * Handles toggling a die's held state.
     */
    private void handleHold(HttpServletRequest request, HttpServletResponse response,
                            Game game) throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        
        // Can only hold dice after first roll
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
     * Handles choosing a scoring category.
     */
    private void handleScore(HttpServletRequest request, HttpServletResponse response,
                             Game game, HttpSession session) throws IOException {
        if (!game.isGameStarted() || game.isGameOver()) {
            sendError(response, "Game is not active.");
            return;
        }
        
        // Must roll at least once before scoring
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
        
        // Advance to next turn
        game.nextTurn();
        session.setAttribute(GAME_SESSION_KEY, game);
        
        sendGameState(response, game);
    }
    
    /**
     * Handles a full AI turn (roll up to 3 times, then choose best category).
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
        
        // AI performs its rolls (up to 3)
        // First roll
        ai.rollDice(diceSet);
        
        // Second roll with hold strategy
        if (diceSet.canRoll()) {
            ai.decideDiceHolds(diceSet);
            ai.rollDice(diceSet);
        }
        
        // Third roll with updated holds
        if (diceSet.canRoll()) {
            ai.decideDiceHolds(diceSet);
            ai.rollDice(diceSet);
        }
        
        // Choose the best category
        String chosenCategory = ai.chooseScore(diceSet);
        
        game.getNotification().showMessage(
            "AI chose: " + RuleEngine.getCategoryDisplayName(chosenCategory)
        );
        
        // Advance to next turn
        game.nextTurn();
        session.setAttribute(GAME_SESSION_KEY, game);
        
        sendGameState(response, game);
    }
    
    /**
     * AI step 1: Rolls the dice (single roll for the AI).
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
     * AI step 2: Decides which dice to hold.
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
     * AI step 3: Chooses the best scoring category and advances the turn.
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
        
        String displayName = RuleEngine.getCategoryDisplayName(chosenCategory);
        int score = ai.getScoreCard().getScore(chosenCategory);
        game.getNotification().showMessage(
            "AI scored " + score + " on " + displayName + "!"
        );
        
        // Advance to next turn
        game.nextTurn();
        session.setAttribute(GAME_SESSION_KEY, game);
        sendGameState(response, game);
    }
    
    // ============================================================
    // Simple JSON serialization (no external libraries needed)
    // ============================================================
    
    /**
     * Converts a Map to a JSON string manually.
     * Handles nested Maps, Lists, Strings, Numbers, Booleans, and null.
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
