package org.sifu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sifu.dto.CreateProductRequest;
import org.sifu.dto.PageResponse;
import org.sifu.dto.ProductDTO;
import org.sifu.dto.UpdateProductRequest;
import org.sifu.entities.Product;
import org.sifu.exceptions.BusinessRuleViolationException;
import org.sifu.exceptions.ProductNotFoundException;
import org.sifu.mapper.ProductMapper;
import org.sifu.repository.ProductRepository;
import org.sifu.service.impl.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService implementations
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductMapper productMapper;

    @InjectMocks
    ProductServiceImpl productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private UUID productId;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStockQuantity(10);
        testProduct.setIsEnabled((short) 1);

        testProductDTO = new ProductDTO();
        testProductDTO.setId(productId);
        testProductDTO.setName("Test Product");
        testProductDTO.setPrice(BigDecimal.valueOf(99.99));
        testProductDTO.setStockQuantity(10);

        createRequest = new CreateProductRequest("New Product", 20, BigDecimal.valueOf(49.99));
    }

    @Nested
    @DisplayName("findAll method tests")
    class FindAllMethod {

        @Test
        @DisplayName("Returns paginated products")
        void findAll_withPagination_returnsPageResponse() {
            // Given
            List<Product> products = List.of(testProduct);
            long total = 1;
            when(productRepository.countActive()).thenReturn(total);
            when(productRepository.findAll(0, 20)).thenReturn(products);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            // When
            PageResponse<ProductDTO> result = productService.findAll(0, 20);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(0, result.getPage());
        }

        @Test
        @DisplayName("Calculates totalPages correctly")
        void findAll_calculatesTotalPagesCorrectly() {
            // Given
            List<Product> products = List.of(testProduct);
            long total = 25;
            when(productRepository.countActive()).thenReturn(total);
            when(productRepository.findAll(0, 10)).thenReturn(products);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            // When
            PageResponse<ProductDTO> result = productService.findAll(0, 10);

            // Then
            assertEquals(3, result.getTotalPages());
        }

        @Test
        @DisplayName("Handles empty result")
        void findAll_emptyResult_returnsEmptyList() {
            // Given
            List<Product> emptyProducts = new ArrayList<>();
            long total = 0;
            when(productRepository.countActive()).thenReturn(total);
            when(productRepository.findAll(0, 20)).thenReturn(emptyProducts);

            // When
            PageResponse<ProductDTO> result = productService.findAll(0, 20);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findById method tests")
    class FindByIdMethod {

        @Test
        @DisplayName("Returns product when found and enabled")
        void findById_existingEnabledProduct_returnsProductDTO() {
            // Given
            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            // When
            ProductDTO result = productService.findById(productId);

            // Then
            assertNotNull(result);
            assertEquals(productId, result.getId());
            assertEquals("Test Product", result.getName());
        }

        @Test
        @DisplayName("Throws ProductNotFoundException when product not found")
        void findById_nonExistentProduct_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(null);

            assertThrows(ProductNotFoundException.class,
                    () -> productService.findById(nonExistentId));
        }

        @Test
        @DisplayName("Throws ProductNotFoundException when product is disabled")
        void findById_disabledProduct_throwsException() {
            testProduct.setIsEnabled((short) 0);
            when(productRepository.findById(productId)).thenReturn(testProduct);

            assertThrows(ProductNotFoundException.class,
                    () -> productService.findById(productId));
        }
    }

    @Nested
    @DisplayName("create method tests")
    class CreateMethod {

        @Test
        @DisplayName("Creates new product successfully")
        void create_withValidRequest_returnsProductDTO() {
            // Given
            when(productRepository.findActiveByName("New Product")).thenReturn(Optional.empty());
            when(productMapper.toEntity(createRequest)).thenReturn(testProduct);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            // When
            ProductDTO result = productService.create(createRequest);

            // Then
            assertNotNull(result);
            assertEquals("Test Product", result.getName());
            verify(productRepository).persist(testProduct);
        }

        @Test
        @DisplayName("Throws exception when request is null")
        void create_nullRequest_throwsException() {
            assertThrows(BusinessRuleViolationException.class,
                    () -> productService.create(null));
        }

        @Test
        @DisplayName("Throws exception when product name already exists")
        void create_duplicateName_throwsException() {
            when(productRepository.findActiveByName("New Product"))
                    .thenReturn(Optional.of(testProduct));

            assertThrows(BusinessRuleViolationException.class,
                    () -> productService.create(createRequest));
        }

        @Test
        @DisplayName("Throws exception when mapper returns null")
        void create_mapperReturnsNull_throwsException() {
            when(productRepository.findActiveByName("New Product")).thenReturn(Optional.empty());
            when(productMapper.toEntity(createRequest)).thenReturn(null);

            assertThrows(BusinessRuleViolationException.class,
                    () -> productService.create(createRequest));
        }

        @Test
        @DisplayName("Sets isEnabled to 1 on new product")
        void create_setsIsEnabled() {
            testProduct.setIsEnabled((short) 0);
            when(productRepository.findActiveByName("New Product")).thenReturn(Optional.empty());
            when(productMapper.toEntity(createRequest)).thenReturn(testProduct);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            productService.create(createRequest);

            assertEquals((short) 1, testProduct.getIsEnabled());
        }
    }

    @Nested
    @DisplayName("update method tests")
    class UpdateMethod {

        @Test
        @DisplayName("Updates existing product successfully")
        void update_existingProduct_updatesSuccessfully() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest();
            request.setName("Updated Name");
            request.setStockQuantity(15);

            when(productRepository.findById(productId)).thenReturn(testProduct);
            when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

            // When
            ProductDTO result = productService.update(productId, request);

            // Then
            assertNotNull(result);
            verify(productMapper).updateEntity(request, testProduct);
            verify(productRepository).persist(testProduct);
        }

        @Test
        @DisplayName("Throws ProductNotFoundException when product not found")
        void update_nonExistentProduct_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest();
            when(productRepository.findById(nonExistentId)).thenReturn(null);

            assertThrows(ProductNotFoundException.class,
                    () -> productService.update(nonExistentId, request));
        }
    }

    @Nested
    @DisplayName("delete method tests")
    class DeleteMethod {

        @Test
        @DisplayName("Disables existing product successfully")
        void delete_existingProduct_disablesSuccessfully() {
            // Given
            when(productRepository.findById(productId)).thenReturn(testProduct);

            // When
            productService.delete(productId);

            // Then
            assertEquals((short) 0, testProduct.getIsEnabled());
            verify(productRepository).persist(testProduct);
        }

        @Test
        @DisplayName("Throws ProductNotFoundException when product not found")
        void delete_nonExistentProduct_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(null);

            assertThrows(ProductNotFoundException.class,
                    () -> productService.delete(nonExistentId));
        }

        @Test
        @DisplayName("Throws exception when product already disabled")
        void delete_alreadyDisabled_throwsException() {
            testProduct.setIsEnabled((short) 0);
            when(productRepository.findById(productId)).thenReturn(testProduct);

            assertThrows(BusinessRuleViolationException.class,
                    () -> productService.delete(productId));
        }
    }
}