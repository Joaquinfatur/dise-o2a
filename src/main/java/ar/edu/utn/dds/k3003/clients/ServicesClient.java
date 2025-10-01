package ar.edu.utn.dds.k3003.clients;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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

    public ServicesClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * Valida si el hecho existe y está activo
     * Retorna true si el hecho está activo (sin solicitudes de eliminación)
     */
    public boolean isHechoActivoYValido(String hechoId) {
        try {
            
            Map<String, Object> hecho = getHecho(hechoId);
            if (hecho == null) {
                System.err.println("Hecho " + hechoId + " no existe en Fuentes");
                return false;
            }

            
            boolean tieneHecho = isHechoActivo(hechoId);
            if (!tieneHecho) {
                System.err.println("Hecho " + hechoId + " tiene solicitudes de eliminación");
                return false;
            }

            System.out.println("Hecho " + hechoId + " es válido y activo");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error validando hecho " + hechoId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el servicio de Solicitudes está disponible
     */
    public boolean isSolicitudesServiceAvailable() {
        try {
            String response = webClient.get()
                    .uri(solicitudesUrl + "/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            
            return response != null;
        } catch (Exception e) {
            System.err.println("Servicio de Solicitudes no disponible: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica estado del hecho en solicitudes
     */
    public boolean isHechoActivo(String hechoId) {
        try {
            
            if (!isSolicitudesServiceAvailable()) {
                System.err.println("Servicio de Solicitudes no disponible - rechazando PDI");
                return false;
            }

            String response = webClient.get()
                    .uri(solicitudesUrl + "/solicitudes?hecho=" + hechoId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            
            
            boolean activo = response == null || response.equals("[]");
            System.out.println("Hecho " + hechoId + " estado: " + (activo ? "ACTIVO" : "INACTIVO"));
            return activo;
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.err.println("Hecho " + hechoId + " no encontrado en solicitudes");
                return true; 
            }
            System.err.println("Error consultando estado del hecho " + hechoId + ": " + e.getMessage());
            return false; 
        } catch (Exception e) {
            System.err.println("Error consultando estado del hecho " + hechoId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     *Obtiene información del hecho desde Fuentes
     */
    public Map<String, Object> getHecho(String hechoId) {
        try {
            return webClient.get()
                    .uri(fuentesUrl + "/hecho/" + hechoId)
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


    /**
     * Procesa imagen con OCR
     */
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

    /**
     *Procesa imagen con etiquetador
     */
    public String procesarEtiquetado(String imageUrl) {
        try {
            if (labelingApiKey == null || labelingApiKey.trim().isEmpty()) {
                System.err.println("Labeling API Key no configurada");
                return "Etiquetado no disponible - API Key no configurada";
            }

            String response = webClient.get()
                    .uri("https://api.apilayer.com/image_labeling/url?url=" + imageUrl)
                    .header("apikey", labelingApiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
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

    /**
     * Health check mejorado
     */
    public Map<String, String> checkServicesHealth() {
        return Map.of(
            "fuentes", checkServiceHealth(fuentesUrl + "/health"),
            "solicitudes", checkServiceHealth(solicitudesUrl + "/health"),
            "agregador", checkServiceHealth(agregadorUrl + "/health"),
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