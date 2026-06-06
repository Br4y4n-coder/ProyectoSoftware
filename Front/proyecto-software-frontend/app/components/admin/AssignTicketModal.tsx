import { useEffect, useMemo, useState } from "react";
import { Search, Sparkles, X } from "lucide-react";
import apiFetch from "../../api/apiFetch";

interface TicketInfo {
  id: number;
  codigo: string;
  asunto: string;
  prioridad: string;
  estado: string;
  categoriaNombre: string | null;
  clienteNombre: string | null;
  fechaVencimientoSla: string | null;
}

interface UsuarioApi {
  id: number;
  nombres: string;
  apellidos: string;
  rol: string;
  estado: string;
  area?: string | null;
  nivelAgente?: number | null;
}

interface TicketCargaApi {
  agenteId: number | null;
  estado: string;
  fechaVencimientoSla: string | null;
}

interface AgenteInfo {
  id: number;
  nombre: string;
  iniciales: string;
  area: string | null;
  nivel: number;
  activos: number;
  slaCriticos: number;
  disponibilidad: "disponible" | "ocupado" | "saturado";
  match: number;
  recomendado: boolean;
}

const AVATAR_COLORS = [
  "bg-emerald-500",
  "bg-blue-500",
  "bg-violet-500",
  "bg-pink-500",
  "bg-teal-500",
  "bg-indigo-500",
];

function formatSlaRestante(fecha: string | null): string {
  if (!fecha) return "—";
  const diffMin = Math.round((new Date(fecha).getTime() - Date.now()) / 60000);
  if (diffMin < 0) return "Vencido";
  const h = Math.floor(diffMin / 60);
  const m = diffMin % 60;
  return h > 0 ? `${h}h ${m}m` : `${m} min`;
}

export default function AssignTicketModal({
  ticketId,
  onClose,
  onAssigned,
}: {
  ticketId: number | string;
  onClose: () => void;
  onAssigned?: () => void;
}) {
  const [ticket, setTicket] = useState<TicketInfo | null>(null);
  const [agentes, setAgentes] = useState<AgenteInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filtroArea, setFiltroArea] = useState(false);
  const [filtroDisponibles, setFiltroDisponibles] = useState(false);
  const [filtroNivel, setFiltroNivel] = useState(false);
  const [notificar, setNotificar] = useState(true);
  const [asignando, setAsignando] = useState<number | null>(null);

  useEffect(() => {
    let cancelado = false;

    async function cargar() {
      setLoading(true);
      setError("");
      try {
        const [ticketRes, usuariosRes, ticketsRes] = await Promise.all([
          apiFetch(`/api/tickets/${ticketId}`),
          apiFetch(`/api/usuarios?page=0&size=200`),
          apiFetch(`/api/tickets?page=0&size=500`),
        ]);

        if (!ticketRes.ok) throw new Error("No se pudo cargar el ticket");
        if (!usuariosRes.ok) throw new Error("No se pudieron cargar los agentes");

        const t: TicketInfo = (await ticketRes.json())?.data;
        const usuarios: UsuarioApi[] = (await usuariosRes.json())?.data?.content ?? [];
        const tickets: TicketCargaApi[] = ticketsRes.ok
          ? (await ticketsRes.json())?.data?.content ?? []
          : [];

        if (cancelado) return;

        const cerrados = new Set(["cerrado", "resuelto"]);
        const cargaPorAgente = new Map<number, { activos: number; criticos: number }>();
        tickets.forEach((tk) => {
          if (!tk.agenteId || cerrados.has(tk.estado)) return;
          const c = cargaPorAgente.get(tk.agenteId) || { activos: 0, criticos: 0 };
          c.activos += 1;
          if (tk.fechaVencimientoSla) {
            const min = (new Date(tk.fechaVencimientoSla).getTime() - Date.now()) / 60000;
            if (min < 120) c.criticos += 1;
          }
          cargaPorAgente.set(tk.agenteId, c);
        });

        const categoria = (t.categoriaNombre || "").toLowerCase();

        const lista: AgenteInfo[] = usuarios
          .filter((u) => u.rol === "agente" && u.estado === "activo")
          .map((u) => {
            const carga = cargaPorAgente.get(u.id) || { activos: 0, criticos: 0 };
            const disponibilidad =
              carga.activos >= 7 ? "saturado" : carga.activos >= 5 ? "ocupado" : "disponible";
            const area: string | null = u.area ?? null;
            const areaMatch =
              !!categoria &&
              !!area &&
              (area.toLowerCase().includes(categoria) ||
                categoria.includes(area.toLowerCase()));
            const match = Math.max(
              5,
              Math.min(
                99,
                50 +
                  (areaMatch ? 30 : 0) +
                  Math.max(0, 16 - carga.activos * 2) -
                  carga.criticos * 5
              )
            );
            return {
              id: u.id,
              nombre: `${u.nombres} ${u.apellidos}`.trim(),
              iniciales:
                `${u.nombres?.[0] ?? ""}${u.apellidos?.[0] ?? ""}`.toUpperCase() || "?",
              area,
              nivel: u.nivelAgente ?? 1,
              activos: carga.activos,
              slaCriticos: carga.criticos,
              disponibilidad,
              match,
              recomendado: false,
            } as AgenteInfo;
          })
          .sort((a, b) => b.match - a.match);

        if (lista.length > 0) lista[0].recomendado = true;

        setTicket(t);
        setAgentes(lista);
      } catch (e) {
        if (!cancelado) setError(e instanceof Error ? e.message : "Error de conexión");
      } finally {
        if (!cancelado) setLoading(false);
      }
    }

    cargar();
    return () => {
      cancelado = true;
    };
  }, [ticketId]);

  const filtrados = useMemo(() => {
    const q = search.toLowerCase();
    const categoria = (ticket?.categoriaNombre || "").toLowerCase();
    return agentes.filter((a) => {
      if (q) {
        const hay =
          a.nombre.toLowerCase().includes(q) ||
          (a.area?.toLowerCase() || "").includes(q) ||
          `nivel ${a.nivel}`.includes(q);
        if (!hay) return false;
      }
      if (filtroDisponibles && a.disponibilidad !== "disponible") return false;
      if (filtroNivel && a.nivel < 1) return false;
      if (filtroArea && categoria) {
        const area = a.area?.toLowerCase() || "";
        if (!area.includes(categoria) && !categoria.includes(area)) return false;
      }
      return true;
    });
  }, [agentes, search, filtroArea, filtroDisponibles, filtroNivel, ticket]);

  const recomendado = filtrados.find((a) => a.recomendado);
  const otros = filtrados.filter((a) => !a.recomendado);

  const asignar = async (agente: AgenteInfo) => {
    setAsignando(agente.id);
    setError("");
    try {
      const response = await apiFetch(`/api/tickets/${ticketId}/asignar`, {
        method: "PATCH",
        body: JSON.stringify({ agenteId: agente.id }),
      });
      if (response.ok) {
        onAssigned?.();
        onClose();
      } else {
        const data = await response.json();
        setError(data?.message || "Error al asignar el agente");
      }
    } catch {
      setError("Error de conexión");
    } finally {
      setAsignando(null);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-xl max-h-[90vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-start justify-between px-6 pt-5 pb-4 border-b border-zinc-100">
          <div>
            <h2 className="text-lg font-bold text-zinc-900">Asignar ticket a un agente</h2>
            <p className="text-xs text-zinc-400 mt-0.5">
              Selecciona el agente más apropiado según área y carga actual
            </p>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg hover:bg-zinc-100 transition">
            <X className="w-5 h-5 text-zinc-400" />
          </button>
        </div>

        <div className="px-6 py-4 space-y-4 overflow-y-auto flex-1">
          {loading ? (
            <div className="space-y-3 animate-pulse">
              <div className="h-20 bg-zinc-100 rounded-xl" />
              <div className="h-10 bg-zinc-100 rounded-xl" />
              <div className="h-24 bg-zinc-100 rounded-xl" />
              <div className="h-16 bg-zinc-100 rounded-xl" />
            </div>
          ) : (
            <>
              {/* Ticket a asignar */}
              {ticket && (
                <div className="rounded-xl bg-primary-faint border border-primary-subtle p-4">
                  <p className="text-[10px] font-bold tracking-widest text-primary">
                    TICKET A ASIGNAR
                  </p>
                  <p className="mt-1 font-bold text-zinc-900">
                    #{ticket.codigo} — {ticket.asunto}
                  </p>
                  <div className="mt-1.5 flex flex-wrap items-center gap-2 text-[11px] text-zinc-500">
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-100 text-red-600 font-bold">
                      ● {ticket.prioridad?.toUpperCase()}
                    </span>
                    <span>· Categoría: {ticket.categoriaNombre || "—"}</span>
                    <span>· Cliente: {ticket.clienteNombre || "—"}</span>
                    <span>
                      · SLA:{" "}
                      <strong className="text-zinc-700">
                        {formatSlaRestante(ticket.fechaVencimientoSla)}
                      </strong>
                    </span>
                  </div>
                </div>
              )}

              {error && (
                <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700">
                  {error}
                </div>
              )}

              {/* Buscador */}
              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1.5">
                  Buscar agente
                </label>
                <div className="flex items-center gap-2 h-10 px-3 rounded-lg bg-white border border-zinc-200">
                  <Search className="w-4 h-4 text-zinc-400" />
                  <input
                    type="search"
                    placeholder="Nombre, área o nivel..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="flex-1 text-sm outline-none bg-transparent"
                  />
                </div>
              </div>

              {/* Chips de filtro */}
              <div className="flex flex-wrap items-center gap-2">
                {ticket?.categoriaNombre && (
                  <Chip
                    active={filtroArea}
                    onClick={() => setFiltroArea(!filtroArea)}
                    label={`Área: ${ticket.categoriaNombre}`}
                  />
                )}
                <Chip
                  active={filtroDisponibles}
                  onClick={() => setFiltroDisponibles(!filtroDisponibles)}
                  label="Disponibles"
                />
                <Chip
                  active={filtroNivel}
                  onClick={() => setFiltroNivel(!filtroNivel)}
                  label="Nivel 1+"
                />
                <button
                  type="button"
                  onClick={() => {
                    setSearch("");
                    setFiltroArea(false);
                    setFiltroDisponibles(false);
                    setFiltroNivel(false);
                  }}
                  className="ml-auto inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline"
                >
                  Recomendar automáticamente <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                </button>
              </div>

              {/* Recomendado */}
              {recomendado && (
                <div className="rounded-xl border-2 border-emerald-300 bg-emerald-50 overflow-hidden flex items-stretch">
                  <div className="bg-emerald-500 text-white px-3 py-3 flex flex-col items-center justify-center w-24 shrink-0 text-center">
                    <span className="text-[9px] font-bold tracking-wider leading-tight">
                      RECOMEN-DADO
                    </span>
                    <span className="text-[10px] mt-1 opacity-90">{recomendado.match}% match</span>
                  </div>
                  <div className="flex items-center gap-3 p-3 flex-1 min-w-0">
                    <span className="w-9 h-9 rounded-full bg-teal-500 text-white text-xs font-bold flex items-center justify-center shrink-0">
                      {recomendado.iniciales}
                    </span>
                    <div className="flex-1 min-w-0">
                      <p className="font-bold text-zinc-900 truncate">{recomendado.nombre}</p>
                      <p className="text-[11px] text-zinc-500 truncate">
                        {recomendado.area || "Soporte"} · Nivel {recomendado.nivel} ·{" "}
                        {recomendado.activos} tickets activos
                      </p>
                      <div className="mt-1 flex items-center gap-2">
                        <DisponibilidadBadge d={recomendado.disponibilidad} />
                        {recomendado.nivel >= 2 && (
                          <span className="text-[10px] font-bold text-amber-600">★ EXPERTO</span>
                        )}
                      </div>
                    </div>
                    <button
                      type="button"
                      disabled={asignando !== null}
                      onClick={() => asignar(recomendado)}
                      className="shrink-0 px-4 py-2 rounded-lg bg-emerald-500 text-white text-sm font-bold hover:bg-emerald-600 transition disabled:opacity-50"
                    >
                      {asignando === recomendado.id ? "Asignando..." : "Asignar"}
                    </button>
                  </div>
                </div>
              )}

              {/* Otros agentes */}
              {otros.length > 0 && (
                <div>
                  <p className="text-[10px] font-bold tracking-widest text-zinc-400 mb-2">
                    OTROS AGENTES DISPONIBLES
                  </p>
                  <ul className="space-y-2">
                    {otros.map((a, i) => (
                      <li
                        key={a.id}
                        className="rounded-xl border border-zinc-200 bg-white p-3 flex items-center gap-3"
                      >
                        <span
                          className={`w-9 h-9 rounded-full ${AVATAR_COLORS[i % AVATAR_COLORS.length]} text-white text-xs font-bold flex items-center justify-center shrink-0`}
                        >
                          {a.iniciales}
                        </span>
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-zinc-900 truncate">{a.nombre}</p>
                          <p className="text-[11px] text-zinc-500 truncate">
                            {a.area || "Soporte"} · Nivel {a.nivel} · {a.activos} tickets activos
                            {a.slaCriticos > 0 ? ` · ${a.slaCriticos} SLA críticos` : ""}
                          </p>
                          <div className="mt-1">
                            <DisponibilidadBadge d={a.disponibilidad} />
                          </div>
                        </div>
                        <button
                          type="button"
                          disabled={asignando !== null}
                          onClick={() => asignar(a)}
                          className="shrink-0 text-sm font-medium text-primary hover:underline disabled:opacity-50"
                        >
                          {asignando === a.id ? "Asignando..." : "Asignar →"}
                        </button>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {!recomendado && otros.length === 0 && (
                <p className="text-sm text-zinc-400 text-center py-6">
                  No hay agentes que coincidan con los filtros
                </p>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-zinc-100">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-lg border border-zinc-300 text-zinc-700 text-sm font-semibold hover:bg-zinc-50 transition"
          >
            Cancelar
          </button>
          <label className="flex items-center gap-2 text-xs text-zinc-500 cursor-pointer select-none">
            Notificar al cliente
            <button
              type="button"
              role="switch"
              aria-checked={notificar}
              onClick={() => setNotificar(!notificar)}
              className={`relative w-9 h-5 rounded-full transition ${
                notificar ? "bg-primary" : "bg-zinc-300"
              }`}
            >
              <span
                className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-all ${
                  notificar ? "left-[18px]" : "left-0.5"
                }`}
              />
            </button>
          </label>
        </div>
      </div>
    </div>
  );
}

function Chip({
  label,
  active,
  onClick,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`px-3 py-1.5 rounded-full text-xs font-semibold border transition ${
        active
          ? "bg-primary text-white border-primary"
          : "bg-white text-zinc-600 border-zinc-200 hover:bg-zinc-50"
      }`}
    >
      {label}
    </button>
  );
}

function DisponibilidadBadge({ d }: { d: "disponible" | "ocupado" | "saturado" }) {
  const map = {
    disponible: "bg-emerald-100 text-emerald-700",
    ocupado: "bg-amber-100 text-amber-700",
    saturado: "bg-red-100 text-red-600",
  };
  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[9px] font-bold tracking-wider ${map[d]}`}
    >
      ● {d.toUpperCase()}
    </span>
  );
}
