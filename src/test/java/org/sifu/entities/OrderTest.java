package org.sifu.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order entity
 */
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("No args constructor creates instance with null fields")
        void noArgsConstructor() {
            assertNull(order.getId());
            assertNull(order.getCustomerName());
            assertNull(order.getCustomerEmail());
            assertNull(order.getTotalAmount());
            assertNull(order.getOrderStatus());
            assertNull(order.getCreatedAt());
            assertNull(order.getOrderItems());
        }

        @Test
        @DisplayName("All args constructor creates instance")
        void allArgsConstructor() {
            UUID id = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");
            Order orderWithArgs = new Order(
                    id,
                    "Test Customer",
                    "test@example.com",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    amount,
                    BigDecimal.ZERO,
                    OrderStatus.PENDING,
                    LocalDateTime.now(),
                    List.of()
            );

            assertEquals(id, orderWithArgs.getId());
            assertEquals("Test Customer", orderWithArgs.getCustomerName());
            assertEquals("test@example.com", orderWithArgs.getCustomerEmail());
            assertEquals(amount, orderWithArgs.getTotalAmount());
            assertEquals(OrderStatus.PENDING, orderWithArgs.getOrderStatus());
        }
    }

    @Nested
    @DisplayName("Field tests")
    class FieldTests {

        @Test
        @DisplayName("ID can be set and retrieved")
        void idField() {
            UUID id = UUID.randomUUID();
            order.setId(id);
            assertEquals(id, order.getId());
        }

        @Test
        @DisplayName("Customer name can be set and retrieved")
        void customerNameField() {
            order.setCustomerName("John Doe");
            assertEquals("John Doe", order.getCustomerName());
        }

        @Test
        @DisplayName("Customer email can be set and retrieved")
        void customerEmailField() {
            order.setCustomerEmail("john@example.com");
            assertEquals("john@example.com", order.getCustomerEmail());
        }

        @Test
        @DisplayName("Total amount can be set and retrieved")
        void totalAmountField() {
            BigDecimal amount = new BigDecimal("250.50");
            order.setTotalAmount(amount);
            assertEquals(amount, order.getTotalAmount());
        }

        @Test
        @DisplayName("Order status can be set and retrieved")
        void orderStatusField() {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        }

        @Test
        @DisplayName("Created at can be set and retrieved")
        void createdAtField() {
            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            assertEquals(now, order.getCreatedAt());
        }

        @Test
        @DisplayName("Order items can be set and retrieved")
        void orderItemsField() {
            OrderItem item = new OrderItem();
            List<OrderItem> items = List.of(item);
            order.setOrderItems(items);
            assertEquals(items, order.getOrderItems());
            assertEquals(1, order.getOrderItems().size());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Total amount allows zero")
        void zeroAmount() {
            order.setTotalAmount(BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        }

        @Test
        @DisplayName("Total amount allows negative values")
        void negativeAmount() {
            order.setTotalAmount(new BigDecimal("-50.00"));
            assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0);
        }

        @Test
        @DisplayName("Order status can be any enum value")
        void allStatuses() {
            for (OrderStatus status : OrderStatus.values()) {
                order.setOrderStatus(status);
                assertEquals(status, order.getOrderStatus());
            }
        }

        @Test
        @DisplayName("Order items list can be empty")
        void emptyItemsList() {
            order.setOrderItems(List.of());
            assertTrue(order.getOrderItems().isEmpty());
        }

        @Test
        @DisplayName("Order items list can be null")
        void nullItemsList() {
            order.setOrderItems(null);
            assertNull(order.getOrderItems());
        }
    }
}