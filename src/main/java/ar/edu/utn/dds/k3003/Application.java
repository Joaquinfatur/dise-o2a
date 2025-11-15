package ar.edu.utn.dds.k3003;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})  // ← AGREGAR ESTO
public class Application {
    
    @Autowired
    private DataSource dataSource;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @PostConstruct  
    public void verifyDatabase() {
        try {
            String url = dataSource.getConnection().getMetaData().getURL();
            System.out.println("========================================");
            System.out.println(" CONECTADO A: " + url);
            System.out.println("========================================");
            
            // DEBUG: Verificar variables de RabbitMQ
            String rabbitmqEnabled = System.getenv("RABBITMQ_ENABLED");
            String rabbitmqUrl = System.getenv("RABBITMQ_URL");
            System.out.println("========================================");
            System.out.println(" RABBITMQ_ENABLED: " + rabbitmqEnabled);
            System.out.println(" RABBITMQ_URL: " + (rabbitmqUrl != null ? "NULL" : "NULL"));
            System.out.println("========================================");
        } catch (Exception e) {
            System.err.println(" ERROR CONECTANDO DB: " + e.getMessage());
        }
    }
}