import {
  type RouteConfig,
  index,
  route,
  layout,
} from "@react-router/dev/routes";

export default [
  route("auth/login", "pages/Login.jsx"),
  route("auth/register", "pages/Register.jsx"),
  route("auth/verify-email", "pages/VerifyEmail.jsx"),
  route("auth/reset-password", "pages/ResetPassword.jsx"),

  layout("layouts/AgentLayout.tsx", [
    route("agent/dashboard", "pages/agent/AgentDashboard.jsx"),
    route("agent/queue", "pages/agent/TicketQueue.jsx"),
    route("agent/tickets/:id", "pages/agent/TicketDetail.jsx"),
    route("agent/mis-asignados", "pages/agent/MisAsignados.jsx"),
    route("reportes", "pages/Reportes.jsx"),
  ]),

  layout("layouts/AdminLayout.tsx", [
    route("admin/dashboard", "pages/admin/AdminDashboard.tsx"),
    route("admin/users", "pages/admin/UserManagement.tsx"),
    route("admin/tickets", "pages/admin/TicketsAdmin.tsx"),
    route("admin/agentes", "pages/admin/AgentesAdmin.tsx"),
    route("admin/categorias", "pages/admin/CategoriasAdmin.tsx"),
    route("admin/sla", "pages/admin/SlaAdmin.tsx"),
    route("admin/metricas", "pages/admin/MetricasAdmin.tsx"),
    route("admin/auditoria", "pages/admin/AuditoriaAdmin.tsx"),
    route("admin/exportar", "pages/admin/ExportarAdmin.tsx"),
    route("admin/configuracion", "pages/admin/ConfiguracionAdmin.tsx"),
    route("admin/integraciones", "pages/admin/IntegracionesAdmin.tsx"),
  ]),

  layout("layouts/MainLayout.jsx", [
    index("pages/Home.jsx"),
    route("mis-tickets", "pages/MisTickets.jsx"),
    route("tickets/nuevo", "pages/CrearTicket.jsx"),
    route("base-conocimiento", "pages/BaseConocimiento.jsx"),
    route("mi-perfil", "pages/MiPerfil.jsx"),
    route("ayuda", "pages/Ayuda.jsx"),
    route("configuracion", "pages/Configuracion.jsx"),
    route("notificaciones", "pages/Notificaciones.jsx"),
    route("buscar", "pages/Buscar.jsx"),
    route("*", "pages/NotFound.jsx"),
  ]),
] satisfies RouteConfig;