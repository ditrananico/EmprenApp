package org.emprenApp.producto.application;

import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoAdapter {
    ProductoDTO createProducto(ProductCreateRequest request) throws BaseException;
    ProductoDTO getProductoByID(Long id) throws BaseException;
    ProductoDTO updateProducto(ProductUpdateRequest request) throws BaseException;
    String deleteProducto(Long id) throws BaseException;
    // Agregamos paginación por categoría como se solicitó
    Page<ProductoDTO> getProductosByCategoria(Long categoryId, Pageable pageable) throws BaseException;
}
