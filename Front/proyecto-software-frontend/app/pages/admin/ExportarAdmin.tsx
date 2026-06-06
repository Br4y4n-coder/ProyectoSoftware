import { useState } from "react";
import { Download, FileSpreadsheet, FileText, FileJson, Calendar, Filter } from "lucide-react";

export default function ExportarAdmin() {
  const [tipoExportacion, setTipoExportacion] = useState("tickets");
  const [formato, setFormato] = useState("csv");
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");
  const [isExportando, setIsExportando] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const getUrl = () => {
    let endpoint = "";
    switch (tipoExportacion) {
      case "tickets":
        endpoint = `/api/exportar/tickets/${formato}`;
        break;
      case "usuarios":
        endpoint = `/api/exportar/usuarios/${formato}`;
        break;
      case "auditoria":
        endpoint = `/api/exportar/auditoria/${formato}`;
        break;
      default:
        endpoint = `/api/exportar/tickets/${formato}`;
    }
    return endpoint;
  };

  const handleExportar = async () => {
    setIsExportando(true);
    setMensaje("");
    setError("");
    
    const token = localStorage.getItem('auth_token');
    const url = getUrl();
    
    try {
      const response = await fetch(`http://localhost:8080${url}`, {
        headers: { 
          'Authorization': `Bearer ${token}`
        },
      });
      
      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }
      
      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      
      let extension = formato;
      if (formato === "excel") extension = "xlsx";
      
      link.download = `${tipoExportacion}_${new Date().toISOString().slice(0,19).replace(/:/g, '-')}.${extension}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(downloadUrl);
      
      setMensaje(`Exportación de ${tipoExportacion} completada. Archivo descargado.`);
      setTimeout(() => setMensaje(""), 5000);
    } catch (err) {
      setError("Error al exportar: " + err.message);
      setTimeout(() => setError(""), 5000);
    } finally {
      setIsExportando(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Exportar Datos</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Exporta datos del sistema en diferentes formatos
        </p>
      </div>

      {mensaje && (
        <div className="rounded-xl bg-green-50 border border-green-200 p-4">
          <p className="text-green-700 text-sm">{mensaje}</p>
        </div>
      )}

      {error && (
        <div className="rounded-xl bg-red-50 border border-red-200 p-4">
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Panel de configuración */}
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4 flex items-center gap-2">
            <Filter className="w-5 h-5 text-primary" />
            Configuración de exportación
          </h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Tipo de datos
              </label>
              <select
                value={tipoExportacion}
                onChange={(e) => setTipoExportacion(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="tickets">Tickets</option>
                <option value="usuarios">Usuarios</option>
                <option value="auditoria">Auditoría</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Formato de exportación
              </label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  onClick={() => setFormato("csv")}
                  className={`flex items-center justify-center gap-2 px-3 py-2 rounded-lg border transition ${
                    formato === "csv"
                      ? "bg-primary text-white border-primary"
                      : "bg-white border-zinc-300 text-zinc-700 hover:bg-zinc-50"
                  }`}
                >
                  <FileSpreadsheet className="w-4 h-4" />
                  CSV
                </button>
                <button
                  type="button"
                  onClick={() => setFormato("json")}
                  className={`flex items-center justify-center gap-2 px-3 py-2 rounded-lg border transition ${
                    formato === "json"
                      ? "bg-primary text-white border-primary"
                      : "bg-white border-zinc-300 text-zinc-700 hover:bg-zinc-50"
                  }`}
                >
                  <FileJson className="w-4 h-4" />
                  JSON
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1 flex items-center gap-2">
                <Calendar className="w-4 h-4" />
                Rango de fechas (opcional)
              </label>
              <div className="grid grid-cols-2 gap-3">
                <input
                  type="date"
                  value={fechaInicio}
                  onChange={(e) => setFechaInicio(e.target.value)}
                  className="px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Fecha inicio"
                />
                <input
                  type="date"
                  value={fechaFin}
                  onChange={(e) => setFechaFin(e.target.value)}
                  className="px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                  placeholder="Fecha fin"
                />
              </div>
            </div>

            <button
              onClick={handleExportar}
              disabled={isExportando}
              className="w-full mt-4 flex items-center justify-center gap-2 py-2 rounded-lg bg-primary text-white font-semibold hover:bg-primary-light transition disabled:opacity-50"
            >
              <Download className="w-4 h-4" />
              {isExportando ? "Exportando..." : "Exportar datos"}
            </button>
          </div>
        </div>

        {/* Panel de información */}
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4 flex items-center gap-2">
            <FileText className="w-5 h-5 text-primary" />
            Información
          </h2>
          
          <div className="space-y-4 text-sm text-zinc-600">
            <p>
              <strong className="text-zinc-900">Formatos disponibles:</strong>
            </p>
            <ul className="list-disc list-inside space-y-1 ml-2">
              <li><strong>CSV</strong> - Compatible con Excel, Google Sheets y otros</li>
              <li><strong>JSON</strong> - Para integraciones con APIs</li>
            </ul>
            
            <p className="mt-4">
              <strong className="text-zinc-900">Tipo de datos:</strong>
            </p>
            <ul className="list-disc list-inside space-y-1 ml-2">
              <li><strong>Tickets</strong> - Todos los tickets del sistema</li>
              <li><strong>Usuarios</strong> - Lista de usuarios y sus roles</li>
              <li><strong>Auditoría</strong> - Registro de actividades</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}