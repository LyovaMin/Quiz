const params = new URLSearchParams(window.location.search);
const quizId = params.get('id');
let type = params.get('type') || 'QUIZ';
let me = null;
let topics = [];

async function initEditor() {
    me = await requireUser();
    if (!me) return;
    
    document.getElementById('add-q-btn').onclick = () => addQuestionUI();
    document.getElementById('quiz-form').onsubmit = saveQuiz;
    document.getElementById('delete-btn').onclick = deleteQuiz;
    document.getElementById('add-topic-btn').onclick = addTopicSelector;

    const typeSelect = document.getElementById('type-select');
    if (typeSelect) {
        typeSelect.value = type;
        typeSelect.onchange = () => {
            type = typeSelect.value;
            document.getElementById('form-title').textContent = type === 'POLL' ? 'Новый опрос' : 'Новый квиз';
            document.getElementById('add-q-btn').style.display = type === 'POLL' ? 'none' : 'inline-flex';
            // show/hide poll section
            const pollSection = document.getElementById('poll-section');
            if (pollSection) pollSection.style.display = type === 'POLL' ? 'block' : 'none';
            // ensure at least one question present for poll
            const qContainer = document.getElementById('questions-container');
            if (type === 'POLL' && qContainer.children.length === 0) addQuestionUI();
        };
        // apply initial state for provided type (important when opening editor with ?type=POLL)
        typeSelect.onchange();
    }

    await loadTopics(); // Ждем загрузки тем

    // setup poll-specific controls
    const pollSection = document.getElementById('poll-section');
    const pollOptionsContainer = document.getElementById('poll-options-container');
    const addPollOptionBtn = document.getElementById('add-poll-option');
    if (addPollOptionBtn) addPollOptionBtn.onclick = () => addPollOption();

    function addPollOption(value = '') {
        const row = document.createElement('div');
        row.className = 'answer-row';
        row.innerHTML = `
            <input type="text" class="poll-option-text" placeholder="Текст варианта" value="${escapeHtml(value)}">
            <div class="actions"><button type="button" class="danger">Удалить</button></div>
        `;
        row.querySelector('button').onclick = () => row.remove();
        pollOptionsContainer.appendChild(row);
    }

    if (quizId) {
        document.getElementById('form-title').textContent = 'Редактирование';
        document.getElementById('delete-btn').style.display = 'inline-flex';
        const result = await api(`/quizzes/api/${quizId}`);
        fillForm(result.data);
    } else {
        document.getElementById('form-title').textContent = type === 'POLL' ? 'Новый опрос' : 'Новый квиз';
        addQuestionUI();
        addTopicSelector(); // Добавляем первый селектор темы
    }

    if (type === 'POLL') {
        document.getElementById('add-q-btn').style.display = 'none';
    }
}

async function loadTopics() {
    const response = await api('/analitics/topics');
    topics = response.status.startsWith('2') ? response.data || [] : [];
}

function addTopicSelector() {
    const container = document.getElementById('topic-selection');
    if (container.children.length >= 3) {
        return; // Ограничение в 3 темы
    }
    const div = document.createElement('div');
    div.className = 'topic-selector-row';
    const select = document.createElement('select');
    select.innerHTML = topics.map(topic => `<option value="${topic.id}">${escapeHtml(topic.name)}</option>`).join('');
    div.appendChild(select);

    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.className = 'danger';
    removeBtn.textContent = 'Удалить';
    removeBtn.onclick = () => div.remove();
    div.appendChild(removeBtn);

    container.appendChild(div);
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
    if (type === 'POLL') {
        aDiv.innerHTML = `
            <input type="text" placeholder="Текст варианта" class="a-text" value="${escapeHtml(data?.text || '')}" required>
            <div class="actions"><button type="button" class="danger">Удалить</button></div>
        `;
    } else {
        aDiv.innerHTML = `
            <label><input type="checkbox" class="a-correct" ${data?.isCorrect ? 'checked' : ''} style="width:auto;min-height:auto;"> Правильный</label>
            <input type="text" placeholder="Текст ответа" class="a-text" value="${escapeHtml(data?.text || '')}" required>
            <div class="actions"><button type="button" class="danger">Удалить</button></div>
        `;
    }
    aDiv.querySelector('button').onclick = () => aDiv.remove();
    container.appendChild(aDiv);
}

function fillForm(quiz) {
    document.getElementById('title').value = quiz.title || '';
    document.getElementById('description').value = quiz.description || '';
    document.getElementById('timeLimit').value = quiz.timeLimitSeconds || quiz.timeLimit || 60;

    const typeSelect = document.getElementById('type-select');
    if (typeSelect) {
        type = quiz.type || typeSelect.value;
        typeSelect.value = type;
    }
    
    const topicContainer = document.getElementById('topic-selection');
    topicContainer.innerHTML = ''; // Очищаем контейнер
    (quiz.topicIds || []).forEach(topicId => {
        addTopicSelector();
        const lastSelect = topicContainer.lastChild.querySelector('select');
        if (lastSelect) {
            lastSelect.value = topicId;
        }
    });

    // clear existing questions/poll options
    document.getElementById('questions-container').innerHTML = '<h2>Вопросы</h2>';
    const pollSection = document.getElementById('poll-section');
    const pollOptionsContainer = document.getElementById('poll-options-container');
    pollOptionsContainer.innerHTML = '';

    if (type === 'POLL') {
        // populate poll UI from the first question
        pollSection.style.display = 'block';
        document.getElementById('add-q-btn').style.display = 'none';
        const q = (quiz.questions || [])[0] || { description: '', answers: [] };
        document.getElementById('poll-question').value = q.description || '';
        (q.answers || []).forEach(a => addPollOption(a.text));
    } else {
        pollSection.style.display = 'none';
        (quiz.questions || []).forEach(q => addQuestionUI(q));
    }
}

async function saveQuiz(e) {
    e.preventDefault();
    const topicIds = Array.from(document.querySelectorAll('#topic-selection select')).map(select => Number(select.value));
    if (topicIds.length < 1 || topicIds.length > 3) {
        document.getElementById('form-message').textContent = 'Выберите от 1 до 3 тем.';
        return;
    }

    let questionsPayload = [];
    if (type === 'POLL') {
        const pollQuestion = document.getElementById('poll-question').value;
        const optionEls = Array.from(document.querySelectorAll('.poll-option-text'));
        const answers = optionEls.map(el => ({ text: el.value }));
        questionsPayload = [{ description: pollQuestion, points: 0, type: 'EASY', answers }];
    } else {
        questionsPayload = Array.from(document.querySelectorAll('.question-block')).map(q => ({
            description: q.querySelector('.q-desc').value,
            points: Number(q.querySelector('.q-points').value) || 0,
            type: q.querySelector('.q-type').value,
            answers: Array.from(q.querySelectorAll('.answer-row')).map(a => ({
                text: a.querySelector('.a-text').value,
                isCorrect: (a.querySelector('.a-correct') ? a.querySelector('.a-correct').checked : false)
            }))
        }));
    }

    const payload = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        timeLimitSeconds: Number(document.getElementById('timeLimit').value) || 60,
        isPublic: true,
        type: type,
        topicIds,
        questions: questionsPayload
    };

    const endpoint = quizId ? (type === 'POLL' ? `/polls/api/update/${quizId}` : `/quizzes/api/update/${quizId}`) : (type === 'POLL' ? '/polls/api/create' : '/quizzes/api/create');
    const result = await api(endpoint, {
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