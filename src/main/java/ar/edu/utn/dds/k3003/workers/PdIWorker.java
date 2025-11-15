package ar.edu.utn.dds.k3003.workers;

import ar.edu.utn.dds.k3003.clients.ServicesClient;
import ar.edu.utn.dds.k3003.dtos.PdIMessageDTO;
import ar.edu.utn.dds.k3003.persistence.PdIEntity;
import ar.edu.utn.dds.k3003.repositories.PdIRepository;
import ar.edu.utn.dds.k3003.services.ImageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PdIWorker {

    private static final Logger log = LoggerFactory.getLogger(PdIWorker.class);
    
    private final PdIRepository pdiRepository;
    private final ServicesClient servicesClient;
    private final ImageProcessingService imageProcessingService;

    public PdIWorker(PdIRepository pdiRepository, ServicesClient servicesClient, ImageProcessingService imageProcessingService) {
        this.pdiRepository = pdiRepository;
        this.servicesClient = servicesClient;
        this.imageProcessingService = imageProcessingService;
        System.out.println("✅✅✅ PDIWORKER BEAN CREATED ✅✅✅");
    }

    @RabbitListener(queues = "pdis_queue")
    @Transactional
    public void procesarPdI(PdIMessageDTO mensaje) {
        long tiempoInicio = System.currentTimeMillis();
        String pdiId = mensaje.getId();

        try {
            System.out.println("==========================================");
            System.out.println("🔄 WORKER: Procesando PDI " + pdiId);
            System.out.println("==========================================");

            Optional<PdIEntity> entityOpt = pdiRepository.findById(Integer.parseInt(pdiId));
            if (entityOpt.isEmpty()) {
                System.out.println("❌ PDI no encontrado: " + pdiId);
                return;
            }

            PdIEntity entity = entityOpt.get();
            String imagenUrl = entity.getContenido();

            if (imagenUrl == null || !imagenUrl.startsWith("http")) {
                entity.setEtiquetasNuevas(List.of("SinImagen"));
                entity.setProcesado(true);
                pdiRepository.save(entity);
                return;
            }

            String ocrResultado = servicesClient.procesarOCR(imagenUrl);
            entity.setOcrResultado(ocrResultado);

            String etiquetadoResultado = servicesClient.procesarEtiquetado(imagenUrl);
            entity.setEtiquetadoResultado(etiquetadoResultado);

            List<String> etiquetas = imageProcessingService.generarEtiquetasAutomaticas(ocrResultado, etiquetadoResultado);
            entity.setEtiquetasNuevas(etiquetas);
            entity.setProcesado(true);

            pdiRepository.save(entity);

            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            System.out.println("✅ PDI " + pdiId + " PROCESADO en " + tiempoTotal + "ms");

        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}