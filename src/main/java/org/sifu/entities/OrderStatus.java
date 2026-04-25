package org.sifu.entities;

/**
 * Enumeration representing the possible states of an order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    DELETED
}