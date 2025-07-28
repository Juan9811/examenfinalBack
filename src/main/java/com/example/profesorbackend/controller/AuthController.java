package com.example.profesorbackend.controller;

import com.example.profesorbackend.model.Usuario;
import com.example.profesorbackend.repository.UsuarioRepository;
import com.example.profesorbackend.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("usuario");
        String password = loginData.get("contrasena");
        
        // Validar que los campos no estén vacíos
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Usuario y contraseña son requeridos");
            return ResponseEntity.badRequest().body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            Usuario usuario = usuarioRepository.findByUsuario(username).orElseThrow();
            String token = jwtUtil.generateToken(username, usuario.getRol());
            response.put("token", token);
            response.put("rol", usuario.getRol());
            response.put("usuario", usuario.getUsuario());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            response.put("error", "Credenciales incorrectas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
