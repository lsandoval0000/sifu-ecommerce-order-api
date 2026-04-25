package org.sifu.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.sifu.entities.Product;
import org.sifu.exceptions.ErrorMessages;
import org.sifu.exceptions.InsufficientStockException;
import org.sifu.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages stock validation and reservation for orders.
 * Provides methods to validate stock availability, reserve stock, and release stock.
 */
@ApplicationScoped
public class StockManager {

    @Inject
    ProductRepository productRepository;

    /**
     * Validates that sufficient stock is available for all items.
     * @param items the list of stock items to validate
     * @throws InsufficientStockException if any item has insufficient stock
     */
    @Transactional
    public void validateStock(List<StockItem> items) {
        List<String> insufficientStock = new ArrayList<>();

        for (StockItem item : items) {
            Product product = productRepository.findById(item.productId());
            if (product == null) {
                continue;
            }
            if (product.getStockQuantity() < item.quantity()) {
                insufficientStock.add(String.format(ErrorMessages.STOCK_FORMAT,
                        product.getName(), item.quantity(), product.getStockQuantity()));
            }
        }

        if (!insufficientStock.isEmpty()) {
            throw new InsufficientStockException(insufficientStock);
        }
    }

    /**
     * Reserves stock for the specified items by reducing available quantities.
     * @param items the list of stock items to reserve
     */
    @Transactional
    public void reserveStock(List<StockItem> items) {
        for (StockItem item : items) {
            Product product = productRepository.findById(item.productId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() - item.quantity());
            }
        }
    }

    /**
     * Releases stock for the specified items by increasing available quantities.
     * @param items the list of stock items to release
     */
    @Transactional
    public void releaseStock(List<StockItem> items) {
        for (StockItem item : items) {
            Product product = productRepository.findById(item.productId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.quantity());
            }
        }
    }

    /**
     * Record representing a stock item with product ID and quantity.
     * @param productId the product ID
     * @param quantity the quantity
     */
    public record StockItem(UUID productId, int quantity) {}
}