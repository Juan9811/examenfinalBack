package com.example.profesorbackend.repository;

import com.example.profesorbackend.model.Profesor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProfesorRepository extends MongoRepository<Profesor, String> {
    Optional<Profesor> findByNumeroEmpleado(String numeroEmpleado);
}
