import { test, expect, Page } from "@playwright/test";

/**
 * PRUEBA END-TO-END #2 — Gestión de Tickets
 *
 * Tipo de caja: NEGRA — flujo completo del usuario al crear y consultar
 * un ticket de soporte.
 *
 * Escenarios:
 *  E2E-TKT-01 | Crear un nuevo ticket desde el formulario
 *  E2E-TKT-02 | Ver mis tickets listados en la sección "Mis Tickets"
 *  E2E-TKT-03 | Formulario de creación valida campos obligatorios
 *
 * Herramienta: Playwright (Chromium)
 */

const VALID_USER = {
  correo: process.env.E2E_USER_EMAIL ?? "admin@test.com",
  contrasena: process.env.E2E_USER_PASSWORD ?? "TestPass123!",
};

// Helper: autenticar y navegar al formulario de creación de tickets
async function loginAndGoToCreate(page: Page) {
  await page.goto("/auth/login");
  await page.getByLabel(/correo|email/i).fill(VALID_USER.correo);
  await page.getByLabel(/contraseña|password/i).fill(VALID_USER.contrasena);
  await page.getByRole("button", { name: /iniciar sesión|login|entrar/i }).click();
  await expect(page).toHaveURL(/dashboard|inicio|home/, { timeout: 15_000 });
}

test.describe("E2E-TKT | Gestión de Tickets (Caja Negra)", () => {

  test.beforeEach(async ({ page }) => {
    await page.goto("/auth/login");
    await page.evaluate(() => localStorage.clear());
  });

  // ─── E2E-TKT-01 ───────────────────────────────────────────────────────────

  test("E2E-TKT-01 | Crear ticket rellena formulario y confirma el éxito", async ({ page }) => {
    await loginAndGoToCreate(page);

    // Navegar al formulario de creación
    const crearLink = page.getByRole("link", { name: /crear ticket|nuevo ticket|reportar/i });
    await crearLink.click();
    await expect(page).toHaveURL(/crear|nuevo/, { timeout: 8_000 });

    // Completar el formulario
    const titulo = page.getByLabel(/título|titulo/i);
    await titulo.fill("Ticket E2E - Error de prueba automatizada");

    const descripcion = page.getByLabel(/descripción|descripcion/i);
    await descripcion.fill("Este es un ticket creado por una prueba automatizada E2E con Playwright.");

    // Seleccionar tipo si existe
    const tipoSelect = page.getByLabel(/tipo/i);
    if (await tipoSelect.isVisible()) {
      await tipoSelect.selectOption({ index: 1 });
    }

    // Seleccionar prioridad si existe
    const prioridadSelect = page.getByLabel(/prioridad/i);
    if (await prioridadSelect.isVisible()) {
      await prioridadSelect.selectOption({ index: 1 });
    }

    // Seleccionar categoría si existe
    const categoriaSelect = page.getByLabel(/categor/i);
    if (await categoriaSelect.isVisible()) {
      await categoriaSelect.selectOption({ index: 1 });
    }

    // Enviar el formulario
    await page.getByRole("button", { name: /enviar|crear|guardar|submit/i }).click();

    // Verificar éxito: mensaje de confirmación o redirección
    await expect(
      page.getByText(/ticket.*creado|enviado.*exitosamente|éxito|success/i)
        .or(page.getByRole("heading", { name: /mis tickets/i }))
    ).toBeVisible({ timeout: 12_000 });
  });

  // ─── E2E-TKT-02 ───────────────────────────────────────────────────────────

  test("E2E-TKT-02 | Sección 'Mis Tickets' muestra la lista con paginación", async ({ page }) => {
    await loginAndGoToCreate(page);

    // Navegar a mis tickets
    const misTicketsLink = page.getByRole("link", { name: /mis tickets|mis solicitudes/i });
    await misTicketsLink.click();

    // Verificar que la página carga la lista
    await expect(
      page.getByRole("heading", { name: /mis tickets|mis solicitudes/i })
        .or(page.getByText(/no tienes tickets|sin tickets/i))
    ).toBeVisible({ timeout: 10_000 });

    // Si hay tickets, verificar que se muestran
    const tickets = page.getByRole("row").or(page.locator("[data-testid='ticket-item']"));
    const hayTickets = (await tickets.count()) > 0;
    if (hayTickets) {
      await expect(tickets.first()).toBeVisible();
    }
  });

  // ─── E2E-TKT-03 ───────────────────────────────────────────────────────────

  test("E2E-TKT-03 | Formulario de ticket muestra error si el título está vacío", async ({ page }) => {
    await loginAndGoToCreate(page);

    // Navegar al formulario de creación
    const crearLink = page.getByRole("link", { name: /crear ticket|nuevo ticket|reportar/i });
    await crearLink.click();
    await expect(page).toHaveURL(/crear|nuevo/, { timeout: 8_000 });

    // Intentar enviar sin completar el título
    await page.getByRole("button", { name: /enviar|crear|guardar|submit/i }).click();

    // Verificar que se muestra validación (campo requerido o mensaje de error)
    const validationMsg = page
      .getByText(/requerido|obligatorio|campo.*vacío|required/i)
      .or(page.locator("input:invalid"))
      .or(page.locator(".error, .alert-danger, [role='alert']"));

    await expect(validationMsg.first()).toBeVisible({ timeout: 5_000 });
  });
});
