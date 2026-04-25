package org.sifu.messaging;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.sifu.avro.OrderEvent;
import org.sifu.dto.ChangeOrderStatusRequest;
import org.sifu.entities.OrderStatus;
import org.sifu.service.OrderCommandService;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Kafka consumer processor for order notification events.
 * Consumes OrderEvent messages from Kafka and updates order status to PROCESSING.
 * Runs on application startup and processes messages asynchronously using virtual threads.
 */
@ApplicationScoped
@Slf4j
public class OrderNotificationProcessor {
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    
    @Inject
    @Named("consumer")
    Consumer<String, OrderEvent> consumer;
    
    boolean running = true;

    @ConfigProperty(name="kafka.producer.order-notification.topic")
    private String orderNotificationTopic;
    
    @Inject
    private OrderCommandService orderCommandService;
    
    /**
     * Initializes the Kafka consumer on application startup.
     * Subscribes to the order notification topic and starts consuming messages.
     * @param event the startup event
     */
    public void initialize(@Observes StartupEvent event) {
        log.info("### Starting Order Notification Processor ...");
        consumer.subscribe(Collections.singletonList(orderNotificationTopic));
        executorService.submit(() -> {
            while (running) {
                try {
                    var records = consumer.poll(Duration.ofSeconds(1));
                    if (!records.isEmpty()) {
                        records.forEach(record -> executorService.submit(() -> {
                            processRecord(record);
                        }));
                    }
                }
                catch (Exception e) {
                    log.error("Failed to process records: {}", e.getMessage());
                }
            }
        });
    }
    
    /**
     * Processes a single Kafka record containing an order event.
     * Extracts the order ID from the event and changes the order status to PROCESSING.
     * @param record the Kafka consumer record containing the order event
     */
    public void processRecord(ConsumerRecord<String, OrderEvent> record) {
        try {
            var orderEvent = record.value();
            log.info("### Processing order event: {}", orderEvent.toString());
            ChangeOrderStatusRequest request = new ChangeOrderStatusRequest();
            request.setStatus(OrderStatus.PROCESSING);
            var processedOrder = orderCommandService.changeStatus( UUID.fromString(orderEvent.getOrderId()), request);
            log.info("### Processed order: {}", processedOrder.toString());
        }
        catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage());
        }
    }
}
