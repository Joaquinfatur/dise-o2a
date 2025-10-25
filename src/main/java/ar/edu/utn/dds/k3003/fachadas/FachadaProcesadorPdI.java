package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI {

    @Autowired
    private PdIRepository repository;

    @Autowired(required = false)
    private ServicesClient servicesClient;

    private Counter pdisProcessedCounter;
    private Counter pdisRejectedCounter;
    private Timer processingTimer;

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://.*\\.(jpg|jpeg|png|gif|bmp|webp)",
        Pattern.CASE_INSENSITIVE
    );

    @Autowired
    public FachadaProcesadorPdI(MeterRegistry meterRegistry) {
        this.pdisProcessedCounter = Counter.builder("pdis.processed")
            .description("PDIs procesadas exitosamente")
            .register(meterRegistry);
        
        this.pdisRejectedCounter = Counter.builder("pdis.rejected")
            .description("PDIs rechazadas")
            .register(meterRegistry);
        
        this.processingTimer = Timer.builder("pdis.processing.time")
            .description("Tiempo de procesamiento de PDIs")
            .register(meterRegistry);
    }

    // ============ MÉTODOS PÚBLICOS ============
    
    @Transactional(readOnly = true)
    public List<PdILocalDTO> listar() {
        List<PdIEntity> entities = repository.findAll();
        return entities.stream()
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PdILocalDTO obtener(String id) {
        try {
            Optional<PdIEntity> entity = repository.findById(Integer.parseInt(id));
            return entity.map(this::convertirEntityADTO).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println("ID inválido: " + id);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<PdILocalDTO> listarPorHecho(String hechoId) {
        List<PdIEntity> entities = repository.findByHechoId(hechoId);
        return entities.stream()
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public PdILocalDTO procesar(PdILocalDTO dto) {
        Timer.Sample sample = Timer.start();
        
        try {
            System.out.println("=== PROCESAR PDI ===");
            System.out.println("ID: " + dto.getId());
            System.out.println("HechoId: " + dto.getHechoId());
            System.out.println("Contenido: " + dto.getContenido());
            
            // Validar contenido
            if (dto.getContenido() == null || dto.getContenido().trim().isEmpty()) {
                incrementarContador(pdisRejectedCounter);
                System.out.println("PDI rechazada: contenido vacío");
                return null;
            }
            if (esUrlImagen(dto.getContenido())) {
            Optional<PdIEntity> existente = repository.findFirstByImagenUrl(dto.getContenido());
            
            if (existente.isPresent()) {
                System.out.println("Imagen ya procesada, retornando existente ID: " 
                    + existente.get().getId());
                return convertirEntityADTO(existente.get());
            }
        }
            
            // Validar hecho si está configurado
            if (dto.getHechoId() != null && !dto.getHechoId().trim().isEmpty()) {
                if (servicesClient != null) {
                    try {
                        if (!servicesClient.isHechoActivoYValido(dto.getHechoId())) {
                            incrementarContador(pdisRejectedCounter);
                            System.err.println("Hecho " + dto.getHechoId() + " no válido - rechazando PDI");
                            return null;
                        }
                    } catch (Exception e) {
                        System.err.println("Error verificando hecho: " + e.getMessage());
                        // Continuar de todas formas
                    }
                }
            }
            
            // Crear entidad
            PdIEntity entity = new PdIEntity();
            entity.setHechoId(dto.getHechoId());
            entity.setContenido(dto.getContenido());
            entity.setUbicacion(dto.getUbicacion());
            entity.setFecha(LocalDateTime.now());
            entity.setUsuarioId(dto.getUsuarioId());
            entity.setProcesado(false);
            
            // Procesar imagen si es URL
            if (esUrlImagen(dto.getContenido())) {
                System.out.println("URL de imagen encontrada: " + dto.getContenido());
                entity.setImagenUrl(dto.getContenido());
                
                try {
                    String ocr = procesarOCR(dto.getContenido());
                    entity.setOcrResultado(ocr);
                    System.out.println("OCR completado");
                } catch (Exception e) {
                    System.err.println("Error en OCR: " + e.getMessage());
                }
                
                try {
                    String labeling = procesarLabeling(dto.getContenido());
                    entity.setEtiquetadoResultado(labeling);
                    System.out.println("Etiquetado completado");
                    
                    // Extraer etiquetas del resultado
                    List<String> etiquetas = extraerEtiquetas(labeling);
                    entity.setEtiquetasNuevas(etiquetas);
                } catch (Exception e) {
                    System.err.println("Error en Labeling: " + e.getMessage());
                }
            } else {
                // Procesar contenido de texto
                List<String> etiquetas = procesarContenido(dto.getContenido());
                entity.setEtiquetasNuevas(etiquetas);
            }
            
            entity.setProcesado(true);
            
            // Guardar en BD
            PdIEntity guardada = repository.save(entity);
            System.out.println("PDI guardada con ID: " + guardada.getId());
            
            // Notificar agregador (opcional)
            //if (servicesClient != null) {
             //   try {
               //     servicesClient.notificarNuevoPDI(guardada.getId().toString());
                //} catch (Exception e) {
                 //   System.err.println("Error notificando agregador: " + e.getMessage());
                //}
            //}
            
            incrementarContador(pdisProcessedCounter);
            sample.stop(processingTimer);
            
            return convertirEntityADTO(guardada);
            
        } catch (Exception e) {
            incrementarContador(pdisRejectedCounter);
            System.err.println("Error procesando PDI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public void limpiarDatos() {
        repository.deleteAll();
        System.out.println("Todos los PDIs eliminados");
    }

    public Map<String, Object> getEstadisticas() {
        long total = repository.count();
        long procesados = repository.countByProcesado(true);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_pdis", total);
        stats.put("pdis_procesadas", procesados);
        stats.put("pdis_pendientes", total - procesados);
        
        return stats;
    }

    // ============ MÉTODOS PRIVADOS ============
    
    private PdILocalDTO convertirEntityADTO(PdIEntity entity) {
        PdILocalDTO dto = new PdILocalDTO();
        
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setHechoId(entity.getHechoId());
        dto.setContenido(entity.getContenido());
        dto.setUbicacion(entity.getUbicacion());
        dto.setFecha(entity.getFecha());
        dto.setUsuarioId(entity.getUsuarioId());
        dto.setEtiquetas(entity.getEtiquetasNuevas());
        dto.setImagenUrl(entity.getImagenUrl());
        dto.setOcrResultado(entity.getOcrResultado());
        dto.setEtiquetadoResultado(entity.getEtiquetadoResultado());
        dto.setProcesado(entity.getProcesado() != null ? entity.getProcesado() : false);
        
        return dto;
    }

    private boolean esUrlImagen(String contenido) {
        if (contenido == null) return false;
        return URL_PATTERN.matcher(contenido.toLowerCase()).find();
    }

    private List<String> procesarContenido(String contenido) {
        List<String> etiquetas = new ArrayList<>();
        
        if (contenido == null || contenido.trim().isEmpty()) {
            return etiquetas;
        }
        
        String[] palabras = contenido.toLowerCase()
            .replaceAll("[^a-záéíóúñ\\s]", "")
            .split("\\s+");
        
        for (String palabra : palabras) {
            if (palabra.length() > 3) {
                etiquetas.add(palabra);
            }
        }
        
        return etiquetas.stream().distinct().limit(10).collect(Collectors.toList());
    }

    private String procesarOCR(String imageUrl) {
        if (servicesClient != null) {
            try {
                // Validar que la URL sea accesible
             if (!esUrlAccesible(imageUrl)) {
                return "Error: URL de imagen no accesible";
            }
            
            return servicesClient.procesarOCR(imageUrl);
        } catch (Exception e) {
            System.err.println("Error llamando OCR API: " + e.getMessage());
             return "Error en OCR: " + e.getMessage();
            }
        }
        return "OCR no disponible";
    }
    private boolean esUrlAccesible(String url) {
        try {
            WebClient.create()
                .head()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .block();
            return true;
        } catch (Exception e) {
            System.err.println("URL no accesible: " + url);
            return false;
        }
    }

    private String procesarLabeling(String imageUrl) {
        if (servicesClient != null) {
            try {
                return servicesClient.procesarEtiquetado(imageUrl);
            } catch (Exception e) {
                System.err.println("Error llamando Labeling API: " + e.getMessage());
                return "Error en Labeling: " + e.getMessage();
            }
        }
        return "Labeling no disponible";
    }

    private List<String> extraerEtiquetas(String resultadoLabeling) {
        List<String> etiquetas = new ArrayList<>();
        
        if (resultadoLabeling == null || resultadoLabeling.contains("Error")) {
            return etiquetas;
        }
        
        // Parsear JSON simple (buscar palabras entre comillas)
        String[] partes = resultadoLabeling.split("\"");
        for (int i = 0; i < partes.length; i++) {
            String parte = partes[i].trim();
            if (parte.length() > 2 && !parte.contains("{") && !parte.contains("}") 
                && !parte.contains(":") && !parte.matches(".*\\d+.*")) {
                etiquetas.add(parte);
            }
        }
        
        return etiquetas.stream().distinct().limit(10).collect(Collectors.toList());
    }

    private void incrementarContador(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }

}
