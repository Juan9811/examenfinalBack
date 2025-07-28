package com.example.profesorbackend.repository;

import com.example.profesorbackend.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    Optional<Profesor> findByNumeroEmpleado(String numeroEmpleado);
}
