const lobbyId = Number(window.location.pathname.split('/').pop());
let me = null;
let lobby = null;

async function initLobbyRoom() {
    me = await requireUser();
    if (!me) return;

    await api(`/lobby/api/${lobbyId}/join`, { method: 'POST' });
    await Promise.all([loadLobby(), loadQuizzes(), loadPlayers()]);

    // check if user already completed this lobby quiz; if so redirect to results
    if (lobby && lobby.quizId) {
        const completedRes = await api(`/lobby/api/${lobbyId}/completed`);
        const completed = completedRes.status.startsWith('2') ? completedRes.data : false;
        // double-check lobby status or participants before redirecting
        if (completed) {
            // if quiz already ended or user is not a current participant, show results
            if (lobby.status === 'ENDED') {
                window.location.href = `/quiz/${lobby.quizId}?lobbyId=${lobbyId}&showResults=true`;
                return;
            }
            // if lobby not started, but user completed previously, still allow results
            const playersRes = await api(`/lobby/api/${lobbyId}/players`);
            const players = Array.isArray(playersRes.data) ? playersRes.data : Array.from(playersRes.data || []);
            const amParticipant = players.some(p => (p.users || p.id) === me.id);
            if (!amParticipant) {
                window.location.href = `/quiz/${lobby.quizId}?lobbyId=${lobbyId}&showResults=true`;
                return;
            }
            // if user is participant and lobby not started, avoid redirect
            if (lobby.status !== 'STARTED') {
                // do not redirect; user can wait for start
            } else {
                // lobby started and user participant but completed flag true -> show results
                window.location.href = `/quiz/${lobby.quizId}?lobbyId=${lobbyId}&showResults=true`;
                return;
            }
        }
    }

    // if lobby already started, redirect participants to quiz
    if (lobby && lobby.status === 'STARTED') {
        const playersRes = await api(`/lobby/api/${lobbyId}/players`);
        const players = Array.isArray(playersRes.data) ? playersRes.data : Array.from(playersRes.data || []);
        const amParticipant = players.some(p => (p.users || p.id) === me.id);
        if (amParticipant) {
            const quizIdNow = lobby.quizId || document.getElementById('quiz-dropdown').value;
            if (quizIdNow) window.location.href = `/quiz/${quizIdNow}?lobbyId=${lobbyId}`;
            return;
        }
    }

    await quizWs.subscribe(`/topic/lobby/${lobbyId}/started`, (startedLobby) => {
        // redirect only non-host participants; host handles redirect locally
        if (isHost()) return;
        const quizId = startedLobby?.quizId || lobby?.quizId;
        if (quizId) window.location.href = `/quiz/${quizId}?lobbyId=${lobbyId}`;
    });
    await quizWs.subscribe(`/topic/lobby/${lobbyId}/ended`, () => {
        window.location.reload();
    });

    // subscribe to live game updates to refresh players' scores/progress in real-time
    await quizWs.subscribe(`/topic/game/${lobbyId}/result`, (response) => {
        if (response?.status?.startsWith('2')) {
            loadPlayers();
        }
    });
}

async function loadLobby() {
    const result = await api(`/lobby/api/${lobbyId}`);
    lobby = result.data;
    document.getElementById('lobby-name').textContent = `Лобби #${lobby.id}`;
    document.getElementById('lobby-status').textContent = `Статус: ${lobby.status}`;
    document.getElementById('host-actions').style.display = isHost() ? 'flex' : 'none';
    document.getElementById('end-quiz-btn').style.display = isHost() ? 'inline-flex' : 'none';

    // hide start button and disable quiz selection when game already started
    const startBtn = document.getElementById('start-quiz-btn');
    const saveBtn = document.getElementById('save-lobby-btn');
    const quizDropdown = document.getElementById('quiz-dropdown');
    if (lobby.status === 'STARTED') {
        if (startBtn) startBtn.style.display = 'none';
        if (saveBtn) saveBtn.style.display = 'none';
        if (quizDropdown) quizDropdown.disabled = true;
    } else {
        if (startBtn) startBtn.style.display = isHost() ? 'inline-flex' : 'none';
        if (saveBtn) saveBtn.style.display = isHost() ? 'inline-flex' : 'none';
        if (quizDropdown) quizDropdown.disabled = !isHost();
    }
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
    const hostParticipates = document.getElementById('host-participates') ? document.getElementById('host-participates').checked : true;
    const result = await api(`/lobby/api/${lobbyId}/start?quizId=${quizId}&hostParticipates=${hostParticipates}`, { method: 'POST' });
    if (!result.status.startsWith('2')) {
        document.getElementById('room-message').textContent = result.message;
        return;
    }
    // if host chosen to participate, redirect to quiz; otherwise stay to observe
    if (hostParticipates) {
        window.location.href = `/quiz/${quizId}?lobbyId=${lobbyId}`;
    } else {
        document.getElementById('room-message').textContent = 'Квиз начат — вы в режиме наблюдения.';
        await loadLobby();
        await loadPlayers();
    }
};

document.getElementById('end-quiz-btn').onclick = async () => {
    const result = await api(`/lobby/api/${lobbyId}/end`, { method: 'POST' });
    if (!result.status.startsWith('2')) {
        document.getElementById('room-message').textContent = result.message;
    }
};

async function kickPlayer(userId) {
    await api(`/lobby/api/${lobbyId}/players/${userId}`, { method: 'DELETE' });
    await loadPlayers();
}

window.onload = initLobbyRoom;