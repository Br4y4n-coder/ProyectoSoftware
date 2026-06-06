import axios from "axios";
import {
  API_BASE_URL,
  API_PREFIX,
  API_TIMEOUT_MS,
  AUTH_TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_KEY,
} from "../services/config";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT_MS,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== "undefined") {
      // Intentar obtener token con ambas claves posibles
      let token = window.localStorage.getItem(AUTH_TOKEN_KEY);
      if (!token) {
        token = window.localStorage.getItem('token');
      }
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let pendingQueue = [];

function flushQueue(error, newToken) {
  pendingQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error);
      return;
    }
    config.headers.Authorization = `Bearer ${newToken}`;
    resolve(apiClient(config));
  });
  pendingQueue = [];
}

function clearTokensAndRedirect() {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(AUTH_TOKEN_KEY);
  window.localStorage.removeItem('token');
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  window.localStorage.removeItem(USER_KEY);
  // Estando ya en una pantalla pública dejamos que el componente muestre el error
  // (ej. credenciales inválidas en login) en lugar de redirigir.
  if (window.location.pathname.startsWith("/auth/")) return;
  window.location.href = "/auth/login";
}

function isAuthEndpoint(url = "", suffix) {
  return url.endsWith(`${API_PREFIX}/auth/${suffix}`);
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (status !== 401 || !originalRequest) {
      return Promise.reject(error);
    }

    const url = originalRequest.url || "";
    if (isAuthEndpoint(url, "login") || isAuthEndpoint(url, "refresh")) {
      if (isAuthEndpoint(url, "refresh")) clearTokensAndRedirect();
      return Promise.reject(error);
    }

    if (originalRequest._retried) {
      clearTokensAndRedirect();
      return Promise.reject(error);
    }

    const refreshToken =
      typeof window !== "undefined"
        ? window.localStorage.getItem(REFRESH_TOKEN_KEY)
        : null;
    if (!refreshToken) {
      clearTokensAndRedirect();
      return Promise.reject(error);
    }

    // Si ya hay un refresh en curso, encolamos los requests concurrentes para
    // reintentarlos con el token nuevo y disparar una sola llamada a /refresh.
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject, config: originalRequest });
      });
    }

    originalRequest._retried = true;
    isRefreshing = true;

    try {
      // axios crudo, no apiClient: evita re-entrar en este interceptor.
      const refreshResponse = await axios.post(
        `${API_BASE_URL}${API_PREFIX}/auth/refresh`,
        { refreshToken },
        {
          headers: { "Content-Type": "application/json" },
          timeout: API_TIMEOUT_MS,
        }
      );

      const auth = refreshResponse.data?.data;
      const newAccessToken = auth?.accessToken;
      const newRefreshToken = auth?.refreshToken;

      if (!newAccessToken) {
        throw new Error("Respuesta de refresh sin accessToken");
      }

      window.localStorage.setItem(AUTH_TOKEN_KEY, newAccessToken);
      window.localStorage.setItem('token', newAccessToken);
      if (newRefreshToken) {
        window.localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
      }

      flushQueue(null, newAccessToken);

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      flushQueue(refreshError, null);
      clearTokensAndRedirect();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default apiClient;