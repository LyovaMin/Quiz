const API_URL = 'http://localhost:6769/quizzes';
const params = new URLSearchParams(window.location.search);
const quizId = params.get('id');

if (quizId) {
    document.getElementById('form-title').innerText = '??????????????';
    document.getElementById('delete-btn').classList.remove('hidden');
    fetch(`${API_URL}/${quizId}`).then(r => r.json()).then(res => fillForm(res.data));
}

document.getElementById('add-q-btn').onclick = () => addQuestionUI();

function addQuestionUI(data = null) {
    const container = document.getElementById('questions-container');
    const qId = Date.now();
    const qDiv = document.createElement('div');
    qDiv.className = 'question-block border p-5 rounded-lg bg-white shadow-sm relative';
    qDiv.innerHTML = `
        <button type="button" onclick="this.parentElement.remove()" class="absolute top-2 right-2 text-red-400 hover:text-red-600">?</button>
        <input type="text" placeholder="????? ???????" class="q-desc w-full font-semibold border-b mb-4 focus:outline-none focus:border-blue-500" value="${data?.description || ''}">
        <div class="answers-area space-y-2 mb-3"></div>
        <button type="button" class="add-a-btn text-blue-500 text-sm font-medium hover:underline">+ ??????? ??????</button>
    `;

    const ansArea = qDiv.querySelector('.answers-area');
    qDiv.querySelector('.add-a-btn').onclick = () => addAnswerUI(ansArea);

    container.appendChild(qDiv);

    if (data?.answers) data.answers.forEach(a => addAnswerUI(ansArea, a));
    else addAnswerUI(ansArea);
}

function addAnswerUI(container, data = null) {
    const aDiv = document.createElement('div');
    aDiv.className = 'answer-row flex items-center gap-3';
    aDiv.innerHTML = `
        <input type="checkbox" class="a-correct w-5 h-5 accent-green-500" ${data?.isCorrect ? 'checked' : ''}>
        <input type="text" placeholder="??????? ??????" class="a-text flex-1 border rounded p-1 text-sm" value="${data?.text || ''}">
        <button type="button" onclick="this.parentElement.remove()" class="text-gray-300 hover:text-red-500 text-xs">???????</button>
    `;
    container.appendChild(aDiv);
}

function fillForm(quiz) {
    document.getElementById('title').value = quiz.title;
    document.getElementById('description').value = quiz.description;
    quiz.questions.forEach(q => addQuestionUI(q));
}

document.getElementById('quiz-form').onsubmit = async (e) => {
    e.preventDefault();
    const payload = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        questions: Array.from(document.querySelectorAll('.question-block')).map(q => ({
            description: q.querySelector('.q-desc').value,
            points: 1,
            type: 'EASY',
            answers: Array.from(q.querySelectorAll('.answer-row')).map(a => ({
                text: a.querySelector('.a-text').value,
                isCorrect: a.querySelector('.a-correct').checked
            }))
        }))
    };

    const url = quizId ? `${API_URL}/update/${quizId}` : `${API_URL}/create`;
    await fetch(url, {
        method: quizId ? 'POST' : 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });
    window.location.href = 'index.html';
};

document.getElementById('delete-btn').onclick = async () => {
    if (confirm('????? ????????')) {
        await fetch(`${API_URL}/delete/${quizId}`, { method: 'DELETE' });
        window.location.href = 'index.html';
    }
};