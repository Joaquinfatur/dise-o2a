package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.model.PdI.PdI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageProcessingService {

    @Autowired
    private ServicesClient servicesClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?" +
        "\\.(jpg|jpeg|png|gif|bmp|webp)", 
        Pattern.CASE_INSENSITIVE
    );

    public void procesarPdICompleta(PdI pdi) {
        System.out.println("=== PROCESANDO PDI COMPLETA ===");
        System.out.println("PDI ID: " + pdi.getId());
        System.out.println("Contenido: " + pdi.getContenido());
        
        
        String imagenUrl = extraerImagenUrl(pdi);
        
        if (imagenUrl != null) {
            System.out.println("URL de imagen encontrada: " + imagenUrl);
            pdi.setImagenUrl(imagenUrl);
            
            
            procesarImagenAsincrona(pdi, imagenUrl);
        } else {
            System.out.println("No se encontró URL de imagen, procesando solo texto");
            
            agregarEtiquetasPorContenido(pdi);
        }
        
        
        pdi.setProcesado(true);
    }

    private String extraerImagenUrl(PdI pdi) {
        if (pdi.getImagenUrl() != null && !pdi.getImagenUrl().trim().isEmpty()) {
            return pdi.getImagenUrl(); 
        }
        
        if (pdi.getContenido() == null) {
            return null;
        }
        
        Matcher matcher = IMAGE_URL_PATTERN.matcher(pdi.getContenido());
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }

    private void procesarImagenAsincrona(PdI pdi, String imagenUrl) {
        
        CompletableFuture<String> ocrFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String resultado = servicesClient.procesarOCR(imagenUrl);
                System.out.println("OCR completado para PDI " + pdi.getId());
                return resultado;
            } catch (Exception e) {
                System.err.println("Error en OCR: " + e.getMessage());
                return "Error OCR: " + e.getMessage();
            }
        });

        CompletableFuture<String> etiquetadoFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String resultado = servicesClient.procesarEtiquetado(imagenUrl);
                System.out.println("Etiquetado completado para PDI " + pdi.getId());
                return resultado;
            } catch (Exception e) {
                System.err.println("Error en etiquetado: " + e.getMessage());
                return "Error etiquetado: " + e.getMessage();
            }
        });

        
        CompletableFuture.allOf(ocrFuture, etiquetadoFuture).thenRun(() -> {
            try {
                String ocrResultado = ocrFuture.get();
                String etiquetadoResultado = etiquetadoFuture.get();
                
                
                pdi.setOcrResultado(ocrResultado);
                pdi.setEtiquetadoResultado(etiquetadoResultado);
                
                
                List<String> etiquetasAutomaticas = generarEtiquetasAutomaticas(ocrResultado, etiquetadoResultado);
                pdi.etiquetarNuevo(etiquetasAutomaticas);
                
                System.out.println("Procesamiento de imagen completado para PDI " + pdi.getId());
                System.out.println("Etiquetas generadas: " + etiquetasAutomaticas);
                
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error procesando resultados de imagen: " + e.getMessage());
            }
        });
    }

    private List<String> generarEtiquetasAutomaticas(String ocrResultado, String etiquetadoResultado) {
        List<String> etiquetas = new ArrayList<>();
        
        
        if (ocrResultado != null && !ocrResultado.contains("Error")) {
            etiquetas.addAll(procesarResultadoOCR(ocrResultado));
        }
        
        
        if (etiquetadoResultado != null && !etiquetadoResultado.contains("Error")) {
            etiquetas.addAll(procesarResultadoEtiquetado(etiquetadoResultado));
        }
        
        
        etiquetas.add("ConImagen");
        etiquetas.add("ProcesadoIA");
        
        return etiquetas;
    }

    private List<String> procesarResultadoOCR(String ocrJson) {
        List<String> etiquetas = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(ocrJson);
            
            
            if (root.has("ParsedResults") && root.get("ParsedResults").isArray()) {
                for (JsonNode result : root.get("ParsedResults")) {
                    if (result.has("ParsedText")) {
                        String texto = result.get("ParsedText").asText();
                        
                        if (texto != null && !texto.trim().isEmpty()) {
                            etiquetas.add("ConTexto");
                            
                            // Análisis básico del texto
                            String textoLower = texto.toLowerCase();
                            if (textoLower.contains("fecha") || textoLower.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*")) {
                                etiquetas.add("ConFecha");
                            }
                            if (textoLower.matches(".*\\b\\d+\\b.*")) {
                                etiquetas.add("ConNumeros");
                            }
                            if (textoLower.contains("nombre") || textoLower.contains("apellido")) {
                                etiquetas.add("ConNombres");
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando resultado OCR: " + e.getMessage());
            etiquetas.add("ErrorOCR");
        }
        
        return etiquetas;
    }

    private List<String> procesarResultadoEtiquetado(String etiquetadoJson) {
        List<String> etiquetas = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(etiquetadoJson);
            
            
            if (root.has("labels") && root.get("labels").isArray()) {
                for (JsonNode label : root.get("labels")) {
                    if (label.has("name")) {
                        String nombreEtiqueta = label.get("name").asText();
                        double confianza = label.has("confidence") ? label.get("confidence").asDouble() : 0.0;
                        
                        
                        if (confianza >= 0.5) {
                            etiquetas.add("IA_" + nombreEtiqueta.replaceAll("\\s+", "_"));
                        }
                    }
                }
            }
            
            
            if (etiquetas.isEmpty()) {
                etiquetas.add("ImagenAnalizada");
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando resultado etiquetado: " + e.getMessage());
            etiquetas.add("ErrorEtiquetado");
        }
        
        return etiquetas;
    }

    private void agregarEtiquetasPorContenido(PdI pdi) {
        List<String> etiquetas = new ArrayList<>();
        
        String contenido = pdi.getContenido();
        if (contenido != null) {
            String contenidoLower = contenido.toLowerCase();
            
            
            etiquetas.add("SoloTexto");
            
            if (contenido.length() > 100) {
                etiquetas.add("TextoLargo");
            } else if (contenido.length() < 20) {
                etiquetas.add("TextoCorto");
            }
            
            
            if (contenidoLower.contains("urgente") || contenidoLower.contains("importante")) {
                etiquetas.add("Prioritario");
            }
            
            if (contenidoLower.contains("error") || contenidoLower.contains("problema")) {
                etiquetas.add("Problema");
            }
            
            if (contenidoLower.matches(".*\\b\\d+\\b.*")) {
                etiquetas.add("ConDatos");
            }
        }
        
        etiquetas.add("ProcesadoTexto");
        pdi.etiquetarNuevo(etiquetas);
    }
}