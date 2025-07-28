package com.example.profesorbackend.repository;

import com.example.profesorbackend.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsuario(String usuario);
}
