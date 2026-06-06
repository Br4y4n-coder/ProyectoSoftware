import { describe, it, expect, vi, beforeEach } from "vitest";

/**
 * PRUEBAS UNITARIAS — authService.js
 *
 * Tipo de caja: BLANCA — se prueba que cada método del servicio llama
 * al cliente HTTP con el verbo, ruta y payload correctos.
 * El apiClient se mockea completamente para aislar el servicio.
 *
 * Herramienta: Vitest + vi.mock()
 */

// Mock del cliente axios antes de importar el servicio
vi.mock("~/api/axios", () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

import apiClient from "~/api/axios";
import { authService } from "~/services/authService";

const mockPost = vi.mocked(apiClient.post);
const mockGet  = vi.mocked(apiClient.get);

describe("authService — Pruebas Unitarias (Caja Blanca)", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    mockPost.mockResolvedValue({ data: { status: 200 } });
    mockGet.mockResolvedValue({ data: { status: 200 } });
  });

  // ─── register ─────────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-01 | register() llama POST /api/auth/register con payload", async () => {
    const payload = { nombres: "Ana", correo: "ana@test.com", contrasena: "pass" };
    await authService.register(payload);

    expect(mockPost).toHaveBeenCalledOnce();
    expect(mockPost).toHaveBeenCalledWith("/api/auth/register", payload);
  });

  // ─── verifyEmail ──────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-02 | verifyEmail() llama GET /api/auth/verify-email con token como param", async () => {
    await authService.verifyEmail("abc-token");

    expect(mockGet).toHaveBeenCalledOnce();
    expect(mockGet).toHaveBeenCalledWith(
      "/api/auth/verify-email",
      { params: { token: "abc-token" } }
    );
  });

  // ─── login ────────────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-03 | login() llama POST /api/auth/login con credenciales", async () => {
    const creds = { correo: "juan@test.com", contrasena: "pass123" };
    await authService.login(creds);

    expect(mockPost).toHaveBeenCalledWith("/api/auth/login", creds);
  });

  // ─── refresh ──────────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-04 | refresh() llama POST /api/auth/refresh con el token de refresco", async () => {
    await authService.refresh("refresh-tok-xyz");

    expect(mockPost).toHaveBeenCalledWith(
      "/api/auth/refresh",
      { refreshToken: "refresh-tok-xyz" }
    );
  });

  // ─── logout ───────────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-05 | logout() con token llama POST /api/auth/logout con body", async () => {
    await authService.logout("my-refresh-token");

    expect(mockPost).toHaveBeenCalledWith(
      "/api/auth/logout",
      { refreshToken: "my-refresh-token" }
    );
  });

  it("PU-AUTH-SVC-06 | logout() sin token llama POST /api/auth/logout sin body", async () => {
    await authService.logout(undefined);

    expect(mockPost).toHaveBeenCalledWith("/api/auth/logout", undefined);
  });

  // ─── forgotPassword ───────────────────────────────────────────────────────

  it("PU-AUTH-SVC-07 | forgotPassword() llama POST /api/auth/forgot-password con correo", async () => {
    await authService.forgotPassword("user@test.com");

    expect(mockPost).toHaveBeenCalledWith(
      "/api/auth/forgot-password",
      { correo: "user@test.com" }
    );
  });

  // ─── resetPassword ────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-08 | resetPassword() llama POST /api/auth/reset-password con payload", async () => {
    const payload = { token: "reset-tok", nuevaContrasena: "NuevaPass123!" };
    await authService.resetPassword(payload);

    expect(mockPost).toHaveBeenCalledWith("/api/auth/reset-password", payload);
  });

  // ─── me ───────────────────────────────────────────────────────────────────

  it("PU-AUTH-SVC-09 | me() llama GET /api/auth/me sin params", async () => {
    await authService.me();

    expect(mockGet).toHaveBeenCalledWith("/api/auth/me");
  });
});
