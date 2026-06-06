import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { Badge } from "~/components/common/Badge";

/**
 * PRUEBAS UNITARIAS — Badge (Componente de UI)
 *
 * Tipo de caja: BLANCA — se conoce la implementación interna del componente
 * y se prueban todas las variantes y el comportamiento de props.
 *
 * Herramienta: Vitest + React Testing Library
 */
describe("Badge — Pruebas Unitarias (Caja Blanca)", () => {

  // ─── Renderizado básico ────────────────────────────────────────────────────

  it("PU-BADGE-01 | Renderiza el texto hijo correctamente", () => {
    render(<Badge>Abierto</Badge>);
    expect(screen.getByText("Abierto")).toBeInTheDocument();
  });

  it("PU-BADGE-02 | Aplica variante 'default' por defecto (bg-zinc)", () => {
    const { container } = render(<Badge>Default</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-zinc-100");
    expect(span?.className).toContain("text-zinc-700");
  });

  // ─── Variantes de color ────────────────────────────────────────────────────

  it("PU-BADGE-03 | Variante 'success' aplica clases verde esmeralda", () => {
    const { container } = render(<Badge variant="success">Activo</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-emerald-50");
    expect(span?.className).toContain("text-emerald-700");
  });

  it("PU-BADGE-04 | Variante 'warning' aplica clases ámbar", () => {
    const { container } = render(<Badge variant="warning">Pendiente</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-amber-50");
    expect(span?.className).toContain("text-amber-700");
  });

  it("PU-BADGE-05 | Variante 'danger' aplica clases rojas", () => {
    const { container } = render(<Badge variant="danger">Error</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-red-50");
    expect(span?.className).toContain("text-red-700");
  });

  it("PU-BADGE-06 | Variante 'info' aplica clases azules", () => {
    const { container } = render(<Badge variant="info">Info</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-blue-50");
    expect(span?.className).toContain("text-blue-700");
  });

  it("PU-BADGE-07 | Variante 'purple' aplica clases fucsia", () => {
    const { container } = render(<Badge variant="purple">Especial</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-fuchsia-50");
    expect(span?.className).toContain("text-fuchsia-700");
  });

  it("PU-BADGE-08 | Variante 'outline' aplica borde y fondo blanco", () => {
    const { container } = render(<Badge variant="outline">Contorno</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("bg-white");
    expect(span?.className).toContain("border");
    expect(span?.className).toContain("text-zinc-600");
  });

  // ─── Props adicionales ─────────────────────────────────────────────────────

  it("PU-BADGE-09 | Acepta y aplica className adicional", () => {
    const { container } = render(<Badge className="mi-clase-extra">Test</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("mi-clase-extra");
  });

  it("PU-BADGE-10 | Tiene estructura de elemento <span>", () => {
    const { container } = render(<Badge>Contenido</Badge>);
    expect(container.querySelector("span")).not.toBeNull();
  });

  it("PU-BADGE-11 | Renderiza contenido JSX como hijo", () => {
    render(
      <Badge>
        <span data-testid="hijo">Hijo JSX</span>
      </Badge>
    );
    expect(screen.getByTestId("hijo")).toBeInTheDocument();
  });

  it("PU-BADGE-12 | Contiene clases de estilo base (uppercase, tracking-wide)", () => {
    const { container } = render(<Badge>Base</Badge>);
    const span = container.querySelector("span");
    expect(span?.className).toContain("uppercase");
    expect(span?.className).toContain("tracking-wide");
    expect(span?.className).toContain("font-semibold");
  });
});
