let me = null;
let topics = [];

async function loadQuizzes() {
    me = await requireUser();
    if (!me) return;

    await loadTopics();
    document.getElementById('quiz-search').oninput = debounce(loadQuizList, 250);
    document.getElementById('quiz-topic-filter').onchange = loadQuizList;
    await loadQuizList();
}

async function loadTopics() {
    const response = await api('/analitics/topics');
    topics = response.status.startsWith('2') ? response.data || [] : [];
    document.getElementById('quiz-topic-filter').innerHTML = '<option value="">Все темы</option>' + topics
        .map(topic => `<option value="${topic.id}">${escapeHtml(topic.name)}</option>`)
        .join('');
}

async function loadQuizList() {
    const result = await api('/quizzes/api/all', {
        method: 'POST',
        body: JSON.stringify({
            page: 0,
            size: 50,
            search: document.getElementById('quiz-search').value.trim(),
            topicId: document.getElementById('quiz-topic-filter').value || null,
            type: 'QUIZ'
        })
    });
    const quizzes = result.data?.content || [];
    const container = document.getElementById('quiz-list');

    container.innerHTML = quizzes.map(q => {
        const canEdit = me.role === 'ADMIN' || q.createdBy === me.id;
        return `
            <article class="panel">
                <span class="badge">${q.questions?.length || 0} вопросов</span>
                <h2 class="card-title">${escapeHtml(q.title || 'Без названия')}</h2>
                <p class="muted">${escapeHtml(q.description || '')}</p>
                <p class="muted">${renderTopicBadges(q.topics)}</p>
                <div class="actions">
                    <button onclick="viewQuiz(${q.id})">Открыть</button>
                    ${canEdit ? `<a class="button secondary" href="/quizzes/edit?id=${q.id}">Редактировать</a>` : ''}
                    ${canEdit ? `<button class="secondary" onclick="viewAnalytics(${q.id})">Аналитика</button>` : ''}
                </div>
            </article>
        `;
    }).join('') || '<p class="muted">Викторины не найдены.</p>';
}

async function viewQuiz(id) {
    const result = await api(`/quizzes/api/${id}`);
    const quiz = result.data;
    const canEdit = me && (me.role === 'ADMIN' || quiz.createdBy === me.id);

    document.getElementById('modal-content').innerHTML = `
        <h2>${escapeHtml(quiz.title)}</h2>
        <p class="muted">${escapeHtml(quiz.description || '')}</p>
        <p>${renderTopicBadges(quiz.topics)}</p>
        ${(quiz.questions || []).map((q, index) => `
            <section class="question-block">
                <h3>${index + 1}. ${escapeHtml(q.description)}</h3>
                ${(q.answers || []).length > 1 ? q.answers.map(a => `
                    <div class="answer-row ${canEdit && a.isCorrect ? 'correct' : ''}">
                        ${escapeHtml(a.text)}
                    </div>
                `).join('') : '<p class="muted">Ответ вводится текстом.</p>'}
            </section>
        `).join('')}
        <div class="actions">
            <a class="button" href="/quiz/${quiz.id}">Пройти</a>
            ${canEdit ? `<a class="button secondary" href="/quizzes/edit?id=${quiz.id}">Редактировать</a>` : ''}
            ${canEdit ? `<button class="secondary" onclick="viewAnalytics(${quiz.id})">Аналитика</button>` : ''}
        </div>
    `;
    document.getElementById('modal').classList.add('open');
}

async function viewAnalytics(id) {
    const result = await api(`/analitics/quiz/${id}`);
    if (!result.status.startsWith('2')) {
        document.getElementById('modal-content').innerHTML = `<p class="muted">${escapeHtml(result.message || 'Нет доступа')}</p>`;
        document.getElementById('modal').classList.add('open');
        return;
    }
    const data = result.data;
    document.getElementById('modal-content').innerHTML = `
        <h2>Аналитика: ${escapeHtml(data.title)}</h2>
        <div class="stats-grid">
            <div class="stat-card"><span>Попытки</span><strong>${data.attempts}</strong></div>
            <div class="stat-card"><span>Ответы</span><strong>${data.correctAnswers}/${data.totalAnswers}</strong></div>
            <div class="stat-card"><span>Правильные</span><strong>${data.correctPercent}%</strong></div>
        </div>
        ${(data.questions || []).map(row => `
            <div class="topic-row">
                <span>${escapeHtml(row.description)}</span>
                <strong>${row.correctPercent}% (${row.correctAnswers}/${row.totalAnswers})</strong>
            </div>
        `).join('') || '<p class="muted">Статистики пока нет.</p>'}
    `;
    document.getElementById('modal').classList.add('open');
}

function renderTopicBadges(topicNames = []) {
    return (topicNames || []).map(topic => `<span class="badge">${escapeHtml(topic)}</span>`).join(' ');
}

function closeModal() {
    document.getElementById('modal').classList.remove('open');
}

function debounce(fn, timeout) {
    let timer = null;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), timeout);
    };
}

window.onload = loadQuizzes;