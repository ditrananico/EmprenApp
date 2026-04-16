package org.emprenApp.producto.application.service;

import org.emprenApp.producto.application.ProductoAdapter;
import org.emprenApp.producto.application.dto.ProductDTO;
import org.emprenApp.producto.application.mapper.ProductoMapper;
import org.emprenApp.producto.domain.Producto;
import org.emprenApp.producto.domain.ProductoRepository;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductoService implements ProductoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public ProductDTO createProducto(ProductCreateRequest request) throws GenericException {
        try {
            if (request == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
            Producto producto = new Producto();
            producto.setTitulo(request.getTitulo());
            producto.setDescripcion(request.getDescripcion());
            producto.setPrecio(request.getPrecio());
            producto.setStock(request.getStock());
            producto.setStockMinimo(request.getStockMinimo());
            producto.setActive(true);
            Producto created = productoRepository.save(producto);
            logger.info("Producto creado: {}", created.getTitulo());
            return ProductoMapper.INSTANCE.toDTO(created);
        } catch (Exception e) {
            logger.error("Error al crear producto", e);
            throw new GenericException();
        }
    }

    @Override
    public ProductDTO getProductoByID(Long id) throws GenericException, NotFoundException {
        if (id == null || id < 0) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        return ProductoMapper.INSTANCE.toDTO(productoRepository.findById(id).orElseThrow(NotFoundException::new));
    }

    @Override
    public ProductDTO updateProducto(ProductUpdateRequest request) throws GenericException, NotFoundException {
        if (request == null || request.getId() == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        Producto producto = productoRepository.findById(request.getId()).orElseThrow(NotFoundException::new);
        producto.setTitulo(request.getTitulo());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setStockMinimo(request.getStockMinimo());
        productoRepository.save(producto);
        logger.info("Producto actualizado: {}", request.getId());
        return ProductoMapper.INSTANCE.toDTO(producto);
    }

    @Override
    public String deleteProducto(Long id) throws GenericException, NotFoundException {
        if (id == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        if (!productoRepository.existsById(id)) throw new NotFoundException();
        productoRepository.deleteById(id);
        return "Producto eliminado exitosamente";
    }

    @Override
    public String deleteProductoLogical(Long id) throws GenericException, NotFoundException {
        Producto producto = productoRepository.findById(id).orElseThrow(NotFoundException::new);
        producto.setActive(false);
        productoRepository.save(producto);
        return "Producto desactivado exitosamente";
    }

    @Override
    public Page<ProductDTO> getProductosByCategoria(Long categoryId, Pageable pageable) throws GenericException {
        return null; // A implementar cuando exista el repositorio
    }
}
