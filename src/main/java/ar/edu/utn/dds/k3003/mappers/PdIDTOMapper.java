package ar.edu.utn.dds.k3003.mappers;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;

public class PdIDTOMapper {
    
    public static PdIDTO toFacadesDto(PdILocalDTO local) {
        return new PdIDTO(
            local.getId(),
            local.getHechoId(),
            local.getContenido(),
            null,  
            null, 
            null,  
            local.getTags() != null ? local.getTags() : new java.util.ArrayList<>()  // ✅ Usar getTags() en vez de getEtiquetas()
        );
    }
    
    public static PdILocalDTO toLocalDto(PdIDTO facades) {
        PdILocalDTO local = new PdILocalDTO();
        local.setId(facades.id());
        local.setHechoId(facades.hechoId());
        local.setContenido(facades.contenido());
        local.setTags(facades.etiquetas());
        return local;
    }
}