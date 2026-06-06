import React from "react";
import { Link } from "react-router";

export default function Ayuda() {
  const preguntasFrecuentes = [
    {
      id: 1,
      pregunta: "¿Cómo puedo crear un ticket?",
      respuesta: "Haz clic en 'Crear ticket' en el menú lateral, completa el formulario con la información de tu problema y envía el ticket.",
    },
    {
      id: 2,
      pregunta: "¿Cuánto tiempo tarda la respuesta?",
      respuesta: "El tiempo de respuesta depende de la prioridad asignada. Alta prioridad: 2 horas, Media: 8 horas, Baja: 24 horas.",
    },
    {
      id: 3,
      pregunta: "¿Cómo puedo ver el estado de mi ticket?",
      respuesta: "Ve a 'Mis tickets' en el menú lateral, allí encontrarás todos tus tickets y su estado actual.",
    },
    {
      id: 4,
      pregunta: "¿Puedo cambiar la prioridad de mi ticket?",
      respuesta: "No directamente. Un agente o administrador puede modificar la prioridad según la evaluación del problema.",
    },
    {
      id: 5,
      pregunta: "¿Qué significa cada estado del ticket?",
      respuesta: "Abierto: Recién creado. En proceso: Un agente lo está atendiendo. Resuelto: Se ha dado solución. Cerrado: Ticket finalizado.",
    },
  ];

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-2xl font-bold text-zinc-900 mb-2">Centro de Ayuda</h1>
      <p className="text-zinc-500 mb-8">
        Encuentra respuestas a preguntas frecuentes y guías de uso.
      </p>

      <div className="space-y-6">
        {preguntasFrecuentes.map((item) => (
          <div
            key={item.id}
            className="rounded-xl bg-white border border-zinc-200 p-6"
          >
            <h3 className="text-lg font-semibold text-zinc-900 mb-2">
              {item.pregunta}
            </h3>
            <p className="text-zinc-600">
              {item.respuesta}
            </p>
          </div>
        ))}
      </div>

      <div className="mt-8 p-6 rounded-xl bg-primary-faint text-center">
        <h3 className="text-lg font-semibold text-zinc-900 mb-2">
          ¿No encuentras lo que buscas?
        </h3>
        <p className="text-zinc-600 mb-4">
          Crea un ticket y nuestro equipo te ayudará personalmente.
        </p>
        <Link
          to="/tickets/nuevo"
          className="inline-flex h-11 px-5 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary-light transition items-center"
        >
          + Crear ticket
        </Link>
      </div>
    </div>
  );
}