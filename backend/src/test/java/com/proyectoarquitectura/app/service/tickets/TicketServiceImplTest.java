package com.proyectoarquitectura.app.service.tickets;

import com.proyectoarquitectura.app.exception.NotFoundException;
import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.TicketHistory;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.TicketHistoryRepository;
import com.proyectoarquitectura.app.repository.TicketRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.service.auth.EmailService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — TicketServiceImpl
 *
 * Tipo de caja: BLANCA — se conoce la implementación interna y se prueban
 * todas las ramas de lógica condicional del servicio.
 *
 * Herramienta: JUnit 5 + Mockito (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TicketServiceImpl — Pruebas Unitarias (Caja Blanca)")
class TicketServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private TicketHistoryRepository ticketHistoryRepository;
    @Mock private EmailService emailService;

    @Captor private ArgumentCaptor<TicketHistory> historyCaptor;
    @Captor private ArgumentCaptor<Specification<Ticket>> specCaptor;

    private TicketServiceImpl ticketService;

    private Usuario cliente;
    private Usuario agente;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(
                usuarioRepository, ticketRepository, ticketHistoryRepository, emailService);
        cliente = usuario(1, "Laura", "Gomez", "laura@test.com");
        agente  = usuario(2, "Pedro", "Ruiz", "pedro@test.com");
        ticket  = ticketBase();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Usuario usuario(Integer id, String nombres, String apellidos, String correo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setEstado("activo");
        return u;
    }

    private Ticket ticketBase() {
        Ticket t = new Ticket();
        t.setId(100);
        t.setCodigo("TKT-100");
        t.setAsunto("Asunto original");
        t.setDescripcion("Descripcion original");
        t.setTipo("problema");
        t.setPrioridad("media");
        t.setEstado("abierto");
        t.setCliente(cliente);
        t.setFechaCreacion(LocalDateTime.of(2026, 1, 10, 8, 0));
        return t;
    }

    private void stubSaveTicketDevuelveArgumento() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubSaveHistorialDevuelveArgumento() {
        when(ticketHistoryRepository.save(any(TicketHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── crear() ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crear()")
    class CrearTests {

        @Test
        @DisplayName("PU-TKT-01 | Crear ticket exitoso: estado 'abierto', historial y email de creacion")
        void crearExitoso() {
            CreateTicketRequest req = CreateTicketRequest.builder()
                    .asunto("Falla impresora")
                    .descripcion("No imprime nada")
                    .tipo("problema")
                    .prioridad("alta")
                    .categoriaId(1)
                    .build();

            when(usuarioRepository.findById(1)).thenReturn(Optional.of(cliente));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
                Ticket t = inv.getArgument(0);
                t.setId(200);
                return t;
            });
            stubSaveHistorialDevuelveArgumento();

            TicketResponse r = ticketService.crear(req, 1);

            assertThat(r.getId()).isEqualTo(200);
            assertThat(r.getAsunto()).isEqualTo("Falla impresora");
            assertThat(r.getEstado()).isEqualTo("abierto");
            assertThat(r.getPrioridad()).isEqualTo("alta");
            assertThat(r.getClienteId()).isEqualTo(1);
            assertThat(r.getCodigo()).startsWith("TKT-");

            verify(ticketRepository).save(any(Ticket.class));
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getCampoModificado()).isEqualTo("creacion");
            assertThat(historyCaptor.getValue().getValorAnterior()).isNull();
            verify(emailService).notificarTicketCreado(any(Ticket.class));
        }

        @Test
        @DisplayName("PU-TKT-02 | Cliente inexistente lanza NotFoundException y no guarda nada")
        void clienteInexistente() {
            when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

            CreateTicketRequest req = CreateTicketRequest.builder()
                    .asunto("X").descripcion("Y").tipo("consulta").categoriaId(1)
                    .build();

            assertThatThrownBy(() -> ticketService.crear(req, 99))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");

            verify(ticketRepository, never()).save(any(Ticket.class));
            verify(emailService, never()).notificarTicketCreado(any());
        }
    }

    // ─── obtenerPorId() / obtenerPorCodigo() ────────────────────────────────────

    @Nested
    @DisplayName("obtenerPorId() / obtenerPorCodigo()")
    class ObtenerTests {

        @Test
        @DisplayName("PU-TKT-03 | obtenerPorId con ticket existente retorna el DTO mapeado")
        void obtenerPorIdExistente() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));

            TicketResponse r = ticketService.obtenerPorId(100);

            assertThat(r.getId()).isEqualTo(100);
            assertThat(r.getCodigo()).isEqualTo("TKT-100");
            assertThat(r.getClienteNombre()).isEqualTo("Laura Gomez");
        }

        @Test
        @DisplayName("PU-TKT-04 | obtenerPorId con ticket inexistente lanza NotFoundException")
        void obtenerPorIdInexistente() {
            when(ticketRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.obtenerPorId(404))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Ticket no encontrado");
        }

        @Test
        @DisplayName("PU-TKT-05 | obtenerPorCodigo con codigo existente retorna el DTO")
        void obtenerPorCodigoExistente() {
            when(ticketRepository.findByCodigo("TKT-100")).thenReturn(Optional.of(ticket));

            TicketResponse r = ticketService.obtenerPorCodigo("TKT-100");

            assertThat(r.getCodigo()).isEqualTo("TKT-100");
            verify(ticketRepository).findByCodigo("TKT-100");
        }

        @Test
        @DisplayName("PU-TKT-06 | obtenerPorCodigo con codigo inexistente lanza NotFoundException")
        void obtenerPorCodigoInexistente() {
            when(ticketRepository.findByCodigo("TKT-NOPE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.obtenerPorCodigo("TKT-NOPE"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("TKT-NOPE");
        }
    }

    // ─── listados ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listar() y variantes")
    class ListarTests {

        private final Pageable pageable = PageRequest.of(0, 10);

        @Test
        @DisplayName("PU-TKT-07 | listar mapea la pagina de entidades a DTOs")
        void listar() {
            when(ticketRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.listar(pageable);

            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getCodigo()).isEqualTo("TKT-100");
            verify(ticketRepository).findAll(pageable);
        }

        @Test
        @DisplayName("PU-TKT-08 | listarPorCliente delega en findByClienteId")
        void listarPorCliente() {
            when(ticketRepository.findByClienteId(1, pageable))
                    .thenReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.listarPorCliente(1, pageable);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getClienteId()).isEqualTo(1);
            verify(ticketRepository).findByClienteId(1, pageable);
        }

        @Test
        @DisplayName("PU-TKT-09 | listarPorAgente delega en findByAgenteId")
        void listarPorAgente() {
            ticket.setAgente(agente);
            when(ticketRepository.findByAgenteId(2, pageable))
                    .thenReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.listarPorAgente(2, pageable);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getAgenteId()).isEqualTo(2);
            assertThat(page.getContent().get(0).getAgenteNombre()).isEqualTo("Pedro Ruiz");
            verify(ticketRepository).findByAgenteId(2, pageable);
        }

        @Test
        @DisplayName("PU-TKT-10 | listarPorEstado delega en findByEstado")
        void listarPorEstado() {
            when(ticketRepository.findByEstado("abierto", pageable))
                    .thenReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.listarPorEstado("abierto", pageable);

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getEstado()).isEqualTo("abierto");
            verify(ticketRepository).findByEstado("abierto", pageable);
        }
    }

    // ─── asignarAgente() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("asignarAgente()")
    class AsignarAgenteTests {

        @Test
        @DisplayName("PU-TKT-11 | Asignacion feliz: setea agente, fechaInicioAtencion, historial y email")
        void asignacionFeliz() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            when(usuarioRepository.findById(2)).thenReturn(Optional.of(agente));
            stubSaveTicketDevuelveArgumento();
            stubSaveHistorialDevuelveArgumento();

            TicketResponse r = ticketService.asignarAgente(100, 2, 9);

            assertThat(r.getAgenteId()).isEqualTo(2);
            assertThat(r.getFechaInicioAtencion()).isNotNull();
            verify(ticketRepository).save(ticket);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getCampoModificado()).isEqualTo("agente");
            assertThat(historyCaptor.getValue().getValorAnterior()).isNull();
            assertThat(historyCaptor.getValue().getValorNuevo()).isEqualTo("Pedro Ruiz");
            verify(emailService).notificarTicketAsignado(ticket);
        }

        @Test
        @DisplayName("PU-TKT-12 | Reasignacion registra en historial el agente anterior")
        void reasignacionRegistraAgenteAnterior() {
            Usuario anterior = usuario(5, "Mario", "Diaz", "mario@test.com");
            ticket.setAgente(anterior);
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            when(usuarioRepository.findById(2)).thenReturn(Optional.of(agente));
            stubSaveTicketDevuelveArgumento();
            stubSaveHistorialDevuelveArgumento();

            ticketService.asignarAgente(100, 2, 9);

            verify(ticketHistoryRepository).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getValorAnterior()).isEqualTo("Mario Diaz");
            assertThat(historyCaptor.getValue().getValorNuevo()).isEqualTo("Pedro Ruiz");
        }

        @Test
        @DisplayName("PU-TKT-13 | Agente inexistente lanza NotFoundException")
        void agenteInexistente() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            when(usuarioRepository.findById(77)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.asignarAgente(100, 77, 9))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Agente no encontrado");

            verify(ticketRepository, never()).save(any(Ticket.class));
            verify(emailService, never()).notificarTicketAsignado(any());
        }
    }

    // ─── cambiarEstado() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarEstado()")
    class CambiarEstadoTests {

        @Test
        @DisplayName("PU-TKT-14 | Cambio a 'cerrado' setea fechaCierre, registra historial y notifica")
        void cambioACerrado() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();
            stubSaveHistorialDevuelveArgumento();

            TicketResponse r = ticketService.cambiarEstado(100, "cerrado", 9);

            assertThat(r.getEstado()).isEqualTo("cerrado");
            assertThat(r.getFechaCierre()).isNotNull();
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getCampoModificado()).isEqualTo("estado");
            assertThat(historyCaptor.getValue().getValorAnterior()).isEqualTo("abierto");
            assertThat(historyCaptor.getValue().getValorNuevo()).isEqualTo("cerrado");
            verify(emailService).notificarCambioEstado(ticket, "abierto");
        }

        @Test
        @DisplayName("PU-TKT-15 | Cambio a 'resuelto' tambien setea fechaCierre")
        void cambioAResuelto() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();
            stubSaveHistorialDevuelveArgumento();

            TicketResponse r = ticketService.cambiarEstado(100, "resuelto", 9);

            assertThat(r.getEstado()).isEqualTo("resuelto");
            assertThat(r.getFechaCierre()).isNotNull();
        }

        @Test
        @DisplayName("PU-TKT-16 | Mismo estado: guarda pero NO registra historial ni envia email")
        void mismoEstadoNoNotifica() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();

            TicketResponse r = ticketService.cambiarEstado(100, "ABIERTO", 9);

            assertThat(r.getEstado()).isEqualTo("ABIERTO");
            assertThat(r.getFechaCierre()).isNull();
            verify(ticketRepository).save(ticket);
            verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
            verify(emailService, never()).notificarCambioEstado(any(), anyString());
        }

        @Test
        @DisplayName("PU-TKT-17 | Estado distinto no terminal: registra historial y notifica sin fechaCierre")
        void estadoDistintoNoTerminal() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();
            stubSaveHistorialDevuelveArgumento();

            TicketResponse r = ticketService.cambiarEstado(100, "en_progreso", 9);

            assertThat(r.getEstado()).isEqualTo("en_progreso");
            assertThat(r.getFechaCierre()).isNull();
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
            verify(emailService).notificarCambioEstado(ticket, "abierto");
        }

        @Test
        @DisplayName("PU-TKT-18 | Ticket inexistente lanza NotFoundException")
        void ticketInexistente() {
            when(ticketRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.cambiarEstado(404, "cerrado", 9))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ─── actualizar() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("PU-TKT-19 | Cambia asunto, descripcion y prioridad registrando un historial por campo")
        void actualizaTodosLosCampos() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();
            stubSaveHistorialDevuelveArgumento();

            ActualizarTicketRequest req = ActualizarTicketRequest.builder()
                    .asunto("Asunto nuevo")
                    .descripcion("Descripcion nueva")
                    .prioridad("alta")
                    .build();

            TicketResponse r = ticketService.actualizar(100, req, 9);

            assertThat(r.getAsunto()).isEqualTo("Asunto nuevo");
            assertThat(r.getDescripcion()).isEqualTo("Descripcion nueva");
            assertThat(r.getPrioridad()).isEqualTo("alta");
            verify(ticketHistoryRepository, times(3)).save(any(TicketHistory.class));
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("PU-TKT-20 | Campos null no modifican el ticket ni registran historial")
        void camposNullNoTocanNada() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();

            ActualizarTicketRequest req = ActualizarTicketRequest.builder().build();

            TicketResponse r = ticketService.actualizar(100, req, 9);

            assertThat(r.getAsunto()).isEqualTo("Asunto original");
            assertThat(r.getDescripcion()).isEqualTo("Descripcion original");
            assertThat(r.getPrioridad()).isEqualTo("media");
            verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("PU-TKT-21 | Valores iguales a los actuales no registran historial")
        void valoresIgualesNoRegistranHistorial() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            stubSaveTicketDevuelveArgumento();

            ActualizarTicketRequest req = ActualizarTicketRequest.builder()
                    .asunto("Asunto original")
                    .descripcion("Descripcion original")
                    .prioridad("media")
                    .build();

            ticketService.actualizar(100, req, 9);

            verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("PU-TKT-22 | Ticket inexistente lanza NotFoundException")
        void ticketInexistente() {
            when(ticketRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.actualizar(
                    404, ActualizarTicketRequest.builder().build(), 9))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ─── buscar() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscar()")
    class BuscarTests {

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        @DisplayName("PU-TKT-23 | Busqueda con todos los filtros devuelve la pagina mapeada y arma los predicados")
        void busquedaConTodosLosFiltros() {
            when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.buscar(
                    "abierto", "alta", "problema",
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 12, 31, 23, 59),
                    1, 2, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getCodigo()).isEqualTo("TKT-100");

            verify(ticketRepository).findAll(specCaptor.capture(), any(Pageable.class));

            // Ejercitar la Specification capturada para cubrir las ramas de filtros
            Root<Ticket> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path path = mock(Path.class);
            Expression expr = mock(Expression.class);
            Predicate predicate = mock(Predicate.class);

            when(root.get(anyString())).thenReturn(path);
            when(path.get(anyString())).thenReturn(path);
            when(cb.lower(any())).thenReturn(expr);
            when(cb.equal(any(Expression.class), any(Object.class))).thenReturn(predicate);
            when(cb.greaterThanOrEqualTo(any(Expression.class), any(LocalDateTime.class))).thenReturn(predicate);
            when(cb.lessThanOrEqualTo(any(Expression.class), any(LocalDateTime.class))).thenReturn(predicate);
            when(cb.and(any(Predicate[].class))).thenReturn(predicate);

            Predicate resultado = specCaptor.getValue().toPredicate(root, query, cb);

            assertThat(resultado).isNotNull();
            verify(cb, times(3)).lower(any());
            verify(cb).greaterThanOrEqualTo(any(Expression.class), any(LocalDateTime.class));
            verify(cb).lessThanOrEqualTo(any(Expression.class), any(LocalDateTime.class));
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        @DisplayName("PU-TKT-24 | Busqueda sin filtros no agrega predicados de campo")
        void busquedaSinFiltros() {
            when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.buscar(
                    null, "  ", null, null, null, null, null, PageRequest.of(0, 5));

            assertThat(page.getContent()).hasSize(1);

            verify(ticketRepository).findAll(specCaptor.capture(), any(Pageable.class));

            Root<Ticket> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Predicate predicate = mock(Predicate.class);
            when(cb.and(any(Predicate[].class))).thenReturn(predicate);

            Predicate resultado = specCaptor.getValue().toPredicate(root, query, cb);

            assertThat(resultado).isNotNull();
            verify(cb, never()).lower(any());
            verify(root, never()).get(anyString());
        }
    }

    // ─── obtenerHistorial() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerHistorial()")
    class ObtenerHistorialTests {

        @Test
        @DisplayName("PU-TKT-25 | Ticket existente mapea la lista del historial")
        void historialExistente() {
            TicketHistory h1 = TicketHistory.builder()
                    .id(1).ticketId(100).campoModificado("estado")
                    .valorAnterior("abierto").valorNuevo("cerrado")
                    .usuario(agente)
                    .fechaHora(LocalDateTime.of(2026, 2, 1, 10, 0))
                    .build();
            TicketHistory h2 = TicketHistory.builder()
                    .id(2).ticketId(100).campoModificado("creacion")
                    .valorNuevo("TKT-100")
                    .build();

            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            when(ticketHistoryRepository.findByTicketIdOrderByFechaHoraDesc(100))
                    .thenReturn(List.of(h1, h2));

            List<TicketHistoryResponse> historial = ticketService.obtenerHistorial(100);

            assertThat(historial).hasSize(2);
            assertThat(historial.get(0).getCampoModificado()).isEqualTo("estado");
            assertThat(historial.get(0).getUsuarioId()).isEqualTo(2);
            assertThat(historial.get(0).getUsuarioNombre()).isEqualTo("Pedro Ruiz");
            assertThat(historial.get(1).getUsuarioId()).isNull();
            verify(ticketHistoryRepository).findByTicketIdOrderByFechaHoraDesc(100);
        }

        @Test
        @DisplayName("PU-TKT-26 | Ticket inexistente lanza NotFoundException antes de consultar historial")
        void historialTicketInexistente() {
            when(ticketRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.obtenerHistorial(404))
                    .isInstanceOf(NotFoundException.class);

            verify(ticketHistoryRepository, never()).findByTicketIdOrderByFechaHoraDesc(anyInt());
        }
    }

    // ─── comentar() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("comentar()")
    class ComentarTests {

        @Test
        @DisplayName("PU-TKT-27 | Comentario feliz con actor: guarda y mapea el comentario")
        void comentarConActor() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            when(usuarioRepository.findById(2)).thenReturn(Optional.of(agente));
            when(ticketHistoryRepository.save(any(TicketHistory.class))).thenAnswer(inv -> {
                TicketHistory h = inv.getArgument(0);
                h.setId(7);
                return h;
            });

            TicketHistoryResponse r = ticketService.comentar(100, "Estamos revisando", 2);

            assertThat(r.getId()).isEqualTo(7);
            assertThat(r.getTicketId()).isEqualTo(100);
            assertThat(r.getCampoModificado()).isEqualTo("comentario");
            assertThat(r.getValorNuevo()).isEqualTo("Estamos revisando");
            assertThat(r.getUsuarioId()).isEqualTo(2);
            assertThat(r.getUsuarioNombre()).isEqualTo("Pedro Ruiz");
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("PU-TKT-28 | Comentario sin actor: no consulta usuario y usuarioId queda null")
        void comentarSinActor() {
            when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
            when(ticketHistoryRepository.save(any(TicketHistory.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TicketHistoryResponse r = ticketService.comentar(100, "Comentario anonimo", null);

            assertThat(r.getValorNuevo()).isEqualTo("Comentario anonimo");
            assertThat(r.getUsuarioId()).isNull();
            verify(usuarioRepository, never()).findById(anyInt());
        }

        @Test
        @DisplayName("PU-TKT-29 | Ticket inexistente lanza NotFoundException y no guarda comentario")
        void comentarTicketInexistente() {
            when(ticketRepository.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.comentar(404, "Hola", 2))
                    .isInstanceOf(NotFoundException.class);

            verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }
    }

    // ─── validarTicketActivo() ──────────────────────────────────────────────────

    @Test
    @DisplayName("PU-TKT-30 | validarTicketActivo siempre responde sin ticket activo")
    void validarTicketActivo() {
        ValidacionTicketActivoResponse r = ticketService.validarTicketActivo("soporte", 1);

        assertThat(r.isTieneTicketActivo()).isFalse();
    }
}
