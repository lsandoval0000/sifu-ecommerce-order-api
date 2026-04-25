package org.sifu.service;

import org.sifu.dto.ChangeOrderStatusRequest;
import org.sifu.dto.CreateOrderRequest;
import org.sifu.dto.OrderDTO;

import java.util.UUID;

/**
 * Service interface for order command operations.
 * This interface provides methods for creating, deleting, confirming, canceling, and changing the status of orders.
 */
public interface OrderCommandService {

    /**
     * Creates a new order.
     * @param request the order creation request containing order details
     * @return the created order as a DTO
     */
    OrderDTO create(CreateOrderRequest request);

    /**
     * Deletes an order by its ID.
     * @param id the unique identifier of the order to delete
     */
    void delete(UUID id);

    /**
     * Confirms an order, which reserves the stock for the products.
     * @param id the unique identifier of the order to confirm
     * @return the confirmed order as a DTO
     */
    OrderDTO confirm(UUID id);

    /**
     * Cancels an order, releasing stock if it was previously confirmed.
     * @param id the unique identifier of the order to cancel
     * @return the canceled order as a DTO
     */
    OrderDTO cancel(UUID id);

    /**
     * Changes the status of an order.
     * @param id the unique identifier of the order
     * @param request the request containing the new status
     * @return the order with updated status as a DTO
     */
    OrderDTO changeStatus(UUID id, ChangeOrderStatusRequest request);
}