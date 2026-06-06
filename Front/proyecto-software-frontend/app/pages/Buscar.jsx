import React, { useState } from "react";
import apiFetch from "../api/apiFetch";

import { useAuth } from "../contexts/AuthContext";
import { Link } from "react-router";

export default function Buscar() {
  const { token } = useAuth();
  const [termino, setTermino] = useState("");
  const [resultados, setResultados] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [buscado, setBuscado] = useState(false);

  const handleBuscar = async (e) => {
    e.preventDefault();
    if (!termino.trim()) return;

    setIsLoading(true);
    setBuscado(true);

    try {
      const response = await apiFetch(`/api/tickets/mios?page=0&size=100`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        const tickets = data?.data?.content || [];
        
        const filtrados = tickets.filter(ticket => 
          ticket.codigo?.toLowerCase().includes(termino.toLowerCase()) ||
          ticket.asunto?.toLowerCase().includes(termino.toLowerCase()) ||
          ticket.descripcion?.toLowerCase().includes(termino.toLowerCase()) ||
          ticket.estado?.toLowerCase().includes(termino.toLowerCase())
        );
        setResultados(filtrados);
      }
    } catch (error) {
      console.error("Error al buscar:", error);
      setResultados([]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-2xl font-bold text-zinc-900 mb-2">Búsqueda</h1>
      <p className="text-zinc-500 mb-6">
        Busca tickets por código, asunto, descripción o estado.
      </p>

      <form onSubmit={handleBuscar} className="mb-8">
        <div className="flex gap-3">
          <input
            type="text"
            value={termino}
            onChange={(e) => setTermino(e.target.value)}
            placeholder="Ej: TKT-001, error, resuelto..."
            className="flex-1 px-4 py-3 border border-zinc-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
            autoFocus
          />
          <button
            type="submit"
            className="px-6 py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primary-light transition"
          >
            Buscar
          </button>
        </div>
      </form>

      {buscado && (
        <>
          {isLoading ? (
            <div className="text-center py-12">
              <div className="text-gray-500">Buscando...</div>
            </div>
          ) : resultados.length === 0 ? (
            <div className="rounded-xl bg-yellow-50 border border-yellow-200 p-6 text-center">
              <p className="text-yellow-700">
                No se encontraron tickets para "{termino}"
              </p>
            </div>
          ) : (
            <div>
              <p className="text-sm text-zinc-500 mb-4">
                Se encontraron {resultados.length} resultado(s)
              </p>
              <div className="space-y-3">
                {resultados.map((ticket) => (
                  <Link
                    key={ticket.id}
                    to={`/ticket/${ticket.id}`}
                    className="block rounded-xl bg-white border border-zinc-200 p-4 hover:shadow-md transition"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-semibold text-primary">
                        #{ticket.codigo || `TKT-${ticket.id}`}
                      </span>
                      <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                        ticket.estado === 'abierto' ? 'bg-blue-100 text-blue-800' :
                        ticket.estado === 'en_proceso' ? 'bg-amber-100 text-amber-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        {ticket.estado?.toUpperCase()}
                      </span>
                    </div>
                    <h3 className="font-semibold text-zinc-900 mb-1">
                      {ticket.asunto}
                    </h3>
                    <p className="text-sm text-zinc-500 line-clamp-2">
                      {ticket.descripcion}
                    </p>
                    <div className="flex items-center gap-4 mt-3 text-xs text-zinc-400">
                      <span>Prioridad: {ticket.prioridad}</span>
                      <span>Fecha: {new Date(ticket.fechaCreacion).toLocaleDateString()}</span>
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}