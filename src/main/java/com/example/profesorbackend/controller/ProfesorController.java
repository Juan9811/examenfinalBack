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
        usuarioRepository.save(usuario);

        Profesor profesor = new Profesor();
        profesor.setNombre(dto.nombre);
        profesor.setEmail(dto.email);
        profesor.setEspecialidad(dto.especialidad);
        profesor.setNumeroEmpleado(dto.numeroEmpleado);
        profesor.setTelefono(dto.telefono);
        profesor.setUsuario(usuario);
        Profesor saved = profesorRepository.save(profesor);
        return ResponseEntity.ok(saved);
    }

    // Solo el profesor puede ver su perfil por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication auth) {
        Optional<Profesor> profOpt = profesorRepository.findById(id);
        if (profOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElse(null);
        boolean isAdmin = usuario != null && "ADMIN".equals(usuario.getRol());
        boolean isOwner = usuario != null && profOpt.get().getUsuario().getId().equals(usuario.getId());
        
        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).body("No autorizado");
        }
        
        return ResponseEntity.ok(profOpt.get());
    }

    // Admin: editar cualquier profesor, Profesor: solo el suyo
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Profesor profesor, Authentication auth) {
        Optional<Profesor> profOpt = profesorRepository.findById(id);
        if (profOpt.isEmpty()) return ResponseEntity.notFound().build();
        Profesor existente = profOpt.get();
        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElse(null);
        boolean isAdmin = usuario != null && "ADMIN".equals(usuario.getRol());
        boolean isOwner = usuario != null && existente.getUsuario().getId().equals(usuario.getId());
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
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        System.out.println("DELETE llamado para ID: " + id);
        System.out.println("Authentication: " + (auth != null ? auth.getName() : "null"));
        
        Optional<Profesor> profOpt = profesorRepository.findById(id);
        if (profOpt.isEmpty()) return ResponseEntity.notFound().build();
        Profesor profesor = profOpt.get();
        Usuario usuarioActual = usuarioRepository.findByUsuario(auth.getName()).orElse(null);
        
        System.out.println("Usuario actual: " + (usuarioActual != null ? usuarioActual.getUsuario() + " - " + usuarioActual.getRol() : "null"));
        System.out.println("Profesor a eliminar: " + profesor.getNombre() + " (Usuario ID: " + profesor.getUsuario().getId() + ")");
        System.out.println("Usuario actual ID: " + (usuarioActual != null ? usuarioActual.getId() : "null"));
        
        // Verificar que sea admin
        if (usuarioActual == null || !"ADMIN".equals(usuarioActual.getRol())) {
            System.out.println("FALLA: No es admin o usuario nulo");
            return ResponseEntity.status(403).body("Solo el administrador puede eliminar profesores.");
        }
        
        // Verificar que no se elimine a sí mismo
        if (profesor.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("FALLA: Intenta eliminarse a sí mismo");
            return ResponseEntity.status(403).body("No puedes eliminar tu propio usuario.");
        }
        
        System.out.println("Eliminando profesor...");
        
        // Guardar el ID del usuario antes de eliminar
        Long usuarioId = profesor.getUsuario().getId();
        Usuario usuarioAEliminar = profesor.getUsuario();
        
        // Romper la relación bidireccional antes de eliminar
        profesor.setUsuario(null);
        usuarioAEliminar.setProfesor(null);
        
        // Guardar los cambios para romper las referencias
        profesorRepository.save(profesor);
        usuarioRepository.save(usuarioAEliminar);
        
        // Ahora eliminar el profesor
        profesorRepository.deleteById(id);
        
        // Finalmente eliminar el usuario
        usuarioRepository.deleteById(usuarioId);
        
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
        
        if (usuario.getProfesor() == null) {
            return ResponseEntity.status(404).body("El usuario no tiene un perfil de profesor asociado");
        }
        
        return ResponseEntity.ok(usuario.getProfesor());
    }
}
