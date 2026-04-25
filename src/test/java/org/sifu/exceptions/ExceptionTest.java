package org.sifu.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom exceptions
 */
class ExceptionTest {

    @Nested
    @DisplayName("OrderNotFoundException tests")
    class OrderNotFoundExceptionTests {

        @Test
        @DisplayName("Creates exception with UUID")
        void createsWithUUID() {
            UUID id = UUID.randomUUID();
            OrderNotFoundException ex = new OrderNotFoundException(id);

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains(id.toString()));
            assertEquals(id, ex.getId());
        }

        @Test
        @DisplayName("Creates exception with String")
        void createsWithString() {
            OrderNotFoundException ex = new OrderNotFoundException("order-123");

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("order-123"));
            assertEquals("order-123", ex.getId());
        }

        @Test
        @DisplayName("Message contains ErrorMessages text")
        void messageContainsErrorText() {
            OrderNotFoundException ex = new OrderNotFoundException(UUID.randomUUID());

            assertTrue(ex.getMessage().contains("Order"));
            assertTrue(ex.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(OrderNotFoundException.class));
        }
    }

    @Nested
    @DisplayName("ProductNotFoundException tests")
    class ProductNotFoundExceptionTests {

        @Test
        @DisplayName("Creates exception with UUID")
        void createsWithUUID() {
            UUID id = UUID.randomUUID();
            ProductNotFoundException ex = new ProductNotFoundException(id);

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains(id.toString()));
            assertEquals(id, ex.getId());
        }

        @Test
        @DisplayName("Creates exception with String")
        void createsWithString() {
            ProductNotFoundException ex = new ProductNotFoundException("prod-456");

            assertNotNull(ex.getMessage());
            assertEquals("prod-456", ex.getId());
        }

        @Test
        @DisplayName("Message contains Product text")
        void messageContainsProductText() {
            ProductNotFoundException ex = new ProductNotFoundException(UUID.randomUUID());

            assertTrue(ex.getMessage().contains("Product"));
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(ProductNotFoundException.class));
        }
    }

    @Nested
    @DisplayName("OrderItemNotFoundException tests")
    class OrderItemNotFoundExceptionTests {

        @Test
        @DisplayName("Creates exception with UUID")
        void createsWithUUID() {
            UUID id = UUID.randomUUID();
            OrderItemNotFoundException ex = new OrderItemNotFoundException(id);

            assertNotNull(ex.getMessage());
            assertEquals(id, ex.getId());
        }

        @Test
        @DisplayName("Message contains Order item text")
        void messageContainsOrderItemText() {
            OrderItemNotFoundException ex = new OrderItemNotFoundException(UUID.randomUUID());

            assertTrue(ex.getMessage().contains("Order item"));
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(OrderItemNotFoundException.class));
        }
    }

    @Nested
    @DisplayName("BusinessRuleViolationException tests")
    class BusinessRuleViolationExceptionTests {

        @Test
        @DisplayName("Creates with error message")
        void createsWithMessage() {
            BusinessRuleViolationException ex = new BusinessRuleViolationException("Test error");

            assertEquals("Test error", ex.getMessage());
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(BusinessRuleViolationException.class));
        }

        @Test
        @DisplayName("Message can be empty")
        void messageCanBeEmpty() {
            BusinessRuleViolationException ex = new BusinessRuleViolationException("");

            assertEquals("", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("InvalidStatusTransitionException tests")
    class InvalidStatusTransitionExceptionTests {

        @Test
        @DisplayName("Creates exception with from and to status")
        void createsWithStatuses() {
            InvalidStatusTransitionException ex = new InvalidStatusTransitionException(
                    org.sifu.entities.OrderStatus.PENDING,
                    org.sifu.entities.OrderStatus.SHIPPED
            );

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("PENDING"));
            assertTrue(ex.getMessage().contains("SHIPPED"));
        }

        @Test
        @DisplayName("Includes both statuses in message")
        void messageIncludesStatuses() {
            InvalidStatusTransitionException ex = new InvalidStatusTransitionException(
                    org.sifu.entities.OrderStatus.CONFIRMED,
                    org.sifu.entities.OrderStatus.CANCELLED
            );

            assertTrue(ex.getMessage().contains("CONFIRMED"));
            assertTrue(ex.getMessage().contains("CANCELLED"));
            assertTrue(ex.getMessage().contains("transition"));
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(InvalidStatusTransitionException.class));
        }
    }

    @Nested
    @DisplayName("InsufficientStockException tests")
    class InsufficientStockExceptionTests {

        @Test
        @DisplayName("Creates with details list")
        void createsWithDetails() {
            java.util.List<String> details = java.util.List.of("Product A: requested 10, available 5");
            InsufficientStockException ex = new InsufficientStockException(details);

            assertEquals(ErrorMessages.INSUFFICIENT_STOCK, ex.getMessage());
            assertEquals(details, ex.getDetails());
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(InsufficientStockException.class));
        }
    }

    @Nested
    @DisplayName("InvalidPathParameterException tests")
    class InvalidPathParameterExceptionTests {

        @Test
        @DisplayName("Creates with all parameters")
        void createsWithAllParams() {
            InvalidPathParameterException ex = new InvalidPathParameterException(
                    "id", "invalid-uuid", "UUID format"
            );

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("id"));
            assertTrue(ex.getMessage().contains("invalid-uuid"));
            assertEquals("id", ex.getParameter());
            assertEquals("invalid-uuid", ex.getValue());
            assertEquals("UUID format", ex.getExpectedFormat());
        }

        @Test
        @DisplayName("Message contains all values")
        void messageContainsAllValues() {
            InvalidPathParameterException ex = new InvalidPathParameterException(
                    "orderId", "not-a-uuid", "UUID"
            );

            assertTrue(ex.getMessage().contains("orderId"));
            assertTrue(ex.getMessage().contains("not-a-uuid"));
        }

        @Test
        @DisplayName("Extends RuntimeException")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(InvalidPathParameterException.class));
        }
    }

    @Nested
    @DisplayName("ErrorMessages tests")
    class ErrorMessagesTests {

        @Test
        @DisplayName("ORDER_NOT_FOUND contains placeholder")
        void orderNotFoundContainsPlaceholder() {
            assertNotNull(ErrorMessages.ORDER_NOT_FOUND);
            assertTrue(ErrorMessages.ORDER_NOT_FOUND.contains("%s"));
        }

        @Test
        @DisplayName("PRODUCT_NOT_FOUND contains placeholder")
        void productNotFoundContainsPlaceholder() {
            assertNotNull(ErrorMessages.PRODUCT_NOT_FOUND);
            assertTrue(ErrorMessages.PRODUCT_NOT_FOUND.contains("%s"));
        }

        @Test
        @DisplayName("Error messages are not empty")
        void messagesAreNotEmpty() {
            assertFalse(ErrorMessages.REQUEST_BODY_REQUIRED.isEmpty());
            assertFalse(ErrorMessages.INVALID_REQUEST_DATA.isEmpty());
            assertFalse(ErrorMessages.CANNOT_CONFIRM.isEmpty());
            assertFalse(ErrorMessages.CANNOT_CANCEL_SHIPPED_OR_DELIVERED.isEmpty());
            assertFalse(ErrorMessages.CANNOT_DELETE_DELETED.isEmpty());
            assertFalse(ErrorMessages.CANNOT_ADD_ITEMS.isEmpty());
            assertFalse(ErrorMessages.INSUFFICIENT_STOCK.isEmpty());
        }
    }
}