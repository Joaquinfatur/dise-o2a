package ar.edu.utn.dds.k3003.mappers;

import java.util.List;

import ar.edu.utn.dds.k3003.model.PdI.PdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;

public class PdIMapper {
    public static PdIEntity toEntity(PdI pdi) {
        PdIEntity entity = new PdIEntity();
        // No setear ID porque es auto-generado
        entity.setHechoId(pdi.getHechoId());
        entity.setContenido(pdi.getContenido());
        entity.setEtiquetas(pdi.getEtiquetas());
        return entity;
    }

    public static PdI toModel(PdIEntity entity) {
        PdI pdi = new PdI(entity.getId(), entity.getContenido());
        pdi.setHechoId(entity.getHechoId());
        
        if (entity.getEtiquetas() != null && !entity.getEtiquetas().isEmpty()) {
            pdi.etiquetar(entity.getEtiquetas());
        }
        return pdi;
    }
}