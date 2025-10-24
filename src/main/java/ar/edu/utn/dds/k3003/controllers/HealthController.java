package ar.edu.utn.dds.k3003.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public String home() {
        return "Procesador PDI está corriendo";
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "procesador-pdi",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}