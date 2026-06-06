import { useEffect, useMemo, useState } from "react";
import { Download, Plus, Search } from "lucide-react";
import { Badge } from "../../components/common/Badge";

interface Usuario {
  id: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  estado: string;
}

export default function UserManagement() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState<"all" | string>("all");
  const [statusFilter, setStatusFilter] = useState<"activos" | "todos">("activos");
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [cambiandoRol, setCambiandoRol] = useState<number | null>(null);

  const fetchUsuarios = async () => {
    const token = localStorage.getItem('auth_token');
    
    if (!token) {
      setError("No hay sesión activa");
      setIsLoading(false);
      return;
    }
    
    try {
      const response = await fetch(`http://localhost:8080/api/usuarios?page=0&size=100`, {
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        setUsuarios(data?.data?.content || []);
      } else {
        setError("Error al cargar usuarios");
      }
    } catch (error) {
      setError("Error de conexión");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsuarios();
  }, []);

  const cambiarRol = async (userId: number, nuevoRol: string) => {
    const token = localStorage.getItem('auth_token');
    setCambiandoRol(userId);
    
    try {
      const response = await fetch(`http://localhost:8080/api/usuarios/${userId}/rol`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ rol: nuevoRol })
      });
      
      if (response.ok) {
        setMensaje(`Rol cambiado a ${nuevoRol} correctamente`);
        fetchUsuarios();
        setTimeout(() => setMensaje(""), 3000);
      } else {
        alert('Error al cambiar el rol');
      }
    } catch (error) {
      alert('Error de conexión');
    } finally {
      setCambiandoRol(null);
    }
  };

  const filtered = useMemo(() => {
    return usuarios.filter((u) => {
      if (roleFilter !== "all" && u.rol !== roleFilter) return false;
      if (statusFilter === "activos" && u.estado !== "activo") return false;
      if (search.trim()) {
        const q = search.toLowerCase();
        return (
          (u.nombres?.toLowerCase() || "").includes(q) ||
          (u.apellidos?.toLowerCase() || "").includes(q) ||
          u.correo.toLowerCase().includes(q)
        );
      }
      return true;
    });
  }, [roleFilter, statusFilter, search, usuarios]);

  const stats = useMemo(() => {
    const total = usuarios.length;
    const activos = usuarios.filter(u => u.estado === "activo").length;
    const agentes = usuarios.filter(u => u.rol === "agente").length;
    const administradores = usuarios.filter(u => u.rol === "administrador").length;
    const pendientes = usuarios.filter(u => u.estado === "pendiente").length;
    return { total, activos, agentes, administradores, pendientes };
  }, [usuarios]);

  if (isLoading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  if (error) {
    return (
      <div className="rounded-xl bg-red-50 border border-red-200 p-6 text-center">
        <p className="text-red-700">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-primary text-white rounded-lg"
        >
          Reintentar
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <header className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Gestión de usuarios</h1>
          <p className="text-sm text-zinc-500 mt-1">
            Administra cuentas, roles y permisos del sistema
          </p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg border border-zinc-200 bg-white hover:bg-zinc-50 transition"
          >
            <Download className="w-4 h-4" /> EXPORTAR
          </button>
          <button
            type="button"
            onClick={() => alert("Nuevo usuario - próximamente")}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:opacity-90 transition"
          >
            <Plus className="w-4 h-4" /> Nuevo
          </button>
        </div>
      </header>

      {mensaje && (
        <div className="rounded-xl bg-green-50 border border-green-200 p-4">
          <p className="text-green-700 text-sm">{mensaje}</p>
        </div>
      )}

      <section className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <MiniStat label="USUARIOS TOTALES" value={stats.total} />
        <MiniStat label="ACTIVOS" value={stats.activos} />
        <MiniStat label="AGENTES" value={stats.agentes} />
        <MiniStat label="ADMINISTRADORES" value={stats.administradores} />
        <MiniStat label="PENDIENTES" value={stats.pendientes} highlight />
      </section>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1 flex items-center gap-2 h-10 px-3 rounded-lg bg-white border border-zinc-200">
          <Search className="w-4 h-4 text-zinc-400" />
          <input
            type="search"
            placeholder="Buscar por correo o nombre..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 text-sm outline-none bg-transparent"
          />
        </div>
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="h-10 px-3 rounded-lg border border-zinc-200 text-sm bg-white"
        >
          <option value="all">ROL: Todos</option>
          <option value="administrador">Administrador</option>
          <option value="agente">Agente</option>
          <option value="usuario">Usuario</option>
        </select>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as typeof statusFilter)}
          className="h-10 px-3 rounded-lg border border-zinc-200 text-sm bg-white"
        >
          <option value="activos">Estado: Activos</option>
          <option value="todos">Estado: Todos</option>
        </select>
      </div>

      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-[11px] uppercase tracking-wider text-zinc-400 bg-zinc-50">
                <th className="px-5 py-3 text-left">Correo</th>
                <th className="px-5 py-3 text-left">Nombre</th>
                <th className="px-5 py-3 text-left">Rol</th>
                <th className="px-5 py-3 text-left">Estado</th>
                <th className="px-5 py-3 text-left">Cambiar Rol</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {filtered.map((user) => (
                <tr key={user.id} className="hover:bg-zinc-50 transition">
                  <td className="px-5 py-3 text-zinc-600">{user.correo}</td>
                  <td className="px-5 py-3 font-medium text-zinc-900">
                    {user.nombres} {user.apellidos}
                  </td>
                  <td className="px-5 py-3">
                    <RoleBadge role={user.rol} />
                  </td>
                  <td className="px-5 py-3">
                    <StatusBadge status={user.estado} />
                  </td>
                  <td className="px-5 py-3">
                    <select
                      value={user.rol}
                      onChange={(e) => cambiarRol(user.id, e.target.value)}
                      disabled={cambiandoRol === user.id}
                      className="text-sm border border-zinc-300 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50"
                    >
                      <option value="usuario">Usuario</option>
                      <option value="agente">Agente</option>
                      <option value="administrador">Administrador</option>
                    </select>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-5 py-8 text-center text-zinc-500">
                    No hay usuarios que coincidan con los filtros
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function MiniStat({ label, value, highlight }: { label: string; value: number; highlight?: boolean }) {
  return (
    <div
      className={`rounded-xl border p-4 ${
        highlight
          ? "bg-amber-50 border-amber-200"
          : "bg-white border-zinc-200 shadow-sm"
      }`}
    >
      <p className="text-[10px] font-semibold tracking-wider text-zinc-500">{label}</p>
      <p className="mt-1 text-2xl font-bold text-zinc-900">{value}</p>
    </div>
  );
}

function RoleBadge({ role }: { role: string }) {
  const map: Record<string, { label: string; variant: "purple" | "info" | "default" }> = {
    administrador: { label: "Admin", variant: "purple" },
    agente: { label: "Agente", variant: "info" },
    usuario: { label: "Usuario", variant: "default" },
  };
  const { label, variant } = map[role] || { label: role, variant: "default" };
  return <Badge variant={variant}>{label}</Badge>;
}

function StatusBadge({ status }: { status: string }) {
  const map: Record<string, { label: string; variant: "success" | "warning" | "danger" | "default" }> = {
    activo: { label: "Activo", variant: "success" },
    pendiente: { label: "Pendiente", variant: "warning" },
    suspendido: { label: "Suspendido", variant: "danger" },
    eliminado: { label: "Eliminado", variant: "default" },
  };
  const { label, variant } = map[status] || { label: status, variant: "default" };
  return <Badge variant={variant}>{label}</Badge>;
}