package org.emprenApp.producto.infrastructure.controller;

import org.emprenApp.producto.application.ProductoAdapter;
import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.infrastructure.mapper.ProductoInfrastructureMapper;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.producto.infrastructure.response.ProductResponse;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/product")
public class ProductoController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoAdapter productoAdapter;

    @GetMapping("/categoria/{categoryId}")
    public ResponseEntity<Page<ProductoDTO>> getProductosByCategoria(@PathVariable Long categoryId, Pageable pageable) throws BaseException {
        Page<ProductoDTO> productos = productoAdapter.getProductosByCategoria(categoryId, pageable);
        return responseOk(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProducto(@PathVariable Long id) throws BaseException {
        ProductoDTO dto = productoAdapter.getProductoByID(id);
        return responseOk(ProductoInfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/emprendimiento/{emprendimientoId}")
    public ResponseEntity<Page<ProductoDTO>> getProductosByEmprendimiento(
            @PathVariable Long emprendimientoId,
            Pageable pageable) throws BaseException {
        return responseOk(productoAdapter.getProductosByEmprendimiento(emprendimientoId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductoDTO>> searchProductos(@RequestParam(required = false) String query, Pageable pageable) throws BaseException {
        return responseOk(productoAdapter.searchProductos(query, pageable));
    }

    @PostMapping("/")
    public ResponseEntity<ProductResponse> createProducto(@RequestBody @Validated ProductCreateRequest request) throws BaseException {
        logger.info("Creando producto: " + request.getTitulo());
        ProductoDTO created = productoAdapter.createProducto(request);
        return responseOk(ProductoInfrastructureMapper.INSTANCE.toResponse(created));
    }

    @PutMapping("/")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteProducto(@PathVariable Long id) throws BaseException {
        return responseOk(productoAdapter.deleteProducto(id));
    }

}