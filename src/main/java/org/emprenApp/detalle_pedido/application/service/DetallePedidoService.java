package org.emprenApp.detalle_pedido.application.service;

import jakarta.validation.ValidationException;
import org.emprenApp.detalle_pedido.application.DetallePedidoAdapter;
import org.emprenApp.detalle_pedido.application.mapper.DetallePedidoMapper;
import org.emprenApp.detalle_pedido.domain.DetallePedido;
import org.emprenApp.detalle_pedido.domain.DetallePedidoRepository;
import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.producto.application.service.ProductoService;
import org.emprenApp.producto.application.dto.ProductoDTO;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.emprenApp.shared.application.enums.ErrorCodeEnum.GENERIC_ERROR;


@Service
public class DetallePedidoService implements DetallePedidoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(DetallePedidoService.class);

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoService productoService;

    @Override
    public List<DetallePedidoResponse> obtenerDetallesPorPedido(Long pedidoId) throws GenericException {
        try {
            logger.info("Obteniendo detalles para pedido ID: " + pedidoId);
            List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedidoId);

            return DetallePedidoMapper.INSTANCE.toListResponse(detalles);
        } catch (Exception e) {
            logger.error("Error al obtener detalles por pedido ID: " + pedidoId, e);
            throw new GenericException();
        }
    }

    @Override
    public DetallePedidoResponse agregarDetalle(DetallePedidoAddRequest request) throws GenericException, NotFoundException {
        try {
            logger.info("Iniciando proceso para agregar detalle de producto ID: {} al pedido", request.getProductoId());
            ProductoDTO producto = productoService.getProductoByID(request.getProductoId());
            if (producto.getStock() < request.getCantidad()) {
                throw new ValidationException("Stock insuficiente para el producto: " + producto.getDescripcion());
            }
            
            DetallePedido detalle = new DetallePedido();
            detalle.setCantidad(request.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());

            //Falta la relacion con el Pedido

            DetallePedido detalleGuardado = detallePedidoRepository.save(detalle);
            logger.info("Detalle de pedido guardado exitosamente con ID: {}", detalleGuardado.getId());
            
            return DetallePedidoMapper.INSTANCE.toResponse(detalleGuardado);
        } catch (ValidationException e) {
            logger.error("Error de validación al agregar detalle: {}", e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            logger.error("No se encontró el producto con ID: {}", request.getProductoId());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al agregar detalle de pedido", e);
            throw new GenericException(GENERIC_ERROR);
        }
    }

    @Override
    public void eliminarDetalle(Long id) throws GenericException, NotFoundException {
        if (!detallePedidoRepository.existsById(id)) throw new NotFoundException();
        detallePedidoRepository.deleteById(id);
    }
}
