const pollId = window.location.pathname.split('/').pop();
let me = null;
let poll = null;

async function initPollPage() {
    me = await requireUser();
    if (!me) return;

    const pollResponse = await api(`/quizzes/api/${pollId}`);
    if (!pollResponse.status.startsWith('2')) {
        document.body.innerHTML = '<h1>Опрос не найден</h1>';
        return;
    }
    poll = pollResponse.data;

    document.getElementById('poll-title').textContent = poll.title;
    document.getElementById('poll-description').textContent = poll.description;

    // Проверяем, голосовал ли пользователь уже
    const voteCheckResponse = await api(`/polls/api/${pollId}/results`);
    if (voteCheckResponse.status.startsWith('2') && voteCheckResponse.data.userHasVoted) {
        showResults(voteCheckResponse.data.results);
    } else {
        renderVotingOptions();
    }

    addEditButton();
}

function renderVotingOptions() {
    const optionsContainer = document.getElementById('poll-options');
    const answers = poll.questions?.[0]?.answers || [];
    optionsContainer.innerHTML = answers.map(answer => `
        <button class="button" onclick="vote(${answer.id})">${escapeHtml(answer.text)}</button>
    `).join('');
}

function addEditButton() {
    if (me && (me.role === 'ADMIN' || poll.createdBy === me.id)) {
        const actions = document.createElement('div');
        actions.className = 'actions';
        const editLink = document.createElement('a');
        editLink.className = 'button secondary';
        editLink.href = `/quizzes/edit?id=${pollId}&type=POLL`;
        editLink.textContent = 'Редактировать';
        actions.appendChild(editLink);
        document.getElementById('poll-container').appendChild(actions);
    }
}

async function vote(answerId) {
    const response = await api(`/polls/api/${pollId}/vote?answerId=${answerId}`, {
        method: 'POST'
    });

    if (response.status.startsWith('2')) {
        // Правильно передаем только массив с результатами
        showResults(response.data.results);
    } else {
        alert(response.message || 'Ошибка при голосовании');
        // Если ошибка "уже голосовал", то просто показываем результаты
        if (response.status === '400') {
            const resultsResponse = await api(`/polls/api/${pollId}/results`);
            if (resultsResponse.status.startsWith('2')) {
                showResults(resultsResponse.data.results);
            }
        }
    }
}

function showResults(results) {
    document.getElementById('poll-container').style.display = 'none';
    document.getElementById('results-container').style.display = 'block';

    const resultsChart = document.getElementById('results-chart');
    if (!results || results.length === 0) {
        resultsChart.innerHTML = '<p class="muted">Пока нет голосов.</p>';
        return;
    }

    resultsChart.innerHTML = results.map(result => {
        const percentage = Number(result.percentage).toFixed(1);
        return `
            <div class="result-bar-container">
                <div class="result-bar-label">
                    <span>${escapeHtml(result.answerText)}</span>
                    <span>${result.votes} голосов</span>
                </div>
                <div class="result-bar">
                    <div class="result-bar-fill" style="width: ${percentage}%;">${percentage}%</div>
                </div>
            </div>
        `;
    }).join('');
}

window.onload = initPollPage;