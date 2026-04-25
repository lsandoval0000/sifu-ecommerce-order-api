package org.sifu.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product.
 * Contains optional fields for product name, stock quantity, and price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;
    
    @Positive(message = "Stock quantity must be positive")
    private Integer stockQuantity;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
}