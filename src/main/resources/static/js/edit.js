const params = new URLSearchParams(window.location.search);
const quizId = params.get('id');
const type = params.get('type') || 'QUIZ';
let me = null;
let topics = [];

async function initEditor() {
    me = await requireUser();
    if (!me) return;
    await loadTopics();
    document.getElementById('add-q-btn').onclick = () => addQuestionUI();
    document.getElementById('quiz-form').onsubmit = saveQuiz;
    document.getElementById('delete-btn').onclick = deleteQuiz;

    if (quizId) {
        document.getElementById('form-title').textContent = 'Редактирование';
        document.getElementById('delete-btn').style.display = 'inline-flex';
        const result = await api(`/quizzes/api/${quizId}`);
        fillForm(result.data);
    } else {
        document.getElementById('form-title').textContent = type === 'POLL' ? 'Новый опрос' : 'Новый квиз';
        addQuestionUI();
    }

    if (type === 'POLL') {
        document.getElementById('add-q-btn').style.display = 'none';
    }
}

async function loadTopics() {
    const response = await api('/analitics/topics');
    topics = response.status.startsWith('2') ? response.data || [] : [];
    document.getElementById('topic-options').innerHTML = topics.map(topic => `
        <label class="topic-check">
            <input type="checkbox" value="${topic.id}" style="width:auto;min-height:auto;">
            ${escapeHtml(topic.name)}
        </label>
    `).join('');
}

function addQuestionUI(data = null) {
    const container = document.getElementById('questions-container');
    if (type === 'POLL' && container.children.length > 0) {
        return;
    }
    const qDiv = document.createElement('section');
    qDiv.className = 'question-block';
    qDiv.innerHTML = `
        <label>Текст вопроса</label>
        <input type="text" class="q-desc" value="${escapeHtml(data?.description || '')}" required>
        <label>Сложность</label>
        <select class="q-type">
            <option value="EASY">Легкий</option>
            <option value="MEDIUM">Средний</option>
            <option value="HARD">Сложный</option>
        </select>
        <label>Баллы</label>
        <input type="number" class="q-points" value="${data?.points || 25}" min="0">
        <div class="answers-area"></div>
        <div class="actions">
            <button type="button" class="add-a-btn secondary">Добавить ответ</button>
            <button type="button" class="danger remove-q-btn">Удалить вопрос</button>
        </div>
    `;

    qDiv.querySelector('.q-type').value = data?.type || 'EASY';
    const ansArea = qDiv.querySelector('.answers-area');
    qDiv.querySelector('.add-a-btn').onclick = () => addAnswerUI(ansArea);
    qDiv.querySelector('.remove-q-btn').onclick = () => qDiv.remove();
    container.appendChild(qDiv);

    if (data?.answers?.length) data.answers.forEach(a => addAnswerUI(ansArea, a));
    else {
        addAnswerUI(ansArea, { isCorrect: true });
        addAnswerUI(ansArea);
    }
}

function addAnswerUI(container, data = null) {
    const aDiv = document.createElement('div');
    aDiv.className = 'answer-row';
    aDiv.innerHTML = `
        <label><input type="checkbox" class="a-correct" ${data?.isCorrect ? 'checked' : ''} style="width:auto;min-height:auto;"> Правильный</label>
        <input type="text" placeholder="Текст ответа" class="a-text" value="${escapeHtml(data?.text || '')}" required>
        <div class="actions"><button type="button" class="danger">Удалить</button></div>
    `;
    aDiv.querySelector('button').onclick = () => aDiv.remove();
    container.appendChild(aDiv);
}

function fillForm(quiz) {
    document.getElementById('title').value = quiz.title || '';
    document.getElementById('description').value = quiz.description || '';
    document.getElementById('timeLimit').value = quiz.timeLimitSeconds || quiz.timeLimit || 60;
    (quiz.topicIds || []).forEach(topicId => {
        const input = document.querySelector(`#topic-options input[value="${topicId}"]`);
        if (input) input.checked = true;
    });
    (quiz.questions || []).forEach(q => addQuestionUI(q));
}

async function saveQuiz(e) {
    e.preventDefault();
    const topicIds = Array.from(document.querySelectorAll('#topic-options input:checked')).map(input => Number(input.value));
    if (topicIds.length < 1 || topicIds.length > 3) {
        document.getElementById('form-message').textContent = 'Выберите от 1 до 3 тем.';
        return;
    }

    const payload = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        timeLimitSeconds: Number(document.getElementById('timeLimit').value) || 60,
        isPublic: true,
        type: type,
        topicIds,
        questions: Array.from(document.querySelectorAll('.question-block')).map(q => ({
            description: q.querySelector('.q-desc').value,
            points: Number(q.querySelector('.q-points').value) || 0,
            type: q.querySelector('.q-type').value,
            answers: Array.from(q.querySelectorAll('.answer-row')).map(a => ({
                text: a.querySelector('.a-text').value,
                isCorrect: a.querySelector('.a-correct').checked
            }))
        }))
    };

    const result = await api(quizId ? `/quizzes/api/update/${quizId}` : '/quizzes/api/create', {
        method: 'POST',
        body: JSON.stringify(payload)
    });
    if (result.status.startsWith('2')) {
        window.location.href = type === 'POLL' ? '/polls' : '/quizzes';
    } else {
        document.getElementById('form-message').textContent = result.message;
    }
}

async function deleteQuiz() {
    if (!confirm('Удалить?')) return;
    const result = await api(`/quizzes/api/delete/${quizId}`, { method: 'DELETE' });
    if (result.status.startsWith('2')) {
        window.location.href = type === 'POLL' ? '/polls' : '/quizzes';
    } else {
        document.getElementById('form-message').textContent = result.message;
    }
}

window.onload = initEditor;
