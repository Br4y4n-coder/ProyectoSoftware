import React, { useState, useEffect } from "react";

import apiFetch from "../api/apiFetch";

export default function Reportes() {
  const [isLoading, setIsLoading] = useState(true);
  const [stats, setStats] = useState({
    totalTickets: 0,
    ticketsAbiertos: 0,
    ticketsResueltos: 0,
    ticketsEnProceso: 0,
    promedioResolucion: 0,
  });
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchStats = async () => {
      // Leer token con la clave correcta
      const token = localStorage.getItem('auth_token');
      console.log("=== Reportes: Token desde localStorage ===");
      console.log("Token existe?", token ? "Sí" : "No");
      
      if (!token) {
        console.log("No hay token en localStorage");
        setError("No hay sesión activa. Por favor, inicia sesión nuevamente.");
        setIsLoading(false);
        return;
      }
      
      try {
        setIsLoading(true);
        console.log("Haciendo petición a /api/tickets");
        
        const response = await apiFetch(`/api/tickets?page=0&size=100`, {
          headers: { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
        });
        
        console.log("Respuesta status:", response.status);
        
        if (response.ok) {
          const data = await response.json();
          console.log("Datos recibidos:", data);
          const tickets = data?.data?.content || [];
          console.log("Tickets encontrados:", tickets.length);
          
          const abiertos = tickets.filter(t => t.estado === "abierto").length;
          const resueltos = tickets.filter(t => t.estado === "cerrado").length;
          const enProceso = tickets.filter(t => t.estado === "en_proceso").length;
          
          setStats({
            totalTickets: tickets.length,
            ticketsAbiertos: abiertos,
            ticketsResueltos: resueltos,
            ticketsEnProceso: enProceso,
            promedioResolucion: resueltos > 0 ? Math.round(24 / resueltos * 10) / 10 : 0,
          });
        } else {
          const errorData = await response.text();
          console.error("Error response:", errorData);
          if (response.status === 401) {
            setError("Sesión expirada. Por favor, inicia sesión nuevamente.");
          } else {
            setError(`Error al cargar los datos: ${response.status}`);
          }
        }
      } catch (error) {
        console.error("Error en fetch:", error);
        setError("Error de conexión: " + error.message);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchStats();
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando reportes...</div>
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
      <h1 className="text-2xl font-bold text-zinc-900 mb-2">Reportes</h1>
      <p className="text-zinc-500 mb-8">
        Estadísticas y métricas de rendimiento
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <p className="text-sm text-zinc-500">Total Tickets</p>
          <p className="text-3xl font-bold text-zinc-900 mt-2">{stats.totalTickets}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <p className="text-sm text-zinc-500">Tickets Abiertos</p>
          <p className="text-3xl font-bold text-blue-600 mt-2">{stats.ticketsAbiertos}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <p className="text-sm text-zinc-500">Tickets Resueltos</p>
          <p className="text-3xl font-bold text-green-600 mt-2">{stats.ticketsResueltos}</p>
        </div>
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <p className="text-sm text-zinc-500">En Proceso</p>
          <p className="text-3xl font-bold text-amber-600 mt-2">{stats.ticketsEnProceso}</p>
        </div>
      </div>

      <div className="rounded-xl bg-white border border-zinc-200 p-6">
        <h2 className="text-lg font-semibold text-zinc-900 mb-4">Resumen</h2>
        <div className="space-y-3">
          <div className="flex justify-between items-center py-2 border-b border-zinc-100">
            <span className="text-zinc-600">Tasa de resolución</span>
            <span className="font-semibold text-zinc-900">
              {stats.totalTickets > 0 ? Math.round((stats.ticketsResueltos / stats.totalTickets) * 100) : 0}%
            </span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-zinc-100">
            <span className="text-zinc-600">Tiempo promedio de resolución</span>
            <span className="font-semibold text-zinc-900">{stats.promedioResolucion} horas</span>
          </div>
        </div>
      </div>
    </div>
  );
}