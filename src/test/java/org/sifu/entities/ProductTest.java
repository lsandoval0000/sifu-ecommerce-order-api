package org.sifu.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product entity
 */
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("No args constructor creates instance with null fields")
        void noArgsConstructor() {
            assertNull(product.getId());
            assertNull(product.getName());
            assertNull(product.getStockQuantity());
            assertNull(product.getPrice());
            assertNull(product.getIsEnabled());
            assertNull(product.getOrderItems());
        }

        @Test
        @DisplayName("All args constructor creates instance")
        void allArgsConstructor() {
            UUID id = UUID.randomUUID();
            Product productWithArgs = new Product(
                    id,
                    "Test Product",
                    10,
                    BigDecimal.valueOf(99.99),
                    (short) 1,
                    new ArrayList<>()
            );

            assertEquals(id, productWithArgs.getId());
            assertEquals("Test Product", productWithArgs.getName());
            assertEquals(10, productWithArgs.getStockQuantity());
            assertEquals(BigDecimal.valueOf(99.99), productWithArgs.getPrice());
            assertEquals((short) 1, productWithArgs.getIsEnabled());
        }
    }

    @Nested
    @DisplayName("Field tests")
    class FieldTests {

        @Test
        @DisplayName("ID can be set and retrieved")
        void idField() {
            UUID id = UUID.randomUUID();
            product.setId(id);
            assertEquals(id, product.getId());
        }

        @Test
        @DisplayName("Name can be set and retrieved")
        void nameField() {
            product.setName("Laptop Pro");
            assertEquals("Laptop Pro", product.getName());
        }

        @Test
        @DisplayName("Stock quantity can be set and retrieved")
        void stockQuantityField() {
            product.setStockQuantity(50);
            assertEquals(50, product.getStockQuantity());
        }

        @Test
        @DisplayName("Price can be set and retrieved")
        void priceField() {
            BigDecimal price = BigDecimal.valueOf(299.99);
            product.setPrice(price);
            assertEquals(price, product.getPrice());
        }

        @Test
        @DisplayName("Is enabled can be set and retrieved")
        void isEnabledField() {
            product.setIsEnabled((short) 1);
            assertEquals((short) 1, product.getIsEnabled());
        }

        @Test
        @DisplayName("Order items can be set and retrieved")
        void orderItemsField() {
            OrderItem item = new OrderItem();
            List<OrderItem> items = List.of(item);
            product.setOrderItems(items);
            assertEquals(items, product.getOrderItems());
            assertEquals(1, product.getOrderItems().size());
        }
    }

    @Nested
    @DisplayName("Business logic tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Stock quantity allows zero")
        void zeroStock() {
            product.setStockQuantity(0);
            assertEquals(0, product.getStockQuantity());
        }

        @Test
        @DisplayName("Stock quantity allows large values")
        void largeStock() {
            product.setStockQuantity(10000);
            assertEquals(10000, product.getStockQuantity());
        }

        @Test
        @DisplayName("Price allows zero")
        void zeroPrice() {
            product.setPrice(BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO, product.getPrice());
        }

        @Test
        @DisplayName("Price allows decimal precision")
        void decimalPrice() {
            product.setPrice(new BigDecimal("99.99"));
            assertEquals(new BigDecimal("99.99"), product.getPrice());
        }

        @Test
        @DisplayName("Is enabled true = 1")
        void enabledTrue() {
            product.setIsEnabled((short) 1);
            assertTrue(product.getIsEnabled() == 1);
        }

        @Test
        @DisplayName("Is enabled false = 0")
        void enabledFalse() {
            product.setIsEnabled((short) 0);
            assertTrue(product.getIsEnabled() == 0);
        }

        @Test
        @DisplayName("Order items list can be empty")
        void emptyOrderItems() {
            product.setOrderItems(new ArrayList<>());
            assertTrue(product.getOrderItems().isEmpty());
        }

        @Test
        @DisplayName("Order items list can be null")
        void nullOrderItems() {
            product.setOrderItems(null);
            assertNull(product.getOrderItems());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Name accepts unicode characters")
        void unicodeName() {
            product.setName("Тест Продукт");
            assertEquals("Тест Продукт", product.getName());
        }

        @Test
        @DisplayName("Name accepts special characters")
        void specialCharactersName() {
            product.setName("Product #@!$%");
            assertEquals("Product #@!$%", product.getName());
        }

        @Test
        @DisplayName("Name accepts empty string")
        void emptyName() {
            product.setName("");
            assertEquals("", product.getName());
        }

        @Test
        @DisplayName("Name accepts long string")
        void longName() {
            String longName = "A".repeat(255);
            product.setName(longName);
            assertEquals(255, product.getName().length());
        }
    }
}