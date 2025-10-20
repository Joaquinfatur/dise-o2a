package ar.edu.utn.dds.k3003.model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import ar.edu.utn.dds.k3003.app.FachadaProcesador;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;

import java.time.LocalDateTime;

public class FachadaProcesadorTest {
    
    private FachadaProcesador procesador;
    
    @BeforeEach
    void setUp() {
        procesador = new FachadaProcesador();
    }

    @Test
    public void procesarPdICorrectamente() {
        try {
            PdIDTO dto = new PdIDTO(
                "1",
                "2",
                "Texto de prueba",
                "",
                LocalDateTime.now(),
                "",
                List.of()
            );
            
            PdIDTO resultado = procesador.procesar(dto);

            assertNotNull(resultado);
            assertNotNull(resultado.contenido());
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorDebeRechazarPdICuandoContenidoVacio() {
        try {
            PdIDTO piezaDTO = new PdIDTO(
                "1", 
                "1", 
                "",
                "",
                LocalDateTime.now(),
                "",
                List.of()
            );
            
            PdIDTO resultado = procesador.procesar(piezaDTO);
            
            assertNull(resultado, "Debería rechazar PdI con contenido vacío"); 
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorDebeRetornarPiezasDeHecho() {
        try {
            PdIDTO dto = new PdIDTO(
                "1",
                "2",
                "Texto de prueba",
                "",
                LocalDateTime.now(),
                "",
                List.of()
            );
            procesador.procesar(dto);
            
            List<PdIDTO> piezas = procesador.buscarPorHecho("2");
            System.out.println("Cantidad de piezas recibidas después de procesar: " + piezas.size());
            assertTrue(piezas.size() >= 1);
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorNoReprocesaPdIDuplicado() {
        try {
            PdIDTO dto = new PdIDTO(
                "1",
                "2",
                "Texto de prueba",
                "",
                LocalDateTime.now(),
                "",
                List.of()
            );
            
            PdIDTO resultado1 = procesador.procesar(dto);
            PdIDTO resultado2 = procesador.procesar(dto);
            
            assertNotNull(resultado1);
            assertNotNull(resultado2);
            assertEquals(resultado1.id(), resultado2.id());
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }

    @Test
    void procesadorManejaEntradaInvalida() {
        try {
            PdIDTO dtoInvalido = new PdIDTO(
                "2",
                "3",
                "",
                "",
                LocalDateTime.now(),
                "",
                List.of()
            );
            
            PdIDTO resultado = procesador.procesar(dtoInvalido);
            
            assertNull(resultado, "Se esperaba null para un PdIDTO con contenido inválido");
        } catch (Exception e) {
            throw new RuntimeException("Test falló con excepción: " + e.getMessage(), e);
        }
    }
}