package org.emprenApp.producto.application.mapper;

import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.domain.Producto;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

//nullValuePropertyMappingStrategy un campo null del request no pisa el valor actual de la entidad.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductoMapper {
    ProductoMapper INSTANCE = Mappers.getMapper(ProductoMapper.class);

    // sin estos mapeos categoriaId / emprendimientoId salían en null
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "emprendimientoId", source = "emprendimiento.id")
    ProductoDTO toDTO(Producto producto);

    Producto toEntity(ProductoDTO productDTO);

    @Mapping(target = "active", constant = "true")
    Producto toEntity(ProductCreateRequest request);


    // Solo copia los campos no nulos del request; las relaciones y el estado los maneja el service.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "emprendimiento", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Producto producto);

    // normaliza los espacios del título después de cualquier mapeo hacia Producto
    @AfterMapping
    default void limpiarBlancosEnElNombre(@MappingTarget Producto producto) {
        if (producto.getTitulo() != null) {
            producto.setTitulo(producto.getTitulo().trim().replaceAll("\\s+", " "));
        }
    }
}
