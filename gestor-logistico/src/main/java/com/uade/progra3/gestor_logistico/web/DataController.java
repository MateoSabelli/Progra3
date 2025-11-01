package com.uade.progra3.gestor_logistico.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.progra3.gestor_logistico.service.DataInitService;

@RestController
@RequestMapping("/data")
public class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);
    private final DataInitService dataInitService;

    public DataController(DataInitService dataInitService) {
        this.dataInitService = dataInitService;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initializeData() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of(
                    "status", "info", 
                    "message", "La inicialización debe hacerse desde Neo4j directamente. Usa el archivo init-neo4j-completo.cypher",
                    "instrucciones", "1. Abre Neo4j Aura/Browser, 2. Copia el contenido de init-neo4j-completo.cypher, 3. Ejecuta en la consola"
                ));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearData() {
        try {
            String message = dataInitService.clearData();
            return ResponseEntity.ok(Map.of("status", "success", "message", message));
        } catch (Exception e) {
            log.error("Error al limpiar datos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Error al limpiar: " + e.getMessage()));
        }
    }
}
