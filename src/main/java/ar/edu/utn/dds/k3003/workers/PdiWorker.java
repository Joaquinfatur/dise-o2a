package ar.edu.utn.dds.k3003.workers;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.dtos.PdIMessageDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Worker que consume mensajes de RabbitMQ y procesa PDIs de forma asíncrona
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class PdIWorker {
    
    private static final Logger log = LoggerFactory.getLogger(PdIWorker.class);
    
    @Autowired
    private FachadaProcesadorPdI fachada;
    
    private Counter messagesReceivedCounter;
    private Counter messagesProcessedCounter;
    private Counter messagesFailedCounter;
    private Timer messageProcessingTimer;
    
    @Autowired
    public PdIWorker(MeterRegistry meterRegistry) {
        // Métricas para el worker
        this.messagesReceivedCounter = Counter.builder("worker.messages.received")
                .description("Total de mensajes recibidos por el worker")
                .register(meterRegistry);
        
        this.messagesProcessedCounter = Counter.builder("worker.messages.processed")
                .description("Total de mensajes procesados exitosamente")
                .register(meterRegistry);
        
        this.messagesFailedCounter = Counter.builder("worker.messages.failed")
                .description("Total de mensajes que fallaron en procesamiento")
                .register(meterRegistry);
        
        this.messageProcessingTimer = Timer.builder("worker.message.processing.time")
                .description("Tiempo de procesamiento de cada mensaje")
                .register(meterRegistry);
    }
    
    /**

     */
    @RabbitListener(queues = "${rabbitmq.queue.name:pdis_queue}")
    public void procesarPdI(PdIMessageDTO mensaje) {
        Timer.Sample sample = Timer.start();
        messagesReceivedCounter.increment();
        
        log.info("=== WORKER RECIBIÓ MENSAJE ===");
        log.info("HechoId: {}", mensaje.getHechoId());
        log.info("Contenido: {}", mensaje.getContenido());
        log.info("Ubicación: {}", mensaje.getUbicacion());
        
        try {
            // Convertir mensaje a PdILocalDTO
            PdILocalDTO pdiDTO = new PdILocalDTO();
            pdiDTO.setHechoId(mensaje.getHechoId());
            pdiDTO.setContenido(mensaje.getContenido());
            pdiDTO.setUbicacion(mensaje.getUbicacion());
            pdiDTO.setUsuarioId(mensaje.getUsuarioId());
            pdiDTO.setFecha(mensaje.getFecha() != null ? mensaje.getFecha() : LocalDateTime.now());
            
            // Procesar el PDI (OCR + Etiquetado)
            log.info("Iniciando procesamiento de PDI...");
            PdILocalDTO resultado = fachada.procesar(pdiDTO);
            
            if (resultado != null) {
                log.info("PDI procesado exitosamente. ID: {}", resultado.getId());
                log.info("Etiquetas generadas: {}", resultado.getEtiquetas());
                messagesProcessedCounter.increment();
            } else {
                log.error("El procesamiento retornó null - PDI rechazado");
                messagesFailedCounter.increment();
                throw new RuntimeException("PDI rechazado por la fachada");
            }
            
        } catch (Exception e) {
            log.error("Error procesando PDI: {}", e.getMessage(), e);
            messagesFailedCounter.increment();
            
            // Relanzar la excepción para que RabbitMQ maneje el reintento
            throw new RuntimeException("Error procesando PDI", e);
            
        } finally {
            sample.stop(messageProcessingTimer);
            log.info("=== FIN DE PROCESAMIENTO ===");
        }
    }
}