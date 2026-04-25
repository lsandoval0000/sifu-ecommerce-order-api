package org.sifu.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.sifu.entities.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entities using Panache.
 * Provides custom queries for finding active products.
 */
@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, UUID> {

    /**
     * Finds all enabled products with pagination.
     * @param page the page number
     * @param size the page size
     * @return list of enabled products ordered by name
     */
    public List<Product> findAll(int page, int size) {
        return find("isEnabled = 1 order by name")
                .page(page, size)
                .list();
    }

    /**
     * Counts the number of enabled products.
     * @return the count of active products
     */
    public long countActive() {
        return count("isEnabled = 1");
    }

    /**
     * Finds an enabled product by name (case-insensitive).
     * @param name the product name to search for
     * @return an Optional containing the product if found and enabled
     */
    public Optional<Product> findActiveByName(String name) {
        return find("LOWER(name) = LOWER(?1) and isEnabled = 1", name).firstResultOptional();
    }
}