// ===============================
// Prueba de carga de contexto de Spring Boot
// Objetivo: Verificar que la aplicación arranca correctamente
// ===============================
package com.example.profesor_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = com.example.profesorbackend.ProfesorBackendApplication.class)
// Clase de prueba para verificar el arranque del contexto de Spring Boot
class ProfesorBackendApplicationTests {

	/**
	 * Prueba vacía que verifica que el contexto de Spring Boot carga sin errores.
	 */
	@Test
	void contextLoads() {
	}

}
