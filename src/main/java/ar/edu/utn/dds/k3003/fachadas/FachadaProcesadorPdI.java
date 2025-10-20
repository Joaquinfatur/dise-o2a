package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO; // ← Asegúrate de importar el correcto
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI implements ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI {

    @Autowired(required = false)
    private PdIRepository repository;
    
    private Map<Long, PdI> piezasProcesadas = new ConcurrentHashMap<>();
    private FachadaSolicitudes fachadaSolicitudes;

    @Override
    public List<PdIDTO> listar() {
        if (repository != null) {
            return repository.findAll().stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
        } else {
            return piezasProcesadas.values().stream()
                .map(this::convertirPdIADTO)
                .collect(Collectors.toList());
        }
    }

    @Override
    public PdIDTO buscarPdIPorId(String id) {
        if (repository != null) {
            return repository.findById(Long.parseLong(id))
                .map(this::convertirEntityADTO)
                .orElse(null);
        } else {
            PdI pdi = piezasProcesadas.get(Long.parseLong(id));
            return pdi != null ? convertirPdIADTO(pdi) : null;
        }
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) {
        if (repository != null) {
            return repository.findByHechoId(hechoId).stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
        } else {
            return piezasProcesadas.values().stream()
                .filter(pdi -> pdi.getHechoId().equals(hechoId))
                .map(this::convertirPdIADTO)
                .collect(Collectors.toList());
        }
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    // Método para convertir Entity a DTO
    private PdIDTO convertirEntityADTO(PdIEntity entity) {
        // OPCIÓN A: Si PdIDTO tiene constructor con 4 parámetros
        return new PdIDTO(
            String.valueOf(entity.getId()),
            entity.getHechoId(),
            entity.getContenido(),
            entity.getEtiquetasNuevas()
        );
        
        /* OPCIÓN B: Si PdIDTO tiene más parámetros, ajusta según corresponda
        return new PdIDTO(
            String.valueOf(entity.getId()),
            entity.getHechoId(),
            entity.getContenido(),
            entity.getUbicacion(),
            entity.getUsuarioId(),
            entity.getEtiquetasNuevas()
        );
        */
    }

    // Método para convertir PdI a DTO (para cuando no hay BD)
    private PdIDTO convertirPdIADTO(PdI pdi) {
        // OPCIÓN A: Constructor con 4 parámetros
        return new PdIDTO(
            pdi.getId().toString(),
            pdi.getHechoId(),
            pdi.getContenido(),
            pdi.getEtiquetas()
        );
        
        /* OPCIÓN B: Ajusta según tu PdIDTO
        return new PdIDTO(
            pdi.getId().toString(),
            pdi.getHechoId(),
            pdi.getContenido(),
            pdi.getUbicacion(),
            pdi.getUsuarioId(),
            pdi.getEtiquetas()
        );
        */
    }
    
    // Resto de tus métodos existentes...
}