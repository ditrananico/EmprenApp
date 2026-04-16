package org.emprenApp.producto.application;

import org.emprenApp.producto.application.dto.ProductDTO;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoAdapter {
    ProductDTO createProducto(ProductCreateRequest request) throws GenericException;
    ProductDTO getProductoByID(Long id) throws GenericException, NotFoundException;
    ProductDTO updateProducto(ProductUpdateRequest request) throws GenericException, NotFoundException;
    String deleteProducto(Long id) throws GenericException, NotFoundException;
    String deleteProductoLogical(Long id) throws GenericException, NotFoundException;
    // Agregamos paginación por categoría como se solicitó
    Page<ProductDTO> getProductosByCategoria(Long categoryId, Pageable pageable) throws GenericException;
}
