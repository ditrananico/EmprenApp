package org.emprenApp.producto.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.categoria.domain.Categoria;
import org.emprenApp.categoria.domain.CategoriaRepository;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.emprendimiento.domain.EmprendimientoRepository;
import org.emprenApp.producto.application.ProductoAdapter;
import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.producto.application.mapper.ProductoMapper;
import org.emprenApp.producto.domain.Producto;
import org.emprenApp.producto.domain.ProductoRepository;
import org.emprenApp.producto.infrastructure.request.ProductCreateRequest;
import org.emprenApp.producto.infrastructure.request.ProductUpdateRequest;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductoService implements ProductoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    CategoriaRepository categoriaRepository ;
    EmprendimientoRepository emprendimientoRepository;

    @Override
    @Transactional
    public ProductoDTO createProducto(ProductCreateRequest request) throws BaseException {
        try {
            // Validación de Rol / Seguridad
            ValidateGeneric.validateId(request.getCategoriaId());
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(NotFoundException::new);
            ValidateGeneric.validateId(request.getEmprendimientoId());
            Emprendimiento emprendimiento = emprendimientoRepository.findById(request.getEmprendimientoId()).orElseThrow(NotFoundException::new);

            Producto producto = ProductoMapper.INSTANCE.toEntity(request);

            producto.setCategoria(categoria);
            producto.setEmprendimiento(emprendimiento);
            // Manejo del Archivo / Foto del Producto

            Producto created = productoRepository.save(producto);
            logger.info("Producto creado: {}", created.getTitulo());
            return ProductoMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear producto", e);
            throw new GenericException();
        }
    }

    @Override
    public ProductoDTO getProductoByID(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return ProductoMapper.INSTANCE.toDTO(productoRepository.findById(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional
    public ProductoDTO updateProducto(ProductUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateId(request.getId());

            Producto producto = productoRepository.findById(request.getId()).orElseThrow(NotFoundException::new);

            if (request.getCategoriaId() != null) {
                if (producto.getCategoria() == null || !producto.getCategoria().getId().equals(request.getCategoriaId())) {
                    Categoria nuevaCategoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(NotFoundException::new); // O el código de error correspondiente
                    producto.setCategoria(nuevaCategoria);
                }
            }
            if (request.getTitulo() != null && !request.getTitulo().isBlank()) {
                producto.setTitulo(request.getTitulo());
            }

            if (request.getPrecio() != null) {
                if (request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BaseException(ErrorCodeEnum.INVALID_PRICE);
                }
                producto.setPrecio(request.getPrecio());
            }
            if (request.getStock() != null) {
                if (request.getStock() < 0) {
                    throw new BaseException(ErrorCodeEnum.INVALID_STOCK);
                }
                producto.setStock(request.getStock());
            }
            if (request.getDescripcion() != null) {
                producto.setDescripcion(request.getDescripcion());
            }
            if (request.getStockMinimo() != null) {
                if (request.getStockMinimo() < 0) {
                    throw new BaseException(ErrorCodeEnum.INVALID_STOCK);
                }
                producto.setStockMinimo(request.getStockMinimo());
            }

            Producto updated = productoRepository.save(producto);
            logger.info("Producto actualizado exitosamente: {}", updated.getId());
            return ProductoMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        }catch (Exception e) {
            logger.error("Error al actualizar producto", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deleteProducto(Long id)  throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Producto producto = productoRepository.findById(id).orElseThrow(NotFoundException::new);
            producto.setActive(false);
            productoRepository.save(producto);
            logger.info("Producto eliminado exitosamente ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar el producto con ID: {}", id, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> getProductosByCategoria(Long categoryId, Pageable pageable) throws BaseException {
       try{
           ValidateGeneric.validateId(categoryId);
           if (!categoriaRepository.existsById(categoryId)) {
               throw new NotFoundException();
           }
           Page<Producto> productosPage = productoRepository.findByCategoriaIdAndActiveTrue(categoryId, pageable);
           return productosPage.map(ProductoMapper.INSTANCE::toDTO);
       }catch (BaseException e) {
            throw e;
       }catch (Exception e) {
           logger.error("Error al obtener productos por categoría - ID: {}", categoryId, e);
           throw new GenericException();
       }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> getProductosByEmprendimiento(Long emprendimientoId, Pageable pageable) throws BaseException {
        try {
            ValidateGeneric.validateId(emprendimientoId);
            if (!emprendimientoRepository.existsById(emprendimientoId)) {
                throw new NotFoundException();
            }
            Page<Producto> productosPage = productoRepository.findByEmprendimientoIdAndActiveTrue(emprendimientoId, pageable);
            return productosPage.map(ProductoMapper.INSTANCE::toDTO);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener productos por emprendimiento - ID: {}", emprendimientoId, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> searchProductos(String query, Pageable pageable) throws BaseException {
        try {
            ValidateGeneric.validateNotBlank(query);
            Page<Producto> productosPage = productoRepository.searchProductosActivos(query, pageable);
            return productosPage.map(ProductoMapper.INSTANCE::toDTO);
        } catch (Exception e) {
            logger.error("Error crítico en el buscador de productos con la query: {}", query, e);
            throw new GenericException();
        }
    }
}
