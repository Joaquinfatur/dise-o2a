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
    private MeterRegistry meterRegistry;

    @Bean
    public Counter pdisProcessedCounter() {
        return Counter.builder("pdis.processed.total")
                .description("Total number of PdIs processed")
                .register(meterRegistry);
    }

    @Bean
    public Counter pdisErrorCounter() {
        return Counter.builder("pdis.errors.total")
                .description("Total number of errors processing PdIs")
                .register(meterRegistry);
    }

    @Bean
    public Counter pdisRejectedCounter() {
        return Counter.builder("pdis.rejected.total")
                .description("Total number of rejected PdIs")
                .register(meterRegistry);
    }

    @Bean
    public Timer processingTimer() {
        return Timer.builder("pdis.processing.duration")
                .description("Time taken to process PdIs")
                .register(meterRegistry);
    }

    @Bean
    public Counter httpRequestsCounter() {
        return Counter.builder("http.requests.total")
                .description("Total HTTP requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter databaseOperationsCounter() {
        return Counter.builder("database.operations.total")
                .description("Total database operations")
                .register(meterRegistry);
    }
}