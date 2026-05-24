const pollId = window.location.pathname.split('/').pop();
const userId = 1; // Mock user ID

async function loadPoll() {
    const response = await fetch(`/quizzes/api/${pollId}`);
    const result = await response.json();
    const poll = result.data;

    document.getElementById('poll-title').textContent = poll.title;
    document.getElementById('poll-description').textContent = poll.description;

    const optionsContainer = document.getElementById('poll-options');
    optionsContainer.innerHTML = poll.questions[0].answers.map(answer => `
        <button class="button" onclick="vote(${answer.id})">${answer.text}</button>
    `).join('');
}

async function vote(answerId) {
    const response = await fetch(`/polls/api/${pollId}/vote?answerId=${answerId}&userId=${userId}`, {
        method: 'POST'
    });

    if (response.ok) {
        const result = await response.json();
        showResults(result.data);
    } else {
        const error = await response.json();
        alert(error.message);
        // If already voted, show results
        if (response.status === 400) {
            const resultsResponse = await fetch(`/polls/api/${pollId}/results`);
            const results = await resultsResponse.json();
            showResults(results.data);
        }
    }
}

function showResults(poll) {
    document.getElementById('poll-container').style.display = 'none';
    document.getElementById('results-container').style.display = 'block';

    const totalVotes = poll.questions[0].answers.reduce((sum, answer) => sum + answer.votes, 0);
    const resultsChart = document.getElementById('results-chart');

    resultsChart.innerHTML = poll.questions[0].answers.map(answer => {
        const percentage = totalVotes > 0 ? ((answer.votes / totalVotes) * 100).toFixed(1) : 0;
        return `
            <div class="result-bar">
                <span>${answer.text} (${answer.votes} ???????)</span>
                <div style="width: ${percentage}%; background-color: #4CAF50;">${percentage}%</div>
            </div>
        `;
    }).join('');
}

window.onload = () => {
    loadPoll();
    if (window.setCurrentUser) {
        window.setCurrentUser();
    }
};
