package ar.edu.utn.Simulacion;

import java.util.ArrayList;
import java.util.List;
import ar.edu.utn.dds.k3003.model.PdI.PdI;

public class SolicitudesSimulacion {
    public static boolean hechoActivo(int idHecho) {
        // Simulación: asumimos que todos los hechos están activos.
        return true;
    }

    /**
     * @param idHecho 
     * @param todasLasPiezas 
     * @return una lista de piezas de información asociadas al hecho
     */
    public static List<PdI> obtenerPdIsPorHecho(int idHecho, List<PdI> todasLasPiezas) {
        return new ArrayList<>(todasLasPiezas);
    }
}
