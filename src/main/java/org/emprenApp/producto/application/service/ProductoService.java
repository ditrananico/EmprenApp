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
import org.emprenApp.shared.application.enums.EstadoEmprendimientoEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService implements ProductoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private static final int BUSQUEDA_MAX = 100;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final EmprendimientoRepository emprendimientoRepository;
    private final ProductoValidationService productoValidationService;

    @Override
    @Transactional
    public ProductoDTO createProducto(ProductCreateRequest request) throws BaseException {
        try {
            // Validación de Rol / Seguridad

            productoValidationService.validarCamposProducto(
                    request.getTitulo(), request.getDescripcion(), request.getPrecio(),
                    request.getStock(), request.getStockMinimo());

            ValidateGeneric.validateId(request.getCategoriaId());
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(NotFoundException::new);
            ValidateGeneric.validateId(request.getEmprendimientoId());
            Emprendimiento emprendimiento = emprendimientoRepository.findById(request.getEmprendimientoId()).orElseThrow(NotFoundException::new);

            Producto producto = ProductoMapper.INSTANCE.toEntity(request);

            producto.setCategoria(categoria);
            producto.setEmprendimiento(emprendimiento);
            // Manejo del Archivo / Foto del Producto

            if (producto.getStock() == null)       producto.setStock(0);
            if (producto.getStockMinimo() == null) producto.setStockMinimo(0);

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
        return ProductoMapper.INSTANCE.toDTO(productoRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional
    public ProductoDTO updateProducto(ProductUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateId(request.getId());

            Producto producto = productoRepository.findById(request.getId()).orElseThrow(NotFoundException::new);

            productoValidationService.validarCamposProducto(
                    request.getTitulo(), request.getDescripcion(), request.getPrecio(),
                    request.getStock(), request.getStockMinimo());

            if (request.getCategoriaId() != null) {
                if (producto.getCategoria() == null || !producto.getCategoria().getId().equals(request.getCategoriaId())) {
                    Categoria nuevaCategoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(NotFoundException::new);
                    producto.setCategoria(nuevaCategoria);
                }
            }

            ProductoMapper.INSTANCE.updateEntityFromRequest(request, producto);

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
            if (emprendimientoRepository.findByIdAndEstado(emprendimientoId, EstadoEmprendimientoEnum.ACTIVO).isEmpty()) {
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
            //todo "validar ademas q no traiga un inyeccion de sql"  VER ESTO
            String termino = query.trim();
            ValidateGeneric.validateMaxLength(termino, BUSQUEDA_MAX);

            termino = escaparComodinesLike(termino);

            Page<Producto> productosPage = productoRepository.searchProductosActivos(termino, pageable);
            return productosPage.map(ProductoMapper.INSTANCE::toDTO);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error crítico en el buscador de productos con la query: {}", query, e);
            throw new GenericException();
        }
    }

    private String escaparComodinesLike(String valor) {
        return valor.replace("|", "||")
                    .replace("%", "|%")
                    .replace("_", "|_");
    }
}
