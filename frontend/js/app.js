import { getAuthToken, getCurrentUser, clearAuthSession } from './api.js';
import { initAuth } from './auth.js';

document.addEventListener('DOMContentLoaded', () => {
    const token = getAuthToken();
    const user = getCurrentUser();

    const authSection = document.getElementById('auth-section');
    const mainApp = document.getElementById('main-app');
    const userGreeting = document.getElementById('user-greeting');
    const logoutBtn = document.getElementById('logout-btn');

    if (token && user) {
        authSection.classList.add('hidden');
        if (mainApp) mainApp.classList.remove('hidden');

        if (userGreeting) {
            userGreeting.textContent = `Olá, ${user.name}!`;
        }

        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                clearAuthSession();
                window.location.reload();
            });
        }

        console.log('Sessão ativa encontrada para:', user.email);
    } else {
        authSection.classList.remove('hidden');
        if (mainApp) mainApp.classList.add('hidden');

        initAuth();
    }
});