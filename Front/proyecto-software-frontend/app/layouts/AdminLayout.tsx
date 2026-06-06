import {
  BarChart3,
  ClipboardList,
  Download,
  LayoutDashboard,
  Plug,
  Settings,
  Shield,
  Tags,
  Timer,
  Users,
  UserCog,
} from "lucide-react";
import { useLocation } from "react-router";
import { AppShell, type SidebarSection } from "../components/common/AppShell";

const panelItems: SidebarSection = {
  label: "PANEL",
  items: [
    { label: "Dashboard", to: "/admin/dashboard", icon: <LayoutDashboard className="w-4 h-4" /> },
    { label: "Tickets", to: "/admin/tickets", icon: <ClipboardList className="w-4 h-4" /> },
    { label: "Usuarios", to: "/admin/users", icon: <Users className="w-4 h-4" /> },
    { label: "Agentes", to: "/admin/agentes", icon: <UserCog className="w-4 h-4" /> },
    { label: "Categorías", to: "/admin/categorias", icon: <Tags className="w-4 h-4" /> },
    { label: "SLA y reglas", to: "/admin/sla", icon: <Timer className="w-4 h-4" /> },
  ],
};

const reportesItems: SidebarSection = {
  label: "REPORTES",
  items: [
    { label: "Métricas", to: "/admin/metricas", icon: <BarChart3 className="w-4 h-4" /> },
    { label: "Auditoría", to: "/admin/auditoria", icon: <Shield className="w-4 h-4" /> },
    { label: "Exportar datos", to: "/admin/exportar", icon: <Download className="w-4 h-4" /> },
  ],
};

const sistemaItems: SidebarSection = {
  label: "SISTEMA",
  items: [
    { label: "Configuración", to: "/admin/configuracion", icon: <Settings className="w-4 h-4" /> },
    { label: "Integraciones", to: "/admin/integraciones", icon: <Plug className="w-4 h-4" /> },
  ],
};

/** Título de cada ruta para la barra superior (estilo mockup: "Admin › Página"). */
const TITULOS: Array<{ prefix: string; titulo: string }> = [
  { prefix: "/admin/dashboard", titulo: "Dashboard de administración" },
  { prefix: "/admin/tickets", titulo: "Gestión de tickets" },
  { prefix: "/admin/users", titulo: "Gestión de usuarios" },
  { prefix: "/admin/agentes", titulo: "Gestión de agentes" },
  { prefix: "/admin/categorias", titulo: "Categorías" },
  { prefix: "/admin/sla", titulo: "SLA y reglas" },
  { prefix: "/admin/metricas", titulo: "Métricas" },
  { prefix: "/admin/auditoria", titulo: "Auditoría" },
  { prefix: "/admin/exportar", titulo: "Exportar datos" },
  { prefix: "/admin/configuracion", titulo: "Configuración" },
  { prefix: "/admin/integraciones", titulo: "Integraciones" },
  { prefix: "/admin/assign-ticket", titulo: "Asignar ticket" },
];

function AdminHeaderTitle() {
  const { pathname } = useLocation();
  const titulo =
    TITULOS.find((t) => pathname.startsWith(t.prefix))?.titulo ?? "Panel de administración";

  return (
    <div className="min-w-0">
      <p className="text-[11px] text-zinc-400 leading-tight truncate">
        Admin › <span className="text-zinc-500">{titulo}</span>
      </p>
      <p className="text-base sm:text-lg font-bold text-zinc-900 leading-tight truncate">
        {titulo}
      </p>
    </div>
  );
}

export default function AdminLayout() {
  return (
    <AppShell
      sections={[panelItems, reportesItems, sistemaItems]}
      modeBadge="MODO ADMINISTRADOR"
      headerLeft={<AdminHeaderTitle />}
      showSearch={false}
    />
  );
}
