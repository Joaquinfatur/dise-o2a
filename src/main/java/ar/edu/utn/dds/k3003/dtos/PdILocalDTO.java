package ar.edu.utn.dds.k3003.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class PdILocalDTO {
    private String id;
    private String hechoId;
    private String contenido;
    private String ubicacion;
    private LocalDateTime fecha;
    private String usuarioId;
    private List<String> etiquetas;
    private String imagenUrl;
    private String ocrResultado;
    private String etiquetadoResultado;
    private boolean procesado;
    
    // Constructor vacío
    public PdILocalDTO() {
    }
    
    // Constructor simple
    public PdILocalDTO(String id, String hechoId, String contenido) {
        this.id = id;
        this.hechoId = hechoId;
        this.contenido = contenido;
    }
    
    // Constructor completo
    public PdILocalDTO(String id, String hechoId, String contenido, 
                       String ubicacion, LocalDateTime fecha, String usuarioId,
                       List<String> etiquetas) {
        this.id = id;
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.etiquetas = etiquetas;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getHechoId() { return hechoId; }
    public void setHechoId(String hechoId) { this.hechoId = hechoId; }
    
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    
    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }
    
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    public String getOcrResultado() { return ocrResultado; }
    public void setOcrResultado(String ocrResultado) { this.ocrResultado = ocrResultado; }
    
    public String getEtiquetadoResultado() { return etiquetadoResultado; }
    public void setEtiquetadoResultado(String etiquetadoResultado) { this.etiquetadoResultado = etiquetadoResultado; }
    
    public boolean isProcesado() { return procesado; }
    public void setProcesado(boolean procesado) { this.procesado = procesado; }
}