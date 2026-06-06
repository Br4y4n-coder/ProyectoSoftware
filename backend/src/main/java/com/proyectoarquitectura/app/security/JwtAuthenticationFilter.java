package com.proyectoarquitectura.app.security;

import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        log.info("=== JWT Filter ejecutándose para: {} ===", request.getRequestURI());
        
        String header = request.getHeader(HEADER);
        log.info("Authorization header: {}", header != null ? header : "NO HAY HEADER");

        if (header == null || !header.startsWith(PREFIX)) {
            log.warn("No hay token Authorization o no empieza con Bearer");
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length()).trim();
        log.info("Token extraído: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

        try {
            Claims claims = jwtService.parseAndValidate(token);
            TokenPurpose purpose = jwtService.extractPurpose(claims);
            log.info("Token válido. Purpose: {}", purpose);
            
            if (purpose != TokenPurpose.ACCESS) {
                log.warn("Token no es de acceso, es: {}", purpose);
                chain.doFilter(request, response);
                return;
            }

            String correo = claims.getSubject();
            log.info("Email del token: {}", correo);
            
            if (correo == null) {
                log.warn("Email es null");
                chain.doFilter(request, response);
                return;
            }
            
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.info("Ya hay autenticación en contexto");
                chain.doFilter(request, response);
                return;
            }

            Optional<Usuario> opt = usuarioRepository.findByCorreoConRol(correo);
            if (opt.isEmpty()) {
                log.warn("Usuario no encontrado en BD: {}", correo);
                chain.doFilter(request, response);
                return;
            }

            Usuario u = opt.get();
            log.info("Usuario encontrado: {}, rol: {}", u.getCorreo(), u.getRol() != null ? u.getRol().getNombre() : "sin rol");
            
            CustomUserDetails details = new CustomUserDetails(u);

            if (!details.isEnabled()) {
                log.warn("Usuario no habilitado. Estado: {}", u.getEstado());
                chain.doFilter(request, response);
                return;
            }
            
            if (!details.isAccountNonLocked()) {
                log.warn("Usuario bloqueado hasta: {}", u.getBloqueadoHasta());
                chain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("✅ Autenticación exitosa para: {}", correo);
            
        } catch (JwtException e) {
            log.error("❌ JWT invalido: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado: {}", e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }
}