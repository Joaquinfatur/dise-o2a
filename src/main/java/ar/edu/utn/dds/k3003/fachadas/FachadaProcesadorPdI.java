package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.mappers.PdIDTOMapper;
import ar.edu.utn.dds.k3003.mappers.PdIMapper;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.persistence.PdIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI implements ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI {
    
    @Autowired
    private PdIRepository repository;

    @Override
    public PdIDTO procesar(PdIDTO dto) {
        PdILocalDTO localDTO = PdIDTOMapper.toLocalDto(dto);
        
        // Verificar si ya existe
        Optional<PdIEntity> existente = repository.findById(Integer.parseInt(localDTO.getId()));
        if (existente.isPresent()) {
            PdI pdi = PdIMapper.toModel(existente.get());
            PdILocalDTO result = new PdILocalDTO(
                String.valueOf(pdi.getId()),
                String.valueOf(existente.get().getHechoId()),
                pdi.getContenido(),
                pdi.getUbicacion(),
                pdi.getFecha(),
                pdi.getUsuarioId(),
                pdi.getEtiquetas()
            );
            return PdIDTOMapper.toFacadesDto(result);
        }
        
        // Crear nueva PdI
        PdI nuevaPdi = new PdI(localDTO.getId(), localDTO.getContenido());
        nuevaPdi.etiquetar(List.of("Procesado", "Importante"));
        
        // Crear entidad y guardar
        PdIEntity entity = PdIMapper.toEntity(nuevaPdi);
        if (localDTO.getHechoId() != null) {
            entity.setHechoId(Integer.parseInt(localDTO.getHechoId()));
        }
        
        PdIEntity saved = repository.save(entity);
        
        // Retornar resultado
        PdI savedPdi = PdIMapper.toModel(saved);
        PdILocalDTO result = new PdILocalDTO(
            String.valueOf(saved.getId()),
            String.valueOf(saved.getHechoId()),
            savedPdi.getContenido(),
            savedPdi.getUbicacion(),
            savedPdi.getFecha(),
            savedPdi.getUsuarioId(),
            savedPdi.getEtiquetas()
        );
        
        return PdIDTOMapper.toFacadesDto(result);
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) {
        Integer id = Integer.parseInt(hechoId);
        List<PdIEntity> entities = repository.findByHechoId(id);
        
        return entities.stream()
            .map(entity -> {
                PdI pdi = PdIMapper.toModel(entity);
                PdILocalDTO localDTO = new PdILocalDTO(
                    String.valueOf(entity.getId()),
                    String.valueOf(entity.getHechoId()),
                    pdi.getContenido(),
                    pdi.getUbicacion(),
                    pdi.getFecha(),
                    pdi.getUsuarioId(),
                    pdi.getEtiquetas()
                );
                return PdIDTOMapper.toFacadesDto(localDTO);
            })
            .collect(Collectors.toList());
    }

    @Override
    public PdIDTO buscarPdIPorId(String id) {
        Integer pdiId = Integer.parseInt(id);
        Optional<PdIEntity> entity = repository.findById(pdiId);
        
        if (entity.isEmpty()) {
            throw new RuntimeException("PdI no encontrada con ID: " + id);
        }
        
        PdI pdi = PdIMapper.toModel(entity.get());
        PdILocalDTO localDTO = new PdILocalDTO(
            String.valueOf(entity.get().getId()),
            String.valueOf(entity.get().getHechoId()),
            pdi.getContenido(),
            pdi.getUbicacion(),
            pdi.getFecha(),
            pdi.getUsuarioId(),
            pdi.getEtiquetas()
        );
        
        return PdIDTOMapper.toFacadesDto(localDTO);
    }
}