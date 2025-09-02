package ar.edu.utn.dds.k3003.persistence;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


public class PdIEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "hecho_id")
    private Integer hechoId;
    
    @Column(columnDefinition = "TEXT")
    private String contenido;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pdi_etiquetas", joinColumns = @JoinColumn(name = "pdi_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetas = new ArrayList<>();

    // Constructores
    public PdIEntity() {}

    public PdIEntity(Integer hechoId, String contenido, List<String> etiquetas) {
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.etiquetas = etiquetas != null ? new ArrayList<>(etiquetas) : new ArrayList<>();
    }

    public PdIEntity(Integer id, Integer hechoId, String contenido, List<String> etiquetas) {
        this.id = id;
        this.hechoId = hechoId;
        this.contenido = contenido;
        this.etiquetas = etiquetas != null ? new ArrayList<>(etiquetas) : new ArrayList<>();
    }

    // Getters
    public Integer getId() { 
        return id; 
    }
    
    public Integer getHechoId() { 
        return hechoId; 
    }
    
    public String getContenido() { 
        return contenido; 
    }
    
    public List<String> getEtiquetas() { 
        return etiquetas != null ? etiquetas : new ArrayList<>(); 
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setHechoId(Integer hechoId) {
        this.hechoId = hechoId;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas != null ? etiquetas : new ArrayList<>();
    }
}