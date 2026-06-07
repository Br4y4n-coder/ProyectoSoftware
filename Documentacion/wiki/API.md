# API REST

Documentación interactiva completa: **[Swagger UI](https://proyecto-ticket-26xq.onrender.com/swagger-ui.html)**.

Todas las respuestas usan el sobre `ApiResponse`: `{ status, message, data, timestamp }`. Los endpoints protegidos requieren header `Authorization: Bearer <accessToken>`.

## Autenticación — `/api/auth`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/register` | Registro (queda `pendiente` hasta verificar correo) |
| GET | `/verify-email?token=` | Activa la cuenta |
| POST | `/login` | Devuelve access + refresh token |
| POST | `/refresh` | Renueva el access token |
| POST | `/logout` | Invalida la sesión |
| POST | `/forgot-password` / `/reset-password` | Recuperación de contraseña |
| GET | `/me` | Usuario autenticado |

## Tickets — `/api/tickets`

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/` | autenticado | Crear ticket (estado `abierto`, sin agente) |
| GET | `/mios` | autenticado | Tickets del cliente |
| GET | `/` | admin/agente | Listar (filtros `estado`, `agenteId`, paginación) |
| GET | `/buscar` | admin/agente | Búsqueda con filtros (estado, prioridad, tipo, fechas, cliente, agente) |
| GET | `/{id}` · `/codigo/{codigo}` | autenticado | Detalle |
| GET | `/{id}/historial` | autenticado | Historial de cambios y comentarios |
| POST | `/{id}/comentarios` | autenticado | Agregar comentario/nota |
| PATCH | `/{id}` | admin/agente | Actualizar asunto/descripción/prioridad |
| PATCH | `/{id}/asignar` | admin/agente | Asignar agente (notifica por correo) |
| PATCH | `/{id}/estado` | admin/agente | Cambiar estado (notifica por correo) |

## Usuarios — `/api/usuarios` (solo ADMINISTRADOR)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Listar usuarios (incluye área, nivel y último acceso) |
| PUT | `/{id}` | Actualizar datos del usuario |
| PATCH | `/{id}/rol` | Cambiar rol (`usuario`/`agente`/`administrador`) |
| PATCH | `/{id}/estado` | Cambiar estado (`activo`, `suspendido`, `rechazado`, `eliminado`...) |

## Otros dominios

- **Métricas** `/api/metrics`: tickets por estado, por prioridad, tiempo promedio de resolución, resueltos por agente.
- **Categorías** `/api/categorias` · **SLA** `/api/sla` · **Integraciones** `/api/integraciones` · **Configuración** `/api/configuracion`.
- **Auditoría** `/api/auditoria`: registro de actividades con búsqueda por usuario/acción.
- **Exportación** `/api/exportar`: tickets, usuarios y auditoría en CSV o JSON.
