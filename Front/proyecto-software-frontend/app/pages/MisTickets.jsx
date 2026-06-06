import React, { useState, useEffect } from "react";
import { Link } from "react-router";
import { ticketsService } from "../services";

export default function MisTickets() {
  const [tickets, setTickets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await ticketsService.mios({
          page: 0,
          size: 100,
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

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando tickets...</div>
      </div>
    );
  }

  if (errorMsg) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {errorMsg}
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-zinc-900">Mis Tickets</h1>
        <Link
          to="/tickets/nuevo"
          className="h-11 px-5 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition flex items-center"
        >
          + Crear ticket
        </Link>
      </div>

      {tickets.length === 0 ? (
        <div className="rounded-xl border border-yellow-200 bg-yellow-50 px-6 py-12 text-center">
          <p className="text-yellow-800 mb-4">No tienes tickets creados.</p>
          <Link
            to="/tickets/nuevo"
            className="inline-flex h-11 px-5 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition items-center"
          >
            Crear mi primer ticket
          </Link>
        </div>
      ) : (
        <div className="rounded-xl bg-white border border-zinc-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-[#F9FAFB]">
                <tr className="text-left text-[11px] font-semibold tracking-wider text-zinc-500 uppercase">
                  <th className="px-6 py-3">ID</th>
                  <th className="px-6 py-3">Asunto</th>
                  <th className="px-6 py-3">Descripción</th>
                  <th className="px-6 py-3">Tipo</th>
                  <th className="px-6 py-3">Estado</th>
                  <th className="px-6 py-3">Prioridad</th>
                  <th className="px-6 py-3">Fecha</th>
                  <th className="px-6 py-3">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {tickets.map((ticket) => (
                  <tr key={ticket.id} className="hover:bg-zinc-50/50">
                    <td className="px-6 py-3.5 font-semibold text-zinc-900 whitespace-nowrap">
                      #{ticket.codigo || `TKT-${ticket.id}`}
                    </td>
                    <td className="px-6 py-3.5 text-zinc-900 max-w-xs truncate">
                      {ticket.asunto}
                    </td>
                    <td className="px-6 py-3.5 text-zinc-500 max-w-md truncate">
                      {ticket.descripcion}
                    </td>
                    <td className="px-6 py-3.5">
                      <span className="px-2 py-1 rounded-full text-[10px] font-bold bg-gray-100 text-gray-700 capitalize">
                        {ticket.tipo}
                      </span>
                    </td>
                    <td className="px-6 py-3.5">
                      <EstadoBadge estado={ticket.estado} />
                    </td>
                    <td className="px-6 py-3.5 whitespace-nowrap">
                      <PrioridadDot prioridad={ticket.prioridad} />
                      <span className="ml-2 text-zinc-700 capitalize">
                        {ticket.prioridad}
                      </span>
                    </td>
                    <td className="px-6 py-3.5 text-zinc-500 whitespace-nowrap">
                      {formatDate(ticket.fechaCreacion)}
                    </td>
                    <td className="px-6 py-3.5 whitespace-nowrap">
                      <Link
                        to={`/ticket/${ticket.id}`}
                        className="text-primary hover:text-primary-light font-medium"
                      >
                        Ver detalle
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
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
      baja: "bg-green-500",
    }[prioridad] || "bg-zinc-400";
  return (
    <span
      className={`inline-block w-2 h-2 rounded-full align-middle ${color}`}
    />
  );
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