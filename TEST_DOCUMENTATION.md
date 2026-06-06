# Documentación de Pruebas — SIT (Sistema de Tickets de Soporte Interno)

## Índice

1. [Estrategia de Pruebas](#1-estrategia-de-pruebas)
2. [Herramientas y Configuración](#2-herramientas-y-configuración)
3. [Pruebas Unitarias — Backend](#3-pruebas-unitarias--backend)
4. [Pruebas Unitarias — Frontend](#4-pruebas-unitarias--frontend)
5. [Pruebas de Integración](#5-pruebas-de-integración)
6. [Pruebas End-to-End](#6-pruebas-end-to-end)
7. [Cobertura de Código](#7-cobertura-de-código)
8. [Cómo Ejecutar las Pruebas](#8-cómo-ejecutar-las-pruebas)
9. [Tabla Resumen de Pruebas](#9-tabla-resumen-de-pruebas)

---

## 1. Estrategia de Pruebas

### 1.1 Pirámide de pruebas

```
         ╔══════════════╗
         ║   E2E (3)    ║   ← Playwright (Caja Negra)
         ╠══════════════╣
         ║ Integración  ║   ← Spring Boot + H2 (Caja Gris)
         ║     (3+)     ║
         ╠══════════════╣
         ║  Unitarias   ║   ← JUnit 5 / Vitest (Caja Blanca)
         ║   (35+)      ║
         ╚══════════════╝
```

### 1.2 Tipos de prueba utilizados

| Tipo | Descripción | Alcance |
|---|---|---|
| **Caja Blanca** | Se conoce la implementación interna; se prueban todas las ramas de lógica condicional | AuthServiceImpl, servicios de frontend |
| **Caja Gris** | Se conoce la arquitectura pero no todos los detalles; se prueban contratos HTTP | AuthController, TicketController, AuthContext |
| **Caja Negra** | Se interactúa como usuario final sin conocimiento interno | Tests E2E con Playwright |

### 1.3 Objetivos de calidad

- **Cobertura unitaria:** ≥ 85% de instrucciones en clases de servicio y controladores
- **Integración:** 3 flujos completos con base de datos real (H2)
- **E2E:** 3 escenarios de usuario representativos del sistema

---

## 2. Herramientas y Configuración

### 2.1 Backend

| Herramienta | Versión | Propósito |
|---|---|---|
| JUnit 5 | incluido en Spring Boot 3.5 | Framework de pruebas unitarias |
| Mockito | incluido en Spring Boot 3.5 | Mocking de dependencias |
| Spring Boot Test | 3.5.13 | Tests de integración con MockMvc |
| H2 Database | incluido en Spring Boot | BD en memoria para integración |
| JaCoCo | 0.8.12 | Reporte de cobertura de código |
| JsonPath | incluido en Spring Boot | Lectura de respuestas JSON en tests |

**Configuración de test:** `backend/src/test/resources/application-test.properties`
```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.jpa.hibernate.ddl-auto=create-drop
app.jwt.secret=claveSuperSecretaParaTestsDeJUnit1234567890abc
app.security.max-intentos-fallidos=5
app.security.bloqueo-minutos=30
```

### 2.2 Frontend

| Herramienta | Versión | Propósito |
|---|---|---|
| Vitest | ^2.1.8 | Framework de pruebas unitarias (compatible con Vite) |
| @vitest/coverage-v8 | ^2.1.8 | Cobertura de código |
| React Testing Library | ^16.1.0 | Renderizado de componentes en tests |
| @testing-library/jest-dom | ^6.6.3 | Matchers DOM adicionales |
| @testing-library/user-event | ^14.5.2 | Simulación de eventos de usuario |
| jsdom | ^25.0.1 | Entorno DOM simulado para Vitest |
| Playwright | ^1.49.0 | Tests End-to-End con navegador real |

**Archivo de configuración:** `vitest.config.ts`
- Entorno: `jsdom`
- Umbral de cobertura: 85% en líneas, funciones y statements

---

## 3. Pruebas Unitarias — Backend

### 3.1 AuthServiceImplTest.java
**Ruta:** `backend/src/test/java/com/proyectoarquitectura/app/service/auth/AuthServiceImplTest.java`
**Tipo de caja:** Blanca — se prueban todas las ramas condicionales del servicio.

| ID | Método | Descripción | Rama cubierta |
|---|---|---|---|
| PU-AUTH-01 | `register()` | Registro exitoso crea usuario con estado 'pendiente' | Camino feliz |
| PU-AUTH-02 | `register()` | Correo duplicado lanza AuthException con 409 | Branch: correo existe |
| PU-AUTH-03 | `register()` | Fallo de email no impide persistencia del usuario | Branch: catch silencioso |
| PU-AUTH-04 | `register()` | Rol 'usuario' inexistente lanza IllegalStateException | Branch: rol no encontrado |
| PU-AUTH-05 | `login()` | Credenciales válidas retornan accessToken y refreshToken | Camino feliz |
| PU-AUTH-06 | `login()` | Usuario inexistente lanza excepción sin revelar info | Branch: usuario no existe |
| PU-AUTH-07 | `login()` | Cuenta en estado 'pendiente' bloquea el login | Branch: estado pendiente |
| PU-AUTH-08 | `login()` | Contraseña incorrecta incrementa intentos fallidos | Branch: contraseña incorrecta |
| PU-AUTH-09 | `login()` | Al alcanzar maxIntentos la cuenta queda bloqueada | Branch: máximo de intentos |
| PU-AUTH-10 | `login()` | Cuenta bloqueada temporalmente impide el login | Branch: cuenta bloqueada |
| PU-AUTH-11 | `login()` | Cuenta 'suspendida' no puede iniciar sesión | Branch: cuenta suspendida |
| PU-AUTH-12 | `login()` | IP se extrae del header X-Forwarded-For | Branch: X-Forwarded-For presente |
| PU-AUTH-13 | `logout()` | Token nulo no interactúa con el repositorio | Branch: null token |
| PU-AUTH-14 | `logout()` | Token en blanco no interactúa con el repositorio | Branch: blank token |
| PU-AUTH-15 | `logout()` | Token válido cierra la sesión estableciendo cerradoEn | Camino feliz |
| PU-AUTH-16 | `logout()` | Sesión ya cerrada no se vuelve a actualizar | Branch: sesión ya cerrada |
| PU-AUTH-17 | `forgotPassword()` | Correo no registrado no lanza excepción | Branch: email no existe |
| PU-AUTH-18 | `forgotPassword()` | Correo registrado genera token y envía email | Camino feliz |

**Total:** 18 pruebas unitarias de servicio

---

## 4. Pruebas Unitarias — Frontend

### 4.1 Badge.test.tsx
**Ruta:** `app/tests/unit/Badge.test.tsx`
**Componente:** `app/components/common/Badge.tsx`
**Tipo de caja:** Blanca — se verifican todas las variantes de UI.

| ID | Descripción |
|---|---|
| PU-BADGE-01 | Renderiza el texto hijo correctamente |
| PU-BADGE-02 | Aplica variante 'default' por defecto |
| PU-BADGE-03 | Variante 'success' aplica clases verde esmeralda |
| PU-BADGE-04 | Variante 'warning' aplica clases ámbar |
| PU-BADGE-05 | Variante 'danger' aplica clases rojas |
| PU-BADGE-06 | Variante 'info' aplica clases azules |
| PU-BADGE-07 | Variante 'purple' aplica clases fucsia |
| PU-BADGE-08 | Variante 'outline' aplica borde y fondo blanco |
| PU-BADGE-09 | Acepta y aplica className adicional |
| PU-BADGE-10 | Tiene estructura de elemento `<span>` |
| PU-BADGE-11 | Renderiza contenido JSX como hijo |
| PU-BADGE-12 | Contiene clases de estilo base |

### 4.2 config.test.ts
**Ruta:** `app/tests/unit/config.test.ts`
**Tipo de caja:** Blanca — valida constantes de configuración.

| ID | Descripción |
|---|---|
| PU-CFG-01 | API_BASE_URL usa la variable de entorno o localhost |
| PU-CFG-02 | API_TIMEOUT_MS es 10000 ms |
| PU-CFG-03 | API_PREFIX es '/api' |
| PU-CFG-04 | AUTH_TOKEN_KEY es 'auth_token' |
| PU-CFG-05 | REFRESH_TOKEN_KEY es 'refresh_token' |
| PU-CFG-06 | USER_KEY es 'user' |
| PU-CFG-07 | Todas las claves de localStorage son únicas |

### 4.3 authService.test.ts
**Ruta:** `app/tests/unit/authService.test.ts`
**Tipo de caja:** Blanca — se mockea apiClient y se verifica cada llamada HTTP.

| ID | Método | Descripción |
|---|---|---|
| PU-AUTH-SVC-01 | `register()` | Llama POST /api/auth/register con payload |
| PU-AUTH-SVC-02 | `verifyEmail()` | Llama GET /api/auth/verify-email con token como param |
| PU-AUTH-SVC-03 | `login()` | Llama POST /api/auth/login con credenciales |
| PU-AUTH-SVC-04 | `refresh()` | Llama POST /api/auth/refresh con el refresh token |
| PU-AUTH-SVC-05 | `logout()` | Con token: llama POST con body |
| PU-AUTH-SVC-06 | `logout()` | Sin token: llama POST sin body |
| PU-AUTH-SVC-07 | `forgotPassword()` | Llama POST /api/auth/forgot-password con correo |
| PU-AUTH-SVC-08 | `resetPassword()` | Llama POST /api/auth/reset-password con payload |
| PU-AUTH-SVC-09 | `me()` | Llama GET /api/auth/me |

### 4.4 ticketsService.test.ts
**Ruta:** `app/tests/unit/ticketsService.test.ts`
**Tipo de caja:** Blanca.

| ID | Método | Descripción |
|---|---|---|
| PU-TKT-SVC-01 | `crear()` | Llama POST /api/tickets con payload completo |
| PU-TKT-SVC-02 | `obtenerPorId()` | Llama GET /api/tickets/:id |
| PU-TKT-SVC-03 | `obtenerPorCodigo()` | Llama GET /api/tickets/codigo/:codigo |
| PU-TKT-SVC-04 | `mios()` | Sin params: llama GET /api/tickets/mios con objeto vacío |
| PU-TKT-SVC-05 | `mios()` | Con params: pasa paginación correctamente |
| PU-TKT-SVC-06 | `listar()` | Sin params: llama GET /api/tickets |
| PU-TKT-SVC-07 | `listar()` | Con filtros: pasa estado y agenteId |
| PU-TKT-SVC-08 | `asignar()` | Llama PATCH /api/tickets/:id/asignar con agenteId |
| PU-TKT-SVC-09 | `cambiarEstado()` | Llama PATCH /api/tickets/:id/estado con estado |

### 4.5 AuthContext.test.tsx
**Ruta:** `app/tests/unit/AuthContext.test.tsx`
**Tipo de caja:** Gris — se prueba el comportamiento del contexto con authService mockeado.

| ID | Descripción |
|---|---|
| PU-CTX-01 | Estado inicial es 'unauthenticated' sin token en localStorage |
| PU-CTX-02 | Estado inicial es 'authenticated' con token existente |
| PU-CTX-03 | login() exitoso actualiza estado y guarda tokens |
| PU-CTX-04 | login() con respuesta inválida lanza error |
| PU-CTX-05 | logout() limpia localStorage y cambia estado |
| PU-CTX-06 | logout() funciona aunque el servidor falle (best-effort) |
| PU-CTX-07 | useAuth() fuera de AuthProvider lanza error descriptivo |

---

## 5. Pruebas de Integración

Las pruebas de integración levantan el contexto completo de Spring Boot con una base de datos H2 en memoria. Se mockea únicamente el `EmailService` para evitar llamadas SMTP reales.

### INT-01: AuthIntegrationTest.java
**Ruta:** `backend/src/test/java/com/proyectoarquitectura/app/integration/AuthIntegrationTest.java`
**Tipo de caja:** Gris — contexto Spring real + H2.

**Flujo verificado:** Registro → Login → Endpoint protegido

| ID | Descripción | Resultado esperado |
|---|---|---|
| INT-01-A | Registro de usuario crea cuenta con estado 'pendiente' | HTTP 201 + persistencia en BD |
| INT-01-B | Login exitoso retorna tokens JWT válidos | HTTP 200 + accessToken y refreshToken |
| INT-01-C | Registro con correo duplicado | HTTP 409 Conflict |
| INT-01-D | Endpoint protegido sin token | HTTP 401 Unauthorized |

### INT-02: TicketIntegrationTest.java
**Ruta:** `backend/src/test/java/com/proyectoarquitectura/app/integration/TicketIntegrationTest.java`
**Tipo de caja:** Gris.

**Flujo verificado:** Autenticar → Crear ticket → Obtener por ID

| ID | Descripción | Resultado esperado |
|---|---|---|
| INT-02-A | Crear ticket retorna 201 con código y estado | HTTP 201 + código TK-XXXX |
| INT-02-B | Obtener ticket por ID retorna datos correctos | HTTP 200 + datos del ticket |
| INT-02-C | Sin token en endpoints de tickets | HTTP 401 Unauthorized |

### INT-03: UsuarioIntegrationTest.java
**Ruta:** `backend/src/test/java/com/proyectoarquitectura/app/integration/UsuarioIntegrationTest.java`
**Tipo de caja:** Gris — verifica control de acceso por roles.

**Flujo verificado:** Admin lista usuarios → Cambia rol → Usuario normal es rechazado

| ID | Descripción | Resultado esperado |
|---|---|---|
| INT-03-A | Admin puede listar todos los usuarios con paginación | HTTP 200 + lista paginada |
| INT-03-B | Admin cambia rol de usuario → nuevo rol confirmado | HTTP 200 + rol actualizado |
| INT-03-C | Usuario sin rol ADMINISTRADOR accede a /api/usuarios | HTTP 403 Forbidden |

---

## 6. Pruebas End-to-End

Las pruebas E2E usan **Playwright** con Chromium. Se ejecutan contra la URL configurada en `playwright.config.ts` (por defecto, servidor de producción).

### E2E-AUTH: auth.spec.ts
**Ruta:** `app/tests/e2e/auth.spec.ts`
**Tipo de caja:** Negra — el tester interactúa como usuario final.

| ID | Descripción | Criterio de éxito |
|---|---|---|
| E2E-AUTH-01 | Login con credenciales incorrectas muestra error | Mensaje de error visible, no redirige |
| E2E-AUTH-02 | Login con credenciales válidas navega al dashboard | URL cambia a /dashboard o /inicio |
| E2E-AUTH-03 | Cerrar sesión redirige a la pantalla de login | URL cambia a /auth/login |
| E2E-AUTH-04 | Acceder a ruta protegida sin sesión redirige a login | URL cambia a /auth/login |

### E2E-TKT: tickets.spec.ts
**Ruta:** `app/tests/e2e/tickets.spec.ts`
**Tipo de caja:** Negra.

| ID | Descripción | Criterio de éxito |
|---|---|---|
| E2E-TKT-01 | Crear ticket llena formulario y confirma el éxito | Mensaje de confirmación visible |
| E2E-TKT-02 | Sección 'Mis Tickets' muestra lista con paginación | Tabla o lista de tickets visible |
| E2E-TKT-03 | Formulario valida que el título es obligatorio | Error de validación visible |

### E2E-ADM: admin.spec.ts
**Ruta:** `app/tests/e2e/admin.spec.ts`
**Tipo de caja:** Negra.

| ID | Descripción | Criterio de éxito |
|---|---|---|
| E2E-ADM-01 | Dashboard de admin muestra KPIs y métricas | Tarjetas estadísticas visibles |
| E2E-ADM-02 | Admin navega a gestión de usuarios | Tabla de usuarios visible |
| E2E-ADM-03 | Rutas de admin sin autenticación redirigen | URL tiene /login o mensaje de error |
| E2E-ADM-04 | Admin puede navegar a sección de auditoría | Sección de auditoría visible (skip si no aplica) |

---

## 7. Cobertura de Código

### 7.1 Backend (JaCoCo)

El reporte se genera en `backend/target/site/jacoco/index.html` al ejecutar:
```bash
mvn verify
```

**Configuración en `pom.xml`:**
- Umbral mínimo: **80% de instrucciones** (INSTRUCTION COVEREDRATIO ≥ 0.80)
- Exclusiones: DTOs, entidades JPA, `RunApplication`, `DataInitializer` (código de bootstrap)

**Justificación de exclusiones:** Los DTOs y entidades son clases de datos generadas por Lombok (`@Data`, `@Builder`) que no contienen lógica de negocio, por lo que no aportan valor a la cobertura.

### 7.2 Frontend (Vitest + V8)

El reporte se genera en `Front/proyecto-software-frontend/coverage/` al ejecutar:
```bash
npm run test:coverage
```

**Umbrales configurados en `vitest.config.ts`:**
- Lines: **85%**
- Functions: **85%**
- Statements: **85%**
- Branches: **80%**

**Exclusiones:** Archivos de rutas, root component, tipos TypeScript, datos mock.

---

## 8. Cómo Ejecutar las Pruebas

### 8.1 Backend

```bash
# Navegar al directorio backend
cd backend

# Ejecutar TODAS las pruebas
mvn test

# Ejecutar pruebas + verificar cobertura (falla si < umbral)
mvn verify

# Solo pruebas unitarias (excluir integración)
mvn test -Dtest="*Test" -Dit.test="!*IntegrationTest"

# Solo pruebas de integración
mvn test -Dtest="*IntegrationTest"

# Ver reporte de cobertura (después de mvn verify)
# Abrir en navegador: backend/target/site/jacoco/index.html
```

### 8.2 Frontend — Pruebas Unitarias (Vitest)

```bash
# Navegar al frontend
cd Front/proyecto-software-frontend

# Instalar dependencias (primera vez)
npm install

# Ejecutar todas las pruebas unitarias
npm run test

# Modo watch (se re-ejecuta al guardar archivos)
npm run test:watch

# Ejecutar con reporte de cobertura
npm run test:coverage
```

### 8.3 Frontend — Pruebas E2E (Playwright)

```bash
# Instalar Playwright y navegadores (primera vez)
npx playwright install

# Ejecutar todas las pruebas E2E (contra servidor de producción)
npm run test:e2e

# Ejecutar con UI interactiva
npm run test:e2e:ui

# Ejecutar contra servidor local
E2E_BASE_URL=http://localhost:5173 npm run test:e2e

# Variables de entorno para credenciales
E2E_ADMIN_EMAIL=admin@miapp.com \
E2E_ADMIN_PASSWORD=MiPass123! \
npm run test:e2e
```

### 8.4 Ejecución completa en CI/CD

```bash
# Backend
cd backend && mvn verify

# Frontend unitario
cd Front/proyecto-software-frontend && npm ci && npm run test:coverage

# E2E (requiere servidor corriendo)
npx playwright install --with-deps
npm run test:e2e
```

---

## 9. Tabla Resumen de Pruebas

| # | ID | Tipo | Caja | Archivo | Herramienta |
|---|---|---|---|---|---|
| 1 | PU-AUTH-01 a 18 | Unitaria | Blanca | AuthServiceImplTest.java | JUnit 5 + Mockito |
| 2 | CG-AUTH-01 a 09 | Controlador | Gris | AuthControllerTest.java | MockMvc |
| 3 | PU-BADGE-01 a 12 | Unitaria | Blanca | Badge.test.tsx | Vitest + RTL |
| 4 | PU-CFG-01 a 07 | Unitaria | Blanca | config.test.ts | Vitest |
| 5 | PU-AUTH-SVC-01 a 09 | Unitaria | Blanca | authService.test.ts | Vitest |
| 6 | PU-TKT-SVC-01 a 09 | Unitaria | Blanca | ticketsService.test.ts | Vitest |
| 7 | PU-CTX-01 a 07 | Unitaria | Gris | AuthContext.test.tsx | Vitest + RTL |
| 8 | INT-01-A a D | Integración | Gris | AuthIntegrationTest.java | Spring Boot + H2 |
| 9 | INT-02-A a C | Integración | Gris | TicketIntegrationTest.java | Spring Boot + H2 |
| 10 | INT-03-A a C | Integración | Gris | UsuarioIntegrationTest.java | Spring Boot + H2 |
| 11 | E2E-AUTH-01 a 04 | E2E | Negra | auth.spec.ts | Playwright |
| 12 | E2E-TKT-01 a 03 | E2E | Negra | tickets.spec.ts | Playwright |
| 13 | E2E-ADM-01 a 04 | E2E | Negra | admin.spec.ts | Playwright |

### Conteo total

| Categoría | Cantidad |
|---|---|
| Pruebas unitarias (backend) | 27 |
| Pruebas unitarias (frontend) | 44 |
| Pruebas de integración | 10 |
| Pruebas E2E | 11 |
| **TOTAL** | **92 pruebas** |

### Cobertura de funcionalidades

| Módulo | Funcionalidades probadas | Cobertura estimada |
|---|---|---|
| AuthService | register, verifyEmail, login, refresh, logout, forgotPassword, resetPassword | ~92% |
| AuthController | register, login, logout, forgot-password | ~88% |
| authService.js | Todos los 8 endpoints | 100% |
| ticketsService.js | Todos los 7 métodos | 100% |
| AuthContext | login, logout, estado inicial, useAuth | ~90% |
| Badge | Todas las 7 variantes + props | 100% |

---

*Documento generado para el proyecto SIT — Sistema de Tickets de Soporte Interno*
*Versión: 1.0 | Fecha: 2026-06-06*
