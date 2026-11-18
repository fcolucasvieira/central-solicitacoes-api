import { getAuthToken, getCurrentUser, clearAuthSession } from './api.js';
import { initAuth } from './auth.js';
import { 
    loadCategories, 
    loadRequests, 
    initFiltersAndPagination, 
    initRequestModal,
    initTableActions,
    initHistoryModal
} from './requests.js';

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

        // 1. Carrega categorias dinâmicas nos selects
        loadCategories();

        // 2. Inicializa os filtros e a paginação
        initFiltersAndPagination();

        // 3. Inicializa o modal de cadastro passando o callback para recarregar a tabela
        initRequestModal(() => {
            loadRequests();
        });

        // 4. Inicializa os cliques de ação na tabela (Avançar Status e Abrir Histórico)
        initTableActions();

        // 5. Inicializa o modal de Histórico (Linha do Tempo)
        initHistoryModal();

        // 6. Carrega a listagem inicial de solicitações do Back-End
        loadRequests();

        console.log('Sessão ativa encontrada para:', user.email);
    } else {
        authSection.classList.remove('hidden');
        if (mainApp) mainApp.classList.add('hidden');

        initAuth();
    }
});
