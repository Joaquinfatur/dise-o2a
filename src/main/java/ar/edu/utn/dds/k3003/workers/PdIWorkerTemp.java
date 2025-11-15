package ar.edu.utn.dds.k3003.workers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.dtos.PdIMessageDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Worker que consume mensajes de RabbitMQ y procesa PDIs de forma asíncrona
 */
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class PdIWorkerTemp {
    
    private static final Logger log = LoggerFactory.getLogger(PdIWorkerTemp.class);
    
    @Autowired
    private FachadaProcesadorPdI fachada;
    
    private Counter messagesReceivedCounter;
    private Counter messagesProcessedCounter;
    private Counter messagesFailedCounter;
    private Timer messageProcessingTimer;
    
    @Autowired
    public PdIWorkerTemp(MeterRegistry meterRegistry) {
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
     * Procesa mensajes de PDI recibidos desde RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.name:pdis_queue}")
    public void procesarPdI(PdIMessageDTO mensaje) {
        Timer.Sample sample = Timer.start();
        messagesReceivedCounter.increment();
        
        log.info("=== WORKER RECIBIÓ MENSAJE ===");
        log.info("ID: {}", mensaje.getId());
        log.info("HechoId: {}", mensaje.getHechoId());
        log.info("Contenido: {}", mensaje.getContenido());
        
        try {
            // Convertir mensaje a DTO
            PdILocalDTO pdiDTO = new PdILocalDTO();
            pdiDTO.setId(mensaje.getId());
            pdiDTO.setHechoId(mensaje.getHechoId());
            pdiDTO.setContenido(mensaje.getContenido());
            pdiDTO.setUbicacion(mensaje.getUbicacion());
            pdiDTO.setUsuarioId(mensaje.getUsuarioId());
            pdiDTO.setFecha(mensaje.getFecha() != null ? mensaje.getFecha() : LocalDateTime.now());
            
            // Procesar COMPLETO (OCR + Etiquetado + Guardar)
            log.info("Iniciando procesamiento completo del PDI...");
            PdILocalDTO resultado = fachada.procesarCompleto(pdiDTO);
            
            if (resultado != null) {
                log.info("PDI procesado exitosamente. ID: {}", resultado.getId());
                log.info("Etiquetas generadas: {}", resultado.getTags());
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