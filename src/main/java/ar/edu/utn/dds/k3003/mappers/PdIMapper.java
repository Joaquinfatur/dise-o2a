package ar.edu.utn.dds.k3003.mappers;

import java.util.List;

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
    
    
        if (pdi.getEtiquetasNuevas() != null && !pdi.getEtiquetasNuevas().isEmpty()) {
        entity.setEtiquetasNuevas(pdi.getEtiquetasNuevas());  
        entity.setEtiquetas(new java.util.ArrayList<>());
        } else {
        entity.setEtiquetas(pdi.getEtiquetas());
        entity.setEtiquetasNuevas(new java.util.ArrayList<>());
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
        
        
        pdi.setHechoId(entity.getHechoId());
        
        
        pdi.setUbicacion(entity.getUbicacion());
        pdi.setFecha(entity.getFecha());
        pdi.setUsuarioId(entity.getUsuarioId());
        
        
        pdi.setImagenUrl(entity.getImagenUrl());
        pdi.setOcrResultado(entity.getOcrResultado());
        pdi.setEtiquetadoResultado(entity.getEtiquetadoResultado());
        
        
        if (entity.getEtiquetasNuevas() != null && !entity.getEtiquetasNuevas().isEmpty()) {
            pdi.setEtiquetasNuevas(entity.getEtiquetasNuevas());
        } else if (entity.getEtiquetas() != null && !entity.getEtiquetas().isEmpty()) {
            
            pdi.etiquetarNuevo(entity.getEtiquetas());
            pdi.agregarEtiquetaAutomatica("MigradoDeDeprecated");
        }
        
        return pdi;
    }
}