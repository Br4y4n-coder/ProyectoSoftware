# SIT — Sistema de Tickets de Soporte Interno

Aplicación web full-stack para la gestión centralizada de tickets de soporte técnico. Permite a los usuarios registrar incidencias, a los agentes gestionarlas y resolverlas, y a los administradores supervisar métricas, asignar cargas de trabajo y auditar el sistema.

---

## Arquitectura general

```
ProyectoSoftware/
├── backend/                  # API REST — Spring Boot 3.5 + Java 17 + PostgreSQL
└── Front/
    └── proyecto-software-frontend/   # SPA — React 19 + TypeScript + Tailwind CSS v4
```

La comunicación entre frontend y backend se realiza vía HTTP/JSON. El backend expone una API REST documentada con Swagger/OpenAPI.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.5, Java 17, Spring Security, JWT |
| Base de datos | PostgreSQL (producción), H2 en memoria (tests) |
| Frontend | React 19, TypeScript, Tailwind CSS v4, React Router 7, Vite |
| Pruebas (BE) | JUnit 5, Mockito, Spring Boot Test |
| Pruebas (FE) | Vitest, React Testing Library, Playwright |
| Contenedores | Docker (Dockerfile multietapa en backend) |
| Documentación API | Swagger UI — `/swagger-ui/index.html` |

---

## Inicio rápido

### Requisitos previos

- Java 17+
- Maven 3.8+
- Node.js 20+
- PostgreSQL (o usar la BD de Render configurada en `application.properties`)

### 1. Levantar el backend

```bash
cd backend
mvn spring-boot:run
# API disponible en http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui/index.html
```

### 2. Levantar el frontend

```bash
cd Front/proyecto-software-frontend
npm install
npm run dev
# App disponible en http://localhost:5173
```

---

## Roles del sistema

| Rol | Acceso |
|---|---|
| `usuario` | Crear y consultar sus propios tickets |
| `agente` | Cola de tickets, asignación propia, detalle y cambio de estado |
| `administrador` | Panel completo: tickets, usuarios, agentes, métricas, SLA, auditoría |

---

## Documentación adicional

- [README Backend](./backend/README.md) — configuración, endpoints y pruebas del servidor
- [README Frontend](./Front/proyecto-software-frontend/README.md) — rutas, estructura y scripts del cliente
- [TEST\_DOCUMENTATION.md](./TEST_DOCUMENTATION.md) — estrategia de pruebas, casos y cobertura

---

## API en producción

La API está desplegada en Render:
```
https://proyecto-ticket-26xq.onrender.com
https://proyecto-ticket-26xq.onrender.com/swagger-ui/index.html
```

---

*Proyecto de ingeniería de software — 2026*
