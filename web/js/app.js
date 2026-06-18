/**
 * Game Dadu Yatzy — Frontend Controller
 * Ngurusin semua urusan nampilin UI, interaksi user, sama ngobrol ke API.
 */

// ============================================================
// Bagian Komunikasi API
// ============================================================

const API = {
    BASE_URL: 'api/game',

    async request(method, params = {}) {
        let url = this.BASE_URL;
        let fetchOptions = { method };

        if (method === 'GET') {
            // GET: pake query parameter
            const queryString = Object.entries(params)
                .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
                .join('&');
            if (queryString) url += '?' + queryString;
        } else {
            // POST: pake form body
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
// Bagian Nampilin UI (Rendering)
// ============================================================

const UI = {
    notificationTimer: null,

    /**
     * Nampilin layar sesuai ID, yang lain disembunyiin.
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

        // Atur musik sesuai layarnya biar gak nabrak
        const menuAudio = document.getElementById('menu-audio');
        const gameAudio = document.getElementById('game-audio');

        if (menuAudio && gameAudio) {
            if (screenId === 'menu-screen' || screenId === 'setup-screen') {
                gameAudio.pause();
                gameAudio.currentTime = 0; // reset dari awal
                menuAudio.play().catch(e => console.log('Autoplay menu ditahan browser'));
            } else if (screenId === 'game-screen') {
                menuAudio.pause();
                menuAudio.currentTime = 0; // reset dari awal
                gameAudio.play().catch(e => console.log('Autoplay game ditahan browser'));
            }
        }
    },

    /**
     * Nampilin status game terbaru dari respon server.
     */
    renderGameState(state) {
        if (!state) return;

        this.renderDice(state.dice, state.rollsLeft, state.canRoll);
        this.renderScorecard(state.players, state.currentTurn, state.rollsLeft);
        this.renderPlayerAvatars(state.players, state.currentTurn);
        this.renderRollInfo(state.rollsLeft, state.canRoll);

        // Munculin notif kalo ada
        if (state.notification) {
            this.showNotification(state.notification);
        }

        // Cek kalo game udah kelar
        if (state.gameOver) {
            this.showResult(state);
        }
    },

    /**
     * Nampilin 5 dadu di baris aktif + slot dadu yang ditahan.
     */
    renderDice(diceData, rollsLeft, canRoll) {
        if (!diceData) return;

        diceData.forEach((d, i) => {
            const diceEl = document.getElementById(`dice-${i}`);
            const heldSlot = document.getElementById(`held-slot-${i}`);

            if (d.value === 0) {
                // Belom dikocok nih
                diceEl.className = 'dice empty';
                diceEl.innerHTML = '';
                heldSlot.className = 'held-slot';
                heldSlot.innerHTML = '';
            } else {
                // Tunjukin titik-titik di dadu
                diceEl.className = 'dice';
                if (d.held) {
                    diceEl.classList.add('held');
                }
                diceEl.innerHTML = this.createPips(d.value);

                // Update slot dadu yang ditahan
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
     * Bikin elemen HTML titik-titik dadu (1-6) pake CSS grid.
     */
    createPips(value) {
        // Posisi grid buat tiap titik: [baris, kolom] di grid 3x3
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
     * Bikin versi kecil dari titik dadu buat dadu yang ditahan.
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
     * Nampilin tabel skor (scorecard) buat semua pemain.
     */
    renderScorecard(players, currentTurn, rollsLeft) {
        if (!players) return;

        const categories = [
            'ones', 'twos', 'threes', 'fours', 'fives', 'sixes',
            'threeOfKind', 'fourOfKind', 'fullHouse',
            'smallStraight', 'largeStraight', 'chance', 'yatzy'
        ];

        players.forEach((player, pIndex) => {
            // Update skor per kategori
            categories.forEach(category => {
                const cell = document.getElementById(`score-${category}-${pIndex}`);
                if (!cell) return;

                const score = player.scores[category];

                if (score !== null && score !== undefined) {
                    // Skor udah dikunci (locked)
                    cell.textContent = score;
                    cell.className = 'score-cell ' + (score === 0 ? 'zero-locked' : 'locked');
                    cell.onclick = null;
                } else if (pIndex === currentTurn && player.potentialScores &&
                    player.potentialScores[category] !== undefined && !player.isAI) {
                    // Nampilin potensi skor (bisa diklik)
                    cell.textContent = player.potentialScores[category];
                    cell.className = 'score-cell clickable';
                    cell.onclick = () => chooseScore(category);
                } else {
                    // Kosong / belom bisa diisi
                    cell.textContent = '';
                    cell.className = 'score-cell';
                    cell.onclick = null;
                }
            });

            // Update jumlah, bonus, sama total skor
            const sumCell = document.getElementById(`score-sum-${pIndex}`);
            const bonusCell = document.getElementById(`score-bonus-${pIndex}`);
            const totalCell = document.getElementById(`score-total-${pIndex}`);

            if (sumCell) sumCell.textContent = player.upperSum || '';
            if (bonusCell) bonusCell.textContent = player.upperBonus > 0 ? player.upperBonus : '';
            if (totalCell) totalCell.textContent = player.total || 0;
        });
    },

    /**
     * Nampilin avatar pemain, foto profil, sama siapa yang lagi jalan.
     */
    renderPlayerAvatars(players, currentTurn) {
        if (!players) return;

        players.forEach((player, i) => {
            const avatarEl = document.getElementById(`player-avatar-${i + 1}`);
            if (!avatarEl) return;

            const innerEl = avatarEl.querySelector('.avatar-inner');
            const nameEl = avatarEl.querySelector('.avatar-name');

            if (innerEl) {
                // Nampilin foto profil kalo ada
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

            // Update juga avatar yang di atas skor (header)
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
     * Update info sisa lemparan sama status gelas dadu.
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
     * Munculin popup notifikasi (toast).
     */
    showNotification(message, type = 'info') {
        const toast = document.getElementById('notification-toast');
        const textEl = document.getElementById('notification-text');

        if (!toast || !textEl) return;

        textEl.textContent = message;
        toast.classList.remove('hidden');

        // Bersihin timer yang sebelumnya
        if (this.notificationTimer) {
            clearTimeout(this.notificationTimer);
        }

        // Sembunyiin otomatis abis 3 detik
        this.notificationTimer = setTimeout(() => {
            toast.classList.add('hidden');
        }, 3000);
    },

    /**
     * Nampilin layar hasil akhir (menang/kalah).
     */
    showResult(state) {
        const overlay = document.getElementById('result-overlay');
        const textEl = document.getElementById('result-text');
        const scoresEl = document.getElementById('result-scores');

        if (!overlay || !textEl || !scoresEl) return;

        // Tentuín siapa pemenangnya (P1 kalo single, atau pemenang asli kalo multi)
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

        // Nampilin skor akhir
        const scoreLines = state.players.map(p =>
            `${p.name}: ${p.total} points`
        ).join(' • ');
        scoresEl.textContent = scoreLines;

        overlay.classList.remove('hidden');
    },

    /**
     * Nambahin animasi muter pas dadu dikocok.
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
// Handler Aksi Game (dipanggil dari onclick di HTML)
// ============================================================

let isProcessing = false; // Biar ga kecolongan klik dobel
let currentSetupMode = null; // 'single' or 'multi'
let playerAvatars = { 1: '', 2: '' }; // Buat nyimpen data URL avatar base64

/**
 * Nampilin layar setup sebelum mulai main.
 */
function showSetup(mode) {
    currentSetupMode = mode;
    playerAvatars = { 1: '', 2: '' };

    // Kosongin input form
    document.getElementById('input-name-1').value = '';
    document.getElementById('input-name-2').value = '';

    // Reset gambar avatar
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
 * Nampilin preview foto profil pas abis milih file.
 */
function previewAvatar(playerNum, input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const dataUrl = e.target.result;
            playerAvatars[playerNum] = dataUrl;

            const avatarEl = document.getElementById(`setup-avatar-${playerNum}`);
            avatarEl.innerHTML = `<img src="${dataUrl}" alt="Avatar"><div class="setup-avatar-overlay">📷</div>`;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Konfirmasi setup dan mulai game pake data pemain.
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
 * Ngocok dadu yang gak ditahan (dipanggil pas ngeklik gelas dadu).
 */
async function rollDice() {
    if (isProcessing) return;
    isProcessing = true;

    const cupEl = document.getElementById('btn-roll');

    // Jalanin animasi gelas dikocok
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

    // Buka kuncian proses abis animasinya beres
    setTimeout(() => { isProcessing = false; }, 600);
}

/**
 * Nahan atau ngelepas dadu.
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
 * Milih kategori skor buat disimpen.
 */
async function chooseScore(category) {
    if (isProcessing) return;
    isProcessing = true;

    const state = await API.chooseScore(category);
    if (state) {
        UI.renderGameState(state);

        // Cek apa abis ini gilirannya AI
        if (!state.gameOver) {
            checkAITurn(state);
        }
    }

    isProcessing = false;
}

/**
 * Balik ke menu utama.
 */
function goToMenu() {
    const overlay = document.getElementById('result-overlay');
    if (overlay) overlay.classList.add('hidden');
    currentSetupMode = null;
    playerAvatars = { 1: '', 2: '' };
    UI.showScreen('menu-screen');
}

/**
 * Fungsi bantu: nunggu beberapa milidetik.
 */
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Ngecek apa sekarang giliran AI, terus jalanin turn-nya pake animasi.
 * Ditampilin bertahap: ngocok, milih dadu, milih skor.
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
        // Bikin scorecard kedap-kedip dikit biar keliatan bagian mana yang diskor AI
        UI.renderGameState(finalState);
        UI.showNotification(finalState.notification || 'AI scored!');

        // Kasih highlight di sel skor yang baru diisi AI
        highlightAIScore(finalState);

        await delay(2000);

        if (!finalState.gameOver) {
            checkAITurn(finalState);
        }
    }

    isProcessing = false;
}

/**
 * Ngasih efek nyala (highlight) di skor yang baru aja dipilih AI.
 */
function highlightAIScore(state) {
    // Cari index AI (biasanya index 1 kalo main singleplayer)
    const aiIndex = state.players.findIndex(p => p.isAI);
    if (aiIndex < 0) return;

    // Cari semua sel skor punya AI buat di-flash
    const categories = [
        'ones', 'twos', 'threes', 'fours', 'fives', 'sixes',
        'threeOfKind', 'fourOfKind', 'fullHouse',
        'smallStraight', 'largeStraight', 'chance', 'yatzy'
    ];

    categories.forEach(cat => {
        const cell = document.getElementById(`score-${cat}-${aiIndex}`);
        if (cell && cell.classList.contains('locked')) {
            // Cek apa sel ini udah pernah di-highlight sebelumnya
            if (!cell.dataset.highlighted) {
                cell.dataset.highlighted = 'true';
            }
        }
    });

    // Cari sel yang BARU AJA diisi (udah dilock tapi belom di-highlight)
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
// Inisialisasi pas halaman beres diload
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // Set volume default
    const menuAudio = document.getElementById('menu-audio');
    const gameAudio = document.getElementById('game-audio');
    if (menuAudio) menuAudio.volume = 0.5;
    if (gameAudio) gameAudio.volume = 0.5;

    // Coba mainin lagu pas klik di mana aja buat bypass autoplay block browser
    const playMusicInit = () => {
        // Cek layar yang lagi aktif
        const activeScreen = document.querySelector('.screen.active');
        const screenId = activeScreen ? activeScreen.id : 'menu-screen';

        if (screenId === 'menu-screen' || screenId === 'setup-screen') {
            if (menuAudio) menuAudio.play().catch(e => console.log(e));
        } else if (screenId === 'game-screen') {
            if (gameAudio) gameAudio.play().catch(e => console.log(e));
        }

        document.removeEventListener('click', playMusicInit);
    };

    document.addEventListener('click', playMusicInit);

    // Langsung tampilin menu pas pertama buka web
    UI.showScreen('menu-screen');
    console.log('Yatzy game loaded successfully!');
});
