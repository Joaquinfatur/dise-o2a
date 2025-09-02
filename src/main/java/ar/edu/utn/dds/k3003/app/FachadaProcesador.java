package ar.edu.utn.dds.k3003.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import ar.edu.utn.Simulacion.SolicitudesSimulacion;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.model.PdI.PdI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class FachadaProcesador extends FachadaProcesadorPdI {
    private final Map<Integer, PdI> piezasProcesadas = new HashMap<>();

    public PdILocalDTO procesarPdI(PdILocalDTO piezaDTO) {
        // Validar entrada inválida (contenido vacío)
        if (piezaDTO.getContenido() == null || piezaDTO.getContenido().trim().isEmpty()) {
            return null;
        }
        
        // Validar si el hecho está activo
        if (piezaDTO.getHechoId() != null) {
            int hechoId = Integer.parseInt(piezaDTO.getHechoId());
            if (!SolicitudesSimulacion.hechoActivo(hechoId)) {
                return null;
            }
        }
        
        // Si ya existe, devolver la existente
        if (piezasProcesadas.containsKey(piezaDTO.getId())) {
            PdI piezaExistente = piezasProcesadas.get(piezaDTO.getId());
            return new PdILocalDTO(
                String.valueOf(piezaExistente.getId()),
                String.valueOf(piezaExistente.getHechoId()),
                piezaExistente.getContenido(),
                piezaExistente.getUbicacion(),
                piezaExistente.getFecha(),
                piezaExistente.getUsuarioId(),
                piezaExistente.getEtiquetas()
            );
        }
         
        // Crear nueva PdI
        PdI nuevaPieza = new PdI(piezaDTO.getId(), piezaDTO.getContenido());
        
        // Establecer hechoId si está presente
        if (piezaDTO.getHechoId() != null) {
            nuevaPieza.setHechoId(Integer.parseInt(piezaDTO.getHechoId()));
        }
        
        // Etiquetar la pieza
        nuevaPieza.etiquetar(List.of("Importante", "Ubicación relevante"));
        
        // Guardar en el mapa
        piezasProcesadas.put(nuevaPieza.getId(), nuevaPieza);
        
        return new PdILocalDTO(
            String.valueOf(nuevaPieza.getId()),    // id → String
            piezaDTO.getHechoId(),                 // hechoId mantenido
            nuevaPieza.getContenido(),
            null,                                  // ubicacion
            null,                                  // fecha
            null,                                  // usuarioId
            nuevaPieza.getEtiquetas()
        );
    }
    
    
    private boolean hechoActivo(int idHecho) {
        // Delegar a la simulación
        return SolicitudesSimulacion.hechoActivo(idHecho);
    }

    public List<PdI> obtenerPdIsPorHecho(int idHecho) {
        List<PdI> piezasInternas = new ArrayList<>(piezasProcesadas.values());
        // Llama al método de simulación pasándole las piezas internas
        return SolicitudesSimulacion.obtenerPdIsPorHecho(idHecho, piezasInternas);
    }
}

