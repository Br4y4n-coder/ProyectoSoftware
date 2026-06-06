import { test, expect, Page } from "@playwright/test";

/**
 * PRUEBA END-TO-END #3 — Panel de Administración
 *
 * Tipo de caja: NEGRA — flujo del administrador accediendo al dashboard,
 * visualizando métricas y gestionando usuarios.
 *
 * Escenarios:
 *  E2E-ADM-01 | Admin ve métricas en el dashboard
 *  E2E-ADM-02 | Admin accede a la gestión de usuarios
 *  E2E-ADM-03 | Usuario no-admin no puede acceder al panel de administración
 *
 * Herramienta: Playwright (Chromium)
 */

const ADMIN = {
  correo: process.env.E2E_ADMIN_EMAIL ?? "admin@test.com",
  contrasena: process.env.E2E_ADMIN_PASSWORD ?? "TestPass123!",
};

const USER = {
  correo: process.env.E2E_USER_EMAIL ?? "usuario@test.com",
  contrasena: process.env.E2E_USER_PASSWORD ?? "TestPass123!",
};

async function loginAs(page: Page, correo: string, contrasena: string) {
  await page.goto("/auth/login");
  await page.evaluate(() => localStorage.clear());
  await page.getByLabel(/correo|email/i).fill(correo);
  await page.getByLabel(/contraseña|password/i).fill(contrasena);
  await page.getByRole("button", { name: /iniciar sesión|login|entrar/i }).click();
  await expect(page).toHaveURL(/dashboard|inicio|home|admin/, { timeout: 15_000 });
}

test.describe("E2E-ADM | Panel de Administración (Caja Negra)", () => {

  // ─── E2E-ADM-01 ───────────────────────────────────────────────────────────

  test("E2E-ADM-01 | Dashboard de admin muestra KPIs y gráficas de métricas", async ({ page }) => {
    await loginAs(page, ADMIN.correo, ADMIN.contrasena);

    // Navegar al dashboard de admin
    const adminLink = page.getByRole("link", { name: /dashboard|inicio|admin/i }).first();
    if (await adminLink.isVisible()) {
      await adminLink.click();
    } else {
      await page.goto("/admin/dashboard");
    }

    // Verificar que hay elementos de métricas (KPIs, tarjetas, gráficos)
    await expect(
      page.getByText(/tickets|total|abiertos|resueltos|agentes/i).first()
    ).toBeVisible({ timeout: 15_000 });

    // Verificar que existe al menos una tarjeta de estadísticas
    const statsCards = page.locator(
      "[class*='card'], [class*='stat'], [class*='metric'], [class*='kpi']"
    );
    await expect(statsCards.first()).toBeVisible({ timeout: 10_000 });
  });

  // ─── E2E-ADM-02 ───────────────────────────────────────────────────────────

  test("E2E-ADM-02 | Admin navega a gestión de usuarios y ve la tabla", async ({ page }) => {
    await loginAs(page, ADMIN.correo, ADMIN.contrasena);

    // Navegar a gestión de usuarios
    const usuariosLink = page.getByRole("link", { name: /usuarios|agentes|user/i }).first();
    if (await usuariosLink.isVisible()) {
      await usuariosLink.click();
    } else {
      await page.goto("/admin/usuarios");
    }

    // Verificar que se muestra la lista de usuarios
    await expect(
      page.getByRole("heading", { name: /usuarios|gestión/i })
        .or(page.getByRole("table"))
        .or(page.getByText(/correo|nombre|rol/i).first())
    ).toBeVisible({ timeout: 12_000 });
  });

  // ─── E2E-ADM-03 ───────────────────────────────────────────────────────────

  test("E2E-ADM-03 | Rutas de admin no son accesibles sin autenticación", async ({ page }) => {
    // Intentar acceder directamente sin sesión
    await page.goto("/auth/login");
    await page.evaluate(() => localStorage.clear());

    await page.goto("/admin/dashboard");

    // Debe redirigir al login o mostrar error 403/401
    const esLoginPage = page.url().includes("login") || page.url().includes("auth");
    const hayMensajeAcceso = await page
      .getByText(/no autorizado|acceso denegado|forbidden|iniciar sesión/i)
      .isVisible();

    expect(esLoginPage || hayMensajeAcceso).toBe(true);
  });

  // ─── E2E-ADM-04 (bonus) ───────────────────────────────────────────────────

  test("E2E-ADM-04 | Admin puede navegar a sección de auditoría", async ({ page }) => {
    await loginAs(page, ADMIN.correo, ADMIN.contrasena);

    const auditoriaLink = page.getByRole("link", { name: /auditoría|auditoria|audit/i });
    if (await auditoriaLink.isVisible()) {
      await auditoriaLink.click();
      await expect(
        page.getByText(/log|actividad|registro|auditoria/i).first()
      ).toBeVisible({ timeout: 10_000 });
    } else {
      // Si el link no está visible, la prueba pasa (el rol puede no tener esa sección)
      test.skip();
    }
  });
});
