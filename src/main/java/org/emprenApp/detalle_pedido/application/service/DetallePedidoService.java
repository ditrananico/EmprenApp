package org.emprenApp.detalle_pedido.application.service;

import org.emprenApp.detalle_pedido.application.DetallePedidoAdapter;
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
import java.util.stream.Collectors;

@Service
public class DetallePedidoService implements DetallePedidoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(DetallePedidoService.class);

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoService productoService;

    @Override
    public List<DetallePedidoResponse> obtenerDetallesPorPedido(Long pedidoId) {
        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedidoId);
        return detalles.stream().map(d -> DetallePedidoResponse.builder()
                .id(d.getId())
                .precioUnitario(d.getPrecioUnitario())
                .cantidad(d.getCantidad())
                .subtotal(d.getPrecioUnitario() * d.getCantidad())
                .build()).collect(Collectors.toList());
    }

    @Override
    public DetallePedidoResponse agregarDetalle(DetallePedidoAddRequest request) throws GenericException, NotFoundException {
        try {
            ProductoDTO producto = productoService.getProductoByID(request.getProductoId());
            if (producto.getStock() < request.getCantidad()) {
                throw new GenericException(); 
            }
            
            DetallePedido detalle = new DetallePedido();
            detalle.setCantidad(request.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio().doubleValue());
            
            detallePedidoRepository.save(detalle);
            
            return DetallePedidoResponse.builder()
                    .id(detalle.getId())
                    .precioUnitario(detalle.getPrecioUnitario())
                    .cantidad(detalle.getCantidad())
                    .subtotal(detalle.getPrecioUnitario() * detalle.getCantidad())
                    .build();
        } catch (Exception e) {
            logger.error("Error al agregar detalle", e);
            throw new GenericException();
        }
    }

    @Override
    public void eliminarDetalle(Long id) throws GenericException, NotFoundException {
        if (!detallePedidoRepository.existsById(id)) throw new NotFoundException();
        detallePedidoRepository.deleteById(id);
    }
}
