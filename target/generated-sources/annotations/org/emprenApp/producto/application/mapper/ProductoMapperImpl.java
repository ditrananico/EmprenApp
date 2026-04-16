package org.emprenApp.producto.application.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.domain.Producto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-16T14:19:29-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
public class ProductoMapperImpl implements ProductoMapper {

    @Override
    public ProductoDTO toDTO(Producto producto) {
        if ( producto == null ) {
            return null;
        }

        ProductoDTO productoDTO = new ProductoDTO();

        productoDTO.setId( producto.getId() );
        productoDTO.setTitulo( producto.getTitulo() );
        productoDTO.setDescripcion( producto.getDescripcion() );
        productoDTO.setPrecio( producto.getPrecio() );
        productoDTO.setStock( producto.getStock() );
        productoDTO.setStockMinimo( producto.getStockMinimo() );
        productoDTO.setActive( producto.getActive() );

        return productoDTO;
    }

    @Override
    public Producto toEntity(ProductoDTO productDTO) {
        if ( productDTO == null ) {
            return null;
        }

        Producto producto = new Producto();

        producto.setId( productDTO.getId() );
        producto.setTitulo( productDTO.getTitulo() );
        producto.setDescripcion( productDTO.getDescripcion() );
        producto.setPrecio( productDTO.getPrecio() );
        producto.setStock( productDTO.getStock() );
        producto.setStockMinimo( productDTO.getStockMinimo() );
        producto.setActive( productDTO.getActive() );

        return producto;
    }
}
