import { describe, it, expect } from "vitest";
import {
  API_BASE_URL,
  API_TIMEOUT_MS,
  API_PREFIX,
  AUTH_TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_KEY,
} from "~/services/config";

/**
 * PRUEBAS UNITARIAS — config.js
 *
 * Tipo de caja: BLANCA — valida que las constantes de configuración
 * tienen los valores esperados que el resto del sistema asume.
 *
 * Herramienta: Vitest (sin DOM, solo módulo puro)
 */
describe("Config — Constantes de configuración (Caja Blanca)", () => {

  it("PU-CFG-01 | API_BASE_URL usa la variable de entorno o localhost:8080", () => {
    expect(API_BASE_URL).toBeDefined();
    expect(typeof API_BASE_URL).toBe("string");
    expect(API_BASE_URL.startsWith("http")).toBe(true);
  });

  it("PU-CFG-02 | API_TIMEOUT_MS es 10000 ms (10 segundos)", () => {
    expect(API_TIMEOUT_MS).toBe(10000);
  });

  it("PU-CFG-03 | API_PREFIX es '/api'", () => {
    expect(API_PREFIX).toBe("/api");
  });

  it("PU-CFG-04 | AUTH_TOKEN_KEY es 'auth_token'", () => {
    expect(AUTH_TOKEN_KEY).toBe("auth_token");
  });

  it("PU-CFG-05 | REFRESH_TOKEN_KEY es 'refresh_token'", () => {
    expect(REFRESH_TOKEN_KEY).toBe("refresh_token");
  });

  it("PU-CFG-06 | USER_KEY es 'user'", () => {
    expect(USER_KEY).toBe("user");
  });

  it("PU-CFG-07 | Todas las claves de localStorage son diferentes entre sí", () => {
    const keys = new Set([AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY]);
    expect(keys.size).toBe(3);
  });
});
