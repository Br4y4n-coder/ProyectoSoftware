# SIT Frontend

React 19 · TypeScript · Tailwind CSS v4 · React Router 7 · Vite

---

## Requisitos

- Node.js 20+

---

## Ejecutar el proyecto

```bash
npm install
npm run dev
# App disponible en http://localhost:5173
```

---

## Scripts disponibles

| Comando | Descripción |
|---|---|
| `npm run dev` | Servidor de desarrollo (Vite HMR) |
| `npm run build` | Build de producción |
| `npm run start` | Sirve el build de producción |
| `npm run typecheck` | Verificación de tipos TypeScript |
| `npm run lint` | Revisión con ESLint |
| `npm run lint:fix` | Corregir errores de lint automáticamente |
| `npm run format` | Formatear código con Prettier |
| `npm run test` | Pruebas unitarias con Vitest (una sola ejecución) |
| `npm run test:watch` | Pruebas en modo watch |
| `npm run test:coverage` | Pruebas + reporte de cobertura (V8) |
| `npm run test:e2e` | Pruebas End-to-End con Playwright |
| `npm run test:e2e:ui` | Playwright con interfaz visual |

---

## Roles y rutas

Tras iniciar sesión, `/` redirige automáticamente según `user.rol`:

| Rol | Redirección |
|---|---|
| `agente` | `/agent/dashboard` |
| `administrador` | `/admin/dashboard` |
| `usuario` | Home del cliente |

### Rol Agente

| Ruta | Pantalla |
|---|---|
| `/agent/dashboard` | Dashboard con KPIs y tabla de tickets asignados |
| `/agent/queue` | Cola general de tickets (filtros, tabs, selección múltiple) |
| `/agent/tickets/:id` | Detalle de ticket (historial, cambio de estado, asignación) |
| `/agent/mis-asignados` | Mis tickets asignados (filtros y acciones rápidas) |

### Rol Administrador

| Ruta | Pantalla |
|---|---|
| `/admin/dashboard` | Dashboard con KPIs, gráficos y tickets en riesgo |
| `/admin/tickets` | Gestión de tickets (tabla, filtros, panel de detalle, asignación) |
| `/admin/users` | Gestión de usuarios |
| `/admin/agentes` | Gestión de agentes |
| `/admin/categorias` | Categorías de tickets |
| `/admin/sla` | Reglas de SLA |
| `/admin/metricas` | Métricas y estadísticas |
| `/admin/auditoria` | Logs de auditoría |
| `/admin/exportar` | Exportar datos |
| `/admin/configuracion` | Configuración del sistema |
| `/admin/integraciones` | Integraciones externas |

### Rutas públicas (autenticación)

| Ruta | Pantalla |
|---|---|
| `/auth/login` | Inicio de sesión |
| `/auth/register` | Registro de cuenta |
| `/auth/verify-email` | Verificación de correo |
| `/auth/reset-password` | Restablecer contraseña |

---

## Estructura de carpetas

```
app/
├── api/                  # Cliente axios y wrapper apiFetch
├── components/
│   ├── admin/            # Componentes exclusivos del panel admin (AssignTicketModal, etc.)
│   └── common/           # Componentes compartidos (AppShell, Badge, StatusBadge, etc.)
├── contexts/             # AuthContext — autenticación global
├── data/                 # Datos estáticos y helpers
├── hooks/                # useDashboardData, useAdminDashboard, etc.
├── layouts/              # AdminLayout, AgentLayout, MainLayout
├── pages/
│   ├── admin/            # Pantallas del panel administrador (.tsx)
│   └── agent/            # Pantallas del agente (.jsx)
├── services/             # authService, ticketsService, config
├── tests/
│   ├── unit/             # Pruebas unitarias Vitest
│   └── e2e/              # Pruebas E2E Playwright
├── types/                # Tipos TypeScript (auth, tickets, etc.)
└── routes.ts             # Definición de rutas React Router 7
```

---

## Autenticación

El contexto `AuthContext` gestiona el estado de sesión global:

- Tokens almacenados en `localStorage` (`auth_token`, `refresh_token`, `user`)
- Renovación automática del access token via refresh token
- Redirección automática a `/auth/login` si no hay sesión activa

### Probar sin backend

En DevTools → Application → Local Storage, define:
- `auth_token`: cualquier valor no vacío
- `user`: JSON con los datos del usuario

```json
// Agente
{ "nombres": "Ana", "apellidos": "Pérez", "rol": "agente", "id": 1 }

// Administrador
{ "nombres": "Carlos", "apellidos": "Gómez", "rol": "administrador", "id": 2 }
```

---

## Variables de entorno

Crea un archivo `.env` en la raíz del frontend:

```env
VITE_API_URL=http://localhost:8080
```

Si no se define, el cliente usa `http://localhost:8080` por defecto.

---

## Dependencias principales

| Paquete | Uso |
|---|---|
| `react` + `react-dom` | UI — React 19 |
| `react-router` | Enrutamiento (React Router 7) |
| `axios` | Cliente HTTP para la API REST |
| `tailwindcss` | Estilos (Tailwind CSS v4) |
| `lucide-react` | Iconos |
| `recharts` | Gráficos en el dashboard |
| `date-fns` | Formateo de fechas |
| `vitest` | Pruebas unitarias |
| `@testing-library/react` | Renderizado de componentes en tests |
| `@playwright/test` | Pruebas E2E |

---

## Pruebas

```bash
# Unitarias
npm run test

# Con cobertura (reporte en coverage/)
npm run test:coverage

# E2E — requiere servidor corriendo
npm run test:e2e
```

Umbral de cobertura configurado: **85%** en líneas, funciones y statements.

Ver detalle completo en [TEST\_DOCUMENTATION.md](../../TEST_DOCUMENTATION.md).
