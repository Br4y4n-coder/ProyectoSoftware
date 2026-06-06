import { useEffect, useState } from "react";
import apiFetch from "../../api/apiFetch";

import { 
  Plug, 
  Webhook, 
  Mail, 
  MessageSquare, 
  Calendar, 
  Cloud, 
  Shield,
  X,
  Check
} from "lucide-react";

export default function IntegracionesAdmin() {
  const [integraciones, setIntegraciones] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");

  const fetchIntegraciones = async () => {
    const token = localStorage.getItem('auth_token');
    
    if (!token) {
      setError("No hay sesión activa");
      setIsLoading(false);
      return;
    }
    
    try {
      const response = await apiFetch(`/api/integraciones`);
      
      if (response.ok) {
        const data = await response.json();
        setIntegraciones(data?.data || []);
      } else {
        setError("Error al cargar integraciones");
      }
    } catch (error) {
      setError("Error de conexión");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchIntegraciones();
  }, []);

  const toggleConexion = async (id, conectado) => {
    const token = localStorage.getItem('auth_token');
    const accion = conectado ? "desconectar" : "conectar";
    
    try {
      const response = await apiFetch(`/api/integraciones/${id}/${accion}`, {
        method: "POST"
      });
      
      if (response.ok) {
        const data = await response.json();
        setIntegraciones(integraciones.map(integ => 
          integ.id === id ? data.data : integ
        ));
        setMensaje(`Integración ${accion}ada correctamente`);
        setTimeout(() => setMensaje(""), 3000);
      } else {
        setMensaje("Error al cambiar estado");
        setTimeout(() => setMensaje(""), 3000);
      }
    } catch (error) {
      setMensaje("Error de conexión");
      setTimeout(() => setMensaje(""), 3000);
    }
  };

  const getIcono = (tipo) => {
    const iconos = {
      email: <Mail className="w-8 h-8 text-blue-500" />,
      webhook: <Webhook className="w-8 h-8 text-purple-500" />,
      chat: <MessageSquare className="w-8 h-8 text-green-500" />,
      calendar: <Calendar className="w-8 h-8 text-red-500" />,
      api: <Cloud className="w-8 h-8 text-cyan-500" />,
      auth: <Shield className="w-8 h-8 text-indigo-500" />,
    };
    return iconos[tipo] || <Plug className="w-8 h-8 text-gray-500" />;
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
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Integraciones</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Conecta TicketHub con servicios externos
        </p>
      </div>

      {mensaje && (
        <div className="rounded-xl bg-green-50 border border-green-200 p-4">
          <p className="text-green-700 text-sm">{mensaje}</p>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {integraciones.map((integ) => (
          <div
            key={integ.id}
            className="rounded-xl bg-white border border-zinc-200 p-5 hover:shadow-md transition"
          >
            <div className="flex items-start justify-between mb-3">
              <div className="p-2 rounded-lg bg-zinc-50">
                {getIcono(integ.tipo)}
              </div>
              <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                integ.conectado 
                  ? "bg-green-100 text-green-800" 
                  : "bg-gray-100 text-gray-600"
              }`}>
                {integ.conectado ? "CONECTADO" : "DESCONECTADO"}
              </span>
            </div>
            
            <h3 className="text-base font-bold text-zinc-900 mb-1">
              {integ.nombre}
            </h3>
            <p className="text-xs text-zinc-500 mb-4">
              {integ.descripcion}
            </p>
            
            <button
              onClick={() => toggleConexion(integ.id, integ.conectado)}
              className={`w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition ${
                integ.conectado
                  ? "bg-red-50 text-red-600 hover:bg-red-100 border border-red-200"
                  : "bg-primary text-white hover:bg-primary-light"
              }`}
            >
              {integ.conectado ? (
                <>
                  <X className="w-3 h-3" />
                  Desconectar
                </>
              ) : (
                <>
                  <Check className="w-3 h-3" />
                  Conectar
                </>
              )}
            </button>
          </div>
        ))}
      </div>

      {/* Documentación */}
      <div className="rounded-xl bg-primary-faint border border-primary-subtle p-6">
        <div className="flex items-start gap-4">
          <div className="p-2 rounded-lg bg-primary text-white">
            <Plug className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-primary">API y Documentación</h3>
            <p className="text-sm text-zinc-600 mt-1">
              Consulta nuestra documentación para desarrolladores y aprende a integrar TicketHub con tus sistemas.
            </p>
            <button
              onClick={() => alert("Documentación - próximamente")}
              className="mt-3 text-sm text-primary hover:underline"
            >
              Ver documentación →
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}