package org.sifu.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sifu.dto.OrderItemDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderItem;
import org.sifu.entities.Product;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderItemMapper
 */
class OrderItemMapperTest {

    private OrderItemMapper orderItemMapper = new org.sifu.mapper.OrderItemMapperImpl();

    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setName("Test Product");

        testOrder = new Order();
        testOrder.setId(orderId);

        testOrderItem = new OrderItem();
        testOrderItem.setProduct(testProduct);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setOrder(testOrder);
    }

    @Nested
    @DisplayName("toDTO method tests")
    class ToDTOMethod {

        @Test
        @DisplayName("Maps OrderItem to OrderItemDTO correctly")
        void toDTO_mapsCorrectly() {
            // Given
            testOrderItem.setQuantity(3);
            testOrderItem.setUnitPrice(BigDecimal.valueOf(99.99));

            // When
            OrderItemDTO result = orderItemMapper.toDTO(testOrderItem);

            // Then
            assertNotNull(result);
            assertEquals(productId.toString(), result.getProductId());
            assertEquals("Test Product", result.getProductName());
            assertEquals(3, result.getQuantity());
            assertEquals(BigDecimal.valueOf(99.99), result.getUnitPrice());
        }

        @Test
        @DisplayName("Calculates subtotal correctly")
        void toDTO_calculatesSubtotal() {
            // Given
            testOrderItem.setQuantity(2);
            testOrderItem.setUnitPrice(BigDecimal.valueOf(50.00));

            // When
            OrderItemDTO result = orderItemMapper.toDTO(testOrderItem);

            // Then
            assertEquals(BigDecimal.valueOf(100.00), result.getSubtotal());
        }

        @Test
        @DisplayName("Handles null unit price")
        void toDTO_nullUnitPrice() {
            // Given
            testOrderItem.setQuantity(1);
            testOrderItem.setUnitPrice(null);

            // When
            OrderItemDTO result = orderItemMapper.toDTO(testOrderItem);

            // Then
            assertNotNull(result);
            assertNull(result.getUnitPrice());
            assertNull(result.getSubtotal());
        }

        @Test
        @DisplayName("Handles null quantity")
        void toDTO_nullQuantity() {
            // Given
            testOrderItem.setQuantity(null);
            testOrderItem.setUnitPrice(BigDecimal.valueOf(50.00));

            // When
            OrderItemDTO result = orderItemMapper.toDTO(testOrderItem);

            // Then
            assertNotNull(result);
            assertNull(result.getQuantity());
            assertNull(result.getSubtotal());
        }

        @Test
        @DisplayName("Handles null OrderItem")
        void toDTO_nullOrderItem() {
            // When
            OrderItemDTO result = orderItemMapper.toDTO(null);

            // Then
            assertNull(result);
        }
    }
}