package com.proyectoarquitectura.app.service.auditoria;

import com.proyectoarquitectura.app.models.dto.auditoria.LogAuditoriaResponse;
import com.proyectoarquitectura.app.models.entity.LogAuditoria;
import com.proyectoarquitectura.app.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — AuditoriaService
 *
 * JUnit 5 + Mockito puro (sin contexto Spring).
 * Se mockea LogAuditoriaRepository, única dependencia del constructor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditoriaService — Pruebas Unitarias")
class AuditoriaServiceTest {

    @Mock
    private LogAuditoriaRepository logAuditoriaRepository;

    private AuditoriaService auditoriaService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaService(logAuditoriaRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private LogAuditoria log(Long id, String usuario, String accion) {
        return LogAuditoria.builder()
                .id(id)
                .usuario(usuario)
                .accion(accion)
                .detalles("detalle de prueba")
                .ip("192.168.1.10")
                .fechaHora(LocalDateTime.of(2026, 5, 10, 14, 30))
                .build();
    }

    // ─── registrar() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registrar()")
    class RegistrarTests {

        @Test
        @DisplayName("PU-AUD-01 | Construye el log con todos los campos y lo persiste")
        void registraLogConTodosLosCampos() {
            when(logAuditoriaRepository.save(any(LogAuditoria.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            auditoriaService.registrar("admin@test.com", "LOGIN", "Ingreso exitoso", "10.0.0.1");

            ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
            verify(logAuditoriaRepository).save(captor.capture());
            LogAuditoria guardado = captor.getValue();
            assertThat(guardado.getUsuario()).isEqualTo("admin@test.com");
            assertThat(guardado.getAccion()).isEqualTo("LOGIN");
            assertThat(guardado.getDetalles()).isEqualTo("Ingreso exitoso");
            assertThat(guardado.getIp()).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("PU-AUD-02 | Acepta detalles e ip nulos sin lanzar excepción")
        void registraConCamposOpcionalesNulos() {
            when(logAuditoriaRepository.save(any(LogAuditoria.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            auditoriaService.registrar("user@test.com", "LOGOUT", null, null);

            ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
            verify(logAuditoriaRepository).save(captor.capture());
            assertThat(captor.getValue().getDetalles()).isNull();
            assertThat(captor.getValue().getIp()).isNull();
        }
    }

    // ─── listarTodos() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodosTests {

        @Test
        @DisplayName("PU-AUD-03 | Retorna página de logs mapeados a LogAuditoriaResponse")
        void retornaPaginaDeLogs() {
            when(logAuditoriaRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(log(1L, "admin", "LOGIN"))));

            Page<LogAuditoriaResponse> resultado = auditoriaService.listarTodos(pageable);

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            LogAuditoriaResponse r = resultado.getContent().get(0);
            assertThat(r.getId()).isEqualTo(1L);
            assertThat(r.getUsuario()).isEqualTo("admin");
            assertThat(r.getAccion()).isEqualTo("LOGIN");
            assertThat(r.getDetalles()).isEqualTo("detalle de prueba");
            assertThat(r.getIp()).isEqualTo("192.168.1.10");
            assertThat(r.getFechaHora()).isEqualTo(LocalDateTime.of(2026, 5, 10, 14, 30));
            verify(logAuditoriaRepository).findAll(pageable);
        }

        @Test
        @DisplayName("PU-AUD-04 | Sin registros retorna página vacía")
        void sinRegistrosRetornaPaginaVacia() {
            when(logAuditoriaRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            Page<LogAuditoriaResponse> resultado = auditoriaService.listarTodos(pageable);

            assertThat(resultado.getContent()).isEmpty();
        }
    }

    // ─── buscarPorUsuario() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorUsuario()")
    class BuscarPorUsuarioTests {

        @Test
        @DisplayName("PU-AUD-05 | Delega en findByUsuarioContainingIgnoreCase y mapea los resultados")
        void buscaPorUsuarioYMapea() {
            when(logAuditoriaRepository.findByUsuarioContainingIgnoreCase("admin", pageable))
                    .thenReturn(new PageImpl<>(List.of(log(2L, "admin@test.com", "UPDATE"))));

            Page<LogAuditoriaResponse> resultado =
                    auditoriaService.buscarPorUsuario("admin", pageable);

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            assertThat(resultado.getContent().get(0).getUsuario()).isEqualTo("admin@test.com");
            verify(logAuditoriaRepository).findByUsuarioContainingIgnoreCase("admin", pageable);
        }

        @Test
        @DisplayName("PU-AUD-06 | Usuario sin coincidencias retorna página vacía")
        void usuarioSinCoincidenciasRetornaPaginaVacia() {
            when(logAuditoriaRepository.findByUsuarioContainingIgnoreCase("ghost", pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            assertThat(auditoriaService.buscarPorUsuario("ghost", pageable).getContent()).isEmpty();
        }
    }

    // ─── buscarPorAccion() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorAccion()")
    class BuscarPorAccionTests {

        @Test
        @DisplayName("PU-AUD-07 | Delega en findByAccion y mapea los resultados")
        void buscaPorAccionYMapea() {
            when(logAuditoriaRepository.findByAccion("DELETE", pageable))
                    .thenReturn(new PageImpl<>(List.of(log(3L, "ops", "DELETE"))));

            Page<LogAuditoriaResponse> resultado =
                    auditoriaService.buscarPorAccion("DELETE", pageable);

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            assertThat(resultado.getContent().get(0).getAccion()).isEqualTo("DELETE");
            verify(logAuditoriaRepository).findByAccion("DELETE", pageable);
        }

        @Test
        @DisplayName("PU-AUD-08 | Acción sin coincidencias retorna página vacía")
        void accionSinCoincidenciasRetornaPaginaVacia() {
            when(logAuditoriaRepository.findByAccion("NADA", pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            assertThat(auditoriaService.buscarPorAccion("NADA", pageable).getContent()).isEmpty();
        }
    }
}
