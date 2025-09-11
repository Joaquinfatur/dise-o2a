package ar.edu.utn.dds.k3003.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/health")
    public String health() {
        return "Application is running";
    }

    @GetMapping("/test-pdis")
    public ResponseEntity<String> testPdis() {
    return ResponseEntity.ok("PdI endpoint working");
    }
}