import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router";
import { AlertTriangle, ArrowRight, CheckCircle, Clock, Inbox } from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import ticketsService from "../../services/ticketsService";
import {
  PriorityBadge,
  StatusBadge,
} from "../../components/common/ticketHelpers";

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "—";
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}/${d.getFullYear()}`;
}

export default function AgentDashboard() {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

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
        if (!cancelled) {
          setError(err?.response?.data?.message || err?.message || "No se pudieron cargar los tickets");
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [user?.id]);

  const kpis = useMemo(
    () => ({
      asignados: tickets.length,
      abiertos: tickets.filter((t) => t.estado === "abierto").length,
      enProceso: tickets.filter((t) => t.estado === "en_proceso").length,
      cerrados: tickets.filter((t) => t.estado === "cerrado").length,
    }),
    [tickets]
  );

  const recientes = useMemo(() => tickets.slice(0, 6), [tickets]);
  const nCriticos = useMemo(
    () =>
      tickets.filter((t) => t.prioridad === "alta" && t.estado !== "cerrado")
        .length,
    [tickets]
  );

  if (isLoading) return <DashboardSkeleton />;

  return (
    <div className="space-y-6 animate-fade-in">
      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {nCriticos > 0 && (
        <div className="rounded-xl bg-red-50 border border-red-200 px-4 py-3 text-sm font-medium text-red-700">
          {nCriticos} ticket{nCriticos !== 1 ? "s" : ""} con prioridad alta
          requieren tu atención inmediata
        </div>
      )}

      <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <KpiCard
          label="Total asignados"
          value={kpis.asignados}
          icon={<Inbox className="w-5 h-5" />}
          bg="bg-indigo-50"
          color="text-indigo-600"
        />
        <KpiCard
          label="Abiertos"
          value={kpis.abiertos}
          icon={<AlertTriangle className="w-5 h-5" />}
          bg="bg-blue-50"
          color="text-blue-600"
        />
        <KpiCard
          label="En proceso"
          value={kpis.enProceso}
          icon={<Clock className="w-5 h-5" />}
          bg="bg-amber-50"
          color="text-amber-600"
        />
        <KpiCard
          label="Cerrados"
          value={kpis.cerrados}
          icon={<CheckCircle className="w-5 h-5" />}
          bg="bg-green-50"
          color="text-green-600"
        />
      </section>

      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100">
          <h2 className="font-semibold text-zinc-900">Mis tickets asignados</h2>
          <Link
            to="/agent/mis-asignados"
            className="text-sm text-primary font-medium flex items-center gap-1 hover:underline"
          >
            Ver todos <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-zinc-400 bg-zinc-50">
                <th className="px-5 py-3">Ticket</th>
                <th className="px-5 py-3">Cliente</th>
                <th className="px-5 py-3">Prioridad</th>
                <th className="px-5 py-3">Estado</th>
                <th className="px-5 py-3">Fecha</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {recientes.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className="px-5 py-10 text-center text-zinc-500"
                  >
                    No tienes tickets asignados todavía.{" "}
                    <Link
                      to="/agent/queue"
                      className="text-primary hover:underline"
                    >
                      Ver la cola general
                    </Link>
                  </td>
                </tr>
              ) : (
                recientes.map((t) => (
                  <tr key={t.id} className="hover:bg-zinc-50 transition-colors">
                    <td className="px-5 py-3">
                      <Link
                        to={`/agent/tickets/${t.id}`}
                        className="font-semibold text-primary hover:underline"
                      >
                        {t.codigo || `TKT-${t.id}`}
                      </Link>
                      <p className="text-xs text-zinc-500 mt-0.5 truncate max-w-[200px]">
                        {t.asunto}
                      </p>
                    </td>
                    <td className="px-5 py-3 text-zinc-700">
                      {t.clienteNombre}
                    </td>
                    <td className="px-5 py-3">
                      <PriorityBadge priority={t.prioridad} />
                    </td>
                    <td className="px-5 py-3">
                      <StatusBadge status={t.estado} />
                    </td>
                    <td className="px-5 py-3 text-zinc-500 text-xs">
                      {formatDate(t.fechaCreacion)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function KpiCard({ label, value, icon, bg, color }) {
  return (
    <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <p className="text-sm text-zinc-500">{label}</p>
        <div
          className={`w-10 h-10 rounded-lg ${bg} ${color} flex items-center justify-center`}
        >
          {icon}
        </div>
      </div>
      <p className="mt-3 text-3xl font-bold text-zinc-900">{value}</p>
    </div>
  );
}

function DashboardSkeleton() {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="h-10 bg-zinc-200 rounded-lg w-2/3" />
      <div className="grid grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-28 bg-zinc-200 rounded-xl" />
        ))}
      </div>
      <div className="h-64 bg-zinc-200 rounded-xl" />
    </div>
  );
}
