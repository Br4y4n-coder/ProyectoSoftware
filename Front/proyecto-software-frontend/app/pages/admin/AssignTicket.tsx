import { useMemo, useState } from "react";
import { Link, useParams } from "react-router";
import { ArrowLeft, Search, Sparkles, UserCheck } from "lucide-react";
import { Avatar } from "../../components/common/Avatar";
import { Badge } from "../../components/common/Badge";
import { PriorityBadge } from "../../components/common/ticketHelpers";
import { assignableAgents, assignTicketPreview } from "../../data/mockData";
import { useMockData } from "../../hooks/useMockData";
import type { AgentAvailability, AssignableAgent } from "../../types";

export default function AssignTicket() {
  const { ticketId } = useParams();
  const ticket = { ...assignTicketPreview, id: ticketId ?? assignTicketPreview.id };
  const { loading } = useMockData(true);
  const [search, setSearch] = useState("");
  const [selectedId, setSelectedId] = useState(
    assignableAgents.find((a) => a.recommended)?.id ?? ""
  );

  const grouped = useMemo(() => {
    const q = search.toLowerCase();
    const filtered = assignableAgents.filter(
      (a) =>
        a.name.toLowerCase().includes(q) ||
        a.area.toLowerCase().includes(q) ||
        a.level.toLowerCase().includes(q)
    );
    const groups: Record<string, AssignableAgent[]> = {};
    for (const agent of filtered) {
      const key = `Área: ${agent.area}`;
      if (!groups[key]) groups[key] = [];
      groups[key].push(agent);
    }
    return groups;
  }, [search]);

  if (loading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  return (
    <div className="space-y-6 animate-fade-in max-w-3xl">
      <Link
        to="/admin/dashboard"
        className="inline-flex items-center gap-2 text-sm text-zinc-500 hover:text-primary transition"
      >
        <ArrowLeft className="w-4 h-4" /> Volver al dashboard
      </Link>

      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Asignar ticket</h1>
      </header>

      <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-mono font-semibold text-primary">{ticket.id}</span>
          <PriorityBadge priority={ticket.priority} />
          <Badge variant="outline">{ticket.category}</Badge>
        </div>
        <h2 className="mt-2 text-lg font-semibold text-zinc-900">{ticket.subject}</h2>
        <p className="mt-2 text-sm text-zinc-500">
          Cliente: <strong className="text-zinc-700">{ticket.client}</strong> — SLA restante:{" "}
          <strong className="text-red-600">{ticket.slaRemaining}</strong>
        </p>
      </div>

      <div className="flex items-center gap-2 h-10 px-3 rounded-lg bg-white border border-zinc-200">
        <Search className="w-4 h-4 text-zinc-400" />
        <input
          type="search"
          placeholder="Buscar agente (nombre, área o nivel...)"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="flex-1 text-sm outline-none bg-transparent"
        />
      </div>

      <div className="space-y-6">
        {Object.entries(grouped).map(([area, agents]) => (
          <section key={area}>
            <h3 className="text-xs font-semibold uppercase tracking-wider text-zinc-400 mb-3">
              {area}
            </h3>
            <ul className="space-y-3">
              {agents.map((agent) => (
                <AgentCard
                  key={agent.id}
                  agent={agent}
                  selected={selectedId === agent.id}
                  onSelect={() => setSelectedId(agent.id)}
                />
              ))}
            </ul>
          </section>
        ))}
      </div>

      <div className="flex flex-wrap gap-3 pt-2">
        <button
          type="button"
          onClick={() => {
            const rec = assignableAgents.find((a) => a.recommended);
            if (rec) setSelectedId(rec.id);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium rounded-lg border border-zinc-200 bg-white hover:bg-zinc-50 transition"
        >
          <Sparkles className="w-4 h-4" /> Recomendar automáticamente
        </button>
        <button
          type="button"
          disabled={!selectedId}
          className="inline-flex items-center gap-2 px-6 py-2.5 text-sm font-medium rounded-lg bg-primary text-white hover:opacity-90 transition disabled:opacity-50"
        >
          <UserCheck className="w-4 h-4" /> Asignar
        </button>
      </div>
    </div>
  );
}

function AgentCard({
  agent,
  selected,
  onSelect,
}: {
  agent: AssignableAgent;
  selected: boolean;
  onSelect: () => void;
}) {
  const initials = agent.name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2);

  return (
    <li>
      <button
        type="button"
        onClick={onSelect}
        className={`w-full text-left rounded-xl border p-4 transition hover:shadow-md ${
          selected
            ? "border-primary bg-primary-faint ring-2 ring-primary/20"
            : agent.recommended
              ? "border-primary/40 bg-white"
              : "border-zinc-200 bg-white"
        }`}
      >
        <div className="flex items-start gap-3">
          <Avatar initials={initials} />
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-semibold text-zinc-900">{agent.name}</span>
              {agent.recommended && (
                <Badge variant="info">Recomendada</Badge>
              )}
              {agent.badge && (
                <Badge variant="purple">{agent.badge}</Badge>
              )}
            </div>
            <p className="text-sm text-zinc-500 mt-0.5">{agent.level}</p>
            <p className="text-xs text-zinc-400 mt-1">
              {agent.activeTickets} tickets activos
            </p>
          </div>
          <AvailabilityBadge availability={agent.availability} />
        </div>
      </button>
    </li>
  );
}

function AvailabilityBadge({ availability }: { availability: AgentAvailability }) {
  const map: Record<AgentAvailability, { label: string; variant: "success" | "warning" | "danger" }> = {
    disponible: { label: "Disponible", variant: "success" },
    ocupado: { label: "OCUPADO", variant: "warning" },
    saturado: { label: "SATURADO", variant: "danger" },
  };
  const { label, variant } = map[availability];
  return <Badge variant={variant}>{label}</Badge>;
}
