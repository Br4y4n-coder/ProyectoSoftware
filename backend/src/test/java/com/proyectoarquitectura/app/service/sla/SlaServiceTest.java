package com.proyectoarquitectura.app.service.sla;

import com.proyectoarquitectura.app.models.dto.sla.SlaReglaResponse;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.SlaRegla;
import com.proyectoarquitectura.app.repository.SlaReglaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — SlaService
 *
 * JUnit 5 + Mockito puro (sin contexto Spring).
 * Se mockea SlaReglaRepository, única dependencia del constructor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SlaService — Pruebas Unitarias")
class SlaServiceTest {

    @Mock
    private SlaReglaRepository slaReglaRepository;

    private SlaService slaService;

    @BeforeEach
    void setUp() {
        slaService = new SlaService(slaReglaRepository);
    }

    @Test
    @DisplayName("PU-SLA-01 | listarTodas retorna las reglas mapeadas a SlaReglaResponse")
    void listarTodasRetornaReglasMapeadas() {
        Rol rol = Rol.builder().id(3).nombre("agente").build();
        SlaRegla conRol = SlaRegla.builder()
                .id(1)
                .nombre("SLA Crítico")
                .prioridad("alta")
                .tiempoRespuestaHoras(1)
                .tiempoResolucionHoras(4)
                .aplicaRol(rol)
                .activo(true)
                .creadoEn(LocalDateTime.of(2026, 3, 1, 9, 0))
                .build();
        SlaRegla sinRol = SlaRegla.builder()
                .id(2)
                .nombre("SLA Normal")
                .prioridad("media")
                .tiempoRespuestaHoras(8)
                .tiempoResolucionHoras(24)
                .aplicaRol(null)
                .activo(false)
                .creadoEn(LocalDateTime.of(2026, 3, 2, 9, 0))
                .build();

        when(slaReglaRepository.findAll()).thenReturn(List.of(conRol, sinRol));

        List<SlaReglaResponse> resultado = slaService.listarTodas();

        assertThat(resultado).hasSize(2);

        // Regla con rol asociado: se mapean los datos del rol
        SlaReglaResponse r1 = resultado.get(0);
        assertThat(r1.getId()).isEqualTo(1);
        assertThat(r1.getNombre()).isEqualTo("SLA Crítico");
        assertThat(r1.getPrioridad()).isEqualTo("alta");
        assertThat(r1.getTiempoRespuestaHoras()).isEqualTo(1);
        assertThat(r1.getTiempoResolucionHoras()).isEqualTo(4);
        assertThat(r1.getAplicaRolId()).isEqualTo(3);
        assertThat(r1.getAplicaRolNombre()).isEqualTo("agente");
        assertThat(r1.getActivo()).isTrue();
        assertThat(r1.getCreadoEn()).isEqualTo(LocalDateTime.of(2026, 3, 1, 9, 0));

        // Regla sin rol asociado: los campos de rol quedan en null
        SlaReglaResponse r2 = resultado.get(1);
        assertThat(r2.getAplicaRolId()).isNull();
        assertThat(r2.getAplicaRolNombre()).isNull();
        assertThat(r2.getActivo()).isFalse();

        verify(slaReglaRepository).findAll();
    }

    @Test
    @DisplayName("PU-SLA-02 | listarTodas sin reglas retorna lista vacía")
    void listarTodasSinReglasRetornaListaVacia() {
        when(slaReglaRepository.findAll()).thenReturn(Collections.emptyList());

        assertThat(slaService.listarTodas()).isEmpty();
        verify(slaReglaRepository).findAll();
    }
}
