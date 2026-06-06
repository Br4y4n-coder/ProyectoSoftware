import { useState } from "react";
import { X } from "lucide-react";
import apiFetch from "../../api/apiFetch";

const FORM_INICIAL = {
  nombres: "",
  apellidos: "",
  correo: "",
  contrasena: "",
  rol: "usuario",
  tipoDocumento: "CC",
  numeroDocumento: "",
  telefono: "",
};

const inputClass =
  "w-full px-3 py-2 rounded-lg border border-zinc-300 text-sm focus:outline-none focus:ring-2 focus:ring-primary";
const labelClass = "block text-sm font-medium text-zinc-700 mb-1";

/**
 * Modal para que el administrador cree un usuario con cualquier rol.
 * Registra la cuenta, asigna el rol si no es "usuario" y la activa
 * de inmediato (sin esperar verificación de correo).
 */
export default function NuevoUsuarioModal({ isOpen, onClose, onSuccess }) {
  const [formData, setFormData] = useState(FORM_INICIAL);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [activarInmediato, setActivarInmediato] = useState(true);

  if (!isOpen) return null;

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError("");
  };

  const cerrar = () => {
    setFormData(FORM_INICIAL);
    setError("");
    onClose();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      // 1. Registrar la cuenta
      const registerResponse = await apiFetch(`/api/auth/register`, {
        method: "POST",
        body: JSON.stringify({
          correo: formData.correo.trim(),
          contrasena: formData.contrasena,
          nombres: formData.nombres.trim(),
          apellidos: formData.apellidos.trim(),
          tipoDocumento: formData.tipoDocumento,
          numeroDocumento: formData.numeroDocumento.trim(),
          telefono: formData.telefono.trim(),
        }),
      });

      const registerData = await registerResponse.json();
      if (!registerResponse.ok) {
        setError(registerData?.message || "Error al crear el usuario");
        return;
      }

      const usuarioId = registerData?.data?.id;
      const advertencias = [];

      // 2. Asignar rol (solo si es distinto al rol por defecto)
      if (usuarioId && formData.rol !== "usuario") {
        const rolResponse = await apiFetch(`/api/usuarios/${usuarioId}/rol`, {
          method: "PATCH",
          body: JSON.stringify({ rol: formData.rol }),
        });
        if (!rolResponse.ok) advertencias.push("no se pudo asignar el rol");
      }

      // 3. Activar la cuenta (el registro la deja en "pendiente")
      if (usuarioId && activarInmediato) {
        const estadoResponse = await apiFetch(`/api/usuarios/${usuarioId}/estado`, {
          method: "PATCH",
          body: JSON.stringify({ estado: "activo" }),
        });
        if (!estadoResponse.ok) advertencias.push("quedó pendiente de activación");
      }

      if (advertencias.length > 0) {
        setError(`Usuario creado, pero ${advertencias.join(" y ")}. Revísalo en la tabla.`);
        if (onSuccess) onSuccess();
        return;
      }

      setFormData(FORM_INICIAL);
      if (onSuccess) onSuccess();
      onClose();
    } catch (err) {
      setError("Error de conexión: " + err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={cerrar}
    >
      <div
        className="bg-white rounded-xl shadow-xl w-full max-w-md max-h-[90vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-200 shrink-0">
          <h2 className="text-lg font-bold text-zinc-900">Nuevo usuario</h2>
          <button
            type="button"
            onClick={cerrar}
            className="p-1 rounded-lg hover:bg-zinc-100 transition"
            aria-label="Cerrar"
          >
            <X className="w-5 h-5 text-zinc-400" />
          </button>
        </div>

        {/* Formulario con scroll propio */}
        <form onSubmit={handleSubmit} className="flex flex-col flex-1 min-h-0">
          <div className="px-6 py-5 space-y-4 overflow-y-auto flex-1">
            {error && (
              <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className={labelClass}>Nombres *</label>
                <input
                  type="text"
                  name="nombres"
                  value={formData.nombres}
                  onChange={handleChange}
                  required
                  autoComplete="off"
                  className={inputClass}
                />
              </div>
              <div>
                <label className={labelClass}>Apellidos *</label>
                <input
                  type="text"
                  name="apellidos"
                  value={formData.apellidos}
                  onChange={handleChange}
                  required
                  autoComplete="off"
                  className={inputClass}
                />
              </div>
            </div>

            <div>
              <label className={labelClass}>Correo electrónico *</label>
              <input
                type="email"
                name="correo"
                value={formData.correo}
                onChange={handleChange}
                required
                autoComplete="off"
                className={inputClass}
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className={labelClass}>Contraseña *</label>
                <input
                  type="password"
                  name="contrasena"
                  value={formData.contrasena}
                  onChange={handleChange}
                  required
                  minLength={8}
                  autoComplete="new-password"
                  className={inputClass}
                />
                <p className="text-[11px] text-zinc-400 mt-1">Mínimo 8 caracteres</p>
              </div>
              <div>
                <label className={labelClass}>Rol *</label>
                <select
                  name="rol"
                  value={formData.rol}
                  onChange={handleChange}
                  className={inputClass}
                >
                  <option value="usuario">Usuario</option>
                  <option value="agente">Agente</option>
                  <option value="administrador">Administrador</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className={labelClass}>Tipo documento</label>
                <select
                  name="tipoDocumento"
                  value={formData.tipoDocumento}
                  onChange={handleChange}
                  className={inputClass}
                >
                  <option value="CC">Cédula</option>
                  <option value="CE">Cédula Extranjería</option>
                  <option value="NIT">NIT</option>
                  <option value="PAS">Pasaporte</option>
                </select>
              </div>
              <div>
                <label className={labelClass}>Número documento</label>
                <input
                  type="text"
                  name="numeroDocumento"
                  value={formData.numeroDocumento}
                  onChange={handleChange}
                  autoComplete="off"
                  className={inputClass}
                />
              </div>
            </div>

            <div>
              <label className={labelClass}>Teléfono</label>
              <input
                type="tel"
                name="telefono"
                value={formData.telefono}
                onChange={handleChange}
                autoComplete="off"
                className={inputClass}
              />
            </div>

            <label className="flex items-center gap-2 text-sm text-zinc-600 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={activarInmediato}
                onChange={(e) => setActivarInmediato(e.target.checked)}
                className="rounded border-zinc-300"
              />
              Activar la cuenta inmediatamente (sin verificación de correo)
            </label>
          </div>

          {/* Footer */}
          <div className="flex gap-3 px-6 py-4 border-t border-zinc-200 shrink-0">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 py-2 rounded-lg bg-primary text-white font-semibold hover:opacity-90 transition disabled:opacity-50"
            >
              {isSubmitting ? "Creando..." : "Crear usuario"}
            </button>
            <button
              type="button"
              onClick={cerrar}
              className="flex-1 py-2 rounded-lg border border-zinc-300 text-zinc-700 font-semibold hover:bg-zinc-50 transition"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
