document.addEventListener("DOMContentLoaded", () => {
    const requestData = { page: 0, size: 10 };

    fetch('/quizzes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById('quiz-container');
            const quizzes = data.content || [];

            quizzes.forEach(quiz => {
                const card = `
                <div class="card">
                    <h3>${quiz.title}</h3>
                    <p>${quiz.description || ''}</p>
                    <a href="/quiz-view.html?id=${quiz.id}">?????? ????</a>
                    <button onclick="location.href='/admin-quiz.html?id=${quiz.id}'">?????????????</button>
                </div>`;
                container.innerHTML += card;
            });
        });
});