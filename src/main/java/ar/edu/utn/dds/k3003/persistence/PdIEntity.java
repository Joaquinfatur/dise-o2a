package ar.edu.utn.dds.k3003.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@NoArgsConstructor
@Getter
@Setter
@Entity
@AllArgsConstructor
public class PdIEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int hechoId;
    private String contenido;
    @ElementCollection
    private List<String> etiquetas;

    public int getId() { return id; }
    public String getContenido() { return contenido; }
    public List<String> getEtiquetas() { return etiquetas; }

    public void setEtiquetas(List<String> etiquetas) {
    this.etiquetas = etiquetas;
    }
    public void setId(int id) {
    this.id = id;
    }

    public void setHechoId(int hechoId) {
    this.hechoId = hechoId;
    }

    public void setContenido(String contenido) {
    this.contenido = contenido;
    }
}   
