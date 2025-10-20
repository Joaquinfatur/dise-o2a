package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI implements ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI {
    
    private FachadaSolicitudes fachadaSolicitudes;
    
    @Autowired
    private PdIRepository repository;
    
    @Autowired
    private ServicesClient servicesClient;
    
    private Counter pdisProcessedCounter;
    private Counter pdisRejectedCounter;
    private Timer processingTimer;
    
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+\\.(jpg|jpeg|png|gif|bmp|webp)",
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

    // ============ MÉTODOS DE LA INTERFAZ ============
    
    public List<PdIDTO> listar() {
        List<PdIEntity> entities = repository.findAll();
        return entities.stream()
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    @Override
    public PdIDTO buscarPdIPorId(String id) {
        Optional<PdIEntity> entity = repository.findById(Integer.parseInt(id));
        return entity.map(this::convertirEntityADTO).orElse(null);
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) {
        List<PdIEntity> entities = repository.findByHechoId(hechoId);
        return entities.stream()
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    @Override
    @Transactional
    public PdIDTO procesar(PdIDTO dto) {
        Timer.Sample sample = Timer.start();
        
        try {
            System.out.println("=== PROCESAR PDI ===");
            System.out.println("ID: " + dto.id());
            System.out.println("HechoId: " + dto.hechoId());
            System.out.println("Contenido: " + dto.contenido());
            
            // Validar contenido
            if (dto.contenido() == null || dto.contenido().trim().isEmpty()) {
                pdisRejectedCounter.increment();
                System.out.println("PDI rechazada: contenido vacío");
                return null;
            }
            
            // Validar hecho si está configurado
            if (dto.hechoId() != null && !dto.hechoId().trim().isEmpty()) {
                if (!servicesClient.isHechoActivoYValido(dto.hechoId())) {
                    pdisRejectedCounter.increment();
                    System.err.println("Hecho " + dto.hechoId() + " no válido - rechazando PDI");
                    return null;
                }
            }
            
            // Verificar si ya existe
            if (dto.id() != null) {
                Optional<PdIEntity> existente = repository.findById(Integer.parseInt(dto.id()));
                if (existente.isPresent()) {
                    System.out.println("PDI ya existe, devolviendo existente");
                    return convertirEntityADTO(existente.get());
                }
            }
            
            // Crear nueva entidad
            PdIEntity entity = new PdIEntity(dto.hechoId(), dto.contenido());
            
            // Buscar URL de imagen
            String imagenUrl = extraerImagenUrl(dto.contenido());
            
            if (imagenUrl != null) {
                System.out.println("URL de imagen encontrada: " + imagenUrl);
                entity.setImagenUrl(imagenUrl);
                
                // Procesar OCR
                try {
                    String ocrResultado = servicesClient.procesarOCR(imagenUrl);
                    entity.setOcrResultado(ocrResultado);
                    System.out.println("OCR completado");
                } catch (Exception e) {
                    System.err.println("Error en OCR: " + e.getMessage());
                    entity.setOcrResultado("{\"error\": \"" + e.getMessage() + "\"}");
                }
                
                // Procesar etiquetado
                try {
                    String etiquetadoResultado = servicesClient.procesarEtiquetado(imagenUrl);
                    entity.setEtiquetadoResultado(etiquetadoResultado);
                    System.out.println("Etiquetado completado");
                } catch (Exception e) {
                    System.err.println("Error en etiquetado: " + e.getMessage());
                    entity.setEtiquetadoResultado("{\"error\": \"" + e.getMessage() + "\"}");
                }
                
                // Generar etiquetas basadas en los resultados
                generarEtiquetas(entity);
            } else {
                System.out.println("No hay imagen, procesando solo texto");
                agregarEtiquetasPorTexto(entity);
            }
            
            entity.setProcesado(true);
            
            // Persistir en base de datos
            entity = repository.save(entity);
            System.out.println("PDI guardada con ID: " + entity.getId());
            
            pdisProcessedCounter.increment();
            
            // Notificar al agregador si corresponde
            if (dto.hechoId() != null && servicesClient != null) {
                try {
                    servicesClient.notifyAggregator(
                        dto.hechoId(),
                        List.of(String.valueOf(entity.getId()))
                    );
                } catch (Exception e) {
                    System.err.println("Error notificando agregador: " + e.getMessage());
                }
            }
            
            return convertirEntityADTO(entity);
            
        } finally {
            if (sample != null && processingTimer != null) {
                sample.stop(processingTimer);
            }
        }
    }

    // ============ MÉTODOS AUXILIARES ============
    
    public PdIDTO obtener(String id) {
        Optional<PdIEntity> entity = repository.findById(Integer.parseInt(id));
        return entity.map(this::convertirEntityADTO).orElse(null);
    }

    public List<PdIDTO> listarPorHecho(String hechoId) {
        List<PdIEntity> entities = repository.findByHechoId(hechoId);
        return entities.stream()
            .map(this::convertirEntityADTO)
            .collect(Collectors.toList());
    }

    private String extraerImagenUrl(String contenido) {
        if (contenido == null) return null;
        
        Matcher matcher = IMAGE_URL_PATTERN.matcher(contenido);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private void generarEtiquetas(PdIEntity entity) {
        List<String> etiquetas = new ArrayList<>();
        
        // Procesar resultado OCR
        if (entity.getOcrResultado() != null && !entity.getOcrResultado().contains("error")) {
            if (entity.getOcrResultado().contains("ParsedText")) {
                etiquetas.add("ConTextoOCR");
            }
        }
        
        // Procesar resultado etiquetado
        if (entity.getEtiquetadoResultado() != null && !entity.getEtiquetadoResultado().contains("error")) {
            if (entity.getEtiquetadoResultado().contains("labels")) {
                etiquetas.add("ConEtiquetasIA");
            }
        }
        
        // Etiquetas generales
        etiquetas.add("ConImagen");
        etiquetas.add("ProcesadoCompleto");
        
        entity.setEtiquetasNuevas(etiquetas);
    }

    private void agregarEtiquetasPorTexto(PdIEntity entity) {
        List<String> etiquetas = new ArrayList<>();
        
        String contenido = entity.getContenido().toLowerCase();
        
        if (contenido.length() > 100) {
            etiquetas.add("TextoLargo");
        } else {
            etiquetas.add("TextoCorto");
        }
        
        if (contenido.contains("urgente") || contenido.contains("importante")) {
            etiquetas.add("Prioritario");
        }
        
        etiquetas.add("SoloTexto");
        
        entity.setEtiquetasNuevas(etiquetas);
    }

    // ============ CONVERSIÓN DE ENTITIES A DTO ============
    
    private PdIDTO convertirEntityADTO(PdIEntity entity) {
        return new PdIDTO(
            String.valueOf(entity.getId()),
            entity.getHechoId(),
            entity.getContenido(),
            entity.getUbicacion() != null ? entity.getUbicacion() : "",
            entity.getFecha() != null ? entity.getFecha() : LocalDateTime.now(),
            entity.getUsuarioId() != null ? entity.getUsuarioId() : "",
            entity.getEtiquetasNuevas() != null ? entity.getEtiquetasNuevas() : new ArrayList<>()
        );
    }

    // ============ MÉTODOS UTILITARIOS ============
    
    @Transactional
    public void limpiarDatos() {
        repository.deleteAll();
        System.out.println("Todos los PDIs han sido eliminados");
    }

    public Map<String, Object> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEnBD", repository.count());
        stats.put("procesados", repository.findByProcesado(true).size());
        stats.put("sinProcesar", repository.findByProcesado(false).size());
        return stats;
    }
}