package com.proyectoarquitectura.app.service.auth;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.auth.AuthResponse;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.dto.auth.RegisterRequest;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.entity.LogAcceso;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Sesion;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.LogAccesoRepository;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.SesionRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.security.JwtService;
import com.proyectoarquitectura.app.security.SecurityAuditLogger;
import com.proyectoarquitectura.app.security.TokenPurpose;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SesionRepository sesionRepository;
    private final LogAccesoRepository logAccesoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final int maxIntentosFallidos;
    private final int bloqueoMinutos;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           SesionRepository sesionRepository,
                           LogAccesoRepository logAccesoRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           EmailService emailService,
                           @Value("${app.security.max-intentos-fallidos}") int maxIntentosFallidos,
                           @Value("${app.security.bloqueo-minutos}") int bloqueoMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sesionRepository = sesionRepository;
        this.logAccesoRepository = logAccesoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.maxIntentosFallidos = maxIntentosFallidos;
        this.bloqueoMinutos = bloqueoMinutos;
    }

    @Override
    @Transactional
    public UsuarioResponse register(RegisterRequest req) {
        if (usuarioRepository.existsByCorreo(req.getCorreo())) {
            throw AuthException.conflict("El correo ya esta registrado");
        }
        if (req.getNumeroDocumento() != null && !req.getNumeroDocumento().isBlank()
                && usuarioRepository.existsByNumeroDocumento(req.getNumeroDocumento())) {
            throw AuthException.conflict("El numero de documento ya esta registrado");
        }

        Rol rolUsuario = rolRepository.findByNombre("usuario")
                .orElseThrow(() -> new IllegalStateException("Rol 'usuario' no existe (ejecuta el seed)"));

        Usuario u = Usuario.builder()
                .nombres(req.getNombres().trim())
                .apellidos(req.getApellidos().trim())
                .correo(req.getCorreo().trim().toLowerCase())
                .contrasenaHash(passwordEncoder.encode(req.getContrasena()))
                .telefono(req.getTelefono())
                .tipoDocumento(req.getTipoDocumento())
                .numeroDocumento(req.getNumeroDocumento())
                .rol(rolUsuario)
                .estado("pendiente")
                .intentosFallidos(0)
                .build();

        u = usuarioRepository.save(u);

        String token = jwtService.generateVerifyEmailToken(u);
        try {
            emailService.enviarVerificacionCorreo(u, token);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el correo de verificacion a {}: {}", u.getCorreo(), ex.getMessage());
        }

        return UsuarioResponse.from(u);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Claims claims;
        try {
            claims = jwtService.parseAndValidate(token);
        } catch (JwtException e) {
            throw AuthException.badRequest("Token invalido o expirado");
        }
        if (jwtService.extractPurpose(claims) != TokenPurpose.VERIFY_EMAIL) {
            throw AuthException.badRequest("Token con proposito invalido");
        }

        Usuario u = usuarioRepository.findByCorreo(claims.getSubject())
                .orElseThrow(() -> AuthException.notFound("Usuario no encontrado"));

        if ("activo".equalsIgnoreCase(u.getEstado())) {
            return;
        }
        u.setEstado("activo");
        usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req, HttpServletRequest httpRequest) {
        String correo = req.getCorreo().trim().toLowerCase();
        String ip = obtenerIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Optional<Usuario> opt = usuarioRepository.findByCorreo(correo);
        if (opt.isEmpty()) {
            registrarLogAcceso(null, correo, ip, userAgent, false, "usuario_no_existe");
            SecurityAuditLogger.loginFallido(correo, ip);
            throw AuthException.unauthorized("Credenciales invalidas");
        }
        Usuario u = opt.get();

        if (u.getBloqueadoHasta() != null && u.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            registrarLogAcceso(u, correo, ip, userAgent, false, "cuenta_bloqueada");
            throw AuthException.unauthorized("Cuenta bloqueada temporalmente. Intenta mas tarde.");
        }
        if ("suspendido".equalsIgnoreCase(u.getEstado()) || "eliminado".equalsIgnoreCase(u.getEstado())) {
            registrarLogAcceso(u, correo, ip, userAgent, false, "cuenta_" + u.getEstado());
            throw AuthException.unauthorized("Cuenta no disponible");
        }
        if ("pendiente".equalsIgnoreCase(u.getEstado())) {
            registrarLogAcceso(u, correo, ip, userAgent, false, "cuenta_pendiente_verificacion");
            throw AuthException.unauthorized("Debes verificar tu correo antes de iniciar sesion");
        }

        if (!passwordEncoder.matches(req.getContrasena(), u.getContrasenaHash())) {
            int fallidos = (u.getIntentosFallidos() == null ? 0 : u.getIntentosFallidos()) + 1;
            u.setIntentosFallidos(fallidos);
            if (fallidos >= maxIntentosFallidos) {
                u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(bloqueoMinutos));
                u.setIntentosFallidos(0);
                log.warn("Usuario {} bloqueado por {} intentos fallidos", correo, maxIntentosFallidos);
            }
            usuarioRepository.save(u);
            registrarLogAcceso(u, correo, ip, userAgent, false, "contrasena_incorrecta");
            SecurityAuditLogger.loginFallido(correo, ip);
            throw AuthException.unauthorized("Credenciales invalidas");
        }

        u.setIntentosFallidos(0);
        u.setBloqueadoHasta(null);
        u.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(u);

        String access = jwtService.generateAccessToken(u);
        String refresh = jwtService.generateRefreshToken(u);

        Sesion sesion = Sesion.builder()
                .usuario(u)
                .tokenHash(jwtService.hashToken(refresh))
                .ipOrigen(ip)
                .userAgent(userAgent)
                .expiraEn(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getRefreshExpirationMs())))
                .build();
        sesionRepository.save(sesion);

        registrarLogAcceso(u, correo, ip, userAgent, true, "login_ok");

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessExpirationMs())
                .usuario(UsuarioResponse.from(u))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseAndValidate(refreshToken);
        } catch (JwtException e) {
            throw AuthException.unauthorized("Refresh token invalido o expirado");
        }
        if (jwtService.extractPurpose(claims) != TokenPurpose.REFRESH) {
            throw AuthException.unauthorized("Token con proposito invalido");
        }

        String hash = jwtService.hashToken(refreshToken);
        Sesion sesion = sesionRepository.findByTokenHash(hash)
                .orElseThrow(() -> AuthException.unauthorized("Sesion no encontrada"));
        if (sesion.getCerradoEn() != null) {
            throw AuthException.unauthorized("Sesion cerrada");
        }
        if (sesion.getExpiraEn() != null && sesion.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw AuthException.unauthorized("Sesion expirada");
        }

        Usuario u = usuarioRepository.findByCorreo(claims.getSubject())
                .orElseThrow(() -> AuthException.unauthorized("Usuario no encontrado"));

        String access = jwtService.generateAccessToken(u);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessExpirationMs())
                .usuario(UsuarioResponse.from(u))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        String hash = jwtService.hashToken(refreshToken);
        sesionRepository.findByTokenHash(hash).ifPresent(s -> {
            if (s.getCerradoEn() == null) {
                s.setCerradoEn(LocalDateTime.now());
                sesionRepository.save(s);
            }
        });
    }

    @Override
    @Transactional
    public void forgotPassword(String correo) {
        // Si el correo no existe igual respondemos 200 desde el controller para no filtrar que cuentas son validas.
        Optional<Usuario> opt = usuarioRepository.findByCorreo(correo.trim().toLowerCase());
        opt.ifPresent(u -> {
            String token = jwtService.generateResetPasswordToken(u);
            try {
                emailService.enviarResetPassword(u, token);
            } catch (Exception ex) {
                log.warn("No se pudo enviar el correo de reset a {}: {}", u.getCorreo(), ex.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String nuevaContrasena) {
        Claims claims;
        try {
            claims = jwtService.parseAndValidate(token);
        } catch (JwtException e) {
            throw AuthException.badRequest("Token invalido o expirado");
        }
        if (jwtService.extractPurpose(claims) != TokenPurpose.RESET_PASSWORD) {
            throw AuthException.badRequest("Token con proposito invalido");
        }

        Usuario u = usuarioRepository.findByCorreo(claims.getSubject())
                .orElseThrow(() -> AuthException.notFound("Usuario no encontrado"));

        u.setContrasenaHash(passwordEncoder.encode(nuevaContrasena));
        u.setIntentosFallidos(0);
        u.setBloqueadoHasta(null);
        usuarioRepository.save(u);
    }

    private void registrarLogAcceso(Usuario u, String correo, String ip, String userAgent,
                                    boolean exitoso, String motivo) {
        try {
            LogAcceso registro = LogAcceso.builder()
                    .usuario(u)
                    .correoIntento(correo)
                    .ipOrigen(ip)
                    .userAgent(userAgent)
                    .exitoso(exitoso)
                    .motivo(motivo)
                    .build();
            logAccesoRepository.save(registro);
        } catch (Exception e) {
            log.warn("No se pudo registrar log de acceso: {}", e.getMessage());
        }
    }

    private String obtenerIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
