import React from "react";
import { Link } from "react-router";

export default function BaseConocimiento() {
  const articulos = [
    {
      id: 1,
      titulo: "¿Cómo restablecer mi contraseña?",
      descripcion: "Pasos para recuperar el acceso a tu cuenta si olvidaste tu contraseña.",
      categoria: "Cuenta",
    },
    {
      id: 2,
      titulo: "Error al iniciar sesión: Credenciales inválidas",
      descripcion: "Soluciones comunes cuando no puedes acceder a tu cuenta.",
      categoria: "Soporte",
    },
    {
      id: 3,
      titulo: "¿Cómo crear un ticket correctamente?",
      descripcion: "Recomendaciones para que tu ticket sea atendido más rápido.",
      categoria: "Tickets",
    },
    {
      id: 4,
      titulo: "Tiempos de respuesta y SLA",
      descripcion: "Conoce los tiempos estimados de respuesta según la prioridad.",
      categoria: "Normativas",
    },
    {
      id: 5,
      titulo: "¿Cómo adjuntar archivos a un ticket?",
      descripcion: "Guía para agregar capturas de pantalla y documentos.",
      categoria: "Tickets",
    },
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-zinc-900 mb-2">Base de Conocimiento</h1>
      <p className="text-zinc-500 mb-8">
        Encuentra soluciones a problemas comunes y respuestas a preguntas frecuentes.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {articulos.map((articulo) => (
          <div
            key={articulo.id}
            className="rounded-xl bg-white border border-zinc-200 p-6 hover:shadow-md transition"
          >
            <span className="inline-block px-2 py-1 rounded-full text-[10px] font-bold bg-primary-faint text-primary mb-3">
              {articulo.categoria}
            </span>
            <h3 className="text-lg font-semibold text-zinc-900 mb-2">
              {articulo.titulo}
            </h3>
            <p className="text-sm text-zinc-500 mb-4">{articulo.descripcion}</p>
            <button
              type="button"
              onClick={() => alert("Artículo completo — próximamente.")}
              className="text-sm text-primary hover:text-primary-light font-medium"
            >
              Leer más →
            </button>
          </div>
        ))}
      </div>

      <div className="mt-8 text-center">
        <Link
          to="/tickets/nuevo"
          className="inline-flex h-11 px-5 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition items-center"
        >
          + Crear ticket si no encuentras solución
        </Link>
      </div>
    </div>
  );
}