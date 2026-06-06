import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { authService } from "../services";
import { useAuth } from "../contexts/AuthContext";

export default function Login() {
  const navigate = useNavigate();
  const { isAuthenticated, login } = useAuth();
  const [form, setForm] = useState({ correo: "", contrasena: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [infoMsg, setInfoMsg] = useState("");
  const [forgotLoading, setForgotLoading] = useState(false);

  const onChange = (e) => {
    if (errorMsg) setErrorMsg("");
    if (infoMsg) setInfoMsg("");
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  useEffect(() => {
    if (isAuthenticated) navigate("/");
  }, [isAuthenticated, navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMsg("");
    setInfoMsg("");
    setSubmitting(true);
    try {
      const response = await authService.login(form);
      
      // Guardar el token con la clave correcta que espera axios.js
      if (response?.data?.data?.accessToken) {
        localStorage.setItem('auth_token', response.data.data.accessToken);
        localStorage.setItem('refresh_token', response.data.data.refreshToken);
        console.log("Token guardado correctamente con clave 'auth_token'");
      } else if (response?.data?.accessToken) {
        localStorage.setItem('auth_token', response.data.accessToken);
        localStorage.setItem('refresh_token', response.data.refreshToken);
        console.log("Token guardado correctamente con clave 'auth_token'");
      }
      
      await login(form);
      navigate("/");
    } catch (err) {
      console.error("Error de login:", err);
      setErrorMsg(
        err?.response?.data?.message ||
          err?.message ||
          "No fue posible iniciar sesión"
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleForgotPassword = async () => {
    setErrorMsg("");
    setInfoMsg("");
    if (!form.correo) {
      setErrorMsg(
        "Ingresa tu correo en el campo de arriba y volvemos a intentar."
      );
      return;
    }
    setForgotLoading(true);
    try {
      const { data } = await authService.forgotPassword(form.correo);
      setInfoMsg(
        data?.message ||
          "Si el correo existe, recibirás un enlace para restablecer tu contraseña."
      );
    } catch (err) {
      setErrorMsg(
        err?.response?.data?.message ||
          err?.message ||
          "No fue posible solicitar el restablecimiento"
      );
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="flex flex-col lg:flex-row min-h-screen bg-[#F9FAFB] font-sans text-zinc-900">
      {/* LADO IZQUIERDO — Branding */}
      <aside className="relative hidden lg:flex lg:w-5/12 bg-primary text-white overflow-hidden">
        {/* Círculos decorativos */}
        <div className="absolute -top-24 -left-24 w-[440px] h-[440px] rounded-full bg-circle opacity-40 pointer-events-none" />
        <div className="absolute bottom-0 left-40 w-[360px] h-[360px] rounded-full bg-primary-light opacity-30 pointer-events-none" />

        {/* Logo */}
        <div className="absolute top-12 left-12 flex items-center space-x-3 z-10">
          <div className="w-11 h-11 rounded-xl bg-white flex items-center justify-center">
            <span className="text-primary text-xl font-bold">T</span>
          </div>
          <span className="text-lg font-bold tracking-tight">TicketHub</span>
        </div>

        {/* Texto principal */}
        <div className="relative z-10 px-12 self-center max-w-lg">
          <h1 className="text-4xl lg:text-5xl font-bold leading-tight">
            Gestión de tickets,<br />simple y efectiva.
          </h1>
          <p className="mt-6 text-base text-primary-faint leading-relaxed">
            Centraliza solicitudes, asigna agentes y mejora tus tiempos de
            respuesta.
          </p>

          {/* Tarjeta decorativa */}
          <div className="mt-12 w-80 rounded-2xl bg-white/10 backdrop-blur-sm p-5 border border-white/10">
            <span className="inline-block px-3 py-1 rounded-full bg-success text-[10px] font-semibold tracking-wider">
              RESUELTO
            </span>
            <p className="mt-3 text-base font-semibold">
              #TKT-1024 · Error servidor
            </p>
            <p className="mt-1 text-sm text-primary-faint">
              Asignado a Andrés Rodríguez
            </p>
            <p className="mt-5 text-[11px] text-primary-subtle">
              SLA: 02:30h restantes
            </p>
            <div className="mt-1 h-1.5 w-full rounded-full bg-white/20 overflow-hidden">
              <div className="h-full w-[65%] bg-success rounded-full" />
            </div>
          </div>
        </div>
      </aside>

      {/* LADO DERECHO — Formulario */}
      <main className="flex-1 flex flex-col items-center justify-center px-6 py-12">
        <div className="w-full max-w-md bg-white rounded-2xl border border-zinc-200 shadow-sm p-10">
          <h2 className="text-2xl font-bold text-zinc-900">Iniciar sesión</h2>
          <p className="mt-2 text-sm text-zinc-500">
            Ingresa tus credenciales para acceder al sistema
          </p>

          {errorMsg && (
            <div className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {errorMsg}
            </div>
          )}
          {infoMsg && (
            <div className="mt-6 rounded-lg border border-primary-subtle bg-primary-faint px-4 py-3 text-sm text-primary">
              {infoMsg}
            </div>
          )}

          <form onSubmit={handleLogin} className="mt-8 space-y-5">
            <div>
              <label
                htmlFor="correo"
                className="block text-sm font-medium text-zinc-700 mb-1.5"
              >
                Correo electrónico
              </label>
              <input
                id="correo"
                name="correo"
                type="email"
                required
                value={form.correo}
                onChange={onChange}
                placeholder="tucorreo@empresa.com"
                className="w-full h-11 px-4 rounded-lg border border-zinc-300 bg-white text-zinc-900 placeholder-zinc-400 focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition"
              />
            </div>

            <div>
              <label
                htmlFor="contrasena"
                className="block text-sm font-medium text-zinc-700 mb-1.5"
              >
                Contraseña
              </label>
              <div className="relative">
                <input
                  id="contrasena"
                  name="contrasena"
                  type={showPassword ? "text" : "password"}
                  required
                  value={form.contrasena}
                  onChange={onChange}
                  placeholder="••••••••"
                  className="w-full h-11 pl-4 pr-11 rounded-lg border border-zinc-300 bg-white text-zinc-900 placeholder-zinc-400 focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute inset-y-0 right-3 flex items-center text-zinc-400 hover:text-zinc-700"
                  aria-label={
                    showPassword ? "Ocultar contraseña" : "Mostrar contraseña"
                  }
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center gap-2 text-zinc-700 cursor-pointer">
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                  className="w-4 h-4 rounded border-zinc-300 text-primary focus:ring-primary/40"
                />
                Recordar sesión
              </label>
              <button
                type="button"
                onClick={handleForgotPassword}
                disabled={forgotLoading}
                className="font-medium text-primary hover:text-primary-light disabled:opacity-60 disabled:cursor-not-allowed"
              >
                {forgotLoading ? "Enviando…" : "¿Olvidaste tu contraseña?"}
              </button>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full h-12 rounded-lg bg-primary hover:bg-primary-light text-white font-semibold transition disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {submitting ? "Ingresando…" : "Ingresar"}
            </button>
          </form>

          <hr className="my-6 border-zinc-200" />

          <p className="text-center text-sm text-zinc-500">
            ¿No tienes cuenta?{" "}
            <Link
              to="/auth/register"
              className="font-semibold text-primary hover:text-primary-light"
            >
              Regístrate aquí
            </Link>
          </p>
        </div>

        <p className="mt-8 text-xs text-zinc-400">
          © 2026 TicketHub · Corporación Universitaria Iberoamericana
        </p>
      </main>
    </div>
  );
}

function EyeIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 18 18"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
    >
      <path d="M1 9 C4 3, 14 3, 17 9 C14 15, 4 15, 1 9 Z" />
      <circle cx="9" cy="9" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 18 18"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
    >
      <path d="M2 9 C4 5, 7 4, 9 4 M16 9 C14 13, 11 14, 9 14" />
      <path d="M2 2 L16 16" />
    </svg>
  );
}