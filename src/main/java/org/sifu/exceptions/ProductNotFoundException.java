package org.sifu.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a product is not found.
 */
@Getter
public class ProductNotFoundException extends RuntimeException {

    private final Object id;

    public ProductNotFoundException(Object id) {
        super(String.format(ErrorMessages.PRODUCT_NOT_FOUND, id));
        this.id = id;
    }
}