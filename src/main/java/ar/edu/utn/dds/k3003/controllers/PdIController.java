package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/pdis")
public class PdIController {

    private final FachadaProcesadorPdI fachada;

    public PdIController(FachadaProcesadorPdI fachada) {
        this.fachada = fachada;
    }

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