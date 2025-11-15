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
import jakarta.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name:pdis_queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.name:pdis_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key:pdis.process}")
    private String routingKey;
    
    @Value("${spring.rabbitmq.uri:NOT_SET}")
    private String rabbitmqUri;

    @PostConstruct
    public void debugRabbitMQConfig() {
        System.out.println("=================================================");
        System.out.println("🐰 RABBITMQ CONFIG DEBUG");
        System.out.println("=================================================");
        System.out.println("Queue Name: " + queueName);
        System.out.println("Exchange Name: " + exchangeName);
        System.out.println("Routing Key: " + routingKey);
        System.out.println("RabbitMQ URI: " + (rabbitmqUri != null && rabbitmqUri.length() > 30 
            ? rabbitmqUri.substring(0, 30) + "..." 
            : rabbitmqUri));
        System.out.println("RABBITMQ_ENABLED (env): " + System.getenv("RABBITMQ_ENABLED"));
        System.out.println("RABBITMQ_URL (env): " + 
            (System.getenv("RABBITMQ_URL") != null 
                ? System.getenv("RABBITMQ_URL").substring(0, Math.min(30, System.getenv("RABBITMQ_URL").length())) + "..." 
                : "NULL"));
        System.out.println("=================================================");
    }

    @Bean
    public Queue pdisQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public TopicExchange pdisExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue pdisQueue, TopicExchange pdisExchange) {
        return BindingBuilder
                .bind(pdisQueue)
                .to(pdisExchange)
                .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}