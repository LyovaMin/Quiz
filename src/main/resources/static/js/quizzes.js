const API_URL = 'http://localhost:6769/quizzes';

async function loadQuizzes() {
    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ page: 0, size: 20 })
        });
        const result = await res.json();
        const quizzes = result.data.content; // ??? ?????? result.data ? ??????????? ?? Response

        const container = document.getElementById('quiz-list');
        container.innerHTML = quizzes.map(q => `
            <div class="bg-white p-6 rounded-lg shadow-sm border flex justify-between items-center hover:border-blue-300 transition">
                <div>
                    <h2 class="text-xl font-semibold">${q.title || '??? ????????'}</h2>
                    <p class="text-gray-500 text-sm">${q.description || ''}</p>
                </div>
                <div class="flex gap-2">
                    <button onclick="viewQuiz(${q.id})" class="px-4 py-2 text-blue-600 font-medium">????????</button>
                    <a href="edit.html?id=${q.id}" class="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200">??????</a>
                </div>
            </div>
        `).join('');
    } catch (e) { console.error("?????? ????????:", e); }
}

async function viewQuiz(id) {
    const res = await fetch(`${API_URL}/${id}`);
    const { data: quiz } = await res.json();

    document.getElementById('modal-content').innerHTML = `
        <h2 class="text-2xl font-bold mb-2">${quiz.title}</h2>
        <p class="text-gray-600 mb-6">${quiz.description || ''}</p>
        <div class="space-y-6">
            ${quiz.questions.map((q, i) => `
                <div class="bg-blue-50 p-4 rounded">
                    <p class="font-bold mb-2">${i+1}. ${q.description}</p>
                    <div class="grid grid-cols-2 gap-2">
                        ${q.answers.map(a => `
                            <div class="p-2 rounded ${a.isCorrect ? 'bg-green-100 border border-green-300' : 'bg-white border'} text-sm">
                                ${a.text}
                            </div>
                        `).join('')}
                    </div>
                </div>
            `).join('')}
        </div>
    `;
    document.getElementById('modal').classList.remove('hidden');
}

function closeModal() { document.getElementById('modal').classList.add('hidden'); }
window.onload = loadQuizzes;