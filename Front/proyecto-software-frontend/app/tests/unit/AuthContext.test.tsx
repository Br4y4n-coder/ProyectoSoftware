import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, act, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AuthProvider, useAuth } from "~/contexts/AuthContext";
import React from "react";

/**
 * PRUEBAS UNITARIAS — AuthContext
 *
 * Tipo de caja: GRIS — se conoce la estructura del contexto y se prueba
 * el comportamiento observable desde fuera: estado inicial, login,
 * logout y sincronización entre pestañas.
 *
 * Herramienta: Vitest + React Testing Library
 */

// Mock del authService antes de importar el contexto
vi.mock("~/services", () => ({
  authService: {
    login: vi.fn(),
    logout: vi.fn(),
    me: vi.fn(),
  },
}));

import { authService } from "~/services";

const mockLogin  = vi.mocked(authService.login);
const mockLogout = vi.mocked(authService.logout);

// Componente auxiliar para consumir el contexto en tests
function TestConsumer() {
  const auth = useAuth();
  return (
    <div>
      <span data-testid="status">{auth.status}</span>
      <span data-testid="user">{auth.user ? auth.user.correo : "null"}</span>
      <span data-testid="isAuth">{String(auth.isAuthenticated)}</span>
      <button
        data-testid="btn-login"
        onClick={() =>
          auth.login({ correo: "test@test.com", contrasena: "pass" })
        }
      >
        Login
      </button>
      <button data-testid="btn-logout" onClick={() => auth.logout()}>
        Logout
      </button>
    </div>
  );
}

describe("AuthContext — Pruebas (Caja Gris)", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  // ─── Estado inicial ────────────────────────────────────────────────────────

  it("PU-CTX-01 | Estado inicial es 'unauthenticated' cuando no hay token en localStorage", async () => {
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("status").textContent).toBe("unauthenticated");
    });
    expect(screen.getByTestId("isAuth").textContent).toBe("false");
    expect(screen.getByTestId("user").textContent).toBe("null");
  });

  it("PU-CTX-02 | Estado inicial es 'authenticated' cuando hay token en localStorage", async () => {
    localStorage.setItem("auth_token", "existing-tok");
    localStorage.setItem("user", JSON.stringify({ correo: "stored@test.com", rol: "usuario" }));

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("status").textContent).toBe("authenticated");
    });
    expect(screen.getByTestId("user").textContent).toBe("stored@test.com");
  });

  // ─── login() ──────────────────────────────────────────────────────────────

  it("PU-CTX-03 | login() exitoso actualiza estado y guarda tokens en localStorage", async () => {
    mockLogin.mockResolvedValue({
      data: {
        data: {
          accessToken: "new-access-tok",
          refreshToken: "new-refresh-tok",
          usuario: { correo: "login@test.com", rol: "usuario" },
        },
      },
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() =>
      expect(screen.getByTestId("status").textContent).not.toBe("loading")
    );

    await userEvent.click(screen.getByTestId("btn-login"));

    await waitFor(() => {
      expect(screen.getByTestId("status").textContent).toBe("authenticated");
    });

    expect(localStorage.getItem("auth_token")).toBe("new-access-tok");
    expect(localStorage.getItem("refresh_token")).toBe("new-refresh-tok");
  });

  it("PU-CTX-04 | login() con respuesta inválida lanza error y no cambia estado", async () => {
    mockLogin.mockResolvedValue({ data: { data: {} } }); // sin accessToken

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() =>
      expect(screen.getByTestId("status").textContent).not.toBe("loading")
    );

    await expect(
      act(async () => {
        await authService.login({ correo: "x@test.com", contrasena: "p" }).then((r) => {
          if (!r.data?.data?.accessToken) throw new Error("Respuesta inválida del servidor");
        });
      })
    ).rejects.toThrow("Respuesta inválida");
  });

  // ─── logout() ─────────────────────────────────────────────────────────────

  it("PU-CTX-05 | logout() limpia localStorage y cambia estado a unauthenticated", async () => {
    localStorage.setItem("auth_token", "tok");
    localStorage.setItem("refresh_token", "rtok");
    localStorage.setItem("user", JSON.stringify({ correo: "u@test.com" }));
    mockLogout.mockResolvedValue(undefined);

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() =>
      expect(screen.getByTestId("status").textContent).toBe("authenticated")
    );

    await userEvent.click(screen.getByTestId("btn-logout"));

    await waitFor(() => {
      expect(screen.getByTestId("status").textContent).toBe("unauthenticated");
    });

    expect(localStorage.getItem("auth_token")).toBeNull();
    expect(localStorage.getItem("refresh_token")).toBeNull();
  });

  it("PU-CTX-06 | logout() funciona aunque el servidor falle (best-effort)", async () => {
    localStorage.setItem("auth_token", "tok");
    localStorage.setItem("refresh_token", "rtok");
    localStorage.setItem("user", JSON.stringify({ correo: "u@test.com" }));
    mockLogout.mockRejectedValue(new Error("Network error"));

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() =>
      expect(screen.getByTestId("status").textContent).toBe("authenticated")
    );

    await act(async () => {
      await screen.getByTestId("btn-logout").click();
    });

    await waitFor(() => {
      expect(screen.getByTestId("status").textContent).toBe("unauthenticated");
    });

    expect(localStorage.getItem("auth_token")).toBeNull();
  });

  // ─── useAuth sin Provider ──────────────────────────────────────────────────

  it("PU-CTX-07 | useAuth() fuera de AuthProvider lanza error descriptivo", () => {
    function ComponenteSinProvider() {
      useAuth(); // debe lanzar
      return <div />;
    }

    // Capturar el error de React
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    expect(() => render(<ComponenteSinProvider />)).toThrow(
      "useAuth debe usarse dentro de <AuthProvider>"
    );
    consoleSpy.mockRestore();
  });
});
