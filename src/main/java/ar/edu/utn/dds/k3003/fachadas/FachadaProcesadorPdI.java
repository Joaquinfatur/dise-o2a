package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.mappers.PdIDTOMapper;
import ar.edu.utn.dds.k3003.mappers.PdIMapper;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.persistence.PdIRepository;
import ar.edu.utn.dds.k3003.services.ImageProcessingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI implements ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI {
    
    @Autowired
    private PdIRepository repository;
    
    @Autowired
    private ServicesClient servicesClient;
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
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
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PdIDTO procesar(PdIDTO dto) {
        Timer.Sample sample = null;
        if (processingTimer != null) {
            sample = Timer.start();
        }
        
        System.out.println("=== PROCESAR PdI===");
        System.out.println("ID recibido: " + dto.id());
        System.out.println("HechoId recibido: " + dto.hechoId());
        System.out.println("Contenido recibido: " + dto.contenido());
        
        try {
            
            if (dto.contenido() == null || dto.contenido().trim().isEmpty()) {
                if (pdisRejectedCounter != null) {
                    pdisRejectedCounter.increment();
                }
                System.out.println("PdI rechazada: contenido vacío");
                return null;
            }
            
            
            if (dto.hechoId() != null && !dto.hechoId().trim().isEmpty()) {
                
                
                if (!servicesClient.isSolicitudesServiceAvailable()) {
                    if (pdisRejectedCounter != null) {
                        pdisRejectedCounter.increment();
                    }
                    System.err.println("Servicio de Solicitudes no disponible - rechazando PDI");
                    return null;
                }
                
                
                if (!servicesClient.isHechoActivoYValido(dto.hechoId())) {
                    if (pdisRejectedCounter != null) {
                        pdisRejectedCounter.increment();
                    }
                    System.err.println("Hecho " + dto.hechoId() + " no válido o inactivo - rechazando PDI");
                    return null;
                }
            }
            
            
            Optional<PdIEntity> existente = repository.findById(Integer.parseInt(dto.id()));
            if (existente.isPresent()) {
                System.out.println("PdI ya existe, devolviendo existente");
                return convertirEntityADTO(existente.get());
            }
            
            
            System.out.println("Creando nueva PdI con procesamiento avanzado...");

         
            String hechoId = dto.hechoId();
            
            
            PdI nuevaPdi = new PdI(Integer.parseInt(dto.id()), dto.contenido(), hechoId);
            
            
            imageProcessingService.procesarPdICompleta(nuevaPdi);
            
            
            PdIEntity entity = PdIMapper.toEntity(nuevaPdi);
            entity.setHechoId(hechoId); 
            
            
            PdIEntity saved = repository.save(entity);
            System.out.println("PdI guardada en BD con ID: " + saved.getId());
            
            
            if (pdisProcessedCounter != null) {
                pdisProcessedCounter.increment();
                System.out.println("Contador incrementado: " + pdisProcessedCounter.count());
            }
            
            
            if (hechoId != null) {
                try {
                    servicesClient.notifyAggregator(
                        String.valueOf(hechoId), 
                        List.of(String.valueOf(saved.getId()))
                    );
                } catch (Exception e) {
                    System.err.println("Error notificando agregador: " + e.getMessage());
                }
            }
            
            System.out.println("PdI procesada exitosamente con análisis de imagen: " + saved.getId());
            return convertirEntityADTO(saved);
            
        } catch (Exception e) {
            
            if (pdisErrorCounter != null) {
                pdisErrorCounter.increment();
            }
            
            System.err.println("Error procesando PdI: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error procesando PdI", e);
            
        } finally {
            
            if (processingTimer != null && sample != null) {
                sample.stop(processingTimer);
            }
        }
    }

    @Override
        public List<PdIDTO> buscarPorHecho(String hechoId) {
        System.out.println("=== Buscando PdIs por hecho: " + hechoId + " ===");
    
        if (hechoId == null || hechoId.trim().isEmpty()) {
            System.err.println("HechoId vacío o null");
           return new ArrayList<>();
        }
    
        // ELIMINAR el parseInt, usar directamente
        List<PdIEntity> entities = repository.findByHechoId(hechoId);
    
        System.out.println("Encontradas " + entities.size() + " PdIs para hecho " + hechoId);
    
        return entities.stream()
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    @Override
    public PdIDTO buscarPdIPorId(String id) {
        System.out.println("=== Buscando PdI por ID: " + id + " ===");
        
        try {
            Integer pdiId = Integer.parseInt(id);
            Optional<PdIEntity> entity = repository.findById(pdiId);
            
            if (entity.isEmpty()) {
                System.err.println("PdI no encontrada con ID: " + id);
                throw new RuntimeException("PdI no encontrada con ID: " + id);
            }
            
            System.out.println("PdI encontrada: " + entity.get().getId());
            return convertirEntityADTO(entity.get());
            
        } catch (NumberFormatException e) {
            System.err.println("ID inválido: " + id);
            throw new RuntimeException("ID inválido: " + id);
        }
    }

    /**
     * Método para convertir Entity a DTO con campos de Entrega 4
     */
    private PdIDTO convertirEntityADTO(PdIEntity entity) {
        PdI pdi = PdIMapper.toModel(entity);
        
        
        PdILocalDTO localDTO = new PdILocalDTO(
            String.valueOf(entity.getId()),
            entity.getHechoId() != null ? String.valueOf(entity.getHechoId()) : null, 
            pdi.getContenido(),
            pdi.getUbicacion(),
            pdi.getFecha(),
            pdi.getUsuarioId(),
            combinarEtiquetas(entity) 
        );
        
        return PdIDTOMapper.toFacadesDto(localDTO);
    }

    /**
     * Combina etiquetas deprecated con las nuevas
     */
    private List<String> combinarEtiquetas(PdIEntity entity) {
        List<String> todasLasEtiquetas = new ArrayList<>();
        
       
        if (entity.getEtiquetasNuevas() != null && !entity.getEtiquetasNuevas().isEmpty()) {
            todasLasEtiquetas.addAll(entity.getEtiquetasNuevas());
        }
        
        
        if (todasLasEtiquetas.isEmpty() && entity.getEtiquetas() != null) {
            todasLasEtiquetas.addAll(entity.getEtiquetas());
            
            todasLasEtiquetas.add("EtiquetasDeprecated");
        }
        
        return todasLasEtiquetas;
    }

    /**
     * Método para estadísticas avanzadas
     */
    public void verificarMetricas() {
        System.out.println("=== VERIFICACIÓN MÉTRICAS ENTREGA 4 ===");
        System.out.println("pdisProcessedCounter: " + (pdisProcessedCounter != null ? "OK (" + pdisProcessedCounter.count() + ")" : "NULL"));
        System.out.println("pdisRejectedCounter: " + (pdisRejectedCounter != null ? "OK (" + pdisRejectedCounter.count() + ")" : "NULL"));
        System.out.println("pdisErrorCounter: " + (pdisErrorCounter != null ? "OK (" + pdisErrorCounter.count() + ")" : "NULL"));
        System.out.println("processingTimer: " + (processingTimer != null ? "OK" : "NULL"));
        System.out.println("ServicesClient: " + (servicesClient != null ? "OK" : "NULL"));
        System.out.println("ImageProcessingService: " + (imageProcessingService != null ? "OK" : "NULL"));
        
        if (servicesClient != null) {
            System.out.println("Health check servicios externos:");
            servicesClient.checkServicesHealth().forEach((k, v) -> 
                System.out.println("  " + k + ": " + v)
            );
        }
    }

    /**
     * Método para obtener estadísticas detalladas
     */
    public java.util.Map<String, Object> obtenerEstadisticasDetalladas() {
        long totalPdIs = repository.count();
        long conImagen = repository.findAll().stream()
            .filter(entity -> entity.getImagenUrl() != null && !entity.getImagenUrl().trim().isEmpty())
            .count();
        
        return java.util.Map.of(
            "totalPdIs", totalPdIs,
            "conImagenes", conImagen,
            "sinImagenes", totalPdIs - conImagen,
            "procesadasExitosas", pdisProcessedCounter != null ? pdisProcessedCounter.count() : 0,
            "rechazadas", pdisRejectedCounter != null ? pdisRejectedCounter.count() : 0,
            "errores", pdisErrorCounter != null ? pdisErrorCounter.count() : 0,
            "serviciosExternos", servicesClient != null ? servicesClient.checkServicesHealth() : "No disponible"
        );
    }
}