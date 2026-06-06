import { defineConfig, devices } from "@playwright/test";

/**
 * Configuración de Playwright para pruebas End-to-End.
 *
 * Por defecto apunta al servidor de producción.
 * Definir la variable de entorno E2E_BASE_URL para cambiar el objetivo.
 *
 * Ejecutar: npm run test:e2e
 * Ver UI:   npm run test:e2e:ui
 */
export default defineConfig({
  testDir: "./app/tests/e2e",
  testMatch: "**/*.spec.ts",
  timeout: 30_000,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,

  reporter: [
    ["list"],
    ["html", { outputFolder: "playwright-report", open: "never" }],
  ],

  use: {
    baseURL: process.env.E2E_BASE_URL ?? "https://proyecto-ticket-26xq.onrender.com",
    headless: true,
    screenshot: "only-on-failure",
    video: "retain-on-failure",
    trace: "on-first-retry",
    actionTimeout: 10_000,
    navigationTimeout: 20_000,
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
