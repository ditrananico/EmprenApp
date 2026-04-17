package org.emprenApp.producto.application;

import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoAdapter {
    ProductoDTO createProducto(ProductCreateRequest request) throws GenericException;
    ProductoDTO getProductoByID(Long id) throws GenericException, NotFoundException;
    ProductoDTO updateProducto(ProductUpdateRequest request) throws GenericException, NotFoundException;
    String deleteProducto(Long id) throws GenericException, NotFoundException;
    String deleteProductoLogical(Long id) throws GenericException, NotFoundException;
    // Agregamos paginación por categoría como se solicitó
    Page<ProductoDTO> getProductosByCategoria(Long categoryId, Pageable pageable) throws GenericException;
}
