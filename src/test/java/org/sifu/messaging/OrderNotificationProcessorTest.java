package org.sifu.messaging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.sifu.dto.ChangeOrderStatusRequest;
import org.sifu.dto.OrderDTO;
import org.sifu.entities.OrderStatus;
import org.sifu.service.OrderCommandService;
import java.util.UUID;


/**
 * Unit tests for OrderNotificationProcessor
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderNotificationProcessorTest {

    @Mock
    Consumer<String, OrderEvent> consumer;

    @Mock
    OrderCommandService orderCommandService;

    @Mock
    OrderDTO processedOrder;

    private OrderNotificationProcessor processor;

    private String testTopic = "product-order-topic";
    private UUID testOrderId;
    private OrderEvent testOrderEvent;

    @BeforeEach
    void setUp() throws Exception {
        processor = new OrderNotificationProcessor();
        
        var consumerField = OrderNotificationProcessor.class.getDeclaredField("consumer");
        consumerField.setAccessible(true);
        consumerField.set(processor, consumer);
        
        var serviceField = OrderNotificationProcessor.class.getDeclaredField("orderCommandService");
        serviceField.setAccessible(true);
        serviceField.set(processor, orderCommandService);
        
        var topicField = OrderNotificationProcessor.class.getDeclaredField("orderNotificationTopic");
        topicField.setAccessible(true);
        topicField.set(processor, testTopic);
        
        var runningField = OrderNotificationProcessor.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(processor, false);
        
        testOrderId = UUID.randomUUID();
        testOrderEvent = new OrderEvent();
        testOrderEvent.setOrderId(testOrderId.toString());
        testOrderEvent.setCustomerName("Test Customer");
        testOrderEvent.setCustomerEmail("test@example.com");
        testOrderEvent.setSubTotalAmount("847.46");
        testOrderEvent.setTotalAmount("1000.00");
        testOrderEvent.setItf("0.00");
        testOrderEvent.setOrderStatus("CONFIRMED");
    }

    @Test
    @DisplayName("processRecord should change order status to PROCESSING")
    void processRecord_changesStatusToProcessing() {
        // Given
        ConsumerRecord<String, OrderEvent> record = new ConsumerRecord<>(
                testTopic, 0, 0L, null, testOrderEvent);
        
        when(orderCommandService.changeStatus(eq(testOrderId), any(ChangeOrderStatusRequest.class)))
                .thenReturn(processedOrder);
        
        // When
        processor.processRecord(record);
        
        // Then
        ArgumentCaptor<ChangeOrderStatusRequest> requestCaptor = 
                ArgumentCaptor.forClass(ChangeOrderStatusRequest.class);
        verify(orderCommandService, times(1)).changeStatus(eq(testOrderId), requestCaptor.capture());
        
        ChangeOrderStatusRequest capturedRequest = requestCaptor.getValue();
        assertEquals(OrderStatus.PROCESSING, capturedRequest.getStatus());
    }

    @Test
    @DisplayName("processRecord should extract order ID from event")
    void processRecord_extractsOrderId() {
        // Given
        ConsumerRecord<String, OrderEvent> record = new ConsumerRecord<>(
                testTopic, 0, 0L, null, testOrderEvent);
        
        when(orderCommandService.changeStatus(any(UUID.class), any(ChangeOrderStatusRequest.class)))
                .thenReturn(processedOrder);
        
        // When
        processor.processRecord(record);
        
        // Then
        ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(orderCommandService).changeStatus(uuidCaptor.capture(), any());
        
        assertEquals(testOrderId, uuidCaptor.getValue());
    }

    @Test
    @DisplayName("processRecord should handle null order event gracefully")
    void processRecord_handlesNullEvent() {
        // Given
        ConsumerRecord<String, OrderEvent> record = new ConsumerRecord<>(
                testTopic, 0, 0L, null, null);
        
        // When & Then
        assertDoesNotThrow(() -> processor.processRecord(record));
        
        verify(orderCommandService, never()).changeStatus(any(UUID.class), any(ChangeOrderStatusRequest.class));
    }

    @Test
    @DisplayName("processRecord should handle service exception gracefully")
    void processRecord_handlesServiceException() {
        // Given
        ConsumerRecord<String, OrderEvent> record = new ConsumerRecord<>(
                testTopic, 0, 0L, null, testOrderEvent);
        
        when(orderCommandService.changeStatus(any(UUID.class), any(ChangeOrderStatusRequest.class)))
                .thenThrow(new RuntimeException("Order not found"));
        
        // When & Then
        assertDoesNotThrow(() -> processor.processRecord(record));
        
        verify(orderCommandService, times(1)).changeStatus(any(UUID.class), any(ChangeOrderStatusRequest.class));
    }

    @Test
    @DisplayName("processRecord calls changeStatus with correct order ID")
    void processRecord_callsChangeStatusWithCorrectOrderId() {
        // Given
        ConsumerRecord<String, OrderEvent> record = new ConsumerRecord<>(
                testTopic, 0, 0L, "test-key", testOrderEvent);
        
        when(orderCommandService.changeStatus(any(UUID.class), any(ChangeOrderStatusRequest.class)))
                .thenReturn(processedOrder);
        
        // When
        processor.processRecord(record);
        
        // Then
        verify(orderCommandService, times(1)).changeStatus(
                eq(testOrderId), any(ChangeOrderStatusRequest.class));
    }
}