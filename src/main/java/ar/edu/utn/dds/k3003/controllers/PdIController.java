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

    @PostMapping
    public ResponseEntity<PdIDTO> crear(@RequestBody PdIDTO dto) {
        try {
            PdIDTO resultado = fachada.procesar(dto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PdIDTO>> obtenerPdIs(@RequestParam(name = "hecho", required = false) String hecho) {
        try {
            if (hecho != null) {
                List<PdIDTO> pdis = fachada.buscarPorHecho(hecho);
                return ResponseEntity.ok(pdis);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdIDTO> obtenerPorId(@PathVariable String id) {
        try {
            PdIDTO pdi = fachada.buscarPdIPorId(id);
            return ResponseEntity.ok(pdi);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}