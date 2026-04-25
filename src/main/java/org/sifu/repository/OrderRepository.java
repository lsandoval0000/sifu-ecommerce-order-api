package org.sifu.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.sifu.entities.Order;
import org.sifu.entities.OrderStatus;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Order entities using Panache.
 * Provides custom queries for finding active orders and orders with items.
 */
@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<Order, UUID> {

    /**
     * Finds all non-deleted orders with pagination.
     * @param page the page number
     * @param size the page size
     * @return list of non-deleted orders ordered by creation date descending
     */
    public List<Order> findAll(int page, int size) {
        return find("orderStatus != ?1 order by createdAt desc", OrderStatus.DELETED)
                .page(page, size)
                .list();
    }

    /**
     * Counts the number of non-deleted orders.
     * @return the count of active orders
     */
    public long countActive() {
        return count("orderStatus != ?1", OrderStatus.DELETED);
    }

    /**
     * Finds an order by ID with its items and products eagerly loaded.
     * @param id the order ID
     * @return the order with items and products, or null if not found
     */
    public Order findByIdWithItems(UUID id) {
        return find("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems i LEFT JOIN FETCH i.product WHERE o.id = ?1", id)
                .firstResult();
    }
}