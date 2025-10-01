package ar.edu.utn.dds.k3003;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CommandLineRunner verifyDatabase(javax.sql.DataSource dataSource) {
        return args -> {
            try {
                String url = dataSource.getConnection().getMetaData().getURL();
                System.out.println("========================================");
                System.out.println(" CONECTADO A: " + url);
                System.out.println("========================================");
            } catch (Exception e) {
                System.err.println(" ERROR CONECTANDO DB: " + e.getMessage());
            }
        };
    }
    
}