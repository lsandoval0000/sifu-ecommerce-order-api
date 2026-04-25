package org.sifu.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an order in the system.
 * Contains customer information, order status, and associated order items.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_email")
    private String customerEmail;
    @Column(name = "igv")
    private BigDecimal igv;
    @Column(name = "sub_total_amount")
    private BigDecimal subTotalAmount;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    @Column(name = "itf")
    private BigDecimal itf;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "orderStatus")
    private OrderStatus orderStatus;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}