import { useEffect, useState, type ReactNode } from "react";
import { Link, useParams } from "react-router";
import {
  ArrowLeft,
  Building2,
  Calendar,
  Clock,
  Send,
  User,
} from "lucide-react";
import { Avatar } from "../../components/common/Avatar";
import { Badge } from "../../components/common/Badge";
import { ProgressBar } from "../../components/common/ProgressBar";
import {
  PriorityBadge,
  StatusBadge,
} from "../../components/common/ticketHelpers";
import { useAuth } from "../../contexts/AuthContext";
import ticketsService from "../../services/ticketsService";
import type { TicketPriority, TicketStatus } from "../../types";

interface AuthUser {
  id: number;
  nombres: string;
  apellidos: string;
  correo?: string;
  rol?: string;
}

interface TicketData {
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

type EstadoTicket = "abierto" | "en_proceso" | "cerrado";

function formatDate(iso: string | null | undefined): string {
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

function slaProgress(fechaCreacion: string | null, fechaVencimiento: string | null): number {
  if (!fechaCreacion || !fechaVencimiento) return 0;
  const now = Date.now();
  const inicio = new Date(fechaCreacion).getTime();
  const fin = new Date(fechaVencimiento).getTime();
  const total = Math.max(1, fin - inicio);
  return Math.min(100, Math.round(((now - inicio) / total) * 100));
}

function isSlaVencido(fechaVencimiento: string | null): boolean {
  if (!fechaVencimiento) return false;
  return Date.now() > new Date(fechaVencimiento).getTime();
}

export default function TicketDetail() {
  const { id } = useParams<{ id: string }>();
  const { user: rawUser } = useAuth();
  const user = rawUser as AuthUser | null;

  const [ticket, setTicket] = useState<TicketData | null>(null);
  const [historial, setHistorial] = useState<HistorialEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [status, setStatus] = useState<EstadoTicket>("abierto");
  const [savingStatus, setSavingStatus] = useState(false);
  const [asignando, setAsignando] = useState(false);
  const [successMsg, setSuccessMsg] = useState("");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;

    const load = async () => {
      try {
        const [ticketRes, histRes] = await Promise.all([
          ticketsService.obtenerPorId(Number(id)),
          ticketsService.historial(Number(id)),
        ]);

        if (cancelled) return;

        const t: TicketData = ticketRes.data?.data;
        setTicket(t);
        setStatus((t?.estado as EstadoTicket) || "abierto");
        setHistorial(histRes.data?.data || []);
      } catch (err: any) {
        if (!cancelled)
          setError(
            err?.response?.data?.message ||
              err?.message ||
              "No se pudo cargar el ticket"
          );
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [id]);

  const handleCambiarEstado = async (nuevoEstado: EstadoTicket) => {
    if (!id || nuevoEstado === status || savingStatus) return;
    setSavingStatus(true);
    try {
      const { data } = await ticketsService.cambiarEstado(Number(id), nuevoEstado);
      setStatus(data?.data?.estado || nuevoEstado);
      setTicket((prev) => prev ? { ...prev, estado: data?.data?.estado || nuevoEstado } : prev);
      flash("Estado actualizado correctamente");
    } catch (err: any) {
      flash(err?.response?.data?.message || "Error al cambiar estado", true);
    } finally {
      setSavingStatus(false);
    }
  };

  const handleAsignarme = async () => {
    if (!id || !user?.id || asignando) return;
    setAsignando(true);
    try {
      const { data } = await ticketsService.asignar(Number(id), user.id);
      setTicket((prev) =>
        prev
          ? {
              ...prev,
              agenteId: data?.data?.agenteId ?? user.id,
              agenteNombre: data?.data?.agenteNombre ?? `${user.nombres} ${user.apellidos}`,
            }
          : prev
      );
      flash("Ticket asignado a ti correctamente");
    } catch (err: any) {
      flash(err?.response?.data?.message || "Error al asignarte el ticket", true);
    } finally {
      setAsignando(false);
    }
  };

  const flash = (msg: string, isError = false) => {
    setSuccessMsg(isError ? `❌ ${msg}` : `✓ ${msg}`);
    setTimeout(() => setSuccessMsg(""), 3000);
  };

  if (isLoading)
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;

  if (error)
    return (
      <div className="rounded-xl bg-red-50 border border-red-200 p-6 text-center">
        <p className="text-red-700">{error}</p>
        <Link
          to="/agent/queue"
          className="mt-4 inline-block text-sm text-primary hover:underline"
        >
          ← Volver a la cola
        </Link>
      </div>
    );

  if (!ticket)
    return (
      <div className="rounded-xl bg-zinc-50 border border-zinc-200 p-6 text-center text-zinc-500">
        Ticket no encontrado
      </div>
    );

  const slaVal = slaProgress(ticket.fechaCreacion, ticket.fechaVencimientoSla);
  const vencido = isSlaVencido(ticket.fechaVencimientoSla);

  return (
    <div className="space-y-6 animate-fade-in">
      <Link
        to="/agent/queue"
        className="inline-flex items-center gap-2 text-sm text-zinc-500 hover:text-primary transition"
      >
        <ArrowLeft className="w-4 h-4" /> Volver a la cola
      </Link>

      {successMsg && (
        <div className="rounded-lg bg-green-50 border border-green-200 px-4 py-2.5 text-sm text-green-700 font-medium">
          {successMsg}
        </div>
      )}

      <div className="flex flex-col xl:flex-row gap-6">
        {/* Columna principal */}
        <div className="flex-1 min-w-0 space-y-5">
          {/* Encabezado del ticket */}
          <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
            <div className="flex flex-wrap items-start gap-3">
              <span className="text-sm font-mono text-zinc-500">
                {ticket.codigo}
              </span>
              {ticket.categoriaNombre && (
                <Badge variant="outline">{ticket.categoriaNombre}</Badge>
              )}
              <StatusBadge status={status as TicketStatus} />
              <PriorityBadge priority={ticket.prioridad as TicketPriority} />
            </div>
            <h1 className="mt-2 text-xl font-bold text-zinc-900">
              {ticket.asunto}
            </h1>

            <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
              <InfoRow
                icon={<User className="w-4 h-4" />}
                label="Cliente"
                value={ticket.clienteNombre || "—"}
              />
              <InfoRow
                icon={<Building2 className="w-4 h-4" />}
                label="Tipo"
                value={ticket.tipo ? ticket.tipo.charAt(0).toUpperCase() + ticket.tipo.slice(1) : "—"}
              />
              <InfoRow
                icon={<Calendar className="w-4 h-4" />}
                label="Creado"
                value={formatDate(ticket.fechaCreacion)}
              />
              <InfoRow
                icon={<Calendar className="w-4 h-4" />}
                label="Vence SLA"
                value={formatDate(ticket.fechaVencimientoSla)}
              />
            </div>

            {ticket.fechaVencimientoSla && (
              <div
                className={`mt-4 p-4 rounded-lg border ${
                  vencido
                    ? "bg-red-50 border-red-100"
                    : "bg-amber-50 border-amber-100"
                }`}
              >
                <p
                  className={`text-sm font-semibold ${
                    vencido ? "text-red-700" : "text-amber-700"
                  }`}
                >
                  {vencido ? "SLA vencido" : `SLA en curso — ${slaVal}% transcurrido`}
                </p>
                <div className="mt-2">
                  <ProgressBar
                    value={slaVal}
                    variant={vencido ? "danger" : "warning"}
                  />
                </div>
              </div>
            )}

            <div className="mt-5">
              <h3 className="text-sm font-semibold text-zinc-900">
                Descripción
              </h3>
              <p className="mt-2 text-sm text-zinc-600 leading-relaxed whitespace-pre-wrap">
                {ticket.descripcion}
              </p>
            </div>
          </div>

          {/* Historial de cambios como actividad */}
          <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-zinc-900">
              Historial de actividad
            </h3>

            {historial.length === 0 ? (
              <p className="mt-4 text-sm text-zinc-500">
                Sin cambios registrados todavía.
              </p>
            ) : (
              <ul className="mt-4 space-y-3 max-h-72 overflow-y-auto">
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
                            {" "}
                            de{" "}
                            <span className="text-zinc-500 line-through">
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
            )}

            {/* Área de comentarios (sin endpoint aún — placeholder funcional) */}
            <div className="mt-4 flex flex-col sm:flex-row gap-2 border-t border-zinc-100 pt-4">
              <textarea
                placeholder="Escribe un comentario o nota interna..."
                rows={2}
                className="flex-1 px-3 py-2 text-sm rounded-lg border border-zinc-200 focus:ring-2 focus:ring-primary/20 outline-none resize-none"
              />
              <div className="flex sm:flex-col gap-2 shrink-0">
                <button
                  type="button"
                  className="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-sm rounded-lg bg-primary text-white hover:opacity-90 transition"
                >
                  <Send className="w-4 h-4" /> Enviar
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Panel lateral */}
        <aside className="w-full xl:w-80 shrink-0 space-y-4">
          {/* Cambiar estado */}
          <SidePanel title="Estado del ticket">
            <div className="flex flex-wrap gap-2">
              {(["abierto", "en_proceso", "cerrado"] as const).map((s) => (
                <button
                  key={s}
                  type="button"
                  disabled={savingStatus}
                  onClick={() => handleCambiarEstado(s)}
                  className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition disabled:opacity-60 ${
                    status === s
                      ? "bg-primary text-white border-primary"
                      : "border-zinc-200 text-zinc-600 hover:border-primary"
                  }`}
                >
                  {s === "en_proceso"
                    ? "En proceso"
                    : s.charAt(0).toUpperCase() + s.slice(1)}
                </button>
              ))}
            </div>
            {savingStatus && (
              <p className="mt-2 text-xs text-zinc-400">Guardando…</p>
            )}
            {ticket.tiempoResolucionMinutos != null && (
              <p className="mt-3 text-xs text-zinc-500">
                Tiempo de resolución:{" "}
                <strong>
                  {ticket.tiempoResolucionMinutos < 60
                    ? `${ticket.tiempoResolucionMinutos} min`
                    : `${Math.floor(ticket.tiempoResolucionMinutos / 60)}h ${ticket.tiempoResolucionMinutos % 60}min`}
                </strong>
              </p>
            )}
          </SidePanel>

          {/* Asignación */}
          <SidePanel title="Asignación">
            <div className="flex items-center gap-3">
              <Avatar
                initials={
                  ticket.agenteNombre
                    ? ticket.agenteNombre.slice(0, 2).toUpperCase()
                    : "—"
                }
              />
              <div className="min-w-0">
                <p className="text-sm font-semibold truncate">
                  {ticket.agenteNombre || "Sin asignar"}
                </p>
                <p className="text-xs text-zinc-500">Agente asignado</p>
              </div>
            </div>
            {ticket.agenteId !== user?.id && (
              <button
                type="button"
                disabled={asignando}
                onClick={handleAsignarme}
                className="mt-3 w-full py-2 text-sm font-medium rounded-lg border border-zinc-200 hover:bg-zinc-50 transition disabled:opacity-60"
              >
                {asignando ? "Asignando…" : "Asignarme este ticket"}
              </button>
            )}
            {ticket.agenteId === user?.id && (
              <p className="mt-3 text-xs text-green-600 font-medium">
                Este ticket ya está asignado a ti
              </p>
            )}
          </SidePanel>

          {/* Información adicional */}
          <SidePanel title="Información adicional">
            <dl className="space-y-2 text-sm">
              <Meta label="Cliente" value={ticket.clienteNombre || "—"} />
              <Meta label="Categoría" value={ticket.categoriaNombre || "—"} />
              <Meta
                label="Tipo"
                value={
                  ticket.tipo
                    ? ticket.tipo.charAt(0).toUpperCase() + ticket.tipo.slice(1)
                    : "—"
                }
              />
              <Meta
                label="Prioridad"
                value={
                  ticket.prioridad
                    ? ticket.prioridad.charAt(0).toUpperCase() +
                      ticket.prioridad.slice(1)
                    : "—"
                }
              />
              <Meta label="Creado" value={formatDate(ticket.fechaCreacion)} />
            </dl>
          </SidePanel>

          {/* Accesos rápidos */}
          <div className="rounded-xl bg-blue-50 border border-blue-100 p-4">
            <p className="text-xs font-semibold text-blue-800 uppercase tracking-wide flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5" /> Acciones rápidas
            </p>
            <div className="mt-3 space-y-2">
              <Link
                to="/agent/queue"
                className="block text-sm text-primary hover:underline"
              >
                ← Volver a la cola
              </Link>
              <Link
                to="/agent/mis-asignados"
                className="block text-sm text-primary hover:underline"
              >
                Ver mis asignados
              </Link>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}

function InfoRow({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center gap-2 text-zinc-600">
      <span className="text-zinc-400">{icon}</span>
      <span className="text-zinc-400">{label}:</span>
      <span className="font-medium text-zinc-800">{value}</span>
    </div>
  );
}

function SidePanel({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
      <h3 className="text-sm font-semibold text-zinc-900">{title}</h3>
      <div className="mt-3">{children}</div>
    </div>
  );
}

function Meta({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-2">
      <dt className="text-zinc-500">{label}</dt>
      <dd className="font-medium text-zinc-800 text-right">{value}</dd>
    </div>
  );
}
