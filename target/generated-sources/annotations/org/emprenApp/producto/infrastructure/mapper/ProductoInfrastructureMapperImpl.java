package org.emprenApp.producto.infrastructure.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.infrastructure.response.ProductResponse;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T19:47:38-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
public class ProductoInfrastructureMapperImpl implements ProductoInfrastructureMapper {

    @Override
    public ProductResponse toResponse(ProductoDTO productDTO) {
        if ( productDTO == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.id( productDTO.getId() );
        productResponse.titulo( productDTO.getTitulo() );
        productResponse.descripcion( productDTO.getDescripcion() );
        productResponse.precio( productDTO.getPrecio() );
        productResponse.stock( productDTO.getStock() );
        productResponse.stockMinimo( productDTO.getStockMinimo() );
        productResponse.categoriaId( productDTO.getCategoriaId() );
        productResponse.emprendimientoId( productDTO.getEmprendimientoId() );
        productResponse.active( productDTO.getActive() );

        return productResponse.build();
    }
}
