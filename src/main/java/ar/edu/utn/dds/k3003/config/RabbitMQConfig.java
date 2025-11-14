package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name:pdis_queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.name:pdis_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key:pdis.process}")
    private String routingKey;

    /**
     * Declarar la cola (queue) donde se publicarán los PDIs a procesar
     */
    @Bean
    public Queue pdisQueue() {
        return QueueBuilder.durable(queueName)
                .build();
    }

    /**
     * Declarar el exchange de tipo topic
     */
    @Bean
    public TopicExchange pdisExchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * Binding: conectar la cola con el exchange usando la routing key
     */
    @Bean
    public Binding binding(Queue pdisQueue, TopicExchange pdisExchange) {
        return BindingBuilder
                .bind(pdisQueue)
                .to(pdisExchange)
                .with(routingKey);
    }

    /**
     * Message converter para serializar/deserializar JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate para enviar mensajes (útil para testing)
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}