package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pdis")
public class PdIController {

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
            throw new UnsupportedOperationException("GET /pdis sin filtro");
        }
    }

    @GetMapping("/{id}")
    public PdIDTO obtenerPorId(@PathVariable String id) {
        return fachada.buscarPdIPorId(id);
    }
}