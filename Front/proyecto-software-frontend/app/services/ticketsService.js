import apiClient from "../api/axios";
import { API_PREFIX } from "./config";

const BASE = `${API_PREFIX}/tickets`;

export const ticketsService = {
  crear(payload) {
    return apiClient.post(BASE, payload);
  },

  obtenerPorId(id) {
    return apiClient.get(`${BASE}/${id}`);
  },

  obtenerPorCodigo(codigo) {
    return apiClient.get(`${BASE}/codigo/${codigo}`);
  },

  mios(options = {}) {
    return apiClient.get(`${BASE}/mios`, { params: options });
  },

  listar(options = {}) {
    return apiClient.get(BASE, { params: options });
  },

  asignar(id, agenteId) {
    return apiClient.patch(`${BASE}/${id}/asignar`, { agenteId });
  },

  cambiarEstado(id, estado) {
    return apiClient.patch(`${BASE}/${id}/estado`, { estado });
  },

  historial(id) {
    return apiClient.get(`${BASE}/${id}/historial`);
  },

  comentar(id, texto) {
    return apiClient.post(`${BASE}/${id}/comentarios`, { texto });
  },
};

export default ticketsService;
