# TicketHub — Frontend

React 19 + TypeScript + Tailwind CSS v4 + React Router 7.

## Ejecutar el proyecto

```bash
npm install
npm run dev
```

Abre la URL que muestra Vite (por defecto `http://localhost:5173`).

## Navegación — pantallas TicketHub

### Rol Agente

| Ruta | Pantalla |
|------|----------|
| `/agent/dashboard` | Dashboard del agente (KPIs, gráfico, tickets, actividad) |
| `/agent/queue` | Cola de tickets (filtros, tabs, selección múltiple) |
| `/agent/tickets/:id` | Detalle de ticket (ej. `/agent/tickets/TKT-1038`) |

### Rol Administrador

| Ruta | Pantalla |
|------|----------|
| `/admin/dashboard` | Dashboard de administración |
| `/admin/users` | Gestión de usuarios |
| `/admin/assign-ticket` o `/admin/assign-ticket/TKT-1040` | Asignación de ticket |

Tras iniciar sesión, `/` redirige automáticamente según `user.rol`:

- `agente` → `/agent/dashboard`
- `administrador` → `/admin/dashboard`
- `usuario` → home de cliente (layout existente)

### Probar sin backend (solo UI)

1. Inicia sesión con una cuenta real, **o**
2. En DevTools → Application → Local Storage, define:
   - `auth_token` (cualquier valor no vacío)
   - `user` → JSON, por ejemplo agente:

```json
{
  "nombres": "Andrés",
  "apellidos": "Rodríguez",
  "rol": "agente",
  "email": "andres@empresa.com"
}
```

Para admin, usa `"rol": "administrador"`.

## Estructura de código

El proyecto usa la convención de **React Router 7** bajo `app/` (equivalente a `src/` en otros setups):

```
app/
  components/common/   # StatCard, AppShell, Badge, etc.
  data/mockData.ts     # Datos de ejemplo
  hooks/useMockData.ts
  layouts/             # AgentLayout, AdminLayout, MainLayout
  pages/agent/         # Pantallas agente (.tsx)
  pages/admin/         # Pantallas admin (.tsx)
  types/index.ts
  routes.ts
```

## Scripts

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | Servidor de desarrollo |
| `npm run build` | Build de producción |
| `npm run typecheck` | Verificación de tipos |

## Dependencias UI

- **lucide-react** — iconos
- **recharts** — gráficos
- **date-fns** — fechas en español
