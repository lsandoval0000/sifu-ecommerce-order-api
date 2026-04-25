package org.sifu.exceptions;

import lombok.Getter;

/**
 * Exception thrown when an order is not found.
 */
@Getter
public class OrderNotFoundException extends RuntimeException {

    private final Object id;

    public OrderNotFoundException(Object id) {
        super(String.format(ErrorMessages.ORDER_NOT_FOUND, id));
        this.id = id;
    }
}