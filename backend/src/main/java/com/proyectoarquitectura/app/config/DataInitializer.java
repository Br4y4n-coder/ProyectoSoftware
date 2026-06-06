package com.proyectoarquitectura.app.config;

import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Crear roles si la tabla está vacía
        if (rolRepository.count() == 0) {
            Rol usuario = new Rol();
            usuario.setNombre("usuario");
            usuario.setCreadoEn(LocalDateTime.now());
            rolRepository.save(usuario);

            Rol agente = new Rol();
            agente.setNombre("agente");
            agente.setCreadoEn(LocalDateTime.now());
            rolRepository.save(agente);

            Rol administrador = new Rol();
            administrador.setNombre("administrador");
            administrador.setCreadoEn(LocalDateTime.now());
            rolRepository.save(administrador);

            System.out.println("✅ Roles creados: usuario, agente, administrador");
        }

        // 2. Crear usuario administrador si no existe
        if (usuarioRepository.findByCorreo("admin@test.com").isEmpty()) {
            // Buscar el rol administrador (puede ser por nombre)
            Rol rolAdmin = rolRepository.findByNombre("administrador")
                    .orElseThrow(() -> new RuntimeException("No se encontró el rol 'administrador'"));

            Usuario admin = new Usuario();
            admin.setNombres("Admin");
            admin.setApellidos("Sistema");
            admin.setCorreo("admin@test.com");
            admin.setContrasenaHash(passwordEncoder.encode("12345678"));
            admin.setRol(rolAdmin);                 // ⬅️ CORRECCIÓN CLAVE: usa setRol, no setRolId
            admin.setEstado("activo");
            admin.setCreadoEn(LocalDateTime.now());
            admin.setActualizadoEn(LocalDateTime.now());
            admin.setIntentosFallidos(0);

            usuarioRepository.save(admin);
            System.out.println("✅ Usuario admin creado: admin@test.com / 12345678");
        } else {
            System.out.println("ℹ️ El usuario admin ya existe, no se creó nuevamente.");
        }
    }
}