package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class FachadaProcesadorPdI {
    
    private final Map<String, PdIDTO> pdis = new HashMap<>();
    
    public PdIDTO procesar(PdIDTO dto) {
        // Implementación básica para procesar PdI
        pdis.put(dto.id(), dto);
        return dto;
    }
    
    public List<PdIDTO> buscarPorHecho(String hecho) {
        return pdis.values().stream()
                .filter(pdi -> hecho.equals(pdi.hechoId()))
                .toList();
    }
    
    public PdIDTO buscarPdIPorId(String id) {
        return pdis.get(id);
    }
}