package ar.edu.utn.dds.k3003.clients;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ServicesClient {

    private final WebClient webClient;

    @Value("${services.fuentes.url:http://localhost:8081}")
    private String fuentesUrl;

    @Value("${services.solicitudes.url:http://localhost:8082}")  
    private String solicitudesUrl;

    @Value("${services.agregador.url:http://localhost:8083}")
    private String agregadorUrl;

    @Value("${external.ocr.apikey:}")
    private String ocrApiKey;
    
    @Value("${external.labeling.apikey:}")
    private String labelingApiKey;

    
    
    public record HechoResponse(String hechoId, boolean activo) {}

    public ServicesClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }


    public boolean isHechoActivoYValido(String hechoId) {
        if (hechoId == null || hechoId.trim().isEmpty()) {
        return false;
        }
    
        try {
            Map<String, Object> hecho = webClient.get()
                .uri(fuentesUrl + "/hechos/" + hechoId)
                .retrieve()  // ← FALTABA ESTO
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(15))
                .block();
        
            if (hecho == null) {
            return false;
            }
        
            Object estado = hecho.get("estado");
            return estado != null && !"borrado".equals(estado.toString());
        
        } catch (Exception e) {
            System.err.println("Error obteniendo hecho " + hechoId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isHechoActivo(String hechoId) {
        try {
            HechoResponse response = webClient.get()
                    .uri(solicitudesUrl + "/solicitudes/hecho/{id}/activo", hechoId)
                    .retrieve()
                    .bodyToMono(HechoResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            
            if (response == null) {
                System.err.println("Respuesta nula de Solicitudes para hecho " + hechoId);
                return false;
            }
            
            System.out.println("Hecho " + hechoId + " estado: " + (response.activo() ? "ACTIVO" : "INACTIVO"));
            return response.activo();
            
        } catch (Exception e) {
            System.err.println("Error consultando estado del hecho " + hechoId + ": " + e.getMessage());
            return false;
        }
    }


    public Map<String, Object> getHecho(String hechoId) {
        try {
            return webClient.get()
                    .uri(fuentesUrl + "/hechos/" + hechoId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.err.println("Hecho " + hechoId + " no encontrado en Fuentes");
                return null;
            }
            System.err.println("Error obteniendo hecho " + hechoId + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error obteniendo hecho " + hechoId + ": " + e.getMessage());
            return null;
        }
    }

    public String procesarOCR(String imageUrl) {
        try {
            if (ocrApiKey == null || ocrApiKey.trim().isEmpty()) {
                System.err.println("OCR API Key no configurada");
                return "OCR no disponible - API Key no configurada";
            }

            String response = webClient.get()
                    .uri("https://api.ocr.space/parse/imageurl?apikey=" + ocrApiKey + "&url=" + imageUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            System.out.println("Resultado OCR obtenido para imagen: " + imageUrl);
            return response;
            
        } catch (Exception e) {
            System.err.println("Error procesando OCR para imagen " + imageUrl + ": " + e.getMessage());
            return "Error en OCR: " + e.getMessage();
        }
    }


    public String procesarEtiquetado(String imageUrl) {
        try {
            if (labelingApiKey == null || labelingApiKey.trim().isEmpty()) {
                System.err.println("Labeling API Key no configurada");
                return "Etiquetado no disponible - API Key no configurada";
            }

            String response = webClient.get()
                    .uri("https://api.apilayer.com/image_labeling/url?url=" + imageUrl)
                    .header("apiKey", labelingApiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            System.out.println("Resultado etiquetado obtenido para imagen: " + imageUrl);
            return response;
            
        } catch (Exception e) {
            System.err.println("Error procesando etiquetado para imagen " + imageUrl + ": " + e.getMessage());
            return "Error en etiquetado: " + e.getMessage();
        }
    }

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


    public Map<String, String> checkServicesHealth() {
        return Map.of(
            "fuentes", checkServiceHealth(fuentesUrl),
            "solicitudes", checkServiceHealth(solicitudesUrl ),
            "agregador", checkServiceHealth(agregadorUrl ),
            "ocr", ocrApiKey != null && !ocrApiKey.trim().isEmpty() ? "CONFIGURED" : "NOT_CONFIGURED",
            "labeling", labelingApiKey != null && !labelingApiKey.trim().isEmpty() ? "CONFIGURED" : "NOT_CONFIGURED"
        );
    }

    private String checkServiceHealth(String url) {
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            return "UP";
        } catch (Exception e) {
            return "DOWN: " + e.getMessage();
        }
    }
}