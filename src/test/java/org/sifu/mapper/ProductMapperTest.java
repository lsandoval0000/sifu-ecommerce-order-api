package org.sifu.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sifu.dto.CreateProductRequest;
import org.sifu.dto.ProductDTO;
import org.sifu.dto.UpdateProductRequest;
import org.sifu.entities.Product;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductMapper
 */
class ProductMapperTest {

    private ProductMapper productMapper = new org.sifu.mapper.ProductMapperImpl();

    private UUID productId;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);
        testProduct.setIsEnabled((short) 1);
    }

    @Nested
    @DisplayName("toDTO method tests")
    class ToDTOMethod {

        @Test
        @DisplayName("Maps Product to ProductDTO correctly")
        void toDTO_mapsCorrectly() {
            // When
            ProductDTO result = productMapper.toDTO(testProduct);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getId());
            assertEquals("Test Product", result.getName());
            assertEquals(BigDecimal.valueOf(99.99), result.getPrice());
            assertEquals(10, result.getStockQuantity());
        }

        @Test
        @DisplayName("Handles null Product")
        void toDTO_nullProduct() {
            // When
            ProductDTO result = productMapper.toDTO(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Maps all fields correctly")
        void toDTO_allFields() {
            // Given
            testProduct.setName("Full Product");
            testProduct.setPrice(BigDecimal.valueOf(299.99));
            testProduct.setStockQuantity(25);
            testProduct.setIsEnabled((short) 1);

            // When
            ProductDTO result = productMapper.toDTO(testProduct);

            // Then
            assertEquals("Full Product", result.getName());
            assertEquals(BigDecimal.valueOf(299.99), result.getPrice());
            assertEquals(25, result.getStockQuantity());
        }
    }

    @Nested
    @DisplayName("toEntity method tests")
    class ToEntityMethod {

        @Test
        @DisplayName("Maps CreateProductRequest to Product correctly")
        void toEntity_mapsCorrectly() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "New Product",
                    15,
                    BigDecimal.valueOf(49.99)
            );

            // When
            Product result = productMapper.toEntity(request);

            // Then
            assertNotNull(result);
            assertEquals("New Product", result.getName());
            assertEquals(15, result.getStockQuantity());
            assertEquals(BigDecimal.valueOf(49.99), result.getPrice());
        }

        @Test
        @DisplayName("Handles null request")
        void toEntity_nullRequest() {
            // When
            Product result = productMapper.toEntity(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Maps all fields correctly")
        void toEntity_allFields() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "Complete Product",
                    30,
                    BigDecimal.valueOf(199.99)
            );

            // When
            Product result = productMapper.toEntity(request);

            // Then
            assertEquals("Complete Product", result.getName());
            assertEquals(30, result.getStockQuantity());
            assertEquals(BigDecimal.valueOf(199.99), result.getPrice());
        }
    }

    @Nested
    @DisplayName("updateEntity method tests")
    class UpdateEntityMethod {

        @Test
        @DisplayName("Updates Product from UpdateProductRequest correctly")
        void updateEntity_mapsCorrectly() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest();
            request.setName("Updated Name");
            request.setStockQuantity(20);

            // When
            productMapper.updateEntity(request, testProduct);

            // Then
            assertEquals("Updated Name", testProduct.getName());
            assertEquals(20, testProduct.getStockQuantity());

            assertEquals(BigDecimal.valueOf(99.99), testProduct.getPrice());
        }

        @Test
        @DisplayName("Ignores null values in request")
        void updateEntity_ignoresNulls() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest();

            // When
            productMapper.updateEntity(request, testProduct);

            // Then
            // Original values should remain
            assertEquals("Test Product", testProduct.getName());
            assertEquals(10, testProduct.getStockQuantity());
            assertEquals(BigDecimal.valueOf(99.99), testProduct.getPrice());
        }

        @Test
        @DisplayName("Handles null request")
        void updateEntity_nullRequest() {
            // When
            productMapper.updateEntity(null, testProduct);

            // Then
            assertEquals("Test Product", testProduct.getName());
        }

        @Test
        @DisplayName("Throws exception when entity is null")
        void updateEntity_nullEntity_throwsException() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest();
            request.setName("Should Fail");

            // When/Then
            assertThrows(NullPointerException.class,
                    () -> productMapper.updateEntity(request, null));
        }

        @Test
        @DisplayName("Updates multiple fields")
        void updateEntity_multipleFields() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest();
            request.setName("Multi Updated");
            request.setStockQuantity(50);
            request.setPrice(BigDecimal.valueOf(149.99));

            // When
            productMapper.updateEntity(request, testProduct);

            // Then
            assertEquals("Multi Updated", testProduct.getName());
            assertEquals(50, testProduct.getStockQuantity());
            assertEquals(BigDecimal.valueOf(149.99), testProduct.getPrice());
        }
    }
}