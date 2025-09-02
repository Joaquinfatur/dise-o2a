package ar.edu.utn.dds.k3003;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "ar.edu.utn.dds.k3003")
@EnableJpaRepositories(basePackages = "ar.edu.utn.dds.k3003.persistence")
@EntityScan(basePackages = "ar.edu.utn.dds.k3003.persistence")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}