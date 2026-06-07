package com.proyectoarquitectura.app.controller.usuarios;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.dto.usuarios.ActualizarUsuarioRequest;
import com.proyectoarquitectura.app.models.dto.usuarios.CambiarEstadoUsuarioRequest;
import com.proyectoarquitectura.app.models.dto.usuarios.CambiarRolRequest;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.usuarios.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — UsuarioController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController — Pruebas Unitarias")
class UsuarioControllerUnitTest {

    @Mock private UsuarioService usuarioService;

    private UsuarioController controller;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        controller = new UsuarioController(usuarioService);
    }

    private CustomUserDetails admin(int id) {
        Rol rol = new Rol();
        rol.setNombre("administrador");
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombres("Admin");
        u.setApellidos("Test");
        u.setCorreo("admin@test.com");
        u.setContrasenaHash("hash");
        u.setEstado("activo");
        u.setRol(rol);
        return new CustomUserDetails(u);
    }

    @Test
    @DisplayName("PU-USR-01 | listarUsuarios() retorna 200 con la página de usuarios")
    void listarUsuariosRetorna200() {
        Page<UsuarioResponse> esperado = new PageImpl<>(List.of(UsuarioResponse.builder().id(1).build()));
        when(usuarioService.listarUsuarios(pageable)).thenReturn(esperado);

        ResponseEntity<ApiResponse<Page<UsuarioResponse>>> resp = controller.listarUsuarios(pageable);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(usuarioService).listarUsuarios(pageable);
    }

    @Test
    @DisplayName("PU-USR-02 | cambiarRol() con admin autenticado retorna 200 y delega en el servicio")
    void cambiarRolAutenticadoRetorna200() {
        UsuarioResponse esperado = UsuarioResponse.builder().id(9).rol("agente").build();
        CambiarRolRequest req = CambiarRolRequest.builder().rol("agente").build();
        when(usuarioService.cambiarRol(9, "agente", 1)).thenReturn(esperado);

        ResponseEntity<ApiResponse<UsuarioResponse>> resp = controller.cambiarRol(9, req, admin(1));

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Rol actualizado");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(usuarioService).cambiarRol(9, "agente", 1);
    }

    @Test
    @DisplayName("PU-USR-03 | cambiarRol() sin autenticación lanza AuthException 401")
    void cambiarRolSinAutenticacionLanza401() {
        CambiarRolRequest req = CambiarRolRequest.builder().rol("agente").build();

        assertThatThrownBy(() -> controller.cambiarRol(9, req, null))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("No autenticado");
        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("PU-USR-04 | actualizar() con admin autenticado retorna 200 con el usuario actualizado")
    void actualizarAutenticadoRetorna200() {
        UsuarioResponse esperado = UsuarioResponse.builder().id(4).nombres("Nuevo").build();
        ActualizarUsuarioRequest req = ActualizarUsuarioRequest.builder().nombres("Nuevo").build();
        when(usuarioService.actualizar(4, req, 2)).thenReturn(esperado);

        ResponseEntity<ApiResponse<UsuarioResponse>> resp = controller.actualizar(4, req, admin(2));

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Usuario actualizado");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(usuarioService).actualizar(4, req, 2);
    }

    @Test
    @DisplayName("PU-USR-05 | actualizar() sin autenticación lanza AuthException 401")
    void actualizarSinAutenticacionLanza401() {
        ActualizarUsuarioRequest req = ActualizarUsuarioRequest.builder().nombres("X").build();

        assertThatThrownBy(() -> controller.actualizar(4, req, null))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("No autenticado");
        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("PU-USR-06 | cambiarEstado() con admin autenticado retorna 200 con el estado actualizado")
    void cambiarEstadoAutenticadoRetorna200() {
        UsuarioResponse esperado = UsuarioResponse.builder().id(6).estado("suspendido").build();
        CambiarEstadoUsuarioRequest req = CambiarEstadoUsuarioRequest.builder().estado("suspendido").build();
        when(usuarioService.cambiarEstado(6, "suspendido", 3)).thenReturn(esperado);

        ResponseEntity<ApiResponse<UsuarioResponse>> resp = controller.cambiarEstado(6, req, admin(3));

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Estado actualizado");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(usuarioService).cambiarEstado(6, "suspendido", 3);
    }

    @Test
    @DisplayName("PU-USR-07 | cambiarEstado() sin autenticación lanza AuthException 401")
    void cambiarEstadoSinAutenticacionLanza401() {
        CambiarEstadoUsuarioRequest req = CambiarEstadoUsuarioRequest.builder().estado("activo").build();

        assertThatThrownBy(() -> controller.cambiarEstado(6, req, null))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("No autenticado");
        verifyNoInteractions(usuarioService);
    }
}
