package com.proyectoarquitectura.app.controller.tickets;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.AsignarAgenteRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CambiarEstadoRequest;
import com.proyectoarquitectura.app.models.dto.tickets.ComentarioTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.tickets.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — TicketController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController — Pruebas Unitarias")
class TicketControllerUnitTest {

    @Mock private TicketService ticketService;

    private TicketController controller;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        controller = new TicketController(ticketService);
    }

    private CustomUserDetails usuario(int id) {
        Rol rol = new Rol();
        rol.setNombre("cliente");
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombres("Cliente");
        u.setApellidos("Test");
        u.setCorreo("cliente@test.com");
        u.setContrasenaHash("hash");
        u.setEstado("activo");
        u.setRol(rol);
        return new CustomUserDetails(u);
    }

    // ─── crear() ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crear()")
    class CrearTests {

        @Test
        @DisplayName("PU-TKT-01 | crear() con usuario autenticado retorna 201 con el ticket creado")
        void crearAutenticadoRetorna201() {
            CreateTicketRequest req = CreateTicketRequest.builder().build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.crear(req, 5)).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.crear(req, usuario(5));

            assertThat(resp.getStatusCode().value()).isEqualTo(201);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getStatus()).isEqualTo(201);
            assertThat(resp.getBody().getMessage()).isEqualTo("Ticket creado");
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).crear(req, 5);
        }

        @Test
        @DisplayName("PU-TKT-02 | crear() sin autenticación lanza AuthException 401")
        void crearSinAutenticacionLanza401() {
            CreateTicketRequest req = CreateTicketRequest.builder().build();

            assertThatThrownBy(() -> controller.crear(req, null))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("No autenticado");
            verifyNoInteractions(ticketService);
        }
    }

    // ─── validarActivo() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("validarActivo()")
    class ValidarActivoTests {

        @Test
        @DisplayName("PU-TKT-03 | validarActivo() con usuario autenticado retorna 200")
        void validarActivoRetorna200() {
            ValidacionTicketActivoResponse esperado = mock(ValidacionTicketActivoResponse.class);
            when(ticketService.validarTicketActivo("Soporte", 7)).thenReturn(esperado);

            ResponseEntity<ApiResponse<ValidacionTicketActivoResponse>> resp =
                    controller.validarActivo("Soporte", usuario(7));

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).validarTicketActivo("Soporte", 7);
        }

        @Test
        @DisplayName("PU-TKT-04 | validarActivo() sin autenticación lanza AuthException 401")
        void validarActivoSinAutenticacionLanza401() {
            assertThatThrownBy(() -> controller.validarActivo("Soporte", null))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("No autenticado");
            verifyNoInteractions(ticketService);
        }
    }

    // ─── buscar() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PU-TKT-05 | buscar() retorna 200 delegando todos los filtros al servicio")
    void buscarRetorna200ConFiltros() {
        Page<TicketResponse> esperado = new PageImpl<>(List.of(new TicketResponse()));
        LocalDateTime desde = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 2, 1, 0, 0);
        when(ticketService.buscar("ABIERTO", "ALTA", "INCIDENTE", desde, hasta, 1, 2, pageable))
                .thenReturn(esperado);

        ResponseEntity<ApiResponse<Page<TicketResponse>>> resp =
                controller.buscar("ABIERTO", "ALTA", "INCIDENTE", desde, hasta, 1, 2, pageable);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(ticketService).buscar("ABIERTO", "ALTA", "INCIDENTE", desde, hasta, 1, 2, pageable);
    }

    // ─── mios() ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("mios()")
    class MiosTests {

        @Test
        @DisplayName("PU-TKT-06 | mios() con usuario autenticado retorna 200 con sus tickets")
        void miosRetorna200() {
            Page<TicketResponse> esperado = new PageImpl<>(List.of(new TicketResponse()));
            when(ticketService.listarPorCliente(8, pageable)).thenReturn(esperado);

            ResponseEntity<ApiResponse<Page<TicketResponse>>> resp = controller.mios(usuario(8), pageable);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).listarPorCliente(8, pageable);
        }

        @Test
        @DisplayName("PU-TKT-07 | mios() sin autenticación lanza AuthException 401")
        void miosSinAutenticacionLanza401() {
            assertThatThrownBy(() -> controller.mios(null, pageable))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("No autenticado");
            verifyNoInteractions(ticketService);
        }
    }

    // ─── listar() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listar()")
    class ListarTests {

        @Test
        @DisplayName("PU-TKT-08 | listar() con agenteId filtra por agente")
        void listarConAgenteIdFiltraPorAgente() {
            Page<TicketResponse> esperado = new PageImpl<>(List.of(new TicketResponse()));
            when(ticketService.listarPorAgente(3, pageable)).thenReturn(esperado);

            ResponseEntity<ApiResponse<Page<TicketResponse>>> resp =
                    controller.listar("ABIERTO", 3, pageable);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).listarPorAgente(3, pageable);
            verify(ticketService, never()).listarPorEstado(anyString(), any());
            verify(ticketService, never()).listar(any());
        }

        @Test
        @DisplayName("PU-TKT-09 | listar() solo con estado filtra por estado")
        void listarConEstadoFiltraPorEstado() {
            Page<TicketResponse> esperado = new PageImpl<>(List.of(new TicketResponse()));
            when(ticketService.listarPorEstado("CERRADO", pageable)).thenReturn(esperado);

            ResponseEntity<ApiResponse<Page<TicketResponse>>> resp =
                    controller.listar("CERRADO", null, pageable);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).listarPorEstado("CERRADO", pageable);
            verify(ticketService, never()).listarPorAgente(anyInt(), any());
            verify(ticketService, never()).listar(any());
        }

        @Test
        @DisplayName("PU-TKT-10 | listar() sin filtros lista todos los tickets")
        void listarSinFiltrosListaTodo() {
            Page<TicketResponse> esperado = new PageImpl<>(List.of(new TicketResponse()));
            when(ticketService.listar(pageable)).thenReturn(esperado);

            ResponseEntity<ApiResponse<Page<TicketResponse>>> resp =
                    controller.listar(null, null, pageable);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).listar(pageable);
            verify(ticketService, never()).listarPorAgente(anyInt(), any());
            verify(ticketService, never()).listarPorEstado(anyString(), any());
        }
    }

    // ─── consultas individuales ─────────────────────────────────────────────────

    @Test
    @DisplayName("PU-TKT-11 | obtenerPorCodigo() retorna 200 con el ticket")
    void obtenerPorCodigoRetorna200() {
        TicketResponse esperado = new TicketResponse();
        when(ticketService.obtenerPorCodigo("TK-0001")).thenReturn(esperado);

        ResponseEntity<ApiResponse<TicketResponse>> resp = controller.obtenerPorCodigo("TK-0001");

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(ticketService).obtenerPorCodigo("TK-0001");
    }

    @Test
    @DisplayName("PU-TKT-12 | obtener() retorna 200 con el ticket por ID")
    void obtenerRetorna200() {
        TicketResponse esperado = new TicketResponse();
        when(ticketService.obtenerPorId(11)).thenReturn(esperado);

        ResponseEntity<ApiResponse<TicketResponse>> resp = controller.obtener(11);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(ticketService).obtenerPorId(11);
    }

    @Test
    @DisplayName("PU-TKT-13 | historial() retorna 200 con la línea de tiempo del ticket")
    void historialRetorna200() {
        List<TicketHistoryResponse> esperado = List.of(TicketHistoryResponse.builder().build());
        when(ticketService.obtenerHistorial(11)).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<TicketHistoryResponse>>> resp = controller.historial(11);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(ticketService).obtenerHistorial(11);
    }

    // ─── actualizar() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("PU-TKT-14 | actualizar() con usuario autenticado pasa su ID como actor")
        void actualizarConActorRetorna200() {
            ActualizarTicketRequest req = ActualizarTicketRequest.builder().build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.actualizar(11, req, 4)).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.actualizar(11, req, usuario(4));

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getMessage()).isEqualTo("Ticket actualizado");
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).actualizar(11, req, 4);
        }

        @Test
        @DisplayName("PU-TKT-15 | actualizar() sin principal pasa actor nulo al servicio")
        void actualizarSinPrincipalActorNulo() {
            ActualizarTicketRequest req = ActualizarTicketRequest.builder().build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.actualizar(eq(11), eq(req), isNull())).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.actualizar(11, req, null);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).actualizar(eq(11), eq(req), isNull());
        }
    }

    // ─── asignar() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("asignar()")
    class AsignarTests {

        @Test
        @DisplayName("PU-TKT-16 | asignar() con usuario autenticado asigna el agente indicado")
        void asignarConActorRetorna200() {
            AsignarAgenteRequest req = AsignarAgenteRequest.builder().agenteId(20).build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.asignarAgente(11, 20, 4)).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.asignar(11, req, usuario(4));

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getMessage()).isEqualTo("Agente asignado");
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).asignarAgente(11, 20, 4);
        }

        @Test
        @DisplayName("PU-TKT-17 | asignar() sin principal pasa actor nulo al servicio")
        void asignarSinPrincipalActorNulo() {
            AsignarAgenteRequest req = AsignarAgenteRequest.builder().agenteId(20).build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.asignarAgente(eq(11), eq(20), isNull())).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.asignar(11, req, null);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).asignarAgente(eq(11), eq(20), isNull());
        }
    }

    // ─── comentar() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("comentar()")
    class ComentarTests {

        @Test
        @DisplayName("PU-TKT-18 | comentar() con usuario autenticado retorna 201 con el comentario")
        void comentarAutenticadoRetorna201() {
            ComentarioTicketRequest req = ComentarioTicketRequest.builder().texto("Hola").build();
            TicketHistoryResponse esperado = TicketHistoryResponse.builder().build();
            when(ticketService.comentar(11, "Hola", 5)).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketHistoryResponse>> resp = controller.comentar(11, req, usuario(5));

            assertThat(resp.getStatusCode().value()).isEqualTo(201);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getStatus()).isEqualTo(201);
            assertThat(resp.getBody().getMessage()).isEqualTo("Comentario agregado");
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).comentar(11, "Hola", 5);
        }

        @Test
        @DisplayName("PU-TKT-19 | comentar() sin autenticación lanza AuthException 401")
        void comentarSinAutenticacionLanza401() {
            ComentarioTicketRequest req = ComentarioTicketRequest.builder().texto("Hola").build();

            assertThatThrownBy(() -> controller.comentar(11, req, null))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("No autenticado");
            verifyNoInteractions(ticketService);
        }
    }

    // ─── cambiarEstado() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarEstado()")
    class CambiarEstadoTests {

        @Test
        @DisplayName("PU-TKT-20 | cambiarEstado() con usuario autenticado retorna 200")
        void cambiarEstadoConActorRetorna200() {
            CambiarEstadoRequest req = CambiarEstadoRequest.builder().estado("RESUELTO").build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.cambiarEstado(11, "RESUELTO", 4)).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.cambiarEstado(11, req, usuario(4));

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getMessage()).isEqualTo("Estado actualizado");
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).cambiarEstado(11, "RESUELTO", 4);
        }

        @Test
        @DisplayName("PU-TKT-21 | cambiarEstado() sin principal pasa actor nulo al servicio")
        void cambiarEstadoSinPrincipalActorNulo() {
            CambiarEstadoRequest req = CambiarEstadoRequest.builder().estado("CERRADO").build();
            TicketResponse esperado = new TicketResponse();
            when(ticketService.cambiarEstado(eq(11), eq("CERRADO"), isNull())).thenReturn(esperado);

            ResponseEntity<ApiResponse<TicketResponse>> resp = controller.cambiarEstado(11, req, null);

            assertThat(resp.getStatusCode().value()).isEqualTo(200);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getData()).isSameAs(esperado);
            verify(ticketService).cambiarEstado(eq(11), eq("CERRADO"), isNull());
        }
    }
}
