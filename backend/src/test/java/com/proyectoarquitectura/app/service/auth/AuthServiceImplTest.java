package com.proyectoarquitectura.app.service.auth;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.auth.AuthResponse;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.dto.auth.RegisterRequest;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Sesion;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.LogAccesoRepository;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.SesionRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.security.JwtService;
import com.proyectoarquitectura.app.security.TokenPurpose;
import com.proyectoarquitectura.app.service.auditoria.AuditoriaService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — AuthServiceImpl
 *
 * Tipo de caja: BLANCA — se conoce la implementación interna y se prueban
 * todas las ramas de lógica condicional del servicio.
 *
 * Herramienta: JUnit 5 + Mockito (sin contexto Spring).
 *
 * Cobertura objetivo: ≥ 85% de instrucciones del servicio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — Pruebas Unitarias (Caja Blanca)")
class AuthServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private SesionRepository sesionRepository;
    @Mock private LogAccesoRepository logAccesoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private AuditoriaService auditoriaService;
    @Mock private HttpServletRequest httpRequest;
    @Mock private Claims claims;

    private AuthServiceImpl authService;

    private static final int MAX_INTENTOS = 5;
    private static final int BLOQUEO_MIN  = 30;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                usuarioRepository, rolRepository, sesionRepository,
                logAccesoRepository, passwordEncoder, jwtService,
                emailService, auditoriaService,
                MAX_INTENTOS, BLOQUEO_MIN
        );
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Usuario usuarioActivo(String correo) {
        Rol rol = new Rol(); rol.setNombre("usuario");
        Usuario u = new Usuario();
        u.setId(1); u.setNombres("Test"); u.setApellidos("User");
        u.setCorreo(correo); u.setContrasenaHash("hashed");
        u.setEstado("activo"); u.setRol(rol); u.setIntentosFallidos(0);
        return u;
    }

    private void stubHttpRequest() {
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit-Test");
    }

    // ─── register() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("PU-AUTH-01 | Registro exitoso crea usuario con estado 'pendiente'")
        void registroExitoso() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .nombres("Carlos").apellidos("López")
                    .correo("carlos@test.com").contrasena("Segura123!")
                    .build();
            Rol rol = new Rol(); rol.setNombre("usuario");

            when(usuarioRepository.existsByCorreo("carlos@test.com")).thenReturn(false);
            when(rolRepository.findByNombre("usuario")).thenReturn(Optional.of(rol));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
            when(jwtService.generateVerifyEmailToken(any())).thenReturn("verify-tok");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setId(10);
                return u;
            });

            UsuarioResponse result = authService.register(req);

            assertThat(result.getCorreo()).isEqualTo("carlos@test.com");
            assertThat(result.getEstado()).isEqualTo("pendiente");
            verify(usuarioRepository).save(any(Usuario.class));
            verify(emailService).enviarVerificacionCorreo(any(), eq("verify-tok"));
        }

        @Test
        @DisplayName("PU-AUTH-02 | Correo duplicado lanza AuthException con 409")
        void correoDuplicadoLanzaExcepcion() {
            RegisterRequest req = RegisterRequest.builder()
                    .correo("dup@test.com").nombres("X").apellidos("Y").contrasena("pass1234")
                    .build();
            when(usuarioRepository.existsByCorreo("dup@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("correo");
        }

        @Test
        @DisplayName("PU-AUTH-03 | Fallo de email no impide que el usuario se persista")
        void falloEmailUsuarioSeGuarda() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .nombres("Ana").apellidos("Gil")
                    .correo("ana@test.com").contrasena("Segura123!")
                    .build();
            Rol rol = new Rol(); rol.setNombre("usuario");

            when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
            when(rolRepository.findByNombre("usuario")).thenReturn(Optional.of(rol));
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(jwtService.generateVerifyEmailToken(any())).thenReturn("tok");
            when(usuarioRepository.save(any())).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0); u.setId(2); return u;
            });
            doThrow(new RuntimeException("SMTP down")).when(emailService)
                    .enviarVerificacionCorreo(any(), any());

            UsuarioResponse result = authService.register(req);

            assertThat(result).isNotNull();
            assertThat(result.getEstado()).isEqualTo("pendiente");
            verify(usuarioRepository).save(any());
        }

        @Test
        @DisplayName("PU-AUTH-04 | Rol 'usuario' inexistente lanza IllegalStateException")
        void rolInexistenteLanzaIllegalState() {
            RegisterRequest req = RegisterRequest.builder()
                    .nombres("X").apellidos("Y").correo("x@test.com").contrasena("pass1234")
                    .build();
            when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
            when(rolRepository.findByNombre("usuario")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ─── login() ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("PU-AUTH-05 | Login con credenciales válidas retorna accessToken y refreshToken")
        void loginCredencialesValidas() {
            stubHttpRequest();
            Usuario u = usuarioActivo("juan@test.com");

            when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("pass1234", "hashed")).thenReturn(true);
            when(jwtService.generateAccessToken(u)).thenReturn("access-tok");
            when(jwtService.generateRefreshToken(u)).thenReturn("refresh-tok");
            when(jwtService.hashToken("refresh-tok")).thenReturn("hash-tok");
            when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);
            when(jwtService.getAccessExpirationMs()).thenReturn(86400000L);
            when(sesionRepository.save(any())).thenReturn(new Sesion());
            when(usuarioRepository.save(u)).thenReturn(u);

            AuthResponse r = authService.login(
                    LoginRequest.builder().correo("juan@test.com").contrasena("pass1234").build(),
                    httpRequest);

            assertThat(r.getAccessToken()).isEqualTo("access-tok");
            assertThat(r.getRefreshToken()).isEqualTo("refresh-tok");
            assertThat(r.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("PU-AUTH-06 | Usuario inexistente lanza excepción sin revelar info")
        void loginUsuarioInexistente() {
            stubHttpRequest();
            when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
            when(logAccesoRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().correo("x@test.com").contrasena("p").build(), httpRequest))
                    .isInstanceOf(AuthException.class);
        }

        @Test
        @DisplayName("PU-AUTH-07 | Cuenta en estado 'pendiente' bloquea el login")
        void loginCuentaPendiente() {
            stubHttpRequest();
            Usuario u = new Usuario();
            u.setCorreo("pend@test.com"); u.setEstado("pendiente"); u.setIntentosFallidos(0);
            when(usuarioRepository.findByCorreo("pend@test.com")).thenReturn(Optional.of(u));
            when(logAccesoRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().correo("pend@test.com").contrasena("p").build(), httpRequest))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("verificar");
        }

        @Test
        @DisplayName("PU-AUTH-08 | Contraseña incorrecta incrementa contador de intentos fallidos")
        void loginContrasenaIncorrecta_incrementaIntentos() {
            stubHttpRequest();
            Usuario u = usuarioActivo("ju@test.com");
            u.setIntentosFallidos(2);

            when(usuarioRepository.findByCorreo("ju@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
            when(usuarioRepository.save(u)).thenReturn(u);
            when(logAccesoRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().correo("ju@test.com").contrasena("wrong").build(), httpRequest))
                    .isInstanceOf(AuthException.class);
            assertThat(u.getIntentosFallidos()).isEqualTo(3);
        }

        @Test
        @DisplayName("PU-AUTH-09 | Al alcanzar maxIntentos la cuenta queda bloqueada")
        void loginMaxIntentos_bloqueaCuenta() {
            stubHttpRequest();
            Usuario u = usuarioActivo("bu@test.com");
            u.setIntentosFallidos(MAX_INTENTOS - 1); // un intento más bloquea

            when(usuarioRepository.findByCorreo("bu@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
            when(usuarioRepository.save(u)).thenReturn(u);
            when(logAccesoRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().correo("bu@test.com").contrasena("wrong").build(), httpRequest))
                    .isInstanceOf(AuthException.class);
            assertThat(u.getBloqueadoHasta()).isNotNull()
                    .isAfter(LocalDateTime.now());
            assertThat(u.getIntentosFallidos()).isEqualTo(0);
        }

        @Test
        @DisplayName("PU-AUTH-10 | Cuenta bloqueada temporalmente impide el login")
        void loginCuentaBloqueada() {
            stubHttpRequest();
            Usuario u = new Usuario();
            u.setCorreo("bl@test.com"); u.setEstado("activo");
            u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(20));
            when(usuarioRepository.findByCorreo("bl@test.com")).thenReturn(Optional.of(u));
            when(logAccesoRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().correo("bl@test.com").contrasena("p").build(), httpRequest))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("bloqueada");
        }

        @Test
        @DisplayName("PU-AUTH-11 | Cuenta 'suspendida' no puede iniciar sesión")
        void loginCuentaSuspendida() {
            stubHttpRequest();
            Usuario u = new Usuario();
            u.setCorreo("sus@test.com"); u.setEstado("suspendido"); u.setIntentosFallidos(0);
            when(usuarioRepository.findByCorreo("sus@test.com")).thenReturn(Optional.of(u));
            when(logAccesoRepository.save(any())).thenReturn(null);

            assertThatThrownBy(() -> authService.login(
                    LoginRequest.builder().correo("sus@test.com").contrasena("p").build(), httpRequest))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("disponible");
        }

        @Test
        @DisplayName("PU-AUTH-12 | IP se extrae del header X-Forwarded-For si está presente")
        void loginIpDesdeHeader() {
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 10.0.0.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("Test");
            Usuario u = usuarioActivo("fw@test.com");

            when(usuarioRepository.findByCorreo("fw@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
            when(jwtService.generateAccessToken(u)).thenReturn("at");
            when(jwtService.generateRefreshToken(u)).thenReturn("rt");
            when(jwtService.hashToken("rt")).thenReturn("h");
            when(jwtService.getRefreshExpirationMs()).thenReturn(1000L);
            when(jwtService.getAccessExpirationMs()).thenReturn(1000L);
            when(sesionRepository.save(any())).thenReturn(new Sesion());
            when(usuarioRepository.save(u)).thenReturn(u);

            authService.login(LoginRequest.builder().correo("fw@test.com").contrasena("pass").build(), httpRequest);

            // Verificar que la sesión guarda IP correcta
            verify(sesionRepository).save(argThat(s -> "203.0.113.1".equals(s.getIpOrigen())));
        }
    }

    // ─── logout() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("PU-AUTH-13 | Token nulo no interactúa con el repositorio")
        void logoutTokenNulo() {
            authService.logout(null);
            verify(sesionRepository, never()).findByTokenHash(any());
        }

        @Test
        @DisplayName("PU-AUTH-14 | Token en blanco no interactúa con el repositorio")
        void logoutTokenBlanco() {
            authService.logout("  ");
            verify(sesionRepository, never()).findByTokenHash(any());
        }

        @Test
        @DisplayName("PU-AUTH-15 | Token válido cierra la sesión estableciendo cerradoEn")
        void logoutTokenValido_cierraSesion() {
            Sesion sesion = new Sesion();
            sesion.setCerradoEn(null);
            when(jwtService.hashToken("rt")).thenReturn("h");
            when(sesionRepository.findByTokenHash("h")).thenReturn(Optional.of(sesion));
            when(sesionRepository.save(any())).thenReturn(sesion);

            authService.logout("rt");

            assertThat(sesion.getCerradoEn()).isNotNull();
            verify(sesionRepository).save(sesion);
        }

        @Test
        @DisplayName("PU-AUTH-16 | Sesión ya cerrada no se vuelve a actualizar")
        void logoutSesionYaCerrada_noActualiza() {
            Sesion sesion = new Sesion();
            sesion.setCerradoEn(LocalDateTime.now().minusHours(1));
            when(jwtService.hashToken("rt")).thenReturn("h");
            when(sesionRepository.findByTokenHash("h")).thenReturn(Optional.of(sesion));

            authService.logout("rt");

            verify(sesionRepository, never()).save(any());
        }
    }

    // ─── forgotPassword() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPasswordTests {

        @Test
        @DisplayName("PU-AUTH-17 | Correo no registrado no lanza excepción (evita enumeración)")
        void correoNoRegistrado_noLanzaExcepcion() {
            when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());

            assertThatCode(() -> authService.forgotPassword("ghost@test.com"))
                    .doesNotThrowAnyException();
            verify(emailService, never()).enviarResetPassword(any(), any());
        }

        @Test
        @DisplayName("PU-AUTH-18 | Correo registrado genera token y envía email")
        void correoRegistrado_enviaReset() throws Exception {
            Usuario u = usuarioActivo("user@test.com");
            when(usuarioRepository.findByCorreo("user@test.com")).thenReturn(Optional.of(u));
            when(jwtService.generateResetPasswordToken(u)).thenReturn("reset-tok");

            authService.forgotPassword("user@test.com");

            verify(emailService).enviarResetPassword(u, "reset-tok");
        }
    }
}
