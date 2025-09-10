package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pdis")
public class PdIController {

    @Autowired
    private FachadaProcesadorPdI fachada;

    // POST /pdis - Procesar una nueva PdI
    @PostMapping
    public ResponseEntity<PdIDTO> procesar(@RequestBody PdIDTO dto) {
        try {
            PdIDTO resultado = fachada.procesar(dto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /pdis - Obtener todas las PdIs (sin filtro)
    // GET /pdis?hecho={hechoId} - Obtener PdIs por hecho
    @GetMapping
    public ResponseEntity<List<PdIDTO>> obtenerPdIs(@RequestParam(name = "hecho", required = false) String hecho) {
        try {
            if (hecho != null && !hecho.trim().isEmpty()) {
                // Buscar por hecho específico
                List<PdIDTO> pdis = fachada.buscarPorHecho(hecho);
                return ResponseEntity.ok(pdis);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /pdis/{id} - Obtener PdI por ID
    @GetMapping("/{id}")
    public ResponseEntity<PdIDTO> obtenerPorId(@PathVariable String id) {
        try {
            System.out.println("Buscando PdI con ID: " + id);
            PdIDTO pdi = fachada.buscarPdIPorId(id);
            System.out.println("PdI encontrada: " + pdi);
            return ResponseEntity.ok(pdi);
        } catch (Exception e) {
            System.err.println("Error en GET /pdis/" + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}