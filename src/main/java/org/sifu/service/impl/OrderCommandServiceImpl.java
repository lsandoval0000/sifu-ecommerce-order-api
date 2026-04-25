package org.sifu.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.sifu.domain.OrderStateMachine;
import org.sifu.domain.StockManager;
import org.sifu.dto.ChangeOrderStatusRequest;
import org.sifu.dto.CreateOrderRequest;
import org.sifu.dto.OrderDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderItem;
import org.sifu.entities.OrderStatus;
import org.sifu.entities.Product;
import org.sifu.exceptions.BusinessRuleViolationException;
import org.sifu.exceptions.ErrorMessages;
import org.sifu.exceptions.OrderNotFoundException;
import org.sifu.mapper.OrderMapper;
import org.sifu.repository.OrderRepository;
import org.sifu.service.OrderCommandService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Command Service Implementation
 * Provides methods for creating, deleting, confirming, canceling, and changing order status.
 */
@ApplicationScoped
public class OrderCommandServiceImpl implements OrderCommandService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderMapper orderMapper;

    @Inject
    OrderStateMachine stateMachine;

    @Inject
    StockManager stockManager;

    /**
     * Creates a new order with default values.
     * @param request the order creation request
     * @return the created order DTO
     * @throws BusinessRuleViolationException if the request is invalid
     */
    @Override
    @Transactional
    public OrderDTO create(CreateOrderRequest request) {
        if (request == null) {
            throw new BusinessRuleViolationException(ErrorMessages.REQUEST_BODY_REQUIRED);
        }
        Order order = orderMapper.toEntity(request);
        if (order == null) {
            throw new BusinessRuleViolationException(ErrorMessages.INVALID_REQUEST_DATA);
        }
        order.setOrderItems(new ArrayList<>());
        orderRepository.persist(order);
        return orderMapper.toDTO(order);
    }

    /**
     * Deletes an order by marking it as DELETED.
     * Only pending orders can be deleted.
     * @param id the order ID
     * @throws OrderNotFoundException if the order does not exist
     * @throws BusinessRuleViolationException if the order cannot be deleted
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        Order order = orderRepository.findById(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        if (!stateMachine.canDelete(order.getOrderStatus())) {
            throw new BusinessRuleViolationException(ErrorMessages.CANNOT_DELETE_DELETED);
        }
        order.setOrderStatus(OrderStatus.DELETED);
        orderRepository.persist(order);
    }

    /**
     * Confirms an order, validating and reserving stock for the products.
     * @param id the order ID
     * @return the confirmed order DTO
     * @throws OrderNotFoundException if the order does not exist
     * @throws BusinessRuleViolationException if the order cannot be confirmed
     * @throws InsufficientStockException if there is insufficient stock for any item
     */
    @Override
    @Transactional
    public OrderDTO confirm(UUID id) {
        Order order = orderRepository.findByIdWithItems(id);

        if (order == null) {
            throw new OrderNotFoundException(id);
        }

        if (!stateMachine.canConfirm(order.getOrderStatus())) {
            throw new BusinessRuleViolationException(ErrorMessages.CANNOT_CONFIRM);
        }

        List<StockManager.StockItem> stockItems = buildStockItems(order.getOrderItems());

        stockManager.validateStock(stockItems);
        stockManager.reserveStock(stockItems);

        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.persist(order);
        
        return orderMapper.toDTO(order);
    }

    /**
     * Cancels an order, releasing stock if it was previously confirmed.
     * @param id the order ID
     * @return the cancelled order DTO
     * @throws OrderNotFoundException if the order does not exist
     * @throws BusinessRuleViolationException if the order cannot be cancelled
     */
    @Override
    @Transactional
    public OrderDTO cancel(UUID id) {
        Order order = orderRepository.findByIdWithItems(id);

        if (order == null) {
            throw new OrderNotFoundException(id);
        }

        if (!stateMachine.canCancel(order.getOrderStatus())) {
            throw new BusinessRuleViolationException(ErrorMessages.CANNOT_CANCEL_SHIPPED_OR_DELIVERED);
        }

        List<StockManager.StockItem> stockItems = buildStockItems(order.getOrderItems());

        if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
            stockManager.releaseStock(stockItems);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.persist(order);

        return orderMapper.toDTO(order);
    }

    /**
     * Changes the status of an order.
     * Validates the status transition using the state machine.
     * @param id the order ID
     * @param request the request containing the new status
     * @return the order with updated status as a DTO
     * @throws OrderNotFoundException if the order does not exist
     * @throws InvalidStatusTransitionException if the status transition is invalid
     */
    @Override
    @Transactional
    public OrderDTO changeStatus(UUID id, ChangeOrderStatusRequest request) {
        Order order = orderRepository.findById(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }

        OrderStatus newStatus = request.getStatus();
        OrderStatus currentStatus = order.getOrderStatus();

        stateMachine.transition(currentStatus, newStatus);

        order.setOrderStatus(newStatus);
        orderRepository.persist(order);

        return orderMapper.toDTO(order);
    }

    /**
     * Builds a list of StockItems from order items.
     * @param orderItems the order items to convert
     * @return list of StockManager.StockItem
     */
    private List<StockManager.StockItem> buildStockItems(List<OrderItem> orderItems) {
        List<StockManager.StockItem> stockItems = new ArrayList<>();
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                if (product != null) {
                    stockItems.add(new StockManager.StockItem(
                            product.getId(),
                            item.getQuantity()
                    ));
                }
            }
        }
        return stockItems;
    }
}