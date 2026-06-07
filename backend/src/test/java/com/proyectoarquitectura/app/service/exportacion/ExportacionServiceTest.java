package com.proyectoarquitectura.app.service.exportacion;

import com.proyectoarquitectura.app.models.entity.LogAuditoria;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.LogAuditoriaRepository;
import com.proyectoarquitectura.app.repository.TicketRepository;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — ExportacionService
 *
 * Tipo de caja: BLANCA — se conoce la implementación interna y se prueban
 * los formatos de salida CSV/JSON y las ramas de escape de caracteres.
 *
 * Herramienta: JUnit 5 + Mockito (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ExportacionService — Pruebas Unitarias (Caja Blanca)")
class ExportacionServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LogAuditoriaRepository logAuditoriaRepository;

    private ExportacionService exportacionService;

    @BeforeEach
    void setUp() {
        exportacionService = new ExportacionService(
                ticketRepository, usuarioRepository, logAuditoriaRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Usuario usuario(Integer id, String nombres, String apellidos, String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setRol(rol);
        u.setEstado("activo");
        return u;
    }

    private Rol rol(String nombre) {
        Rol r = new Rol();
        r.setId(1);
        r.setNombre(nombre);
        return r;
    }

    private Ticket ticketCompleto() {
        Ticket t = new Ticket();
        t.setId(1);
        t.setCodigo("TKT-1");
        t.setAsunto("Falla de red");
        t.setDescripcion("Sin conexion");
        t.setTipo("problema");
        t.setPrioridad("alta");
        t.setEstado("abierto");
        t.setCliente(usuario(10, "Laura", "Gomez", "laura@test.com", rol("usuario")));
        t.setFechaCreacion(LocalDateTime.of(2026, 1, 15, 10, 30));
        t.setFechaCierre(LocalDateTime.of(2026, 1, 20, 16, 45));
        return t;
    }

    private LogAuditoria logAuditoria() {
        return LogAuditoria.builder()
                .id(1L)
                .usuario("admin@test.com")
                .accion("LOGIN")
                .detalles("Inicio de sesion exitoso")
                .ip("127.0.0.1")
                .fechaHora(LocalDateTime.of(2026, 3, 1, 9, 0))
                .build();
    }

    // ─── CSV ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Exportacion CSV")
    class CsvTests {

        @Test
        @DisplayName("PU-EXP-01 | exportarTicketsCSV incluye encabezado y datos del ticket")
        void ticketsCsvConDatos() {
            when(ticketRepository.findAll()).thenReturn(List.of(ticketCompleto()));

            String csv = new String(exportacionService.exportarTicketsCSV(), StandardCharsets.UTF_8);

            assertThat(csv).contains(
                    "ID,Codigo,Asunto,Descripcion,Tipo,Prioridad,Estado,Cliente,Fecha Creacion,Fecha Cierre");
            assertThat(csv)
                    .contains("TKT-1")
                    .contains("Falla de red")
                    .contains("laura@test.com")
                    .contains("2026-01-15T10:30")
                    .contains("2026-01-20T16:45");
            verify(ticketRepository).findAll();
        }

        @Test
        @DisplayName("PU-EXP-02 | exportarTicketsCSV escapa comas y comillas, y tolera cliente/fechas null")
        void ticketsCsvEscapadoYNulos() {
            Ticket t = ticketCompleto();
            t.setAsunto("Hola, mundo");
            t.setDescripcion("Dijo \"error\"");
            t.setCliente(null);
            t.setFechaCreacion(null);
            t.setFechaCierre(null);
            when(ticketRepository.findAll()).thenReturn(List.of(t));

            String csv = new String(exportacionService.exportarTicketsCSV(), StandardCharsets.UTF_8);

            assertThat(csv).contains("\"Hola, mundo\"");
            assertThat(csv).contains("\"Dijo \"\"error\"\"\"");
            assertThat(csv).contains("TKT-1");
        }

        @Test
        @DisplayName("PU-EXP-03 | exportarTicketsCSV sin tickets devuelve solo el encabezado")
        void ticketsCsvVacio() {
            when(ticketRepository.findAll()).thenReturn(List.of());

            String csv = new String(exportacionService.exportarTicketsCSV(), StandardCharsets.UTF_8);

            assertThat(csv.trim()).isEqualTo(
                    "ID,Codigo,Asunto,Descripcion,Tipo,Prioridad,Estado,Cliente,Fecha Creacion,Fecha Cierre");
        }

        @Test
        @DisplayName("PU-EXP-04 | exportarUsuariosCSV incluye encabezado, rol y tolera rol/fecha null")
        void usuariosCsv() {
            Usuario conRol = usuario(1, "Carlos", "Lopez", "carlos@test.com", rol("admin"));
            conRol.setCreadoEn(LocalDateTime.of(2026, 2, 1, 8, 0));
            Usuario sinRol = usuario(2, "Ana", "Gil", "ana@test.com", null);
            when(usuarioRepository.findAll()).thenReturn(List.of(conRol, sinRol));

            String csv = new String(exportacionService.exportarUsuariosCSV(), StandardCharsets.UTF_8);

            assertThat(csv).contains("ID,Nombres,Apellidos,Correo,Rol,Estado,Fecha Registro");
            assertThat(csv)
                    .contains("Carlos")
                    .contains("carlos@test.com")
                    .contains("admin")
                    .contains("2026-02-01T08:00")
                    .contains("ana@test.com");
            verify(usuarioRepository).findAll();
        }

        @Test
        @DisplayName("PU-EXP-05 | exportarAuditoriaCSV incluye encabezado y datos del log")
        void auditoriaCsv() {
            when(logAuditoriaRepository.findAll()).thenReturn(List.of(logAuditoria()));

            String csv = new String(exportacionService.exportarAuditoriaCSV(), StandardCharsets.UTF_8);

            assertThat(csv).contains("ID,Usuario,Accion,Detalles,IP,Fecha Hora");
            assertThat(csv)
                    .contains("admin@test.com")
                    .contains("LOGIN")
                    .contains("Inicio de sesion exitoso")
                    .contains("127.0.0.1")
                    .contains("2026-03-01T09:00");
            verify(logAuditoriaRepository).findAll();
        }
    }

    // ─── JSON ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Exportacion JSON")
    class JsonTests {

        @Test
        @DisplayName("PU-EXP-06 | exportarTicketsJSON arma un arreglo con los campos del ticket")
        void ticketsJsonConDatos() {
            Ticket t2 = ticketCompleto();
            t2.setId(2);
            t2.setCodigo("TKT-2");
            when(ticketRepository.findAll()).thenReturn(List.of(ticketCompleto(), t2));

            String json = exportacionService.exportarTicketsJSON();

            assertThat(json).startsWith("[").endsWith("]");
            assertThat(json)
                    .contains("\"id\":1")
                    .contains("\"codigo\":\"TKT-1\"")
                    .contains("\"asunto\":\"Falla de red\"")
                    .contains("\"prioridad\":\"alta\"")
                    .contains("\"estado\":\"abierto\"")
                    .contains("\"codigo\":\"TKT-2\"")
                    .contains("},{");
            verify(ticketRepository).findAll();
        }

        @Test
        @DisplayName("PU-EXP-07 | exportarTicketsJSON escapa comillas y saltos de linea")
        void ticketsJsonEscapado() {
            Ticket t = ticketCompleto();
            t.setAsunto("Pantalla \"azul\"");
            t.setDescripcion("linea1\nlinea2");
            when(ticketRepository.findAll()).thenReturn(List.of(t));

            String json = exportacionService.exportarTicketsJSON();

            assertThat(json).contains("Pantalla \\\"azul\\\"");
            assertThat(json).contains("linea1\\nlinea2");
        }

        @Test
        @DisplayName("PU-EXP-08 | exportarTicketsJSON sin datos devuelve arreglo vacio")
        void ticketsJsonVacio() {
            when(ticketRepository.findAll()).thenReturn(List.of());

            assertThat(exportacionService.exportarTicketsJSON()).isEqualTo("[]");
        }

        @Test
        @DisplayName("PU-EXP-09 | exportarUsuariosJSON incluye rol y deja vacio cuando es null")
        void usuariosJson() {
            Usuario conRol = usuario(1, "Carlos", "Lopez", "carlos@test.com", rol("admin"));
            Usuario sinRol = usuario(2, "Ana", "Gil", "ana@test.com", null);
            when(usuarioRepository.findAll()).thenReturn(List.of(conRol, sinRol));

            String json = exportacionService.exportarUsuariosJSON();

            assertThat(json)
                    .contains("\"id\":1")
                    .contains("\"nombres\":\"Carlos\"")
                    .contains("\"correo\":\"carlos@test.com\"")
                    .contains("\"rol\":\"admin\"")
                    .contains("\"rol\":\"\"")
                    .contains("\"estado\":\"activo\"");
            verify(usuarioRepository).findAll();
        }

        @Test
        @DisplayName("PU-EXP-10 | exportarAuditoriaJSON incluye accion, ip y fechaHora")
        void auditoriaJson() {
            when(logAuditoriaRepository.findAll()).thenReturn(List.of(logAuditoria()));

            String json = exportacionService.exportarAuditoriaJSON();

            assertThat(json)
                    .contains("\"id\":1")
                    .contains("\"usuario\":\"admin@test.com\"")
                    .contains("\"accion\":\"LOGIN\"")
                    .contains("\"ip\":\"127.0.0.1\"")
                    .contains("\"fechaHora\":\"2026-03-01T09:00\"");
            verify(logAuditoriaRepository).findAll();
        }
    }
}
