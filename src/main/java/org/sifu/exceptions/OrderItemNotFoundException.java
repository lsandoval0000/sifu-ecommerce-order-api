package org.sifu.exceptions;

import lombok.Getter;

/**
 * Exception thrown when an order item is not found.
 */
@Getter
public class OrderItemNotFoundException extends RuntimeException {

    private final Object id;

    public OrderItemNotFoundException(Object id) {
        super(String.format(ErrorMessages.ORDER_ITEM_NOT_FOUND, id));
        this.id = id;
    }
}