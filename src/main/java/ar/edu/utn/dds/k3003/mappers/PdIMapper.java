package ar.edu.utn.dds.k3003.mappers;

import java.util.List;
import java.util.ArrayList;

import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;

public class PdIMapper {
    
    /**
     * Convierte PdI a PdIEntity 
     */
    public static PdIEntity toEntity(PdI pdi) {
        PdIEntity entity = new PdIEntity();
    
        entity.setHechoId(pdi.getHechoId()); 
        entity.setContenido(pdi.getContenido());
        entity.setProcesado(pdi.isProcesado());
    
        // Solo usar etiquetasNuevas
        if (pdi.getEtiquetasNuevas() != null && !pdi.getEtiquetasNuevas().isEmpty()) {
            entity.setEtiquetasNuevas(pdi.getEtiquetasNuevas());
        } else if (pdi.getEtiquetas() != null && !pdi.getEtiquetas().isEmpty()) {
            // Si solo tiene etiquetas viejas, migrarlas a las nuevas
            entity.setEtiquetasNuevas(new ArrayList<>(pdi.getEtiquetas()));
        } else {
            entity.setEtiquetasNuevas(new ArrayList<>());
        }
    
        // Campos de imagen
        entity.setImagenUrl(pdi.getImagenUrl());
        entity.setOcrResultado(pdi.getOcrResultado());
        entity.setEtiquetadoResultado(pdi.getEtiquetadoResultado());
    
        return entity;
    }

    /**
     * Convierte PdIEntity a PdI 
     */
    public static PdI toModel(PdIEntity entity) {
        
        PdI pdi = new PdI(entity.getId(), entity.getContenido());
        
        // Establecer campos básicos
        pdi.setHechoId(entity.getHechoId());
        pdi.setUbicacion(entity.getUbicacion());
        pdi.setFecha(entity.getFecha());
        pdi.setUsuarioId(entity.getUsuarioId());
        
        // Campos de imagen
        pdi.setImagenUrl(entity.getImagenUrl());
        pdi.setOcrResultado(entity.getOcrResultado());
        pdi.setEtiquetadoResultado(entity.getEtiquetadoResultado());
        
        // Solo usar etiquetasNuevas
        if (entity.getEtiquetasNuevas() != null && !entity.getEtiquetasNuevas().isEmpty()) {
            pdi.setEtiquetasNuevas(entity.getEtiquetasNuevas());
        } else {
            pdi.setEtiquetasNuevas(new ArrayList<>());
        }
        
        return pdi;
    }
}