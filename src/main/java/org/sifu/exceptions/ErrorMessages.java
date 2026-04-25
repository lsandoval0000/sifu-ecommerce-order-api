package org.sifu.exceptions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized constants for error messages used throughout the application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessages {

    // ========== Product ==========
    public static final String PRODUCT_NOT_FOUND = "Product not found with id: %s";

    // ========== Order ==========
    public static final String ORDER_NOT_FOUND = "Order not found with id: %s";
    public static final String ORDER_ITEM_NOT_FOUND = "Order item not found with id: %s";

    // ========== Stock ==========
    public static final String INSUFFICIENT_STOCK = "Insufficient stock";
    public static final String STOCK_FORMAT = "Product '%s': requested %d, available %d";

    // ========== Status ==========
    public static final String INVALID_STATUS_TRANSITION = "Invalid status transition from %s to %s";
    public static final String CANNOT_CANCEL_SHIPPED_OR_DELIVERED = "Cannot cancel confirmed, shipped or delivered orders";
    public static final String CANNOT_CONFIRM = "Cannot confirm order in current status";
    public static final String CANNOT_ADD_ITEMS = "Cannot add items to order in current status";

    // ========== General ==========
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String UNEXPECTED_ERROR = "Unexpected error: ";

    // ========== Validation ==========
    public static final String INVALID_PATH_PARAM = "Invalid path parameter '%s': '%s'. Expected format: %s";
    public static final String INVALID_JSON = "Invalid JSON format";
    public static final String MISSING_REQUIRED_FIELD = "Missing required field: %s";
    public static final String REQUEST_BODY_REQUIRED = "Request body is required";
    public static final String INVALID_REQUEST_DATA = "Invalid request data";
    public static final String CANNOT_DELETE_DELETED = "Cannot delete an already deleted order";
    public static final String CANNOT_DISABLE_DISABLED = "Cannot disable an already disabled product";
    public static final String PRODUCT_ALREADY_EXISTS = "Product with name '%s' already exists and is active";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String INVALID_DATA = "Invalid data";
    public static final String INVALID_VALUE_FOR_FIELD = "Invalid value '%s' for field %s";
}