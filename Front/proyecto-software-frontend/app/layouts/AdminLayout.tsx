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

export default function AdminLayout() {
  return (
    <AppShell
      sections={[panelItems, reportesItems, sistemaItems]}
      modeBadge="MODO ADMINISTRADOR"
    />
  );
}
