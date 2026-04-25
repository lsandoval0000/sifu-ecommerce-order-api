package org.sifu.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sifu.avro.OrderEvent;
import org.sifu.dto.CreateOrderRequest;
import org.sifu.dto.OrderDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Unit tests for OrderMapper
 */
class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapperImpl();
    }

    @Nested
    @DisplayName("toEntity method")
    class ToEntityMethod {

        @Test
        @DisplayName("Maps CreateOrderRequest to Order entity")
        void mapsRequestToEntity() {
            // Given
            CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@example.com");

            // When
            Order order = orderMapper.toEntity(request);

            // Then
            assertNotNull(order);
            assertEquals("John Doe", order.getCustomerName());
            assertEquals("john@example.com", order.getCustomerEmail());
            assertEquals(OrderStatus.PENDING, order.getOrderStatus());
            assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        }

        @Test
        @DisplayName("Returns null when request is null")
        void returnsNullWhenNull() {
            Order order = orderMapper.toEntity(null);
            assertNull(order);
        }

        @Test
        @DisplayName("Sets PENDING as default status")
        void setsPendingAsDefaultStatus() {
            CreateOrderRequest request = new CreateOrderRequest("Test", "test@test.com");
            Order order = orderMapper.toEntity(request);
            assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        }

        @Test
        @DisplayName("Initializes total amount to ZERO")
        void initializesTotalToZero() {
            CreateOrderRequest request = new CreateOrderRequest("Test", "test@test.com");
            Order order = orderMapper.toEntity(request);
            assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("toDTO method")
    class ToDTOMethod {

        @Test
        @DisplayName("Maps Order entity to OrderDTO")
        void mapsEntityToDTO() {
            // Given
            Order order = new Order();
            order.setId(UUID.randomUUID());
            order.setCustomerName("Jane Doe");
            order.setCustomerEmail("jane@example.com");
            order.setTotalAmount(new BigDecimal("100.50"));
            order.setOrderStatus(OrderStatus.PROCESSING);

            // When
            OrderDTO dto = orderMapper.toDTO(order);

            // Then
            assertNotNull(dto);
            assertEquals(order.getId(), dto.getId());
            assertEquals(order.getCustomerName(), dto.getCustomerName());
            assertEquals(order.getCustomerEmail(), dto.getCustomerEmail());
            assertEquals(order.getTotalAmount(), dto.getTotalAmount());
            assertEquals(order.getOrderStatus(), dto.getOrderStatus());
        }

        @Test
        @DisplayName("Returns null when order is null")
        void returnsNullWhenOrderIsNull() {
            OrderDTO dto = orderMapper.toDTO(null);
            assertNull(dto);
        }

        @Test
        @DisplayName("Maps all fields correctly")
        void mapsAllFields() {
            UUID id = UUID.randomUUID();
            Order order = new Order();
            order.setId(id);
            order.setCustomerName("Test Customer");
            order.setCustomerEmail("test@domain.com");
            order.setIgv(new BigDecimal("250.00"));
            order.setSubTotalAmount(new BigDecimal("250.00"));
            order.setSubTotalAmount(new BigDecimal("250.00"));
            order.setTotalAmount(new BigDecimal("250.00"));
            order.setOrderStatus(OrderStatus.SHIPPED);

            OrderDTO dto = orderMapper.toDTO(order);

            assertEquals(id, dto.getId());
            assertEquals("Test Customer", dto.getCustomerName());
            assertEquals("test@domain.com", dto.getCustomerEmail());
            assertEquals(new BigDecimal("250.00"), dto.getTotalAmount());
            assertEquals(OrderStatus.SHIPPED, dto.getOrderStatus());
        }
    }

    @Nested
    @DisplayName("bigDecimalToString method")
    class BigDecimalToStringMethod {

        @Test
        @DisplayName("Converts BigDecimal to String")
        void convertsBigDecimalToString() {
            // Given
            BigDecimal value = new BigDecimal("100.50");

            // When
            String result = orderMapper.bigDecimalToString(value);

            // Then
            assertEquals("100.50", result);
        }

        @Test
        @DisplayName("Returns null when BigDecimal is null")
        void returnsNullWhenBigDecimalIsNull() {
            String result = orderMapper.bigDecimalToString(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Converts zero BigDecimal to String")
        void convertsZeroBigDecimalToString() {
            BigDecimal value = BigDecimal.ZERO;
            String result = orderMapper.bigDecimalToString(value);
            assertEquals("0", result);
        }
    }

    @Nested
    @DisplayName("enumToString method")
    class EnumToStringMethod {

        @Test
        @DisplayName("Converts OrderStatus enum to String")
        void convertsOrderStatusToString() {
            // Given
            OrderStatus status = OrderStatus.PROCESSING;

            // When
            String result = orderMapper.enumToString(status);

            // Then
            assertEquals("PROCESSING", result);
        }

        @Test
        @DisplayName("Returns null when OrderStatus is null")
        void returnsNullWhenOrderStatusIsNull() {
            String result = orderMapper.enumToString(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Converts PENDING enum to String")
        void convertsPendingEnumToString() {
            OrderStatus status = OrderStatus.PENDING;
            String result = orderMapper.enumToString(status);
            assertEquals("PENDING", result);
        }
    }

    @Nested
    @DisplayName("toOrderEventFromDto method")
    class ToOrderEventMethod {

        @Test
        @DisplayName("Maps Order dto to OrderEvent")
        void mapsEntityToOrderEvent() {
            // Given
            UUID id = UUID.randomUUID();
            OrderDTO order = new OrderDTO();
            order.setId(id);
            order.setCustomerName("John Doe");
            order.setCustomerEmail("john@example.com");
            order.setSubTotalAmount(new BigDecimal("50.00"));
            order.setTotalAmount(new BigDecimal("60.00"));
            order.setItf(new BigDecimal("5.00"));
            order.setOrderStatus(OrderStatus.PROCESSING);

            // When
            OrderEvent event = orderMapper.toOrderEventFromDto(order);

            // Then
            assertNotNull(event);
            assertEquals(id.toString(), event.getOrderId());
            assertEquals("John Doe", event.getCustomerName());
            assertEquals("john@example.com", event.getCustomerEmail());
            assertEquals("50.00", event.getSubTotalAmount());
            assertEquals("60.00", event.getTotalAmount());
            assertEquals("5.00", event.getItf());
            assertEquals("PROCESSING", event.getOrderStatus());
        }

        @Test
        @DisplayName("Returns null when order is null")
        void returnsNullWhenOrderIsNull() {
            OrderEvent event = orderMapper.toOrderEventFromDto(null);
            assertNull(event);
        }

        @Test
        @DisplayName("Converts BigDecimal fields to String")
        void convertsBigDecimalFieldsToString() {
            OrderDTO order = new OrderDTO();
            order.setId(UUID.randomUUID());
            order.setCustomerName("Test");
            order.setCustomerEmail("test@test.com");
            order.setSubTotalAmount(new BigDecimal("123.45"));
            order.setTotalAmount(new BigDecimal("234.56"));
            order.setItf(new BigDecimal("12.34"));
            order.setOrderStatus(OrderStatus.SHIPPED);

            OrderEvent event = orderMapper.toOrderEventFromDto(order);

            assertEquals("123.45", event.getSubTotalAmount());
            assertEquals("234.56", event.getTotalAmount());
            assertEquals("12.34", event.getItf());
        }

        @Test
        @DisplayName("Converts OrderStatus enum to String")
        void convertsOrderStatusEnumToString() {
            OrderDTO order = new OrderDTO();
            order.setId(UUID.randomUUID());
            order.setCustomerName("Test");
            order.setCustomerEmail("test@test.com");
            order.setSubTotalAmount(BigDecimal.ZERO);
            order.setTotalAmount(BigDecimal.ZERO);
            order.setItf(BigDecimal.ZERO);
            order.setOrderStatus(OrderStatus.DELIVERED);

            OrderEvent event = orderMapper.toOrderEventFromDto(order);

            assertEquals("DELIVERED", event.getOrderStatus());
        }
    }
}