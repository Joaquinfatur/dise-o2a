package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.app.FachadaProcesador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatsController {

    @Autowired
    private FachadaProcesador fachada;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(fachada.getEstadisticas());
    }
    
    @GetMapping("/clear")
    public ResponseEntity<String> clearData() {
        fachada.limpiarDatos();
        return ResponseEntity.ok("Datos limpiados exitosamente");
    }
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> actuatorHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "database", "PostgreSQL", 
            "datadog", "enabled"
        ));
    }
}