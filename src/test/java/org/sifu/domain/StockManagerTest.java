package org.sifu.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sifu.entities.Product;
import org.sifu.exceptions.InsufficientStockException;
import org.sifu.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockManager
 */
@ExtendWith(MockitoExtension.class)
class StockManagerTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    StockManager stockManager;

    private UUID productId;
    private Product testProduct;
    private StockManager.StockItem stockItem;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);
        testProduct.setIsEnabled((short) 1);
        stockItem = new StockManager.StockItem(productId, 5);
    }

    @Nested
    @DisplayName("validateStock method tests")
    class ValidateStockMethod {

        @Test
        @DisplayName("Does not throw when stock is sufficient")
        void validateStock_sufficientStock_noException() {
            // Given
            when(productRepository.findById(productId)).thenReturn(testProduct);

            // When/Then - should not throw
            assertDoesNotThrow(() -> stockManager.validateStock(List.of(stockItem)));
        }

        @Test
        @DisplayName("Throws InsufficientStockException when stock is insufficient")
        void validateStock_insufficientStock_throwsException() {
            // Given
            testProduct.setStockQuantity(3); // requested 5, available 3
            when(productRepository.findById(productId)).thenReturn(testProduct);

            // When/Then
            InsufficientStockException exception = assertThrows(
                    InsufficientStockException.class,
                    () -> stockManager.validateStock(List.of(stockItem))
            );
            // Check details field contains the specific message
            assertNotNull(exception.getDetails());
            assertFalse(exception.getDetails().isEmpty());
            assertTrue(exception.getDetails().get(0).contains("requested"),
                    "Details should contain 'requested'");
        }

        @Test
        @DisplayName("Does not throw when product is not found")
        void validateStock_productNotFound_noException() {
            // Given
            when(productRepository.findById(productId)).thenReturn(null);

            // When/Then - should not throw even if product doesn't exist
            assertDoesNotThrow(() -> stockManager.validateStock(List.of(stockItem)));
        }

        @Test
        @DisplayName("Validates multiple items correctly")
        void validateStock_multipleItems_allValid() {
            // Given
            UUID productId2 = UUID.randomUUID();
            Product testProduct2 = new Product();
            testProduct2.setId(productId2);
            testProduct2.setName("Test Product 2");
            testProduct2.setStockQuantity(20);

            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(productRepository.findById(productId2)).thenReturn(testProduct2);

            // When/Then
            assertDoesNotThrow(() -> stockManager.validateStock(List.of(
                    stockItem,
                    new StockManager.StockItem(productId2, 5)
            )));
        }

        @Test
        @DisplayName("Throws when any item has insufficient stock")
        void validateStock_oneInsufficient_throwsException() {
            // Given
            UUID productId2 = UUID.randomUUID();
            Product testProduct2 = new Product();
            testProduct2.setId(productId2);
            testProduct2.setName("Test Product 2");
            testProduct2.setStockQuantity(1); // insufficient

            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(productRepository.findById(productId2)).thenReturn(testProduct2);

            // When/Then
            assertThrows(InsufficientStockException.class,
                    () -> stockManager.validateStock(List.of(
                            stockItem,
                            new StockManager.StockItem(productId2, 5)
                    )));
        }

        @Test
        @DisplayName("Throws with exact stock amount")
        void validateStock_exactStock_noException() {
            // Given - exact match: requested 5, available 5
            testProduct.setStockQuantity(5);
            when(productRepository.findById(productId)).thenReturn(testProduct);

            // When/Then
            assertDoesNotThrow(() -> stockManager.validateStock(List.of(stockItem)));
        }
    }

    @Nested
    @DisplayName("reserveStock method tests")
    class ReserveStockMethod {

        @Test
        @DisplayName("Reduces stock quantity when product exists")
        void reserveStock_existingProduct_reducesQuantity() {
            // Given
            when(productRepository.findById(productId)).thenReturn(testProduct);
            int originalStock = testProduct.getStockQuantity();

            // When
            stockManager.reserveStock(List.of(stockItem));

            // Then
            assertEquals(originalStock - 5, testProduct.getStockQuantity());
        }

        @Test
        @DisplayName("Does nothing when product is not found")
        void reserveStock_productNotFound_noChange() {
            // Given
            when(productRepository.findById(productId)).thenReturn(null);

            // When
            stockManager.reserveStock(List.of(stockItem));

            // Then - no exception, no changes
            verify(productRepository, never()).persist(any(Product.class));
        }

        @Test
        @DisplayName("Reserves multiple items")
        void reserveStock_multipleItems_reservesAll() {
            // Given
            UUID productId2 = UUID.randomUUID();
            Product testProduct2 = new Product();
            testProduct2.setId(productId2);
            testProduct2.setStockQuantity(15);

            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(productRepository.findById(productId2)).thenReturn(testProduct2);

            // When
            stockManager.reserveStock(List.of(
                    stockItem,
                    new StockManager.StockItem(productId2, 10)
            ));

            // Then
            assertEquals(5, testProduct.getStockQuantity()); // 10 - 5
            assertEquals(5, testProduct2.getStockQuantity()); // 15 - 10
        }
    }

    @Nested
    @DisplayName("releaseStock method tests")
    class ReleaseStockMethod {

        @Test
        @DisplayName("Increases stock quantity when product exists")
        void releaseStock_existingProduct_increasesQuantity() {
            // Given
            when(productRepository.findById(productId)).thenReturn(testProduct);
            int originalStock = testProduct.getStockQuantity();

            // When
            stockManager.releaseStock(List.of(stockItem));

            // Then
            assertEquals(originalStock + 5, testProduct.getStockQuantity());
        }

        @Test
        @DisplayName("Does nothing when product is not found")
        void releaseStock_productNotFound_noChange() {
            // Given
            when(productRepository.findById(productId)).thenReturn(null);

            // When
            stockManager.releaseStock(List.of(stockItem));

            // Then - no exception, no changes
            verify(productRepository, never()).persist(any(Product.class));
        }
    }

    @Nested
    @DisplayName("StockItem record tests")
    class StockItemRecord {

        @Test
        @DisplayName("StockItem stores productId and quantity correctly")
        void stockItem_storesValues() {
            // Given/When
            StockManager.StockItem item = new StockManager.StockItem(productId, 10);

            // Then
            assertEquals(productId, item.productId());
            assertEquals(10, item.quantity());
        }

        @Test
        @DisplayName("StockItem equals and hashCode work correctly")
        void stockItem_equalsAndHashCode() {
            // Given
            StockManager.StockItem item1 = new StockManager.StockItem(productId, 5);
            StockManager.StockItem item2 = new StockManager.StockItem(productId, 5);
            StockManager.StockItem item3 = new StockManager.StockItem(UUID.randomUUID(), 5);

            // Then
            assertEquals(item1, item2);
            assertNotEquals(item1, item3);
            assertEquals(item1.hashCode(), item2.hashCode());
        }
    }
}