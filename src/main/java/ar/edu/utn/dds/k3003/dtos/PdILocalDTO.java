package ar.edu.utn.dds.k3003.dtos;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class PdILocalDTO {
    private String id;
    
    @JsonProperty("hecho_id")  // ← AQUÍ CORRECTO
    private String hechoId;
    
    private String contenido;
    private String tipo;
    private String fuenteId;
    private String ubicacion;
    private LocalDateTime fecha;
    private String usuarioId;
    private String estadoProcesamiento;
    private String textoExtraido;
    private List<String> tags = new ArrayList<>();
    
    public PdILocalDTO() {}
    
    public PdILocalDTO(String id, String hechoId, String contenido, String tipo) {
        this.id = id;
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.tipo = tipo;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getHechoId() { return hechoId; }
    public void setHechoId(String hechoId) { this.hechoId = hechoId; }
    
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getFuenteId() { return fuenteId; }
    public void setFuenteId(String fuenteId) { this.fuenteId = fuenteId; }
    
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    
    public String getEstadoProcesamiento() { return estadoProcesamiento; }
    public void setEstadoProcesamiento(String estadoProcesamiento) { this.estadoProcesamiento = estadoProcesamiento; }
    
    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    @JsonIgnore 
    public List<String> getEtiquetas() { return tags; }
    public void setEtiquetas(List<String> etiquetas) { this.tags = etiquetas; }
    
    @Override
    public String toString() {
        return "PdILocalDTO{id='" + id + "', hechoId='" + hechoId + "', tags=" + tags + '}';
    }
}