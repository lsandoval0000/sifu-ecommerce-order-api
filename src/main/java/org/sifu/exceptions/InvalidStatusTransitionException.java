package org.sifu.exceptions;

import org.sifu.entities.OrderStatus;

/**
 * Exception thrown when an invalid order status transition is attempted.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format(ErrorMessages.INVALID_STATUS_TRANSITION, from, to));
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}