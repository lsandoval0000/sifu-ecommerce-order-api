package org.sifu.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.sifu.dto.OrderDTO;
import org.sifu.dto.OrderItemDTO;
import org.sifu.dto.PageResponse;
import org.sifu.entities.Order;
import org.sifu.exceptions.OrderNotFoundException;
import org.sifu.mapper.OrderItemMapper;
import org.sifu.mapper.OrderMapper;
import org.sifu.repository.OrderRepository;
import org.sifu.service.OrderQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Order Query Service Implementation
 * Provides methods for retrieving orders and order items with pagination.
 */
@ApplicationScoped
public class OrderQueryServiceImpl implements OrderQueryService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderMapper orderMapper;

    @Inject
    OrderItemMapper orderItemMapper;

    /**
     * Retrieves all orders with pagination.
     * Excludes deleted orders.
     * @param page the page number
     * @param size the page size
     * @return paginated response of orders
     */
    @Override
    public PageResponse<OrderDTO> findAll(int page, int size) {
        long total = orderRepository.countActive();
        int totalPages = (int) Math.ceil((double) total / size);

        List<OrderDTO> content = orderRepository.findAll(page, size).stream()
                .map(orderMapper::toDTO)
                .toList();

        return PageResponse.<OrderDTO>builder()
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
     * Retrieves an order by ID.
     * @param id the order ID
     * @return the order DTO
     * @throws OrderNotFoundException if the order does not exist
     */
    @Override
    public OrderDTO findById(UUID id) {
        Order order = orderRepository.findById(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return orderMapper.toDTO(order);
    }

    /**
     * Retrieves the items of an order.
     * @param orderId the order ID
     * @return list of order item DTOs
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
}