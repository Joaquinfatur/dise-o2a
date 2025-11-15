package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    
   
    @Value("${rabbitmq.url:}")
    private String rabbitmqUrl;

    @PostConstruct
    public void debugRabbitMQConfig() {
        System.out.println("=================================================");
        System.out.println("RABBITMQ CONFIG DEBUG");
        System.out.println("=================================================");
        System.out.println("RabbitMQ URL configurada: " + (rabbitmqUrl != null && !rabbitmqUrl.isEmpty() ? "SÍ" : "NO"));
        System.out.println("Queue Name: " + queueName);
        System.out.println("Exchange Name: " + exchangeName);
        System.out.println("Routing Key: " + routingKey);
        System.out.println("=================================================");
    }

    @Bean
    @Primary
    public org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        
        if (rabbitmqUrl != null && !rabbitmqUrl.isEmpty()) {
            // Usar URL de CloudAMQP
            factory.setUri(rabbitmqUrl);
            System.out.println("Conectando a CloudAMQP");
        } else {
            // Fallback a localhost (para desarrollo)
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            System.out.println("⚠️ Conectando a localhost");
        }
        
        return factory;
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
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}