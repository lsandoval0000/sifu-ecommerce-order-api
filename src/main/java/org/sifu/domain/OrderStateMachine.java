package org.sifu.domain;

import jakarta.enterprise.context.ApplicationScoped;
import org.sifu.entities.OrderStatus;
import org.sifu.exceptions.InvalidStatusTransitionException;

/**
 * State machine for managing order status transitions.
 * Validates and enforces valid transitions between order states.
 */
@ApplicationScoped
public class OrderStateMachine {

    /**
     * Validates if a status transition is allowed.
     * @param from the current status
     * @param to the target status
     * @return true if the transition is valid, false otherwise
     */
    public boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }

        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED || to == OrderStatus.DELETED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED || to == OrderStatus.DELETED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.DELETED;
            case SHIPPED -> to == OrderStatus.DELIVERED || to == OrderStatus.DELETED;
            case DELIVERED -> to == OrderStatus.REFUNDED || to == OrderStatus.DELETED;
            case CANCELLED, REFUNDED -> to == OrderStatus.DELETED;
            case DELETED -> false;
        };
    }

    /**
     * Validates and returns the new status, or throws an exception if invalid.
     * @param from the current status
     * @param to the target status
     * @return the new status if the transition is valid
     * @throws InvalidStatusTransitionException if the transition is invalid
     */
    public OrderStatus transition(OrderStatus from, OrderStatus to) {
        if (!isValidTransition(from, to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
        return to;
    }

    /**
     * Checks if the current status allows cancellation.
     * Only pending orders can be cancelled (confirmed orders have reserved stock).
     * @param current the current status
     * @return true if cancellation is allowed, false otherwise
     */
    public boolean canCancel(OrderStatus current) {
        return current == OrderStatus.PENDING;
    }

    /**
     * Checks if the current status allows confirmation.
     * @param current the current status
     * @return true if confirmation is allowed, false otherwise
     */
    public boolean canConfirm(OrderStatus current) {
        return current == OrderStatus.PENDING;
    }

    /**
     * Checks if the current status allows adding items.
     * @param current the current status
     * @return true if adding items is allowed, false otherwise
     */
    public boolean canAddItems(OrderStatus current) {
        return current == OrderStatus.PENDING;
    }

    /**
     * Checks if the current status allows order modification.
     * @param current the current status
     * @return true if modification is allowed, false otherwise
     */
    public boolean canModifyOrder(OrderStatus current) {
        return current != null && current != OrderStatus.DELETED && current != OrderStatus.CANCELLED && current != OrderStatus.REFUNDED && current != OrderStatus.SHIPPED && current != OrderStatus.DELIVERED;
    }

    /**
     * Checks if the current status allows deletion.
     * Only pending orders can be deleted (to maintain inventory consistency).
     * @param current the current status
     * @return true if deletion is allowed, false otherwise
     */
    public boolean canDelete(OrderStatus current) {
        return current == OrderStatus.PENDING;
    }
}