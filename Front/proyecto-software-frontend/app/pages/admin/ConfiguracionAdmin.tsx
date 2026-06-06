import { useEffect, useState } from "react";
import { Save, Globe, Bell, Shield, Mail, Server, Eye, EyeOff } from "lucide-react";

export default function ConfiguracionAdmin() {
  const [config, setConfig] = useState({
    siteName: "TicketHub",
    siteDescription: "Sistema de Gestión de Tickets",
    smtpHost: "smtp.gmail.com",
    smtpPort: "587",
    smtpUser: "soporte@tickethub.com",
    smtpPass: "",
    notifyEmail: true,
    notifySystem: true,
    sessionTimeout: "60",
    maxLoginAttempts: "5",
    twoFactorAuth: false,
    maintenanceMode: false,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState("");

  const fetchConfiguraciones = async () => {
    const token = localStorage.getItem('auth_token');
    
    if (!token) {
      setError("No hay sesión activa");
      setIsLoading(false);
      return;
    }
    
    try {
      const response = await fetch(`http://localhost:8080/api/configuracion/mapa`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      if (response.ok) {
        const data = await response.json();
        const configs = data?.data || {};
        setConfig({
          siteName: configs.siteName || "TicketHub",
          siteDescription: configs.siteDescription || "Sistema de Gestión de Tickets",
          smtpHost: configs.smtpHost || "smtp.gmail.com",
          smtpPort: configs.smtpPort || "587",
          smtpUser: configs.smtpUser || "soporte@tickethub.com",
          smtpPass: "",
          notifyEmail: configs.notifyEmail === "true",
          notifySystem: configs.notifySystem === "true",
          sessionTimeout: configs.sessionTimeout || "60",
          maxLoginAttempts: configs.maxLoginAttempts || "5",
          twoFactorAuth: configs.twoFactorAuth === "true",
          maintenanceMode: configs.maintenanceMode === "true",
        });
      }
    } catch (error) {
      setError("Error de conexión");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchConfiguraciones();
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setConfig({
      ...config,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleSave = async () => {
    const token = localStorage.getItem('auth_token');
    
    try {
      const payload = {
        siteName: config.siteName,
        siteDescription: config.siteDescription,
        smtpHost: config.smtpHost,
        smtpPort: config.smtpPort,
        smtpUser: config.smtpUser,
        sessionTimeout: config.sessionTimeout,
        maxLoginAttempts: config.maxLoginAttempts,
        notifyEmail: String(config.notifyEmail),
        notifySystem: String(config.notifySystem),
        twoFactorAuth: String(config.twoFactorAuth),
        maintenanceMode: String(config.maintenanceMode),
      };
      
      const response = await fetch(`http://localhost:8080/api/configuracion`, {
        method: "POST",
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });
      
      if (response.ok) {
        setSaved(true);
        setTimeout(() => setSaved(false), 3000);
      } else {
        setError("Error al guardar configuración");
        setTimeout(() => setError(""), 3000);
      }
    } catch (error) {
      setError("Error de conexión");
      setTimeout(() => setError(""), 3000);
    }
  };

  if (isLoading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Configuración</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Configuración general del sistema
        </p>
      </div>

      {saved && (
        <div className="rounded-xl bg-green-50 border border-green-200 p-4">
          <p className="text-green-700 text-sm">Configuración guardada correctamente</p>
        </div>
      )}

      {error && (
        <div className="rounded-xl bg-red-50 border border-red-200 p-4">
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Configuración General */}
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4 flex items-center gap-2">
            <Globe className="w-5 h-5 text-primary" />
            General
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Nombre del sitio
              </label>
              <input
                type="text"
                name="siteName"
                value={config.siteName}
                onChange={handleChange}
                className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Descripción
              </label>
              <textarea
                name="siteDescription"
                value={config.siteDescription}
                onChange={handleChange}
                rows="2"
                className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-zinc-900">Modo mantenimiento</p>
                <p className="text-sm text-zinc-500">Bloquea el acceso a usuarios no administradores</p>
              </div>
              <button
                type="button"
                onClick={() => setConfig({ ...config, maintenanceMode: !config.maintenanceMode })}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition ${
                  config.maintenanceMode ? "bg-primary" : "bg-gray-300"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                    config.maintenanceMode ? "translate-x-6" : "translate-x-1"
                  }`}
                />
              </button>
            </div>
          </div>
        </div>

        {/* Configuración de Correo */}
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4 flex items-center gap-2">
            <Mail className="w-5 h-5 text-primary" />
            Correo SMTP
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Servidor SMTP
              </label>
              <input
                type="text"
                name="smtpHost"
                value={config.smtpHost}
                onChange={handleChange}
                className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">
                  Puerto
                </label>
                <input
                  type="text"
                  name="smtpPort"
                  value={config.smtpPort}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">
                  Usuario
                </label>
                <input
                  type="text"
                  name="smtpUser"
                  value={config.smtpUser}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-zinc-700 mb-1">
                Contraseña
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  name="smtpPass"
                  value={config.smtpPass}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-3 flex items-center text-zinc-400"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Seguridad */}
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4 flex items-center gap-2">
            <Shield className="w-5 h-5 text-primary" />
            Seguridad
          </h2>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">
                  Timeout sesión (minutos)
                </label>
                <input
                  type="number"
                  name="sessionTimeout"
                  value={config.sessionTimeout}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-700 mb-1">
                  Intentos máximos
                </label>
                <input
                  type="number"
                  name="maxLoginAttempts"
                  value={config.maxLoginAttempts}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-zinc-900">Autenticación de dos factores</p>
                <p className="text-sm text-zinc-500">Requiere código adicional al iniciar sesión</p>
              </div>
              <button
                type="button"
                onClick={() => setConfig({ ...config, twoFactorAuth: !config.twoFactorAuth })}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition ${
                  config.twoFactorAuth ? "bg-primary" : "bg-gray-300"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                    config.twoFactorAuth ? "translate-x-6" : "translate-x-1"
                  }`}
                />
              </button>
            </div>
          </div>
        </div>

        {/* Notificaciones */}
        <div className="rounded-xl bg-white border border-zinc-200 p-6">
          <h2 className="text-lg font-semibold text-zinc-900 mb-4 flex items-center gap-2">
            <Bell className="w-5 h-5 text-primary" />
            Notificaciones
          </h2>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-zinc-900">Notificaciones por correo</p>
                <p className="text-sm text-zinc-500">Recibe alertas por correo electrónico</p>
              </div>
              <button
                type="button"
                onClick={() => setConfig({ ...config, notifyEmail: !config.notifyEmail })}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition ${
                  config.notifyEmail ? "bg-primary" : "bg-gray-300"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                    config.notifyEmail ? "translate-x-6" : "translate-x-1"
                  }`}
                />
              </button>
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-zinc-900">Notificaciones del sistema</p>
                <p className="text-sm text-zinc-500">Notificaciones internas en la plataforma</p>
              </div>
              <button
                type="button"
                onClick={() => setConfig({ ...config, notifySystem: !config.notifySystem })}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition ${
                  config.notifySystem ? "bg-primary" : "bg-gray-300"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition ${
                    config.notifySystem ? "translate-x-6" : "translate-x-1"
                  }`}
                />
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Botón Guardar */}
      <div className="flex justify-end">
        <button
          onClick={handleSave}
          className="flex items-center gap-2 px-6 py-2 rounded-lg bg-primary text-white font-semibold hover:bg-primary-light transition"
        >
          <Save className="w-4 h-4" />
          Guardar configuración
        </button>
      </div>
    </div>
  );
}