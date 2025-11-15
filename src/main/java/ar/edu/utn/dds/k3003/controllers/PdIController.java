// ✅ CÓDIGO NUEVO - PONER ESTO

package ar.edu.utn.dds.k3003.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.dtos.PdIMessageDTO;  // ← AGREGAR ESTE IMPORT
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.services.PdIMessageProducer;  // ← CAMBIAR EL IMPORT

@RestController
@RequestMapping("/pdis")
public class PdIController {
   
    @Autowired
    private FachadaProcesadorPdI fachadaProcesador;
    
    @Autowired
    private PdIMessageProducer messageProducer;  

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

    // POST /pdis - Procesar un nuevo PDI (ASINCRÓNICO)
    @PostMapping
    public ResponseEntity<PdILocalDTO> procesarPdi(@RequestBody PdILocalDTO pdiDTO) {
    try {
        System.out.println("Recibido POST /pdis");
        
        // 1. Guardar el PDI sin procesar (solo en BD)
        PdILocalDTO resultado = fachadaProcesador.guardarSinProcesar(pdiDTO);
        
        if (resultado != null) {
            System.out.println("PDI guardado con ID: " + resultado.getId());
            
            // 2. Crear mensaje con TODOS los datos del PDI (SIN FECHA - se genera automáticamente)
            PdIMessageDTO mensaje = new PdIMessageDTO(
            resultado.getId(),           // 1. id
            resultado.getHechoId(),      // 2. hechoId
            resultado.getContenido(),    // 3. contenido
            resultado.getUbicacion() != null ? resultado.getUbicacion() : "",  // 4. ubicacion
            resultado.getUsuarioId() != null ? resultado.getUsuarioId() : ""   // 5. usuarioId
            );
            
            // 3. Publicar mensaje a RabbitMQ para procesamiento asíncrono
            messageProducer.publicarPdI(mensaje);
            
            System.out.println("Mensaje enviado a RabbitMQ");
            
            // 4. Retornar respuesta inmediata (202 ACCEPTED)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resultado);
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