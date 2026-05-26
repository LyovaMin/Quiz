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
let bonusQuestionIndexes = new Set();
let activeBonus = null;
let liveScores = [];

// timer
let countdownInterval = null;
let attemptStartedAt = null; // ISO string or null


async function startQuiz() {
    me = await requireUser();
    if (!me) return;

    const result = await api(`/quizzes/api/${quizId}`);
    quizData = result.data;
    document.getElementById('quiz-title').textContent = quizData.title;
    bonusQuestionIndexes = buildBonusQuestionIndexes(quizData.questions?.length || 0);
    updateLiveMember({ userId: me.id, username: me.name, score: 0, progress: 0 });

    if (lobbyId) {
        await initLobbyMode();
    }

    // if we are coming to see results only (re-entering a lobby after finishing), show results and skip starting a new attempt
    if (query.get('showResults') === 'true' && lobbyId) {
        const results = await loadLobbyResults();
        await showResult(results);
        return;
    }

    const attempt = await api('/ws/api/attempt/start', {
        method: 'POST',
        body: JSON.stringify({ quizId, userId: me.id })
    });
    attemptId = attempt.data?.id;
    attemptStartedAt = attempt.data?.startedAt || new Date().toISOString();

    // start countdown timer
    startCountdown();

    displayQuestion();
}

async function initLobbyMode() {
    const lobbyResponse = await api(`/lobby/api/${lobbyId}`);
    lobbyData = lobbyResponse.data;
    const isHost = lobbyData && (lobbyData.host === me.id || me.role === 'ADMIN');
    const endButton = document.getElementById('end-lobby-quiz-btn');
    endButton.style.display = isHost ? 'inline-flex' : 'none';
    endButton.onclick = endQuizForLobby;

    // start countdown using lobby start if present
    if (lobbyData && lobbyData.startedAt) {
        attemptStartedAt = lobbyData.startedAt;
        startCountdown();
    }

    await quizWs.subscribe(`/topic/lobby/${lobbyId}/ended`, async (results) => {
        await showResult(results);
    });
    await quizWs.subscribe(`/topic/game/${lobbyId}/result`, (response) => {
        if (response?.status?.startsWith('2') && response.data) {
            updateLiveMember(response.data);
        }
    });

    liveScores = await loadLobbyResults();
    renderLiveScores();
}

function displayQuestion() {
    const question = quizData.questions[currentQuestionIndex];
    questionStartedAt = new Date();
    answerLocked = false;
    activeBonus = null;
    document.getElementById('quiz-progress').textContent = `Вопрос ${currentQuestionIndex + 1} из ${quizData.questions.length}`;
    document.getElementById('question-text').textContent = question.description;

    renderBonuses(question);

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
            const correct = input.value.trim().toLowerCase() === expected.trim().toLowerCase();
            selectAnswer(question, input.value, correct, submitButton);
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
        updateLiveMember({
            userId: me.id,
            username: me.name,
            score: totalScore,
            progress: (currentQuestionIndex + 1) / quizData.questions.length
        });
    }

    showNextButton();
}

function revealAnswer(isCorrect, selectedElement) {
    const buttons = Array.from(document.querySelectorAll('.answer-option'));
    buttons.forEach(button => {
        button.disabled = true;
        button.style.display = '';
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
        activeBonus,
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
    // stop timer
    if (countdownInterval) { clearInterval(countdownInterval); countdownInterval = null; }

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

    // show finish-for-all button to host/admin on results screen
    if (lobbyId) {
        const isHost = lobbyData && (lobbyData.host === me.id || me.role === 'ADMIN');
        const finishBtn = document.getElementById('finish-for-all-btn');
        if (finishBtn) {
            finishBtn.style.display = isHost ? 'inline-flex' : 'none';
            finishBtn.onclick = endQuizForLobby;
        }
        // mark this as final results
        document.getElementById('quiz-result').classList.add('final-results');
        const h2 = document.querySelector('#quiz-result h2');
        if (h2) h2.textContent = 'Финальные результаты';
    }

    await api('/ws/api/attempt/finish', {
        method: 'POST',
        body: JSON.stringify({ quizId, userId: me.id, score })
    });
}

async function loadLobbyResults() {
    const response = await api(`/lobby/api/${lobbyId}/results`);
    return response.status.startsWith('2') ? response.data : [];
}

function formatTimeLeft(seconds) {
    if (seconds <= 0) return '00:00';
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}`;
}

function startCountdown() {
    if (!quizData || (!quizData.timeLimit && !quizData.timeLimitSeconds) ) return;
    // support both naming
    const timeLimit = Number(quizData.timeLimit || quizData.timeLimitSeconds || quizData.timeLimitSecondsRaw) || 0; // seconds
    if (!timeLimit) return;

    // compute startedAt
    const started = attemptStartedAt ? new Date(attemptStartedAt) : new Date();
    function tick() {
        const now = new Date();
        const elapsed = Math.floor((now - started) / 1000);
        const left = Math.max(0, timeLimit - elapsed);
        const el = document.getElementById('timer');
        if (el) el.textContent = formatTimeLeft(left);
        if (left <= 0) {
            clearInterval(countdownInterval);
            countdownInterval = null;
            // if lobby - wait for server to send ended message; but still show results locally
            if (lobbyId) {
                // ask server to finish lobby if host
                if (lobbyData && (lobbyData.host === me.id || me.role === 'ADMIN')) {
                    endQuizForLobby();
                } else {
                    // just show results
                    showResult();
                }
            } else {
                showResult();
            }
        }
    }
    if (countdownInterval) clearInterval(countdownInterval);
    tick();
    countdownInterval = setInterval(tick, 1000);
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

function buildBonusQuestionIndexes(totalQuestions) {
    if (totalQuestions <= 0) return new Set();
    if (totalQuestions < 5) return new Set([0]);

    const count = Math.min(4, Math.max(1, Math.floor(totalQuestions / 5)));
    const indexes = new Set();
    for (let i = 1; i <= count; i++) {
        indexes.add(Math.min(totalQuestions - 1, Math.ceil((totalQuestions * i) / count) - 1));
    }
    return indexes;
}

function renderBonuses(question) {
    const panel = document.getElementById('bonus-panel');
    panel.innerHTML = '';
    if (!bonusQuestionIndexes.has(currentQuestionIndex)) return;

    panel.innerHTML = pickBonuses(question).map(bonus => `
        <button type="button" class="bonus-button" data-bonus="${bonus.type}" title="${escapeHtml(bonus.description)}">
            ${escapeHtml(bonus.label)}
        </button>
    `).join('');

    panel.querySelectorAll('.bonus-button').forEach(button => {
        button.onclick = () => activateBonus(button.dataset.bonus, question);
    });
}

function pickBonuses(question) {
    const bonuses = [
        { type: 'DOUBLING', label: 'x2', description: 'Удваивает баллы за правильный ответ' },
        { type: 'BONUS_POINTS', label: '+50%', description: 'Добавляет половину баллов за правильный ответ' }
    ];
    if ((question.answers || []).filter(answer => !answer.isCorrect).length >= 2) {
        bonuses.push({ type: 'HALFTOHALF', label: '50/50', description: 'Убирает часть неверных вариантов' });
    }
    return bonuses;
}

function activateBonus(type, question) {
    if (answerLocked) return;
    document.querySelectorAll('.bonus-button').forEach(button => button.classList.remove('active'));
    const button = document.querySelector(`.bonus-button[data-bonus="${type}"]`);
    if (button) button.classList.add('active');

    if (type === 'HALFTOHALF') {
        activeBonus = null;
        applyHalfToHalf();
        return;
    }
    activeBonus = type;
}

function applyHalfToHalf() {
    const wrongButtons = Array.from(document.querySelectorAll('.answer-option'))
        .filter(button => button.dataset.correct !== 'true');
    const keepWrongCount = Math.max(1, Math.floor(wrongButtons.length / 2));
    wrongButtons.slice(keepWrongCount).forEach(button => {
        button.disabled = true;
        button.style.display = 'none';
    });
}

function updateLiveMember(member) {
    const userId = member.userId || member.users || member.id;
    const existing = liveScores.find(row => row.userId === userId);
    const row = {
        userId,
        username: member.username || existing?.username || (userId === me.id ? me.name : `Игрок #${userId}`),
        score: member.score || 0,
        progress: member.progress || 0
    };
    if (existing) {
        Object.assign(existing, row);
    } else {
        liveScores.push(row);
    }
    liveScores.sort((a, b) => (b.score || 0) - (a.score || 0));
    const mine = liveScores.find(item => item.userId === me.id);
    if (mine) {
        totalScore = mine.score || 0;
        renderScore();
    }
    renderLiveScores();
}

function renderScore() {
    document.getElementById('score-corner').textContent = `${totalScore} очков`;
}

function renderLiveScores() {
    const board = document.getElementById('live-scoreboard');
    if (!board) return;
    const rows = liveScores.length ? liveScores : [{ userId: me.id, username: me.name, score: totalScore, progress: 0 }];
    board.innerHTML = `
        <h3>Баллы участников</h3>
        <div class="leaderboard">
            ${rows.map((row, index) => `
                <div class="leaderboard-row ${row.userId === me.id ? 'me' : ''}">
                    <span class="place">#${index + 1}</span>
                    <span>${escapeHtml(row.username || `Игрок #${row.userId}`)}</span>
                    <strong>${row.score || 0} очков</strong>
                </div>
            `).join('')}
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
