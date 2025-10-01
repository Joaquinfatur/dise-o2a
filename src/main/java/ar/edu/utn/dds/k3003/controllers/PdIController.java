package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.mappers.PdIDTOMapper;
import ar.edu.utn.dds.k3003.mappers.PdIMapper;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.persistence.PdIRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pdis")
public class PdIController {

    @Autowired
    private FachadaProcesadorPdI fachada;
    
    @Autowired
    private PdIRepository repository; // ← AQUÍ VA, DENTRO DE LA CLASE

    // GET /pdis/test
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        System.out.println("Test endpoint llamado");
        return ResponseEntity.ok(Map.of(
            "status", "PdIController working correctly",
            "service", "procesador-pdi",
            "timestamp", System.currentTimeMillis(),
            "endpoints", List.of(
                "POST /pdis - Create PdI",
                "GET /pdis - Get all PdIs",
                "GET /pdis?hecho={id} - Get PdIs by hecho",
                "GET /pdis/{id} - Get PdI by ID",
                "PATCH /pdis/{id} - Update PdI",
                "DELETE /pdis/{id} - Delete PdI",
                "GET /pdis/test - This test endpoint",
                "GET /pdis/stats - Statistics endpoint"
            )
        ));
    }

    // GET /pdis/stats 
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            System.out.println("Stats endpoint llamado");
            return ResponseEntity.ok(Map.of(
                "message", "Stats from PdIController",
                "controller", "PdIController",
                "fachada", fachada.getClass().getSimpleName(),
                "info", "For full stats check /stats endpoint"
            ));
        } catch (Exception e) {
            System.err.println("Error en stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error retrieving stats"));
        }
    }

    // POST /pdis
    @PostMapping
    public ResponseEntity<?> procesar(@RequestBody PdIDTO dto) {
        try {
            System.out.println("=== POST /pdis ===");
            System.out.println("ID recibido: " + dto.id());
            System.out.println("HechoId recibido: " + dto.hechoId());
            System.out.println("Contenido recibido: " + dto.contenido());
            
            PdIDTO resultado = fachada.procesar(dto);
            
            if (resultado == null) {
                System.out.println("PdI rechazada");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "PdI was rejected"));
            }
            
            System.out.println("PdI procesada exitosamente - ID resultado: " + resultado.id());
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("Error procesando PdI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Internal server error", 
                    "message", e.getMessage()
                ));
        }
    }

    // GET /pdis/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        try {
            System.out.println("=== GET /pdis/" + id + " ===");
            
            // Validar que el ID no sea un endpoint específico
            if ("test".equals(id) || "stats".equals(id) || "all".equals(id)) {
                return ResponseEntity.notFound().build();
            }
            
            PdIDTO pdi = fachada.buscarPdIPorId(id);
            System.out.println("PdI encontrada con ID: " + pdi.id());
            return ResponseEntity.ok(pdi);
            
        } catch (RuntimeException e) {
            System.err.println("PdI no encontrada con ID: " + id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error obteniendo PdI " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Internal server error",
                    "id", id
                ));
        }
    }

    // GET /pdis - Obtener todas o por hecho
    @GetMapping
    public ResponseEntity<?> obtenerPdIs(@RequestParam(name = "hecho", required = false) String hecho) {
        try {
            System.out.println("=== GET /pdis ===");
            System.out.println("Parámetro hecho: " + hecho);
            
            if (hecho != null && !hecho.trim().isEmpty()) {
                List<PdIDTO> pdis = fachada.buscarPorHecho(hecho);
                System.out.println("Encontradas " + pdis.size() + " PdIs para hecho " + hecho);
                return ResponseEntity.ok(pdis);
            } else {
                List<PdIDTO> todasLasPdis = fachada.buscarTodas();
                System.out.println("Encontradas " + todasLasPdis.size() + " PdIs en total");
                return ResponseEntity.ok(todasLasPdis);
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo PdIs para hecho " + hecho + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Internal server error",
                    "hecho", hecho
                ));
        }
    }

    // GET /pdis/all - Endpoint alternativo explícito
    @GetMapping("/all")
    public ResponseEntity<?> obtenerTodasLasPdIs() {
        try {
            System.out.println("=== GET /pdis/all ===");
            
            List<PdIDTO> todasLasPdis = fachada.buscarTodas();
            System.out.println("Encontradas " + todasLasPdis.size() + " PdIs en total");
            
            return ResponseEntity.ok(Map.of(
                "total", todasLasPdis.size(),
                "pdis", todasLasPdis
            ));
            
        } catch (Exception e) {
            System.err.println("Error obteniendo todas las PdIs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error retrieving all PdIs"));
        }
    }

    // PATCH /pdis/{id} - Actualizar PDI
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarPdI(
        @PathVariable String id,
        @RequestBody Map<String, Object> updates
    ) {
        try {
            System.out.println("=== PATCH /pdis/" + id + " ===");
            
            Optional<PdIEntity> optional = repository.findById(Integer.parseInt(id));
            
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            PdIEntity entity = optional.get();
            
            // Actualizar hechoId si viene en el request
            if (updates.containsKey("hecho_id")) {
                String nuevoHechoId = updates.get("hecho_id").toString();
                entity.setHechoId(Integer.parseInt(nuevoHechoId));
                System.out.println("Actualizando hechoId a: " + nuevoHechoId);
            }
            
            // Actualizar contenido si viene en el request
            if (updates.containsKey("contenido")) {
                entity.setContenido(updates.get("contenido").toString());
                System.out.println("Actualizando contenido");
            }
            
            // Guardar cambios
            PdIEntity saved = repository.save(entity);
            System.out.println("PDI actualizada exitosamente");
            
            return ResponseEntity.ok(convertirEntityADTO(saved));
            
        } catch (Exception e) {
            System.err.println("Error actualizando PDI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /pdis/{id} - Borrar PDI
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPdI(@PathVariable String id) {
        try {
            System.out.println("=== DELETE /pdis/" + id + " ===");
            
            repository.deleteById(Integer.parseInt(id));
            
            return ResponseEntity.ok(Map.of(
                "message", "PDI eliminada exitosamente",
                "id", id
            ));
            
        } catch (Exception e) {
            System.err.println("Error eliminando PDI: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Método helper para convertir Entity a DTO
    private PdIDTO convertirEntityADTO(PdIEntity entity) {
        PdI pdi = PdIMapper.toModel(entity);
        
        PdILocalDTO localDTO = new PdILocalDTO(
            String.valueOf(entity.getId()),
            entity.getHechoId() != null ? String.valueOf(entity.getHechoId()) : null,
            pdi.getContenido(),
            pdi.getUbicacion(),
            pdi.getFecha(),
            pdi.getUsuarioId(),
            combinarEtiquetas(entity)
        );
        
        return PdIDTOMapper.toFacadesDto(localDTO);
    }

    // Método helper para combinar etiquetas
    private List<String> combinarEtiquetas(PdIEntity entity) {
        List<String> todasLasEtiquetas = new ArrayList<>();
        
        if (entity.getEtiquetasNuevas() != null && !entity.getEtiquetasNuevas().isEmpty()) {
            todasLasEtiquetas.addAll(entity.getEtiquetasNuevas());
        }
        
        if (todasLasEtiquetas.isEmpty() && entity.getEtiquetas() != null) {
            todasLasEtiquetas.addAll(entity.getEtiquetas());
        }
        
        return todasLasEtiquetas;
    }
}