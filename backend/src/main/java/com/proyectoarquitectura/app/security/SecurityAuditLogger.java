package com.proyectoarquitectura.app.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecurityAuditLogger {

    private static final Logger SEGURIDAD = LoggerFactory.getLogger("SEGURIDAD");

    private SecurityAuditLogger() {
    }

    public static void loginFallido(String email, String ip) {
        SEGURIDAD.warn("LOGIN_FALLIDO | email={} | ip={}", email, ip);
    }

    public static void cambioRol(Integer usuarioId, String email, String rolAnterior, String rolNuevo, Integer adminId) {
        SEGURIDAD.info("CAMBIO_ROL | usuarioId={} | email={} | rolAnterior={} | rolNuevo={} | adminId={}",
                usuarioId, email, rolAnterior, rolNuevo, adminId);
    }

    public static void accesoEndpointSensible(String metodo, String uri, String email, String ip) {
        SEGURIDAD.info("ACCESO_SENSIBLE | {} {} | usuario={} | ip={}", metodo, uri, email, ip);
    }
}
