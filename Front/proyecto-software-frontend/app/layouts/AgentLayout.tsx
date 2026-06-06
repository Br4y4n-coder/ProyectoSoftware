import {
  BookOpen,
  ClipboardList,
  Home,
  Inbox,
  Search,
  Settings,
  User,
  BarChart3,
} from "lucide-react";
import { AppShell, type SidebarNavItem } from "../components/common/AppShell";
import { CURRENT_AGENT } from "../data/mockData";

const menuItems: SidebarNavItem[] = [
  { label: "Inicio", to: "/agent/dashboard", icon: <Home className="w-4 h-4" /> },
  { label: "Cola de tickets", to: "/agent/queue", icon: <Inbox className="w-4 h-4" />, badge: 12 },
  { label: "Mis asignados", to: "/agent/mis-asignados", icon: <ClipboardList className="w-4 h-4" /> },
  { label: "Búsqueda", to: "/buscar", icon: <Search className="w-4 h-4" /> },
  { label: "Base de conocimiento", to: "/base-conocimiento", icon: <BookOpen className="w-4 h-4" /> },
  { label: "Reportes", to: "/reportes", icon: <BarChart3 className="w-4 h-4" /> },
];

const accountItems: SidebarNavItem[] = [
  { label: "Mi perfil", to: "/mi-perfil", icon: <User className="w-4 h-4" /> },
  { label: "Configuración", to: "/configuracion", icon: <Settings className="w-4 h-4" /> },
];

export default function AgentLayout() {
  return (
    <AppShell
      menuItems={menuItems}
      accountItems={accountItems}
      onlineStatus={CURRENT_AGENT.status}
    />
  );
}