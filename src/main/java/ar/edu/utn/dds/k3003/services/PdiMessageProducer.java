package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.dtos.PdIMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**

 */
@Service
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class PdIMessageProducer {
    
    private static final Logger log = LoggerFactory.getLogger(PdIMessageProducer.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name:pdis_exchange}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key:pdis.process}")
    private String routingKey;
    
    /**
     * Publicar un mensaje de PDI a la cola para procesamiento
     */
    public void publicarPdI(PdIMessageDTO mensaje) {
        try {
            log.info("📤 Publicando mensaje a RabbitMQ");
            log.info("Exchange: {}, RoutingKey: {}", exchangeName, routingKey);
            log.info("Mensaje: {}", mensaje);
            
            rabbitTemplate.convertAndSend(exchangeName, routingKey, mensaje);
            
            log.info("✅ Mensaje publicado exitosamente");
            
        } catch (Exception e) {
            log.error("Error publicando mensaje a RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Error publicando a RabbitMQ", e);
        }
    }
    
    /**
     * Publicar un PDI simple para testing
     */
    public void publicarPdISimple(String hechoId, String contenido) {
        PdIMessageDTO mensaje = new PdIMessageDTO(
            hechoId,
            contenido,
            "Test Location",
            "test-user"
        );
        publicarPdI(mensaje);
    }
}