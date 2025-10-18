package ar.edu.utn.dds.k3003.clients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class ServicesClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${external.ocr.apikey:}")
    private String ocrApiKey;
    
    @Value("${external.labeling.apikey:}")
    private String labelingApiKey;
    
    @Value("${services.fuentes.url:http://localhost:8081}")
    private String fuentesUrl;
    
    @Value("${services.agregador.url:http://localhost:8083}")
    private String agregadorUrl;

    public ServicesClient() {
        this.restTemplate = new RestTemplate();
    }

    // Procesar OCR
    public String procesarOCR(String imagenUrl) {
        if (ocrApiKey == null || ocrApiKey.isEmpty()) {
            System.out.println("OCR API key no configurada, usando resultado simulado");
            return generarOCRSimulado();
        }
        
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("https://api.ocr.space/parse/imageurl")
                .queryParam("apikey", ocrApiKey)
                .queryParam("url", imagenUrl)
                .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "{\"error\": \"OCR falló con status: " + response.getStatusCode() + "\"}";
            }
        } catch (Exception e) {
            System.err.println("Error llamando OCR API: " + e.getMessage());
            return generarOCRSimulado();
        }
    }

    // Procesar etiquetado
    public String procesarEtiquetado(String imagenUrl) {
        if (labelingApiKey == null || labelingApiKey.isEmpty()) {
            System.out.println("Labeling API key no configurada, usando resultado simulado");
            return generarEtiquetadoSimulado();
        }
        
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("https://api.apilayer.com/image_labeling/url")
                .queryParam("url", imagenUrl)
                .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", labelingApiKey);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "{\"error\": \"Etiquetado falló con status: " + response.getStatusCode() + "\"}";
            }
        } catch (Exception e) {
            System.err.println("Error llamando Labeling API: " + e.getMessage());
            return generarEtiquetadoSimulado();
        }
    }

    // Validar si un hecho está activo
    public boolean isHechoActivoYValido(String hechoId) {
        try {
            String url = fuentesUrl + "/hecho/" + hechoId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map hecho = response.getBody();
                String estado = (String) hecho.get("estado");
                return !"borrado".equals(estado) && !"censurado".equals(estado);
            }
        } catch (Exception e) {
            System.err.println("Error verificando hecho: " + e.getMessage());
        }
        
        // Si no podemos verificar, asumimos que está activo
        return true;
    }

    // Notificar al agregador
    public void notifyAggregator(String hechoId, List<String> pdisIds) {
        try {
            String url = agregadorUrl + "/notificar-pdis";
            
            Map<String, Object> body = Map.of(
                "hechoId", hechoId,
                "pdisIds", pdisIds
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(url, entity, Void.class);
            System.out.println("Agregador notificado para hecho: " + hechoId);
        } catch (Exception e) {
            System.err.println("Error notificando agregador: " + e.getMessage());
        }
    }

    // Métodos de simulación para cuando no hay API keys
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
            {
                "labels": [
                    {"name": "persona", "confidence": 0.95},
                    {"name": "edificio", "confidence": 0.87},
                    {"name": "calle", "confidence": 0.76}
                ]
            }
            """;
    }
}