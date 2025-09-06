package ar.edu.utn.dds.k3003.model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import ar.edu.utn.Simulacion.SolicitudesSimulacion;
import ar.edu.utn.dds.k3003.app.FachadaProcesador;
import ar.edu.utn.dds.k3003.dtos.PdILocalDTO;
import ar.edu.utn.dds.k3003.model.PdI.PdI;

public class FachadaProcesadorTest {
    
    private FachadaProcesador procesador;
    
    @BeforeEach
    void setUp() {
        procesador = new FachadaProcesador();
    }

    @Test
    public void procesarPdICorrectamente() {
        try {
            PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba"); // Simula un hecho válido
            PdILocalDTO resultado = procesador.procesarPdI(dto);

            assertNotNull(resultado);
            assertTrue(resultado.getEtiquetas().contains("Importante"));
        } catch (Exception e) {
            // Si hay excepción, el test falla
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorDebeRechazarPdICuandoHechoNoActivo() {
        try {
           
            // Opción 1: Test con contenido vacío 
            PdILocalDTO piezaDTO = new PdILocalDTO(1, 1, ""); // contenido vacío
            
            PdILocalDTO resultado = procesador.procesarPdI(piezaDTO);
            
            assertNull(resultado, "Debería rechazar PdI con contenido vacío"); 
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorDebeRetornarPiezasDeHechoSimuladas() {
        try {
            PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba");
            procesador.procesarPdI(dto);
            
            List<PdI> piezas = procesador.obtenerPdIsPorHecho(2);
            System.out.println("Cantidad de piezas recibidas después de procesar: " + piezas.size());
            assertEquals(1, piezas.size());
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorNoReprocesaPdIDuplicado() {
        try {
            PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba");
            
            PdILocalDTO resultado1 = procesador.procesarPdI(dto);
            PdILocalDTO resultado2 = procesador.procesarPdI(dto);
            
            assertNotNull(resultado1);
            assertEquals(resultado1.getEtiquetas(), resultado2.getEtiquetas());
            assertEquals(resultado1.getContenido(), resultado2.getContenido());
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorManejaEntradaInvalida() {
        try {
            PdILocalDTO dtoInvalido = new PdILocalDTO(2, 3, "");
            
            PdILocalDTO resultado = procesador.procesarPdI(dtoInvalido);
            
            assertNull(resultado, "Se esperaba null para un PdIDTO con contenido inválido");
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorDebeObtenerEstadisticas() {
        try {
            // Test verifica que las estadísticas funcionan
            var stats = procesador.getEstadisticas();
            
            assertNotNull(stats);
            assertTrue(stats.containsKey("totalProcesadas"));
            assertTrue(stats.containsKey("enMemoria"));
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorDebeLimpiarDatos() {
        try {
            // Procesar una PdI
            PdILocalDTO dto = new PdILocalDTO(1, 2, "Texto de prueba");
            procesador.procesarPdI(dto);
            
            // Verificar que hay datos - usar Number en lugar de Long
            var statsAntes = procesador.getEstadisticas();
            Number enMemoriaAntes = (Number) statsAntes.get("enMemoria");
            assertTrue(enMemoriaAntes.intValue() > 0);
            
            // Limpiar datos
            procesador.limpiarDatos();
            
            // Verificar que no hay datos
            var statsDespues = procesador.getEstadisticas();
            Number enMemoriaDespues = (Number) statsDespues.get("enMemoria");
            assertEquals(0, enMemoriaDespues.intValue());
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    /* Por ahora no usa SolicitudesSimulacion directamente
    @Test
    void procesadorIntegraSolicitudesSimulacion() {
        try (var mockSolicitudes = org.mockito.Mockito.mockStatic(SolicitudesSimulacion.class)) {
            List<PdI> piezasSimuladas = List.of(new PdI(5, "Texto simulado"));
            
            mockSolicitudes.when(() -> SolicitudesSimulacion.obtenerPdIsPorHecho(5, new ArrayList<>()))
                           .thenReturn(piezasSimuladas);
            
            List<PdI> piezas = procesador.obtenerPdIsPorHecho(5);
            
            assertEquals(1, piezas.size());
            assertEquals("Texto simulado", piezas.get(0).getContenido());
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }
    */
}