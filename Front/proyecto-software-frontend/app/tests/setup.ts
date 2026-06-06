import "@testing-library/jest-dom";

// Silenciar errores de consola esperados en tests
const originalError = console.error;
beforeAll(() => {
  console.error = (...args: unknown[]) => {
    if (
      typeof args[0] === "string" &&
      (args[0].includes("Warning: An update to") ||
        args[0].includes("Warning: ReactDOM.render"))
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

// Mock de import.meta.env para todos los tests
Object.defineProperty(import.meta, "env", {
  value: {
    VITE_API_URL: "http://localhost:8080",
    MODE: "test",
    DEV: false,
    PROD: false,
  },
  writable: true,
});
