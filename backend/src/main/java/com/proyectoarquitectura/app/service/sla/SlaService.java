package com.proyectoarquitectura.app.service.sla;

import com.proyectoarquitectura.app.models.dto.sla.SlaReglaResponse;
import com.proyectoarquitectura.app.repository.SlaReglaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlaService {

    private final SlaReglaRepository slaReglaRepository;

    public SlaService(SlaReglaRepository slaReglaRepository) {
        this.slaReglaRepository = slaReglaRepository;
    }

    public List<SlaReglaResponse> listarTodas() {
        return slaReglaRepository.findAll().stream()
                .map(SlaReglaResponse::from)
                .collect(Collectors.toList());
    }
}