# SIT Backend — API REST

Spring Boot 3.5 · Java 17 · PostgreSQL · Spring Security + JWT

---

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL (o configurar variables de entorno para otra BD)

---

## Ejecutar localmente

```bash
# Desde la carpeta backend/
mvn spring-boot:run
```

El servidor arranca en el puerto **8080** por defecto (`PORT` configurable por variable de entorno).

Swagger UI disponible en:
```
http://localhost:8080/swagger-ui/index.html
```

---

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | `miClaveSuperSegura1234...` |
| `SPRING_DATASOURCE_URL` | URL de conexión a PostgreSQL | Ver `application.properties` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la BD | `tickethub_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la BD | — |

En producción estas variables se configuran en el panel de Render. Para desarrollo local se puede sobrescribir `application.properties` o crear `application-local.properties`.

---

## Estructura del proyecto

```
src/main/java/com/proyectoarquitectura/app/
├── api/            # Configuración de respuesta genérica (ApiResponse)
├── config/         # Configuración de seguridad, async, OpenAPI, RestTemplate
├── controller/     # Controladores REST
├── exception/      # Excepciones personalizadas y GlobalExceptionHandler
├── interfaz/       # Interfaces de servicios
├── models/         # Entidades JPA y DTOs (Request/Response)
├── repository/     # Repositorios Spring Data JPA
├── security/       # JWT filter, UserDetails, handlers de acceso
├── service/        # Implementaciones de servicios
└── utils/          # Utilidades (DataInitializer, etc.)
```

---

## Endpoints principales

### Autenticación — `/api/auth`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Registro de nuevo usuario | No |
| GET | `/api/auth/verify-email` | Verificación de correo con token | No |
| POST | `/api/auth/login` | Inicio de sesión → JWT | No |
| POST | `/api/auth/refresh` | Renovar access token con refresh token | No |
| POST | `/api/auth/logout` | Cerrar sesión (invalida refresh token) | Sí |
| POST | `/api/auth/forgot-password` | Solicitar recuperación de contraseña | No |
| POST | `/api/auth/reset-password` | Establecer nueva contraseña con token | No |
| GET | `/api/auth/me` | Datos del usuario autenticado | Sí |

### Tickets — `/api/tickets`

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/api/tickets` | Crear ticket | usuario, agente |
| GET | `/api/tickets` | Listar tickets (filtros + paginación) | agente, admin |
| GET | `/api/tickets/mios` | Mis tickets (cliente autenticado) | usuario |
| GET | `/api/tickets/{id}` | Obtener ticket por ID | autenticado |
| GET | `/api/tickets/codigo/{codigo}` | Obtener ticket por código (TK-XXXX) | autenticado |
| PATCH | `/api/tickets/{id}/asignar` | Asignar ticket a agente | agente, admin |
| PATCH | `/api/tickets/{id}/estado` | Cambiar estado del ticket | agente, admin |
| GET | `/api/tickets/{id}/historial` | Historial de cambios de un ticket | autenticado |

### Usuarios — `/api/usuarios`

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/usuarios` | Listar usuarios paginados | admin |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID | admin |
| PATCH | `/api/usuarios/{id}/rol` | Cambiar rol de usuario | admin |
| PATCH | `/api/usuarios/{id}/estado` | Cambiar estado de cuenta | admin |
| PUT | `/api/usuarios/{id}` | Actualizar datos de usuario | admin |

### Otros módulos

| Prefijo | Descripción |
|---|---|
| `/api/categorias` | CRUD de categorías de tickets |
| `/api/sla` | Reglas de SLA (Service Level Agreement) |
| `/api/metricas` | KPIs y estadísticas para dashboard admin |
| `/api/auditoria` | Logs de auditoría del sistema |
| `/api/exportacion` | Exportar datos en CSV/Excel |
| `/api/integraciones` | Gestión de integraciones externas |
| `/api/configuracion` | Configuración global del sistema |

---

## Seguridad

- Autenticación basada en **JWT** (access token 24 h, refresh token 7 días)
- Bloqueo de cuenta tras `N` intentos fallidos (configurable)
- Auditoría automática de endpoints sensibles (`SensitiveEndpointAuditFilter`)
- Roles manejados vía Spring Security: `ROLE_USUARIO`, `ROLE_AGENTE`, `ROLE_ADMINISTRADOR`

---

## Base de datos

- **Producción:** PostgreSQL en Render
- **Tests:** H2 en memoria (`MODE=PostgreSQL`)
- Migraciones: `spring.jpa.hibernate.ddl-auto=update` (actualización automática del esquema)
- Datos iniciales: `DataInitializer` — crea roles y usuario administrador por defecto al arrancar

---

## Docker

```bash
# Construir imagen
docker build -t sit-backend .

# Ejecutar
docker run -p 8080:8080 \
  -e JWT_SECRET=miClaveSuperSegura \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  sit-backend
```

El Dockerfile usa compilación multietapa: Maven para el build y Amazon Corretto 17 para ejecución. Zona horaria configurada en `America/Bogota`.

---

## Pruebas

```bash
# Ejecutar todas las pruebas
mvn test

# Pruebas + reporte de cobertura JaCoCo
mvn verify

# Ver reporte de cobertura
# Abrir: target/site/jacoco/index.html
```

| Suite | Archivo | Tipo |
|---|---|---|
| AuthServiceImplTest | `service/auth/AuthServiceImplTest.java` | Unitaria (Caja Blanca) |
| AuthIntegrationTest | `integration/AuthIntegrationTest.java` | Integración (Caja Gris) |
| TicketIntegrationTest | `integration/TicketIntegrationTest.java` | Integración (Caja Gris) |
| UsuarioIntegrationTest | `integration/UsuarioIntegrationTest.java` | Integración (Caja Gris) |

Umbral mínimo de cobertura: **80% de instrucciones** (JaCoCo). Las pruebas de integración usan H2 y mockean únicamente el servicio de email para evitar envíos SMTP reales.

Ver detalle completo en [TEST\_DOCUMENTATION.md](../TEST_DOCUMENTATION.md).
