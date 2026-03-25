package org.emprenApp.pedido.application.service;

import org.emprenApp.pedido.application.PedidoAdapter;
import org.emprenApp.pedido.application.dto.PedidoDTO;
import org.emprenApp.pedido.application.mapper.PedidoMapper;
import org.emprenApp.pedido.domain.Pedido;
import org.emprenApp.pedido.domain.PedidoRepository;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.user.application.UserAdapter;
import org.emprenApp.user.application.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class PedidoService implements PedidoAdapter {
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private PedidoRepository pedidoRepository;
    private UserAdapter  userService;

    @Override
    public Page<PedidoDTO> getAllPedidos(Pageable pageable) throws GenericException {
            return null;
    }

    @Override
    public PedidoDTO getPedidoByID(Long id) throws GenericException, NotFoundException {
        if (id == null || id < 0) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

        return PedidoMapper.INSTANCE.toDTO(pedidoRepository.findById(id)
                .orElseThrow(NotFoundException::new));
    }

    @Override
    public Page<PedidoDTO> getAllPedidoByUserIDAndStatus(Long userId, EstadoPedidoEnum status, Pageable pageable) throws GenericException, NotFoundException {

        //Para validar el usuario esta bien llamar al service o directamente al repository del User?
        userService.getUserByID(userId);
        Page<Pedido> pedidosPage = pedidoRepository.findByUserIdAndOptionalStatus(userId, status, pageable);

         return PedidoMapper.INSTANCE.toPageDTO(pedidosPage);
    }

    @Override
    public Page<PedidoDTO> getAllPedidoByEmprendimientoIDAndStatus(Long emprendimientoId,EstadoPedidoEnum status, Pageable pageable) throws GenericException, NotFoundException {

        //falta servicio getID del Emprendimiento
     //emprendimientoService.getEmprendimientoID(emprendimientoId);
       Page<Pedido> pedidosPage = pedidoRepository.findByEmprendimientoIdAndStatusOptional(emprendimientoId, status, pageable);

        return PedidoMapper.INSTANCE.toPageDTO(pedidosPage);
    }
}
