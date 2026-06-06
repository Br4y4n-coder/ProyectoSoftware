import { defineConfig } from "vitest/config";
import { resolve } from "path";

/**
 * Configuración de Vitest para pruebas unitarias y de componentes.
 * Se usa esbuild con JSX automático (compatible con Vite 8 + React 19)
 * sin necesidad de @vitejs/plugin-react que aún no soporta Vite 8.
 */
export default defineConfig({
  esbuild: {
    jsx: "automatic",
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./app/tests/setup.ts"],
    include: ["app/tests/unit/**/*.{test,spec}.{ts,tsx,js,jsx}"],
    exclude: ["app/tests/e2e/**", "node_modules/**"],
    coverage: {
      provider: "v8",
      reporter: ["text", "json", "html", "lcov"],
      reportsDirectory: "./coverage",
      include: ["app/**/*.{ts,tsx,js,jsx}"],
      exclude: [
        "app/tests/**",
        "app/routes.ts",
        "app/root.tsx",
        "app/types/**",
        "app/data/**",
        "node_modules/**",
      ],
      thresholds: {
        lines: 85,
        functions: 85,
        branches: 80,
        statements: 85,
      },
    },
  },
  resolve: {
    alias: {
      "~": resolve(__dirname, "./app"),
    },
  },
});
