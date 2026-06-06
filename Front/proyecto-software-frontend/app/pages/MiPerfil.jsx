import React, { useState, useEffect } from "react";
import apiFetch from "../api/apiFetch";

import { useAuth } from "../contexts/AuthContext";

export default function MiPerfil() {
  const { user, token } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    nombres: "",
    apellidos: "",
    telefono: "",
    correo: "",
  });
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (user) {
      setFormData({
        nombres: user.nombres || "",
        apellidos: user.apellidos || "",
        telefono: user.telefono || "",
        correo: user.correo || "",
      });
    }
  }, [user]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await apiFetch("/api/usuarios/perfil", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(formData),
      });
      if (response.ok) {
        setMessage("Perfil actualizado correctamente");
        setIsEditing(false);
        setTimeout(() => setMessage(""), 3000);
      } else {
        setMessage("Error al actualizar el perfil");
      }
    } catch (error) {
      setMessage("Error de conexión");
    }
  };

  if (!user) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-zinc-900 mb-2">Mi Perfil</h1>
      <p className="text-zinc-500 mb-8">
        Gestiona tu información personal y preferencias de cuenta.
      </p>

      {message && (
        <div className="mb-4 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
          {message}
        </div>
      )}

      <div className="rounded-xl bg-white border border-zinc-200 overflow-hidden">
        <div className="p-6 border-b border-zinc-100">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-zinc-900">
              Información Personal
            </h2>
            {!isEditing && (
              <button
                type="button"
                onClick={() => setIsEditing(true)}
                className="px-4 py-2 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition"
              >
                Editar perfil
              </button>
            )}
          </div>
        </div>

        {isEditing ? (
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Nombres
              </label>
              <input
                type="text"
                name="nombres"
                value={formData.nombres}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-zinc-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Apellidos
              </label>
              <input
                type="text"
                name="apellidos"
                value={formData.apellidos}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-zinc-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Teléfono
              </label>
              <input
                type="tel"
                name="telefono"
                value={formData.telefono}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-zinc-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Correo electrónico
              </label>
              <input
                type="email"
                name="correo"
                value={formData.correo}
                className="w-full px-3 py-2 border border-zinc-300 rounded-lg bg-gray-50 text-gray-500"
                disabled
              />
              <p className="text-xs text-zinc-400 mt-1">
                El correo no se puede modificar
              </p>
            </div>
            <div className="flex gap-3 pt-4">
              <button
                type="submit"
                className="px-4 py-2 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition"
              >
                Guardar cambios
              </button>
              <button
                type="button"
                onClick={() => setIsEditing(false)}
                className="px-4 py-2 rounded-lg bg-gray-200 text-gray-700 text-sm font-semibold hover:bg-gray-300 transition"
              >
                Cancelar
              </button>
            </div>
          </form>
        ) : (
          <div className="p-6 space-y-4">
            <div className="grid grid-cols-3 gap-4">
              <div className="col-span-1">
                <p className="text-sm text-zinc-500">Nombres</p>
                <p className="text-sm font-medium text-zinc-900 mt-1">
                  {user.nombres || "-"}
                </p>
              </div>
              <div className="col-span-2">
                <p className="text-sm text-zinc-500">Apellidos</p>
                <p className="text-sm font-medium text-zinc-900 mt-1">
                  {user.apellidos || "-"}
                </p>
              </div>
            </div>
            <div>
              <p className="text-sm text-zinc-500">Teléfono</p>
              <p className="text-sm font-medium text-zinc-900 mt-1">
                {user.telefono || "No registrado"}
              </p>
            </div>
            <div>
              <p className="text-sm text-zinc-500">Correo electrónico</p>
              <p className="text-sm font-medium text-zinc-900 mt-1">
                {user.correo}
              </p>
            </div>
            <div>
              <p className="text-sm text-zinc-500">Rol</p>
              <p className="text-sm font-medium text-zinc-900 mt-1 capitalize">
                {user.rol || "Usuario"}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}