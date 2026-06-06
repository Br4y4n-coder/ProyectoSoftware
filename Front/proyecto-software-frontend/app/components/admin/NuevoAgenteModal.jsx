import { useState } from "react";
import apiFetch from "../../api/apiFetch";

import { X } from "lucide-react";

export default function NuevoAgenteModal({ isOpen, onClose, onSuccess }) {
  const [formData, setFormData] = useState({
    nombres: "",
    apellidos: "",
    correo: "",
    contrasena: "",
    tipoDocumento: "CC",
    numeroDocumento: "",
    telefono: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError("");

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
        setError(registerData?.message || "Error al crear el usuario");
        setIsSubmitting(false);
        return;
      }

      const usuarioId = registerData?.data?.id;

      const rolResponse = await apiFetch(`/api/usuarios/${usuarioId}/rol`, {
        method: "PATCH",
        body: JSON.stringify({ rol: "agente" }),
      });

      if (!rolResponse.ok) {
        setError("Usuario creado pero error al asignar rol de agente");
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
      
      if (onSuccess) onSuccess();
      onClose();
      
    } catch (err) {
      setError("Error de conexión: " + err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 9999
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        width: '90%',
        maxWidth: '500px',
        maxHeight: '90vh',
        overflow: 'auto',
        boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: '16px 24px',
          borderBottom: '1px solid #e4e4e7',
          position: 'sticky',
          top: 0,
          backgroundColor: 'white',
          zIndex: 10
        }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#18181b' }}>Nuevo Agente</h2>
          <button
            onClick={onClose}
            style={{ padding: '4px', borderRadius: '8px', background: 'transparent', border: 'none', cursor: 'pointer' }}
          >
            <X size={20} color="#71717a" />
          </button>
        </div>

        <form onSubmit={handleSubmit} style={{ padding: '24px' }}>
          {error && (
            <div style={{ backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '8px', padding: '12px', marginBottom: '16px', color: '#b91c1c', fontSize: '14px' }}>
              {error}
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Nombres *</label>
              <input
                type="text"
                name="nombres"
                value={formData.nombres}
                onChange={handleChange}
                required
                style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Apellidos *</label>
              <input
                type="text"
                name="apellidos"
                value={formData.apellidos}
                onChange={handleChange}
                required
                style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
              />
            </div>
          </div>

          <div style={{ marginBottom: '16px' }}>
            <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Correo electrónico *</label>
            <input
              type="email"
              name="correo"
              value={formData.correo}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
            />
          </div>

          <div style={{ marginBottom: '16px' }}>
            <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Contraseña *</label>
            <input
              type="password"
              name="contrasena"
              value={formData.contrasena}
              onChange={handleChange}
              required
              minLength={8}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
            />
            <p style={{ fontSize: '11px', color: '#a1a1aa', marginTop: '4px' }}>Mínimo 8 caracteres</p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Tipo Documento</label>
              <select
                name="tipoDocumento"
                value={formData.tipoDocumento}
                onChange={handleChange}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
              >
                <option value="CC">Cédula</option>
                <option value="CE">Cédula Extranjería</option>
                <option value="NIT">NIT</option>
                <option value="PAS">Pasaporte</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Número Documento</label>
              <input
                type="text"
                name="numeroDocumento"
                value={formData.numeroDocumento}
                onChange={handleChange}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
              />
            </div>
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#3f3f46', marginBottom: '4px' }}>Teléfono</label>
            <input
              type="tel"
              name="telefono"
              value={formData.telefono}
              onChange={handleChange}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d4d4d8', outline: 'none' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '12px', paddingTop: '16px', borderTop: '1px solid #e4e4e7' }}>
            <button
              type="submit"
              disabled={isSubmitting}
              style={{ flex: 1, padding: '10px', borderRadius: '8px', backgroundColor: '#2563eb', color: 'white', fontWeight: '600', border: 'none', cursor: 'pointer' }}
            >
              {isSubmitting ? "Creando..." : "Crear Agente"}
            </button>
            <button
              type="button"
              onClick={onClose}
              style={{ flex: 1, padding: '10px', borderRadius: '8px', backgroundColor: 'white', border: '1px solid #d4d4d8', color: '#3f3f46', fontWeight: '600', cursor: 'pointer' }}
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}