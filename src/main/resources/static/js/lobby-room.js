const lobbyId = Number(window.location.pathname.split('/').pop());
let me = null;
let lobby = null;

async function initLobbyRoom() {
    me = await requireUser();
    if (!me) return;

    await api(`/lobby/api/${lobbyId}/join`, { method: 'POST' });
    await Promise.all([loadLobby(), loadQuizzes(), loadPlayers()]);
    await quizWs.subscribe(`/topic/lobby/${lobbyId}/started`, (startedLobby) => {
        const quizId = startedLobby?.quiz?.id || lobby?.quiz?.id;
        if (quizId) window.location.href = `/quiz/${quizId}?lobbyId=${lobbyId}`;
    });
}

async function loadLobby() {
    const result = await api(`/lobby/api/${lobbyId}`);
    lobby = result.data;
    document.getElementById('lobby-name').textContent = `Лобби #${lobby.id}`;
    document.getElementById('lobby-status').textContent = `Статус: ${lobby.status}`;
    document.getElementById('host-actions').style.display = isHost() ? 'flex' : 'none';
}

async function loadQuizzes() {
    const result = await api('/lobby/api/quizzes');
    const select = document.getElementById('quiz-dropdown');
    select.innerHTML = (result.data || [])
        .map(quiz => `<option value="${quiz.id}">${escapeHtml(quiz.title)}</option>`)
        .join('');
    if (lobby?.quiz?.id) select.value = lobby.quiz.id;
    select.disabled = !isHost();
}

async function loadPlayers() {
    const result = await api(`/lobby/api/${lobbyId}/players`);
    const players = Array.isArray(result.data) ? result.data : Array.from(result.data || []);
    document.getElementById('players').innerHTML = players.map(player => `
        <div class="answer-row">
            <strong>Игрок #${player.users || player.id}</strong>
            <span class="muted">Счет: ${player.score || 0}</span>
            ${isHost() && (player.users || player.id) !== lobby.host ? `<button class="danger" onclick="kickPlayer(${player.users || player.id})">Кик</button>` : ''}
        </div>
    `).join('') || '<p class="muted">Пока нет игроков.</p>';
}

function isHost() {
    return me && lobby && (me.role === 'ADMIN' || lobby.host === me.id);
}

document.getElementById('save-lobby-btn').onclick = async () => {
    const quizId = Number(document.getElementById('quiz-dropdown').value);
    const result = await api(`/lobby/api/${lobbyId}/update`, {
        method: 'POST',
        body: JSON.stringify({ quizId })
    });
    document.getElementById('room-message').textContent = result.message;
    await loadLobby();
};

document.getElementById('start-quiz-btn').onclick = async () => {
    const quizId = Number(document.getElementById('quiz-dropdown').value);
    const result = await api(`/lobby/api/${lobbyId}/start?quizId=${quizId}`, { method: 'POST' });
    if (result.status.startsWith('2')) {
        window.location.href = `/quiz/${quizId}?lobbyId=${lobbyId}`;
    } else {
        document.getElementById('room-message').textContent = result.message;
    }
};

async function kickPlayer(userId) {
    await api(`/lobby/api/${lobbyId}/players/${userId}`, { method: 'DELETE' });
    await loadPlayers();
}

window.onload = initLobbyRoom;
