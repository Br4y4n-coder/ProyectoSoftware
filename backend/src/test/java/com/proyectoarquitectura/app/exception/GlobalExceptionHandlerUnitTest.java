package com.proyectoarquitectura.app.exception;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PRUEBAS UNITARIAS — GlobalExceptionHandler
 * JUnit 5 puro (sin contexto Spring): se invoca cada @ExceptionHandler
 * directamente y se verifica el status del ResponseEntity construido.
 */
@DisplayName("GlobalExceptionHandler — Pruebas Unitarias")
class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // Método de apoyo para construir un MethodParameter real.
    @SuppressWarnings("unused")
    private void metodoDummy(String argumento) {
        // intencionalmente vacío
    }

    @Test
    @DisplayName("PU-GEH-01 | AuthException unauthorized → 401 con su mensaje")
    void authExceptionRetornaSuStatus() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleAuth(AuthException.unauthorized("Credenciales invalidas"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(401);
        assertThat(resp.getBody().getMessage()).isEqualTo("Credenciales invalidas");
    }

    @Test
    @DisplayName("PU-GEH-02 | AuthException conflict → 409")
    void authExceptionConflictRetorna409() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleAuth(AuthException.conflict("El correo ya existe"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("PU-GEH-03 | BusinessException forbidden → 403 con su mensaje")
    void businessExceptionRetornaSuStatus() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleBusiness(BusinessException.forbidden("Operacion no permitida"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Operacion no permitida");
    }

    @Test
    @DisplayName("PU-GEH-04 | NotFoundException → 404 con su mensaje")
    void notFoundExceptionRetorna404() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleNotFound(new NotFoundException("Ticket no encontrado"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(404);
        assertThat(resp.getBody().getMessage()).isEqualTo("Ticket no encontrado");
    }

    @Test
    @DisplayName("PU-GEH-05 | MethodArgumentNotValidException → 400 con mapa de errores por campo")
    void validacionRetorna400ConErrores() throws Exception {
        Method metodo = GlobalExceptionHandlerUnitTest.class
                .getDeclaredMethod("metodoDummy", String.class);
        MethodParameter parametro = new MethodParameter(metodo, 0);
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "correo", "no debe estar vacio"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parametro, bindingResult);

        ResponseEntity<ApiResponse<Object>> resp = handler.handleValidation(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Datos invalidos");
        @SuppressWarnings("unchecked")
        Map<String, String> errores = (Map<String, String>) resp.getBody().getData();
        assertThat(errores).containsEntry("correo", "no debe estar vacio");
    }

    @Test
    @DisplayName("PU-GEH-06 | BadCredentialsException → 401 'No autenticado'")
    void badCredentialsRetorna401() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleSecurityAuth(new BadCredentialsException("Bad credentials"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("No autenticado");
        assertThat(resp.getBody().getError()).isEqualTo("Bad credentials");
    }

    @Test
    @DisplayName("PU-GEH-07 | DisabledException y LockedException → 401")
    void disabledYLockedRetornan401() {
        assertThat(handler.handleSecurityAuth(new DisabledException("Cuenta deshabilitada"))
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.handleSecurityAuth(new LockedException("Cuenta bloqueada"))
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("PU-GEH-08 | AccessDeniedException → 403 'Acceso denegado'")
    void accessDeniedRetorna403() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleAccessDenied(new AccessDeniedException("denegado"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Acceso denegado");
    }

    @Test
    @DisplayName("PU-GEH-09 | IllegalArgumentException → 400 con su mensaje")
    void illegalArgumentRetorna400() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleIllegalArg(new IllegalArgumentException("Parametro invalido"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Parametro invalido");
    }

    @Test
    @DisplayName("PU-GEH-10 | Excepción genérica → 500 con el nombre de la clase como error")
    void excepcionGenericaRetorna500() {
        ResponseEntity<ApiResponse<Object>> resp =
                handler.handleGeneric(new IllegalStateException("boom"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Error interno del servidor");
        assertThat(resp.getBody().getError()).isEqualTo("IllegalStateException");
        assertThat(resp.getBody().getTimestamp()).isNotNull();
    }
}
