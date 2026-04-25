package org.sifu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.sifu.avro.OrderEvent;
import org.sifu.dto.CreateOrderRequest;
import org.sifu.dto.OrderDTO;
import org.sifu.entities.Order;
import org.sifu.entities.OrderStatus;

import java.math.BigDecimal;

/**
 * MapStruct mapper for converting between Order entities and OrderDTOs.
 */
@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "cdi")
public interface OrderMapper {

    /**
     * Converts an Order entity to an OrderDTO.
     * @param entity the order entity to convert
     * @return the order DTO
     */
    @Mapping(target = "orderItems", ignore = true)
    OrderDTO toDTO(Order entity);
    
    /**
     * Converts an OrderDTO to an OrderEvent for Kafka publishing.
     * @param dto the order DTO to convert
     * @return the order event
     */
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customerName", source = "customerName")
    @Mapping(target = "customerEmail", source = "customerEmail")
    @Mapping(target = "subTotalAmount", source = "subTotalAmount", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "totalAmount", source = "totalAmount", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "itf", source = "itf", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "orderStatus", source = "orderStatus", qualifiedByName = "enumToString")
    OrderEvent toOrderEventFromDto(OrderDTO dto);

    /**
     * Converts a CreateOrderRequest to an Order entity.
     * @param request the create order request
     * @return the order entity with default values initialized
     */
    default Order toEntity(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setIgv(BigDecimal.ZERO);
        order.setSubTotalAmount(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItf(BigDecimal.ZERO);
        order.setOrderStatus(OrderStatus.PENDING);
        return order;
    }

    /**
     * Converts a BigDecimal to its string representation.
     * @param value the BigDecimal value
     * @return the string representation, or null if value is null
     */
    @Named("bigDecimalToString")
    default String bigDecimalToString(BigDecimal value) {
        return value != null ? value.toString() : null;
    }

    /**
     * Converts an OrderStatus enum to its string name.
     * @param value the order status enum
     * @return the enum name, or null if value is null
     */
    @Named("enumToString")
    default String enumToString(OrderStatus value) {
        return value != null ? value.name() : null;
    }
}