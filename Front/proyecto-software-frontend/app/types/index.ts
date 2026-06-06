export type TicketPriority = "baja" | "media" | "alta" | "critica";
export type TicketStatus = "abierto" | "en_proceso" | "cerrado" | "vencido";
export type SlaLevel = "normal" | "critico" | "vencido";
export type UserRole = "administrador" | "agente" | "usuario";
export type UserStatus = "activo" | "pendiente" | "sin_acceso" | "inactivo";
export type AgentAvailability = "disponible" | "ocupado" | "saturado";

export interface Ticket {
  id: string;
  subject: string;
  client: string;
  company?: string;
  category: string;
  type?: string;
  priority: TicketPriority;
  status: TicketStatus;
  sla: SlaLevel;
  slaLabel?: string;
  slaProgress?: number;
  assignedTo?: string;
  assignedAvatar?: string;
  createdAt?: string;
  updatedAt?: string;
  description?: string;
}

export interface TicketMessage {
  id: string;
  author: string;
  role: "cliente" | "agente" | "sistema";
  content: string;
  time: string;
  internal?: boolean;
}

export interface TicketEvidence {
  id: string;
  name: string;
  size: string;
  type: "image" | "pdf" | "doc";
}

export interface TicketDetail extends Ticket {
  clientEmail?: string;
  timeInStatus: string;
  totalTime: string;
  commentsCount: number;
  slaMinutesLeft?: number;
  description: string;
  evidences: TicketEvidence[];
  messages: TicketMessage[];
  similarSolution?: { title: string; url: string };
}

export interface AgentKpi {
  label: string;
  value: string | number;
  subtext?: string;
  trend?: { value: string; positive?: boolean };
  accent?: "blue" | "green" | "amber" | "red";
}

export interface ActivityItem {
  id: string;
  icon: "ticket" | "comment" | "status" | "sla";
  text: string;
  time: string;
}

export interface ChartPoint {
  day: string;
  resolved: number;
  assigned?: number;
}

export interface AdminUser {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  status: UserStatus;
}

export interface AssignableAgent {
  id: string;
  name: string;
  area: string;
  level: string;
  activeTickets: number;
  availability: AgentAvailability;
  badge?: string;
  recommended?: boolean;
}

export interface AgentPerformance {
  name: string;
  resolved: number;
  average: string;
  sla: string;
}

export interface CategoryStat {
  name: string;
  count: number;
}

export interface RiskTicket {
  id: string;
  description: string;
  assigned: string;
  minutesLeft: number;
}

export interface StatusDistribution {
  name: string;
  value: number;
  color: string;
}
