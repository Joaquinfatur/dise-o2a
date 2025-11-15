package ar.edu.utn.dds.k3003.config;

import com.rabbitmq.client.ConnectionFactory;
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
    
    @Value("${spring.rabbitmq.host:localhost}")
    private String host;
    
    @Value("${spring.rabbitmq.port:5672}")
    private int port;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String username;
    
    @Value("${spring.rabbitmq.password:guest}")
    private String password;
    
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;
    
    @Value("${spring.rabbitmq.ssl.enabled:false}")
    private boolean sslEnabled;

    @PostConstruct
    public void debugRabbitMQConfig() {
        System.out.println("=================================================");
        System.out.println("RABBITMQ CONFIG DEBUG");
        System.out.println("=================================================");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Username: " + username);
        System.out.println("VHost: " + virtualHost);
        System.out.println("SSL Enabled: " + sslEnabled);
        System.out.println("Queue Name: " + queueName);
        System.out.println("Exchange Name: " + exchangeName);
        System.out.println("Routing Key: " + routingKey);
        System.out.println("=================================================");
    }

    @Bean
    @Primary
    public org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory() {
        com.rabbitmq.client.ConnectionFactory rabbitFactory = new com.rabbitmq.client.ConnectionFactory();
        
        rabbitFactory.setHost(host);
        rabbitFactory.setPort(port);
        rabbitFactory.setUsername(username);
        rabbitFactory.setPassword(password);
        rabbitFactory.setVirtualHost(virtualHost);
        
        if (sslEnabled) {
            try {
                rabbitFactory.useSslProtocol();
            } catch (Exception e) {
                throw new RuntimeException("Error configurando SSL para RabbitMQ", e);
            }
        }
        
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitFactory);
        
        System.out.println("ConnectionFactory configurado para: " + host + ":" + port);
        
        return connectionFactory;
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