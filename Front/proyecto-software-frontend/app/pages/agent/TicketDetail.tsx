import { useState, type ReactNode } from "react";
import { Link, useParams } from "react-router";
import {
  ArrowLeft,
  Building2,
  Calendar,
  FileImage,
  FileText,
  Paperclip,
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
import { defaultTicketDetail, ticketDetails } from "../../data/mockData";
import { useMockData } from "../../hooks/useMockData";
import type { TicketEvidence, TicketMessage } from "../../types";

export default function TicketDetail() {
  const { id } = useParams();
  const ticket = ticketDetails[id ?? ""] ?? defaultTicketDetail;
  const { loading } = useMockData(ticket);
  const [comment, setComment] = useState("");
  const [status, setStatus] = useState(ticket.status);

  if (loading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <Link
        to="/agent/queue"
        className="inline-flex items-center gap-2 text-sm text-zinc-500 hover:text-primary transition"
      >
        <ArrowLeft className="w-4 h-4" /> Volver a la cola
      </Link>

      <div className="flex flex-col xl:flex-row gap-6">
        <div className="flex-1 min-w-0 space-y-5">
          <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
            <div className="flex flex-wrap items-start gap-3">
              <span className="text-sm font-mono text-zinc-500">{ticket.id}</span>
              <Badge variant="outline">{ticket.category}</Badge>
              <StatusBadge status={status} />
              <PriorityBadge priority={ticket.priority} />
            </div>
            <h1 className="mt-2 text-xl font-bold text-zinc-900">{ticket.subject}</h1>

            <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
              <InfoRow icon={<User className="w-4 h-4" />} label="Cliente" value={ticket.client} />
              <InfoRow icon={<Building2 className="w-4 h-4" />} label="Empresa" value={ticket.company || "—"} />
              <InfoRow icon={<Calendar className="w-4 h-4" />} label="Creado" value={ticket.createdAt || "—"} />
              <InfoRow icon={<Calendar className="w-4 h-4" />} label="Actualizado" value={ticket.updatedAt ?? "—"} />
            </div>

            {ticket.sla === "critico" && (
              <div className="mt-4 p-4 rounded-lg bg-red-50 border border-red-100">
                <p className="text-sm font-semibold text-red-700">
                  SLA crítico — {ticket.slaLabel}
                </p>
                <div className="mt-2">
                  <ProgressBar value={ticket.slaProgress ?? 85} variant="danger" />
                </div>
              </div>
            )}

            <div className="mt-5">
              <h3 className="text-sm font-semibold text-zinc-900">Descripción</h3>
              <p className="mt-2 text-sm text-zinc-600 leading-relaxed">{ticket.description}</p>
            </div>

            <div className="mt-5">
              <h3 className="text-sm font-semibold text-zinc-900">Evidencias</h3>
              <ul className="mt-2 space-y-2">
                {ticket.evidences.map((ev) => (
                  <EvidenceItem key={ev.id} evidence={ev} />
                ))}
              </ul>
            </div>
          </div>

          <div className="rounded-xl bg-white border border-zinc-200 p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-zinc-900">Conversación</h3>
            <div className="mt-4 space-y-4 max-h-80 overflow-y-auto">
              {ticket.messages.map((msg) => (
                <MessageBubble key={msg.id} message={msg} />
              ))}
            </div>
            <div className="mt-4 flex flex-col sm:flex-row gap-2">
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Escribe un comentario para el cliente o nota interna..."
                rows={2}
                className="flex-1 px-3 py-2 text-sm rounded-lg border border-zinc-200 focus:ring-2 focus:ring-primary/20 outline-none resize-none"
              />
              <div className="flex sm:flex-col gap-2 shrink-0">
                <button
                  type="button"
                  className="inline-flex items-center justify-center gap-1.5 px-3 py-2 text-sm rounded-lg border border-zinc-200 hover:bg-zinc-50 transition"
                >
                  <Paperclip className="w-4 h-4" /> Adjuntar
                </button>
                <button
                  type="button"
                  className="inline-flex items-center justify-center gap-1.5 px-3 py-2 text-sm rounded-lg border border-zinc-200 hover:bg-zinc-50 transition"
                >
                  Nota interna
                </button>
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

        <aside className="w-full xl:w-80 shrink-0 space-y-4">
          <SidePanel title="Estado del ticket">
            <div className="flex flex-wrap gap-2">
              {(["abierto", "en_proceso", "cerrado"] as const).map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setStatus(s)}
                  className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition ${
                    status === s
                      ? "bg-primary text-white border-primary"
                      : "border-zinc-200 text-zinc-600 hover:border-primary"
                  }`}
                >
                  {s === "en_proceso" ? "En proceso" : s.charAt(0).toUpperCase() + s.slice(1)}
                </button>
              ))}
            </div>
            <p className="mt-3 text-xs text-zinc-500">
              Tiempo en estado actual: <strong>{ticket.timeInStatus}</strong>
            </p>
          </SidePanel>

          <SidePanel title="Asignación">
            <div className="flex items-center gap-3">
              <Avatar initials={ticket.assignedAvatar ?? "AR"} />
              <div className="min-w-0">
                <p className="text-sm font-semibold truncate">{ticket.assignedTo || "—"}</p>
                <p className="text-xs text-zinc-500">Agente asignado</p>
              </div>
            </div>
            <button
              type="button"
              className="mt-3 w-full py-2 text-sm font-medium rounded-lg border border-zinc-200 hover:bg-zinc-50 transition"
            >
              Cambiar / Asignar a otro agente
            </button>
          </SidePanel>

          <SidePanel title="Información adicional">
            <dl className="space-y-2 text-sm">
              <Meta label="Cliente" value={ticket.client} />
              <Meta label="Empresa" value={ticket.company || "—"} />
              <Meta label="Categoría" value={ticket.category} />
              <Meta label="Tipo" value={ticket.type ?? "—"} />
              <Meta label="Creado" value={ticket.createdAt || "—"} />
              <Meta label="Tiempo total" value={ticket.totalTime} />
              <Meta label="Comentarios" value={String(ticket.commentsCount)} />
            </dl>
          </SidePanel>

          {ticket.similarSolution && (
            <div className="rounded-xl bg-blue-50 border border-blue-100 p-4">
              <p className="text-xs font-semibold text-blue-800 uppercase tracking-wide">
                Solución similar encontrada
              </p>
              <p className="mt-1 text-sm text-blue-900">{ticket.similarSolution.title}</p>
              <a
                href={ticket.similarSolution.url}
                className="mt-2 inline-block text-sm font-medium text-primary hover:underline"
              >
                Ver solución →
              </a>
            </div>
          )}
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

function SidePanel({ title, children }: { title: string; children: ReactNode }) {
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

function EvidenceItem({ evidence }: { evidence: TicketEvidence }) {
  const Icon =
    evidence.type === "image" ? FileImage : FileText;
  return (
    <li className="flex items-center gap-3 p-2 rounded-lg hover:bg-zinc-50 transition">
      <Icon className="w-5 h-5 text-zinc-400" />
      <div className="min-w-0 flex-1">
        <p className="text-sm font-medium text-zinc-800 truncate">{evidence.name}</p>
        <p className="text-xs text-zinc-400">{evidence.size}</p>
      </div>
    </li>
  );
}

function MessageBubble({ message }: { message: TicketMessage }) {
  const isAgent = message.role === "agente";
  return (
    <div className={`flex ${isAgent ? "justify-end" : "justify-start"}`}>
      <div
        className={`max-w-[85%] rounded-2xl px-4 py-3 ${
          isAgent
            ? "bg-primary text-white rounded-br-md"
            : "bg-zinc-100 text-zinc-800 rounded-bl-md"
        }`}
      >
        <p className="text-xs font-semibold opacity-80 mb-1">
          {message.author} · {message.time}
        </p>
        <p className="text-sm leading-relaxed">{message.content}</p>
      </div>
    </div>
  );
}
