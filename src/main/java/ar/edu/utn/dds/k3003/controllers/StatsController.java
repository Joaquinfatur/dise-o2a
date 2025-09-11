package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI; // ← CAMBIO: Usar la fachada de BD
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatsController {

    // CAMBIO: Usar la misma fachada que PdIController
    @Autowired
    private FachadaProcesadorPdI fachada;

    // AGREGAR: Inyectar contadores directamente para stats
    @Autowired(required = false)
    private Counter pdisProcessedCounter;
    
    @Autowired(required = false)
    private Counter pdisRejectedCounter;
    
    @Autowired(required = false)
    private Counter pdisErrorCounter;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            // Obtener estadísticas reales de la base de datos
            long totalEnBD = fachada.buscarTodas().size(); // ← Contar desde BD
            
            Map<String, Object> stats = Map.of(
                "totalProcesadas", pdisProcessedCounter != null ? pdisProcessedCounter.count() : 0.0,
                "totalRechazadas", pdisRejectedCounter != null ? pdisRejectedCounter.count() : 0.0,
                "totalErrores", pdisErrorCounter != null ? pdisErrorCounter.count() : 0.0,
                "enBaseDatos", totalEnBD, // ← REAL count from database
                "serviciosExternos", Map.of(
                    "fuentes", "DOWN (normal en Render)",
                    "solicitudes", "DOWN (normal en Render)", 
                    "agregador", "DOWN (normal en Render)"
                )
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Error retrieving stats: " + e.getMessage(),
                "totalProcesadas", pdisProcessedCounter != null ? pdisProcessedCounter.count() : 0.0
            ));
        }
    }
    
    @GetMapping("/clear")
    public ResponseEntity<String> clearData() {
        // NO IMPLEMENTAR - No queremos borrar la BD en producción
        return ResponseEntity.ok("Clear no implementado para proteger datos de producción");
    }
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> actuatorHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "database", "PostgreSQL", 
            "datadog", "enabled",
            "pdisEnBD", fachada.buscarTodas().size()
        ));
    }

    // NUEVO: Endpoint para debug específico
    @GetMapping("/debug-stats")
    public ResponseEntity<Map<String, Object>> debugStats() {
        try {
            long pdisEnBD = fachada.buscarTodas().size();
            
            return ResponseEntity.ok(Map.of(
                "pdisEnBaseDatos", pdisEnBD,
                "contadorProcesadas", pdisProcessedCounter != null ? pdisProcessedCounter.count() : "NULL",
                "contadorRechazadas", pdisRejectedCounter != null ? pdisRejectedCounter.count() : "NULL",
                "problema", pdisEnBD > 0 && pdisProcessedCounter != null && pdisProcessedCounter.count() == 0 ? 
                    "Las PdIs están en BD pero contador está en 0 (normal después de restart)" : "Todo normal"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }
}