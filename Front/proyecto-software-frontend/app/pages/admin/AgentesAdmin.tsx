import { useEffect, useMemo, useState } from "react";
import apiFetch from "../../api/apiFetch";

import { Search, UserPlus, X, Edit, Save } from "lucide-react";
import { Badge } from "../../components/common/Badge";

export default function AgentesAdmin() {
  const [agentes, setAgentes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState("activos");
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [agenteEditando, setAgenteEditando] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState("");
  const [formData, setFormData] = useState({
    nombres: "",
    apellidos: "",
    correo: "",
    contrasena: "",
    tipoDocumento: "CC",
    numeroDocumento: "",
    telefono: "",
  });
  const [editFormData, setEditFormData] = useState({
    nombres: "",
    apellidos: "",
    telefono: "",
    estado: "",
  });

  const fetchAgentes = async () => {
    const token = localStorage.getItem('auth_token');
    
    if (!token) {
      setError("No hay sesión activa");
      setIsLoading(false);
      return;
    }
    
    try {
      const response = await apiFetch(`/api/usuarios?page=0&size=100`);
      
      if (response.ok) {
        const data = await response.json();
        const todos = data?.data?.content || [];
        const soloAgentes = todos.filter((u) => u.rol === "agente");
        setAgentes(soloAgentes);
      } else {
        setError("Error al cargar agentes");
      }
    } catch (error) {
      setError("Error de conexión");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAgentes();
  }, []);

  const filtered = useMemo(() => {
    return agentes.filter((a) => {
      if (statusFilter === "activos" && a.estado !== "activo") return false;
      if (search.trim()) {
        const q = search.toLowerCase();
        return (
          (a.nombres?.toLowerCase() || "").includes(q) ||
          (a.apellidos?.toLowerCase() || "").includes(q) ||
          a.correo.toLowerCase().includes(q)
        );
      }
      return true;
    });
  }, [statusFilter, search, agentes]);

  const stats = useMemo(() => {
    const total = agentes.length;
    const activos = agentes.filter((a) => a.estado === "activo").length;
    const inactivos = agentes.filter((a) => a.estado !== "activo").length;
    return { total, activos, inactivos };
  }, [agentes]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (formError) setFormError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFormError("");

    const token = localStorage.getItem('auth_token');

    try {
      const registerResponse = await apiFetch(`/api/auth/register`, {
        method: "POST",
        body: JSON.stringify({
          correo: formData.correo,
          contrasena: formData.contrasena,
          nombres: formData.nombres,
          apellidos: formData.apellidos,
          tipoDocumento: formData.tipoDocumento,
          numeroDocumento: formData.numeroDocumento,
          telefono: formData.telefono,
        }),
      });

      const registerData = await registerResponse.json();

      if (!registerResponse.ok) {
        setFormError(registerData?.message || "Error al crear el usuario");
        setIsSubmitting(false);
        return;
      }

      const usuarioId = registerData?.data?.id;

      const rolResponse = await apiFetch(`/api/usuarios/${usuarioId}/rol`, {
        method: "PATCH",
        body: JSON.stringify({ rol: "agente" }),
      });

      if (!rolResponse.ok) {
        setFormError("Usuario creado pero error al asignar rol de agente");
        setIsSubmitting(false);
        return;
      }

      setFormData({
        nombres: "",
        apellidos: "",
        correo: "",
        contrasena: "",
        tipoDocumento: "CC",
        numeroDocumento: "",
        telefono: "",
      });
      
      setIsModalOpen(false);
      setMensaje("Agente creado correctamente");
      fetchAgentes();
      setTimeout(() => setMensaje(""), 3000);
      
    } catch (err) {
      setFormError("Error de conexión: " + err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEditar = (agente) => {
    setAgenteEditando(agente);
    setEditFormData({
      nombres: agente.nombres,
      apellidos: agente.apellidos,
      telefono: agente.telefono || "",
      estado: agente.estado,
    });
    setIsEditModalOpen(true);
  };

  const handleEditChange = (e) => {
    setEditFormData({ ...editFormData, [e.target.name]: e.target.value });
  };

  const handleActualizar = async () => {
    const token = localStorage.getItem('auth_token');
    setIsSubmitting(true);
    
    try {
      const response = await apiFetch(`/api/usuarios/${agenteEditando.id}`, {
        method: "PUT",
        body: JSON.stringify({
          nombres: editFormData.nombres,
          apellidos: editFormData.apellidos,
          telefono: editFormData.telefono,
          estado: editFormData.estado,
        }),
      });
      
      if (response.ok) {
        setMensaje("Agente actualizado correctamente");
        setIsEditModalOpen(false);
        fetchAgentes();
        setTimeout(() => setMensaje(""), 3000);
      } else {
        setError("Error al actualizar agente");
        setTimeout(() => setError(""), 3000);
      }
    } catch (error) {
      setError("Error de conexión");
      setTimeout(() => setError(""), 3000);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleNuevoAgente = () => {
    setIsModalOpen(true);
  };

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
      {mensaje && (
        <div className="rounded-xl bg-green-50 border border-green-200 p-4">
          <p className="text-green-700 text-sm">{mensaje}</p>
        </div>
      )}

      {/* Modal de Nuevo Agente */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-200 sticky top-0 bg-white">
              <h2 className="text-xl font-bold text-zinc-900">Nuevo Agente</h2>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1 rounded-lg hover:bg-zinc-100 transition"
              >
                <X className="w-5 h-5 text-zinc-500" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {formError && (
                <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700">
                  {formError}
                </div>
              )}

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-zinc-700 mb-1">Nombres *</label>
                  <input
                    type="text"
                    name="nombres"
                    value={formData.nombres}
                    onChange={handleChange}
                    required
                    className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-zinc-700 mb-1">Apellidos *</label>
                  <input
                    type="text"
                    name="apellidos"
                    value={formData.apellidos}
                    onChange={handleChange}
                    required
                    className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Correo electrónico *</label>
                <input
                  type="email"
                  name="correo"
                  value={formData.correo}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Contraseña *</label>
                <input
                  type="password"
                  name="contrasena"
                  value={formData.contrasena}
                  onChange={handleChange}
                  required
                  minLength={8}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <p className="text-xs text-zinc-400 mt-1">Mínimo 8 caracteres</p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-zinc-700 mb-1">Tipo Documento</label>
                  <select
                    name="tipoDocumento"
                    value={formData.tipoDocumento}
                    onChange={handleChange}
                    className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="CC">Cédula</option>
                    <option value="CE">Cédula Extranjería</option>
                    <option value="NIT">NIT</option>
                    <option value="PAS">Pasaporte</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-zinc-700 mb-1">Número Documento</label>
                  <input
                    type="text"
                    name="numeroDocumento"
                    value={formData.numeroDocumento}
                    onChange={handleChange}
                    className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Teléfono</label>
                <input
                  type="tel"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex-1 py-2 rounded-lg bg-primary text-white font-semibold hover:bg-primary-light transition disabled:opacity-50"
                >
                  {isSubmitting ? "Creando..." : "Crear Agente"}
                </button>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 py-2 rounded-lg border border-zinc-300 text-zinc-700 font-semibold hover:bg-zinc-50 transition"
                >
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Editar Agente */}
      {isEditModalOpen && agenteEditando && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-200">
              <h2 className="text-xl font-bold text-zinc-900">Editar Agente</h2>
              <button
                onClick={() => setIsEditModalOpen(false)}
                className="p-1 rounded-lg hover:bg-zinc-100 transition"
              >
                <X className="w-5 h-5 text-zinc-500" />
              </button>
            </div>

            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-zinc-700 mb-1">Nombres</label>
                  <input
                    type="text"
                    name="nombres"
                    value={editFormData.nombres}
                    onChange={handleEditChange}
                    className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-zinc-700 mb-1">Apellidos</label>
                  <input
                    type="text"
                    name="apellidos"
                    value={editFormData.apellidos}
                    onChange={handleEditChange}
                    className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Teléfono</label>
                <input
                  type="tel"
                  name="telefono"
                  value={editFormData.telefono}
                  onChange={handleEditChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">Estado</label>
                <select
                  name="estado"
                  value={editFormData.estado}
                  onChange={handleEditChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                >
                  <option value="activo">Activo</option>
                  <option value="inactivo">Inactivo</option>
                  <option value="suspendido">Suspendido</option>
                </select>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  onClick={handleActualizar}
                  disabled={isSubmitting}
                  className="flex-1 py-2 rounded-lg bg-primary text-white font-semibold hover:bg-primary-light transition disabled:opacity-50"
                >
                  <Save className="w-4 h-4 inline mr-2" />
                  {isSubmitting ? "Guardando..." : "Guardar Cambios"}
                </button>
                <button
                  onClick={() => setIsEditModalOpen(false)}
                  className="flex-1 py-2 rounded-lg border border-zinc-300 text-zinc-700 font-semibold hover:bg-zinc-50 transition"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <header className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Gestión de Agentes</h1>
          <p className="text-sm text-zinc-500 mt-1">
            Administra los agentes de soporte del sistema
          </p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={handleNuevoAgente}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:opacity-90 transition"
          >
            <UserPlus className="w-4 h-4" /> Nuevo Agente
          </button>
        </div>
      </header>

      <section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
          <p className="text-[10px] font-semibold tracking-wider text-zinc-500">TOTAL AGENTES</p>
          <p className="mt-1 text-2xl font-bold text-zinc-900">{stats.total}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
          <p className="text-[10px] font-semibold tracking-wider text-zinc-500">AGENTES ACTIVOS</p>
          <p className="mt-1 text-2xl font-bold text-zinc-900">{stats.activos}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-4 shadow-sm">
          <p className="text-[10px] font-semibold tracking-wider text-zinc-500">AGENTES INACTIVOS</p>
          <p className="mt-1 text-2xl font-bold text-zinc-900">{stats.inactivos}</p>
        </div>
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
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="h-10 px-3 rounded-lg border border-zinc-200 text-sm bg-white"
        >
          <option value="activos">Estado: Activos</option>
          <option value="todos">Estado: Todos</option>
        </select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map((agente) => (
          <div key={agente.id} className="rounded-xl bg-white border border-zinc-200 p-4 hover:shadow-md transition">
            <div className="flex items-start justify-between mb-2">
              <div>
                <h3 className="font-bold text-zinc-900">{agente.nombres} {agente.apellidos}</h3>
                <p className="text-xs text-zinc-500 mt-1">{agente.correo}</p>
              </div>
              <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                agente.estado === 'activo' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              }`}>
                {agente.estado?.toUpperCase()}
              </span>
            </div>
            <div className="mt-3 pt-3 border-t border-zinc-100 flex justify-end">
              <button
                onClick={() => handleEditar(agente)}
                className="text-primary text-sm font-medium hover:underline flex items-center gap-1"
              >
                <Edit className="w-3 h-3" />
                Editar
              </button>
            </div>
          </div>
        ))}
      </div>

      {filtered.length === 0 && (
        <div className="rounded-xl bg-yellow-50 border border-yellow-200 p-6 text-center">
          <p className="text-yellow-700">No hay agentes que coincidan con los filtros</p>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status }) {
  const map = {
    activo: { label: "Activo", variant: "success" },
    pendiente: { label: "Pendiente", variant: "warning" },
    suspendido: { label: "Suspendido", variant: "danger" },
    eliminado: { label: "Eliminado", variant: "default" },
    inactivo: { label: "Inactivo", variant: "default" },
  };
  const { label, variant } = map[status] || { label: status, variant: "default" };
  return <Badge variant={variant}>{label}</Badge>;
}