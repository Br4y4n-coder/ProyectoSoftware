import React, { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";

export default function Notificaciones() {
  const { user } = useAuth();
  const [notificaciones, setNotificaciones] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Simular carga de notificaciones
    setTimeout(() => {
      setNotificaciones([
        {
          id: 1,
          titulo: "Ticket actualizado",
          mensaje: "Tu ticket #TKT-001 ha sido actualizado a estado 'En proceso'",
          fecha: new Date(),
          leido: false,
          tipo: "ticket",
        },
        {
          id: 2,
          titulo: "Nuevo mensaje",
          mensaje: "Un agente ha respondido a tu ticket #TKT-003",
          fecha: new Date(),
          leido: false,
          tipo: "mensaje",
        },
        {
          id: 3,
          titulo: "Ticket resuelto",
          mensaje: "Tu ticket #TKT-002 ha sido marcado como resuelto",
          fecha: new Date(Date.now() - 86400000),
          leido: true,
          tipo: "ticket",
        },
      ]);
      setIsLoading(false);
    }, 1000);
  }, []);

  const marcarComoLeido = (id) => {
    setNotificaciones(notificaciones.map(notif => 
      notif.id === id ? { ...notif, leido: true } : notif
    ));
  };

  const marcarTodasLeidas = () => {
    setNotificaciones(notificaciones.map(notif => ({ ...notif, leido: true })));
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando notificaciones...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-zinc-900">Notificaciones</h1>
        {notificaciones.some(n => !n.leido) && (
          <button
            type="button"
            onClick={marcarTodasLeidas}
            className="px-3 py-1.5 text-sm text-primary hover:text-primary-light font-medium"
          >
            Marcar todas como leídas
          </button>
        )}
      </div>

      {notificaciones.length === 0 ? (
        <div className="rounded-xl bg-white border border-zinc-200 p-12 text-center">
          <div className="text-6xl mb-4">🔔</div>
          <p className="text-zinc-500">No tienes notificaciones</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notificaciones.map((notif) => (
            <div
              key={notif.id}
              className={`rounded-xl border p-4 transition ${
                notif.leido
                  ? "bg-white border-zinc-200"
                  : "bg-blue-50 border-blue-200"
              }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="font-semibold text-zinc-900">
                      {notif.titulo}
                    </h3>
                    {!notif.leido && (
                      <span className="inline-block w-2 h-2 rounded-full bg-blue-500" />
                    )}
                  </div>
                  <p className="text-sm text-zinc-600 mb-2">
                    {notif.mensaje}
                  </p>
                  <p className="text-xs text-zinc-400">
                    {formatFecha(notif.fecha)}
                  </p>
                </div>
                {!notif.leido && (
                  <button
                    type="button"
                    onClick={() => marcarComoLeido(notif.id)}
                    className="text-xs text-primary hover:text-primary-light"
                  >
                    Marcar leído
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function formatFecha(fecha) {
  const ahora = new Date();
  const diff = ahora - fecha;
  const minutos = Math.floor(diff / 60000);
  const horas = Math.floor(diff / 3600000);
  const dias = Math.floor(diff / 86400000);

  if (minutos < 1) return "Justo ahora";
  if (minutos < 60) return `Hace ${minutos} minutos`;
  if (horas < 24) return `Hace ${horas} horas`;
  return `Hace ${dias} días`;
}