package org.emprenApp.publicacion.infrastructure.mapper;

import org.emprenApp.publicacion.application.dto.PublicacionDTO;
import org.emprenApp.publicacion.infrastructure.response.PublicacionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface PublicacionInfrastructureMapper {

    PublicacionInfrastructureMapper INSTANCE = Mappers.getMapper(PublicacionInfrastructureMapper.class);

    PublicacionResponse toResponse(PublicacionDTO dto);

    default Page<PublicacionResponse> toResponse(Page<PublicacionDTO> publicaciones) {
        return publicaciones.map(this::toResponse);
    }

}
