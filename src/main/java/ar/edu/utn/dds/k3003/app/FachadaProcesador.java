package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@Primary  
public class FachadaProcesador extends FachadaProcesadorPdI {
    private final Map<Integer, PdI> piezasProcesadas = new HashMap<>();

    @Autowired(required = false) 
    private ServicesClient servicesClient;

    @Autowired
    private Counter pdisProcessedCounter;
    
    @Autowired
    private Counter pdisRejectedCounter;
    
    @Autowired
    private Counter pdisErrorCounter;
    
    @Autowired
    private Timer processingTimer;

    public PdILocalDTO procesarPdI(PdILocalDTO piezaDTO) {
        Timer.Sample sample = Timer.start();

         System.out.println("=== DEBUG ===");
        System.out.println("hechoId recibido: " + piezaDTO.getHechoId());
        System.out.println("contenido recibido: " + piezaDTO.getContenido());
        try {
        if (piezaDTO.getContenido() == null || piezaDTO.getContenido().trim().isEmpty()) {
            pdisRejectedCounter.increment();
            System.out.println("PdI rechazada: contenido vacío");
            return null;
        }
        
        if (piezaDTO.getHechoId() != null && servicesClient != null) {
            String hechoId = piezaDTO.getHechoId();
            
            if (!servicesClient.isHechoActivo(hechoId)) {
                System.out.println("Hecho " + hechoId + " no está activo, rechazando PdI");
                pdisRejectedCounter.increment();
                return null;
            }
            Map<String, Object> hechoInfo = servicesClient.getHecho(hechoId);
            if (hechoInfo == null) {
                System.out.println("No se pudo obtener información del hecho " + hechoId);
            }
        } else if (piezaDTO.getHechoId() != null) {
            System.out.println("ServicesClient no disponible, usando lógica de simulación");
            int hechoId = Integer.parseInt(piezaDTO.getHechoId());
            if (!hechoActivoFallback(hechoId)) {
                pdisRejectedCounter.increment();
                return null;
            }
        }
        
        if (piezasProcesadas.containsKey(Integer.parseInt(piezaDTO.getId()))) {
            PdI piezaExistente = piezasProcesadas.get(Integer.parseInt(piezaDTO.getId()));
            System.out.println("PdI ya existe, devolviendo existente: " + piezaExistente.getId());
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
         
        PdI nuevaPieza = new PdI(Integer.parseInt(piezaDTO.getId()), piezaDTO.getContenido());
        
        if (piezaDTO.getHechoId() != null) {
            nuevaPieza.setHechoId(Integer.parseInt(piezaDTO.getHechoId()));
        }
        
        nuevaPieza.etiquetar(List.of("Importante", "Ubicación relevante"));
        
        piezasProcesadas.put(nuevaPieza.getId(), nuevaPieza);
        
        if (piezaDTO.getHechoId() != null && servicesClient != null) {
            try {
                servicesClient.notifyAggregator(
                    piezaDTO.getHechoId(), 
                    List.of(String.valueOf(nuevaPieza.getId()))
                );
            } catch (Exception e) {
                System.err.println("Error notificando agregador: " + e.getMessage());
            }
        }
        
        pdisProcessedCounter.increment();
        System.out.println("PdI procesada exitosamente: " + nuevaPieza.getId());
        
        return new PdILocalDTO(
            String.valueOf(nuevaPieza.getId()),
            piezaDTO.getHechoId(),
            nuevaPieza.getContenido(),
            null,
            null,
            null,
            nuevaPieza.getEtiquetas()
        );
        
        } catch (Exception e) {
        pdisErrorCounter.increment();
        System.err.println("Error procesando PdI: " + e.getMessage());
        e.printStackTrace();
        return null; 
     } finally {
        sample.stop(processingTimer);
        }
    }

    private boolean hechoActivoFallback(int idHecho) {
        return true;
    }
    
    public List<PdI> obtenerPdIsPorHecho(int idHecho) {
        List<PdI> piezasDelHecho = new ArrayList<>();
        for (PdI pieza : piezasProcesadas.values()) {
            if (pieza.getHechoId() == idHecho) {
                piezasDelHecho.add(pieza);
            }
        }
        System.out.println("Obteniendo PdIs para hecho " + idHecho + ": " + piezasDelHecho.size() + " encontradas");
        return piezasDelHecho;
    }

    public Map<String, Object> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalProcesadas", pdisProcessedCounter.count());
        stats.put("totalRechazadas", pdisRejectedCounter.count());
        stats.put("totalErrores", pdisErrorCounter.count());
        stats.put("enMemoria", piezasProcesadas.size());
        
        if (servicesClient != null) {
            try {
                stats.put("serviciosExternos", servicesClient.checkServicesHealth());
            } catch (Exception e) {
                stats.put("serviciosExternos", "Error: " + e.getMessage());
            }
        } else {
            stats.put("serviciosExternos", "ServicesClient no disponible");
        }
        
        return stats;
    }

    public void limpiarDatos() {
        piezasProcesadas.clear();

        System.out.println("Datos limpiados del procesador");
    }
}