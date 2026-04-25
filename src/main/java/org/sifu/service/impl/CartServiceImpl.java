package org.sifu.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.sifu.clients.FinancialApiRestClient;
import org.sifu.clients.model.ItfModel;
import org.sifu.domain.OrderStateMachine;
import org.sifu.dto.AddItemRequest;
import org.sifu.dto.OrderItemDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderItem;
import org.sifu.entities.Product;
import org.sifu.exceptions.BusinessRuleViolationException;
import org.sifu.exceptions.ErrorMessages;
import org.sifu.exceptions.OrderItemNotFoundException;
import org.sifu.exceptions.OrderNotFoundException;
import org.sifu.exceptions.ProductNotFoundException;
import org.sifu.mapper.OrderItemMapper;
import org.sifu.repository.OrderItemRepository;
import org.sifu.repository.OrderRepository;
import org.sifu.repository.ProductRepository;
import org.sifu.service.CartService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.sifu.utils.GeneralConstants.IGV;
import static org.sifu.utils.GeneralConstants.NET_VALUE;

/**
 * Cart Service Implementation
 * Manages shopping cart operations including adding/removing items and recalculating totals.
 */
@ApplicationScoped
public class CartServiceImpl implements CartService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderItemRepository orderItemRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    OrderItemMapper orderItemMapper;

    @Inject
    OrderStateMachine stateMachine;

    @Inject
    @RestClient
    FinancialApiRestClient financialApiRestClient;

    /**
     * Retrieves all items from the shopping cart.
     * @param orderId the unique identifier of the order/cart
     * @return list of order items in the cart
     * @throws OrderNotFoundException if the order does not exist
     */
    @Override
    public List<OrderItemDTO> getItems(UUID orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return order.getOrderItems().stream()
                .map(orderItemMapper::toDTO)
                .toList();
    }

    /**
     * Adds an item to the shopping cart.
     * Validates that the order can be modified and the product exists and is enabled.
     * Recalculates the order total after adding the item.
     * @param orderId the unique identifier of the order/cart
     * @param request the request containing item details to add
     * @return the added order item as a DTO
     * @throws OrderNotFoundException if the order does not exist
     * @throws BusinessRuleViolationException if items cannot be added to the order
     * @throws ProductNotFoundException if the product does not exist or is disabled
     */
    @Override
    @Transactional
    public OrderItemDTO addItem(UUID orderId, AddItemRequest request) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        
        if (!stateMachine.canAddItems(order.getOrderStatus())) {
            throw new BusinessRuleViolationException(ErrorMessages.CANNOT_ADD_ITEMS);
        }

        Product product = productRepository.findById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException(request.getProductId());
        }
        
        if (product.getIsEnabled() == null || product.getIsEnabled() == 0) {
            throw new ProductNotFoundException(request.getProductId());
        }

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setOrder(order);

        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }
        order.getOrderItems().add(item);

        orderItemRepository.persist(item);
        recalculateTotal(order);
        
        orderRepository.persist(order);

        return orderItemMapper.toDTO(item);
    }

    /**
     * Removes an item from the shopping cart.
     * Recalculates the order total after removing the item.
     * @param orderId the unique identifier of the order/cart
     * @param itemId the unique identifier of the item to remove
     * @throws OrderNotFoundException if the order does not exist
     * @throws OrderItemNotFoundException if the item does not exist or does not belong to the order
     */
    @Override
    @Transactional
    public void removeItem(UUID orderId, UUID itemId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        OrderItem item = orderItemRepository.findById(itemId);
        if (item == null || !item.getOrder().getId().equals(orderId)) {
            throw new OrderItemNotFoundException(itemId);
        }

        order.getOrderItems().remove(item);
        orderItemRepository.delete(item);

        recalculateTotal(order);

        orderRepository.persist(order);
    }

    /**
     * Recalculates the order total including IGV and ITF taxes.
     * Fetches ITF from the external financial API.
     * @param order the order to recalculate totals for
     */
    private void recalculateTotal(Order order) {
        if (order.getOrderItems() == null) {
            order.setTotalAmount(java.math.BigDecimal.ZERO);
            return;
        }
        order.setTotalAmount(order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        
        order.setIgv((order.getTotalAmount().divide(NET_VALUE, 2, RoundingMode.HALF_UP)).multiply(IGV));
        order.setSubTotalAmount(order.getTotalAmount().subtract(order.getIgv()));
        
        try {
            ItfModel itf = financialApiRestClient.getItf(order.getTotalAmount().intValue());
            order.setItf(new BigDecimal(itf.getItf()));
        } catch (Exception e) {
            order.setItf(java.math.BigDecimal.ZERO);
        }
        order.setTotalAmount(order.getTotalAmount().add(order.getItf()));
    }
}