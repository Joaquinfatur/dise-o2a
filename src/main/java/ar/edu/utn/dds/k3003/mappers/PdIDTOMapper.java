package ar.edu.utn.dds.k3003.mappers;

import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;

public class PdIDTOMapper {

    public static PdIDTO toFacadesDto(PdILocalDTO local) {
        return new PdIDTO(
            local.getId(),
            local.getHechoId(),
            local.getContenido(),
            local.getUbicacion(),
            local.getFecha(),
            local.getUsuarioId(),
            local.getEtiquetas()
        );
    }

    public static PdILocalDTO toLocalDto(PdIDTO facades) {
        return new PdILocalDTO(
            facades.id(),
            facades.hechoId(),
            facades.contenido(),
            null,
            null,
            null,
            facades.etiquetas()      
        );
    }
}