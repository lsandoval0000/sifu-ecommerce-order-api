package org.sifu.messaging;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.sifu.avro.OrderEvent;
import org.sifu.dto.OrderDTO;
import org.sifu.entities.OrderStatus;
import org.sifu.mapper.OrderMapper;

import java.math.BigDecimal;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderNotificationKafkaService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderNotificationKafkaServiceTest {

    @Mock
    Producer<String, OrderEvent> producer;

    @Mock
    OrderMapper orderMapper;

    @Mock
    Future<RecordMetadata> futureMetadata;

    private OrderNotificationKafkaService kafkaService;

    private OrderDTO testOrder;
    private OrderEvent testOrderEvent;
    private String testTopic = "product-order-topic";

    @BeforeEach
    void setUp() throws Exception {
        kafkaService = new OrderNotificationKafkaService();
        
        var producerField = OrderNotificationKafkaService.class.getDeclaredField("producer");
        producerField.setAccessible(true);
        producerField.set(kafkaService, producer);
        
        var mapperField = OrderNotificationKafkaService.class.getDeclaredField("orderMapper");
        mapperField.setAccessible(true);
        mapperField.set(kafkaService, orderMapper);
        
        var topicField = OrderNotificationKafkaService.class.getDeclaredField("orderNotificationTopic");
        topicField.setAccessible(true);
        topicField.set(kafkaService, testTopic);
        
        testOrder = new OrderDTO();
        testOrder.setId(java.util.UUID.randomUUID());
        testOrder.setCustomerName("Test Customer");
        testOrder.setCustomerEmail("test@example.com");
        testOrder.setOrderStatus(OrderStatus.CONFIRMED);
        testOrder.setTotalAmount(BigDecimal.valueOf(1000.00));
        testOrder.setSubTotalAmount(BigDecimal.valueOf(847.46));
        testOrder.setIgv(BigDecimal.valueOf(152.54));
        testOrder.setItf(BigDecimal.valueOf(0.00));
        
        testOrderEvent = new OrderEvent();
        testOrderEvent.setOrderId(testOrder.getId().toString());
        testOrderEvent.setCustomerName("Test Customer");
        testOrderEvent.setCustomerEmail("test@example.com");
        testOrderEvent.setSubTotalAmount("847.46");
        testOrderEvent.setTotalAmount("1000.00");
        testOrderEvent.setItf("0.00");
        testOrderEvent.setOrderStatus("CONFIRMED");
    }

    @Test
    @DisplayName("sendNotificationOnOrderConfirmation sends message to Kafka successfully")
    void sendNotification_sendsToKafka_success() {
        // Given
        when(orderMapper.toOrderEventFromDto(testOrder)).thenReturn(testOrderEvent);

        doAnswer(invocation -> {
            Future<?> future = mock(Future.class);
            return future;
        }).when(producer).send(any(ProducerRecord.class), any());

        // When
        kafkaService.sendNotificationOnOrderConfirmation(testOrder);

        // Then
        verify(orderMapper).toOrderEventFromDto(testOrder);
        
        ArgumentCaptor<ProducerRecord<String, OrderEvent>> recordCaptor = 
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(producer).send(recordCaptor.capture(), any());
        
        ProducerRecord<String, OrderEvent> capturedRecord = recordCaptor.getValue();
        assertEquals(testTopic, capturedRecord.topic());
        assertNotNull(capturedRecord.value());
    }

    @Test
    @DisplayName("sendNotificationOnOrderConfirmation maps order to OrderEvent correctly")
    void sendNotification_mapsOrderToEventCorrectly() {
        // Given
        when(orderMapper.toOrderEventFromDto(testOrder)).thenReturn(testOrderEvent);
        doAnswer(invocation -> mock(Future.class)).when(producer).send(any(ProducerRecord.class), any());

        // When
        kafkaService.sendNotificationOnOrderConfirmation(testOrder);

        // Then
        verify(orderMapper).toOrderEventFromDto(testOrder);
    }

    @Test
    @DisplayName("sendNotificationOnOrderConfirmation uses correct topic from config")
    void sendNotification_usesCorrectTopic() {
        // Given
        when(orderMapper.toOrderEventFromDto(testOrder)).thenReturn(testOrderEvent);
        doAnswer(invocation -> mock(Future.class)).when(producer).send(any(ProducerRecord.class), any());

        // When
        kafkaService.sendNotificationOnOrderConfirmation(testOrder);

        // Then
        ArgumentCaptor<ProducerRecord<String, OrderEvent>> recordCaptor = 
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(producer).send(recordCaptor.capture(), any());
        
        assertEquals(testTopic, recordCaptor.getValue().topic());
    }

    @Test
    @DisplayName("sendNotificationOnOrderConfirmation constructs ProducerRecord with correct topic and event")
    void sendNotification_constructsCorrectProducerRecord() {
        // Given
        when(orderMapper.toOrderEventFromDto(testOrder)).thenReturn(testOrderEvent);
        doAnswer(invocation -> mock(Future.class)).when(producer).send(any(ProducerRecord.class), any());

        // When
        kafkaService.sendNotificationOnOrderConfirmation(testOrder);

        // Then
        ArgumentCaptor<ProducerRecord<String, OrderEvent>> recordCaptor = 
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(producer).send(recordCaptor.capture(), any());
        
        ProducerRecord<String, OrderEvent> record = recordCaptor.getValue();
        
        assertEquals(testTopic, record.topic());
        
        OrderEvent sentEvent = record.value();
        assertNotNull(sentEvent);
        assertEquals(testOrder.getId().toString(), sentEvent.getOrderId().toString());
        assertEquals("Test Customer", sentEvent.getCustomerName().toString());
        assertEquals("test@example.com", sentEvent.getCustomerEmail().toString());
    }

    @Test
    @DisplayName("sendNotificationOnOrderConfirmation is called once per order confirmation")
    void sendNotification_isCalledOnce() {
        // Given
        when(orderMapper.toOrderEventFromDto(testOrder)).thenReturn(testOrderEvent);
        doAnswer(invocation -> mock(Future.class)).when(producer).send(any(ProducerRecord.class), any());

        // When
        kafkaService.sendNotificationOnOrderConfirmation(testOrder);

        // Then
        verify(producer, times(1)).send(any(ProducerRecord.class), any());
    }
}