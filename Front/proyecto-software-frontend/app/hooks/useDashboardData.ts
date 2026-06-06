import { useCallback, useEffect, useState } from "react";
import apiFetch from "../api/apiFetch";

/* ---------- Tipos de la API ---------- */

interface TicketApi {
  id: number;
  codigo: string;
  asunto: string;
  estado: string;
  prioridad: string;
  categoriaNombre: string | null;
  agenteNombre: string | null;
  fechaCreacion: string | null;
  fechaCierre: string | null;
  fechaVencimientoSla: string | null;
  tiempoResolucionMinutos: number | null;
  valoracion?: number | null;
}

interface EstadoApi {
  estado: string;
  cantidad: number;
}

interface AgenteApi {
  agenteNombre: string;
  cantidad: number;
}

/* ---------- Tipos calculados para el dashboard ---------- */

export interface KpiData {
  ticketsHoy: number;
  deltaHoyPct: number | null;
  abiertosHoy: number;
  cerradosHoy: number;
  slaCumplidoPct: number | null;
  tiempoMedioMin: number | null;
  totalResueltos30d: number;
  satisfaccion: number | null;
  totalValoraciones: number;
}

export interface SeriePunto {
  label: string;
  nuevos: number;
  resueltos: number;
}

export interface CategoriaStat {
  name: string;
  count: number;
}

export interface AgenteStat {
  name: string;
  avatar: string;
  resolved: number;
  averageMin: number | null;
  sla: number | null;
}

export interface EstadoStat {
  name: string;
  value: number; // porcentaje
  cantidad: number;
  color: string;
}

export interface TicketRiesgo {
  id: number;
  codigo: string;
  asunto: string;
  assigned: string;
  minutosRestantes: number;
}

export interface DashboardData {
  kpis: KpiData;
  serieDiaria: SeriePunto[];
  serieSemanal: SeriePunto[];
  categorias: CategoriaStat[];
  agentes: AgenteStat[];
  estados: EstadoStat[];
  totalTickets: number;
  ticketsRiesgo: TicketRiesgo[];
}

/* ---------- Helpers ---------- */

const CERRADOS = new Set(["cerrado", "resuelto"]);

const ESTADO_META: Record<string, { label: string; color: string }> = {
  abierto: { label: "Abierto", color: "#3B82F6" },
  en_proceso: { label: "Proceso", color: "#F59E0B" },
  en_espera: { label: "En espera", color: "#8B5CF6" },
  resuelto: { label: "Resuelto", color: "#14B8A6" },
  cerrado: { label: "Cerrado", color: "#10B981" },
  vencido: { label: "Vencido", color: "#EF4444" },
};

function sameDay(a: Date, b: Date) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

function parseFecha(s: string | null): Date | null {
  if (!s) return null;
  const d = new Date(s);
  return isNaN(d.getTime()) ? null : d;
}

function iniciales(nombre: string) {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]!.toUpperCase())
    .join("");
}

/* ---------- Agregaciones ---------- */

function calcular(
  tickets: TicketApi[],
  estadosApi: EstadoApi[],
  tiempoApi: { promedioHoras: number | null; totalTickets: number } | null,
  agentesApi: AgenteApi[]
): DashboardData {
  const ahora = new Date();
  const ayer = new Date(ahora);
  ayer.setDate(ayer.getDate() - 1);

  /* KPIs de hoy */
  const deHoy = tickets.filter((t) => {
    const f = parseFecha(t.fechaCreacion);
    return f !== null && sameDay(f, ahora);
  });
  const deAyer = tickets.filter((t) => {
    const f = parseFecha(t.fechaCreacion);
    return f !== null && sameDay(f, ayer);
  });
  const cerradosHoy = deHoy.filter((t) => CERRADOS.has(t.estado)).length;

  /* SLA cumplido: cerrados con cierre antes del vencimiento */
  const cerradosConSla = tickets.filter((t) => {
    return CERRADOS.has(t.estado) && t.fechaCierre && t.fechaVencimientoSla;
  });
  const aTiempo = cerradosConSla.filter((t) => {
    const cierre = parseFecha(t.fechaCierre)!;
    const limite = parseFecha(t.fechaVencimientoSla)!;
    return cierre.getTime() <= limite.getTime();
  }).length;

  /* Satisfacción (solo si el backend la expone) */
  const conValoracion = tickets.filter(
    (t) => typeof t.valoracion === "number" && t.valoracion > 0
  );
  const satisfaccion =
    conValoracion.length > 0
      ? conValoracion.reduce((s, t) => s + (t.valoracion as number), 0) /
        conValoracion.length
      : null;

  /* Serie diaria: últimos 28 días */
  const serieDiaria: SeriePunto[] = [];
  for (let i = 27; i >= 0; i--) {
    const dia = new Date(ahora);
    dia.setDate(dia.getDate() - i);
    const nuevos = tickets.filter((t) => {
      const f = parseFecha(t.fechaCreacion);
      return f !== null && sameDay(f, dia);
    }).length;
    const resueltos = tickets.filter((t) => {
      const f = parseFecha(t.fechaCierre);
      return f !== null && sameDay(f, dia);
    }).length;
    const idx = 27 - i;
    serieDiaria.push({
      label: idx % 7 === 0 ? `S${idx / 7 + 1}` : i === 0 ? "Hoy" : "",
      nuevos,
      resueltos,
    });
  }

  /* Serie semanal: 4 buckets de 7 días */
  const serieSemanal: SeriePunto[] = [0, 1, 2, 3].map((w) => {
    const bucket = serieDiaria.slice(w * 7, w * 7 + 7);
    return {
      label: w === 3 ? "Esta semana" : `Sem ${w + 1}`,
      nuevos: bucket.reduce((s, p) => s + p.nuevos, 0),
      resueltos: bucket.reduce((s, p) => s + p.resueltos, 0),
    };
  });

  /* Top categorías */
  const porCategoria = new Map<string, number>();
  tickets.forEach((t) => {
    const nombre = t.categoriaNombre || "Sin categoría";
    porCategoria.set(nombre, (porCategoria.get(nombre) || 0) + 1);
  });
  const categorias = [...porCategoria.entries()]
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 6);

  /* Rendimiento de agentes: resueltos (API) + promedio y SLA (tickets) */
  const statsPorAgente = new Map<
    string,
    { totalMin: number; conTiempo: number; cerrados: number; aTiempo: number }
  >();
  tickets.forEach((t) => {
    if (!t.agenteNombre || !CERRADOS.has(t.estado)) return;
    const s =
      statsPorAgente.get(t.agenteNombre) ||
      { totalMin: 0, conTiempo: 0, cerrados: 0, aTiempo: 0 };
    if (typeof t.tiempoResolucionMinutos === "number") {
      s.totalMin += t.tiempoResolucionMinutos;
      s.conTiempo += 1;
    }
    if (t.fechaCierre && t.fechaVencimientoSla) {
      s.cerrados += 1;
      const cierre = parseFecha(t.fechaCierre)!;
      const limite = parseFecha(t.fechaVencimientoSla)!;
      if (cierre.getTime() <= limite.getTime()) s.aTiempo += 1;
    }
    statsPorAgente.set(t.agenteNombre, s);
  });

  const agentes: AgenteStat[] = agentesApi
    .slice()
    .sort((a, b) => b.cantidad - a.cantidad)
    .slice(0, 4)
    .map((a) => {
      const s = statsPorAgente.get(a.agenteNombre);
      return {
        name: a.agenteNombre,
        avatar: iniciales(a.agenteNombre),
        resolved: a.cantidad,
        averageMin: s && s.conTiempo > 0 ? s.totalMin / s.conTiempo : null,
        sla: s && s.cerrados > 0 ? Math.round((s.aTiempo / s.cerrados) * 100) : null,
      };
    });

  /* Estado actual (donut) */
  const totalEstados = estadosApi.reduce((s, e) => s + e.cantidad, 0);
  const estados: EstadoStat[] = estadosApi
    .filter((e) => e.cantidad > 0)
    .map((e) => {
      const meta = ESTADO_META[e.estado] || { label: e.estado, color: "#A1A1AA" };
      return {
        name: meta.label,
        color: meta.color,
        cantidad: e.cantidad,
        value: totalEstados > 0 ? Math.round((e.cantidad / totalEstados) * 100) : 0,
      };
    });

  /* Tickets en riesgo: abiertos con SLA más próximo a vencer */
  const ticketsRiesgo: TicketRiesgo[] = tickets
    .filter((t) => !CERRADOS.has(t.estado) && t.fechaVencimientoSla)
    .map((t) => ({
      id: t.id,
      codigo: t.codigo,
      asunto: t.asunto,
      assigned: t.agenteNombre || "Sin asignar",
      minutosRestantes: Math.round(
        (parseFecha(t.fechaVencimientoSla)!.getTime() - ahora.getTime()) / 60000
      ),
    }))
    .sort((a, b) => a.minutosRestantes - b.minutosRestantes)
    .slice(0, 3);

  return {
    kpis: {
      ticketsHoy: deHoy.length,
      deltaHoyPct:
        deAyer.length > 0
          ? Math.round(((deHoy.length - deAyer.length) / deAyer.length) * 100)
          : null,
      abiertosHoy: deHoy.length - cerradosHoy,
      cerradosHoy,
      slaCumplidoPct:
        cerradosConSla.length > 0
          ? Math.round((aTiempo / cerradosConSla.length) * 1000) / 10
          : null,
      tiempoMedioMin:
        tiempoApi?.promedioHoras != null
          ? Math.round(tiempoApi.promedioHoras * 60)
          : null,
      totalResueltos30d: tiempoApi?.totalTickets ?? 0,
      satisfaccion: satisfaccion !== null ? Math.round(satisfaccion * 10) / 10 : null,
      totalValoraciones: conValoracion.length,
    },
    serieDiaria,
    serieSemanal,
    categorias,
    agentes,
    estados,
    totalTickets: totalEstados,
    ticketsRiesgo,
  };
}

/* ---------- Hook ---------- */

export function useDashboardData() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const cargar = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [ticketsRes, estadoRes, tiempoRes, agentesRes] = await Promise.all([
        apiFetch("/api/tickets?page=0&size=500&sort=fechaCreacion,desc"),
        apiFetch("/api/metrics/tickets-por-estado"),
        apiFetch("/api/metrics/tiempo-promedio-resolucion"),
        apiFetch("/api/metrics/tickets-resueltos-por-agente"),
      ]);

      if (!ticketsRes.ok) {
        throw new Error(`No se pudieron cargar los tickets (${ticketsRes.status})`);
      }

      const ticketsJson = await ticketsRes.json();
      const tickets: TicketApi[] = ticketsJson?.data?.content ?? [];

      const estadosApi: EstadoApi[] = estadoRes.ok
        ? (await estadoRes.json())?.data ?? []
        : [];
      const tiempoApi = tiempoRes.ok ? (await tiempoRes.json())?.data ?? null : null;
      const agentesApi: AgenteApi[] = agentesRes.ok
        ? (await agentesRes.json())?.data ?? []
        : [];

      setData(calcular(tickets, estadosApi, tiempoApi, agentesApi));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Error de conexión");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  return { data, loading, error, recargar: cargar };
}

/* ---------- Formateadores compartidos ---------- */

export function formatMinutos(min: number | null): string {
  if (min === null) return "—";
  const abs = Math.abs(min);
  const h = Math.floor(abs / 60);
  const m = Math.round(abs % 60);
  return h > 0 ? `${h}h ${m}m` : `${m} min`;
}
