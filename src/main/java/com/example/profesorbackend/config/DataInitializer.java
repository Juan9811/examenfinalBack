package com.example.profesorbackend.config;

import com.example.profesorbackend.model.Usuario;
import com.example.profesorbackend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initAdmin(UsuarioRepository usuarioRepository) {
        return args -> {
            if (usuarioRepository.findByUsuario("admin").isEmpty()) {
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                Usuario admin = new Usuario();
                admin.setUsuario("admin");
                admin.setContrasena(encoder.encode("admin123"));
                admin.setEmail("admin@admin.com");
                admin.setRol("ADMIN");
                usuarioRepository.save(admin);
                System.out.println("Usuario admin creado: usuario=admin, contraseña=admin123");
            }
        };
    }
}
