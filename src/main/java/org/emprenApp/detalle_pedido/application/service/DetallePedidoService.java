package org.emprenApp.detalle_pedido.application.service;

import org.emprenApp.detalle_pedido.application.DetallePedidoAdapter;
import org.emprenApp.detalle_pedido.application.mapper.DetallePedidoMapper;
import org.emprenApp.detalle_pedido.domain.DetallePedido;
import org.emprenApp.detalle_pedido.domain.DetallePedidoRepository;
import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoRequestItem;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.pedido.domain.Pedido;
import org.emprenApp.pedido.domain.PedidoRepository;
import org.emprenApp.producto.domain.Producto;
import org.emprenApp.producto.domain.ProductoRepository;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.emprenApp.shared.application.enums.ErrorCodeEnum.GENERIC_ERROR;


@Service
public class DetallePedidoService implements DetallePedidoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(DetallePedidoService.class);

    DetallePedidoRepository detallePedidoRepository;
    PedidoRepository pedidoRepository;
    ProductoRepository productoRepository;

    public DetallePedidoResponse getDetallePedidoByPedidoId(Long pedidoId) throws BaseException {
        try {
            logger.info("Obteniendo detalles para pedido ID: " + pedidoId);

            Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(NotFoundException::new);
            List<DetallePedido> detalles =  detallePedidoRepository.findByPedidoIdAndActivoTrue(pedido);
            if (detalles == null || detalles.isEmpty()) {
                throw new NotFoundException();
            }
            return DetallePedidoMapper.INSTANCE.toResponse(detalles);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener el detallePedido ID: {}", pedidoId, e);
            throw new GenericException();
        }
    }

    public List<DetallePedidoResponse> obtenerDetallesPorPedido(Long pedidoId) throws BaseException {
        //todo falta hacer
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public DetallePedidoResponse agregarDetallePedido(DetallePedidoAddRequest request) throws BaseException  {
        try {
            logger.info("Iniciando proceso para agregar un DetallePedido");

            ValidateGeneric.validateId(request.getPedidoId());
            Pedido pedido = pedidoRepository.findById(request.getPedidoId()).orElseThrow(NotFoundException::new);

            //  valida que el estado del pedido sea BORRADOR o ACEPTADO para poder guardar el DetallePedido
            if (!(EstadoPedidoEnum.BORRADOR.equals(pedido.getStatus()) || EstadoPedidoEnum.ACEPTADO.equals(pedido.getStatus()))) {
                logger.error(" El pedido con ID {} tiene un estado no valido para agregar detalles: {}", request.getPedidoId(), pedido.getStatus());
                throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
            }

            List<DetallePedido> detallesAGuardar = new ArrayList<>();
            Set<Long> productosProcesados = new HashSet<>();

            for (DetallePedidoRequestItem item : request.getItemsDetallePedido()) {

                // valida que el producto venga duplicado en distintas lineas del request
                if (productosProcesados.contains(item.getProductoId())) {
                    logger.error("El producto ID {} vino duplicado en el mismo request", item.getProductoId());
                    throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
                }

                ValidateGeneric.validateId(item.getProductoId());
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(NotFoundException::new);

                // valida control de stock de negocio
                if (producto.getStock() < item.getCantidad()) {
                    logger.warn("Stock insuficiente para el producto {}.", producto.getId());
                    throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
                }

                // se agrega ID al Set para marcarlo como verificado
                productosProcesados.add(item.getProductoId());

                DetallePedido detalle = new DetallePedido();
                detalle.setPedidoId(pedido);
                detalle.setProductoId(producto);
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setCantidad(item.getCantidad());
                detalle.setActivo(true);

                detallesAGuardar.add(detalle);
            }

            List<DetallePedido> detallesGuardados = detallePedidoRepository.saveAll(detallesAGuardar);
            logger.info("Se guardaron exitosamente {} detalles para el pedido ID: {}", detallesGuardados.size(), pedido.getId());

            return DetallePedidoMapper.INSTANCE.toResponse(detallesGuardados);
       } catch (Exception e) {
            logger.error("Error inesperado al agregar detalle de pedido", e);
            throw new GenericException(GENERIC_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminarDetalle(Long id) throws BaseException {
        try {
            logger.info("Inicio de borrado logico - DetallePedido");

            DetallePedido detalle = detallePedidoRepository.findById(id).orElseThrow(NotFoundException::new);

            if (!detalle.getActivo()) {
                throw new NotFoundException();
            }

            // validacion para eliminar Detalles de Pedidos que esten es estado BORRADOR y ACEPTADO
            Pedido pedido = detalle.getPedidoId();
            if (!(EstadoPedidoEnum.BORRADOR.equals(pedido.getStatus()) || EstadoPedidoEnum.ACEPTADO.equals(pedido.getStatus()))) {
                logger.error("No se puede eliminar el detalle. El pedido ID {} esta en estado: {}", pedido.getId(), pedido.getStatus());
                throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
            }

            //Controlar stock ?? o eso le corresponde al PedidoService
          //  Producto producto = detalle.getProductoId();
          //  producto.setStock(producto.getStock() + detalle.getCantidad());
           // productoRepository.save(producto);

            detalle.setActivo(false);
            detallePedidoRepository.save(detalle);
            logger.info("DetallePedido ID {} eliminado exitosamente ", id);

        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar el detallePedido ID: {}", id, e);
            throw new GenericException(GENERIC_ERROR);
        }
    }
}
