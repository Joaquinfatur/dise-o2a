package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI; // ← CAMBIO: Usar la fachada de BD
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class StatsController {

    
    @Autowired
    private FachadaProcesadorPdI fachada;

    
    @Autowired(required = false)
    private Counter pdisProcessedCounter;
    
    @Autowired(required = false)
    private Counter pdisRejectedCounter;
    
    @Autowired(required = false)
    private Counter pdisErrorCounter;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {

            long totalEnBD = fachada.buscarTodas().size(); 
            
            Map<String, Object> stats = Map.of(
                "totalProcesadas", pdisProcessedCounter != null ? pdisProcessedCounter.count() : 0.0,
                "totalRechazadas", pdisRejectedCounter != null ? pdisRejectedCounter.count() : 0.0,
                "totalErrores", pdisErrorCounter != null ? pdisErrorCounter.count() : 0.0,
                "enBaseDatos", totalEnBD, 
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

    private static final long START_TIME = System.currentTimeMillis();

    @GetMapping("/uptime")
    public ResponseEntity<Map<String, Object>> getUptime() {
    long uptimeMs = System.currentTimeMillis() - START_TIME;
    long uptimeSeconds = uptimeMs / 1000;
    long uptimeMinutes = uptimeSeconds / 60;
    
    return ResponseEntity.ok(Map.of(
        "uptimeMs", uptimeMs,
        "uptimeSeconds", uptimeSeconds,
        "uptimeMinutes", uptimeMinutes,
        "startTime", new java.util.Date(START_TIME).toString(),
        "currentTime", new java.util.Date().toString()
    ));
    }
    @Autowired
    private javax.sql.DataSource dataSource;

    @GetMapping("/force-create-tables")
    public ResponseEntity<Map<String, Object>> forceCreateTables() {
    try {
        var conn = dataSource.getConnection();
        var stmt = conn.createStatement();
        
        // Crear tabla principal
        stmt.execute("CREATE TABLE IF NOT EXISTS pdis (id SERIAL PRIMARY KEY, hecho_id VARCHAR(255), contenido TEXT, ubicacion VARCHAR(255), fecha TIMESTAMP, usuario_id VARCHAR(255), imagen_url VARCHAR(500), ocr_resultado TEXT, etiquetado_resultado TEXT, procesado BOOLEAN DEFAULT false)");
        
        // Crear tablas de etiquetas
        stmt.execute("CREATE TABLE IF NOT EXISTS pdi_etiquetas_deprecated (pdi_id INTEGER REFERENCES pdis(id) ON DELETE CASCADE, etiqueta VARCHAR(255))");
        stmt.execute("CREATE TABLE IF NOT EXISTS pdi_etiquetas_nuevas (pdi_id INTEGER REFERENCES pdis(id) ON DELETE CASCADE, etiqueta VARCHAR(255))");
        
        conn.close();
        
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "Tablas creadas correctamente",
            "tables", List.of("pdis", "pdi_etiquetas_deprecated", "pdi_etiquetas_nuevas")
        ));
    } catch (Exception e) {
        return ResponseEntity.ok(Map.of(
            "status", "ERROR",
            "error", e.getMessage()
        ));
    }
    }
    /*
    @Bean
    public Gauge databasePdisGauge(MeterRegistry meterRegistry) {
    return Gauge.builder("procesador.database.pdis.real", this, obj -> {
        try {
            return fachada != null ? fachada.buscarTodas().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    })
    .description("Real PdIs count from PostgreSQL database")
    .tag("service", "procesador-pdi")
    .tag("env", "prod")
    .register(meterRegistry);
    }*/
}