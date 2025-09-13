package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.mappers.PdIDTOMapper;
import ar.edu.utn.dds.k3003.mappers.PdIMapper;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.persistence.PdIRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI implements ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI {
    
    @Autowired
    private PdIRepository repository;
    
    private FachadaSolicitudes fachadaSolicitudes;

    // MÉTRICAS DATADOG
    @Autowired(required = false)
    private Counter pdisProcessedCounter;
    
    @Autowired(required = false)
    private Counter pdisRejectedCounter;
    
    @Autowired(required = false)
    private Counter pdisErrorCounter;
    
    @Autowired(required = false)
    private Timer processingTimer;

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }
    
    public List<PdIDTO> buscarTodas() {
        System.out.println("=== Buscando todas las PdIs ===");
        
        List<PdIEntity> entities = repository.findAll();
        System.out.println("Encontradas " + entities.size() + " entidades en la base de datos");
        
        return entities.stream()
            .map(entity -> {
                PdI pdi = PdIMapper.toModel(entity);
                PdILocalDTO localDTO = new PdILocalDTO(
                    String.valueOf(entity.getId()),
                    entity.getHechoId() != null ? String.valueOf(entity.getHechoId()) : null,
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
    public PdIDTO procesar(PdIDTO dto) {
        Timer.Sample sample = null;
        if (processingTimer != null) {
            sample = Timer.start();
        }
        
        System.out.println("=== PROCESAR PdI ===");
        System.out.println("MÉTODO LLAMADO: FachadaProcesadorPdI.procesar()");
        System.out.println("ID recibido: " + dto.id());
        System.out.println("HechoId recibido: " + dto.hechoId());
        System.out.println("Contenido recibido: " + dto.contenido());
        
        try {
            // Contenido no puede estar vacío
            if (dto.contenido() == null || dto.contenido().trim().isEmpty()) {
                if (pdisRejectedCounter != null) {
                    pdisRejectedCounter.increment();
                    System.out.println("PdI rechazada: contenido vacío. Contador rechazadas: " + pdisRejectedCounter.count());
                }
                return null;
            }
            
            // Convertir DTO
            PdILocalDTO localDTO = PdIDTOMapper.toLocalDto(dto);
         
            // VALIDACIÓN 2: Verificar si ya existe
            Optional<PdIEntity> existente = repository.findById(Integer.parseInt(localDTO.getId()));
            if (existente.isPresent()) {
                System.out.println("PdI ya existe, devolviendo existente");
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
            
            // CREAR NUEVA PdI
            System.out.println("Creando nueva PdI...");

            // Preparar TODAS las etiquetas ANTES de crear la PdI
            List<String> etiquetasFinales = new ArrayList<>();

            // Agregar etiquetas del usuario si existen
            if (localDTO.getEtiquetas() != null && !localDTO.getEtiquetas().isEmpty()) {
                etiquetasFinales.addAll(localDTO.getEtiquetas());
            }

            // Agregar etiquetas automáticas 
            etiquetasFinales.add("Procesado");
            if (!etiquetasFinales.contains("Importante")) {
                etiquetasFinales.add("Importante");
            }

            // Crear PdI con hechoId
            int hechoId = (localDTO.getHechoId() != null) ? Integer.parseInt(localDTO.getHechoId()) : 0;
            PdI nuevaPdi = new PdI(Integer.parseInt(localDTO.getId()), localDTO.getContenido(), hechoId);

            // Etiquetar UNA SOLA VEZ con todas las etiquetas
            nuevaPdi.etiquetar(etiquetasFinales);
            
            // Mapear a entidad
            PdIEntity entity = PdIMapper.toEntity(nuevaPdi);
            if (localDTO.getHechoId() != null && !localDTO.getHechoId().trim().isEmpty()) {
                entity.setHechoId(Integer.parseInt(localDTO.getHechoId()));
            }
            
            // GUARDAR EN BASE DE DATOS
            PdIEntity saved = repository.save(entity);
            System.out.println("PdI guardada en BD con ID: " + saved.getId());
            
            // INCREMENTAR CONTADOR DE PROCESADAS
            if (pdisProcessedCounter != null) {
                pdisProcessedCounter.increment();
                System.out.println("Contador incrementado: " + pdisProcessedCounter.count());
            } else {
                System.err.println("pdisProcessedCounter es NULL");
            }
            
            // Crear respuesta
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
            
            System.out.println("PdI procesada exitosamente: " + saved.getId());
            return PdIDTOMapper.toFacadesDto(result);
            
        } catch (Exception e) {
            // INCREMENTAR CONTADOR DE ERRORES
            if (pdisErrorCounter != null) {
                pdisErrorCounter.increment();
                System.err.println("Error counter incrementado: " + pdisErrorCounter.count());
            }
            
            System.err.println("Error procesando PdI: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error procesando PdI", e);
            
        } finally {
            // PARAR TIMER
            if (processingTimer != null && sample != null) {
                sample.stop(processingTimer);
            }
        }
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) {
        System.out.println("=== Buscando PdIs por hecho: " + hechoId + " ===");
        
        Integer id = Integer.parseInt(hechoId);
        List<PdIEntity> entities = repository.findByHechoId(id);
        
        System.out.println("Encontradas " + entities.size() + " PdIs para hecho " + hechoId);
        
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
        System.out.println("=== Buscando PdI por ID: " + id + " ===");
        
        Integer pdiId = Integer.parseInt(id);
        Optional<PdIEntity> entity = repository.findById(pdiId);
        
        if (entity.isEmpty()) {
            System.err.println("PdI no encontrada con ID: " + id);
            throw new RuntimeException("PdI no encontrada con ID: " + id);
        }
        
        System.out.println("PdI encontrada: " + entity.get().getId());
        
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

    // MÉTODO PARA VERIFICAR ESTADO DE MÉTRICAS
    public void verificarMetricas() {
        System.out.println("=== VERIFICACIÓN MÉTRICAS ===");
        System.out.println("pdisProcessedCounter: " + (pdisProcessedCounter != null ? "OK (" + pdisProcessedCounter.count() + ")" : "NULL"));
        System.out.println("pdisRejectedCounter: " + (pdisRejectedCounter != null ? "OK (" + pdisRejectedCounter.count() + ")" : "NULL"));
        System.out.println("pdisErrorCounter: " + (pdisErrorCounter != null ? "OK (" + pdisErrorCounter.count() + ")" : "NULL"));
        System.out.println("processingTimer: " + (processingTimer != null ? "OK" : "NULL"));
    }
}