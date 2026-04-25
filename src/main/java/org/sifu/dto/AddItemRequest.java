package org.sifu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding an item to a shopping cart.
 * Contains the product ID and quantity to be added.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}