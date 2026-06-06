import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router";
import { ticketsService } from "../services";

export default function HomeUsuario() {
  const [tickets, setTickets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await ticketsService.mios({
          page: 0,
          size: 50,
          sort: "fechaCreacion,desc",
        });
        if (cancelled) return;
        setTickets(data?.data?.content || []);
      } catch (err) {
        if (cancelled) return;
        setErrorMsg(
          err?.response?.data?.message ||
            err?.message ||
            "No fue posible cargar tus tickets"
        );
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const stats = useMemo(() => buildStats(tickets), [tickets]);
  const recientes = useMemo(() => tickets.slice(0, 6), [tickets]);
  const slaItems = useMemo(() => buildSlaItems(tickets), [tickets]);

  return (
    <div className="space-y-8">
      {errorMsg && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {errorMsg}
        </div>
      )}

      {/* Stats */}
      <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-5">
        {STATS_DEF.map((def) => (
          <StatCard
            key={def.key}
            {...def}
            value={isLoading ? null : stats[def.key]}
            footer={def.footerFor(stats)}
          />
        ))}
      </section>

      {/* Layout principal: izquierda 2/3, derecha 1/3 */}
      <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)] gap-6">
        {/* Columna izquierda */}
        <div className="space-y-6">
          {/* Banner crear ticket */}
          <div className="relative overflow-hidden rounded-xl bg-primary text-white p-6 flex items-center gap-6">
            <div className="absolute top-0 right-0 w-60 h-60 rounded-full bg-circle opacity-40 -translate-y-12 translate-x-12 pointer-events-none" />
            <div className="absolute bottom-0 right-32 w-32 h-32 rounded-full bg-primary-light opacity-40 translate-y-10 pointer-events-none" />
            <div className="relative flex-1 min-w-0">
              <h2 className="text-xl font-bold">¿Tienes un nuevo problema?</h2>
              <p className="mt-1 text-sm text-primary-faint">
                Crea un ticket y nuestro equipo lo atenderá lo antes posible.
              </p>
            </div>
            <Link
              to="/tickets/nuevo"
              className="relative shrink-0 h-11 px-5 rounded-lg bg-white text-primary text-sm font-semibold hover:bg-primary-faint transition flex items-center"
            >
              + Crear ticket
            </Link>
          </div>

          {/* Tickets recientes */}
          <section>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-bold text-zinc-900">
                Mis tickets recientes
              </h3>
              <button
                type="button"
                onClick={() => window.location.href = "/mis-tickets"}
                className="text-sm font-medium text-primary hover:text-primary-light"
              >
                Ver todos →
              </button>
            </div>

            <div className="rounded-xl bg-white border border-zinc-200 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-[#F9FAFB]">
                    <tr className="text-left text-[11px] font-semibold tracking-wider text-zinc-500 uppercase">
                      <th className="px-6 py-3">ID</th>
                      <th className="px-6 py-3">Asunto</th>
                      <th className="px-6 py-3">Estado</th>
                      <th className="px-6 py-3">Fecha</th>
                      <th className="px-6 py-3">Prioridad</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-zinc-100">
                    {isLoading ? (
                      Array.from({ length: 4 }).map((_, i) => (
                        <SkeletonRow key={i} />
                      ))
                    ) : recientes.length === 0 ? (
                      <tr>
                        <td
                          colSpan={5}
                          className="px-6 py-12 text-center text-sm text-zinc-500"
                        >
                          Aún no tienes tickets. Crea el primero con el botón
                          de arriba.
                        </td>
                      </tr>
                    ) : (
                      recientes.map((t) => (
                        <tr key={t.id} className="hover:bg-zinc-50/50">
                          <td className="px-6 py-3.5 font-semibold text-zinc-900 whitespace-nowrap">
                            #{t.codigo || `TKT-${t.id}`}
                          </td>
                          <td className="px-6 py-3.5 text-zinc-900">
                            {t.asunto}
                          </td>
                          <td className="px-6 py-3.5">
                            <EstadoBadge estado={t.estado} />
                          </td>
                          <td className="px-6 py-3.5 text-zinc-500 whitespace-nowrap">
                            {formatDate(t.fechaCreacion)}
                          </td>
                          <td className="px-6 py-3.5 whitespace-nowrap">
                            <PrioridadDot prioridad={t.prioridad} />
                            <span className="ml-2 text-zinc-700 capitalize">
                              {t.prioridad}
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        </div>

        {/* Columna derecha */}
        <div className="space-y-6">
          {/* Panel SLA */}
          <section className="rounded-xl bg-white border border-zinc-200 p-5">
            <h3 className="text-base font-bold text-zinc-900">SLA en curso</h3>
            <p className="text-xs text-zinc-500">
              Tickets activos próximos a vencer
            </p>

            <div className="mt-4 space-y-3">
              {isLoading ? (
                <p className="text-xs text-zinc-400">Cargando…</p>
              ) : slaItems.length === 0 ? (
                <p className="text-xs text-zinc-500">
                  No tienes SLAs próximos a vencer.
                </p>
              ) : (
                slaItems.map((s) => <SlaItem key={s.id} item={s} />)
              )}
            </div>
          </section>

          {/* Tip */}
          <section className="rounded-xl bg-primary-faint p-5">
            <div className="flex items-center gap-3">
              <div className="w-7 h-7 rounded-full bg-primary text-white text-xs font-bold flex items-center justify-center">
                i
              </div>
              <h3 className="text-sm font-bold text-indigo-900">
                ¿Sabías qué?
              </h3>
            </div>
            <p className="mt-3 text-sm text-zinc-700 leading-relaxed">
              Antes de crear un ticket, consulta nuestra base de conocimiento.
              Encontrarás soluciones a problemas comunes y podrás resolver tu
              caso al instante.
            </p>
            <button
              type="button"
              onClick={() => alert("Base de conocimiento — próximamente.")}
              className="mt-5 w-full h-11 rounded-lg bg-primary hover:bg-primary-light text-white text-sm font-semibold transition"
            >
              Explorar soluciones
            </button>
          </section>
        </div>
      </div>
    </div>
  );
}

const STATS_DEF = [
  {
    key: "abiertos",
    label: "Tickets abiertos",
    iconBg: "bg-blue-100",
    iconColor: "text-blue-500",
    icon: <IconBoxList />,
    footerFor: () => ({
      text: "Tickets en estado abierto",
      color: "text-zinc-500",
    }),
  },
  {
    key: "enProceso",
    label: "En proceso",
    iconBg: "bg-amber-100",
    iconColor: "text-amber-500",
    icon: <IconClock />,
    footerFor: () => ({
      text: "Asignados a un agente",
      color: "text-zinc-500",
    }),
  },
  {
    key: "resueltos",
    label: "Resueltos",
    iconBg: "bg-green-100",
    iconColor: "text-success",
    icon: <IconCheck />,
    footerFor: () => ({ text: "Total cerrados", color: "text-success" }),
  },
  {
    key: "total",
    label: "Total tickets",
    iconBg: "bg-violet-100",
    iconColor: "text-violet-500",
    icon: <IconBoxList />,
    footerFor: () => ({
      text: "Histórico de tu cuenta",
      color: "text-zinc-500",
    }),
  },
];

function buildStats(tickets) {
  return {
    abiertos: tickets.filter((t) => t.estado === "abierto").length,
    enProceso: tickets.filter((t) => t.estado === "en_proceso").length,
    resueltos: tickets.filter((t) => t.estado === "cerrado").length,
    total: tickets.length,
  };
}

function buildSlaItems(tickets) {
  const now = Date.now();
  return tickets
    .filter(
      (t) =>
        ["abierto", "en_proceso", "reabierto"].includes(t.estado) &&
        t.fechaVencimientoSla
    )
    .map((t) => {
      const venceMs = new Date(t.fechaVencimientoSla).getTime();
      const inicioMs = new Date(t.fechaCreacion).getTime();
      const total = Math.max(1, venceMs - inicioMs);
      const transcurrido = Math.max(0, now - inicioMs);
      const progress = Math.min(1, transcurrido / total);
      const minutosRestantes = Math.round((venceMs - now) / 60000);

      let urgencia;
      if (minutosRestantes <= 0) urgencia = "VENCIDO";
      else if (minutosRestantes <= 60) urgencia = "URGENTE";
      else urgencia = "PRONTO";

      return {
        id: t.id,
        codigo: t.codigo || `TKT-${t.id}`,
        asunto: t.asunto,
        progress,
        minutosRestantes,
        urgencia,
      };
    })
    .sort((a, b) => a.minutosRestantes - b.minutosRestantes)
    .slice(0, 3);
}

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "—";
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}

function formatTimeRemaining(minutos) {
  if (minutos <= 0) return "Vencido";
  if (minutos < 60) return `Vence en ${minutos} minutos`;
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  return m === 0 ? `Vence en ${h}h` : `Vence en ${h}h ${m}min`;
}

function StatCard({ label, value, iconBg, iconColor, icon, footer }) {
  return (
    <div className="rounded-xl bg-white border border-zinc-200 p-5">
      <div className="flex items-start justify-between">
        <p className="text-sm text-zinc-500">{label}</p>
        <div
          className={`w-10 h-10 rounded-lg ${iconBg} ${iconColor} flex items-center justify-center`}
        >
          {icon}
        </div>
      </div>
      <p className="mt-3 text-3xl font-bold text-zinc-900">
        {value === null ? (
          <span className="inline-block w-8 h-7 rounded bg-zinc-200 animate-pulse align-middle" />
        ) : (
          value
        )}
      </p>
      <p className={`mt-1 text-[11px] ${footer.color}`}>{footer.text}</p>
    </div>
  );
}

function SkeletonRow() {
  return (
    <tr>
      {[80, 240, 90, 90, 80].map((w, i) => (
        <td key={i} className="px-6 py-3.5">
          <div
            className="h-3 rounded bg-zinc-100 animate-pulse"
            style={{ width: w }}
          />
        </td>
      ))}
    </tr>
  );
}

function EstadoBadge({ estado }) {
  const map = {
    abierto: { label: "ABIERTO", cls: "bg-blue-100 text-blue-800" },
    en_proceso: { label: "EN PROCESO", cls: "bg-amber-100 text-amber-800" },
    cerrado: { label: "CERRADO", cls: "bg-green-100 text-green-800" },
    vencido: { label: "VENCIDO", cls: "bg-red-100 text-red-800" },
    cancelado: { label: "CANCELADO", cls: "bg-zinc-100 text-zinc-600" },
    reabierto: { label: "REABIERTO", cls: "bg-indigo-100 text-indigo-800" },
  };
  const { label, cls } = map[estado] || {
    label: (estado || "").toUpperCase(),
    cls: "bg-zinc-100 text-zinc-700",
  };
  return (
    <span
      className={`inline-flex items-center justify-center px-2.5 py-1 rounded-full text-[10px] font-bold tracking-wider ${cls}`}
    >
      {label}
    </span>
  );
}

function PrioridadDot({ prioridad }) {
  const color =
    {
      alta: "bg-red-500",
      media: "bg-amber-500",
      baja: "bg-success",
    }[prioridad] || "bg-zinc-400";
  return (
    <span
      className={`inline-block w-2 h-2 rounded-full align-middle ${color}`}
    />
  );
}

function SlaItem({ item }) {
  const styles =
    item.urgencia === "URGENTE" || item.urgencia === "VENCIDO"
      ? {
          cardBg: "bg-red-50",
          cardBorder: "border-red-200",
          title: "text-red-900",
          tag: "bg-red-500",
          barTrack: "bg-red-100",
          barFill: "bg-red-500",
          timeText: "text-red-900",
        }
      : {
          cardBg: "bg-amber-50",
          cardBorder: "border-amber-200",
          title: "text-amber-900",
          tag: "bg-amber-500",
          barTrack: "bg-amber-100",
          barFill: "bg-amber-500",
          timeText: "text-amber-900",
        };

  return (
    <div className={`rounded-lg border p-4 ${styles.cardBg} ${styles.cardBorder}`}>
      <div className="flex items-center justify-between">
        <p className={`text-xs font-semibold ${styles.title}`}>
          #{item.codigo}
        </p>
        <span
          className={`px-2 py-0.5 rounded-full text-[9px] font-bold tracking-wider text-white ${styles.tag}`}
        >
          {item.urgencia}
        </span>
      </div>
      <p className="mt-1.5 text-sm text-zinc-900 truncate">{item.asunto}</p>
      <div
        className={`mt-3 h-1.5 w-full rounded-full overflow-hidden ${styles.barTrack}`}
      >
        <div
          className={`h-full rounded-full ${styles.barFill}`}
          style={{ width: `${Math.round(item.progress * 100)}%` }}
        />
      </div>
      <p className={`mt-2 text-xs font-medium ${styles.timeText}`}>
        {formatTimeRemaining(item.minutosRestantes)}
      </p>
    </div>
  );
}

function IconBoxList() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="1" y="2" width="14" height="14" rx="2" />
      <line x1="3" y1="6" x2="13" y2="6" />
      <line x1="3" y1="10" x2="11" y2="10" />
    </svg>
  );
}

function IconClock() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="8" cy="8" r="6.5" />
      <path d="M8 4 v4 l3 2" />
    </svg>
  );
}

function IconCheck() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 9 l4 4 l8 -10" />
    </svg>
  );
}