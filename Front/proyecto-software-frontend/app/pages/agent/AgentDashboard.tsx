import { Link } from "react-router";
import {
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";
import {
  Activity,
  AlertCircle,
  ArrowRight,
  MessageSquare,
  Ticket,
} from "lucide-react";
import { AlertBanner } from "../../components/common/AlertBanner";
import { StatCard } from "../../components/common/StatCard";
import {
  PriorityBadge,
  SlaBadge,
  StatusBadge,
} from "../../components/common/ticketHelpers";
import {
  agentAssignedTickets,
  agentKpis,
  agentPerformanceChart,
  CURRENT_AGENT,
  recentActivity,
} from "../../data/mockData";
import { useMockData } from "../../hooks/useMockData";

const activityIcons = {
  ticket: Ticket,
  comment: MessageSquare,
  status: Activity,
  sla: AlertCircle,
};

export default function AgentDashboard() {
  const { loading } = useMockData(true);

  if (loading) {
    return <DashboardSkeleton />;
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <header>
        <h1 className="text-2xl sm:text-3xl font-bold text-zinc-900">
          Buen día, {CURRENT_AGENT.name} 🎉
        </h1>
        <p className="mt-1 text-zinc-500">
          Tienes <strong className="text-zinc-800">7 tickets asignados</strong> —{" "}
          <strong className="text-red-600">2 con SLA crítico</strong>
        </p>
      </header>

      <AlertBanner>2 tickets críticos requieren tu atención inmediata</AlertBanner>

      <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {agentKpis.map((kpi) => (
          <StatCard key={kpi.label} {...kpi} />
        ))}
      </section>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="xl:col-span-2 rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-zinc-900">
            Tu rendimiento — últimos 7 días
          </h2>
          <div className="h-64 mt-4">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={agentPerformanceChart}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f4f4f5" />
                <XAxis dataKey="day" tick={{ fontSize: 12 }} stroke="#a1a1aa" />
                <YAxis tick={{ fontSize: 12 }} stroke="#a1a1aa" />
                <Tooltip />
                <Line
                  type="monotone"
                  dataKey="resolved"
                  name="Resueltos"
                  stroke="#4F46E5"
                  strokeWidth={2}
                  dot={{ r: 4 }}
                />
                <Line
                  type="monotone"
                  dataKey="assigned"
                  name="Asignados"
                  stroke="#10B981"
                  strokeWidth={2}
                  strokeDasharray="4 4"
                  dot={{ r: 3 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-zinc-900">Actividad reciente</h2>
          <ul className="mt-4 space-y-4">
            {recentActivity.map((item) => {
              const Icon = activityIcons[item.icon];
              return (
                <li key={item.id} className="flex gap-3">
                  <div className="w-8 h-8 rounded-lg bg-zinc-100 flex items-center justify-center shrink-0">
                    <Icon className="w-4 h-4 text-zinc-600" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm text-zinc-700 leading-snug">{item.text}</p>
                    <p className="text-xs text-zinc-400 mt-0.5">{item.time}</p>
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      </div>

      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100">
          <h2 className="font-semibold text-zinc-900">Mis tickets asignados</h2>
          <Link
            to="/agent/queue"
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
                <th className="px-5 py-3">SLA</th>
                <th className="px-5 py-3">Prioridad</th>
                <th className="px-5 py-3">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {agentAssignedTickets.map((ticket) => (
                <tr
                  key={ticket.id}
                  className="hover:bg-zinc-50 transition-colors"
                >
                  <td className="px-5 py-3">
                    <Link
                      to={`/agent/tickets/${ticket.id}`}
                      className="font-semibold text-primary hover:underline"
                    >
                      {ticket.id}
                    </Link>
                    <p className="text-zinc-500 text-xs mt-0.5 truncate max-w-[200px]">
                      {ticket.subject}
                    </p>
                  </td>
                  <td className="px-5 py-3 text-zinc-700">{ticket.client}</td>
                  <td className="px-5 py-3">
                    <SlaBadge sla={ticket.sla} label={ticket.slaLabel} />
                  </td>
                  <td className="px-5 py-3">
                    <PriorityBadge priority={ticket.priority} />
                  </td>
                  <td className="px-5 py-3">
                    <StatusBadge status={ticket.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function DashboardSkeleton() {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="h-10 bg-zinc-200 rounded-lg w-2/3" />
      <div className="h-12 bg-red-100 rounded-lg" />
      <div className="grid grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-28 bg-zinc-200 rounded-xl" />
        ))}
      </div>
    </div>
  );
}
