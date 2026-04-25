package org.sifu.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.sifu.dto.CreateProductRequest;
import org.sifu.dto.PageResponse;
import org.sifu.dto.ProductDTO;
import org.sifu.dto.UpdateProductRequest;
import org.sifu.entities.Product;
import org.sifu.exceptions.BusinessRuleViolationException;
import org.sifu.exceptions.ErrorMessages;
import org.sifu.exceptions.ProductNotFoundException;
import org.sifu.mapper.ProductMapper;
import org.sifu.repository.ProductRepository;
import org.sifu.service.ProductService;

import java.util.List;
import java.util.UUID;

/**
 * Product Service Implementation
 * Provides methods for managing products including CRUD operations.
 */
@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    /**
     * Retrieves all enabled products with pagination.
     * @param page the page number
     * @param size the page size
     * @return paginated response of enabled products
     */
    @Override
    public PageResponse<ProductDTO> findAll(int page, int size) {
        long total = productRepository.countActive();
        int totalPages = (int) Math.ceil((double) total / size);

        List<ProductDTO> content = productRepository.findAll(page, size).stream()
                .map(productMapper::toDTO)
                .toList();

        return PageResponse.<ProductDTO>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    /**
     * Retrieves an enabled product by ID.
     * @param id the product ID
     * @return the product DTO
     * @throws ProductNotFoundException if the product does not exist or is disabled
     */
    @Override
    public ProductDTO findById(UUID id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        if (product.getIsEnabled() == null || product.getIsEnabled() == 0) {
            throw new ProductNotFoundException(id);
        }
        return productMapper.toDTO(product);
    }

    /**
     * Creates a new product.
     * Validates that a product with the same name does not already exist.
     * @param request the product creation request
     * @return the created product DTO
     * @throws BusinessRuleViolationException if the request is invalid or product name already exists
     */
    @Override
    @Transactional
    public ProductDTO create(CreateProductRequest request) {
        if (request == null) {
            throw new BusinessRuleViolationException(ErrorMessages.REQUEST_BODY_REQUIRED);
        }

        var existingProduct = productRepository.findActiveByName(request.getName());
        if (existingProduct.isPresent()) {
            throw new BusinessRuleViolationException(
                String.format(ErrorMessages.PRODUCT_ALREADY_EXISTS, request.getName()));
        }
        
        Product product = productMapper.toEntity(request);
        if (product == null) {
            throw new BusinessRuleViolationException(ErrorMessages.INVALID_REQUEST_DATA);
        }
        product.setIsEnabled((short) 1);
        productRepository.persist(product);
        return productMapper.toDTO(product);
    }

    /**
     * Updates an existing product.
     * @param id the product ID
     * @param request the product update request
     * @return the updated product DTO
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    @Transactional
    public ProductDTO update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        productMapper.updateEntity(request, product);
        productRepository.persist(product);
        return productMapper.toDTO(product);
    }

    /**
     * Deletes (disables) a product by ID.
     * @param id the product ID
     * @throws ProductNotFoundException if the product does not exist or is already disabled
     * @throws BusinessRuleViolationException if the product is already disabled
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        if (product.getIsEnabled() == null || product.getIsEnabled() == 0) {
            throw new BusinessRuleViolationException(ErrorMessages.CANNOT_DISABLE_DISABLED);
        }
        product.setIsEnabled((short) 0);
        productRepository.persist(product);
    }
}