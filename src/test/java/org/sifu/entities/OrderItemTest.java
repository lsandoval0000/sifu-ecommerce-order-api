package org.sifu.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderItem entity
 */
class OrderItemTest {

    private OrderItem orderItem;
    private UUID orderItemId;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        orderItem = new OrderItem();
        orderItemId = UUID.randomUUID();
        product = new Product();
        product.setId(UUID.randomUUID());
        order = new Order();
        order.setId(UUID.randomUUID());
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("No args constructor creates instance with null fields")
        void noArgsConstructor() {
            assertNull(orderItem.getId());
            assertNull(orderItem.getProduct());
            assertNull(orderItem.getQuantity());
            assertNull(orderItem.getUnitPrice());
            assertNull(orderItem.getOrder());
        }

        @Test
        @DisplayName("All args constructor creates instance")
        void allArgsConstructor() {
            UUID id = UUID.randomUUID();
            Product prod = new Product();
            prod.setId(UUID.randomUUID());
            Order ord = new Order();
            ord.setId(UUID.randomUUID());

            OrderItem itemWithArgs = new OrderItem(
                    id,
                    prod,
                    5,
                    BigDecimal.valueOf(99.99),
                    ord
            );

            assertEquals(id, itemWithArgs.getId());
            assertEquals(prod, itemWithArgs.getProduct());
            assertEquals(5, itemWithArgs.getQuantity());
            assertEquals(BigDecimal.valueOf(99.99), itemWithArgs.getUnitPrice());
            assertEquals(ord, itemWithArgs.getOrder());
        }
    }

    @Nested
    @DisplayName("Field tests")
    class FieldTests {

        @Test
        @DisplayName("ID can be set and retrieved")
        void idField() {
            orderItem.setId(orderItemId);
            assertEquals(orderItemId, orderItem.getId());
        }

        @Test
        @DisplayName("Product can be set and retrieved")
        void productField() {
            orderItem.setProduct(product);
            assertEquals(product, orderItem.getProduct());
        }

        @Test
        @DisplayName("Quantity can be set and retrieved")
        void quantityField() {
            orderItem.setQuantity(10);
            assertEquals(10, orderItem.getQuantity());
        }

        @Test
        @DisplayName("Unit price can be set and retrieved")
        void unitPriceField() {
            BigDecimal price = BigDecimal.valueOf(149.99);
            orderItem.setUnitPrice(price);
            assertEquals(price, orderItem.getUnitPrice());
        }

        @Test
        @DisplayName("Order can be set and retrieved")
        void orderField() {
            orderItem.setOrder(order);
            assertEquals(order, orderItem.getOrder());
        }
    }

    @Nested
    @DisplayName("Business logic tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Quantity allows one")
        void minQuantity() {
            orderItem.setQuantity(1);
            assertEquals(1, orderItem.getQuantity());
        }

        @Test
        @DisplayName("Quantity allows large values")
        void largeQuantity() {
            orderItem.setQuantity(1000);
            assertEquals(1000, orderItem.getQuantity());
        }

        @Test
        @DisplayName("Unit price allows zero")
        void zeroPrice() {
            orderItem.setUnitPrice(BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO, orderItem.getUnitPrice());
        }

        @Test
        @DisplayName("Unit price allows decimals")
        void decimalPrice() {
            orderItem.setUnitPrice(new BigDecimal("0.01"));
            assertEquals(new BigDecimal("0.01"), orderItem.getUnitPrice());
        }

        @Test
        @DisplayName("Unit price allows large values")
        void largePrice() {
            orderItem.setUnitPrice(new BigDecimal("999999.99"));
            assertEquals(new BigDecimal("999999.99"), orderItem.getUnitPrice());
        }

        @Test
        @DisplayName("Product can be null")
        void nullProduct() {
            orderItem.setProduct(null);
            assertNull(orderItem.getProduct());
        }

        @Test
        @DisplayName("Order can be null")
        void nullOrder() {
            orderItem.setOrder(null);
            assertNull(orderItem.getOrder());
        }
    }

    @Nested
    @DisplayName("Calculation tests")
    class CalculationTests {

        @Test
        @DisplayName("Subtotal calculation: quantity * unitPrice")
        void subtotalCalculation() {
            orderItem.setQuantity(3);
            orderItem.setUnitPrice(BigDecimal.valueOf(50.00));

            BigDecimal expected = BigDecimal.valueOf(150.00);
            BigDecimal actual = orderItem.getUnitPrice().multiply(
                    BigDecimal.valueOf(orderItem.getQuantity())
            );

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("Subtotal with decimal precision")
        void subtotalDecimals() {
            orderItem.setQuantity(2);
            orderItem.setUnitPrice(new BigDecimal("19.99"));

            BigDecimal expected = new BigDecimal("39.98");
            BigDecimal actual = orderItem.getUnitPrice().multiply(
                    BigDecimal.valueOf(orderItem.getQuantity())
            );

            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("Subtotal with zero quantity")
        void subtotalZeroQuantity() {
            orderItem.setQuantity(0);
            orderItem.setUnitPrice(BigDecimal.valueOf(100.00));

            BigDecimal subtotal = BigDecimal.valueOf(0).multiply(
                    orderItem.getUnitPrice()
            );

            assertEquals(0, subtotal.compareTo(BigDecimal.ZERO));
        }
    }
}