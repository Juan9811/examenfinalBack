// ===============================
// Pruebas de integración para el controlador de Profesores
// Objetivo: Validar los endpoints de creación, edición y consulta de profesores usando MockMvc y JWT
// ===============================
package com.example.profesorbackend;

// Importaciones necesarias para pruebas de integración con Spring Boot y MockMvc
import com.example.profesorbackend.model.Usuario;
import com.example.profesorbackend.model.Profesor;
import com.example.profesorbackend.dto.ProfesorUsuarioDTO;
import com.example.profesorbackend.repository.UsuarioRepository;
import com.example.profesorbackend.repository.ProfesorRepository;
import com.example.profesorbackend.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// Clase de pruebas para el controlador de profesores
class ProfesorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Prueba la creación de un profesor vía endpoint protegido con JWT.
     * 1. Crea un usuario admin y lo guarda en la base de datos.
     * 2. Genera un token JWT para autenticación.
     * 3. Envía un POST con los datos del profesor y verifica la respuesta.
     */
    @Test
    void testCrearProfesor() throws Exception {
        // Crear usuario administrador
        Usuario admin = new Usuario();
        admin.setUsuario("admin1");
        admin.setContrasena(passwordEncoder.encode("admin123"));
        admin.setRol("ADMIN");
        admin.setEmail("admin1@example.com");
        usuarioRepository.save(admin);

        // Generar token JWT
        String token = jwtUtil.generateToken("admin1", "ADMIN");

        // Crear datos del profesor usando DTO
        ProfesorUsuarioDTO dto = new ProfesorUsuarioDTO();
        dto.nombre = "Juan Pérez";
        dto.email = "juan.perez@universidad.edu";
        dto.especialidad = "Matemáticas";
        dto.telefono = "555-1234";
        dto.numeroEmpleado = "EMP001";
        dto.usuario = "juan.perez";
        dto.contrasena = "password123";

        // Ejecutar petición POST
        mockMvc.perform(post("/profesores")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@universidad.edu"))
                .andExpect(jsonPath("$.especialidad").value("Matemáticas"));
    }

    @Test
    void testObtenerProfesores() throws Exception {
        // Crear usuario administrador
        Usuario admin = new Usuario();
        admin.setUsuario("admin2");
        admin.setContrasena(passwordEncoder.encode("admin123"));
        admin.setRol("ADMIN");
        admin.setEmail("admin2@example.com");
        usuarioRepository.save(admin);

        // Generar token JWT
        String token = jwtUtil.generateToken("admin2", "ADMIN");

        // Ejecutar petición GET
        mockMvc.perform(get("/profesores")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testAccesoDenegadoSinToken() throws Exception {
        // Crear datos del profesor
        Profesor profesor = new Profesor();
        profesor.setNombre("Juan Pérez");
        profesor.setEmail("juan.perez@universidad.edu");

        // Ejecutar petición POST sin token
        mockMvc.perform(post("/profesores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profesor)))
                .andExpect(status().isForbidden());
    }
}
