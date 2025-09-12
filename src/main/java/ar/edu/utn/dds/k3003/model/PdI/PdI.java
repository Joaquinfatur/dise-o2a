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
    private int hechoId;
    private boolean procesado;
    private String ubicacion;
    private LocalDateTime fecha;
    private String usuarioId;

    public PdI(int id, String contenido, int hechoId) {
        this.id = id;
        this.contenido = contenido;
        this.etiquetas = new ArrayList<>();
        this.procesado = false;
        this.hechoId = hechoId;
    }

    public PdI(String id, String contenido, int hechoId) {
        this.id = Integer.parseInt(id);
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.etiquetas = new ArrayList<>();
        this.procesado = false;
    }

    public void etiquetar(List<String> nuevasEtiquetas) {
        if (!procesado && nuevasEtiquetas != null) {
            etiquetas.addAll(nuevasEtiquetas);
            procesado = true;
        }
    }

    // Geters
    public int getId() { return id; }
    public String getContenido() { return contenido; }
    public List<String> getEtiquetas() { return new ArrayList<>(etiquetas); }
    public boolean isProcesado() { return procesado; }
    public int getHechoId() { return hechoId; }
    public String getUbicacion() { return this.ubicacion; }
    public LocalDateTime getFecha() { return this.fecha; }
    public String getUsuarioId() { return this.usuarioId; }

    public void setHechoId(int hechoId) { this.hechoId = hechoId; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}