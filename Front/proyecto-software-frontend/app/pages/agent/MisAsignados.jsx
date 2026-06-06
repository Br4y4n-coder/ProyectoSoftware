import { useEffect, useState } from "react";
import { Link } from "react-router";
import { Search } from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import ticketsService from "../../services/ticketsService";
import { PriorityBadge, StatusBadge } from "../../components/common/ticketHelpers";

export default function MisAsignados() {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  useEffect(() => {
    if (!user?.id) return;
    let cancelled = false;

    (async () => {
      try {
        const { data } = await ticketsService.listar({
          agenteId: user.id,
          page: 0,
          size: 100,
          sort: "fechaCreacion,desc",
        });
        if (!cancelled) setTickets(data?.data?.content || []);
      } catch (err) {
        if (!cancelled)
          setError(err?.response?.data?.message || err?.message || "Error al cargar tickets");
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, [user?.id]);

  const filteredTickets = tickets.filter(
    (ticket) =>
      ticket.codigo?.toLowerCase().includes(search.toLowerCase()) ||
      ticket.asunto?.toLowerCase().includes(search.toLowerCase()) ||
      ticket.clienteNombre?.toLowerCase().includes(search.toLowerCase())
  );

  if (isLoading) {
    return <div className="h-64 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  if (error) {
    return (
      <div className="rounded-xl bg-red-50 border border-red-200 p-6 text-center">
        <p className="text-red-700">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-primary text-white rounded-lg"
        >
          Reintentar
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Mis Tickets Asignados</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Tickets asignados a tu cuenta para atención
        </p>
      </header>

      <div className="flex items-center gap-2 h-10 px-3 rounded-lg bg-white border border-zinc-200 focus-within:ring-2 focus-within:ring-primary/20 transition">
        <Search className="w-4 h-4 text-zinc-400 shrink-0" />
        <input
          type="search"
          placeholder="Buscar por código, asunto o cliente..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="flex-1 text-sm outline-none bg-transparent"
        />
      </div>

      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-zinc-400 bg-zinc-50">
                <th className="px-4 py-3">Ticket</th>
                <th className="px-4 py-3">Cliente</th>
                <th className="px-4 py-3">Prioridad</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3">Fecha creación</th>
                <th className="px-4 py-3">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {filteredTickets.map((ticket) => (
                <tr key={ticket.id} className="hover:bg-zinc-50 transition-colors">
                  <td className="px-4 py-3">
                    <Link
                      to={`/agent/tickets/${ticket.id}`}
                      className="font-semibold text-primary hover:underline"
                    >
                      {ticket.codigo || `TKT-${ticket.id}`}
                    </Link>
                    <p className="text-xs text-zinc-500 truncate max-w-[200px]">{ticket.asunto}</p>
                  </td>
                  <td className="px-4 py-3 text-zinc-700">{ticket.clienteNombre}</td>
                  <td className="px-4 py-3">
                    <PriorityBadge priority={ticket.prioridad} />
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={ticket.estado} />
                  </td>
                  <td className="px-4 py-3 text-zinc-500">
                    {ticket.fechaCreacion
                      ? new Date(ticket.fechaCreacion).toLocaleDateString("es-CO")
                      : "—"}
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      to={`/agent/tickets/${ticket.id}`}
                      className="text-primary hover:underline text-sm font-medium"
                    >
                      Ver detalle
                    </Link>
                  </td>
                </tr>
              ))}
              {filteredTickets.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-10 text-center text-zinc-500">
                    {search
                      ? "No hay tickets que coincidan con la búsqueda"
                      : "No tienes tickets asignados"}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
