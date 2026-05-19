let me = null;

async function loadQuizzes() {
    me = await requireUser();
    if (!me) return;

    const result = await api('/quizzes/api/all', {
        method: 'POST',
        body: JSON.stringify({ page: 0, size: 50 })
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
                <div class="actions">
                    <button onclick="viewQuiz(${q.id})">Открыть</button>
                    ${canEdit ? `<a class="button secondary" href="/quizzes/edit?id=${q.id}">Редактировать</a>` : ''}
                </div>
            </article>
        `;
    }).join('') || '<p class="muted">Викторины пока не созданы.</p>';
}

async function viewQuiz(id) {
    const result = await api(`/quizzes/api/${id}`);
    const quiz = result.data;
    const canEdit = me && (me.role === 'ADMIN' || quiz.creator === me.id);

    document.getElementById('modal-content').innerHTML = `
        <h2>${escapeHtml(quiz.title)}</h2>
        <p class="muted">${escapeHtml(quiz.description || '')}</p>
        ${(quiz.questions || []).map((q, index) => `
            <section class="question-block">
                <h3>${index + 1}. ${escapeHtml(q.description)}</h3>
                ${(q.answers || []).length > 1 ? q.answers.map(a => `
                    <div class="answer-row ${a.isCorrect ? 'correct' : ''}">
                        ${escapeHtml(a.text)}
                    </div>
                `).join('') : '<p class="muted">Ответ вводится текстом.</p>'}
            </section>
        `).join('')}
        <div class="actions">
            <a class="button" href="/quiz/${quiz.id}">Пройти</a>
            ${canEdit ? `<a class="button secondary" href="/quizzes/edit?id=${quiz.id}">Редактировать</a>` : ''}
        </div>
    `;
    document.getElementById('modal').classList.add('open');
}

function closeModal() {
    document.getElementById('modal').classList.remove('open');
}

window.onload = loadQuizzes;
