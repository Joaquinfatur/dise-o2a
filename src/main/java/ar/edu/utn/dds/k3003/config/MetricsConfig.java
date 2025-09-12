package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class MetricsConfig {
    @Autowired
    private final MeterRegistry meterRegistry = null;

    @Bean
    public Counter pdisProcessedCounter() {
        return Counter.builder("procesador.pdis.processed.total")
                .description("Total number of PdIs processed")
                .tag("service", "procesador-pdi")
                .register(meterRegistry);
    }

    @Bean
    public Counter pdisErrorCounter() {
        return Counter.builder("procesador.pdis.errors.total")
                .description("Total number of errors processing PdIs")
                .tag("service", "procesador-pdi")
                .register(meterRegistry);
    }

    @Bean
    public Counter pdisRejectedCounter() {
        return Counter.builder("procesador.pdis.rejected.total")
                .description("Total number of rejected PdIs")
                .tag("service", "procesador-pdi")
                .register(meterRegistry);
    }

    @Bean
    public Timer processingTimer() {
        return Timer.builder("procesador.pdis.processing.duration")
                .description("Time taken to process PdIs")
                .tag("service", "procesador-pdi")
                .register(meterRegistry);
    }

    @Bean
    public Counter httpRequestsCounter() {
        return Counter.builder("procesador.http.requests.total")
                .description("Total HTTP requests")
                .tag("service", "procesador-pdi")
                .register(meterRegistry);
    }

    @Bean
    public Counter databaseOperationsCounter() {
        return Counter.builder("procesador.database.operations.total")
                .description("Total database operations")
                .tag("service", "procesador-pdi")
                .register(meterRegistry);
    }
}