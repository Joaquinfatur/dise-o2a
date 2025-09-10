package ar.edu.utn.Simulacion;

import java.util.ArrayList;
import java.util.List;
import ar.edu.utn.dds.k3003.model.PdI.PdI;

public class SolicitudesSimulacion {
    public static boolean hechoActivo(int idHecho) {

        return true;
    }

    /**
      @param idHecho 
      @param todasLasPiezas 
      @return 
     */
    public static List<PdI> obtenerPdIsPorHecho(int idHecho, List<PdI> todasLasPiezas) {
        return new ArrayList<>(todasLasPiezas);
    }
}
