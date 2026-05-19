const quizId = Number(window.location.pathname.split('/').pop());
const query = new URLSearchParams(window.location.search);
const lobbyId = query.get('lobbyId');

let me = null;
let quizData = null;
let attemptId = null;
let currentQuestionIndex = 0;
let correctAnswers = 0;
let totalScore = 0;
let questionStartedAt = null;
let answerLocked = false;
let lobbyData = null;
let resultShown = false;

async function startQuiz() {
    me = await requireUser();
    if (!me) return;

    const result = await api(`/quizzes/api/${quizId}`);
    quizData = result.data;
    document.getElementById('quiz-title').textContent = quizData.title;

    if (lobbyId) {
        await initLobbyMode();
    }

    const attempt = await api('/ws/api/attempt/start', {
        method: 'POST',
        body: JSON.stringify({ quizId, userId: me.id })
    });
    attemptId = attempt.data?.id;
    displayQuestion();
}

async function initLobbyMode() {
    const lobbyResponse = await api(`/lobby/api/${lobbyId}`);
    lobbyData = lobbyResponse.data;
    const isHost = lobbyData && (lobbyData.host === me.id || me.role === 'ADMIN');
    const endButton = document.getElementById('end-lobby-quiz-btn');
    endButton.style.display = isHost ? 'inline-flex' : 'none';
    endButton.onclick = endQuizForLobby;

    await quizWs.subscribe(`/topic/lobby/${lobbyId}/ended`, async (results) => {
        await showResult(results);
    });
}

function displayQuestion() {
    const question = quizData.questions[currentQuestionIndex];
    questionStartedAt = new Date();
    answerLocked = false;
    document.getElementById('quiz-progress').textContent = `Вопрос ${currentQuestionIndex + 1} из ${quizData.questions.length}`;
    document.getElementById('question-text').textContent = question.description;

    const answerOptions = document.getElementById('answer-options');
    answerOptions.innerHTML = '';

    if ((question.answers || []).length > 1) {
        question.answers.forEach(answer => {
            const button = document.createElement('button');
            button.className = 'answer-option';
            button.textContent = answer.text;
            button.dataset.correct = String(answer.isCorrect);
            button.onclick = () => selectAnswer(question, answer.text, answer.isCorrect, button);
            answerOptions.appendChild(button);
        });
    } else {
        const input = document.createElement('input');
        input.type = 'text';
        input.placeholder = 'Введите ответ';
        answerOptions.appendChild(input);

        const submitButton = document.createElement('button');
        submitButton.textContent = 'Ответить';
        submitButton.style.marginTop = '10px';
        submitButton.onclick = () => {
            const expected = question.answers?.[0]?.text || '';
            selectAnswer(question, input.value, input.value.trim().toLowerCase() === expected.trim().toLowerCase(), submitButton);
        };
        answerOptions.appendChild(submitButton);
    }
}

async function selectAnswer(question, answerText, isCorrect, selectedElement) {
    if (answerLocked) return;
    answerLocked = true;
    if (isCorrect) correctAnswers++;
    revealAnswer(isCorrect, selectedElement);

    if (lobbyId) {
        await quizWs.publish(`/app/game/${lobbyId}/answer`, buildAnswerPayload(question, answerText));
    } else {
        const saved = await saveQuestionStat(question, answerText);
        totalScore += Number(saved?.points || 0);
    }

    showNextButton();
}

function revealAnswer(isCorrect, selectedElement) {
    const buttons = Array.from(document.querySelectorAll('.answer-option'));
    buttons.forEach(button => {
        button.disabled = true;
        if (button.dataset.correct === 'true') button.classList.add('correct');
    });
    if (selectedElement?.classList?.contains('answer-option')) {
        selectedElement.classList.add(isCorrect ? 'correct' : 'wrong');
    } else {
        const message = document.createElement('p');
        message.className = isCorrect ? 'answer-row correct' : 'answer-row wrong';
        const correct = quizData.questions[currentQuestionIndex].answers?.find(answer => answer.isCorrect);
        message.textContent = isCorrect ? 'Ответ правильный' : `Неверно. Правильный ответ: ${correct?.text || ''}`;
        document.getElementById('answer-options').appendChild(message);
    }
}

function showNextButton() {
    const next = document.createElement('button');
    next.textContent = currentQuestionIndex + 1 < quizData.questions.length ? 'Следующий вопрос' : 'Показать результат';
    next.style.marginTop = '14px';
    next.onclick = async () => {
        currentQuestionIndex++;
        if (currentQuestionIndex < quizData.questions.length) displayQuestion();
        else await showResult();
    };
    document.getElementById('answer-options').appendChild(next);
}

function buildAnswerPayload(question, answerText) {
    return {
        userId: me.id,
        questionId: question.id,
        attemptId,
        answer: answerText,
        activeBonus: null,
        startedAt: questionStartedAt.toISOString(),
        completedAt: new Date().toISOString()
    };
}

async function saveQuestionStat(question, answerText) {
    const response = await api('/ws/api/answer', {
        method: 'POST',
        body: JSON.stringify(buildAnswerPayload(question, answerText))
    });
    return response.status.startsWith('2') ? response.data : null;
}

async function showResult(pushedResults = null) {
    if (resultShown) return;
    resultShown = true;
    document.getElementById('question-container').style.display = 'none';
    document.getElementById('quiz-result').style.display = 'block';
    document.getElementById('end-lobby-quiz-btn').style.display = 'none';
    document.getElementById('correct-answers').textContent = correctAnswers;
    document.getElementById('total-questions').textContent = quizData.questions.length;

    let score = totalScore;
    let results = null;
    if (lobbyId) {
        results = pushedResults || await loadLobbyResults();
        const mine = results.find(result => result.userId === me.id);
        if (mine) score = mine.score;
        renderLobbyResults(results);
    }

    document.getElementById('score-line').innerHTML = `Очки: <strong>${score}</strong>${lobbyId ? renderMyPlaceText(results) : ''}`;

    await api('/ws/api/attempt/finish', {
        method: 'POST',
        body: JSON.stringify({ quizId, userId: me.id, score })
    });
}

async function loadLobbyResults() {
    const response = await api(`/lobby/api/${lobbyId}/results`);
    return response.status.startsWith('2') ? response.data : [];
}

function renderMyPlaceText(results) {
    const mine = (results || []).find(result => result.userId === me.id);
    return mine ? `, место: <strong>${mine.place}</strong>` : '';
}

function renderLobbyResults(results) {
    document.getElementById('lobby-results').innerHTML = `
        <h3>Таблица лобби</h3>
        <div class="leaderboard">
            ${(results || []).map(result => `
                <div class="leaderboard-row ${result.userId === me.id ? 'me' : ''}">
                    <span class="place">#${result.place}</span>
                    <span>${escapeHtml(result.username)}</span>
                    <strong>${result.score} очков</strong>
                </div>
            `).join('') || '<p class="muted">Результатов пока нет.</p>'}
        </div>
    `;
}

async function endQuizForLobby() {
    const response = await api(`/lobby/api/${lobbyId}/end`, { method: 'POST' });
    if (response.status.startsWith('2')) {
        await showResult(response.data);
    }
}

window.onload = startQuiz;
