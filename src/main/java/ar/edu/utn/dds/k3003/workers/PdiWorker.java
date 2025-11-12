package ar.edu.utn.dds.k3003.workers;

import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.config.RabbitMQConfig;
import ar.edu.utn.dds.k3003.dtos.PdiQueueMessage;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PdiWorker {

    private final PdIRepository pdiRepository;
    private final ServicesClient servicesClient;
    private final ObjectMapper objectMapper;
    
    // Métricas
    private final Counter pdisProcesadosCounter;
    private final Counter pdisErrorCounter;
    private final Timer processingTimer;
    
    public PdiWorker(
            PdIRepository pdiRepository,
            ServicesClient servicesClient,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        
        this.pdiRepository = pdiRepository;
        this.servicesClient = servicesClient;
        this.objectMapper = objectMapper;
        
        // Configurar métricas
        this.pdisProcesadosCounter = Counter.builder("pdi.procesados")
            .description("Número total de PDIs procesados exitosamente")
            .register(meterRegistry);
            
        this.pdisErrorCounter = Counter.builder("pdi.procesados.error")
            .description("Número total de PDIs con error en el procesamiento")
            .register(meterRegistry);
            
        this.processingTimer = Timer.builder("pdi.processing.time")
            .description("Tiempo de procesamiento de PDI (OCR + Tags)")
            .register(meterRegistry);
    }
    
    /**
     * Worker que escucha la cola y procesa PDIs
     */
    @RabbitListener(queues = RabbitMQConfig.PDI_QUEUE_NAME, concurrency = "2")
    public void procesarPdiDesdeCola(PdiQueueMessage mensaje) {
        
        Integer pdiId = Integer.valueOf(mensaje.getPdiId());
        System.out.println("🔄 Worker procesando PDI: " + pdiId);
        
        Timer.Sample sample = Timer.start();
        
        try {
            // 1. Buscar el PDI en la base de datos
            Optional<PdIEntity> pdiOpt = pdiRepository.findById(pdiId);
            
            if (pdiOpt.isEmpty()) {
                System.err.println("❌ PDI no encontrado: " + pdiId);
                pdisErrorCounter.increment();
                return;
            }
            
            PdIEntity pdi = pdiOpt.get();
            
            // 2. Verificar si ya fue procesado (idempotencia)
            if (pdi.getProcesado() != null && pdi.getProcesado()) {
                System.out.println("⚠️ PDI ya procesado: " + pdiId);
                return;
            }
            
            // 3. Procesar imagen si tiene URL
            String imagenUrl = extraerUrlImagen(pdi);
            if (imagenUrl != null) {
                procesarImagen(pdi, imagenUrl);
            } else {
                System.out.println("ℹ️ PDI sin imagen para procesar: " + pdiId);
            }
            
            // 4. Marcar como procesado
            pdi.setProcesado(true);
            pdiRepository.save(pdi);
            
            // 5. Registrar métricas
            sample.stop(processingTimer);
            pdisProcesadosCounter.increment();
            
            System.out.println("✅ PDI procesado exitosamente: " + pdiId);
            
        } catch (Exception e) {
            System.err.println("❌ Error procesando PDI " + pdiId + ": " + e.getMessage());
            e.printStackTrace();
            
            pdisErrorCounter.increment();
            sample.stop(processingTimer);
        }
    }
    
    /**
     * Extrae la URL de imagen del PDI
     */
    private String extraerUrlImagen(PdIEntity pdi) {
        // Prioridad: imagenUrl > contenido (si es URL)
        if (pdi.getImagenUrl() != null && !pdi.getImagenUrl().isEmpty()) {
            return pdi.getImagenUrl();
        }
        
        String contenido = pdi.getContenido();
        if (contenido != null && contenido.startsWith("http")) {
            return contenido;
        }
        
        return null;
    }
    
    /**
     * Procesa la imagen: OCR + Etiquetado
     */
    private void procesarImagen(PdIEntity pdi, String imagenUrl) {
        try {
            System.out.println("📷 Procesando imagen: " + imagenUrl);
            
            // 1. OCR - Extracción de texto
            String ocrResponse = servicesClient.procesarOCR(imagenUrl);
            String textoExtraido = extraerTextoDeOCR(ocrResponse);
            
            if (textoExtraido != null && !textoExtraido.isEmpty()) {
                pdi.setOcrResultado(textoExtraido);
                System.out.println("📝 Texto extraído: " + textoExtraido.substring(0, Math.min(50, textoExtraido.length())) + "...");
            }
            
            // 2. Etiquetado automático
            String labelingResponse = servicesClient.procesarEtiquetado(imagenUrl);
            List<String> tags = extraerTagsDeLabeling(labelingResponse);
            
            if (!tags.isEmpty()) {
                pdi.setEtiquetasNuevas(tags);
                System.out.println("🏷️ Tags generados: " + tags);
            }
            
            // Guardar el JSON completo también
            pdi.setEtiquetadoResultado(labelingResponse);
            
        } catch (Exception e) {
            System.err.println("Error procesando imagen: " + e.getMessage());
            // No lanzar excepción, solo loguear
        }
    }
    
    /**
     * Extrae el texto del response de OCR.space
     */
    private String extraerTextoDeOCR(String ocrResponse) {
        try {
            JsonNode root = objectMapper.readTree(ocrResponse);
            
            if (root.has("ParsedResults") && root.get("ParsedResults").isArray()) {
                JsonNode firstResult = root.get("ParsedResults").get(0);
                if (firstResult.has("ParsedText")) {
                    return firstResult.get("ParsedText").asText();
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error parseando OCR: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae los tags del response de APILayer Image Labeling
     */
    private List<String> extraerTagsDeLabeling(String labelingResponse) {
        List<String> tags = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(labelingResponse);
            
            // El response es un array de objetos con {label, confidence}
            if (root.isArray()) {
                for (JsonNode item : root) {
                    if (item.has("label")) {
                        String label = item.get("label").asText();
                        double confidence = item.has("confidence") ? 
                            item.get("confidence").asDouble() : 0.0;
                        
                        // Solo agregar tags con confianza > 0.7
                        if (confidence > 0.7) {
                            tags.add(label);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error parseando etiquetado: " + e.getMessage());
        }
        
        return tags;
    }
}