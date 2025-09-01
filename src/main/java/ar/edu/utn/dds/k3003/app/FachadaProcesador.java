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
         
        PdI nuevaPieza = new PdI(piezaDTO.getId(), piezaDTO.getContenido());
        nuevaPieza.etiquetar(List.of("Importante", "Ubicación relevante"));
        piezasProcesadas.put(nuevaPieza.getId(), nuevaPieza);
        return new PdILocalDTO(
            String.valueOf(nuevaPieza.getId()),    // id → String
            null,                                  // hechoId
            nuevaPieza.getContenido(),
            null,                                  // ubicacion
            null,                                  // fecha
            null,                                  // usuarioId
            nuevaPieza.getEtiquetas()
        );
    }
    
    
    private boolean hechoActivo(int idHecho) {
        // Simulación: Todos los hechos estan activos.
        return true;
    }

    public List<PdI> obtenerPdIsPorHecho(int idHecho) {
    List<PdI> piezasInternas = new ArrayList<>(piezasProcesadas.values());
    // Llama al método de simulación pasándole las piezas internas
    return SolicitudesSimulacion.obtenerPdIsPorHecho(idHecho, piezasInternas);
}
    
}

