package org.sifu.mapper;

import org.sifu.dto.OrderItemDTO;
import org.sifu.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

/**
 * MapStruct mapper for converting between OrderItem entities and OrderItemDTOs.
 */
@Named("OrderItemMapper")
@Mapper(componentModel = "cdi")
public interface OrderItemMapper {

    /**
     * Converts an OrderItem entity to an OrderItemDTO.
     * @param item the order item entity to convert
     * @return the order item DTO with calculated subtotal
     */
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "subtotal", source = ".", qualifiedByName = "calculateSubtotal")
    OrderItemDTO toDTO(OrderItem item);

    /**
     * Calculates the subtotal for an order item.
     * @param item the order item
     * @return the subtotal (unit price * quantity), or null if item or required fields are null
     */
    @Named("calculateSubtotal")
    default BigDecimal calculateSubtotal(OrderItem item) {
        if (item == null || item.getUnitPrice() == null || item.getQuantity() == null) {
            return null;
        }
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}