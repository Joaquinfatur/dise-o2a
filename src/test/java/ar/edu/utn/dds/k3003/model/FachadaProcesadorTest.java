package ar.edu.utn.dds.k3003.model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ar.edu.utn.Simulacion.SolicitudesSimulacion;
import ar.edu.utn.dds.k3003.app.FachadaProcesador;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.model.PdI.PdI;

public class FachadaProcesadorTest {
    @Test
    public void procesarPdICorrectamente() {
        FachadaProcesador procesador = new FachadaProcesador();
        PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba"); // Simula un hecho válido
        PdILocalDTO resultado = procesador.procesarPdI(dto);

        assertNotNull(resultado);
        assertTrue(resultado.getEtiquetas().contains("Importante"));
    }

   @Test
void procesadorDebeRechazarPdICuandoHechoNoActivo() {
    try (var mockSolicitudes = org.mockito.Mockito.mockStatic(SolicitudesSimulacion.class)) {
        mockSolicitudes.when(() -> SolicitudesSimulacion.hechoActivo(1)).thenReturn(false);
        
        FachadaProcesador procesador = new FachadaProcesador();
        PdILocalDTO piezaDTO = new PdILocalDTO(1, 1, "Texto de prueba");
        
        PdILocalDTO resultado = procesador.procesarPdI(piezaDTO);
        
        assertNull(resultado); 
    }
}
@Test
void procesadorDebeRetornarPiezasDeHechoSimuladas() {
    
    FachadaProcesador procesador = new FachadaProcesador();
    
   
    PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba");
    procesador.procesarPdI(dto);
    
    
    List<PdI> piezas = procesador.obtenerPdIsPorHecho(2); //
    System.out.println("Cantidad de piezas recibidas después de procesar: " + piezas.size());
    assertEquals(1, piezas.size());
}

@Test
void procesadorNoReprocesaPdIDuplicado() {
    FachadaProcesador procesador = new FachadaProcesador();
    PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba");
    
   
    PdILocalDTO resultado1 = procesador.procesarPdI(dto);
    
    PdILocalDTO resultado2 = procesador.procesarPdI(dto);
    
   
    assertNotNull(resultado1);
    assertEquals(resultado1.getEtiquetas(), resultado2.getEtiquetas());
    assertEquals(resultado1.getContenido(), resultado2.getContenido());
}

@Test
void procesadorManejaEntradaInvalida() {
    FachadaProcesador procesador = new FachadaProcesador();
    
    PdILocalDTO dtoInvalido = new PdILocalDTO(2, 3, "");
    
    
    PdILocalDTO resultado = procesador.procesarPdI(dtoInvalido);
    
    
    assertNull(resultado, "Se esperaba null para un PdIDTO con contenido inválido");
}
/* Si FachadaProcesador.obtenerPdIsPorHecho() delega a la clase de simulación.



@Test
void procesadorIntegraSolicitudesSimulacion() {
    try (var mockSolicitudes = org.mockito.Mockito.mockStatic(SolicitudesSimulacion.class)) {
        List<PdI> piezasSimuladas = List.of(new PdI(5, "Texto simulado"));
        
        // Supongamos que la implementación delega y pasa una lista de piezas procesadas.
        mockSolicitudes.when(() -> SolicitudesSimulacion.obtenerPdIsPorHecho(5, new ArrayList<>()))
                       .thenReturn(piezasSimuladas);
        
        FachadaProcesador procesador = new FachadaProcesador();
        List<PdI> piezas = procesador.obtenerPdIsPorHecho(5);
        
        assertEquals(1, piezas.size());
        assertEquals("Texto simulado", piezas.get(0).getContenido());
    }
}*/
}
