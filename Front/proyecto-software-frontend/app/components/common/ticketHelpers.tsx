import type { SlaLevel, TicketPriority, TicketStatus } from "../../types";
import { Badge } from "./Badge";

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  const map: Record<TicketPriority, { label: string; variant: "info" | "warning" | "danger" | "default" }> = {
    baja: { label: "Baja", variant: "default" },
    media: { label: "Media", variant: "info" },
    alta: { label: "Alta", variant: "warning" },
    critica: { label: "Crítica", variant: "danger" },
  };
  const { label, variant } = map[priority];
  return <Badge variant={variant}>{label}</Badge>;
}

export function StatusBadge({ status }: { status: TicketStatus }) {
  const map: Record<TicketStatus, { label: string; variant: "info" | "warning" | "success" | "danger" }> = {
    abierto: { label: "Abierto", variant: "info" },
    en_proceso: { label: "En proceso", variant: "warning" },
    cerrado: { label: "Cerrado", variant: "success" },
    vencido: { label: "Vencido", variant: "danger" },
  };
  const { label, variant } = map[status];
  return <Badge variant={variant}>{label}</Badge>;
}

export function SlaBadge({ sla, label }: { sla: SlaLevel; label?: string }) {
  if (sla === "critico") {
    return <Badge variant="danger">{label || "Crítico"}</Badge>;
  }
  if (sla === "vencido") {
    return <Badge variant="danger">Vencido</Badge>;
  }
  return <Badge variant="outline">{label || "Normal"}</Badge>;
}
