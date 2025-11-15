package ar.edu.utn.dds.k3003.workers;

import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.dtos.PdIMessageDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import ar.edu.utn.dds.k3003.services.ImageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Worker Consumer que procesa PDIs desde RabbitMQ
 * 
 * Responsabilidades:
 * 1. Escucha mensajes de pdis_queue
 * 2. Realiza OCR y etiquetado
 * 3. Actualiza el PDI en la BD
 * 4. Confirma el mensaje (ACK)
 */
@Service
public class PdIWorker {

    private static final Logger log = LoggerFactory.getLogger(PdIWorker.class);

    @Autowired
    private PdIRepository pdiRepository;

    @Autowired
    private ServicesClient servicesClient;

    @Autowired
    private ImageProcessingService imageProcessingService;

    /**
     * Procesa un mensaje de PDI desde la cola
     * 
     * El @RabbitListener automáticamente:
     * - Lee mensajes de pdis_queue
     * - Los desserializa a PdIMessageDTO
     * - Confirma el mensaje si completa sin error
     * - Reintentar si hay excepción
     */
    @RabbitListener(queues = "pdis_queue")
    @Transactional
    public void procesarPdI(PdIMessageDTO mensaje) {
        long tiempoInicio = System.currentTimeMillis();
        String pdiId = mensaje.getId();

        try {
            System.out.println("==========================================");
            System.out.println("🔄 WORKER: Procesando PDI " + pdiId);
            System.out.println("  - HechoId: " + mensaje.getHechoId());
            System.out.println("  - Contenido: " + mensaje.getContenido());
            System.out.println("==========================================");

            // 1. Validar que el PDI existe en la BD
            Optional<PdIEntity> entityOpt = pdiRepository.findById(Integer.parseInt(pdiId));
            if (entityOpt.isEmpty()) {
                System.err.println("❌ PDI no encontrado en BD: " + pdiId);
                return;
            }

            PdIEntity entity = entityOpt.get();

            // 2. Validar que sea procesable (solo URLs de imagen)
            String imagenUrl = entity.getContenido();
            if (imagenUrl == null || !imagenUrl.startsWith("http")) {
                System.out.println("⚠️ PDI sin URL de imagen válida, marcando sin procesar: " + pdiId);
                entity.setEtiquetasNuevas(List.of("SinImagen"));
                entity.setProcesado(true);
                pdiRepository.save(entity);
                return;
            }

            // 3. Procesar OCR
            System.out.println("📝 Procesando OCR...");
            String ocrResultado = servicesClient.procesarOCR(imagenUrl);
            entity.setOcrResultado(ocrResultado);
            System.out.println("✅ OCR completado");

            // 4. Procesar Etiquetado
            System.out.println("🏷️ Procesando etiquetado...");
            String etiquetadoResultado = servicesClient.procesarEtiquetado(imagenUrl);
            entity.setEtiquetadoResultado(etiquetadoResultado);
            System.out.println("✅ Etiquetado completado");

            // 5. Generar etiquetas automáticas
            System.out.println("🤖 Generando etiquetas automáticas...");
            List<String> etiquetas = imageProcessingService.generarEtiquetasAutomaticas(
                ocrResultado,
                etiquetadoResultado
            );
            entity.setEtiquetasNuevas(etiquetas);
            System.out.println("✅ Etiquetas generadas: " + etiquetas);

            // 6. Marcar como procesado
            entity.setProcesado(true);

            // 7. Guardar en BD
            pdiRepository.save(entity);

            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            System.out.println("==========================================");
            System.out.println("✅ PDI " + pdiId + " procesado exitosamente");
            System.out.println("   Tiempo: " + tiempoTotal + "ms");
            System.out.println("==========================================");

        } catch (Exception e) {
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            System.err.println("==========================================");
            System.err.println("❌ ERROR procesando PDI " + pdiId);
            System.err.println("   Mensaje: " + e.getMessage());
            System.err.println("   Tiempo: " + tiempoTotal + "ms");
            System.err.println("==========================================");
            e.printStackTrace();

            // Marcar como procesado pero con error
            try {
                Optional<PdIEntity> entityOpt = pdiRepository.findById(Integer.parseInt(pdiId));
                if (entityOpt.isPresent()) {
                    PdIEntity entity = entityOpt.get();
                    entity.setEtiquetasNuevas(List.of("ErrorProcesamiento"));
                    entity.setProcesado(true);
                    pdiRepository.save(entity);
                }
            } catch (Exception ex) {
                System.err.println("Error guardando estado de error: " + ex.getMessage());
            }

            // Relanzar la excepción para que Spring AMQP la maneje (reintentos)
            throw new RuntimeException("Error procesando PDI: " + e.getMessage(), e);
        }
    }
}