package ar.edu.utn.dds.k3003.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ServicesClient {

    private final WebClient fuentesClient;
    private final WebClient solicitudesClient;
    private final WebClient agregadorClient;

    @Value("${external.ocr.apikey:}")
    private String ocrApiKey;

    @Value("${external.labeling.apikey:}")
    private String labelingApiKey;

    public ServicesClient(
            @Value("${services.fuentes.url}") String fuentesUrl,
            @Value("${services.solicitudes.url}") String solicitudesUrl,
            @Value("${services.agregador.url}") String agregadorUrl) {
        
        this.fuentesClient = WebClient.builder()
            .baseUrl(fuentesUrl)
            .build();
        
        this.solicitudesClient = WebClient.builder()
            .baseUrl(solicitudesUrl)
            .build();
        
        this.agregadorClient = WebClient.builder()
            .baseUrl(agregadorUrl)
            .build();
    }

    // ============ FUENTES ============
    
    public boolean isHechoActivoYValido(String hechoId) {
        try {
            String response = fuentesClient.get()
                .uri("/hecho/{id}", hechoId)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            // Si no lanza excepción, el hecho existe
            return response != null && !response.contains("\"estado\":\"borrado\"");
            
        } catch (Exception e) {
            System.err.println("Error verificando hecho: " + e.getMessage());
            throw e;
        }
    }

    // ============ AGREGADOR ============
    
    public void notificarNuevoPDI(String pdiId) {
        try {
            agregadorClient.post()
                .uri("/pdis/{id}/notificar", pdiId)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .block();
            
            System.out.println("Agregador notificado: PDI " + pdiId);
            
        } catch (Exception e) {
            System.err.println("Error notificando agregador: " + e.getMessage());
            throw e;
        }
    }

    // ============ OCR ============
    
    public String procesarOCR(String imageUrl) {
        if (ocrApiKey == null || ocrApiKey.isEmpty()) {
            return "OCR API Key no configurada";
        }
    
        try {
            // Agregar más parámetros para mejor detección
            String url = "https://api.ocr.space/parse/imageurl" +
                     "?apikey=" + ocrApiKey +
                     "&url=" + imageUrl +
                     "&language=spa" +           // ← Español
                     "&isOverlayRequired=false" + // ← Sin overlay
                     "&detectOrientation=true" +  // ← Detectar orientación
                     "&scale=true";               // ← Escalar imagen
        
            String response = WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
        
            // Verificar si hubo error
            if (response != null && response.contains("\"IsErroredOnProcessing\":true")) {
                System.err.println("OCR Error Response: " + response);
                return "Error OCR: Imagen no válida o formato no soportado";
            }
        
            return response != null ? response : "Sin resultado OCR";
        
        } catch (Exception e) {
            System.err.println("Error en OCR: " + e.getMessage());
            return "Error OCR: " + e.getMessage();
        }
    }

    // ============ LABELING ============
    
    public String procesarLabeling(String imageUrl) {
        if (labelingApiKey == null || labelingApiKey.isEmpty()) {
            return "Labeling API Key no configurada";
        }
        
        try {
            String url = "https://api.apilayer.com/image_labeling/url?url=" + imageUrl;
            
            String response = WebClient.create()
                .get()
                .uri(url)
                .header("apikey", labelingApiKey)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            return response != null ? response : "Sin resultado Labeling";
            
        } catch (Exception e) {
            System.err.println("Error en Labeling: " + e.getMessage());
            throw e;
        }
    }
}