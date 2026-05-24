async function loadPolls() {
    const response = await fetch('/quizzes/api/all', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ page: 0, size: 100, type: 'POLL' })
    });
    const result = await response.json();
    const polls = result.data.content;

    const container = document.getElementById('poll-list');
    container.innerHTML = polls.map(poll => `
        <div class="card">
            <h3>${poll.title}</h3>
            <p>${poll.description}</p>
            <a href="/polls/${poll.id}" class="button">???????????</a>
        </div>
    `).join('');
}

window.onload = () => {
    loadPolls();
    // Add user info to the top bar if auth.js is used
    if (window.setCurrentUser) {
        window.setCurrentUser();
    }
};
