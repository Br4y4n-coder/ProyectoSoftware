import React, { useState, useEffect } from "react";

import apiFetch from "../../api/apiFetch";

export default function TicketsAdmin() {
  const [tickets, setTickets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchTickets = async () => {
      const token = localStorage.getItem('auth_token');
      
      if (!token) {
        setError("No hay sesión activa. Por favor, inicia sesión nuevamente.");
        setIsLoading(false);
        return;
      }
      
      try {
        const response = await apiFetch(`/api/tickets?page=0&size=100`);
        
        if (response.ok) {
          const data = await response.json();
          setTickets(data?.data?.content || []);
        } else {
          setError("Error al cargar los tickets");
        }
      } catch (error) {
        setError("Error de conexión");
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchTickets();
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando tickets...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="rounded-xl bg-red-50 border border-red-200 p-6 text-center">
          <p className="text-red-700">{error}</p>
          <button 
            onClick={() => window.location.href = "/auth/login"}
            className="mt-4 px-4 py-2 bg-primary text-white rounded-lg"
          >
            Ir a iniciar sesión
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="rounded-xl bg-white border border-zinc-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-[#F9FAFB]">
              <tr className="text-left text-[11px] font-semibold tracking-wider text-zinc-500 uppercase">
                <th className="px-6 py-3">ID</th>
                <th className="px-6 py-3">Asunto</th>
                <th className="px-6 py-3">Cliente</th>
                <th className="px-6 py-3">Estado</th>
                <th className="px-6 py-3">Prioridad</th>
                <th className="px-6 py-3">Fecha</th>
                <th className="px-6 py-3">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {tickets.map((ticket) => (
                <tr key={ticket.id} className="hover:bg-zinc-50/50">
                  <td className="px-6 py-3.5 font-semibold text-zinc-900">
                    #{ticket.codigo}
                  </td>
                  <td className="px-6 py-3.5 text-zinc-900">
                    {ticket.asunto}
                  </td>
                  <td className="px-6 py-3.5 text-zinc-500">
                    {ticket.clienteNombre}
                  </td>
                  <td className="px-6 py-3.5">
                    <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                      ticket.estado === 'abierto' ? 'bg-blue-100 text-blue-800' :
                      ticket.estado === 'en_proceso' ? 'bg-amber-100 text-amber-800' :
                      'bg-green-100 text-green-800'
                    }`}>
                      {ticket.estado?.toUpperCase()}
                    </span>
                  </td>
                  <td className="px-6 py-3.5">
                    <span className="capitalize">{ticket.prioridad}</span>
                  </td>
                  <td className="px-6 py-3.5 text-zinc-500">
                    {new Date(ticket.fechaCreacion).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-3.5">
                    <button className="text-primary hover:text-primary-light">
                      Ver detalles
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}