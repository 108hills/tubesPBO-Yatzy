/**
 * Yatzy Dice Game — Frontend Controller
 * Handles all UI rendering, user interactions, and API communication.
 */

// ============================================================
// API Communication Layer
// ============================================================

const API = {
    BASE_URL: 'api/game',

    async request(method, params = {}) {
        let url = this.BASE_URL;
        let fetchOptions = { method };
        
        if (method === 'GET') {
            // GET: use query params
            const queryString = Object.entries(params)
                .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
                .join('&');
            if (queryString) url += '?' + queryString;
        } else {
            // POST: use form body
            const formBody = Object.entries(params)
                .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
                .join('&');
            fetchOptions.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
            fetchOptions.body = formBody;
        }
        
        try {
            const response = await fetch(url, fetchOptions);
            const data = await response.json();
            
            if (data.error) {
                console.error('API Error:', data.error);
                UI.showNotification(data.error, 'error');
                return null;
            }
            
            return data;
        } catch (error) {
            console.error('Network error:', error);
            UI.showNotification('Connection error. Please try again.', 'error');
            return null;
        }
    },

    getState() {
        return this.request('GET', { action: 'state' });
    },

    startGame(mode, p1name, p1image, p2name, p2image) {
        return this.request('POST', { 
            action: 'start', mode, 
            p1name: p1name || '', 
            p1image: p1image || '',
            p2name: p2name || '',
            p2image: p2image || ''
        });
    },

    rollDice() {
        return this.request('POST', { action: 'roll' });
    },

    holdDice(index) {
        return this.request('POST', { action: 'hold', index });
    },

    chooseScore(category) {
        return this.request('POST', { action: 'score', category });
    },

    aiTurn() {
        return this.request('POST', { action: 'aiturn' });
    },

    aiRoll() {
        return this.request('POST', { action: 'airoll' });
    },

    aiHold() {
        return this.request('POST', { action: 'aihold' });
    },

    aiScore() {
        return this.request('POST', { action: 'aiscore' });
    }
};

// ============================================================
// UI Rendering Layer
// ============================================================

const UI = {
    notificationTimer: null,

    /**
     * Shows a screen by ID, hides all others.
     */
    showScreen(screenId) {
        document.querySelectorAll('.screen').forEach(s => {
            s.classList.remove('active');
        });
        const target = document.getElementById(screenId);
        if (target) {
            target.classList.add('active', 'screen-fade-in');
            setTimeout(() => target.classList.remove('screen-fade-in'), 500);
        }
    },

    /**
     * Renders the full game state from server response.
     */
    renderGameState(state) {
        if (!state) return;

        this.renderDice(state.dice, state.rollsLeft, state.canRoll);
        this.renderScorecard(state.players, state.currentTurn, state.rollsLeft);
        this.renderPlayerAvatars(state.players, state.currentTurn);
        this.renderRollInfo(state.rollsLeft, state.canRoll);

        // Show notification
        if (state.notification) {
            this.showNotification(state.notification);
        }

        // Check game over
        if (state.gameOver) {
            this.showResult(state);
        }
    },

    /**
     * Renders all 5 dice in the active row + held slots.
     */
    renderDice(diceData, rollsLeft, canRoll) {
        if (!diceData) return;

        diceData.forEach((d, i) => {
            const diceEl = document.getElementById(`dice-${i}`);
            const heldSlot = document.getElementById(`held-slot-${i}`);

            if (d.value === 0) {
                // Not yet rolled
                diceEl.className = 'dice empty';
                diceEl.innerHTML = '';
                heldSlot.className = 'held-slot';
                heldSlot.innerHTML = '';
            } else {
                // Show dice face with pips
                diceEl.className = 'dice';
                if (d.held) {
                    diceEl.classList.add('held');
                }
                diceEl.innerHTML = this.createPips(d.value);

                // Update held slot
                if (d.held) {
                    heldSlot.className = 'held-slot occupied';
                    heldSlot.innerHTML = this.createSmallPips(d.value);
                } else {
                    heldSlot.className = 'held-slot';
                    heldSlot.innerHTML = '';
                }
            }
        });
    },

    /**
     * Creates pip dots HTML for a dice value (1-6) using CSS grid layout.
     */
    createPips(value) {
        // Grid positions for each pip: [row, col] in a 3x3 grid
        const layouts = {
            1: [[2, 2]],
            2: [[1, 3], [3, 1]],
            3: [[1, 3], [2, 2], [3, 1]],
            4: [[1, 1], [1, 3], [3, 1], [3, 3]],
            5: [[1, 1], [1, 3], [2, 2], [3, 1], [3, 3]],
            6: [[1, 1], [1, 3], [2, 1], [2, 3], [3, 1], [3, 3]]
        };

        const positions = layouts[value] || [];
        return positions.map(([row, col]) =>
            `<div class="pip" style="grid-row:${row};grid-column:${col}"></div>`
        ).join('');
    },

    /**
     * Creates smaller pips for held dice slots.
     */
    createSmallPips(value) {
        const layouts = {
            1: [[2, 2]],
            2: [[1, 3], [3, 1]],
            3: [[1, 3], [2, 2], [3, 1]],
            4: [[1, 1], [1, 3], [3, 1], [3, 3]],
            5: [[1, 1], [1, 3], [2, 2], [3, 1], [3, 3]],
            6: [[1, 1], [1, 3], [2, 1], [2, 3], [3, 1], [3, 3]]
        };

        const positions = layouts[value] || [];
        const pipsHtml = positions.map(([row, col]) =>
            `<div class="pip" style="grid-row:${row};grid-column:${col};width:8px;height:8px"></div>`
        ).join('');

        return `<div style="display:grid;grid-template-rows:1fr 1fr 1fr;grid-template-columns:1fr 1fr 1fr;padding:8px;width:100%;height:100%">${pipsHtml}</div>`;
    },

    /**
     * Renders the scorecard table with all player scores.
     */
    renderScorecard(players, currentTurn, rollsLeft) {
        if (!players) return;

        const categories = [
            'ones', 'twos', 'threes', 'fours', 'fives', 'sixes',
            'threeOfKind', 'fourOfKind', 'fullHouse',
            'smallStraight', 'largeStraight', 'chance', 'yatzy'
        ];

        players.forEach((player, pIndex) => {
            // Update category scores
            categories.forEach(category => {
                const cell = document.getElementById(`score-${category}-${pIndex}`);
                if (!cell) return;

                const score = player.scores[category];
                
                if (score !== null && score !== undefined) {
                    // Score is locked in
                    cell.textContent = score;
                    cell.className = 'score-cell ' + (score === 0 ? 'zero-locked' : 'locked');
                    cell.onclick = null;
                } else if (pIndex === currentTurn && player.potentialScores && 
                           player.potentialScores[category] !== undefined && !player.isAI) {
                    // Show potential score (clickable)
                    cell.textContent = player.potentialScores[category];
                    cell.className = 'score-cell clickable';
                    cell.onclick = () => chooseScore(category);
                } else {
                    // Empty / not available
                    cell.textContent = '';
                    cell.className = 'score-cell';
                    cell.onclick = null;
                }
            });

            // Update sum, bonus, total
            const sumCell = document.getElementById(`score-sum-${pIndex}`);
            const bonusCell = document.getElementById(`score-bonus-${pIndex}`);
            const totalCell = document.getElementById(`score-total-${pIndex}`);
            
            if (sumCell) sumCell.textContent = player.upperSum || '';
            if (bonusCell) bonusCell.textContent = player.upperBonus > 0 ? player.upperBonus : '';
            if (totalCell) totalCell.textContent = player.total || 0;
        });
    },

    /**
     * Renders player avatars with active state and profile images.
     */
    renderPlayerAvatars(players, currentTurn) {
        if (!players) return;

        players.forEach((player, i) => {
            const avatarEl = document.getElementById(`player-avatar-${i + 1}`);
            if (!avatarEl) return;

            const innerEl = avatarEl.querySelector('.avatar-inner');
            const nameEl = avatarEl.querySelector('.avatar-name');

            if (innerEl) {
                // Show profile image if available
                if (player.profileImage && player.profileImage.startsWith('data:')) {
                    innerEl.innerHTML = `<img src="${player.profileImage}" alt="${player.name}" style="width:100%;height:100%;object-fit:cover;border-radius:50%">`;
                } else {
                    innerEl.textContent = player.isAI ? '🤖' : player.name.charAt(0).toUpperCase();
                }
            }
            if (nameEl) {
                nameEl.textContent = player.name;
            }

            if (i === currentTurn) {
                avatarEl.classList.add('active');
            } else {
                avatarEl.classList.remove('active');
            }

            // Also update scorecard header avatars
            const headerAvatar = document.getElementById(`header-avatar-img-${i}`);
            if (headerAvatar) {
                if (player.profileImage && player.profileImage.startsWith('data:')) {
                    headerAvatar.innerHTML = `<img src="${player.profileImage}" alt="${player.name}" style="width:100%;height:100%;object-fit:cover;border-radius:50%">`;
                } else {
                    headerAvatar.textContent = player.isAI ? '🤖' : player.name.charAt(0).toUpperCase();
                }
                headerAvatar.className = 'header-avatar' + (i === currentTurn ? ' active-header' : '');
            }
        });
    },

    /**
     * Updates the roll info display and dice cup state.
     */
    renderRollInfo(rollsLeft, canRoll) {
        const textEl = document.getElementById('rolls-left-text');
        const cupEl = document.getElementById('btn-roll');

        if (textEl) {
            textEl.textContent = `${rollsLeft} Roll${rollsLeft !== 1 ? 's' : ''} Left`;
        }

        if (cupEl) {
            if (canRoll) {
                cupEl.classList.remove('disabled');
            } else {
                cupEl.classList.add('disabled');
            }
        }
    },

    /**
     * Shows a notification toast.
     */
    showNotification(message, type = 'info') {
        const toast = document.getElementById('notification-toast');
        const textEl = document.getElementById('notification-text');

        if (!toast || !textEl) return;

        textEl.textContent = message;
        toast.classList.remove('hidden');

        // Clear existing timer
        if (this.notificationTimer) {
            clearTimeout(this.notificationTimer);
        }

        // Auto-hide after 3 seconds
        this.notificationTimer = setTimeout(() => {
            toast.classList.add('hidden');
        }, 3000);
    },

    /**
     * Shows the win/lose result overlay.
     */
    showResult(state) {
        const overlay = document.getElementById('result-overlay');
        const textEl = document.getElementById('result-text');
        const scoresEl = document.getElementById('result-scores');

        if (!overlay || !textEl || !scoresEl) return;

        // Determine if current player (P1 in single, or display winner in multi)
        const winner = state.players[state.winnerIndex];
        const isSingle = state.mode === 'single';
        const isP1Winner = state.winnerIndex === 0;

        if (isSingle) {
            if (isP1Winner) {
                textEl.textContent = 'YOU WON!';
                textEl.className = 'result-text won';
            } else {
                textEl.textContent = 'YOU LOSE!';
                textEl.className = 'result-text lost';
            }
        } else {
            textEl.textContent = `${winner.name} WINS!`;
            textEl.className = 'result-text won';
        }

        // Show final scores
        const scoreLines = state.players.map(p => 
            `${p.name}: ${p.total} points`
        ).join(' • ');
        scoresEl.textContent = scoreLines;

        overlay.classList.remove('hidden');
    },

    /**
     * Adds rolling animation to dice.
     */
    animateDiceRoll(callback) {
        const diceEls = document.querySelectorAll('.dice:not(.held):not(.empty)');
        diceEls.forEach(el => el.classList.add('rolling'));

        setTimeout(() => {
            diceEls.forEach(el => el.classList.remove('rolling'));
            if (callback) callback();
        }, 500);
    }
};

// ============================================================
// Game Action Handlers (called from HTML onclick)
// ============================================================

let isProcessing = false; // Prevent double-clicks
let currentSetupMode = null; // 'single' or 'multi'
let playerAvatars = { 1: '', 2: '' }; // Store base64 avatar data URLs

/**
 * Shows the setup screen for the chosen mode.
 */
function showSetup(mode) {
    currentSetupMode = mode;
    playerAvatars = { 1: '', 2: '' };

    // Reset inputs
    document.getElementById('input-name-1').value = '';
    document.getElementById('input-name-2').value = '';
    
    // Reset avatars
    const av1 = document.getElementById('setup-avatar-1');
    const av2 = document.getElementById('setup-avatar-2');
    av1.innerHTML = '<span class="setup-avatar-text">P1</span><div class="setup-avatar-overlay">📷</div>';
    av2.innerHTML = '<span class="setup-avatar-text">P2</span><div class="setup-avatar-overlay">📷</div>';

    if (mode === 'single') {
        document.getElementById('setup-player-2').style.display = 'none';
        document.getElementById('setup-ai-indicator').style.display = 'flex';
    } else {
        document.getElementById('setup-player-2').style.display = 'flex';
        document.getElementById('setup-ai-indicator').style.display = 'none';
    }

    UI.showScreen('setup-screen');
}

/**
 * Previews an avatar image when a file is selected.
 */
function previewAvatar(playerNum, input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const dataUrl = e.target.result;
            playerAvatars[playerNum] = dataUrl;

            const avatarEl = document.getElementById(`setup-avatar-${playerNum}`);
            avatarEl.innerHTML = `<img src="${dataUrl}" alt="Avatar"><div class="setup-avatar-overlay">📷</div>`;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Confirms setup and starts the game with player details.
 */
async function confirmSetup() {
    if (isProcessing) return;
    isProcessing = true;

    const p1name = document.getElementById('input-name-1').value.trim() || 'Player 1';
    const p1image = playerAvatars[1];
    const p2name = document.getElementById('input-name-2').value.trim() || 'Player 2';
    const p2image = playerAvatars[2];

    const state = await API.startGame(currentSetupMode, p1name, p1image, p2name, p2image);
    if (state) {
        UI.showScreen('game-screen');
        UI.renderGameState(state);
    }

    isProcessing = false;
}

/**
 * Rolls all non-held dice (triggered by clicking the dice cup).
 */
async function rollDice() {
    if (isProcessing) return;
    isProcessing = true;

    const cupEl = document.getElementById('btn-roll');
    
    // Animate the cup shake
    if (cupEl) {
        cupEl.classList.add('cup-rolling');
        setTimeout(() => cupEl.classList.remove('cup-rolling'), 500);
    }

    const state = await API.rollDice();
    if (state) {
        UI.animateDiceRoll(() => {
            UI.renderGameState(state);
            checkAITurn(state);
        });
    } else {
        isProcessing = false;
    }

    // Release processing lock after animation
    setTimeout(() => { isProcessing = false; }, 600);
}

/**
 * Toggles hold state on a die.
 */
async function toggleHold(index) {
    if (isProcessing) return;
    isProcessing = true;

    const state = await API.holdDice(index);
    if (state) {
        UI.renderGameState(state);
    }

    isProcessing = false;
}

/**
 * Chooses a scoring category to lock in.
 */
async function chooseScore(category) {
    if (isProcessing) return;
    isProcessing = true;

    const state = await API.chooseScore(category);
    if (state) {
        UI.renderGameState(state);

        // Check if it's now AI's turn
        if (!state.gameOver) {
            checkAITurn(state);
        }
    }

    isProcessing = false;
}

/**
 * Returns to the main menu.
 */
function goToMenu() {
    const overlay = document.getElementById('result-overlay');
    if (overlay) overlay.classList.add('hidden');
    currentSetupMode = null;
    playerAvatars = { 1: '', 2: '' };
    UI.showScreen('menu-screen');
}

/**
 * Helper: wait for ms milliseconds.
 */
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Checks if the current player is AI and triggers animated AI turn.
 * Shows each step: rolling dice, holding dice, choosing score.
 */
async function checkAITurn(state) {
    if (!state || state.gameOver) return;

    const currentPlayer = state.players[state.currentTurn];
    if (!currentPlayer || !currentPlayer.isAI) return;

    isProcessing = true;
    UI.showNotification('AI is thinking...');
    await delay(800);

    // --- Roll 1 ---
    UI.showNotification('AI is rolling...');
    let aiState = await API.aiRoll();
    if (!aiState) { isProcessing = false; return; }
    UI.animateDiceRoll(() => UI.renderGameState(aiState));
    await delay(1200);

    // --- Hold + Roll 2 (if rolls remain) ---
    if (aiState.canRoll) {
        UI.showNotification('AI is deciding which dice to keep...');
        aiState = await API.aiHold();
        if (aiState) UI.renderGameState(aiState);
        await delay(1000);

        UI.showNotification('AI is rolling again...');
        aiState = await API.aiRoll();
        if (!aiState) { isProcessing = false; return; }
        UI.animateDiceRoll(() => UI.renderGameState(aiState));
        await delay(1200);
    }

    // --- Hold + Roll 3 (if rolls remain) ---
    if (aiState && aiState.canRoll) {
        UI.showNotification('AI is deciding which dice to keep...');
        aiState = await API.aiHold();
        if (aiState) UI.renderGameState(aiState);
        await delay(1000);

        UI.showNotification('AI is rolling one last time...');
        aiState = await API.aiRoll();
        if (!aiState) { isProcessing = false; return; }
        UI.animateDiceRoll(() => UI.renderGameState(aiState));
        await delay(1200);
    }

    // --- Choose Score ---
    UI.showNotification('AI is choosing a category...');
    await delay(1000);
    const finalState = await API.aiScore();
    if (finalState) {
        // Flash the scorecard to draw attention to the scored category
        UI.renderGameState(finalState);
        UI.showNotification(finalState.notification || 'AI scored!');

        // Highlight the score cell that AI just filled
        highlightAIScore(finalState);

        await delay(2000);

        if (!finalState.gameOver) {
            checkAITurn(finalState);
        }
    }

    isProcessing = false;
}

/**
 * Highlights the scorecard cell that the AI just scored on.
 */
function highlightAIScore(state) {
    // Find the AI player index (should be player index 1 in singleplayer)
    const aiIndex = state.players.findIndex(p => p.isAI);
    if (aiIndex < 0) return;

    // Find all score cells for the AI and flash the most recently filled ones
    const categories = [
        'ones', 'twos', 'threes', 'fours', 'fives', 'sixes',
        'threeOfKind', 'fourOfKind', 'fullHouse',
        'smallStraight', 'largeStraight', 'chance', 'yatzy'
    ];

    categories.forEach(cat => {
        const cell = document.getElementById(`score-${cat}-${aiIndex}`);
        if (cell && cell.classList.contains('locked')) {
            // Check if this cell has the flash-highlight class already
            if (!cell.dataset.highlighted) {
                cell.dataset.highlighted = 'true';
            }
        }
    });

    // Find the cell that was JUST scored (locked but not yet highlighted)
    categories.forEach(cat => {
        const cell = document.getElementById(`score-${cat}-${aiIndex}`);
        if (cell && cell.classList.contains('locked') && cell.dataset.highlighted === 'true' && !cell.dataset.previouslyHighlighted) {
            cell.classList.add('ai-score-flash');
            cell.dataset.previouslyHighlighted = 'true';
            setTimeout(() => cell.classList.remove('ai-score-flash'), 2000);
        }
    });
}

// ============================================================
// Initialization
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // Show menu screen on load
    UI.showScreen('menu-screen');
    console.log('Yatzy game loaded successfully!');
});
