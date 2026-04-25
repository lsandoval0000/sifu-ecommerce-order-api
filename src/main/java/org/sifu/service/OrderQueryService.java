package org.sifu.service;

import org.sifu.dto.OrderDTO;
import org.sifu.dto.OrderItemDTO;
import org.sifu.dto.PageResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for order query operations.
 * This interface provides methods for retrieving orders and order items with pagination support.
 */
public interface OrderQueryService {

    /**
     * Retrieves all orders with pagination
     * @param page the page number
     * @param size the page size
     * @return paginated response of orders
     */
    PageResponse<OrderDTO> findAll(int page, int size);

    /**
     * Retrieves an order by ID
     * @param id the order ID
     * @return the order with the specified ID
     */
    OrderDTO findById(UUID id);

    /**
     * Retrieves the items of an order
     * @param orderId the order ID
     * @return list of order items
     */
    List<OrderItemDTO> getItems(UUID orderId);
}