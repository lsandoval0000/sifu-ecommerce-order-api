package org.sifu.exceptions;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when there is insufficient stock to fulfill an order.
 */
@Getter
public class InsufficientStockException extends RuntimeException {

    private final List<String> details;

    public InsufficientStockException(List<String> details) {
        super(ErrorMessages.INSUFFICIENT_STOCK);
        this.details = details;
    }
}