import { describe, it, expect, vi, beforeEach } from "vitest";

// Mock del cliente axios: apiFetch debe delegar en él y adaptar la respuesta
vi.mock("../../api/axios", () => ({
  default: { request: vi.fn() },
}));

import apiFetch from "../../api/apiFetch";
import apiClient from "../../api/axios";

const mockRequest = (apiClient as { request: ReturnType<typeof vi.fn> }).request;

describe("apiFetch (wrapper estilo fetch sobre axios)", () => {
  beforeEach(() => {
    mockRequest.mockReset();
  });

  it("respuesta 2xx: ok=true y json() devuelve los datos", async () => {
    const payload = { data: { content: [{ id: 1 }] } };
    mockRequest.mockResolvedValue({ status: 200, headers: {}, data: payload });

    const res = await apiFetch("/api/tickets");

    expect(res.ok).toBe(true);
    expect(res.status).toBe(200);
    await expect(res.json()).resolves.toEqual(payload);
    expect(mockRequest).toHaveBeenCalledWith(
      expect.objectContaining({ url: "/api/tickets", method: "GET" })
    );
  });

  it("error HTTP (4xx/5xx): ok=false sin lanzar excepción, igual que fetch", async () => {
    mockRequest.mockRejectedValue({
      response: { status: 404, headers: {}, data: { message: "No encontrado" } },
    });

    const res = await apiFetch("/api/tickets/999");

    expect(res.ok).toBe(false);
    expect(res.status).toBe(404);
    await expect(res.json()).resolves.toEqual({ message: "No encontrado" });
  });

  it("error de red (sin respuesta): relanza la excepción, igual que fetch", async () => {
    mockRequest.mockRejectedValue(new Error("Network Error"));

    await expect(apiFetch("/api/tickets")).rejects.toThrow("Network Error");
  });

  it("pasa method, body y responseType al cliente", async () => {
    mockRequest.mockResolvedValue({ status: 201, headers: {}, data: {} });

    await apiFetch("/api/tickets", {
      method: "POST",
      body: JSON.stringify({ asunto: "x" }),
      responseType: "blob",
    });

    expect(mockRequest).toHaveBeenCalledWith(
      expect.objectContaining({
        url: "/api/tickets",
        method: "POST",
        data: JSON.stringify({ asunto: "x" }),
        responseType: "blob",
      })
    );
  });
});
