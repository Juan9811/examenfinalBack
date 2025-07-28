// ===============================
// Pruebas de integración para el controlador de Autenticación
// Objetivo: Validar el login y manejo de errores usando MockMvc
// ===============================
package com.example.profesorbackend;

// Importaciones necesarias para pruebas de integración con Spring Boot y MockMvc
import com.example.profesorbackend.model.Usuario;
import com.example.profesorbackend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// Clase de pruebas para el controlador de autenticación
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Prueba el login exitoso:
     * 1. Crea un usuario de prueba y lo guarda en la base de datos.
     * 2. Envía un POST a /auth/login con las credenciales correctas.
     * 3. Verifica que la respuesta contiene un token y el rol correcto.
     */
    @Test
    void testLoginSuccess() throws Exception {
        // Crear usuario de prueba
        Usuario usuario = new Usuario();
        usuario.setUsuario("testuser");
        usuario.setContrasena(passwordEncoder.encode("testpass"));
        usuario.setRol("ADMIN");
        usuario.setEmail("test@example.com");
        usuarioRepository.save(usuario);

        // Crear request de login
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("testuser", "testpass")
        );

        // Ejecutar login
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    /**
     * Prueba el login fallido:
     * 1. Envía un POST a /auth/login con credenciales incorrectas.
     * 2. Verifica que la respuesta es 401 Unauthorized.
     */
    @Test
    void testLoginFailure() throws Exception {
        // Crear request con credenciales incorrectas
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("wronguser", "wrongpass")
        );

        // Ejecutar login
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginValidation() throws Exception {
        // Request sin datos
        String emptyRequest = objectMapper.writeValueAsString(
            new LoginRequest("", "")
        );

        // Ejecutar login
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyRequest))
                .andExpect(status().isBadRequest());
    }

    // Clase auxiliar para request
    private static class LoginRequest {
        private final String usuario;
        private final String contrasena;

        LoginRequest(String usuario, String contrasena) {
            this.usuario = usuario;
            this.contrasena = contrasena;
        }

        public String getUsuario() { return usuario; }
        public String getContrasena() { return contrasena; }
    }
}
