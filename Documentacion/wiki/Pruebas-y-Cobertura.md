# Pruebas y Cobertura

## Backend (JUnit 5)

| Tipo | Herramientas | Qué cubre |
|---|---|---|
| **Unitarias** | JUnit 5 + Mockito (sin contexto Spring) | Servicios (tickets, usuarios, auth, métricas, categorías, SLA, integraciones, configuración, auditoría, exportación, correos Brevo), controladores (todos los endpoints y ramas de error), seguridad (`CustomUserDetails`, `CustomUserDetailsService`) y `GlobalExceptionHandler` |
| **Integración** | `@SpringBootTest` + MockMvc + **H2 en memoria** (perfil `test`) | Flujos completos: registro/login/refresh (Auth), ciclo de vida de tickets, gestión de usuarios. `EmailService` mockeado con `@MockitoBean` |

Ejecución local:

```bash
cd backend
./mvnw verify
```

## Cobertura (JaCoCo)

- Umbral exigido: **≥ 85% de instrucciones** — el build **falla** si no se cumple (`jacoco:check` en fase `verify`).
- Reporte: `backend/target/site/jacoco/index.html` (también se publica como artefacto en cada run de CI, y el porcentaje aparece en el Summary del job).
- Exclusiones (estándar): punto de entrada, DTOs y entidades JPA (sin lógica), configuración de Spring, repositorios generados por Spring Data, filtros JWT de infraestructura y factories de excepciones.

## Frontend (Vitest)

- Tests unitarios en `app/tests/unit/` con **Vitest + Testing Library + jsdom**: componentes (Badge, AuthContext), servicios (auth, tickets, config), el wrapper HTTP `apiFetch` (respuestas 2xx, errores HTTP sin lanzar, error de red) y utilidades del dashboard (formato de tiempos).
- E2E con **Playwright** en `app/tests/e2e/` (auth, tickets, admin).

```bash
cd Front/proyecto-software-frontend
npm test              # unitarios
npm run test:coverage # con cobertura
npm run test:e2e      # Playwright
```

## En el pipeline

Cada Pull Request ejecuta automáticamente: build + tests del backend con validación de cobertura, y lint + tests + build del frontend. Un PR no puede mergearse en verde si algo de esto falla.
