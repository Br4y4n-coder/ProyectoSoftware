import { describe, it, expect } from "vitest";
import { formatMinutos } from "../../hooks/useDashboardData";

describe("formatMinutos (dashboard)", () => {
  it("devuelve em dash cuando no hay dato", () => {
    expect(formatMinutos(null)).toBe("—");
  });

  it("formatea minutos menores a una hora", () => {
    expect(formatMinutos(0)).toBe("0 min");
    expect(formatMinutos(45)).toBe("45 min");
    expect(formatMinutos(59)).toBe("59 min");
  });

  it("formatea horas y minutos", () => {
    expect(formatMinutos(60)).toBe("1h 0m");
    expect(formatMinutos(134)).toBe("2h 14m");
    expect(formatMinutos(241)).toBe("4h 1m");
  });

  it("usa valor absoluto para tiempos vencidos (negativos)", () => {
    expect(formatMinutos(-170)).toBe("2h 50m");
    expect(formatMinutos(-35)).toBe("35 min");
  });
});
