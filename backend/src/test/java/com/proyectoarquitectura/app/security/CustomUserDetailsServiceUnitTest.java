package com.proyectoarquitectura.app.security;

import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — CustomUserDetailsService
 * JUnit 5 + Mockito puro (sin contexto Spring) con UsuarioRepository mockeado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService — Pruebas Unitarias")
class CustomUserDetailsServiceUnitTest {

    @Mock private UsuarioRepository usuarioRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(usuarioRepository);
    }

    @Test
    @DisplayName("PU-CUDS-01 | Usuario existente retorna CustomUserDetails con sus datos")
    void usuarioExistenteRetornaDetails() {
        Rol rol = new Rol();
        rol.setNombre("agente");
        Usuario u = new Usuario();
        u.setId(2);
        u.setCorreo("agente@test.com");
        u.setContrasenaHash("hash");
        u.setEstado("activo");
        u.setRol(rol);
        when(usuarioRepository.findByCorreoConRol("agente@test.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("agente@test.com");

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        assertThat(details.getUsername()).isEqualTo("agente@test.com");
        assertThat(((CustomUserDetails) details).getUsuario()).isSameAs(u);
        verify(usuarioRepository).findByCorreoConRol("agente@test.com");
    }

    @Test
    @DisplayName("PU-CUDS-02 | Usuario inexistente lanza UsernameNotFoundException")
    void usuarioInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByCorreoConRol("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost@test.com");
    }
}
