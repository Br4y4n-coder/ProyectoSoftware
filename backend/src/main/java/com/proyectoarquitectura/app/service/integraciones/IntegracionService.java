package com.proyectoarquitectura.app.service.integraciones;

import com.proyectoarquitectura.app.models.dto.integraciones.IntegracionResponse;
import com.proyectoarquitectura.app.models.entity.Integracion;
import com.proyectoarquitectura.app.repository.IntegracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IntegracionService {

    private final IntegracionRepository integracionRepository;

    public IntegracionService(IntegracionRepository integracionRepository) {
        this.integracionRepository = integracionRepository;
    }

    public List<IntegracionResponse> listarTodas() {
        return integracionRepository.findAll().stream()
                .map(IntegracionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public IntegracionResponse conectar(Integer id) {
        Integracion integ = integracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Integración no encontrada"));
        integ.setConectado(true);
        integ.setActualizadoEn(LocalDateTime.now());
        return IntegracionResponse.from(integracionRepository.save(integ));
    }

    @Transactional
    public IntegracionResponse desconectar(Integer id) {
        Integracion integ = integracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Integración no encontrada"));
        integ.setConectado(false);
        integ.setActualizadoEn(LocalDateTime.now());
        return IntegracionResponse.from(integracionRepository.save(integ));
    }
}