import { useEffect, useState, type ReactNode } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router";
import { LogOut, Menu, Search, X } from "lucide-react";
import NotificationsBell from "./NotificationsBell";
import { useAuth } from "../../contexts/AuthContext";
import type { AuthUser } from "../../types/auth";

export interface SidebarNavItem {
  label: string;
  to?: string;
  icon: ReactNode;
  badge?: string | number;
  soon?: boolean;
}

export interface SidebarSection {
  label: string;
  items: SidebarNavItem[];
}

export interface AppShellProps {
  /** Secciones con etiqueta personalizada (PANEL, REPORTES, SISTEMA…) */
  sections?: SidebarSection[];
  /** Compatibilidad: items bajo etiqueta "Menú" */
  menuItems?: SidebarNavItem[];
  /** Compatibilidad: items bajo etiqueta "Cuenta" */
  accountItems?: SidebarNavItem[];
  /** Compatibilidad: items bajo etiqueta "Soporte" */
  supportItems?: SidebarNavItem[];
  /** Badge de modo en el sidebar: "MODO ADMINISTRADOR", "MODO AGENTE"… */
  modeBadge?: string;
  /** Contenido a la izquierda de la barra superior (ej. saludo del agente).
   *  Si se pasa, la búsqueda se compacta y se alinea a la derecha. */
  headerLeft?: ReactNode;
  /** Muestra u oculta la búsqueda de la barra superior (default: true). */
  showSearch?: boolean;
  children?: ReactNode;
}

export function AppShell({
  sections,
  menuItems = [],
  accountItems = [],
  supportItems = [],
  modeBadge,
  headerLeft,
  showSearch = true,
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

  const initials =
    ((user?.nombres?.[0] || "") + (user?.apellidos?.[0] || "")).toUpperCase() || "U";

  if (!isAuthenticated) return null;

  const fullName = user?.nombres
    ? `${user.nombres}${user.apellidos ? " " + user.apellidos : ""}`
    : "Usuario";

  const handleLogout = async () => {
    await (logout as () => Promise<void>)();
    navigate("/auth/login");
  };

  // Construye secciones desde legacy props si no se pasa `sections`
  const allSections: SidebarSection[] = sections ?? [
    ...(menuItems.length ? [{ label: "Menú", items: menuItems }] : []),
    ...(accountItems.length ? [{ label: "Cuenta", items: accountItems }] : []),
    ...(supportItems.length ? [{ label: "Soporte", items: supportItems }] : []),
  ];

  return (
    <div data-theme="app" className="flex h-screen overflow-hidden bg-[#F9FAFB] font-sans text-zinc-900">
      {/* Overlay móvil */}
      {sidebarOpen && (
        <button
          type="button"
          aria-label="Cerrar menú"
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-30 bg-black/40 lg:hidden"
        />
      )}

      {/* Sidebar oscuro */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-40 w-64 shrink-0
          bg-zinc-900 flex flex-col
          transform transition-transform duration-300 ease-out
          ${sidebarOpen ? "translate-x-0" : "-translate-x-full"}
          lg:static lg:translate-x-0 lg:transition-none
        `}
      >
        {/* Logo */}
        <div className="px-5 pt-6 pb-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white text-sm font-bold">T</span>
            </div>
            <span className="text-base font-bold text-white">TicketHub</span>
          </div>
          <button
            type="button"
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden w-8 h-8 rounded-md text-zinc-400 hover:bg-white/10 flex items-center justify-center"
            aria-label="Cerrar menú"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Badge de modo (MODO ADMINISTRADOR / MODO AGENTE) */}
        {modeBadge && (
          <div className="mx-4 mb-3 flex items-center gap-2 px-3 py-2 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse shrink-0" />
            <span className="text-[11px] font-semibold text-emerald-400 tracking-widest uppercase">
              {modeBadge}
            </span>
          </div>
        )}

        {/* Navegación por secciones */}
        <nav className="px-2 flex-1 overflow-y-auto">
          {allSections.map((section, i) => (
            <div key={section.label} className={i > 0 ? "mt-5" : ""}>
              <p className="px-3 pb-1 text-[10px] font-semibold tracking-widest uppercase text-zinc-500">
                {section.label}
              </p>
              <div className="space-y-0.5">
                {section.items.map((item) => (
                  <SidebarItem key={item.label} item={item} pathname={location.pathname} />
                ))}
              </div>
            </div>
          ))}
        </nav>

        {/* Perfil de usuario */}
        <div className="p-4 border-t border-zinc-800">
          <div className="flex items-center gap-3 p-3 rounded-lg bg-white/5">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center shrink-0">
              <span className="text-white text-xs font-bold">{initials}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-[13px] font-semibold text-white truncate">{fullName}</p>
              <p className="text-[11px] text-zinc-400 capitalize">{user?.rol || "Usuario"}</p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="shrink-0 w-8 h-8 rounded-md text-zinc-400 hover:bg-white/10 hover:text-red-400 flex items-center justify-center transition"
              aria-label="Cerrar sesión"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Área de contenido principal */}
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

          {headerLeft ? (
            <>
              {/* Saludo / título a la izquierda, búsqueda compacta a la derecha */}
              <div className="flex-1 min-w-0">{headerLeft}</div>
              {showSearch && (
                <div
                  onClick={() => (window.location.href = "/buscar")}
                  className="hidden md:flex w-64 shrink-0 items-center gap-2 h-9 px-3 rounded-lg bg-zinc-100 text-sm text-zinc-400 cursor-pointer hover:bg-zinc-200 transition"
                >
                  <Search className="w-4 h-4 shrink-0" />
                  <span className="truncate">Buscar tickets, clientes…</span>
                </div>
              )}
            </>
          ) : (
            <>
              {showSearch && (
                <div
                  onClick={() => (window.location.href = "/buscar")}
                  className="hidden md:flex flex-1 max-w-md items-center gap-2 h-9 px-3 rounded-lg bg-zinc-100 text-sm text-zinc-400 cursor-pointer hover:bg-zinc-200 transition"
                >
                  <Search className="w-4 h-4 shrink-0" />
                  <span className="truncate">Buscar tickets, soluciones…</span>
                </div>
              )}
              <div className="flex-1 md:flex-none" />
            </>
          )}

          <NotificationsBell />

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
        className={`${baseClass} text-zinc-400 hover:text-white hover:bg-white/10`}
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
        isActive
          ? "bg-white/10 text-white"
          : "text-zinc-400 hover:text-white hover:bg-white/10"
      }`}
    >
      {isActive && (
        <span className="absolute left-0 top-1.5 bottom-1.5 w-[3px] rounded-full bg-primary" />
      )}
      <span className={isActive ? "text-white" : "text-zinc-500"}>{item.icon}</span>
      <span className="flex-1">{item.label}</span>
      {item.badge !== undefined && (
        <span className="ml-auto text-xs font-semibold px-1.5 py-0.5 rounded-full bg-white/10 text-zinc-300">
          {item.badge}
        </span>
      )}
    </NavLink>
  );
}
