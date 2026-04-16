package org.emprenApp.producto.application.mapper;

import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.domain.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductoMapper {
    ProductoMapper INSTANCE = Mappers.getMapper(ProductoMapper.class);

    ProductoDTO toDTO(Producto producto);
    Producto toEntity(ProductoDTO productDTO);
}
