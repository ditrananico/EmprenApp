package org.emprenApp.pedido.application.mapper;

import org.emprenApp.pedido.application.dto.PedidoDTO;
import org.emprenApp.pedido.domain.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface PedidoMapper {

    PedidoMapper INSTANCE = Mappers.getMapper(PedidoMapper.class);

    PedidoDTO toDTO(Pedido pedido);

    default Page<PedidoDTO> toPageDTO(Page<Pedido> page){
        return page.map(this::toDTO);
    }
}
