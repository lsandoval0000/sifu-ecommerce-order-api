package org.sifu.service;

import org.sifu.dto.CreateProductRequest;
import org.sifu.dto.PageResponse;
import org.sifu.dto.ProductDTO;
import org.sifu.dto.UpdateProductRequest;

import java.util.UUID;

/**
 * Service interface for product operations.
 * This interface provides methods for creating, retrieving, updating, and deleting products with pagination support.
 */
public interface ProductService {

    /**
     * Retrieves all products with pagination
     * @param page the page number to retrieve
     * @param size the number of items per page
     * @return a paginated response containing products
     */
    PageResponse<ProductDTO> findAll(int page, int size);

    /**
     * Retrieves a product by ID
     * @param id the product ID to retrieve
     * @return the product with the specified ID
     */
    ProductDTO findById(UUID id);

    /**
     * Creates a new product
     * @param request the product creation request
     * @return the created product
     */
    ProductDTO create(CreateProductRequest request);

    /**
     * Updates a product
     * @param id the product ID to update
     * @param request the product update request
     * @return the updated product
     */
    ProductDTO update(UUID id, UpdateProductRequest request);

    /**
     * Deletes a product
     * @param id the product ID to delete
     */
    void delete(UUID id);
}