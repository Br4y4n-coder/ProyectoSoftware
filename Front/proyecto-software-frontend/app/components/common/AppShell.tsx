import { useEffect, useMemo, useState, type ReactNode } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router";
import { Bell, LogOut, Menu, Search, X } from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import type { AuthUser } from "../../types/auth";

export interface SidebarNavItem {
  label: string;
  to?: string;
  icon: ReactNode;
  badge?: string | number;
  soon?: boolean;
}

export interface AppShellProps {
  menuItems: SidebarNavItem[];
  accountItems?: SidebarNavItem[];
  supportItems?: SidebarNavItem[];
  onlineStatus?: string;
  children?: ReactNode;
}

export function AppShell({
  menuItems,
  accountItems = [],
  supportItems = [],
  onlineStatus,
}: AppShellProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { isLoading, isAuthenticated, user: rawUser, logout } = useAuth();
  const user = rawUser as AuthUser | null;
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) navigate("/auth/login");
  }, [isLoading, isAuthenticated, navigate]);

  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  const initials = useMemo(() => {
    const a = user?.nombres?.[0] || "";
    const b = user?.apellidos?.[0] || "";
    return (a + b).toUpperCase() || "U";
  }, [user?.nombres, user?.apellidos]);

  if (!isAuthenticated) return null;

  const fullName = user?.nombres
    ? `${user.nombres}${user.apellidos ? " " + user.apellidos : ""}`
    : "Usuario";

  const handleLogout = async () => {
    await (logout as () => Promise<void>)();
    navigate("/auth/login");
  };

  return (
    <div data-theme="app" className="flex h-screen overflow-hidden bg-[#F9FAFB] font-sans text-zinc-900">
      {sidebarOpen && (
        <button
          type="button"
          aria-label="Cerrar menú"
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-30 bg-black/40 lg:hidden"
        />
      )}

      <aside
        className={`
          fixed inset-y-0 left-0 z-40 w-64 shrink-0
          bg-white border-r border-zinc-200 flex flex-col
          transform transition-transform duration-300 ease-out
          ${sidebarOpen ? "translate-x-0" : "-translate-x-full"}
          lg:static lg:translate-x-0 lg:transition-none
        `}
      >
        <div className="px-5 pt-6 pb-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white text-sm font-bold">T</span>
            </div>
            <span className="text-base font-bold text-zinc-900">TicketHub</span>
          </div>
          <button
            type="button"
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden w-8 h-8 rounded-md text-zinc-500 hover:bg-zinc-100 flex items-center justify-center"
            aria-label="Cerrar menú"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {onlineStatus && (
          <div className="mx-4 mb-4 flex items-center gap-2 px-3 py-2 rounded-lg bg-emerald-50 border border-emerald-100">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-xs font-medium text-emerald-700">{onlineStatus}</span>
          </div>
        )}

        <SidebarSection label="Menú" />
        <nav className="px-2 space-y-0.5 flex-1 overflow-y-auto">
          {menuItems.map((item) => (
            <SidebarItem key={item.label} item={item} pathname={location.pathname} />
          ))}

          {accountItems.length > 0 && (
            <>
              <SidebarSection label="Cuenta" className="mt-6" />
              {accountItems.map((item) => (
                <SidebarItem key={item.label} item={item} pathname={location.pathname} />
              ))}
            </>
          )}

          {supportItems.length > 0 && (
            <>
              <SidebarSection label="Soporte" className="mt-6" />
              {supportItems.map((item) => (
                <SidebarItem key={item.label} item={item} pathname={location.pathname} />
              ))}
            </>
          )}
        </nav>

        <div className="p-4 border-t border-zinc-100">
          <div className="flex items-center gap-3 p-3 rounded-lg bg-[#F9FAFB]">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center shrink-0">
              <span className="text-white text-xs font-bold">{initials}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-[13px] font-semibold text-zinc-900 truncate">{fullName}</p>
              <p className="text-[11px] text-zinc-500 capitalize">{user?.rol || "Usuario"}</p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="shrink-0 w-8 h-8 rounded-md text-zinc-500 hover:bg-zinc-100 hover:text-red-500 flex items-center justify-center transition"
              aria-label="Cerrar sesión"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        <header className="h-16 shrink-0 bg-white border-b border-zinc-200 flex items-center px-4 sm:px-6 gap-3">
          <button
            type="button"
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden shrink-0 w-9 h-9 rounded-md hover:bg-zinc-100 flex items-center justify-center"
            aria-label="Abrir menú"
          >
            <Menu className="w-5 h-5" />
          </button>

          {/* Barra de búsqueda CORREGIDA */}
          <div 
            onClick={() => window.location.href = "/buscar"}
            className="hidden md:flex flex-1 max-w-md items-center gap-2 h-9 px-3 rounded-lg bg-zinc-100 text-sm text-zinc-400 cursor-pointer hover:bg-zinc-200 transition"
          >
            <Search className="w-4 h-4 shrink-0" />
            <span className="truncate">Buscar tickets, soluciones…</span>
          </div>

          <div className="flex-1 md:flex-none" />

          {/* Botón de notificaciones CORREGIDO */}
          <button
            type="button"
            onClick={() => window.location.href = "/notificaciones"}
            className="relative shrink-0 w-9 h-9 rounded-full bg-zinc-100 flex items-center justify-center hover:bg-zinc-200 transition"
            aria-label="Notificaciones"
          >
            <Bell className="w-4 h-4 text-zinc-600" />
            <span className="absolute top-0.5 right-0.5 w-2.5 h-2.5 rounded-full bg-red-500 ring-2 ring-white" />
          </button>

          <div className="shrink-0 w-9 h-9 rounded-full bg-primary flex items-center justify-center">
            <span className="text-white text-xs font-bold">{initials}</span>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto">
          <div className="px-4 sm:px-6 lg:px-10 py-6 lg:py-8 max-w-[1400px] mx-auto w-full">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

function SidebarSection({ label, className = "" }: { label: string; className?: string }) {
  return (
    <p
      className={`px-5 pb-1 text-[11px] font-semibold tracking-wider uppercase text-zinc-400 ${className}`}
    >
      {label}
    </p>
  );
}

function SidebarItem({
  item,
  pathname,
}: {
  item: SidebarNavItem;
  pathname: string;
}) {
  const baseClass =
    "flex items-center gap-3 px-3 h-10 rounded-lg text-sm font-medium transition relative w-full";

  if (item.soon) {
    return (
      <button
        type="button"
        onClick={() => alert(`"${item.label}" — próximamente.`)}
        className={`${baseClass} text-zinc-600 hover:bg-zinc-50`}
      >
        <span className="text-zinc-500">{item.icon}</span>
        <span className="flex-1 text-left">{item.label}</span>
      </button>
    );
  }

  if (!item.to) return null;

  const isActive =
    pathname === item.to ||
    (item.to !== "/" && pathname.startsWith(item.to));

  return (
    <NavLink
      to={item.to}
      className={`${baseClass} ${
        isActive ? "bg-primary-faint text-primary" : "text-zinc-600 hover:bg-zinc-50"
      }`}
    >
      {isActive && (
        <span className="absolute left-0 top-1.5 bottom-1.5 w-[3px] rounded-full bg-primary" />
      )}
      <span className={isActive ? "text-primary" : "text-zinc-500"}>{item.icon}</span>
      <span className="flex-1">{item.label}</span>
      {item.badge !== undefined && (
        <span className="ml-auto text-xs font-semibold px-1.5 py-0.5 rounded-full bg-zinc-100 text-zinc-600">
          {item.badge}
        </span>
      )}
    </NavLink>
  );
}