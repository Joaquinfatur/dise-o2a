package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.config.RabbitMQConfig;
import ar.edu.utn.dds.k3003.dtos.PdiQueueMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PdiQueueProducer {

    private final RabbitTemplate rabbitTemplate;
    private final Counter pdisEncoladosCounter;
    
    public PdiQueueProducer(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        
        // Métrica: contador de PDIs encolados
        this.pdisEncoladosCounter = Counter.builder("pdi.encolados")
            .description("Número total de PDIs enviados a la cola para procesamiento")
            .register(meterRegistry);
    }
    
    /**
     * Envía un PDI a la cola para ser procesado de forma asincrónica
     */
    public void enviarPdiParaProcesar(String pdiId) {
        try {
            PdiQueueMessage mensaje = new PdiQueueMessage(pdiId);
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.PDI_QUEUE_NAME, mensaje);
            
            System.out.println("✅ PDI encolado: " + pdiId);
            pdisEncoladosCounter.increment();
            
        } catch (Exception e) {
            System.err.println("Error encolando PDI " + pdiId + ": " + e.getMessage());
            throw new RuntimeException("Error encolando PDI", e);
        }
    }
}