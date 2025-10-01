package ar.edu.utn.dds.k3003.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pdis")
public class PdIEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "hecho_id")
    private Integer hechoId; 
    
    @Column(columnDefinition = "TEXT")
    private String contenido;

    
    @Column(name = "ubicacion")
    private String ubicacion;
    
    @Column(name = "fecha")
    private LocalDateTime fecha;
    
    @Column(name = "usuario_id")
    private String usuarioId;
    
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pdi_etiquetas_deprecated", joinColumns = @JoinColumn(name = "pdi_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetas = new ArrayList<>();

    // NUEVOS CAMPOS PARA ENTREGA 4
    @Column(name = "imagen_url")
    private String imagenUrl;
    
    @Column(name = "ocr_resultado", columnDefinition = "TEXT")
    private String ocrResultado;
    
    @Column(name = "etiquetado_resultado", columnDefinition = "TEXT") 
    private String etiquetadoResultado;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pdi_etiquetas_nuevas", joinColumns = @JoinColumn(name = "pdi_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetasNuevas = new ArrayList<>();

    @Column(name = "procesado")
    private Boolean procesado = false;

    
    public PdIEntity() {}

    public PdIEntity(Integer hechoId, String contenido, List<String> etiquetas) {
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.etiquetas = etiquetas != null ? new ArrayList<>(etiquetas) : new ArrayList<>();
        this.etiquetasNuevas = new ArrayList<>();
        this.procesado = false;
    }

    public PdIEntity(Integer id, Integer hechoId, String contenido, List<String> etiquetas) {
        this.id = id;
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.etiquetas = etiquetas != null ? new ArrayList<>(etiquetas) : new ArrayList<>();
        this.etiquetasNuevas = new ArrayList<>();
        this.procesado = false;
    }

    
    public PdIEntity(Integer hechoId, String contenido, String imagenUrl, List<String> etiquetasNuevas) {
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.imagenUrl = imagenUrl;
        this.etiquetasNuevas = etiquetasNuevas != null ? new ArrayList<>(etiquetasNuevas) : new ArrayList<>();
        this.etiquetas = new ArrayList<>();
        this.procesado = false;
    }

    
    public Integer getId() { 
        return id; 
    }
    
    public Integer getHechoId() { 
        return hechoId; 
    }
    
    public String getContenido() { 
        return contenido; 
    }

    
    public String getUbicacion() {
        return ubicacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getUsuarioId() {
        return usuarioId;
    }
    
    @Deprecated
    public List<String> getEtiquetas() { 
        return etiquetas != null ? etiquetas : new ArrayList<>(); 
    }

    
    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getOcrResultado() {
        return ocrResultado;
    }

    public String getEtiquetadoResultado() {
        return etiquetadoResultado;
    }

    public List<String> getEtiquetasNuevas() {
        return etiquetasNuevas != null ? etiquetasNuevas : new ArrayList<>();
    }

    public Boolean getProcesado() {
        return procesado;
    }

    
    public void setId(Integer id) {
        this.id = id;
    }

    public void setHechoId(Integer hechoId) {
        this.hechoId = hechoId;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Deprecated
    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas != null ? etiquetas : new ArrayList<>();
    }

    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public void setOcrResultado(String ocrResultado) {
        this.ocrResultado = ocrResultado;
    }

    public void setEtiquetadoResultado(String etiquetadoResultado) {
        this.etiquetadoResultado = etiquetadoResultado;
    }

    public void setEtiquetasNuevas(List<String> etiquetasNuevas) {
        this.etiquetasNuevas = etiquetasNuevas != null ? etiquetasNuevas : new ArrayList<>();
    }

    public void setProcesado(Boolean procesado) {
        this.procesado = procesado;
    }
}