package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
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
    
    // Fachada de Solicitudes
    private FachadaSolicitudes fachadaSolicitudes;
    
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

    // ============ MÉTODOS DE LA INTERFAZ FachadaProcesadorPdI ============
    
    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }
    
    @Override
    public PdIDTO buscarPdIPorId(String id) {
        if (repository != null) {
            Optional<PdIEntity> entity = repository.findById(Integer.parseInt(id));
            return entity.map(this::convertirEntityADTO).orElse(null);
        } else {
            PdI pdi = piezasProcesadas.get(Integer.parseInt(id));
            return pdi != null ? convertirPdIADTO(pdi) : null;
        }
    }
    
    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) {
        if (repository != null) {
            List<PdIEntity> entities = repository.findByHechoId(hechoId);
            return entities.stream()
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
            
            // Validar hecho si está configurado
            if (dto.hechoId() != null && !dto.hechoId().trim().isEmpty()) {
                if (servicesClient != null && !servicesClient.isHechoActivoYValido(dto.hechoId())) {
                    incrementarContador(pdisRejectedCounter);
                    System.err.println("Hecho " + dto.hechoId() + " no válido - rechazando PDI");
                    return null;
                }
            }
            
            // Verificar si ya existe
            if (repository != null && dto.id() != null) {
                Optional<PdIEntity> existe = repository.findById(Integer.parseInt(dto.id()));
                if (existe.isPresent()) {
                    System.out.println("PDI ya existe con ID: " + dto.id());
                    return convertirEntityADTO(existe.get());
                }
            }
            
            // Procesar contenido y extraer etiquetas
            List<String> etiquetas = procesarContenido(dto.contenido());
            
            // Generar ID si no existe
            String nuevoId = dto.id() != null ? dto.id() : String.valueOf(System.currentTimeMillis());
            
            // Crear PdI procesada usando el constructor de record
            PdIDTO resultado = new PdIDTO(
                nuevoId,
                dto.hechoId(),
                dto.contenido(),
                "", // ubicacion - vacío por defecto
                LocalDateTime.now(),
                "", // usuarioId - vacío por defecto  
                etiquetas
            );
            
            // Guardar en base de datos o memoria
            if (repository != null) {
                PdIEntity entity = new PdIEntity();
                entity.setId(Integer.parseInt(resultado.id()));
                entity.setHechoId(resultado.hechoId());
                entity.setContenido(resultado.contenido());
                entity.setUbicacion("");
                //entity.setFecha(resultado.fecha());
                entity.setUsuarioId("");
                entity.setEtiquetasNuevas(resultado.etiquetas());
                repository.save(entity);
            } else {
                // Usar el constructor que existe en PdI: PdI(int id, String contenido, String hechoId)
                PdI pdi = new PdI(
                    Integer.parseInt(resultado.id()),
                    resultado.contenido(),
                    resultado.hechoId()
                );
                pdi.setUbicacion("");
                //pdi.setFecha(resultado.fecha());
                pdi.setUsuarioId("");
                pdi.etiquetarNuevo(resultado.etiquetas());
                piezasProcesadas.put(pdi.getId(), pdi);
            }
            
            incrementarContador(pdisProcessedCounter);
            System.out.println("PDI procesada exitosamente: " + resultado.id());
            return resultado;
            
        } catch (Exception e) {
            incrementarContador(pdisErrorCounter);
            System.err.println("Error procesando PDI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
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
    
    // ============ MÉTODOS AUXILIARES ============
    
    private List<String> procesarContenido(String contenido) {
        List<String> etiquetas = new ArrayList<>();
        
        // Detectar URLs de imágenes
        Matcher matcher = IMAGE_URL_PATTERN.matcher(contenido);
        if (matcher.find()) {
            etiquetas.add("Imagen");
        }
        
        // Análisis de contenido simple
        if (contenido.length() > 100) {
            etiquetas.add("Extenso");
        }
        
        if (contenido.toLowerCase().contains("urgente") || 
            contenido.toLowerCase().contains("importante")) {
            etiquetas.add("Importante");
        }
        
        return etiquetas;
    }
    
    private PdIDTO convertirEntityADTO(PdIEntity entity) {
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
}