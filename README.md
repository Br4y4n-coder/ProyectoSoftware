## Descripción del Proyecto
Sistema Web de Gestión de Tickets de Soporte

Plataforma web desarrollada para registrar, organizar y dar seguimiento a las solicitudes de soporte realizadas por los usuarios dentro de una empresa de tecnología. El sistema permite centralizar la gestión de incidencias, optimizar los tiempos de atención y mejorar la trazabilidad de los casos mediante un flujo estructurado de creación, asignación y cierre de tickets.

Stack Tecnológico
Tecnología	Uso
React	Interfaz de usuario
TypeScript	Tipado estático
Vite	Entorno de desarrollo y compilación
React Router	Navegación entre páginas
Tailwind CSS	Diseño y estilos responsivos
PostgreSQL	Base de datos relacional
Node.js	Entorno de ejecución del backend
Express.js	API REST
GitHub	Control de versiones
Inicio Rápido
npm install
npm run dev



Compilación para producción:

npm run build
Roles del Sistema
Usuario
Crear solicitudes de soporte.
Consultar el estado de los tickets.
Visualizar historial de solicitudes.
Adjuntar información relevante al caso.
Técnico
Visualizar tickets asignados.
Actualizar estados de atención.
Registrar soluciones implementadas.
Cerrar tickets resueltos.
Administrador
Gestionar usuarios.
Asignar tickets a técnicos.
Supervisar métricas e indicadores.
Administrar categorías y prioridades.
Flujos Principales
Usuario

Inicio de sesión → Dashboard → Crear Ticket → Seguimiento → Cierre del Ticket

Técnico

Inicio de sesión → Bandeja de Tickets → Atención del Caso → Actualización de Estado → Resolución

Administrador

Inicio de sesión → Gestión de Tickets → Asignación → Seguimiento → Reportes

Arquitectura del Proyecto
src/
├── components/
│   ├── tickets/
│   ├── dashboard/
│   ├── users/
│   └── ui/
├── pages/
├── routes/
├── services/
├── hooks/
├── context/
├── database/
├── utils/
└── assets/
Capas del Sistema
Presentación

Interfaces desarrolladas en React para la interacción de usuarios, técnicos y administradores.

Lógica de Negocio

Gestiona la creación, actualización, asignación y cierre de tickets.

Persistencia

PostgreSQL almacena usuarios, tickets, categorías, estados y registros históricos.

API

Servicios REST para comunicación entre frontend y backend.

Módulos del Sistema
Gestión de Tickets
Creación de tickets.
Actualización de estado.
Asignación de responsables.
Cierre y documentación de soluciones.
Gestión de Usuarios
Registro.
Inicio de sesión.
Roles y permisos.
Dashboard
Indicadores de tickets abiertos.
Tickets en proceso.
Tickets cerrados.
Estadísticas de atención.
Reportes
Historial de incidencias.
Tiempo promedio de respuesta.
Rendimiento de técnicos.
Base de Datos
Tablas Principales
Usuarios
id_usuario
nombre
correo
contraseña
rol
Tickets
id_ticket
titulo
descripcion
fecha_creacion
prioridad
estado
usuario_id
Categorías
id_categoria
nombre_categoria
Historial
id_historial
ticket_id
fecha
accion_realizada
Tecnologías Utilizadas
React
TypeScript
Node.js
Express.js
PostgreSQL
Tailwind CSS
GitHub
Vite
Guía de Instalación
Clonar el repositorio desde GitHub.
Instalar dependencias con npm install.
Configurar las variables de entorno.
Crear la base de datos PostgreSQL.
Ejecutar las migraciones.
Iniciar el servidor backend.
Ejecutar el frontend con npm run dev.
Arquitectura General

El sistema sigue una arquitectura cliente-servidor compuesta por un frontend desarrollado en React y un backend basado en Node.js y Express. La información es almacenada en PostgreSQL y consumida mediante servicios REST que permiten gestionar tickets, usuarios y reportes de manera segura y escalable.

Limitaciones Actuales
Notificaciones por correo electrónico no implementadas.
Carga de archivos en fase de mejora.
Reportes avanzados pendientes de integración.
Sistema desarrollado inicialmente como MVP académico.
