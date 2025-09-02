package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pdis")

public class PdIController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
    }

    @RestController
    public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "ProcesadorPdI");
        return ResponseEntity.ok(status);
    }
    }

    @Autowired
    private FachadaProcesadorPdI fachada;

    @PostMapping
    public PdIDTO crear(@RequestBody PdIDTO dto) {
        return fachada.procesar(dto);
    }

    @GetMapping
    public List<PdIDTO> obtenerPorHecho(@RequestParam(name = "hecho", required = false) String hecho) {
        if (hecho != null) {
            return fachada.buscarPorHecho(hecho);
        } else {
            throw new UnsupportedOperationException("GET /pdis sin filtro no está soportado");
        }
    }

    @GetMapping("/{id}")
    public PdIDTO obtenerPorId(@PathVariable String id) {
        return fachada.buscarPdIPorId(id);
    }
}
