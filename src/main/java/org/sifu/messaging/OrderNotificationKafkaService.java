package org.sifu.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.sifu.avro.OrderEvent;
import org.sifu.dto.OrderDTO;
import org.sifu.mapper.OrderMapper;


/**
 * Service for sending order confirmation notifications to Kafka.
 * Publishes OrderEvent messages to a Kafka topic when an order is confirmed.
 */
@ApplicationScoped
@Slf4j
public class OrderNotificationKafkaService {
    @Inject
    @Named("producer")
    private Producer<String, OrderEvent> producer;
    
    @Inject
    private OrderMapper orderMapper;

    @ConfigProperty(name="kafka.producer.order-notification.topic")
    private String orderNotificationTopic;
    
    /**
     * Sends an order confirmation notification to Kafka.
     * Converts the order DTO to an OrderEvent and publishes it to the configured topic.
     * @param order the order to send as a notification
     */
    public void sendNotificationOnOrderConfirmation(OrderDTO order) {
        OrderEvent orderEvent = orderMapper.toOrderEventFromDto(order);
        ProducerRecord<String, OrderEvent> producerRecord = new ProducerRecord<>(orderNotificationTopic, orderEvent);        
        producer.send(producerRecord, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send order event to Kafka: {}", exception.getMessage());
            } else {
                log.info("### Event sent to Kafka");
                log.info(orderEvent.toString());
            }
        });
    }
}
