import { describe, it, expect, vi, beforeEach } from "vitest";

/**
 * PRUEBAS UNITARIAS — ticketsService.js
 *
 * Tipo de caja: BLANCA — se verifican los métodos del servicio de tickets,
 * comprobando que cada llamada usa el verbo HTTP y la URL correcta.
 *
 * Herramienta: Vitest + vi.mock()
 */

vi.mock("~/api/axios", () => ({
  default: {
    post:   vi.fn(),
    get:    vi.fn(),
    patch:  vi.fn(),
    delete: vi.fn(),
  },
}));

import apiClient from "~/api/axios";
import { ticketsService } from "~/services/ticketsService";

const mockPost  = vi.mocked(apiClient.post);
const mockGet   = vi.mocked(apiClient.get);
const mockPatch = vi.mocked(apiClient.patch);

describe("ticketsService — Pruebas Unitarias (Caja Blanca)", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    mockPost.mockResolvedValue({ data: {} });
    mockGet.mockResolvedValue({ data: {} });
    mockPatch.mockResolvedValue({ data: {} });
  });

  // ─── crear ────────────────────────────────────────────────────────────────

  it("PU-TKT-SVC-01 | crear() llama POST /api/tickets con el payload completo", async () => {
    const payload = {
      titulo: "Error en sistema",
      descripcion: "El sistema falla",
      tipo: "incidente",
      prioridad: "alta",
      categoriaId: 1,
    };
    await ticketsService.crear(payload);

    expect(mockPost).toHaveBeenCalledOnce();
    expect(mockPost).toHaveBeenCalledWith("/api/tickets", payload);
  });

  // ─── obtenerPorId ─────────────────────────────────────────────────────────

  it("PU-TKT-SVC-02 | obtenerPorId() llama GET /api/tickets/:id", async () => {
    await ticketsService.obtenerPorId(42);

    expect(mockGet).toHaveBeenCalledOnce();
    expect(mockGet).toHaveBeenCalledWith("/api/tickets/42");
  });

  // ─── obtenerPorCodigo ─────────────────────────────────────────────────────

  it("PU-TKT-SVC-03 | obtenerPorCodigo() llama GET /api/tickets/codigo/:codigo", async () => {
    await ticketsService.obtenerPorCodigo("TK-0001");

    expect(mockGet).toHaveBeenCalledWith("/api/tickets/codigo/TK-0001");
  });

  // ─── mios ─────────────────────────────────────────────────────────────────

  it("PU-TKT-SVC-04 | mios() llama GET /api/tickets/mios sin params cuando no se pasan", async () => {
    await ticketsService.mios();

    expect(mockGet).toHaveBeenCalledWith("/api/tickets/mios", { params: {} });
  });

  it("PU-TKT-SVC-05 | mios() pasa los parámetros de paginación correctamente", async () => {
    await ticketsService.mios({ page: 0, size: 10 });

    expect(mockGet).toHaveBeenCalledWith("/api/tickets/mios", {
      params: { page: 0, size: 10 },
    });
  });

  // ─── listar ───────────────────────────────────────────────────────────────

  it("PU-TKT-SVC-06 | listar() llama GET /api/tickets sin params cuando no se pasan", async () => {
    await ticketsService.listar();

    expect(mockGet).toHaveBeenCalledWith("/api/tickets", { params: {} });
  });

  it("PU-TKT-SVC-07 | listar() pasa filtros de estado y agente correctamente", async () => {
    await ticketsService.listar({ estado: "ABIERTO", agenteId: 5 });

    expect(mockGet).toHaveBeenCalledWith("/api/tickets", {
      params: { estado: "ABIERTO", agenteId: 5 },
    });
  });

  // ─── asignar ──────────────────────────────────────────────────────────────

  it("PU-TKT-SVC-08 | asignar() llama PATCH /api/tickets/:id/asignar con agenteId", async () => {
    await ticketsService.asignar(10, 3);

    expect(mockPatch).toHaveBeenCalledOnce();
    expect(mockPatch).toHaveBeenCalledWith("/api/tickets/10/asignar", { agenteId: 3 });
  });

  // ─── cambiarEstado ────────────────────────────────────────────────────────

  it("PU-TKT-SVC-09 | cambiarEstado() llama PATCH /api/tickets/:id/estado con el estado", async () => {
    await ticketsService.cambiarEstado(7, "RESUELTO");

    expect(mockPatch).toHaveBeenCalledOnce();
    expect(mockPatch).toHaveBeenCalledWith("/api/tickets/7/estado", { estado: "RESUELTO" });
  });
});
