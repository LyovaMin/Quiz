document.getElementById('register-form').onsubmit = async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const error = document.getElementById('auth-error');

    const result = await api('/api/register', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });

    if (!result.status.startsWith('2')) {
        error.textContent = result.message || 'Не удалось зарегистрироваться';
        return;
    }
    window.location.href = '/login';
};
