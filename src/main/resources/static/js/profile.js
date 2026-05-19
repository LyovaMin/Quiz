let me = null;
let topics = [];
let adminUsers = [];

async function loadProfilePage() {
    me = await requireUser();
    if (!me) return;

    fillProfileForm(me);
    document.getElementById('edit-profile-btn').onclick = openProfileEdit;
    document.getElementById('cancel-profile-edit').onclick = closeProfileEdit;
    document.getElementById('profile-form').onsubmit = saveProfile;
    document.getElementById('profile-avatar-file').onchange = updateAvatarPreview;
    await loadTopics('profile-topic');
    document.getElementById('profile-period').onchange = loadProfile;
    document.getElementById('profile-topic').onchange = loadProfile;
    await loadProfile();

    if (me.role === 'ADMIN') {
        await initAdminUsers();
    }
}

async function loadTopics(selectId) {
    const response = await api('/analitics/topics');
    topics = response.status.startsWith('2') ? response.data || [] : [];
    document.getElementById(selectId).innerHTML = '<option value="">Все темы</option>' + topics
        .map(topic => `<option value="${topic.id}">${escapeHtml(topic.name)}</option>`)
        .join('');
}

async function loadProfile() {
    const period = document.getElementById('profile-period').value;
    const topicId = document.getElementById('profile-topic').value;
    const response = await api(`/analitics/my-stat?username=${encodeURIComponent(me.name)}&period=${period}${topicId ? `&topicId=${topicId}` : ''}`);
    if (!response.status.startsWith('2')) return;

    const profile = response.data;
    setAvatar(profile.user.avatar, profile.user.login);
    document.getElementById('profile-login').textContent = profile.user.login;

    document.getElementById('profile-stats').innerHTML = `
        <div class="stat-card">
            <span>Правильные</span>
            <strong>${profile.stats.correctPercent}%</strong>
        </div>
        <div class="stat-card">
            <span>Ответы</span>
            <strong>${profile.stats.correctAnswers}/${profile.stats.totalAnswers}</strong>
        </div>
        <div class="stat-card">
            <span>Баллы</span>
            <strong>${profile.stats.score}</strong>
        </div>
    `;

    document.getElementById('profile-topic-stats').innerHTML = (profile.byTopic || [])
        .map(row => `
            <div class="topic-row">
                <span>${escapeHtml(row.topic)}</span>
                <strong>${row.correctPercent}%</strong>
            </div>
        `).join('') || '<p class="muted">Нет данных по темам.</p>';
}

async function saveProfile(event) {
    event.preventDefault();
    const message = document.getElementById('profile-message');
    message.textContent = '';

    const formData = new FormData();
    formData.append('username', document.getElementById('profile-username').value.trim());
    formData.append('password', document.getElementById('profile-password').value);
    appendFile(formData, 'avatar', document.getElementById('profile-avatar-file'));

    const response = await api('/api/me', {
        method: 'PUT',
        body: formData
    });

    if (!response.status.startsWith('2')) {
        message.textContent = response.message || 'Не удалось сохранить профиль.';
        return;
    }

    me = response.data;
    fillProfileForm(me);
    closeProfileEdit();
    const label = document.querySelector('[data-current-user]');
    if (label) label.textContent = me.name;
    message.textContent = 'Профиль сохранен.';
    await loadProfile();
}

function openProfileEdit() {
    document.getElementById('profile-form').style.display = 'block';
    document.getElementById('edit-profile-btn').style.display = 'none';
}

function closeProfileEdit() {
    document.getElementById('profile-form').style.display = 'none';
    document.getElementById('edit-profile-btn').style.display = 'inline-flex';
    document.getElementById('profile-password').value = '';
    document.getElementById('profile-avatar-file').value = '';
    document.getElementById('profile-message').textContent = '';
    fillProfileForm(me);
}

function fillProfileForm(user) {
    document.getElementById('profile-username').value = user.name || '';
    setAvatar(user.image, user.name);
}

function updateAvatarPreview() {
    const input = document.getElementById('profile-avatar-file');
    if (input.files?.[0]) {
        document.getElementById('profile-avatar').src = URL.createObjectURL(input.files[0]);
    }
}

async function initAdminUsers() {
    document.getElementById('admin-users-panel').style.display = 'block';
    document.getElementById('admin-user-select').onchange = fillAdminUserForm;
    document.getElementById('admin-user-form').onsubmit = saveAdminUser;
    await loadAdminUsers();
}

async function loadAdminUsers() {
    const response = await api('/api/users');
    adminUsers = response.status.startsWith('2') ? response.data || [] : [];
    document.getElementById('admin-user-select').innerHTML = adminUsers
        .map(user => `<option value="${user.id}">${escapeHtml(user.name)} (${escapeHtml(user.role)})</option>`)
        .join('');
    fillAdminUserForm();
}

function fillAdminUserForm() {
    const user = selectedAdminUser();
    if (!user) return;
    document.getElementById('admin-username').value = user.name || '';
    document.getElementById('admin-password').value = '';
    document.getElementById('admin-avatar-file').value = '';
    document.getElementById('admin-user-message').textContent = '';
}

async function saveAdminUser(event) {
    event.preventDefault();
    const user = selectedAdminUser();
    if (!user) return;

    const formData = new FormData();
    formData.append('username', document.getElementById('admin-username').value.trim());
    formData.append('password', document.getElementById('admin-password').value);
    appendFile(formData, 'avatar', document.getElementById('admin-avatar-file'));

    const response = await api(`/api/users/${user.id}`, {
        method: 'PUT',
        body: formData
    });

    const message = document.getElementById('admin-user-message');
    if (!response.status.startsWith('2')) {
        message.textContent = response.message || 'Не удалось сохранить пользователя.';
        return;
    }

    message.textContent = 'Пользователь сохранен.';
    if (response.data.id === me.id) {
        me = response.data;
        fillProfileForm(me);
        await loadProfile();
    }
    await loadAdminUsers();
}

function selectedAdminUser() {
    const id = Number(document.getElementById('admin-user-select').value);
    return adminUsers.find(user => user.id === id);
}

function appendFile(formData, name, input) {
    if (input.files?.[0]) {
        formData.append(name, input.files[0]);
    }
}

function setAvatar(image, login) {
    const avatar = document.getElementById('profile-avatar');
    avatar.src = image || `https://ui-avatars.com/api/?name=${encodeURIComponent(login || 'User')}&background=9333ea&color=fff`;
}

window.onload = loadProfilePage;
