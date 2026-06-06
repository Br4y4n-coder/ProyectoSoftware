import { format } from "date-fns";
import { es } from "date-fns/locale";
import { Link } from "react-router";
import {
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";
import { ArrowRight, AlertTriangle } from "lucide-react";
import { StatCard } from "../../components/common/StatCard";
import {
  adminKpis,
  agentPerformance,
  riskTickets,
  statusDistribution,
  ticketsPerDayChart,
  topCategories,
} from "../../data/mockData";
import { useMockData } from "../../hooks/useMockData";

export default function AdminDashboard() {
  const { loading } = useMockData(true);
  const today = format(new Date(2026, 3, 27), "EEEE d MMM", { locale: es });

  if (loading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <header>
        <h1 className="text-2xl sm:text-3xl font-bold text-zinc-900">
          Dashboard de administración
        </h1>
        <p className="mt-1 text-sm text-zinc-500 capitalize">
          Visión general del sistema — {today} — Última actualización: hace 2 min
        </p>
      </header>

      <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {adminKpis.map((kpi) => (
          <StatCard key={kpi.label} {...kpi} />
        ))}
      </section>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="xl:col-span-2 rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-zinc-900">
            Tickets por día — últimas 4 semanas
          </h2>
          <div className="h-72 mt-4">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={ticketsPerDayChart}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f4f4f5" />
                <XAxis dataKey="day" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="abiertos"
                  name="Abiertos"
                  stroke="#3B82F6"
                  strokeWidth={2}
                />
                <Line
                  type="monotone"
                  dataKey="resueltos"
                  name="Resueltos"
                  stroke="#10B981"
                  strokeWidth={2}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-zinc-900">Estado actual</h2>
          <div className="h-56 mt-2">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={statusDistribution}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={2}
                >
                  {statusDistribution.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(v) => `${v}%`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100">
            <h2 className="font-semibold text-zinc-900">Top categorías</h2>
            <Link to="#" className="text-sm text-primary font-medium hover:underline">
              Ver reporte completo →
            </Link>
          </div>
          <table className="w-full text-sm">
            <tbody>
              {topCategories.map((cat) => (
                <tr key={cat.name} className="border-t border-zinc-50 hover:bg-zinc-50">
                  <td className="px-5 py-3 font-medium text-zinc-800">{cat.name}</td>
                  <td className="px-5 py-3 text-right text-zinc-600">{cat.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-zinc-100">
            <h2 className="font-semibold text-zinc-900">Rendimiento de agentes</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-[11px] uppercase tracking-wider text-zinc-400 bg-zinc-50">
                  <th className="px-5 py-2 text-left">Agente</th>
                  <th className="px-5 py-2 text-right">Resueltos</th>
                  <th className="px-5 py-2 text-right">Promedio</th>
                  <th className="px-5 py-2 text-right">SLA</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {agentPerformance.map((a) => (
                  <tr key={a.name} className="hover:bg-zinc-50">
                    <td className="px-5 py-3 font-medium">{a.name}</td>
                    <td className="px-5 py-3 text-right">{a.resolved}</td>
                    <td className="px-5 py-3 text-right">{a.average}</td>
                    <td className="px-5 py-3 text-right text-emerald-600 font-medium">
                      {a.sla}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="flex items-center gap-2 px-5 py-4 border-b border-zinc-100">
          <AlertTriangle className="w-5 h-5 text-amber-500" />
          <h2 className="font-semibold text-zinc-900">Tickets en riesgo (SLA por vencer)</h2>
        </div>
        <ul className="divide-y divide-zinc-100">
          {riskTickets.map((t) => (
            <li
              key={t.id}
              className="flex flex-wrap items-center justify-between gap-3 px-5 py-3 hover:bg-zinc-50 transition"
            >
              <div>
                <Link
                  to={`/admin/assign-ticket/${t.id}`}
                  className="font-semibold text-primary hover:underline"
                >
                  {t.id}
                </Link>
                <p className="text-sm text-zinc-500">{t.description}</p>
              </div>
              <div className="text-sm text-zinc-600">
                <span>{t.assigned}</span>
                <span className="mx-2 text-zinc-300">·</span>
                <span className="text-red-600 font-medium">{t.minutesLeft} min</span>
              </div>
              <Link
                to={`/admin/assign-ticket/${t.id}`}
                className="text-sm text-primary flex items-center gap-1 hover:underline"
              >
                Asignar <ArrowRight className="w-4 h-4" />
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
