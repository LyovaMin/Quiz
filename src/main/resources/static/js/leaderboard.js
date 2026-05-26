let me = null;
let topics = [];

async function loadLeaderboardPage() {
    me = await requireUser();
    if (!me) return;

    await loadTopics();
    document.getElementById('leaderboard-period').onchange = loadLeaderboard;
    document.getElementById('leaderboard-topic').onchange = loadLeaderboard;
    await loadLeaderboard();
}

async function loadTopics() {
    const response = await api('/analitics/topics');
    topics = response.status.startsWith('2') ? response.data || [] : [];
    document.getElementById('leaderboard-topic').innerHTML = '<option value="">Все темы</option>' + topics
        .map(topic => `<option value="${topic.id}">${escapeHtml(topic.name)}</option>`)
        .join('');
}

async function loadLeaderboard() {
    const period = document.getElementById('leaderboard-period').value;
    const topicId = document.getElementById('leaderboard-topic').value;
    const response = await api(`/analitics/leaderboard/global?period=${period}${topicId ? `&topicId=${topicId}` : ''}`);
    const rows = response.status.startsWith('2') ? response.data || [] : [];

    document.getElementById('main-leaderboard').innerHTML = rows.map((row, index) => `
        <div class="leaderboard-row ${row.userId === me.id ? 'me' : ''}">
            <span class="place">#${index + 1}</span>
            <img src="${row.avatar || ''}" alt="avatar" class="avatar">
            <span>${escapeHtml(row.login)}</span>
            <strong>${row.score} очков</strong>
        </div>
    `).join('') || '<p class="muted">Результатов пока нет.</p>';
}

window.onload = loadLeaderboardPage;