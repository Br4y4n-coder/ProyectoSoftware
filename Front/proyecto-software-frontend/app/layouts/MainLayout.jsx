import { Outlet, NavLink, useLocation, useNavigate } from "react-router";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../contexts/AuthContext";

export default function MainLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { isLoading, isAuthenticated, user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) navigate("/auth/login");
  }, [isLoading, isAuthenticated, navigate]);

  // Cierra el drawer al cambiar de ruta (evita que quede abierto al navegar en móvil).
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  // Hooks antes del early-return: si no, React rompe entre renders.
  const initials = useMemo(() => {
    const a = user?.nombres?.[0] || "";
    const b = user?.apellidos?.[0] || "";
    return (a + b).toUpperCase() || "U";
  }, [user?.nombres, user?.apellidos]);
  const today = useMemo(
    () =>
      new Intl.DateTimeFormat("es-CO", {
        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric",
      }).format(new Date()),
    []
  );

  if (!isAuthenticated) return null;

  const fullName = user?.nombres
    ? `${user.nombres}${user.apellidos ? " " + user.apellidos : ""}`
    : "Usuario";

  const handleLogout = async () => {
    await logout();
    navigate("/auth/login");
  };

  // Menu items
  const menuItems = [
    { label: "Inicio", icon: <IconHome />, to: "/" },
    { label: "Mis tickets", icon: <IconList />, to: "/mis-tickets" },
    { label: "Crear ticket", icon: <IconPlus />, to: "/tickets/nuevo" },
    { label: "Base de conocimiento", icon: <IconBook />, to: "/base-conocimiento" },
    { label: "Mi perfil", icon: <IconUser />, to: "/mi-perfil" },
  ];
  const supportItems = [
    { label: "Ayuda", icon: <IconHelp />, to: "/ayuda" },
    { label: "Configuración", icon: <IconSettings />, to: "/configuracion" },
  ];

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

      {/* Drawer en <lg, estática en lg+ */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-40 w-60 shrink-0
          bg-white border-r border-zinc-200 flex flex-col
          transform transition-transform duration-300 ease-out
          ${sidebarOpen ? "translate-x-0" : "-translate-x-full"}
          lg:static lg:translate-x-0 lg:transition-none
        `}
      >
        <div className="px-6 pt-6 pb-4 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white text-sm font-bold">T</span>
            </div>
            <span className="text-base font-bold text-zinc-900">TicketHub</span>
          </div>
          <button
            type="button"
            onClick={() => setSidebarOpen(false)}
            aria-label="Cerrar menú"
            className="lg:hidden -mr-1 w-8 h-8 rounded-md text-zinc-500 hover:bg-zinc-100 flex items-center justify-center"
          >
            <IconX />
          </button>
        </div>

        <SidebarSection label="Menú" />
        <nav className="px-2 space-y-1">
          {menuItems.map((item) => (
            <SidebarItem key={item.label} item={item} pathname={location.pathname} />
          ))}
        </nav>

        <SidebarSection label="Soporte" className="mt-8" />
        <nav className="px-2 space-y-1">
          {supportItems.map((item) => (
            <SidebarItem key={item.label} item={item} pathname={location.pathname} />
          ))}
        </nav>

        <div className="mt-auto p-4">
          <div className="flex items-center gap-3 p-3 rounded-lg bg-[#F9FAFB]">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center shrink-0">
              <span className="text-white text-xs font-bold">{initials}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-[13px] font-semibold text-zinc-900 truncate">
                {fullName}
              </p>
              <p className="text-[11px] text-zinc-500 capitalize">
                {user?.rol || "Usuario"}
              </p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              aria-label="Cerrar sesión"
              className="shrink-0 w-8 h-8 rounded-md text-zinc-500 hover:bg-zinc-100 hover:text-red-500 flex items-center justify-center transition"
              title="Cerrar sesión"
            >
              <IconLogout />
            </button>
          </div>
        </div>
      </aside>

      {/* CONTENT */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-[72px] shrink-0 bg-white border-b border-zinc-200 flex items-center px-4 sm:px-6 lg:px-10 gap-3 sm:gap-6">
          <button
            type="button"
            onClick={() => setSidebarOpen(true)}
            aria-label="Abrir menú"
            className="lg:hidden shrink-0 w-9 h-9 rounded-md text-zinc-700 hover:bg-zinc-100 flex items-center justify-center"
          >
            <IconMenu />
          </button>

          <div className="flex-1 min-w-0">
            <h1 className="text-lg sm:text-xl font-bold text-zinc-900 truncate">
              Hola, {user?.nombres || "Usuario"}
            </h1>
            <p className="text-[12px] sm:text-[13px] text-zinc-500 capitalize truncate">
              {today}
            </p>
          </div>

          {/* Barra de búsqueda CORREGIDA */}
          <div 
            onClick={() => window.location.href = "/buscar"}
            className="hidden xl:flex shrink-0 items-center gap-2 w-72 h-9 px-3 rounded-lg bg-zinc-100 text-sm text-zinc-400 cursor-pointer hover:bg-zinc-200 transition"
          >
            <IconSearch />
            <span className="truncate">Buscar tickets, soluciones…</span>
          </div>

          {/* Botón de notificaciones */}
          <button
            type="button"
            onClick={() => window.location.href = "/notificaciones"}
            className="relative shrink-0 w-9 h-9 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-700 hover:bg-zinc-200 transition"
            aria-label="Notificaciones"
          >
            <IconBell />
            <span className="pointer-events-none absolute top-0.5 right-0.5 w-2.5 h-2.5 rounded-full bg-red-500 ring-2 ring-white" />
          </button>

          <div className="shrink-0 w-9 h-9 rounded-full bg-primary flex items-center justify-center">
            <span className="text-white text-xs font-bold">{initials}</span>
          </div>

          <button
            type="button"
            onClick={handleLogout}
            aria-label="Cerrar sesión"
            className="shrink-0 hidden sm:inline text-sm text-zinc-500 hover:text-zinc-900 transition whitespace-nowrap"
          >
            Cerrar sesión
          </button>
        </header>

        {/* MAIN */}
        <main className="flex-1 overflow-y-auto">
          <div className="px-10 py-8 max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

function SidebarSection({ label, className = "" }) {
  return (
    <p
      className={`px-6 pt-2 pb-1 text-[11px] font-semibold tracking-wider uppercase text-zinc-400 ${className}`}
    >
      {label}
    </p>
  );
}

function SidebarItem({ item, pathname }) {
  const isActive = item.to && pathname === item.to;
  const baseClass =
    "flex items-center gap-3 px-3 h-10 rounded-lg text-sm font-medium transition relative";

  if (item.soon) {
    return (
      <button
        type="button"
        onClick={() =>
          alert(`"${item.label}" — próximamente. Esta sección está en desarrollo.`)
        }
        className={`${baseClass} w-full text-zinc-600 hover:bg-zinc-50`}
      >
        {item.icon && <span className="text-zinc-500">{item.icon}</span>}
        <span>{item.label}</span>
      </button>
    );
  }

  return (
    <NavLink
      to={item.to}
      end
      className={({ isActive: a }) =>
        `${baseClass} ${
          a
            ? "bg-primary-faint text-primary"
            : "text-zinc-600 hover:bg-zinc-50"
        }`
      }
    >
      {isActive && (
        <span className="absolute left-0 top-1.5 bottom-1.5 w-[3px] rounded-full bg-primary" />
      )}
      {item.icon && (
        <span className={isActive ? "text-primary" : "text-zinc-500"}>
          {item.icon}
        </span>
      )}
      <span>{item.label}</span>
    </NavLink>
  );
}

function IconHome() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6">
      <rect x="1" y="1" width="14" height="14" rx="2" />
      <line x1="1" y1="6" x2="15" y2="6" />
    </svg>
  );
}
function IconList() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6">
      <rect x="1" y="1" width="14" height="14" rx="2" />
      <line x1="4" y1="5" x2="12" y2="5" />
      <line x1="4" y1="9" x2="12" y2="9" />
      <line x1="4" y1="13" x2="9" y2="13" />
    </svg>
  );
}
function IconPlus() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.8">
      <circle cx="8" cy="8" r="6.5" />
      <line x1="8" y1="4.5" x2="8" y2="11.5" />
      <line x1="4.5" y1="8" x2="11.5" y2="8" />
    </svg>
  );
}
function IconBook() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6">
      <rect x="1" y="3" width="14" height="10" rx="1" />
      <line x1="3" y1="7" x2="13" y2="7" />
    </svg>
  );
}
function IconUser() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="8" cy="6" r="3" />
      <path d="M2 15 c0 -3 3 -5 6 -5 c3 0 6 2 6 5" />
    </svg>
  );
}
function IconSearch() {
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="6" cy="6" r="5" />
      <line x1="9.5" y1="9.5" x2="13" y2="13" />
    </svg>
  );
}
function IconMenu() {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
      <line x1="3" y1="6" x2="17" y2="6" />
      <line x1="3" y1="10" x2="17" y2="10" />
      <line x1="3" y1="14" x2="17" y2="14" />
    </svg>
  );
}
function IconX() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
      <line x1="4" y1="4" x2="12" y2="12" />
      <line x1="12" y1="4" x2="4" y2="12" />
    </svg>
  );
}
function IconLogout() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 4 V2 H2 V14 H9 V12" />
      <path d="M6 8 H14 M11 5 L14 8 L11 11" />
    </svg>
  );
}
function IconBell() {
  return (
    <svg
      className="shrink-0"
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
      <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
    </svg>
  );
}
function IconHelp() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="8" cy="8" r="6.5" />
      <path d="M8 11.5 v0 M8 7.5 v-2 M8 4.5 a1.5 1.5 0 0 1 1.5 1.5 c0 1 -0.5 1.5 -1.5 2" />
    </svg>
  );
}
function IconSettings() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="8" cy="8" r="2.5" />
      <path d="M13 8 a5 5 0 0 0 -0.5 -2.2 l1.5 -1.5 l-2 -2 l-1.5 1.5 a5 5 0 0 0 -2.2 -0.5 a5 5 0 0 0 -2.2 0.5 l-1.5 -1.5 l-2 2 l1.5 1.5 a5 5 0 0 0 -0.5 2.2 a5 5 0 0 0 0.5 2.2 l-1.5 1.5 l2 2 l1.5 -1.5 a5 5 0 0 0 2.2 0.5 a5 5 0 0 0 2.2 -0.5 l1.5 1.5 l2 -2 l-1.5 -1.5 a5 5 0 0 0 0.5 -2.2" />
    </svg>
  );
}