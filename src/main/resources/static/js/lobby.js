let me = null;

async function loadLobbies() {
    me = await requireUser();
    if (!me) return;
    document.getElementById('lobby-search-btn').onclick = loadLobbyList;
    document.getElementById('lobby-id-search').onkeydown = event => {
        if (event.key === 'Enter') loadLobbyList();
    };
    await loadLobbyList();
}

async function loadLobbyList() {
    const id = document.getElementById('lobby-id-search').value;
    const result = await api('/lobby/api/all', {
        method: 'POST',
        body: JSON.stringify({ page: 0, size: 50, id: id ? Number(id) : null })
    });
    const lobbies = result.data?.content || [];
    const container = document.getElementById('lobby-list');
    container.innerHTML = lobbies.map(lobby => `
        <article class="panel">
            <span class="badge">#${lobby.id}</span>
            <span class="badge">${escapeHtml(lobby.status)}</span>
            <h2 class="card-title">${escapeHtml(lobby.quiz?.title || 'Квиз не выбран')}</h2>
            <p class="muted">Хост: ${lobby.host}</p>
            <p class="muted">Игроков: ${lobby.gameMembers?.length || 0}${lobby.maxPlayers ? ` из ${lobby.maxPlayers}` : ''}</p>
            <div class="actions">
                <a class="button" href="/lobby/${lobby.id}">Открыть</a>
            </div>
        </article>
    `).join('') || '<p class="muted">Активные лобби не найдены.</p>';
}

async function openCreateLobby() {
    const quizzes = await api('/lobby/api/quizzes');
    document.getElementById('quiz-dropdown').innerHTML = (quizzes.data || [])
        .map(quiz => `<option value="${quiz.id}">${escapeHtml(quiz.title)}</option>`)
        .join('');
    document.getElementById('create-lobby-modal').classList.add('open');
}

function closeCreateLobby() {
    document.getElementById('create-lobby-modal').classList.remove('open');
}

document.getElementById('create-lobby-btn').onclick = openCreateLobby;

document.getElementById('save-lobby-btn').onclick = async () => {
    const quiz = Number(document.getElementById('quiz-dropdown').value);
    const password = document.getElementById('lobby-password').value;
    const maxPlayers = Number(document.getElementById('lobby-max-players').value) || null;
    const result = await api('/lobby/api/create', {
        method: 'POST',
        body: JSON.stringify({ quiz, password, maxPlayers })
    });
    if (result.status.startsWith('2')) {
        window.location.href = `/lobby/${result.data.id}`;
    }
};

window.onload = loadLobbies;
