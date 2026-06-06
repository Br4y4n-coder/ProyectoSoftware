package com.proyectoarquitectura.app.service.configuracion;

import com.proyectoarquitectura.app.models.dto.configuracion.ConfiguracionResponse;
import com.proyectoarquitectura.app.models.entity.Configuracion;
import com.proyectoarquitectura.app.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    public Map<String, String> obtenerConfiguraciones() {
        List<Configuracion> configs = configuracionRepository.findAll();
        return configs.stream()
                .collect(Collectors.toMap(Configuracion::getClave, Configuracion::getValor));
    }

    public List<ConfiguracionResponse> listarTodas() {
        return configuracionRepository.findAll().stream()
                .map(ConfiguracionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void guardarConfiguraciones(Map<String, String> configuraciones) {
        for (Map.Entry<String, String> entry : configuraciones.entrySet()) {
            Configuracion config = configuracionRepository.findByClave(entry.getKey())
                    .orElse(new Configuracion());
            config.setClave(entry.getKey());
            config.setValor(entry.getValue());
            configuracionRepository.save(config);
        }
    }
}