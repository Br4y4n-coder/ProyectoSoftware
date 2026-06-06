import { useEffect, useMemo, useState } from "react";
import { RefreshCw, Search, UserPlus, X } from "lucide-react";
import ticketsService from "../../services/ticketsService";
import AssignTicketModal from "../../components/admin/AssignTicketModal";
import {
  PriorityBadge,
  StatusBadge,
} from "../../components/common/ticketHelpers";
import type { TicketPriority, TicketStatus } from "../../types";

interface Ticket {
  id: number;
  codigo: string;
  asunto: string;
  descripcion: string;
  tipo: string;
  prioridad: string;
  estado: string;
  categoriaNombre: string | null;
  clienteNombre: string | null;
  agenteId: number | null;
  agenteNombre: string | null;
  fechaCreacion: string | null;
  fechaVencimientoSla: string | null;
  tiempoResolucionMinutos: number | null;
}

interface HistorialEntry {
  id: number;
  campoModificado: string;
  valorAnterior: string | null;
  valorNuevo: string | null;
  usuarioNombre: string | null;
  fechaHora: string;
}

function formatDate(iso: string | null | undefined) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "—";
  return d.toLocaleDateString("es-CO", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDateShort(iso: string | null | undefined) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "—";
  return d.toLocaleDateString("es-CO", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function isSlaVencido(fechaVencimiento: string): boolean {
  return new Date(fechaVencimiento).getTime() < Date.now();
}

function slaProgress(
  fechaCreacion: string | null,
  fechaVencimiento: string | null
) {
  if (!fechaCreacion || !fechaVencimiento) return 0;
  const now = Date.now();
  const inicio = new Date(fechaCreacion).getTime();
  const fin = new Date(fechaVencimiento).getTime();
  const total = Math.max(1, fin - inicio);
  return Math.min(100, Math.round(((now - inicio) / total) * 100));
}

export default function TicketsAdmin() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [search, setSearch] = useState("");
  const [estadoFilter, setEstadoFilter] = useState("todos");
  const [prioridadFilter, setPrioridadFilter] = useState("todas");

  // Panel de detalle
  const [detalleId, setDetalleId] = useState<number | null>(null);
  const [detalle, setDetalle] = useState<Ticket | null>(null);
  const [historial, setHistorial] = useState<HistorialEntry[]>([]);
  const [loadingDetalle, setLoadingDetalle] = useState(false);

  // Modal asignar
  const [asignarTicketId, setAsignarTicketId] = useState<number | null>(null);

  const fetchTickets = async () => {
    setIsLoading(true);
    try {
      const { data } = await ticketsService.listar({ page: 0, size: 200 });
      setTickets(data?.data?.content || []);
      setError("");
    } catch {
      setError("Error al cargar los tickets");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  // Carga detalle + historial cuando cambia detalleId
  useEffect(() => {
    if (!detalleId) {
      setDetalle(null);
      setHistorial([]);
      return;
    }
    let cancelled = false;
    setLoadingDetalle(true);
    Promise.all([
      ticketsService.obtenerPorId(detalleId),
      ticketsService.historial(detalleId),
    ])
      .then(([ticketRes, histRes]) => {
        if (cancelled) return;
        setDetalle(ticketRes.data?.data ?? null);
        setHistorial(histRes.data?.data ?? []);
      })
      .catch(() => {
        if (!cancelled) setDetalle(null);
      })
      .finally(() => {
        if (!cancelled) setLoadingDetalle(false);
      });
    return () => {
      cancelled = true;
    };
  }, [detalleId]);

  const filtered = useMemo(() => {
    return tickets.filter((t) => {
      if (estadoFilter !== "todos" && t.estado !== estadoFilter) return false;
      if (prioridadFilter !== "todas" && t.prioridad !== prioridadFilter)
        return false;
      if (search.trim()) {
        const q = search.toLowerCase();
        return (
          (t.codigo?.toLowerCase() || "").includes(q) ||
          (t.asunto?.toLowerCase() || "").includes(q) ||
          (t.clienteNombre?.toLowerCase() || "").includes(q) ||
          (t.agenteNombre?.toLowerCase() || "").includes(q)
        );
      }
      return true;
    });
  }, [tickets, estadoFilter, prioridadFilter, search]);

  const stats = useMemo(
    () => ({
      total: tickets.length,
      abiertos: tickets.filter((t) => t.estado === "abierto").length,
      enProceso: tickets.filter((t) => t.estado === "en_proceso").length,
      cerrados: tickets.filter((t) => t.estado === "cerrado").length,
    }),
    [tickets]
  );

  const flash = (msg: string) => {
    setMensaje(msg);
    setTimeout(() => setMensaje(""), 3000);
  };

  if (isLoading) return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <header className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs text-zinc-400">
            Admin ›{" "}
            <span className="text-zinc-600 font-medium">Gestión de tickets</span>
          </p>
          <h1 className="text-2xl font-bold text-zinc-900 mt-0.5">
            Gestión de tickets
          </h1>
        </div>
        <button
          type="button"
          onClick={fetchTickets}
          className="inline-flex items-center gap-2 h-9 px-4 text-sm font-medium rounded-lg border border-zinc-200 bg-white hover:bg-zinc-50 transition"
        >
          <RefreshCw className="w-4 h-4" /> Actualizar
        </button>
      </header>

      {mensaje && (
        <div className="rounded-xl bg-green-50 border border-green-200 px-4 py-3">
          <p className="text-green-700 text-sm">{mensaje}</p>
        </div>
      )}
      {error && (
        <div className="rounded-xl bg-red-50 border border-red-200 px-4 py-3">
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      {/* KPIs */}
      <section className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <KpiCard label="TOTAL" value={stats.total} color="text-zinc-900" />
        <KpiCard label="ABIERTOS" value={stats.abiertos} color="text-blue-600" />
        <KpiCard label="EN PROCESO" value={stats.enProceso} color="text-amber-600" />
        <KpiCard label="CERRADOS" value={stats.cerrados} color="text-emerald-600" />
      </section>

      {/* Toolbar */}
      <div className="rounded-xl bg-white border border-zinc-200 p-3 shadow-sm flex flex-col lg:flex-row gap-3 lg:items-center">
        <div className="flex-1 flex items-center gap-2 h-10 px-3 rounded-lg bg-zinc-50 border border-zinc-200">
          <Search className="w-4 h-4 text-zinc-400" />
          <input
            type="search"
            placeholder="Buscar por código, asunto, cliente o agente..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 text-sm outline-none bg-transparent"
          />
        </div>
        <select
          value={estadoFilter}
          onChange={(e) => setEstadoFilter(e.target.value)}
          className="h-10 px-3 rounded-lg border border-zinc-200 text-sm bg-white"
        >
          <option value="todos">Estado: Todos</option>
          <option value="abierto">Abierto</option>
          <option value="en_proceso">En proceso</option>
          <option value="cerrado">Cerrado</option>
          <option value="vencido">Vencido</option>
        </select>
        <select
          value={prioridadFilter}
          onChange={(e) => setPrioridadFilter(e.target.value)}
          className="h-10 px-3 rounded-lg border border-zinc-200 text-sm bg-white"
        >
          <option value="todas">Prioridad: Todas</option>
          <option value="critica">Crítica</option>
          <option value="alta">Alta</option>
          <option value="media">Media</option>
          <option value="baja">Baja</option>
        </select>
      </div>

      {/* Tabla */}
      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-[10px] font-semibold tracking-widest text-zinc-400 text-left border-b border-zinc-100">
                <th className="px-5 py-3">TICKET</th>
                <th className="px-4 py-3">CLIENTE</th>
                <th className="px-4 py-3">AGENTE</th>
                <th className="px-4 py-3">PRIORIDAD</th>
                <th className="px-4 py-3">ESTADO</th>
                <th className="px-4 py-3">FECHA</th>
                <th className="px-4 py-3 text-right pr-5">ACCIONES</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-50">
              {filtered.map((t) => (
                <tr key={t.id} className="hover:bg-zinc-50 transition">
                  <td className="px-5 py-3">
                    <p className="font-semibold text-zinc-900 text-xs font-mono">
                      {t.codigo}
                    </p>
                    <p className="text-zinc-600 truncate max-w-[200px]">
                      {t.asunto}
                    </p>
                  </td>
                  <td className="px-4 py-3 text-zinc-600">
                    {t.clienteNombre || "—"}
                  </td>
                  <td className="px-4 py-3">
                    {t.agenteNombre ? (
                      <span className="text-zinc-700">{t.agenteNombre}</span>
                    ) : (
                      <span className="text-zinc-300 italic text-xs">
                        Sin asignar
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <PriorityBadge priority={t.prioridad as TicketPriority} />
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={t.estado as TicketStatus} />
                  </td>
                  <td className="px-4 py-3 text-zinc-500 text-xs">
                    {formatDateShort(t.fechaCreacion)}
                  </td>
                  <td className="px-4 py-3 pr-5 text-right whitespace-nowrap">
                    <button
                      type="button"
                      onClick={() => setDetalleId(t.id)}
                      className="text-primary text-xs font-medium hover:underline"
                    >
                      Ver detalles
                    </button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td
                    colSpan={7}
                    className="px-5 py-10 text-center text-zinc-400"
                  >
                    No hay tickets que coincidan con los filtros
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <p className="text-sm text-zinc-400">
        Mostrando {filtered.length} de {stats.total} tickets
      </p>

      {/* Panel de detalle (slide-over) */}
      {detalleId !== null && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-40 bg-black/30"
            onClick={() => setDetalleId(null)}
          />
          {/* Panel */}
          <div className="fixed inset-y-0 right-0 z-50 w-full max-w-2xl bg-white shadow-2xl flex flex-col">
            {/* Header del panel */}
            <div className="flex items-start justify-between px-6 py-5 border-b border-zinc-200 shrink-0">
              <div className="min-w-0 flex-1">
                {detalle ? (
                  <>
                    <p className="text-xs text-zinc-400 font-mono">
                      {detalle.codigo}
                    </p>
                    <h2 className="text-lg font-bold text-zinc-900 truncate mt-0.5">
                      {detalle.asunto}
                    </h2>
                  </>
                ) : (
                  <div className="space-y-1.5 animate-pulse">
                    <div className="h-3 w-24 bg-zinc-100 rounded" />
                    <div className="h-5 w-64 bg-zinc-100 rounded" />
                  </div>
                )}
              </div>
              <button
                type="button"
                onClick={() => setDetalleId(null)}
                className="ml-4 p-1.5 rounded-lg hover:bg-zinc-100 transition shrink-0"
              >
                <X className="w-5 h-5 text-zinc-500" />
              </button>
            </div>

            {/* Contenido scrolleable */}
            <div className="flex-1 overflow-y-auto px-6 py-5 space-y-6">
              {loadingDetalle ? (
                <div className="space-y-4 animate-pulse">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="h-20 bg-zinc-100 rounded-xl" />
                  ))}
                </div>
              ) : detalle ? (
                <>
                  {/* Badges */}
                  <div className="flex flex-wrap gap-2 items-center">
                    <StatusBadge status={detalle.estado as TicketStatus} />
                    <PriorityBadge priority={detalle.prioridad as TicketPriority} />
                    {detalle.categoriaNombre && (
                      <span className="px-2.5 py-1 rounded-full bg-zinc-100 text-zinc-600 text-[10px] font-semibold">
                        {detalle.categoriaNombre}
                      </span>
                    )}
                  </div>

                  {/* Info */}
                  <div className="grid grid-cols-2 gap-x-8 gap-y-4">
                    <MetaRow label="Cliente" value={detalle.clienteNombre || "—"} />
                    <MetaRow
                      label="Tipo"
                      value={
                        detalle.tipo
                          ? detalle.tipo.charAt(0).toUpperCase() +
                            detalle.tipo.slice(1)
                          : "—"
                      }
                    />
                    <MetaRow label="Creado" value={formatDate(detalle.fechaCreacion)} />
                    <MetaRow
                      label="Vence SLA"
                      value={formatDate(detalle.fechaVencimientoSla)}
                    />
                    {detalle.tiempoResolucionMinutos != null && (
                      <MetaRow
                        label="Tiempo resolución"
                        value={
                          detalle.tiempoResolucionMinutos < 60
                            ? `${detalle.tiempoResolucionMinutos} min`
                            : `${Math.floor(detalle.tiempoResolucionMinutos / 60)}h ${detalle.tiempoResolucionMinutos % 60}min`
                        }
                      />
                    )}
                  </div>

                  {/* Barra SLA */}
                  {detalle.fechaVencimientoSla && (
                    <SlaBar
                      fechaCreacion={detalle.fechaCreacion}
                      fechaVencimientoSla={detalle.fechaVencimientoSla}
                    />
                  )}

                  {/* Asignación de agente */}
                  <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
                    <p className="text-[10px] font-semibold tracking-widest text-zinc-400 mb-3">
                      AGENTE ASIGNADO
                    </p>
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-primary flex items-center justify-center shrink-0">
                          <span className="text-white text-xs font-bold">
                            {detalle.agenteNombre
                              ? detalle.agenteNombre.slice(0, 2).toUpperCase()
                              : "?"}
                          </span>
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-zinc-900">
                            {detalle.agenteNombre || "Sin asignar"}
                          </p>
                          <p className="text-xs text-zinc-400">Agente asignado</p>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={() => setAsignarTicketId(detalle.id)}
                        className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg bg-primary text-white hover:opacity-90 transition"
                      >
                        <UserPlus className="w-4 h-4" />
                        {detalle.agenteId ? "Reasignar" : "Asignar agente"}
                      </button>
                    </div>
                  </div>

                  {/* Descripción */}
                  <div>
                    <p className="text-[10px] font-semibold tracking-widest text-zinc-400 mb-2">
                      DESCRIPCIÓN
                    </p>
                    <p className="text-sm text-zinc-700 leading-relaxed whitespace-pre-wrap">
                      {detalle.descripcion || "Sin descripción"}
                    </p>
                  </div>

                  {/* Historial */}
                  {historial.length > 0 && (
                    <div>
                      <p className="text-[10px] font-semibold tracking-widest text-zinc-400 mb-3">
                        HISTORIAL DE CAMBIOS
                      </p>
                      <ul className="space-y-2.5 max-h-64 overflow-y-auto">
                        {historial.map((entry) => (
                          <li
                            key={entry.id}
                            className="flex gap-3 items-start text-sm"
                          >
                            <div className="w-2 h-2 mt-1.5 rounded-full bg-primary shrink-0" />
                            <div className="min-w-0">
                              <p className="text-zinc-700">
                                <span className="font-medium">
                                  {entry.usuarioNombre || "Sistema"}
                                </span>{" "}
                                cambió{" "}
                                <span className="font-medium">
                                  {entry.campoModificado}
                                </span>
                                {entry.valorAnterior && (
                                  <>
                                    {" "}de{" "}
                                    <span className="text-zinc-400 line-through">
                                      {entry.valorAnterior}
                                    </span>
                                  </>
                                )}{" "}
                                a{" "}
                                <span className="font-semibold text-primary">
                                  {entry.valorNuevo}
                                </span>
                              </p>
                              <p className="text-xs text-zinc-400 mt-0.5">
                                {formatDate(entry.fechaHora)}
                              </p>
                            </div>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </>
              ) : (
                <p className="text-zinc-400 text-center py-10">
                  No se pudo cargar el ticket
                </p>
              )}
            </div>
          </div>
        </>
      )}

      {/* Modal para asignar agente */}
      {asignarTicketId !== null && (
        <AssignTicketModal
          ticketId={asignarTicketId}
          onClose={() => setAsignarTicketId(null)}
          onAssigned={() => {
            setAsignarTicketId(null);
            flash("Agente asignado correctamente");
            fetchTickets();
            // Recargar el detalle del ticket abierto
            if (detalleId) {
              ticketsService
                .obtenerPorId(detalleId)
                .then((res) => setDetalle(res.data?.data ?? null));
            }
          }}
        />
      )}
    </div>
  );
}

function KpiCard({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: string;
}) {
  return (
    <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
      <p className="text-[10px] font-semibold tracking-widest text-zinc-400">
        {label}
      </p>
      <p className={`mt-1 text-2xl font-bold ${color}`}>{value}</p>
    </div>
  );
}

function MetaRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-[10px] font-semibold tracking-widest text-zinc-400 uppercase">
        {label}
      </dt>
      <dd className="text-zinc-800 font-medium mt-0.5 text-sm">{value}</dd>
    </div>
  );
}

function SlaBar({
  fechaCreacion,
  fechaVencimientoSla,
}: {
  fechaCreacion: string | null;
  fechaVencimientoSla: string;
}) {
  const pct = slaProgress(fechaCreacion, fechaVencimientoSla);
  const vencido = isSlaVencido(fechaVencimientoSla);
  return (
    <div
      className={`rounded-xl p-4 border ${
        vencido ? "bg-red-50 border-red-100" : "bg-amber-50 border-amber-100"
      }`}
    >
      <p
        className={`text-xs font-semibold ${
          vencido ? "text-red-700" : "text-amber-700"
        }`}
      >
        {vencido ? "SLA vencido" : `SLA — ${pct}% transcurrido`}
      </p>
      <div className="mt-2 h-2 rounded-full bg-white/60 overflow-hidden">
        <div
          className={`h-full rounded-full transition-all ${
            vencido ? "bg-red-500" : "bg-amber-500"
          }`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}
