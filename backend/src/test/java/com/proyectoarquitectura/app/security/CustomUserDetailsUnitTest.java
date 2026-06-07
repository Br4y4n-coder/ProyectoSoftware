package com.proyectoarquitectura.app.security;

import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PRUEBAS UNITARIAS — CustomUserDetails
 * JUnit 5 puro (sin contexto Spring ni Mockito): prueba directa de los
 * métodos de la implementación de UserDetails.
 */
@DisplayName("CustomUserDetails — Pruebas Unitarias")
class CustomUserDetailsUnitTest {

    private Usuario usuario(String estado, String nombreRol) {
        Usuario u = new Usuario();
        u.setId(1);
        u.setNombres("Test");
        u.setApellidos("User");
        u.setCorreo("test@test.com");
        u.setContrasenaHash("hash-123");
        u.setEstado(estado);
        if (nombreRol != null) {
            Rol rol = new Rol();
            rol.setNombre(nombreRol);
            u.setRol(rol);
        }
        return u;
    }

    @Test
    @DisplayName("PU-CUD-01 | getAuthorities() retorna ROLE_<ROL> en mayúsculas")
    void getAuthoritiesConRol() {
        CustomUserDetails details = new CustomUserDetails(usuario("activo", "administrador"));

        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMINISTRADOR");
    }

    @Test
    @DisplayName("PU-CUD-02 | getAuthorities() sin rol retorna lista vacía")
    void getAuthoritiesSinRol() {
        CustomUserDetails details = new CustomUserDetails(usuario("activo", null));

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("PU-CUD-03 | getAuthorities() con rol cuyo nombre es nulo retorna lista vacía")
    void getAuthoritiesRolConNombreNulo() {
        Usuario u = usuario("activo", null);
        u.setRol(new Rol()); // rol presente pero sin nombre

        CustomUserDetails details = new CustomUserDetails(u);

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("PU-CUD-04 | getPassword() y getUsername() exponen hash y correo del usuario")
    void getPasswordYUsername() {
        CustomUserDetails details = new CustomUserDetails(usuario("activo", "cliente"));

        assertThat(details.getPassword()).isEqualTo("hash-123");
        assertThat(details.getUsername()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("PU-CUD-05 | isAccountNonExpired() es false solo cuando el estado es 'eliminado'")
    void isAccountNonExpired() {
        assertThat(new CustomUserDetails(usuario("eliminado", "cliente")).isAccountNonExpired()).isFalse();
        assertThat(new CustomUserDetails(usuario("ELIMINADO", "cliente")).isAccountNonExpired()).isFalse();
        assertThat(new CustomUserDetails(usuario("activo", "cliente")).isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("PU-CUD-06 | isAccountNonLocked() es false cuando el estado es 'suspendido'")
    void isAccountNonLockedSuspendido() {
        assertThat(new CustomUserDetails(usuario("suspendido", "cliente")).isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("PU-CUD-07 | isAccountNonLocked() es false con bloqueo vigente y true con bloqueo vencido o nulo")
    void isAccountNonLockedBloqueo() {
        Usuario bloqueado = usuario("activo", "cliente");
        bloqueado.setBloqueadoHasta(LocalDateTime.now().plusMinutes(30));
        assertThat(new CustomUserDetails(bloqueado).isAccountNonLocked()).isFalse();

        Usuario bloqueoVencido = usuario("activo", "cliente");
        bloqueoVencido.setBloqueadoHasta(LocalDateTime.now().minusMinutes(5));
        assertThat(new CustomUserDetails(bloqueoVencido).isAccountNonLocked()).isTrue();

        Usuario sinBloqueo = usuario("activo", "cliente");
        assertThat(new CustomUserDetails(sinBloqueo).isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("PU-CUD-08 | isCredentialsNonExpired() siempre retorna true")
    void isCredentialsNonExpired() {
        assertThat(new CustomUserDetails(usuario("activo", "cliente")).isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("PU-CUD-09 | isEnabled() es true solo con estado 'activo' (sin importar mayúsculas)")
    void isEnabled() {
        assertThat(new CustomUserDetails(usuario("activo", "cliente")).isEnabled()).isTrue();
        assertThat(new CustomUserDetails(usuario("ACTIVO", "cliente")).isEnabled()).isTrue();
        assertThat(new CustomUserDetails(usuario("pendiente", "cliente")).isEnabled()).isFalse();
        assertThat(new CustomUserDetails(usuario("suspendido", "cliente")).isEnabled()).isFalse();
    }

    @Test
    @DisplayName("PU-CUD-10 | getUsuario() expone la entidad envuelta")
    void getUsuarioExponeEntidad() {
        Usuario u = usuario("activo", "cliente");
        CustomUserDetails details = new CustomUserDetails(u);

        assertThat(details.getUsuario()).isSameAs(u);
    }
}
