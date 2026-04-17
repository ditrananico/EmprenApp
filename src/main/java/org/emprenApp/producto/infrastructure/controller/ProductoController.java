package org.emprenApp.producto.infrastructure.controller;

import org.emprenApp.producto.application.ProductoAdapter;
import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.infrastructure.mapper.ProductoInfrastructureMapper;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.producto.infrastructure.response.ProductResponse;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/product")
public class ProductoController {

    private final static Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoAdapter productoAdapter;

    @PostMapping("/create")
    public ResponseEntity createProducto(@RequestBody @Validated ProductCreateRequest request) throws BaseException {
        try {
            logger.info("Creando producto: " + request.getTitulo());
            ProductoDTO created = productoAdapter.createProducto(request);
            return responseOk(ProductoInfrastructureMapper.INSTANCE.toResponse(created));
        } catch (GenericException e) {
            logger.error("Error al crear producto: ", e);
            return responseError(e);
        }
    }

    @GetMapping("/{id}")
    public ProductResponse getProducto(@PathVariable Long id) throws BaseException {
        ProductoDTO dto = productoAdapter.getProductoByID(id);
        return ProductoInfrastructureMapper.INSTANCE.toResponse(dto);
    }

    @PutMapping("/edit")
    public ResponseEntity<ProductResponse> updateProducto(@RequestBody @Validated ProductUpdateRequest request) throws BaseException {
        try {
            logger.info("Actualizando producto: " + request.getId());
            ProductoDTO updated = productoAdapter.updateProducto(request);
            return ResponseEntity.ok(ProductoInfrastructureMapper.INSTANCE.toResponse(updated));
        } catch (GenericException e) {
            logger.error("Error al actualizar producto: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProducto(@PathVariable Long id) throws BaseException {
        return ResponseEntity.ok(productoAdapter.deleteProducto(id));
    }

    @DeleteMapping("/delete/logical/{id}")
    public ResponseEntity<String> deleteProductoLogical(@PathVariable Long id) throws BaseException {
        return ResponseEntity.ok(productoAdapter.deleteProductoLogical(id));
    }

    private ResponseEntity<ProductResponse> responseOk(ProductResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ResponseEntity<String> responseError(GenericException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getError());
    }
}
