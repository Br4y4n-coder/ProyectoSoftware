import { useMemo, useState } from "react";
import { format } from "date-fns";
import { es } from "date-fns/locale";
import { Link } from "react-router";
import {
  Cell,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";
import { Download, RefreshCw, Star } from "lucide-react";
import { useDashboardData, formatMinutos } from "../../hooks/useDashboardData";

const CATEGORIA_COLORS = [
  "bg-primary",
  "bg-blue-500",
  "bg-violet-500",
  "bg-amber-400",
  "bg-teal-500",
  "bg-zinc-300",
];

const AVATAR_COLORS = [
  "bg-blue-500",
  "bg-emerald-500",
  "bg-violet-500",
  "bg-pink-500",
];

export default function AdminDashboard() {
  const { data, loading, error, recargar } = useDashboardData();
  const [rango, setRango] = useState<"dia" | "mes">("dia");
  const today = format(new Date(), "EEEE d MMM", { locale: es });

  const chartData = rango === "dia" ? data?.serieDiaria : data?.serieSemanal;
  const maxCategoria = useMemo(
    () => Math.max(1, ...(data?.categorias.map((c) => c.count) ?? [])),
    [data]
  );

  if (loading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-16 bg-zinc-100 rounded-xl" />
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          {[0, 1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-zinc-100 rounded-xl" />
          ))}
        </div>
        <div className="h-80 bg-zinc-100 rounded-xl" />
        <div className="h-72 bg-zinc-100 rounded-xl" />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="rounded-xl bg-red-50 border border-red-200 p-6 text-center">
        <p className="text-red-700">{error || "No se pudieron cargar los datos"}</p>
        <button
          onClick={recargar}
          className="mt-4 inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium"
        >
          <RefreshCw className="w-4 h-4" /> Reintentar
        </button>
      </div>
    );
  }

  const { kpis, categorias, agentes, estados, totalTickets, ticketsRiesgo } = data;

  const kpiCards = [
    {
      label: "TICKETS HOY",
      value: String(kpis.ticketsHoy),
      delta:
        kpis.deltaHoyPct === null
          ? undefined
          : `${kpis.deltaHoyPct >= 0 ? "▲" : "▼"} ${Math.abs(kpis.deltaHoyPct)}%`,
      deltaClass: (kpis.deltaHoyPct ?? 0) >= 0 ? "text-emerald-600" : "text-red-500",
      bar: {
        width: `${kpis.ticketsHoy > 0 ? Math.round((kpis.cerradosHoy / kpis.ticketsHoy) * 100) : 0}%`,
        color: "bg-primary",
      },
      subtext: `${kpis.abiertosHoy} abiertos · ${kpis.cerradosHoy} cerrados`,
    },
    {
      label: "SLA CUMPLIDO",
      value: kpis.slaCumplidoPct === null ? "—" : `${kpis.slaCumplidoPct}%`,
      bar: {
        width: `${kpis.slaCumplidoPct ?? 0}%`,
        color:
          (kpis.slaCumplidoPct ?? 0) >= 95 ? "bg-emerald-500" : "bg-amber-400",
      },
      subtext:
        kpis.slaCumplidoPct === null
          ? "Sin tickets cerrados con SLA"
          : `Meta: 95% · ${kpis.slaCumplidoPct >= 95 ? "✓ cumplida" : "✗ no cumplida"}`,
    },
    {
      label: "TIEMPO MEDIO",
      value: formatMinutos(kpis.tiempoMedioMin),
      bar: {
        width: `${Math.min(100, Math.round(((kpis.tiempoMedioMin ?? 0) / 240) * 100))}%`,
        color: "bg-amber-400",
      },
      subtext: `Últimos 30 días · ${kpis.totalResueltos30d} resueltos`,
    },
    {
      label: "SATISFACCIÓN",
      value: kpis.satisfaccion === null ? "—" : String(kpis.satisfaccion),
      star: kpis.satisfaccion !== null,
      bar: {
        width: `${((kpis.satisfaccion ?? 0) / 5) * 100}%`,
        color: "bg-amber-400",
      },
      subtext:
        kpis.totalValoraciones > 0
          ? `Basado en ${kpis.totalValoraciones} valoraciones`
          : "Sin valoraciones aún",
    },
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-zinc-900">
            Dashboard de administración
          </h1>
          <p className="mt-1 text-sm text-zinc-500">
            Visión general del sistema · <span className="capitalize">{today}</span>
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={recargar}
            className="inline-flex items-center gap-2 h-9 px-4 text-sm font-medium rounded-lg bg-white border border-zinc-200 text-zinc-700 hover:bg-zinc-50 transition"
          >
            <RefreshCw className="w-4 h-4 text-zinc-400" /> Actualizar
          </button>
          <Link
            to="/admin/exportar"
            className="inline-flex items-center gap-2 h-9 px-4 text-sm font-semibold rounded-lg bg-primary text-white hover:opacity-90 transition"
          >
            <Download className="w-4 h-4" /> Exportar
          </Link>
        </div>
      </header>

      {/* KPIs */}
      <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {kpiCards.map((kpi) => (
          <div
            key={kpi.label}
            className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm hover:shadow-md transition-shadow"
          >
            <p className="text-[10px] font-semibold tracking-widest text-zinc-400">
              {kpi.label}
            </p>
            <div className="mt-2 flex items-baseline gap-2">
              <span className="text-3xl font-bold text-zinc-900">{kpi.value}</span>
              {kpi.star && (
                <Star className="w-5 h-5 text-amber-400 fill-amber-400 self-center" />
              )}
              {kpi.delta && (
                <span className={`text-sm font-semibold ${kpi.deltaClass}`}>
                  {kpi.delta}
                </span>
              )}
            </div>
            <div className="mt-3 h-1.5 rounded-full bg-zinc-100 overflow-hidden">
              <div
                className={`h-full rounded-full ${kpi.bar.color}`}
                style={{ width: kpi.bar.width }}
              />
            </div>
            <p className="mt-2 text-xs text-zinc-500">{kpi.subtext}</p>
          </div>
        ))}
      </section>

      {/* Gráfica + Top categorías */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="xl:col-span-2 rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 className="text-sm font-bold text-zinc-900">
                Tickets por día — últimas 4 semanas
              </h2>
              <p className="text-xs text-zinc-400 mt-0.5">
                Comparativa nuevos vs. resueltos
              </p>
            </div>
            <div className="flex items-center rounded-lg bg-zinc-100 p-0.5 text-xs font-semibold">
              {(["dia", "mes"] as const).map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setRango(r)}
                  className={`px-3 py-1.5 rounded-md transition ${
                    rango === r
                      ? "bg-white text-zinc-900 shadow-sm"
                      : "text-zinc-500 hover:text-zinc-700"
                  }`}
                >
                  {r === "dia" ? "Día" : "Semana"}
                </button>
              ))}
            </div>
          </div>

          <div className="h-64 mt-4">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={chartData}
                margin={{ top: 5, right: 10, left: -20, bottom: 0 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#f4f4f5" vertical={false} />
                <XAxis
                  dataKey="label"
                  tick={{ fontSize: 11, fill: "#a1a1aa" }}
                  axisLine={false}
                  tickLine={false}
                  interval={0}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#a1a1aa" }}
                  axisLine={false}
                  tickLine={false}
                  allowDecimals={false}
                />
                <Tooltip />
                <Line
                  type="monotone"
                  dataKey="nuevos"
                  name="Tickets nuevos"
                  stroke="#4F46E5"
                  strokeWidth={2}
                  dot={false}
                />
                <Line
                  type="monotone"
                  dataKey="resueltos"
                  name="Tickets resueltos"
                  stroke="#10B981"
                  strokeWidth={2}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="flex items-center gap-6 mt-3 text-xs text-zinc-500">
            <span className="inline-flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-primary" /> Tickets nuevos
            </span>
            <span className="inline-flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" /> Tickets resueltos
            </span>
          </div>
        </div>

        {/* Top categorías */}
        <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm flex flex-col">
          <h2 className="text-sm font-bold text-zinc-900">Top categorías</h2>
          <p className="text-xs text-zinc-400 mt-0.5">Por volumen de tickets</p>

          {categorias.length === 0 ? (
            <p className="mt-6 text-sm text-zinc-400 text-center">Sin datos aún</p>
          ) : (
            <ul className="mt-4 space-y-4 flex-1">
              {categorias.map((cat, i) => (
                <li key={cat.name}>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-zinc-700">{cat.name}</span>
                    <span className="font-bold text-zinc-900">{cat.count}</span>
                  </div>
                  <div className="mt-1.5 h-1.5 rounded-full bg-zinc-100 overflow-hidden">
                    <div
                      className={`h-full rounded-full ${CATEGORIA_COLORS[i % CATEGORIA_COLORS.length]}`}
                      style={{ width: `${(cat.count / maxCategoria) * 100}%` }}
                    />
                  </div>
                </li>
              ))}
            </ul>
          )}

          <Link
            to="/admin/metricas"
            className="mt-4 text-sm text-primary font-medium hover:underline inline-flex items-center gap-1"
          >
            Ver reporte completo →
          </Link>
        </div>
      </div>

      {/* Agentes + Estado + Tickets en riesgo */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Rendimiento de agentes */}
        <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-bold text-zinc-900">Rendimiento de agentes</h2>
          {agentes.length === 0 ? (
            <p className="mt-6 text-sm text-zinc-400 text-center">
              Sin tickets resueltos aún
            </p>
          ) : (
            <table className="w-full mt-4 text-sm">
              <thead>
                <tr className="text-[10px] font-semibold tracking-widest text-zinc-400 text-left">
                  <th className="pb-2 font-semibold">AGENTE</th>
                  <th className="pb-2 font-semibold text-right">RESUELTOS</th>
                  <th className="pb-2 font-semibold text-right">PROMEDIO</th>
                  <th className="pb-2 font-semibold text-right">SLA</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-50">
                {agentes.map((a, i) => (
                  <tr key={a.name}>
                    <td className="py-3">
                      <div className="flex items-center gap-3">
                        <span
                          className={`w-8 h-8 rounded-full ${AVATAR_COLORS[i % AVATAR_COLORS.length]} text-white text-[11px] font-bold flex items-center justify-center shrink-0`}
                        >
                          {a.avatar}
                        </span>
                        <p className="font-semibold text-zinc-900 truncate">{a.name}</p>
                      </div>
                    </td>
                    <td className="py-3 text-right font-bold text-zinc-900">
                      {a.resolved}
                    </td>
                    <td className="py-3 text-right text-zinc-600">
                      {formatMinutos(a.averageMin)}
                    </td>
                    <td className="py-3 text-right">
                      {a.sla === null ? (
                        <span className="text-zinc-400 text-[11px]">—</span>
                      ) : (
                        <span
                          className={`inline-block px-2 py-0.5 rounded-md text-[11px] font-bold ${
                            a.sla >= 95
                              ? "bg-emerald-100 text-emerald-700"
                              : "bg-amber-100 text-amber-700"
                          }`}
                        >
                          {a.sla}%
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Estado actual */}
        <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-bold text-zinc-900">Estado actual</h2>
          {estados.length === 0 ? (
            <p className="mt-6 text-sm text-zinc-400 text-center">Sin tickets aún</p>
          ) : (
            <>
              <div className="relative h-48 mt-2">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={estados}
                      dataKey="cantidad"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={58}
                      outerRadius={78}
                      paddingAngle={3}
                      strokeWidth={0}
                    >
                      {estados.map((entry) => (
                        <Cell key={entry.name} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(v, name) => [`${v} tickets`, name]}
                    />
                  </PieChart>
                </ResponsiveContainer>
                <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                  <span className="text-2xl font-bold text-zinc-900">{totalTickets}</span>
                  <span className="text-[11px] text-zinc-400">Total tickets</span>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-x-4 gap-y-2 mt-4 text-xs text-zinc-600">
                {estados.map((e) => (
                  <span key={e.name} className="inline-flex items-center gap-2">
                    <span
                      className="w-2.5 h-2.5 rounded-full shrink-0"
                      style={{ backgroundColor: e.color }}
                    />
                    {e.name} {e.value}%
                  </span>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Tickets en riesgo */}
        <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-bold text-zinc-900">Tickets en riesgo</h2>
          <p className="text-xs text-zinc-400 mt-0.5">SLA por vencer</p>

          {ticketsRiesgo.length === 0 ? (
            <p className="mt-6 text-sm text-zinc-400 text-center">
              No hay tickets en riesgo 🎉
            </p>
          ) : (
            <ul className="mt-4 space-y-3">
              {ticketsRiesgo.map((t) => {
                const vencido = t.minutosRestantes < 0;
                const urgente = t.minutosRestantes < 60;
                return (
                  <li
                    key={t.id}
                    className={`rounded-lg border p-3 ${
                      urgente ? "bg-red-50 border-red-100" : "bg-amber-50 border-amber-100"
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <Link
                        to={`/admin/assign-ticket/${t.id}`}
                        className={`text-sm font-bold hover:underline ${
                          urgente ? "text-red-600" : "text-amber-600"
                        }`}
                      >
                        #{t.codigo}
                      </Link>
                      <span
                        className={`text-xs font-bold ${
                          urgente ? "text-red-500" : "text-amber-600"
                        }`}
                      >
                        {vencido
                          ? `Vencido hace ${formatMinutos(t.minutosRestantes)}`
                          : formatMinutos(t.minutosRestantes)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between gap-2 mt-1 text-xs text-zinc-500">
                      <span className="truncate">{t.asunto}</span>
                      <span className="shrink-0">{t.assigned}</span>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
