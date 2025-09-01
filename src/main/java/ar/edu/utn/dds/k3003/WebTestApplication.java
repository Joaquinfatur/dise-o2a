package ar.edu.utn.dds.k3003;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class WebTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebTestApplication.class, args);
  }

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }
}