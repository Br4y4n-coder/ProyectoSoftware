package com.proyectoarquitectura.app.config;

import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.security.SecurityAuditLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SensitiveEndpointAuditFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (esEndpointSensible(uri) && SecurityContextHolder.getContext().getAuthentication() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            if (auth.getPrincipal() instanceof CustomUserDetails details) {
                email = details.getUsuario().getCorreo();
            }
            String ip = obtenerIp(request);
            SecurityAuditLogger.accesoEndpointSensible(request.getMethod(), uri, email, ip);
        }
        filterChain.doFilter(request, response);
    }

    private boolean esEndpointSensible(String uri) {
        return uri.startsWith("/api/usuarios") || uri.startsWith("/api/metrics");
    }

    private String obtenerIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
