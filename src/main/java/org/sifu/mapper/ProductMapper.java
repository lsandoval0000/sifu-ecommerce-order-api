package org.sifu.mapper;

import org.sifu.dto.CreateProductRequest;
import org.sifu.dto.ProductDTO;
import org.sifu.dto.UpdateProductRequest;
import org.sifu.entities.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting between Product entities and ProductDTOs.
 */
@Mapper(componentModel = "cdi")
public interface ProductMapper {

    /**
     * Converts a Product entity to a ProductDTO.
     * @param entity the product entity to convert
     * @return the product DTO
     */
    ProductDTO toDTO(Product entity);

    /**
     * Converts a CreateProductRequest to a Product entity.
     * @param request the create product request
     * @return the product entity
     */
    Product toEntity(CreateProductRequest request);

    /**
     * Updates a Product entity from an UpdateProductRequest.
     * Null values in the request are ignored.
     * @param request the update product request
     * @param entity the product entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product entity);
}