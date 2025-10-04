package ar.edu.utn.dds.k3003.model.PdI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class PdI {

    @Id
    private int id;
    private String contenido;
    private List<String> etiquetas; 
    private String hechoId; // ← CAMBIO: de Integer a String
    private boolean procesado;
    private String ubicacion;
    private LocalDateTime fecha;
    private String usuarioId;
    
    private String imagenUrl;
    private String ocrResultado;
    private String etiquetadoResultado;
    private List<String> etiquetasNuevas; 

    public PdI(int id, String contenido) {
        this.id = id;
        this.contenido = contenido;
        this.etiquetas = new ArrayList<>();
        this.etiquetasNuevas = new ArrayList<>();
        this.procesado = false;
        this.hechoId = null;
        this.imagenUrl = null;
        this.ocrResultado = null;
        this.etiquetadoResultado = null;
    }

    public PdI(int id, String contenido, String hechoId) { // ← CAMBIO: parámetro String
        this.id = id;
        this.contenido = contenido;
        this.etiquetas = new ArrayList<>();
        this.etiquetasNuevas = new ArrayList<>();
        this.procesado = false;
        this.hechoId = hechoId;
        this.imagenUrl = null;
        this.ocrResultado = null;
        this.etiquetadoResultado = null;
    }

    public PdI(int id, String contenido, String hechoId, String imagenUrl) { // ← CAMBIO: parámetro String
        this.id = id;
        this.contenido = contenido;
        this.etiquetas = new ArrayList<>();
        this.etiquetasNuevas = new ArrayList<>();
        this.procesado = false;
        this.hechoId = hechoId;
        this.imagenUrl = imagenUrl;
        this.ocrResultado = null;
        this.etiquetadoResultado = null;
    }
    
    @Deprecated
    public void etiquetar(List<String> nuevasEtiquetas) {
        if (!procesado && nuevasEtiquetas != null) {
            etiquetas.addAll(nuevasEtiquetas);
            procesado = true;
        }
    }

    public void etiquetarNuevo(List<String> nuevasEtiquetas) {
        if (nuevasEtiquetas != null) {
            if (this.etiquetasNuevas == null) {
                this.etiquetasNuevas = new ArrayList<>();
            }
            this.etiquetasNuevas.addAll(nuevasEtiquetas);
            this.procesado = true;
        }
    }

    public void setOcrResultado(String ocrResultado) {
        this.ocrResultado = ocrResultado;
    }

    public void setEtiquetadoResultado(String etiquetadoResultado) {
        this.etiquetadoResultado = etiquetadoResultado;
    }

    public void agregarEtiquetaAutomatica(String etiqueta) {
        if (this.etiquetasNuevas == null) {
            this.etiquetasNuevas = new ArrayList<>();
        }
        if (!this.etiquetasNuevas.contains(etiqueta)) {
            this.etiquetasNuevas.add(etiqueta);
        }
    }

    public void setProcesado(boolean procesado) {
        this.procesado = procesado;
    }

    public Integer getId() { return id; }
    public String getContenido() { return contenido; }
    
    @Deprecated
    public List<String> getEtiquetas() { 
        return etiquetas != null ? new ArrayList<>(etiquetas) : new ArrayList<>(); 
    }
    
    public boolean isProcesado() { return procesado; }
    public String getHechoId() { return hechoId; } // ← CAMBIO: retorna String
    public String getUbicacion() { return this.ubicacion; }
    public LocalDateTime getFecha() { return this.fecha; }
    public String getUsuarioId() { return this.usuarioId; }

    public String getImagenUrl() { return imagenUrl; }
    public String getOcrResultado() { return ocrResultado; }
    public String getEtiquetadoResultado() { return etiquetadoResultado; }
    public List<String> getEtiquetasNuevas() { 
        return etiquetasNuevas != null ? new ArrayList<>(etiquetasNuevas) : new ArrayList<>(); 
    }

    public void setHechoId(String hechoId) { this.hechoId = hechoId; } // ← CAMBIO: parámetro String
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public void setEtiquetasNuevas(List<String> etiquetasNuevas) { this.etiquetasNuevas = etiquetasNuevas; }
}