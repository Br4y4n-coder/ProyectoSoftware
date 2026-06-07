package com.proyectoarquitectura.app.service.usuarios;

import com.proyectoarquitectura.app.exception.BusinessException;
import com.proyectoarquitectura.app.exception.NotFoundException;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.dto.usuarios.ActualizarUsuarioRequest;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — UsuarioServiceImpl
 *
 * Tipo de caja: BLANCA — se conoce la implementación interna y se prueban
 * todas las ramas de lógica condicional del servicio.
 *
 * Herramienta: JUnit 5 + Mockito (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UsuarioServiceImpl — Pruebas Unitarias (Caja Blanca)")
class UsuarioServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;

    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    private static final Integer USUARIO_ID = 10;
    private static final Integer ADMIN_ID = 1;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, rolRepository);
        usuario = usuarioBase();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Usuario usuarioBase() {
        Rol rol = new Rol();
        rol.setId(1);
        rol.setNombre("usuario");

        Usuario u = new Usuario();
        u.setId(USUARIO_ID);
        u.setNombres("Carlos");
        u.setApellidos("Lopez");
        u.setCorreo("carlos@test.com");
        u.setEstado("activo");
        u.setRol(rol);
        u.setTelefono("3001112233");
        return u;
    }

    private void stubSaveDevuelveArgumento() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── cambiarRol() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarRol()")
    class CambiarRolTests {

        @Test
        @DisplayName("PU-USR-01 | Cambio de rol exitoso normaliza a minusculas y guarda")
        void cambioRolExitoso() {
            Rol rolAgente = new Rol();
            rolAgente.setId(2);
            rolAgente.setNombre("agente");

            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            when(rolRepository.findByNombre("agente")).thenReturn(Optional.of(rolAgente));
            stubSaveDevuelveArgumento();

            UsuarioResponse r = usuarioService.cambiarRol(USUARIO_ID, "AGENTE", ADMIN_ID);

            assertThat(r.getRol()).isEqualTo("agente");
            assertThat(r.getId()).isEqualTo(USUARIO_ID);
            verify(rolRepository).findByNombre("agente");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("PU-USR-02 | Usuario inexistente lanza NotFoundException")
        void usuarioInexistente() {
            when(usuarioRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.cambiarRol(404, "agente", ADMIN_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("PU-USR-03 | Rol invalido lanza BusinessException 400")
        void rolInvalido() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            when(rolRepository.findByNombre("superheroe")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.cambiarRol(USUARIO_ID, "superheroe", ADMIN_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Rol no valido")
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    // ─── cambiarEstado() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarEstado()")
    class CambiarEstadoTests {

        @Test
        @DisplayName("PU-USR-04 | Cambio de estado exitoso normaliza a minusculas")
        void cambioEstadoExitoso() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            stubSaveDevuelveArgumento();

            UsuarioResponse r = usuarioService.cambiarEstado(USUARIO_ID, "SUSPENDIDO", ADMIN_ID);

            assertThat(r.getEstado()).isEqualTo("suspendido");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("PU-USR-05 | Usuario inexistente lanza NotFoundException")
        void usuarioInexistente() {
            when(usuarioRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.cambiarEstado(404, "activo", ADMIN_ID))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("PU-USR-06 | Cambiar el estado de la propia cuenta lanza BusinessException")
        void propiaCuentaLanzaBusinessException() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> usuarioService.cambiarEstado(USUARIO_ID, "suspendido", USUARIO_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("propia cuenta");

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    // ─── actualizar() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("PU-USR-07 | Actualiza todos los campos aplicando trim y minusculas")
        void actualizaTodosLosCampos() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            stubSaveDevuelveArgumento();

            ActualizarUsuarioRequest req = ActualizarUsuarioRequest.builder()
                    .nombres("  Ana Maria  ")
                    .apellidos("  Perez  ")
                    .telefono("  3209998877  ")
                    .nivelAgente(2)
                    .estado("SUSPENDIDO")
                    .build();

            UsuarioResponse r = usuarioService.actualizar(USUARIO_ID, req, ADMIN_ID);

            assertThat(r.getNombres()).isEqualTo("Ana Maria");
            assertThat(r.getApellidos()).isEqualTo("Perez");
            assertThat(usuario.getTelefono()).isEqualTo("3209998877");
            assertThat(r.getNivelAgente()).isEqualTo(2);
            assertThat(r.getEstado()).isEqualTo("suspendido");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("PU-USR-08 | Campos null o en blanco no modifican el usuario")
        void camposNullNoModifican() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            stubSaveDevuelveArgumento();

            ActualizarUsuarioRequest req = ActualizarUsuarioRequest.builder()
                    .nombres("   ")
                    .apellidos(null)
                    .estado("  ")
                    .build();

            UsuarioResponse r = usuarioService.actualizar(USUARIO_ID, req, ADMIN_ID);

            assertThat(r.getNombres()).isEqualTo("Carlos");
            assertThat(r.getApellidos()).isEqualTo("Lopez");
            assertThat(r.getEstado()).isEqualTo("activo");
            assertThat(usuario.getTelefono()).isEqualTo("3001112233");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("PU-USR-09 | Cambiar estado de la propia cuenta lanza BusinessException y no guarda")
        void estadoEnPropiaCuentaLanzaExcepcion() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

            ActualizarUsuarioRequest req = ActualizarUsuarioRequest.builder()
                    .estado("suspendido")
                    .build();

            assertThatThrownBy(() -> usuarioService.actualizar(USUARIO_ID, req, USUARIO_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("propia cuenta");

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("PU-USR-10 | Usuario inexistente lanza NotFoundException")
        void usuarioInexistente() {
            when(usuarioRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.actualizar(
                    404, ActualizarUsuarioRequest.builder().build(), ADMIN_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ─── listarUsuarios() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarUsuarios()")
    class ListarUsuariosTests {

        @Test
        @DisplayName("PU-USR-11 | Lista paginada se mapea a UsuarioResponse")
        void listaPaginada() {
            Pageable pageable = PageRequest.of(0, 10);
            when(usuarioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(usuario)));

            Page<UsuarioResponse> page = usuarioService.listarUsuarios(pageable);

            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getCorreo()).isEqualTo("carlos@test.com");
            assertThat(page.getContent().get(0).getRol()).isEqualTo("usuario");
            verify(usuarioRepository).findAll(pageable);
        }
    }
}
