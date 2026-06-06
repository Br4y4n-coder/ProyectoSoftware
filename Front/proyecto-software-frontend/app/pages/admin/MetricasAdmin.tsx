import { useEffect, useState } from "react";

import apiFetch from "../../api/apiFetch";

export default function MetricasAdmin() {
  const [ticketsPorEstado, setTicketsPorEstado] = useState([]);
  const [ticketsPorPrioridad, setTicketsPorPrioridad] = useState([]);
  const [tiempoPromedio, setTiempoPromedio] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchMetricas = async () => {
      const token = localStorage.getItem('auth_token');
      
      if (!token) {
        setError("No hay sesion activa");
        setIsLoading(false);
        return;
      }
      
      try {
        const resEstado = await apiFetch(`/api/metrics/tickets-por-estado`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (resEstado.ok) {
          const data = await resEstado.json();
          setTicketsPorEstado(data?.data || []);
        }

        const resPrioridad = await apiFetch(`/api/metrics/tickets-por-prioridad`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (resPrioridad.ok) {
          const data = await resPrioridad.json();
          setTicketsPorPrioridad(data?.data || []);
        }

        const resTiempo = await apiFetch(`/api/metrics/tiempo-promedio-resolucion`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (resTiempo.ok) {
          const data = await resTiempo.json();
          setTiempoPromedio(data?.data);
        }

      } catch (error) {
        setError("Error de conexion");
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchMetricas();
  }, []);

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
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Metricas</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Estadisticas y metricas del sistema
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4">Tickets por Estado</h2>
          <div className="space-y-3">
            {ticketsPorEstado.length === 0 ? (
              <p className="text-zinc-500">No hay datos</p>
            ) : (
              ticketsPorEstado.map((item) => (
                <div key={item.estado} className="flex items-center justify-between">
                  <span className="capitalize text-zinc-600">{item.estado}</span>
                  <span className="font-bold text-zinc-900">{item.cantidad}</span>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4">Tickets por Prioridad</h2>
          <div className="space-y-3">
            {ticketsPorPrioridad.length === 0 ? (
              <p className="text-zinc-500">No hay datos</p>
            ) : (
              ticketsPorPrioridad.map((item) => (
                <div key={item.prioridad} className="flex items-center justify-between">
                  <span className={`capitalize px-2 py-1 rounded-full text-xs font-bold ${
                    item.prioridad === 'alta' ? 'bg-red-100 text-red-800' :
                    item.prioridad === 'media' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-green-100 text-green-800'
                  }`}>
                    {item.prioridad}
                  </span>
                  <span className="font-bold text-zinc-900">{item.cantidad}</span>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4">Tiempo Promedio de Resolucion</h2>
          {tiempoPromedio ? (
            <div>
              <p className="text-3xl font-bold text-primary">{tiempoPromedio.promedioHoras} horas</p>
              <p className="text-sm text-zinc-500 mt-2">Total tickets: {tiempoPromedio.totalTickets}</p>
            </div>
          ) : (
            <p className="text-zinc-500">No hay datos</p>
          )}
        </div>

        <div className="rounded-xl bg-primary-faint border border-primary-subtle p-6">
          <h2 className="text-lg font-semibold text-primary mb-4">Resumen General</h2>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span className="text-zinc-600">Total tickets por estado:</span>
              <span className="font-bold">{ticketsPorEstado.reduce((sum, item) => sum + item.cantidad, 0)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-zinc-600">Total tickets por prioridad:</span>
              <span className="font-bold">{ticketsPorPrioridad.reduce((sum, item) => sum + item.cantidad, 0)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}