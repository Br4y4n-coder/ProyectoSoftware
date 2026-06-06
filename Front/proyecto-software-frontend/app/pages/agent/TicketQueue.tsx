import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router";
import { Search, X } from "lucide-react";
import { Avatar } from "../../components/common/Avatar";
import { Pagination } from "../../components/common/Pagination";
import {
  PriorityBadge,
  StatusBadge,
} from "../../components/common/ticketHelpers";

type QueueTab = "general" | "unassigned" | "mine" | "critical";

interface TicketItem {
  id: number;
  codigo: string;
  asunto: string;
  descripcion: string;
  tipo: string;
  prioridad: string;
  estado: string;
  clienteId: number;
  clienteNombre: string;
  agenteId: number | null;
  agenteNombre: string | null;
  fechaCreacion: string;
}

export default function TicketQueue() {
  const [tickets, setTickets] = useState<TicketItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [tab, setTab] = useState<QueueTab>("general");
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [filters, setFilters] = useState<string[]>(["Estado: Abierto"]);

  const fetchTickets = async () => {
    const token = localStorage.getItem('auth_token');
    
    if (!token) {
      setError("No hay sesión activa");
      setIsLoading(false);
      return;
    }
    
    try {
      const response = await fetch(`http://localhost:8080/api/tickets?page=0&size=100`, {
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setTickets(data?.data?.content || []);
      } else {
        setError("Error al cargar tickets");
      }
    } catch (error) {
      setError("Error de conexión");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const filtered = useMemo(() => {
    let list = [...tickets];
    
    if (tab === "unassigned") {
      list = list.filter((t) => t.agenteId === null);
    } else if (tab === "critical") {
      list = list.filter((t) => t.prioridad === "alta");
    }
    
    if (search.trim()) {
      const q = search.toLowerCase();
      list = list.filter(
        (t) =>
          t.codigo?.toLowerCase().includes(q) ||
          t.asunto?.toLowerCase().includes(q) ||
          t.clienteNombre?.toLowerCase().includes(q)
      );
    }
    return list;
  }, [search, tab, tickets]);

  const toggleSelect = (id: number) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleAll = () => {
    if (selected.size === filtered.length) setSelected(new Set());
    else setSelected(new Set(filtered.map((t) => t.id)));
  };

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

  const tabs: { id: QueueTab; label: string; count?: number }[] = [
    { id: "general", label: "Cola general" },
    { id: "unassigned", label: "Sin asignar", count: tickets.filter(t => t.agenteId === null).length },
    { id: "critical", label: "Prioridad alta", count: tickets.filter(t => t.prioridad === "alta").length },
  ];

  return (
    <div className="space-y-5 animate-fade-in">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Cola de tickets</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Gestiona y asigna tickets de la cola general
        </p>
      </header>

      <div className="flex flex-col lg:flex-row gap-3">
        <div className="flex-1 flex items-center gap-2 h-10 px-3 rounded-lg bg-white border border-zinc-200 focus-within:ring-2 focus-within:ring-primary/20 transition">
          <Search className="w-4 h-4 text-zinc-400 shrink-0" />
          <input
            type="search"
            placeholder="Buscar por ID, asunto o cliente..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 text-sm outline-none bg-transparent"
          />
        </div>
      </div>

      {filters.length > 0 && (
        <div className="flex flex-wrap items-center gap-2">
          {filters.map((f) => (
            <span
              key={f}
              className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-medium"
            >
              {f}
              <button
                type="button"
                onClick={() => setFilters((prev) => prev.filter((x) => x !== f))}
                aria-label={`Quitar filtro ${f}`}
              >
                <X className="w-3 h-3" />
              </button>
            </span>
          ))}
          <button
            type="button"
            onClick={() => setFilters([])}
            className="text-xs text-zinc-500 hover:text-zinc-800 font-medium"
          >
            Limpiar todo
          </button>
        </div>
      )}

      <div className="flex flex-wrap gap-2 border-b border-zinc-200 pb-px">
        {tabs.map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => setTab(t.id)}
            className={`px-4 py-2 text-sm font-medium rounded-t-lg transition border-b-2 -mb-px ${
              tab === t.id
                ? "border-primary text-primary bg-primary-faint/50"
                : "border-transparent text-zinc-600 hover:text-zinc-900"
            }`}
          >
            {t.label}
            {t.count !== undefined && (
              <span className="ml-1.5 text-xs opacity-70">({t.count})</span>
            )}
          </button>
        ))}
      </div>

      {selected.size > 0 && (
        <div className="flex flex-wrap items-center gap-3 px-4 py-3 rounded-lg bg-primary-faint border border-primary-subtle text-sm">
          <span className="font-medium text-primary">
            {selected.size} tickets seleccionados
          </span>
          <button type="button" className="text-primary hover:underline font-medium">
            Asignar a...
          </button>
        </div>
      )}

      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-zinc-400 bg-zinc-50">
                <th className="px-4 py-3 w-10">
                  <input
                    type="checkbox"
                    checked={selected.size === filtered.length && filtered.length > 0}
                    onChange={toggleAll}
                    aria-label="Seleccionar todos"
                  />
                </th>
                <th className="px-4 py-3">Ticket</th>
                <th className="px-4 py-3">Cliente</th>
                <th className="px-4 py-3">Prioridad</th>
                <th className="px-4 py-3">Asignado</th>
                <th className="px-4 py-3">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {filtered.map((ticket) => (
                <tr key={ticket.id} className="hover:bg-zinc-50 transition-colors">
                  <td className="px-4 py-3">
                    <input
                      type="checkbox"
                      checked={selected.has(ticket.id)}
                      onChange={() => toggleSelect(ticket.id)}
                      aria-label={`Seleccionar ${ticket.codigo}`}
                    />
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      to={`/agent/tickets/${ticket.id}`}
                      className="font-semibold text-primary hover:underline"
                    >
                      {ticket.codigo}
                    </Link>
                    <p className="text-xs text-zinc-500 truncate max-w-[180px]">{ticket.asunto}</p>
                  </td>
                  <td className="px-4 py-3 text-zinc-700">{ticket.clienteNombre}</td>
                  <td className="px-4 py-3">
                    <PriorityBadge priority={ticket.prioridad} />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      {ticket.agenteId ? (
                        <Avatar initials={ticket.agenteNombre?.slice(0,2).toUpperCase() ?? "A"} size="sm" />
                      ) : (
                        <span className="w-7 h-7 rounded-full bg-zinc-100 flex items-center justify-center text-xs text-zinc-400">
                          ?
                        </span>
                      )}
                      <span className="text-zinc-600 text-xs">{ticket.agenteNombre || "Sin asignar"}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={ticket.estado} />
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-zinc-500">
                    No hay tickets disponibles
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pagination from={1} to={filtered.length} total={filtered.length} />
      </div>
    </div>
  );
}