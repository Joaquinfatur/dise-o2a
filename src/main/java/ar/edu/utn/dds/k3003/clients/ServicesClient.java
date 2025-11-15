package ar.edu.utn.dds.k3003.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
                .timeout(Duration.ofSeconds(5))
                .block();
            
            // Si no lanza excepción, el hecho existe
            return response != null && !response.contains("\"estado\":\"borrado\"");
            
        } catch (Exception e) {
            System.err.println("Error verificando hecho: " + e.getMessage());
            // En caso de error, permitir continuar
            return true;
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
            // No lanzar excepción, solo loguear
        }
    }

    // ============ OCR ============
    
    public String procesarOCR(String imagenUrl) {
        if (ocrApiKey == null || ocrApiKey.isEmpty()) {
            System.out.println("OCR API key no configurada, usando resultado simulado");
            return generarOCRSimulado();
        }
        
        try {
            URI uri = UriComponentsBuilder
            .fromHttpUrl("https://api.ocr.space/parse/imageurl")
            .queryParam("apikey", ocrApiKey)
            .queryParam("url", imagenUrl)
            .encode()  
            .build()
            .toUri();   
            String response = WebClient.create()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            return response != null ? response : "{\"error\": \"Sin respuesta del OCR\"}";
            
        } catch (Exception e) {
            System.err.println("Error llamando OCR API: " + e.getMessage());
            return generarOCRSimulado();
        }
    }

    // ============ ETIQUETADO ============
    
    public String procesarEtiquetado(String imagenUrl) {
        if (labelingApiKey == null || labelingApiKey.isEmpty()) {
            System.out.println("Labeling API key no configurada, usando resultado simulado");
            return generarEtiquetadoSimulado();
        }
        
        try {
            // Encodear la URL de la imagen
            URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.apilayer.com/image_labeling/url")
                .queryParam("url", imagenUrl)
                .build(true)
                .toUri();
            
            String response = WebClient.create()
                .get()
                .uri(uri)
                .header("apikey", labelingApiKey)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            return response != null ? response : "{\"error\": \"Sin resultado Labeling\"}";
            
        } catch (Exception e) {
            System.err.println("Error en Labeling: " + e.getMessage());
            return generarEtiquetadoSimulado();
        }
    }

    // ============ MÉTODOS DE SIMULACIÓN ============
    
    private String generarOCRSimulado() {
        return """
            {
                "ParsedResults": [{
                    "ParsedText": "Texto simulado del OCR",
                    "ErrorMessage": "",
                    "ErrorDetails": ""
                }],
                "OCRExitCode": 1,
                "IsErroredOnProcessing": false
            }
            """;
    }

    private String generarEtiquetadoSimulado() {
        return """
            [
                {"label": "persona", "confidence": 0.95},
                {"label": "edificio", "confidence": 0.87},
                {"label": "calle", "confidence": 0.76}
            ]
            """;
    }
}