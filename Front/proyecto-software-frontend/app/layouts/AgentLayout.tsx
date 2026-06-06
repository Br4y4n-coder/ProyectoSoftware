import {
  BarChart3,
  BookOpen,
  ClipboardList,
  Home,
  Inbox,
  Search,
  Settings,
  User,
} from "lucide-react";
import { AppShell, type SidebarSection } from "../components/common/AppShell";

const panelItems: SidebarSection = {
  label: "PANEL",
  items: [
    { label: "Inicio", to: "/agent/dashboard", icon: <Home className="w-4 h-4" /> },
    { label: "Cola de tickets", to: "/agent/queue", icon: <Inbox className="w-4 h-4" /> },
    { label: "Mis asignados", to: "/agent/mis-asignados", icon: <ClipboardList className="w-4 h-4" /> },
  ],
};

const herramientasItems: SidebarSection = {
  label: "HERRAMIENTAS",
  items: [
    { label: "Búsqueda", to: "/buscar", icon: <Search className="w-4 h-4" /> },
    { label: "Base de conocimiento", to: "/base-conocimiento", icon: <BookOpen className="w-4 h-4" /> },
    { label: "Reportes", to: "/reportes", icon: <BarChart3 className="w-4 h-4" /> },
  ],
};

const cuentaItems: SidebarSection = {
  label: "CUENTA",
  items: [
    { label: "Mi perfil", to: "/mi-perfil", icon: <User className="w-4 h-4" /> },
    { label: "Configuración", to: "/configuracion", icon: <Settings className="w-4 h-4" /> },
  ],
};

export default function AgentLayout() {
  return (
    <AppShell
      sections={[panelItems, herramientasItems, cuentaItems]}
      modeBadge="MODO AGENTE"
    />
  );
}
