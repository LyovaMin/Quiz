async function api(path, options = {}) {
    const isFormData = options.body instanceof FormData;
    const response = await fetch(path, {
        headers: { ...(isFormData ? {} : { 'Content-Type': 'application/json' }), ...(options.headers || {}) },
        credentials: 'same-origin',
        ...options
    });
    return response.json();
}

async function currentUser() {
    const response = await api('/api/me');
    return response.status.startsWith('2') ? response.data : null;
}

async function requireUser() {
    const user = await currentUser();
    if (!user) {
        window.location.href = '/login';
        return null;
    }
    const label = document.querySelector('[data-current-user]');
    if (label) label.textContent = user.name;
    return user;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

async function logout() {
    await api('/api/logout', { method: 'POST' });
    window.location.href = '/login';
}
