import React, { useState } from "react";
import { useAuth } from "../contexts/AuthContext";

export default function Configuracion() {
  const { user } = useAuth();
  const [notificaciones, setNotificaciones] = useState(true);
  const [idioma, setIdioma] = useState("es");
  const [message, setMessage] = useState("");

  const handleSave = () => {
    setMessage("Configuración guardada correctamente");
    setTimeout(() => setMessage(""), 3000);
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-zinc-900 mb-2">Configuración</h1>
      <p className="text-zinc-500 mb-8">
        Personaliza tu experiencia en la plataforma.
      </p>

      {message && (
        <div className="mb-4 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
          {message}
        </div>
      )}

      <div className="rounded-xl bg-white border border-zinc-200 overflow-hidden">
        <div className="p-6 border-b border-zinc-100">
          <h2 className="text-lg font-semibold text-zinc-900">
            Preferencias Generales
          </h2>
        </div>

        <div className="p-6 space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium text-zinc-900">Notificaciones por correo</p>
              <p className="text-sm text-zinc-500">
                Recibe alertas sobre actualizaciones de tus tickets
              </p>
            </div>
            <button
              type="button"
              onClick={() => setNotificaciones(!notificaciones)}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition ${
                notificaciones ? "bg-primary" : "bg-gray-300"
              }`}
            >
              <span
                className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                  notificaciones ? "translate-x-6" : "translate-x-1"
                }`}
              />
            </button>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium text-zinc-900">Idioma</p>
              <p className="text-sm text-zinc-500">Selecciona tu idioma preferido</p>
            </div>
            <select
              value={idioma}
              onChange={(e) => setIdioma(e.target.value)}
              className="px-3 py-2 border border-zinc-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="es">Español</option>
              <option value="en">English</option>
            </select>
          </div>

          <div className="pt-4">
            <button
              type="button"
              onClick={handleSave}
              className="px-4 py-2 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition"
            >
              Guardar cambios
            </button>
          </div>
        </div>
      </div>

      <div className="mt-6 rounded-xl bg-red-50 border border-red-200 p-6">
        <h3 className="text-lg font-semibold text-red-900 mb-2">Zona de Peligro</h3>
        <p className="text-sm text-red-700 mb-4">
          Una vez que elimines tu cuenta, no podrás recuperar tus tickets ni información.
        </p>
        <button
          type="button"
          onClick={() => alert("Esta funcionalidad estará disponible próximamente.")}
          className="px-4 py-2 rounded-lg bg-red-600 text-white text-sm font-semibold hover:bg-red-700 transition"
        >
          Eliminar cuenta
        </button>
      </div>
    </div>
  );
}