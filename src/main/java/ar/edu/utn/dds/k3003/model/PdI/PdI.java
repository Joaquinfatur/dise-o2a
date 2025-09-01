package ar.edu.utn.dds.k3003.model.PdI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PdI {
    
    private int id;
    private String contenido;
    private List<String> etiquetas;
    private int hechoId;
    private boolean procesado;
    private String ubicacion;
    private LocalDateTime fecha;
    private String usuarioId;

    public PdI(int id, String contenido) {
        this.id = id;
        this.contenido = contenido;
        this.etiquetas = new ArrayList<>();
        this.procesado = false;
    }

    public PdI(String id, String contenido) {
    this.id = Integer.parseInt(id);
    this.hechoId = 0; // Valor default
    this.contenido = contenido;
    this.etiquetas = new ArrayList<>();
    this.procesado = false;
    }

    public int getHechoId() {
        return hechoId;
    }   

    public void etiquetar(List<String> nuevasEtiquetas) {
        if (!procesado) {
            etiquetas.addAll(nuevasEtiquetas);
            procesado = true;
        }
    }

    // Agregamos los getters para id y contenido
    public int getId() {
        return id;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public List<String> getEtiquetas() { 
        return etiquetas; 
    }
    
    public boolean isProcesado() { 
        return procesado; 
    }
    public String getUbicacion() {
    return this.ubicacion;
    }

    public LocalDateTime getFecha() {
    return this.fecha;
    }

    public String getUsuarioId() {
    return this.usuarioId;
    }

}
