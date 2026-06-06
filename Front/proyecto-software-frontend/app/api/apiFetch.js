import apiClient from "./axios";

/**
 * Wrapper compatible con fetch() que pasa por apiClient (axios):
 * - usa la baseURL de VITE_API_URL (sin URLs quemadas)
 * - agrega el token Authorization automáticamente
 * - refresca el token en 401 y reintenta la petición
 *
 * Devuelve un objeto estilo Response: { ok, status, headers, json(), text(), blob() }
 *
 * Uso: apiFetch("/api/usuarios", { method: "PATCH", body: JSON.stringify({...}) })
 * Para descargas binarias: apiFetch(url, { responseType: "blob" })
 */
export default async function apiFetch(path, options = {}) {
  const { method = "GET", headers = {}, body, responseType } = options;

  const config = { url: path, method, headers };
  if (body !== undefined) config.data = body;
  if (responseType) config.responseType = responseType;

  let response;
  try {
    response = await apiClient.request(config);
  } catch (error) {
    // Sin respuesta del servidor => error de red (igual que fetch lanzando excepción)
    if (!error.response) throw error;
    response = error.response;
  }

  return {
    ok: response.status >= 200 && response.status < 300,
    status: response.status,
    headers: response.headers,
    json: async () => response.data,
    text: async () =>
      typeof response.data === "string"
        ? response.data
        : JSON.stringify(response.data),
    blob: async () => response.data,
  };
}
