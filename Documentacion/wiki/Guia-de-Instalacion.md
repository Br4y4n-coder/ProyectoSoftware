# Guía de Instalación (entorno local)

## Requisitos

- Java 17 (JDK)
- Node.js 20+
- PostgreSQL local **o** acceso a la BD de desarrollo
- Git

## 1. Clonar el repositorio

```bash
git clone https://github.com/JavierLeonW17/ProyectoSoftware.git
cd ProyectoSoftware
```

## 2. Backend (Spring Boot)

Configura las variables de entorno mínimas:

| Variable | Ejemplo | Notas |
|---|---|---|
| `JWT_SECRET` | cadena aleatoria ≥ 32 chars | obligatoria |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | opcional (tiene default) |
| `BREVO_API_KEY` | — | opcional; sin ella los correos solo se loguean |
| `BREVO_SENDER_EMAIL` | `no-reply@midominio.co` | opcional |

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw spring-boot:run
```

La API queda en `http://localhost:8080` y Swagger en `http://localhost:8080/swagger-ui.html`.

> Al arrancar, `DataInitializer` crea los roles y un usuario admin de pruebas: `admin@test.com / 12345678`.

## 3. Frontend (React)

```bash
cd Front/proyecto-software-frontend
npm install
```

Crea `.env.local` con:

```
VITE_API_URL=http://localhost:8080
```

Y levanta el dev server:

```bash
npm run dev      # http://localhost:5173
```

## 4. Comandos útiles

| Comando | Qué hace |
|---|---|
| `./mvnw verify` (backend) | Build + tests JUnit (H2) + cobertura JaCoCo (mín. 85%) |
| `npm run lint` (frontend) | ESLint |
| `npm test` (frontend) | Tests unitarios con Vitest |
| `npm run build` (frontend) | Build de producción |

## Solución de problemas

- **El backend no arranca**: revisa que `JWT_SECRET` esté definida y que la BD sea accesible.
- **CORS en el navegador**: agrega tu origen del frontend a `CORS_ALLOWED_ORIGINS` y reinicia el backend.
- **No llegan correos**: sin `BREVO_API_KEY` válida verás `[BREVO DESACTIVADO]` en el log — es el comportamiento esperado en local.
