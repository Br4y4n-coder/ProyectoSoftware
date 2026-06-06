import { test, expect } from "@playwright/test";

/**
 * PRUEBA END-TO-END #1 — Flujo de Autenticación
 *
 * Tipo de caja: NEGRA — se interactúa con la aplicación como lo haría
 * un usuario real, sin conocimiento de la implementación interna.
 *
 * Escenarios cubiertos:
 *  E2E-AUTH-01 | Login con credenciales inválidas muestra mensaje de error
 *  E2E-AUTH-02 | Login con credenciales válidas redirige al dashboard
 *  E2E-AUTH-03 | Cerrar sesión redirige a la pantalla de login
 *
 * Herramienta: Playwright (Chromium)
 */

const VALID_USER = {
  correo: process.env.E2E_USER_EMAIL ?? "admin@test.com",
  contrasena: process.env.E2E_USER_PASSWORD ?? "TestPass123!",
};

test.describe("E2E-AUTH | Flujo de Autenticación (Caja Negra)", () => {

  test.beforeEach(async ({ page }) => {
    // Limpiar estado de sesión antes de cada test
    await page.goto("/auth/login");
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
  });

  // ─── E2E-AUTH-01 ──────────────────────────────────────────────────────────

  test("E2E-AUTH-01 | Login con credenciales incorrectas muestra mensaje de error", async ({ page }) => {
    await page.goto("/auth/login");

    // Completar el formulario con datos incorrectos
    await page.getByLabel(/correo|email/i).fill("noexiste@test.com");
    await page.getByLabel(/contraseña|password/i).fill("claveIncorrecta123");
    await page.getByRole("button", { name: /iniciar sesión|login|entrar/i }).click();

    // Verificar que aparece un mensaje de error
    await expect(
      page.getByText(/credencial|inválid|incorrecto|error/i)
    ).toBeVisible({ timeout: 10_000 });

    // La URL no debe cambiar al dashboard
    await expect(page).not.toHaveURL(/dashboard/);
  });

  // ─── E2E-AUTH-02 ──────────────────────────────────────────────────────────

  test("E2E-AUTH-02 | Login con credenciales válidas navega al dashboard", async ({ page }) => {
    await page.goto("/auth/login");

    await page.getByLabel(/correo|email/i).fill(VALID_USER.correo);
    await page.getByLabel(/contraseña|password/i).fill(VALID_USER.contrasena);
    await page.getByRole("button", { name: /iniciar sesión|login|entrar/i }).click();

    // Esperar navegación al dashboard o página principal
    await expect(page).toHaveURL(/dashboard|inicio|home/, { timeout: 15_000 });

    // El usuario debe ver contenido del sistema
    await expect(page.getByText(/tickets|bienvenido|dashboard/i)).toBeVisible();
  });

  // ─── E2E-AUTH-03 ──────────────────────────────────────────────────────────

  test("E2E-AUTH-03 | Cerrar sesión redirige a la página de login", async ({ page }) => {
    // Ir directo al login y autenticar
    await page.goto("/auth/login");
    await page.getByLabel(/correo|email/i).fill(VALID_USER.correo);
    await page.getByLabel(/contraseña|password/i).fill(VALID_USER.contrasena);
    await page.getByRole("button", { name: /iniciar sesión|login|entrar/i }).click();

    await expect(page).toHaveURL(/dashboard|inicio|home/, { timeout: 15_000 });

    // Hacer clic en cerrar sesión (puede estar en un menú de usuario)
    const logoutBtn = page.getByRole("button", { name: /cerrar sesión|logout|salir/i });
    if (await logoutBtn.isVisible()) {
      await logoutBtn.click();
    } else {
      // Intentar abrir menú de usuario primero
      await page.getByRole("button", { name: /perfil|usuario|account/i }).click();
      await page.getByText(/cerrar sesión|logout|salir/i).click();
    }

    // Debe redirigir al login
    await expect(page).toHaveURL(/login|auth/, { timeout: 10_000 });
  });

  // ─── E2E-AUTH-04 ──────────────────────────────────────────────────────────

  test("E2E-AUTH-04 | Acceder a ruta protegida sin sesión redirige a login", async ({ page }) => {
    // Intentar acceder directamente al dashboard sin estar autenticado
    await page.goto("/dashboard");

    // Debe redirigir al login
    await expect(page).toHaveURL(/login|auth/, { timeout: 10_000 });
  });
});
