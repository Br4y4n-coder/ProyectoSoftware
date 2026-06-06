package com.proyectoarquitectura.app.service.categorias;

import com.proyectoarquitectura.app.models.dto.categorias.CategoriaResponse;
import com.proyectoarquitectura.app.models.entity.Categoria;
import com.proyectoarquitectura.app.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponse::from)
                .collect(Collectors.toList());
    }

    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivoTrue().stream()
                .map(CategoriaResponse::from)
                .collect(Collectors.toList());
    }

    public CategoriaResponse obtenerPorId(Integer id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
        return CategoriaResponse.from(cat);
    }
}