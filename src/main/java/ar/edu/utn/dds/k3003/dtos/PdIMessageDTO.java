package ar.edu.utn.dds.k3003.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO para mensajes de RabbitMQ que solicitan procesamiento de un PDI
 */
public class PdIMessageDTO {
    
    @JsonProperty("hecho_id")
    private String hechoId;
    
    @JsonProperty("contenido")
    private String contenido;
    
    @JsonProperty("ubicacion")
    private String ubicacion;
    
    @JsonProperty("usuario_id")
    private String usuarioId;
    
    @JsonProperty("fecha")
    private LocalDateTime fecha;
    
    // Constructor vacío para Jackson
    public PdIMessageDTO() {
    }
    
    // Constructor completo
    public PdIMessageDTO(String hechoId, String contenido, String ubicacion, String usuarioId) {
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.ubicacion = ubicacion;
        this.usuarioId = usuarioId;
        this.fecha = LocalDateTime.now();
    }
    
    // Getters y Setters
    public String getHechoId() {
        return hechoId;
    }
    
    public void setHechoId(String hechoId) {
        this.hechoId = hechoId;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public String getUbicacion() {
        return ubicacion;
    }
    
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    public String getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    @Override
    public String toString() {
        return "PdIMessageDTO{" +
                "hechoId='" + hechoId + '\'' +
                ", contenido='" + contenido + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", usuarioId='" + usuarioId + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}