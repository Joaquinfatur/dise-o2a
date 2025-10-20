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
    private String hechoId;
    
    @Column(columnDefinition = "TEXT")
    private String contenido;
    
    private String ubicacion;
    
    private LocalDateTime fecha;
    
    @Column(name = "usuario_id")
    private String usuarioId;
    
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
    
    @Column(name = "ocr_resultado", columnDefinition = "TEXT")
    private String ocrResultado;
    
    @Column(name = "etiquetado_resultado", columnDefinition = "TEXT")
    private String etiquetadoResultado;
    
    private Boolean procesado = false;
    
    @ElementCollection
    @CollectionTable(
        name = "pdi_etiquetas_nuevas",
        joinColumns = @JoinColumn(name = "pdi_id")
    )
    @Column(name = "etiqueta")
    private List<String> etiquetasNuevas = new ArrayList<>();

    // Constructor vacío requerido por JPA
    public PdIEntity() {
    }

    // Constructor con datos básicos
    public PdIEntity(String hechoId, String contenido) {
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.fecha = LocalDateTime.now();
        this.procesado = false;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getOcrResultado() {
        return ocrResultado;
    }

    public void setOcrResultado(String ocrResultado) {
        this.ocrResultado = ocrResultado;
    }

    public String getEtiquetadoResultado() {
        return etiquetadoResultado;
    }

    public void setEtiquetadoResultado(String etiquetadoResultado) {
        this.etiquetadoResultado = etiquetadoResultado;
    }

    public Boolean getProcesado() {
        return procesado;
    }

    public void setProcesado(Boolean procesado) {
        this.procesado = procesado;
    }

    public List<String> getEtiquetasNuevas() {
        return etiquetasNuevas;
    }

    public void setEtiquetasNuevas(List<String> etiquetasNuevas) {
        this.etiquetasNuevas = etiquetasNuevas;
    }
    
    public void agregarEtiqueta(String etiqueta) {
        if (this.etiquetasNuevas == null) {
            this.etiquetasNuevas = new ArrayList<>();
        }
        if (!this.etiquetasNuevas.contains(etiqueta)) {
            this.etiquetasNuevas.add(etiqueta);
        }
    }
}