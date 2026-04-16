package org.emprenApp.producto.infrastructure.mapper;

import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.infrastructure.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductoInfrastructureMapper {
    ProductoInfrastructureMapper INSTANCE = Mappers.getMapper(ProductoInfrastructureMapper.class);

    ProductResponse toResponse(ProductoDTO productDTO);
}
