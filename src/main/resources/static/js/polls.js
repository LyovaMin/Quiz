let me = null;

async function loadPolls() {
    me = await requireUser();

    const response = await fetch('/quizzes/api/all', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ page: 0, size: 100, type: 'POLL' })
    });
    const result = await response.json();
    const polls = result.data.content || [];

    const container = document.getElementById('poll-list');
    container.innerHTML = polls.map(poll => {
        const canEdit = me && (me.role === 'ADMIN' || poll.createdBy === me.id);
        return `
        <div class="card">
            <h3>${escapeHtml(poll.title)}</h3>
            <p>${escapeHtml(poll.description)}</p>
            <div class="actions">
                <a href="/polls/${poll.id}" class="button">Открыть</a>
                ${canEdit ? `<a class="button secondary" href="/quizzes/edit?id=${poll.id}&type=POLL">Редактировать</a>` : ''}
            </div>
        </div>
    `}).join('');
}

window.onload = () => {
    loadPolls();
    // Add user info to the top bar if auth.js is used
    if (window.setCurrentUser) {
        window.setCurrentUser();
    }
};
