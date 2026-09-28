package demo.appointmentservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;


@Configuration
public class Resilience4jEventListener {
    private static final Logger log = LoggerFactory.getLogger(Resilience4jEventListener.class);

    @Bean
    @Lazy
    @Order(Ordered.LOWEST_PRECEDENCE)
    public RegistryEventConsumer circuitBreakerConsumer(){
        return new RegistryEventConsumer() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent entryAddedEvent) {
                CircuitBreaker circuitBreaker = (CircuitBreaker) entryAddedEvent.getAddedEntry();

                circuitBreaker.getEventPublisher().onStateTransition(event -> {
                    log.info("========== [CIRCUIT BREAKER CHANGE] ==========");
                    log.info("Circuit Breaker '{}': Chuyển trạng thái từ {} sang {}",
                            event.getCircuitBreakerName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                    if (event.getStateTransition().getToState() == CircuitBreaker.State.CLOSED) {
                        log.info(">>> THÔNG BÁO: Đã khôi phục kết nối thành công! Đã ĐÓNG MẠCH doctor-service.");
                    }
                });
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent entryRemoveEvent) {

            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent entryReplacedEvent) {

            }
        };
    }
}
