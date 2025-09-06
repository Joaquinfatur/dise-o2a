package ar.edu.utn.dds.k3003.clients;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ServicesClient {

    private final WebClient webClient;

    @Value("${services.fuentes.url:http://localhost:8081}")
    private String fuentesUrl;

    @Value("${services.solicitudes.url:http://localhost:8082}")
    private String solicitudesUrl;

    @Value("${services.agregador.url:http://localhost:8083}")
    private String agregadorUrl;

    public ServicesClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * Verifica si un hecho está activo consultando al servicio de Solicitudes
     */
    public boolean isHechoActivo(String hechoId) {
        try {
            String response = webClient.get()
                    .uri(solicitudesUrl + "/solicitudes?hecho=" + hechoId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            // Si no hay solicitudes de eliminación para este hecho, está activo
            return response == null || response.equals("[]");
        } catch (Exception e) {
            // En caso de error, asumir que está activo (fail-safe)
            System.err.println("Error consultando estado del hecho " + hechoId + ": " + e.getMessage());
            return true;
        }
    }

    /**
     * Obtiene información de un hecho desde el servicio de Fuentes
     */
    public Map<String, Object> getHecho(String hechoId) {
        try {
            return webClient.get()
                    .uri(fuentesUrl + "/hecho/" + hechoId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            System.err.println("Error obteniendo hecho " + hechoId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Notifica al Agregador que hay nuevas PdIs disponibles
     */
    public void notifyAggregator(String hechoId, List<String> pdiIds) {
        try {
            Map<String, Object> notification = Map.of(
                "hechoId", hechoId,
                "pdiIds", pdiIds,
                "source", "procesador-pdi"
            );

            webClient.post()
                    .uri(agregadorUrl + "/notifications/pdis")
                    .bodyValue(notification)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .subscribe(
                        result -> System.out.println("Agregador notificado exitosamente"),
                        error -> System.err.println("Error notificando agregador: " + error.getMessage())
                    );
        } catch (Exception e) {
            System.err.println("Error enviando notificación al agregador: " + e.getMessage());
        }
    }

    /**
     * Health check para verificar conectividad con otros servicios
     */
    public Map<String, String> checkServicesHealth() {
        return Map.of(
            "fuentes", checkServiceHealth(fuentesUrl + "/health"),
            "solicitudes", checkServiceHealth(solicitudesUrl + "/health"),
            "agregador", checkServiceHealth(agregadorUrl + "/health")
        );
    }

    private String checkServiceHealth(String url) {
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            return "UP";
        } catch (Exception e) {
            return "DOWN: " + e.getMessage();
        }
    }
}