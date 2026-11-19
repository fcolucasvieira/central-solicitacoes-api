import { apiFetch, setAuthSession } from './api.js';

export function initAuth() {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const alertBox = document.getElementById('auth-alert');

    tabLogin.addEventListener('click', () => {
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        hideAlert();
    });

    tabRegister.addEventListener('click', () => {
        tabRegister.classList.add('active');
        tabLogin.classList.remove('active');
        registerForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
        hideAlert();
    });

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();

        const submitBtn = loginForm.querySelector('button[type="submit"]');
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        try {
            setLoading(submitBtn, true, 'Entrando...');

            const response = await apiFetch('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });

            // O Back-End devolve accessToken e name. Passamos como parâmetros
            setAuthSession(response.accessToken, email, response.name);

            showAlert('Login realizado com sucesso! Entrando...', 'success');

            setTimeout(() => {
                window.location.reload();
            }, 600);

        } catch (error) {
            showAlert(error.message, 'danger');
        } finally {
            setLoading(submitBtn, false, 'Entrar no Sistema');
        }
    });

    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideAlert();

        const submitBtn = registerForm.querySelector('button[type="submit"]');
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;

        try {
            setLoading(submitBtn, true, 'Cadastrando...');

            await apiFetch('/auth/register', {
                method: 'POST',
                body: JSON.stringify({ name, email, password })
            });

            showAlert('Conta criada com sucesso! Faça login para continuar.', 'success');

            registerForm.reset();
            tabLogin.click();

        } catch (error) {
            if (error.data?.errors) {
                const errorList = Object.values(error.data.errors).join('<br>');
                showAlert(errorList, 'danger', true);
            } else {
                showAlert(error.message, 'danger');
            }
        } finally {
            setLoading(submitBtn, false, 'Criar Conta');
        }
    });

    function showAlert(message, type = 'danger', isHtml = false) {
        alertBox.className = `alert alert-${type}`;
        if (isHtml) {
            alertBox.innerHTML = message;
        } else {
            alertBox.textContent = message;
        }
        alertBox.style.display = 'block';
    }

    function hideAlert() {
        alertBox.style.display = 'none';
        alertBox.textContent = '';
    }

    function setLoading(button, isLoading, text) {
        button.disabled = isLoading;
        button.textContent = text;
    }
}