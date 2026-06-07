# CI/CD — Pipeline de integración y despliegue continuo

Definido en `.github/workflows/ci.yml` con **GitHub Actions**.

## Disparadores

- **Pull Request** → corre CI completo (build, lint, pruebas).
- **Push/merge a `main`** → corre CI y, si pasa, **despliega a producción**.

## Jobs

### 1. Backend · Build + Tests

- JDK 17 (Temurin) con caché de Maven.
- `mvn verify` con perfil `test`: compila, corre **JUnit** (unitarios + integración con H2 en memoria y MockMvc) y valida cobertura.
- **JaCoCo**: genera el reporte, imprime el resumen de cobertura en el Summary del run y lo exige ≥ **85%** de instrucciones (excluyendo DTOs, entidades, configuración y repositorios generados).
- Publica artefactos: reporte JaCoCo (HTML) y reportes de Surefire.

### 2. Frontend · Lint + Tests + Build

- Node 20 con caché de npm.
- `npm ci` → **ESLint** → **Vitest** (tests unitarios) → typecheck informativo → `npm run build`.

### 3. Deploy a producción (solo push a `main`, requiere que 1 y 2 pasen)

- **Backend**: dispara el **Deploy Hook de Render** usando el secret `RENDER_DEPLOY_HOOK_URL`.
- **Frontend**: Vercel está conectado al repositorio y publica automáticamente con cada push a `main`.

## Secrets requeridos (Settings → Secrets → Actions)

| Secret | Uso |
|---|---|
| `RENDER_DEPLOY_HOOK_URL` | URL del deploy hook del servicio backend en Render |

> Nota: los secrets se configuran en el **repositorio original** (no en forks): el job de deploy corre en el repo donde ocurre el merge a `main`.

## Variables de entorno en producción

- **Render (backend)**: `JWT_SECRET`, credenciales de PostgreSQL, `CORS_ALLOWED_ORIGINS` (incluye el dominio de Vercel), `BREVO_API_KEY`, `BREVO_SENDER_EMAIL`.
- **Vercel (frontend)**: `VITE_API_URL` apuntando a la API de Render.
