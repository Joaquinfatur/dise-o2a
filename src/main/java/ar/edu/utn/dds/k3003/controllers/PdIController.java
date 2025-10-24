package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
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
    private FachadaProcesadorPdI fachadaProcesador;

    // GET /pdis - Listar todos los PDIs o filtrar por hecho
    @GetMapping
    public ResponseEntity<List<PdILocalDTO>> listarPdis(
            @RequestParam(value = "hecho", required = false) String hechoId) {
       
        if (hechoId != null) {
            List<PdILocalDTO> pdis = fachadaProcesador.listarPorHecho(hechoId);
            return ResponseEntity.ok(pdis);
        } else {
            List<PdILocalDTO> pdis = fachadaProcesador.listar();
            return ResponseEntity.ok(pdis);
        }
    }

    // GET /pdis/{id} - Obtener un PDI específico
    @GetMapping("/{id}")
    public ResponseEntity<PdILocalDTO> obtenerPdi(@PathVariable String id) {
        PdILocalDTO pdi = fachadaProcesador.obtener(id);
       
        if (pdi != null) {
            return ResponseEntity.ok(pdi);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /pdis - Procesar un nuevo PDI
    @PostMapping
    public ResponseEntity<PdILocalDTO> procesarPdi(@RequestBody PdILocalDTO pdiDTO) {
        try {
            PdILocalDTO resultado = fachadaProcesador.procesar(pdiDTO);
           
            if (resultado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            System.err.println("Error en POST /pdis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /pdis/limpiar - Endpoint útil para pruebas
    @DeleteMapping("/limpiar")
    public ResponseEntity<Map<String, String>> limpiarDatos() {
        fachadaProcesador.limpiarDatos();
        return ResponseEntity.ok(Map.of("mensaje", "Datos limpiados exitosamente"));
    }

    // GET /pdis/estadisticas - Ver estadísticas
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = fachadaProcesador.getEstadisticas();
        return ResponseEntity.ok(stats);
    }
}