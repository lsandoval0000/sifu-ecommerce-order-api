package org.sifu.service;

import org.sifu.dto.AddItemRequest;
import org.sifu.dto.OrderItemDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for shopping cart operations.
 * This interface provides methods for managing items in a shopping cart, including retrieving, adding, and removing items.
 */
public interface CartService {

    /**
     * Retrieves all items from the shopping cart.
     * @param orderId the unique identifier of the order/cart
     * @return list of order items in the cart
     */
    List<OrderItemDTO> getItems(UUID orderId);

    /**
     * Adds an item to the shopping cart.
     * @param orderId the unique identifier of the order/cart
     * @param request the request containing item details to add
     * @return the added order item as a DTO
     */
    OrderItemDTO addItem(UUID orderId, AddItemRequest request);

    /**
     * Removes an item from the shopping cart.
     * @param orderId the unique identifier of the order/cart
     * @param itemId the unique identifier of the item to remove
     */
    void removeItem(UUID orderId, UUID itemId);
}