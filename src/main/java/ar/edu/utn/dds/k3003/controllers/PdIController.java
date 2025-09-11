package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pdis")
public class PdIController {

    @Autowired
    private FachadaProcesadorPdI fachada;

    // POST /pdis
    @PostMapping
    public ResponseEntity<PdIDTO> procesar(@RequestBody PdIDTO dto) {
        try {
            System.out.println("Recibiendo PdI para procesar: " + dto.id());
            PdIDTO resultado = fachada.procesar(dto);
            
            if (resultado == null) {
                System.out.println("PdI rechazada: " + dto.id());
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("PdI procesada exitosamente: " + resultado.id());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.err.println("Error procesando PdI " + dto.id() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /pdis?hecho={hechoId}
    @GetMapping
    public ResponseEntity<?> obtenerPdIs(@RequestParam(name = "hecho", required = false) String hecho) {
        try {
            if (hecho != null && !hecho.trim().isEmpty()) {
                System.out.println("Buscando PdIs para hecho: " + hecho);
                List<PdIDTO> pdis = fachada.buscarPorHecho(hecho);
                System.out.println("Encontradas " + pdis.size() + " PdIs para hecho " + hecho);
                return ResponseEntity.ok(pdis);
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Parameter 'hecho' is required",
                        "usage", "GET /pdis?hecho={hechoId}",
                        "example", "GET /pdis?hecho=123"
                    ));
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo PdIs para hecho " + hecho + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    // GET /pdis/{id} 
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        try {
            System.out.println("Buscando PdI con ID: " + id);
            PdIDTO pdi = fachada.buscarPdIPorId(id);
            System.out.println("PdI encontrada: " + pdi.id());
            return ResponseEntity.ok(pdi);
        } catch (RuntimeException e) {
            System.err.println("PdI no encontrada con ID: " + id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error obteniendo PdI " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    // GET /pdis/test 
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
            "status", "PdIController working",
            "service", "procesador-pdi",
            "endpoints", List.of(
                "POST /pdis - Create PdI",
                "GET /pdis?hecho={id} - Get PdIs by hecho",
                "GET /pdis/{id} - Get PdI by ID",
                "GET /pdis/test - This test endpoint"
            )
        ));
    }

    // GET /pdis/stats 
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {

            if (fachada instanceof ar.edu.utn.dds.k3003.app.FachadaProcesador) {
                ar.edu.utn.dds.k3003.app.FachadaProcesador fachadaProcesador = 
                    (ar.edu.utn.dds.k3003.app.FachadaProcesador) fachada;
                return ResponseEntity.ok(fachadaProcesador.getEstadisticas());
            } else {
                return ResponseEntity.ok(Map.of(
                    "message", "Stats available at /stats endpoint",
                    "controller", "PdIController",
                    "fachada", fachada.getClass().getSimpleName()
                ));
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error retrieving stats"));
        }
    }
}