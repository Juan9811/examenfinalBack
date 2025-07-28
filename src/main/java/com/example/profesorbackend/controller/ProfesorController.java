package com.example.profesorbackend.controller;

import com.example.profesorbackend.model.Profesor;
import com.example.profesorbackend.model.Usuario;
import com.example.profesorbackend.repository.ProfesorRepository;
import com.example.profesorbackend.repository.UsuarioRepository;
import com.example.profesorbackend.dto.ProfesorUsuarioDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profesores")
public class ProfesorController {
    private final ProfesorRepository profesorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfesorController(ProfesorRepository profesorRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.profesorRepository = profesorRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Solo admin: listar todos
    @GetMapping
    public List<Profesor> getAll() {
        // Siempre devolver un array plano
        return profesorRepository.findAll();
    }

    // Solo admin: crear profesor y usuario
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProfesorUsuarioDTO dto) {
        // Validar usuario único
        if (usuarioRepository.findByUsuario(dto.usuario).isPresent()) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya existe");
        }
        Usuario usuario = new Usuario();
        usuario.setUsuario(dto.usuario);
        usuario.setContrasena(passwordEncoder.encode(dto.contrasena));
        usuario.setEmail(dto.email);
        usuario.setRol("PROFESOR");
        usuario = usuarioRepository.save(usuario);

        Profesor profesor = new Profesor();
        profesor.setNombre(dto.nombre);
        profesor.setEmail(dto.email);
        profesor.setEspecialidad(dto.especialidad);
        profesor.setNumeroEmpleado(dto.numeroEmpleado);
        profesor.setTelefono(dto.telefono);
        profesor.setUsuarioId(usuario.getId());
        Profesor saved = profesorRepository.save(profesor);
        return ResponseEntity.ok(saved);
    }

    // Solo el profesor puede ver su perfil por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id, Authentication auth) {
        Optional<Profesor> profOpt = profesorRepository.findById(id);
        if (profOpt.isEmpty()) return ResponseEntity.notFound().build();

        Profesor profesor = profOpt.get();
        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElse(null);
        boolean isAdmin = usuario != null && "ADMIN".equals(usuario.getRol());
        boolean isOwner = usuario != null && profesor.getUsuarioId() != null && profesor.getUsuarioId().equals(usuario.getId());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).body("No autorizado");
        }

        return ResponseEntity.ok(profesor);
    }

    // Admin: editar cualquier profesor, Profesor: solo el suyo
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Profesor profesor, Authentication auth) {
        Optional<Profesor> profOpt = profesorRepository.findById(id);
        if (profOpt.isEmpty()) return ResponseEntity.notFound().build();
        Profesor existente = profOpt.get();
        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElse(null);
        boolean isAdmin = usuario != null && "ADMIN".equals(usuario.getRol());
        boolean isOwner = usuario != null && existente.getUsuarioId() != null && existente.getUsuarioId().equals(usuario.getId());
        if (!isAdmin && !isOwner) return ResponseEntity.status(403).body("No autorizado");
        existente.setNombre(profesor.getNombre());
        existente.setEmail(profesor.getEmail());
        existente.setEspecialidad(profesor.getEspecialidad());
        existente.setNumeroEmpleado(profesor.getNumeroEmpleado());
        existente.setTelefono(profesor.getTelefono());
        return ResponseEntity.ok(profesorRepository.save(existente));
    }

    // Solo admin: eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication auth) {
        Optional<Profesor> profOpt = profesorRepository.findById(id);
        if (profOpt.isEmpty()) return ResponseEntity.notFound().build();
        Profesor profesor = profOpt.get();
        Usuario usuarioActual = usuarioRepository.findByUsuario(auth.getName()).orElse(null);

        // Verificar que sea admin
        if (usuarioActual == null || !"ADMIN".equals(usuarioActual.getRol())) {
            return ResponseEntity.status(403).body("Solo el administrador puede eliminar profesores.");
        }

        // Verificar que no se elimine a sí mismo
        if (profesor.getUsuarioId() != null && profesor.getUsuarioId().equals(usuarioActual.getId())) {
            return ResponseEntity.status(403).body("No puedes eliminar tu propio usuario.");
        }

        // Eliminar el profesor
        profesorRepository.deleteById(id);

        // Eliminar el usuario asociado si existe
        if (profesor.getUsuarioId() != null) {
            usuarioRepository.deleteById(profesor.getUsuarioId());
        }

        return ResponseEntity.ok().build();
    }

    // Profesor: ver su propio perfil
    @GetMapping("/perfil")
    public ResponseEntity<?> getPerfil(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado: " + auth.getName());
        }

        // Buscar el profesor asociado a este usuario
        Optional<Profesor> profesorOpt = profesorRepository.findAll().stream()
            .filter(p -> usuario.getId().equals(p.getUsuarioId()))
            .findFirst();

        if (profesorOpt.isEmpty()) {
            return ResponseEntity.status(404).body("El usuario no tiene un perfil de profesor asociado");
        }

        return ResponseEntity.ok(profesorOpt.get());
    }
}
