package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FachadaProcesador implements FachadaProcesadorPdI {
    
    // Repositorio JPA para persistencia
    @Autowired(required = false)
    private PdIRepository repository;
    
    // Cliente para servicios externos
    @Autowired(required = false)
    private ServicesClient servicesClient;
    
    // Map en memoria como fallback
    private Map<Integer, PdI> piezasProcesadas = new HashMap<>();
    
    // Métricas
    private Counter pdisProcessedCounter;
    private Counter pdisRejectedCounter;
    private Counter pdisErrorCounter;
    
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+\\.(jpg|jpeg|png|gif|bmp|webp)",
        Pattern.CASE_INSENSITIVE
    );

    // Constructor sin parámetros para compatibilidad
    public FachadaProcesador() {
        // Inicialización básica
    }
    
    // Constructor con MeterRegistry opcional
    @Autowired(required = false)
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        if (meterRegistry != null) {
            this.pdisProcessedCounter = Counter.builder("pdis.processed")
                .description("PDIs procesadas exitosamente")
                .register(meterRegistry);
            
            this.pdisRejectedCounter = Counter.builder("pdis.rejected")
                .description("PDIs rechazadas")
                .register(meterRegistry);
            
            this.pdisErrorCounter = Counter.builder("pdis.error")
                .description("Errores procesando PDIs")
                .register(meterRegistry);
        }
    }

    @Override
    @Transactional
    public PdIDTO procesar(PdIDTO dto) {
        try {
            System.out.println("=== PROCESAR PDI ===");
            System.out.println("ID: " + dto.id());
            System.out.println("HechoId: " + dto.hechoId());
            System.out.println("Contenido: " + dto.contenido());
            
            // Validar contenido
            if (dto.contenido() == null || dto.contenido().trim().isEmpty()) {
                incrementarContador(pdisRejectedCounter);
                System.out.println("PDI rechazada: contenido vacío");
                return null;
            }
            
            // Si tenemos repository, intentar usar persistencia
            if (repository != null) {
                return procesarConPersistencia(dto);
            } else {
                // Fallback a memoria
                return procesarEnMemoria(dto);
            }
            
        } catch (Exception e) {
            incrementarContador(pdisErrorCounter);
            System.err.println("Error procesando PDI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private PdIDTO procesarConPersistencia(PdIDTO dto) {
        // Verificar si ya existe
        if (dto.id() != null) {
            try {
                Optional<PdIEntity> existente = repository.findById(Integer.parseInt(dto.id()));
                if (existente.isPresent()) {
                    System.out.println("PDI ya existe en BD");
                    return convertirEntityADTO(existente.get());
                }
            } catch (Exception e) {
                System.err.println("Error buscando PDI existente: " + e.getMessage());
            }
        }
        
        // Crear nueva entidad
        PdIEntity entity = new PdIEntity(dto.hechoId(), dto.contenido());
        
        // Procesar imagen si existe
        String imagenUrl = extraerImagenUrl(dto.contenido());
        
        if (imagenUrl != null) {
            System.out.println("URL de imagen encontrada: " + imagenUrl);
            entity.setImagenUrl(imagenUrl);
            
            // Procesar OCR si el servicio está disponible
            if (servicesClient != null) {
                try {
                    String ocrResultado = servicesClient.procesarOCR(imagenUrl);
                    entity.setOcrResultado(ocrResultado);
                    System.out.println("OCR completado y guardado");
                } catch (Exception e) {
                    System.err.println("Error en OCR: " + e.getMessage());
                    entity.setOcrResultado("{\"error\": \"" + e.getMessage() + "\"}");
                }
                
                try {
                    String etiquetadoResultado = servicesClient.procesarEtiquetado(imagenUrl);
                    entity.setEtiquetadoResultado(etiquetadoResultado);
                    System.out.println("Etiquetado completado y guardado");
                } catch (Exception e) {
                    System.err.println("Error en etiquetado: " + e.getMessage());
                    entity.setEtiquetadoResultado("{\"error\": \"" + e.getMessage() + "\"}");
                }
            }
            
            generarEtiquetas(entity);
        } else {
            agregarEtiquetasPorTexto(entity);
        }
        
        entity.setProcesado(true);
        
        // Persistir
        entity = repository.save(entity);
        System.out.println("PDI guardada en BD con ID: " + entity.getId());
        
        incrementarContador(pdisProcessedCounter);
        
        return convertirEntityADTO(entity);
    }
    
    private PdIDTO procesarEnMemoria(PdIDTO dto) {
        // Verificar si ya existe en memoria
        int id = dto.id() != null ? Integer.parseInt(dto.id()) : piezasProcesadas.size() + 1;
        
        if (piezasProcesadas.containsKey(id)) {
            PdI existente = piezasProcesadas.get(id);
            System.out.println("PDI ya existe en memoria");
            return convertirPdIADTO(existente);
        }
        
        // Crear nuevo PdI
        PdI pdi = new PdI(id, dto.contenido());
        if (dto.hechoId() != null) {
            pdi.setHechoId(dto.hechoId());
        }
        
        // Procesar imagen si existe
        String imagenUrl = extraerImagenUrl(dto.contenido());
        
        if (imagenUrl != null) {
            System.out.println("URL de imagen encontrada: " + imagenUrl);
            pdi.setImagenUrl(imagenUrl);
            
            if (servicesClient != null) {
                try {
                    String ocrResultado = servicesClient.procesarOCR(imagenUrl);
                    pdi.setOcrResultado(ocrResultado);
                    System.out.println("OCR completado (memoria)");
                } catch (Exception e) {
                    System.err.println("Error en OCR: " + e.getMessage());
                }
                
                try {
                    String etiquetadoResultado = servicesClient.procesarEtiquetado(imagenUrl);
                    pdi.setEtiquetadoResultado(etiquetadoResultado);
                    System.out.println("Etiquetado completado (memoria)");
                } catch (Exception e) {
                    System.err.println("Error en etiquetado: " + e.getMessage());
                }
            }
            
            pdi.etiquetarNuevo(List.of("ConImagen", "Procesado"));
        } else {
            pdi.etiquetarNuevo(List.of("SoloTexto", "Procesado"));
        }
        
        pdi.setProcesado(true);
        
        // Guardar en memoria
        piezasProcesadas.put(id, pdi);
        System.out.println("PDI guardada en memoria con ID: " + id);
        
        incrementarContador(pdisProcessedCounter);
        
        return convertirPdIADTO(pdi);
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
        
        if (entity.getOcrResultado() != null && !entity.getOcrResultado().contains("error")) {
            etiquetas.add("ConTextoOCR");
        }
        
        if (entity.getEtiquetadoResultado() != null && !entity.getEtiquetadoResultado().contains("error")) {
            etiquetas.add("ConEtiquetasIA");
        }
        
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
    
    private PdIDTO convertirEntityADTO(PdIEntity entity) {
        // Usar el constructor correcto de PdIDTO con todos los parámetros
        return new PdIDTO(
            String.valueOf(entity.getId()),
            entity.getHechoId(),
            entity.getContenido(),
            entity.getUbicacion() != null ? entity.getUbicacion() : "",
            entity.getFecha(),
            entity.getUsuarioId() != null ? entity.getUsuarioId() : "",
            entity.getEtiquetasNuevas()
        );
    }
    
    private PdIDTO convertirPdIADTO(PdI pdi) {
        // Usar el constructor correcto de PdIDTO con todos los parámetros
        return new PdIDTO(
            String.valueOf(pdi.getId()),
            pdi.getHechoId(),
            pdi.getContenido(),
            pdi.getUbicacion() != null ? pdi.getUbicacion() : "",
            pdi.getFecha(),
            pdi.getUsuarioId() != null ? pdi.getUsuarioId() : "",
            pdi.getEtiquetasNuevas()
        );
    }
    
    private void incrementarContador(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }
    
    // Métodos adicionales para obtener PDIs
    public List<PdIDTO> listarTodos() {
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
    
    public PdIDTO obtenerPorId(String id) {
        if (repository != null) {
            Optional<PdIEntity> entity = repository.findById(Integer.parseInt(id));
            return entity.map(this::convertirEntityADTO).orElse(null);
        } else {
            PdI pdi = piezasProcesadas.get(Integer.parseInt(id));
            return pdi != null ? convertirPdIADTO(pdi) : null;
        }
    }
    
    public List<PdIDTO> listarPorHecho(String hechoId) {
        if (repository != null) {
            return repository.findByHechoId(hechoId).stream()
                .map(this::convertirEntityADTO)
                .collect(Collectors.toList());
        } else {
            return piezasProcesadas.values().stream()
                .filter(pdi -> hechoId.equals(pdi.getHechoId()))
                .map(this::convertirPdIADTO)
                .collect(Collectors.toList());
        }
    }
    
    // Método para limpiar datos
    @Transactional
    public void limpiarDatos() {
        if (repository != null) {
            repository.deleteAll();
        }
        piezasProcesadas.clear();
        System.out.println("Datos limpiados");
    }
    
    // Método para estadísticas
    public Map<String, Object> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        if (repository != null) {
            stats.put("totalEnBD", repository.count());
            stats.put("modo", "persistencia");
        } else {
            stats.put("totalEnMemoria", piezasProcesadas.size());
            stats.put("modo", "memoria");
        }
        
        if (pdisProcessedCounter != null) {
            stats.put("procesadas", pdisProcessedCounter.count());
        }
        
        if (pdisRejectedCounter != null) {
            stats.put("rechazadas", pdisRejectedCounter.count());
        }
        
        return stats;
    }
}