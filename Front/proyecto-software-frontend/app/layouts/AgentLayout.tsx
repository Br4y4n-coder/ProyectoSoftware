import { useEffect, useState } from "react";
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
import { useAuth } from "../contexts/AuthContext";
import ticketsService from "../services/ticketsService";

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

/** Saludo del agente en la barra superior (como en el mockup). */
function AgentGreeting() {
  const { user } = useAuth() as {
    user: { id?: number; nombres?: string } | null;
  };
  const [total, setTotal] = useState<number | null>(null);

  useEffect(() => {
    if (!user?.id) return;
    let cancelado = false;
    ticketsService
      .listar({ agenteId: user.id, page: 0, size: 1 })
      .then(({ data }: { data: { data?: { totalElements?: number } } }) => {
        if (!cancelado) setTotal(data?.data?.totalElements ?? 0);
      })
      .catch(() => {
        if (!cancelado) setTotal(null);
      });
    return () => {
      cancelado = true;
    };
  }, [user?.id]);

  const hora = new Date().getHours();
  const saludo = hora < 12 ? "Buen día" : hora < 19 ? "Buenas tardes" : "Buenas noches";

  return (
    <div className="min-w-0">
      <p className="text-base sm:text-lg font-bold text-zinc-900 leading-tight truncate">
        {saludo}, {user?.nombres || "Agente"} 👋
      </p>
      <p className="text-xs text-zinc-500 truncate">
        {total === null
          ? "Panel de agente"
          : `Tienes ${total} ticket${total === 1 ? "" : "s"} asignado${total === 1 ? "" : "s"}`}
      </p>
    </div>
  );
}

export default function AgentLayout() {
  return (
    <AppShell
      sections={[panelItems, herramientasItems, cuentaItems]}
      modeBadge="MODO AGENTE"
      headerLeft={<AgentGreeting />}
    />
  );
}
