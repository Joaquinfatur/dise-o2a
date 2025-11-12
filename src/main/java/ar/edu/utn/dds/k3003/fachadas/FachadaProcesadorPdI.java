package ar.edu.utn.dds.k3003.fachadas;

import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FachadaProcesadorPdI {

    private final PdIRepository pdiRepository;
    private final ServicesClient servicesClient;

    public FachadaProcesadorPdI(PdIRepository pdiRepository, ServicesClient servicesClient) {
        this.pdiRepository = pdiRepository;
        this.servicesClient = servicesClient;
    }

    /**
     
     */
    public PdILocalDTO guardarSinProcesar(PdILocalDTO pdiDTO) {
        try {
            // Validar que el hecho existe
            if (!servicesClient.isHechoActivoYValido(pdiDTO.getHechoId())) {
                System.err.println("Hecho no válido: " + pdiDTO.getHechoId());
                return null;
            }
            
            // Crear entidad
            PdIEntity entity = new PdIEntity();
            entity.setHechoId(pdiDTO.getHechoId());
            entity.setContenido(pdiDTO.getContenido());
            entity.setFecha(LocalDateTime.now());
            entity.setProcesado(false); 
            
            // Si el contenido es una URL, también guardarla en imagenUrl
            if (pdiDTO.getContenido() != null && pdiDTO.getContenido().startsWith("http")) {
                entity.setImagenUrl(pdiDTO.getContenido());
            }
            
            // Guardar en base de datos
            entity = pdiRepository.save(entity);
            
            System.out.println("PDI guardado sin procesar: " + entity.getId());
            
            return entityToDTO(entity);
            
        } catch (Exception e) {
            System.err.println("Error guardando PDI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método original (mantener por compatibilidad)
     */
    public PdILocalDTO procesar(PdILocalDTO pdiDTO) {
        return guardarSinProcesar(pdiDTO);
    }

    /**
     * Listar todos los PDIs
     */
    public List<PdILocalDTO> listar() {
        return pdiRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar PDIs por hecho
     */
    public List<PdILocalDTO> listarPorHecho(String hechoId) {
        return pdiRepository.findByHechoId(hechoId)
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un PDI específico
     */
    public PdILocalDTO obtener(String id) {
        try {
            Integer pdiId = Integer.valueOf(id);
            Optional<PdIEntity> entity = pdiRepository.findById(pdiId);
            return entity.map(this::entityToDTO).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println("ID inválido: " + id);
            return null;
        }
    }

    /**
     * Limpiar todos los datos
     */
    public void limpiarDatos() {
        pdiRepository.deleteAll();
        System.out.println("Todos los PDIs eliminados");
    }

    /**
     * Obtener estadísticas
     */
    public Map<String, Object> getEstadisticas() {
        long total = pdiRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_pdis", total);
        
        // Contar por estado de procesamiento
        List<PdIEntity> todos = pdiRepository.findAll();
        
        long procesados = todos.stream()
            .filter(p -> p.getProcesado() != null && p.getProcesado())
            .count();
        
        long pendientes = total - procesados;
        
        Map<String, Long> porEstado = new HashMap<>();
        porEstado.put("PROCESADO", procesados);
        porEstado.put("PENDIENTE", pendientes);
        
        stats.put("por_estado", porEstado);
        
        return stats;
    }

    /**
     * Convertir entidad a DTO
     */
    private PdILocalDTO entityToDTO(PdIEntity entity) {
        PdILocalDTO dto = new PdILocalDTO();
        dto.setId(String.valueOf(entity.getId())); // Convertir Integer a String
        dto.setHechoId(entity.getHechoId());
        dto.setContenido(entity.getContenido());
        dto.setFuenteId(null); // No lo tenemos en la entidad
        
        
        dto.setEstadoProcesamiento(entity.getProcesado() ? "PROCESADO" : "PENDIENTE");
        dto.setTextoExtraido(entity.getOcrResultado());
        dto.setTags(entity.getEtiquetasNuevas());
        
        return dto;
    }
}