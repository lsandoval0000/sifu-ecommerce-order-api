package org.sifu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sifu.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for order information.
 * Contains order details including customer information, status, and items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private UUID id;
    private String customerName;
    private String customerEmail;
    private BigDecimal igv;
    private BigDecimal subTotalAmount;
    private BigDecimal totalAmount;
    private BigDecimal itf;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> orderItems;
}