package org.sifu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sifu.entities.OrderStatus;

/**
 * Request DTO for changing the status of an order.
 * Contains the new status to be applied to the order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;
}