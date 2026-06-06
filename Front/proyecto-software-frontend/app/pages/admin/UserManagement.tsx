import { useEffect, useMemo, useState } from "react";
import apiFetch from "../../api/apiFetch";
import { Plus, Search, Upload, X } from "lucide-react";
import NuevoAgenteModal from "../../components/admin/NuevoAgenteModal";

interface Usuario {
  id: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  estado: string;
  area?: string | null;
  nivelAgente?: number | null;
  ultimoAcceso?: string | null;
}

const AVATAR_COLORS = [
  "bg-amber-500",
  "bg-blue-500",
  "bg-emerald-500",
  "bg-violet-500",
  "bg-pink-500",
  "bg-teal-500",
  "bg-indigo-500",
];

function avatarColor(id: number) {
  return AVATAR_COLORS[id % AVATAR_COLORS.length];
}

function iniciales(u: Usuario) {
  const n = u.nombres?.[0] ?? "";
  const a = u.apellidos?.[0] ?? "";
  return `${n}${a}`.toUpperCase() || "?";
}

function formatUltimoAcceso(fecha?: string | null): string {
  if (!fecha) return "Sin acceso";
  const d = new Date(fecha);
  if (isNaN(d.getTime())) return "Sin acceso";
  const ahora = new Date();
  const diffMin = Math.round((ahora.getTime() - d.getTime()) / 60000);
  if (diffMin < 60) return `Hace ${Math.max(1, diffMin)} min`;
  const esHoy =
    d.getFullYear() === ahora.getFullYear() &&
    d.getMonth() === ahora.getMonth() &&
    d.getDate() === ahora.getDate();
  if (esHoy) {
    return `Hoy ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
  }
  const diffDias = Math.floor(diffMin / 1440);
  if (diffDias <= 1) return "Ayer";
  if (diffDias < 30) return `Hace ${diffDias} días`;
  const meses = Math.floor(diffDias / 30);
  return `Hace ${meses} ${meses === 1 ? "mes" : "meses"}`;
}

export default function UserManagement() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("activo");
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [seleccionados, setSeleccionados] = useState<Set<number>>(new Set());
  const [accionEnCurso, setAccionEnCurso] = useState<number | null>(null);
  const [editando, setEditando] = useState<Usuario | null>(null);
  const [rolEditado, setRolEditado] = useState("");
  const [modalNuevo, setModalNuevo] = useState(false);

  const fetchUsuarios = async () => {
    try {
      const response = await apiFetch(`/api/usuarios?page=0&size=200`);
      if (response.ok) {
        const data = await response.json();
        setUsuarios(data?.data?.content || []);
        setError("");
      } else {
        setError("Error al cargar usuarios");
      }
    } catch {
      setError("Error de conexión");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsuarios();
  }, []);

  const flash = (msg: string) => {
    setMensaje(msg);
    setTimeout(() => setMensaje(""), 3000);
  };

  const cambiarEstado = async (user: Usuario, nuevoEstado: string, confirmar?: string) => {
    if (confirmar && !window.confirm(confirmar)) return;
    setAccionEnCurso(user.id);
    try {
      const response = await apiFetch(`/api/usuarios/${user.id}/estado`, {
        method: "PATCH",
        body: JSON.stringify({ estado: nuevoEstado }),
      });
      if (response.ok) {
        flash(`Estado de ${user.nombres} actualizado a ${nuevoEstado}`);
        fetchUsuarios();
      } else {
        const data = await response.json();
        setError(data?.message || "Error al cambiar el estado");
        setTimeout(() => setError(""), 4000);
      }
    } catch {
      setError("Error de conexión");
      setTimeout(() => setError(""), 4000);
    } finally {
      setAccionEnCurso(null);
    }
  };

  const guardarRol = async () => {
    if (!editando || !rolEditado || rolEditado === editando.rol) {
      setEditando(null);
      return;
    }
    setAccionEnCurso(editando.id);
    try {
      const response = await apiFetch(`/api/usuarios/${editando.id}/rol`, {
        method: "PATCH",
        body: JSON.stringify({ rol: rolEditado }),
      });
      if (response.ok) {
        flash(`Rol de ${editando.nombres} cambiado a ${rolEditado}`);
        fetchUsuarios();
      } else {
        const data = await response.json();
        setError(data?.message || "Error al cambiar el rol");
        setTimeout(() => setError(""), 4000);
      }
    } catch {
      setError("Error de conexión");
      setTimeout(() => setError(""), 4000);
    } finally {
      setAccionEnCurso(null);
      setEditando(null);
    }
  };

  const toggleSeleccion = (id: number) => {
    setSeleccionados((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const filtered = useMemo(() => {
    return usuarios.filter((u) => {
      if (u.estado === "eliminado") return false;
      if (roleFilter !== "all" && u.rol !== roleFilter) return false;
      if (statusFilter !== "todos" && u.estado !== statusFilter) return false;
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
    const visibles = usuarios.filter((u) => u.estado !== "eliminado");
    return {
      total: visibles.length,
      agentesActivos: visibles.filter((u) => u.rol === "agente" && u.estado === "activo").length,
      administradores: visibles.filter((u) => u.rol === "administrador").length,
      pendientes: visibles.filter((u) => u.estado === "pendiente").length,
    };
  }, [usuarios]);

  if (isLoading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {mensaje && (
        <div className="rounded-xl bg-green-50 border border-green-200 p-4">
          <p className="text-green-700 text-sm">{mensaje}</p>
        </div>
      )}
      {error && (
        <div className="rounded-xl bg-red-50 border border-red-200 p-4">
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      {/* KPIs */}
      <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
          <p className="text-[10px] font-semibold tracking-widest text-zinc-400">
            USUARIOS TOTALES
          </p>
          <p className="mt-1 text-2xl font-bold text-zinc-900">{stats.total}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
          <p className="text-[10px] font-semibold tracking-widest text-zinc-400">
            AGENTES ACTIVOS
          </p>
          <p className="mt-1 text-2xl font-bold text-blue-500">{stats.agentesActivos}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
          <p className="text-[10px] font-semibold tracking-widest text-zinc-400">
            ADMINISTRADORES
          </p>
          <p className="mt-1 text-2xl font-bold text-amber-500">{stats.administradores}</p>
        </div>
        <div className="rounded-xl bg-primary-faint border border-primary-subtle p-4 flex items-center justify-between gap-3">
          <div>
            <p className="text-[10px] font-semibold tracking-widest text-primary">
              PENDIENTES DE VERIFICACIÓN
            </p>
            <p className="mt-1 text-2xl font-bold text-primary">{stats.pendientes}</p>
          </div>
          <button
            type="button"
            onClick={() => {
              setStatusFilter("pendiente");
              setRoleFilter("all");
            }}
            className="shrink-0 px-3 py-1.5 text-xs font-semibold rounded-lg bg-primary text-white hover:opacity-90 transition"
          >
            Revisar →
          </button>
        </div>
      </section>

      {/* Toolbar */}
      <div className="rounded-xl bg-white border border-zinc-200 p-3 shadow-sm flex flex-col lg:flex-row gap-3 lg:items-center">
        <div className="flex-1 flex items-center gap-2 h-10 px-3 rounded-lg bg-zinc-50 border border-zinc-200">
          <Search className="w-4 h-4 text-zinc-400" />
          <input
            type="search"
            placeholder="Buscar por nombre o correo..."
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
          <option value="all">Rol: Todos</option>
          <option value="administrador">Administrador</option>
          <option value="agente">Agente</option>
          <option value="usuario">Usuario</option>
        </select>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="h-10 px-3 rounded-lg border border-zinc-200 text-sm bg-white"
        >
          <option value="activo">Estado: Activos</option>
          <option value="todos">Estado: Todos</option>
          <option value="pendiente">Estado: Pendientes</option>
          <option value="suspendido">Estado: Suspendidos</option>
        </select>
        <div className="flex gap-2 lg:ml-auto">
          <button
            type="button"
            onClick={() => flash("Importar CSV estará disponible próximamente")}
            className="inline-flex items-center gap-2 h-10 px-4 text-sm font-medium rounded-lg border border-zinc-200 bg-white hover:bg-zinc-50 transition"
          >
            <Upload className="w-4 h-4" /> Importar CSV
          </button>
          <button
            type="button"
            onClick={() => setModalNuevo(true)}
            className="inline-flex items-center gap-2 h-10 px-4 text-sm font-semibold rounded-lg bg-primary text-white hover:opacity-90 transition"
          >
            <Plus className="w-4 h-4" /> Nuevo
          </button>
        </div>
      </div>

      {/* Tabla */}
      <div className="rounded-xl bg-white border border-zinc-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-[10px] font-semibold tracking-widest text-zinc-400 text-left border-b border-zinc-100">
                <th className="pl-5 pr-2 py-3 w-10">
                  <input
                    type="checkbox"
                    className="rounded border-zinc-300"
                    checked={filtered.length > 0 && seleccionados.size === filtered.length}
                    onChange={() =>
                      setSeleccionados(
                        seleccionados.size === filtered.length
                          ? new Set()
                          : new Set(filtered.map((u) => u.id))
                      )
                    }
                  />
                </th>
                <th className="px-2 py-3">USUARIO</th>
                <th className="px-4 py-3">CORREO</th>
                <th className="px-4 py-3">ROL</th>
                <th className="px-4 py-3">ESTADO</th>
                <th className="px-4 py-3">ÚLTIMO ACCESO</th>
                <th className="px-4 py-3 text-right pr-5">ACCIONES</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-50">
              {filtered.map((user) => {
                const suspendido = user.estado === "suspendido";
                return (
                  <tr
                    key={user.id}
                    className={`hover:bg-zinc-50 transition ${suspendido ? "opacity-60" : ""}`}
                  >
                    <td className="pl-5 pr-2 py-3">
                      <input
                        type="checkbox"
                        className="rounded border-zinc-300"
                        checked={seleccionados.has(user.id)}
                        onChange={() => toggleSeleccion(user.id)}
                      />
                    </td>
                    <td className="px-2 py-3">
                      <div className="flex items-center gap-3">
                        <span
                          className={`w-8 h-8 rounded-full ${suspendido ? "bg-zinc-300" : avatarColor(user.id)} text-white text-[11px] font-bold flex items-center justify-center shrink-0`}
                        >
                          {iniciales(user)}
                        </span>
                        <div className="min-w-0">
                          <p className="font-semibold text-zinc-900 truncate">
                            {user.nombres} {user.apellidos}
                          </p>
                          <p className="text-[11px] text-zinc-400 truncate">
                            {user.correo}
                            {user.area ? ` · ${user.area}` : ""}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-zinc-600">{user.correo}</td>
                    <td className="px-4 py-3">
                      <RolBadge rol={user.rol} />
                    </td>
                    <td className="px-4 py-3">
                      <EstadoBadge estado={user.estado} />
                    </td>
                    <td className="px-4 py-3 text-zinc-500">
                      {formatUltimoAcceso(user.ultimoAcceso)}
                    </td>
                    <td className="px-4 py-3 pr-5 text-right whitespace-nowrap">
                      <Acciones
                        user={user}
                        disabled={accionEnCurso === user.id}
                        onEditar={() => {
                          setEditando(user);
                          setRolEditado(user.rol);
                        }}
                        onEstado={cambiarEstado}
                      />
                    </td>
                  </tr>
                );
              })}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-5 py-10 text-center text-zinc-400">
                    No hay usuarios que coincidan con los filtros
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <p className="text-sm text-zinc-400">
        Mostrando 1-{filtered.length} de {stats.total} usuarios
      </p>

      {/* Modal editar rol */}
      {editando && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-sm mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-200">
              <h2 className="text-lg font-bold text-zinc-900">Editar usuario</h2>
              <button
                onClick={() => setEditando(null)}
                className="p-1 rounded-lg hover:bg-zinc-100 transition"
              >
                <X className="w-5 h-5 text-zinc-500" />
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <p className="font-semibold text-zinc-900">
                  {editando.nombres} {editando.apellidos}
                </p>
                <p className="text-sm text-zinc-500">{editando.correo}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Rol</label>
                <select
                  value={rolEditado}
                  onChange={(e) => setRolEditado(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                >
                  <option value="usuario">Usuario</option>
                  <option value="agente">Agente</option>
                  <option value="administrador">Administrador</option>
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  onClick={guardarRol}
                  disabled={accionEnCurso === editando.id}
                  className="flex-1 py-2 rounded-lg bg-primary text-white font-semibold hover:opacity-90 transition disabled:opacity-50"
                >
                  Guardar
                </button>
                <button
                  onClick={() => setEditando(null)}
                  className="flex-1 py-2 rounded-lg border border-zinc-300 text-zinc-700 font-semibold hover:bg-zinc-50 transition"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <NuevoAgenteModal
        isOpen={modalNuevo}
        onClose={() => setModalNuevo(false)}
        onSuccess={() => {
          setModalNuevo(false);
          flash("Usuario creado correctamente");
          fetchUsuarios();
        }}
      />
    </div>
  );
}

function RolBadge({ rol }: { rol: string }) {
  const map: Record<string, string> = {
    administrador: "bg-amber-100 text-amber-700",
    agente: "bg-blue-100 text-blue-700",
    usuario: "bg-zinc-100 text-zinc-600",
  };
  const label: Record<string, string> = {
    administrador: "ADMIN",
    agente: "AGENTE",
    usuario: "USUARIO",
  };
  return (
    <span
      className={`inline-block px-2.5 py-1 rounded-full text-[10px] font-bold tracking-wider ${map[rol] || "bg-zinc-100 text-zinc-600"}`}
    >
      {label[rol] || rol?.toUpperCase()}
    </span>
  );
}

function EstadoBadge({ estado }: { estado: string }) {
  if (estado === "activo") {
    return (
      <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-700">
        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" /> Activo
      </span>
    );
  }
  const map: Record<string, string> = {
    pendiente: "bg-amber-100 text-amber-700",
    suspendido: "bg-red-100 text-red-700",
    rechazado: "bg-zinc-200 text-zinc-600",
  };
  return (
    <span
      className={`inline-block px-2.5 py-1 rounded-full text-[10px] font-bold tracking-wider ${map[estado] || "bg-zinc-100 text-zinc-600"}`}
    >
      {estado?.toUpperCase()}
    </span>
  );
}

function Acciones({
  user,
  disabled,
  onEditar,
  onEstado,
}: {
  user: Usuario;
  disabled: boolean;
  onEditar: () => void;
  onEstado: (user: Usuario, estado: string, confirmar?: string) => void;
}) {
  const linkClass =
    "text-primary text-xs font-medium hover:underline disabled:opacity-50";
  const sep = <span className="mx-1.5 text-zinc-300">·</span>;

  if (user.estado === "pendiente") {
    return (
      <>
        <button
          disabled={disabled}
          onClick={() => onEstado(user, "activo")}
          className="text-emerald-600 text-xs font-medium hover:underline disabled:opacity-50"
        >
          Aprobar
        </button>
        {sep}
        <button
          disabled={disabled}
          onClick={() =>
            onEstado(user, "rechazado", `¿Rechazar la cuenta de ${user.nombres}?`)
          }
          className="text-red-500 text-xs font-medium hover:underline disabled:opacity-50"
        >
          Rechazar
        </button>
      </>
    );
  }

  if (user.estado === "suspendido" || user.estado === "rechazado") {
    return (
      <>
        <button disabled={disabled} onClick={() => onEstado(user, "activo")} className={linkClass}>
          Reactivar
        </button>
        {sep}
        <button
          disabled={disabled}
          onClick={() =>
            onEstado(user, "eliminado", `¿Eliminar definitivamente a ${user.nombres}?`)
          }
          className="text-red-500 text-xs font-medium hover:underline disabled:opacity-50"
        >
          Borrar
        </button>
      </>
    );
  }

  return (
    <>
      <button disabled={disabled} onClick={onEditar} className={linkClass}>
        Editar
      </button>
      {sep}
      <button
        disabled={disabled}
        onClick={() =>
          onEstado(user, "suspendido", `¿Suspender la cuenta de ${user.nombres}?`)
        }
        className={linkClass}
      >
        Suspender
      </button>
    </>
  );
}
