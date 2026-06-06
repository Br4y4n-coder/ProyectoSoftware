import { useEffect, useState } from "react";
import { Bell, CheckCheck, Inbox } from "lucide-react";
import { useNavigate } from "react-router";
import apiFetch from "../../api/apiFetch";
import { useAuth } from "../../contexts/AuthContext";

const STORAGE_KEY = "notifs_vistas_v1";

function leerVistas() {
  try {
    return JSON.parse(window.localStorage.getItem(STORAGE_KEY)) || {};
  } catch {
    return {};
  }
}

function guardarVistas(vistas) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(vistas));
  } catch {
    /* sin localStorage no hay persistencia, pero no rompemos nada */
  }
}

/** Última actividad conocida del ticket (cierre > inicio atención > creación). */
function ultimaActividad(t) {
  return t.fechaCierre || t.fechaInicioAtencion || t.fechaCreacion || null;
}

function tituloPorEstado(estado) {
  const map = {
    abierto: "Ticket abierto",
    en_proceso: "Ticket en proceso",
    en_espera: "Ticket en espera",
    resuelto: "Ticket resuelto",
    cerrado: "Ticket cerrado",
  };
  return map[estado] || "Ticket actualizado";
}

function formatRelativo(iso) {
  if (!iso) return "";
  const fecha = new Date(iso);
  if (isNaN(fecha.getTime())) return "";
  const diffMin = Math.floor((Date.now() - fecha.getTime()) / 60000);
  if (diffMin < 1) return "Justo ahora";
  if (diffMin < 60) return `Hace ${diffMin} min`;
  const horas = Math.floor(diffMin / 60);
  if (horas < 24) return `Hace ${horas} h`;
  const dias = Math.floor(horas / 24);
  return `Hace ${dias} día${dias === 1 ? "" : "s"}`;
}

/**
 * Campana de notificaciones: despliega un panel anclado (no navega).
 * Las notificaciones se derivan de los tickets relevantes según el rol,
 * y lo "leído" se persiste en localStorage.
 */
export default function NotificationsBell() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [abierto, setAbierto] = useState(false);
  const [items, setItems] = useState([]);
  const [vistas, setVistas] = useState(leerVistas);
  const [cargando, setCargando] = useState(false);
  const [refreshTick, setRefreshTick] = useState(0);

  useEffect(() => {
    if (!user?.id) return;
    let cancelled = false;
    setCargando(true);

    const url =
      user.rol === "agente"
        ? `/api/tickets?agenteId=${user.id}&page=0&size=10`
        : user.rol === "administrador"
          ? `/api/tickets?page=0&size=10`
          : `/api/tickets/mios?page=0&size=10`;

    (async () => {
      try {
        const response = await apiFetch(url);
        if (cancelled || !response.ok) return;
        const data = await response.json();
        const tickets = data?.data?.content || [];

        const notifs = tickets
          .map((t) => ({
            id: t.id,
            codigo: t.codigo,
            titulo: tituloPorEstado(t.estado),
            mensaje:
              user.rol === "administrador"
                ? `#${t.codigo} · ${t.asunto} — ${t.clienteNombre || "Sin cliente"}`
                : `#${t.codigo} · ${t.asunto}`,
            fecha: ultimaActividad(t),
          }))
          .filter((n) => n.fecha)
          .sort((a, b) => new Date(b.fecha) - new Date(a.fecha));

        if (!cancelled) setItems(notifs);
      } catch {
        /* silencioso: la campana no debe romper la app */
      } finally {
        if (!cancelled) setCargando(false);
      }
    })();

    return () => { cancelled = true; };
  }, [user?.id, user?.rol, refreshTick]);

  const noLeidas = items.filter(
    (n) => !vistas[n.id] || new Date(n.fecha) > new Date(vistas[n.id])
  );

  const marcarTodas = () => {
    const nuevas = { ...vistas };
    items.forEach((n) => {
      nuevas[n.id] = n.fecha;
    });
    setVistas(nuevas);
    guardarVistas(nuevas);
  };

  const abrirPanel = () => {
    const siguiente = !abierto;
    setAbierto(siguiente);
    if (siguiente) setRefreshTick((t) => t + 1);
  };

  const irATicket = (notif) => {
    const nuevas = { ...vistas, [notif.id]: notif.fecha };
    setVistas(nuevas);
    guardarVistas(nuevas);
    setAbierto(false);
    if (user?.rol === "agente") navigate(`/agent/tickets/${notif.id}`);
    else if (user?.rol === "administrador") navigate(`/admin/tickets`);
    else navigate(`/mis-tickets`);
  };

  return (
    <div className="relative shrink-0">
      <button
        type="button"
        onClick={abrirPanel}
        className="relative w-9 h-9 rounded-full bg-zinc-100 flex items-center justify-center hover:bg-zinc-200 transition"
        aria-label={`Notificaciones${noLeidas.length > 0 ? ` (${noLeidas.length} sin leer)` : ""}`}
        aria-expanded={abierto}
      >
        <Bell className="w-4 h-4 text-zinc-600" />
        {noLeidas.length > 0 && (
          <span className="absolute top-0.5 right-0.5 w-2.5 h-2.5 rounded-full bg-red-500 ring-2 ring-white" />
        )}
      </button>

      {abierto && (
        <>
          {/* Cierra al hacer clic fuera */}
          <button
            type="button"
            aria-label="Cerrar notificaciones"
            onClick={() => setAbierto(false)}
            className="fixed inset-0 z-40 cursor-default"
          />

          <div className="absolute right-0 top-11 z-50 w-80 sm:w-96 rounded-xl bg-white border border-zinc-200 shadow-xl overflow-hidden">
            <div className="flex items-center justify-between px-4 py-3 border-b border-zinc-100">
              <p className="text-sm font-bold text-zinc-900">
                Notificaciones
                {noLeidas.length > 0 && (
                  <span className="ml-2 px-1.5 py-0.5 rounded-full bg-red-100 text-red-600 text-[10px] font-bold">
                    {noLeidas.length}
                  </span>
                )}
              </p>
              {noLeidas.length > 0 && (
                <button
                  type="button"
                  onClick={marcarTodas}
                  className="inline-flex items-center gap-1 text-xs text-primary font-medium hover:underline"
                >
                  <CheckCheck className="w-3.5 h-3.5" /> Marcar leídas
                </button>
              )}
            </div>

            <div className="max-h-80 overflow-y-auto">
              {cargando && items.length === 0 ? (
                <div className="p-4 space-y-3 animate-pulse">
                  {[0, 1, 2].map((i) => (
                    <div key={i} className="h-12 bg-zinc-100 rounded-lg" />
                  ))}
                </div>
              ) : items.length === 0 ? (
                <div className="p-8 text-center text-zinc-400">
                  <Inbox className="w-8 h-8 mx-auto mb-2 text-zinc-300" />
                  <p className="text-sm">No tienes notificaciones</p>
                </div>
              ) : (
                <ul className="divide-y divide-zinc-50">
                  {items.map((notif) => {
                    const sinLeer =
                      !vistas[notif.id] ||
                      new Date(notif.fecha) > new Date(vistas[notif.id]);
                    return (
                      <li key={`${notif.id}-${notif.fecha}`}>
                        <button
                          type="button"
                          onClick={() => irATicket(notif)}
                          className={`w-full text-left px-4 py-3 hover:bg-zinc-50 transition ${
                            sinLeer ? "bg-primary-faint/40" : ""
                          }`}
                        >
                          <div className="flex items-center gap-2">
                            <p className="text-sm font-semibold text-zinc-900 flex-1 truncate">
                              {notif.titulo}
                            </p>
                            {sinLeer && (
                              <span className="w-2 h-2 rounded-full bg-primary shrink-0" />
                            )}
                          </div>
                          <p className="text-xs text-zinc-500 truncate mt-0.5">
                            {notif.mensaje}
                          </p>
                          <p className="text-[11px] text-zinc-400 mt-0.5">
                            {formatRelativo(notif.fecha)}
                          </p>
                        </button>
                      </li>
                    );
                  })}
                </ul>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
