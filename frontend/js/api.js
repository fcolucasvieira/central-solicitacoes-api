const API_BASE_URL = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
    ? "http://localhost:8080"
    : "https://SEU_DOMINIO_DE_PRODUCAO_AQUI.com";

export function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            window
                .atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Falha ao decodificar token JWT:', error);
        return null;
    }
}

export function getAuthToken() {
    const token = localStorage.getItem("token");
    if (!token || token === "undefined" || token === "null") {
        return null;
    }
    return token;
}

export function setAuthSession(accessToken, emailFallback = '', nameFallback = '') {
    localStorage.setItem("token", accessToken);

    const payload = parseJwt(accessToken);
    const email = payload?.sub || emailFallback;

    const user = {
        email: email,
        name: nameFallback || (email ? email.split('@')[0] : 'Usuário')
    };

    localStorage.setItem("user", JSON.stringify(user));
}

export function clearAuthSession() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}

export function getCurrentUser() {
    const userJson = localStorage.getItem("user");
    if (!userJson || userJson === "undefined" || userJson === "null") {
        return null;
    }
    try {
        return JSON.parse(userJson);
    } catch (e) {
        localStorage.removeItem("user");
        return null;
    }
}

export async function apiFetch(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    const token = getAuthToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);

        if (response.status === 204) {
            return null;
        }

        if (response.status === 401 && !endpoint.startsWith('/auth/')) {
            clearAuthSession();
            window.location.reload();
            return null;
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            const errorMessage = data?.detail || data?.title || 'Ocorreu um erro na requisição';

            const error = new Error(errorMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    } catch (error) {
        if (!error.status) {
            throw new Error('Não foi possível conectar à API. Verifique se o Back-End está rodando na porta 8080.');
        }
        throw error;
    }
}