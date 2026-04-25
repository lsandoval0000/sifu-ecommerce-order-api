package org.sifu.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a product in the system.
 * Contains product details including name, stock quantity, price, and availability status.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "is_enabled")
    private Short isEnabled;
    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<OrderItem> orderItems;
}