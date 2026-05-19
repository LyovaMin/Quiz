let me = null;
let topics = [];

async function loadProfilePage() {
    me = await requireUser();
    if (!me) return;

    await loadTopics('profile-topic');
    document.getElementById('profile-period').onchange = loadProfile;
    document.getElementById('profile-topic').onchange = loadProfile;
    await loadProfile();
}

async function loadTopics(selectId) {
    const response = await api('/analitics/topics');
    topics = response.status.startsWith('2') ? response.data || [] : [];
    document.getElementById(selectId).innerHTML = '<option value="">Все темы</option>' + topics
        .map(topic => `<option value="${topic.id}">${escapeHtml(topic.name)}</option>`)
        .join('');
}

async function loadProfile() {
    const period = document.getElementById('profile-period').value;
    const topicId = document.getElementById('profile-topic').value;
    const response = await api(`/analitics/my-stat?username=${encodeURIComponent(me.name)}&period=${period}${topicId ? `&topicId=${topicId}` : ''}`);
    if (!response.status.startsWith('2')) return;

    const profile = response.data;
    const avatar = document.getElementById('profile-avatar');
    avatar.src = profile.user.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(profile.user.login)}&background=9333ea&color=fff`;
    document.getElementById('profile-login').textContent = profile.user.login;

    document.getElementById('profile-stats').innerHTML = `
        <div class="stat-card">
            <span>Правильные</span>
            <strong>${profile.stats.correctPercent}%</strong>
        </div>
        <div class="stat-card">
            <span>Ответы</span>
            <strong>${profile.stats.correctAnswers}/${profile.stats.totalAnswers}</strong>
        </div>
        <div class="stat-card">
            <span>Баллы</span>
            <strong>${profile.stats.score}</strong>
        </div>
    `;

    document.getElementById('profile-topic-stats').innerHTML = (profile.byTopic || [])
        .map(row => `
            <div class="topic-row">
                <span>${escapeHtml(row.topic)}</span>
                <strong>${row.correctPercent}%</strong>
            </div>
        `).join('') || '<p class="muted">Нет данных по темам.</p>';
}

window.onload = loadProfilePage;
