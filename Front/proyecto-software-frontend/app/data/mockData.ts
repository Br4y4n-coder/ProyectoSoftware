import type {
  ActivityItem,
  AdminUser,
  AgentKpi,
  AgentPerformance,
  AssignableAgent,
  CategoryStat,
  ChartPoint,
  RiskTicket,
  StatusDistribution,
  Ticket,
  TicketDetail,
} from "../types";

export const CURRENT_AGENT = {
  name: "Andrés",
  fullName: "Andrés Rodríguez",
  status: "Listo para tickets",
  initials: "AR",
};

export const agentKpis: AgentKpi[] = [
  {
    label: "ASIGNADOS A MÍ",
    value: 7,
    subtext: "2 críticos • 5 normales",
    accent: "blue",
  },
  {
    label: "RESUELTOS HOY",
    value: 12,
    trend: { value: "+33% vs. ayer", positive: true },
    accent: "green",
  },
  {
    label: "TIEMPO MEDIO",
    value: "2.4h",
    trend: { value: "-18min vs. semana", positive: true },
    accent: "amber",
  },
  {
    label: "CUMPLIMIENTO SLA",
    value: "94%",
    subtext: "meta mensual: 95%",
    accent: "blue",
  },
];

export const agentPerformanceChart: ChartPoint[] = [
  { day: "Lun", resolved: 8, assigned: 6 },
  { day: "Mar", resolved: 11, assigned: 7 },
  { day: "Mié", resolved: 9, assigned: 8 },
  { day: "Jue", resolved: 14, assigned: 10 },
  { day: "Vie", resolved: 12, assigned: 9 },
  { day: "Sáb", resolved: 5, assigned: 4 },
  { day: "Dom", resolved: 3, assigned: 2 },
];

export const agentAssignedTickets: Ticket[] = [
  {
    id: "TKT-1038",
    subject: "Error al exportar reportes",
    client: "María López",
    sla: "critico",
    slaLabel: "35 min",
    priority: "alta",
    status: "en_proceso",
    category: "Software",
  },
  {
    id: "TKT-1035",
    subject: "Impresora no responde en piso 3",
    client: "Carlos Méndez",
    sla: "critico",
    slaLabel: "1h 12m",
    priority: "alta",
    status: "abierto",
    category: "Hardware",
  },
  {
    id: "TKT-1032",
    subject: "Acceso denegado al portal",
    client: "Laura Vega",
    sla: "normal",
    slaLabel: "4h 20m",
    priority: "media",
    status: "en_proceso",
    category: "Accesos",
  },
  {
    id: "TKT-1029",
    subject: "Actualización de licencias Office",
    client: "Pedro Sánchez",
    sla: "normal",
    slaLabel: "6h",
    priority: "baja",
    status: "abierto",
    category: "Software",
  },
  {
    id: "TKT-1025",
    subject: "VPN intermitente",
    client: "Ana Ruiz",
    sla: "normal",
    slaLabel: "8h",
    priority: "media",
    status: "en_proceso",
    category: "Red",
  },
];

export const recentActivity: ActivityItem[] = [
  {
    id: "1",
    icon: "sla",
    text: "SLA crítico en TKT-1038 — requiere respuesta",
    time: "Hace 5 min",
  },
  {
    id: "2",
    icon: "comment",
    text: "María López respondió en TKT-1038",
    time: "Hace 18 min",
  },
  {
    id: "3",
    icon: "status",
    text: "Cambiaste TKT-1032 a En proceso",
    time: "Hace 42 min",
  },
  {
    id: "4",
    icon: "ticket",
    text: "Nuevo ticket asignado: TKT-1035",
    time: "Hace 1 h",
  },
];

export const queueTickets: Ticket[] = [
  ...agentAssignedTickets,
  {
    id: "TKT-1040",
    subject: "No carga el módulo de facturación",
    client: "Carolina Ruiz",
    company: "NovaTech SAS",
    sla: "critico",
    slaLabel: "2h 15m",
    priority: "alta",
    status: "abierto",
    category: "Software",
    assignedTo: "Sin asignar",
  },
  {
    id: "TKT-1039",
    subject: "Pantalla azul en equipo contable",
    client: "Jorge Fernández",
    sla: "normal",
    slaLabel: "5h",
    priority: "media",
    status: "abierto",
    category: "Hardware",
    assignedTo: "Natalia B.",
    assignedAvatar: "NB",
  },
  {
    id: "TKT-1037",
    subject: "Restablecer contraseña de dominio",
    client: "Sofía Castro",
    sla: "normal",
    slaLabel: "7h",
    priority: "baja",
    status: "abierto",
    category: "Accesos",
    assignedTo: "Sin asignar",
  },
];

export const ticketDetails: Record<string, TicketDetail> = {
  "TKT-1038": {
    id: "TKT-1038",
    subject: "Error al exportar reportes financieros",
    client: "María López",
    company: "Finanzas Plus",
    category: "Software",
    type: "Incidente",
    priority: "alta",
    status: "en_proceso",
    sla: "critico",
    slaLabel: "Vence en 35 minutos",
    slaProgress: 85,
    slaMinutesLeft: 35,
    createdAt: "27 abr 2026, 09:14",
    updatedAt: "27 abr 2026, 11:02",
    timeInStatus: "1h 24m",
    totalTime: "3h 48m",
    commentsCount: 6,
    assignedTo: "Andrés Rodríguez",
    assignedAvatar: "AR",
    description:
      "Al intentar exportar el reporte mensual en PDF, la aplicación muestra el error ERR_EXPORT_42 y cierra la sesión. Ocurre en Chrome y Edge, versión del módulo 4.2.1.",
    evidences: [
      { id: "e1", name: "captura-error.png", size: "240 KB", type: "image" },
      { id: "e2", name: "log-servidor.txt", size: "18 KB", type: "doc" },
      { id: "e3", name: "reporte-ejemplo.pdf", size: "1.2 MB", type: "pdf" },
    ],
    messages: [
      {
        id: "m1",
        author: "María López",
        role: "cliente",
        content:
          "Buenos días, necesito el reporte antes del cierre contable de hoy. ¿Pueden ayudarme con urgencia?",
        time: "09:20",
      },
      {
        id: "m2",
        author: "Andrés Rodríguez",
        role: "agente",
        content:
          "Hola María, ya revisé los logs. Voy a aplicar un parche temporal y te confirmo en 30 minutos.",
        time: "10:45",
      },
    ],
    similarSolution: {
      title: "Error ERR_EXPORT en módulo financiero v4.x",
      url: "#",
    },
  },
};

export const defaultTicketDetail = ticketDetails["TKT-1038"];

export const adminKpis = [
  {
    label: "TICKETS HOY",
    value: "47",
    subtext: "12% | 35 abiertos - 12 cerrados",
    accent: "blue" as const,
  },
  {
    label: "SLA CUMPLIDO",
    value: "96.4%",
    subtext: "2.1% | Meta: 95%",
    trend: { value: "2.1%", positive: true },
    accent: "green" as const,
  },
  {
    label: "TIEMPO MEDIO",
    value: "2h 14m",
    subtext: "-18m | Meta SLA Alta: 4h",
    accent: "amber" as const,
  },
  {
    label: "SATISFACCIÓN",
    value: "4.7★",
    subtext: "234 valoraciones",
    accent: "blue" as const,
  },
];

export const ticketsPerDayChart = [
  { day: "Sem 1", abiertos: 120, resueltos: 98 },
  { day: "Sem 2", abiertos: 135, resueltos: 110 },
  { day: "Sem 3", abiertos: 128, resueltos: 125 },
  { day: "Sem 4", abiertos: 142, resueltos: 138 },
];

export const topCategories: CategoryStat[] = [
  { name: "Hardware", count: 142 },
  { name: "Software", count: 98 },
  { name: "Accesos", count: 76 },
  { name: "Red", count: 54 },
  { name: "Otros", count: 31 },
];

export const agentPerformance: AgentPerformance[] = [
  { name: "Andrés Rodríguez", resolved: 48, average: "2.1h", sla: "96%" },
  { name: "Natalia Betancourt", resolved: 52, average: "1.9h", sla: "98%" },
  { name: "Carlos Méndez", resolved: 41, average: "2.5h", sla: "93%" },
  { name: "Laura Vega", resolved: 35, average: "2.8h", sla: "91%" },
];

export const statusDistribution: StatusDistribution[] = [
  { name: "Abierto", value: 32, color: "#3B82F6" },
  { name: "Cerrado", value: 39, color: "#10B981" },
  { name: "En proceso", value: 21, color: "#F59E0B" },
  { name: "Vencido", value: 8, color: "#EF4444" },
];

export const riskTickets: RiskTicket[] = [
  {
    id: "TKT-1038",
    description: "Error al exportar reportes",
    assigned: "Andrés R.",
    minutesLeft: 35,
  },
  {
    id: "TKT-1040",
    description: "No carga módulo de facturación",
    assigned: "Sin asignar",
    minutesLeft: 135,
  },
  {
    id: "TKT-1035",
    description: "Impresora no responde",
    assigned: "Andrés R.",
    minutesLeft: 72,
  },
];

export const adminUsers: AdminUser[] = [
  {
    id: "1",
    email: "jorge.fernandez@empresa.com",
    name: "Jorge Fernández",
    role: "administrador",
    status: "activo",
  },
  {
    id: "2",
    email: "andres.rodriguez@empresa.com",
    name: "Andrés Rodríguez",
    role: "agente",
    status: "activo",
  },
  {
    id: "3",
    email: "natalia.betancourt@empresa.com",
    name: "Natalia Betancourt",
    role: "agente",
    status: "activo",
  },
  {
    id: "4",
    email: "jhon.gomez@empresa.com",
    name: "Jhon Gómez",
    role: "usuario",
    status: "pendiente",
  },
  {
    id: "5",
    email: "maria.lopez@cliente.com",
    name: "María López",
    role: "usuario",
    status: "activo",
  },
  {
    id: "6",
    email: "temp.user@empresa.com",
    name: "Usuario Temporal",
    role: "agente",
    status: "sin_acceso",
  },
];

export const userStats = {
  total: 312,
  totalDelta: "+12",
  activeAgents: 8,
  administrators: 3,
  pendingVerification: 5,
};

export const assignTicketPreview = {
  id: "TKT-1040",
  subject: "No carga el módulo de facturación",
  priority: "alta" as const,
  category: "Software",
  client: "Carolina Ruiz",
  slaRemaining: "2h 15m",
};

export const assignableAgents: AssignableAgent[] = [
  {
    id: "a1",
    name: "Natalia Betancourt",
    area: "Software",
    level: "Nivel 1 — Soporte Software",
    activeTickets: 4,
    availability: "disponible",
    badge: "EXPERTA",
    recommended: true,
  },
  {
    id: "a2",
    name: "Andrés Rodríguez",
    area: "Software",
    level: "Nivel 2 — Soporte Software",
    activeTickets: 7,
    availability: "ocupado",
  },
  {
    id: "a3",
    name: "Carlos Méndez",
    area: "Software",
    level: "Nivel 1 — Soporte Software",
    activeTickets: 11,
    availability: "saturado",
  },
  {
    id: "a4",
    name: "Laura Vega",
    area: "Hardware",
    level: "Nivel 1 — Soporte Hardware",
    activeTickets: 3,
    availability: "disponible",
  },
];

export const DEV_ROLE_KEY = "tickethub_dev_role";
