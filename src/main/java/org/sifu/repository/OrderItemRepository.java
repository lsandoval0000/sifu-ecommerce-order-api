package org.sifu.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.sifu.entities.OrderItem;

import java.util.UUID;

/**
 * Repository for OrderItem entities using Panache.
 */
@ApplicationScoped
public class OrderItemRepository implements PanacheRepositoryBase<OrderItem, UUID> {
}