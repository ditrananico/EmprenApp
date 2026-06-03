package org.emprenApp.publicacion.application.mapper;

import org.emprenApp.publicacion.application.dto.PublicacionDTO;
import org.emprenApp.publicacion.domain.Publicacion;
import org.emprenApp.publicacion.infrastructure.request.PublicacionCreateRequest;
import org.emprenApp.shared.application.enums.EstadoPublicacionEnum;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;

@Mapper
public interface PublicacionMapper {

    PublicacionMapper INSTANCE = Mappers.getMapper(PublicacionMapper.class);

    Publicacion toEntity(PublicacionDTO publicacionDTO);

    PublicacionDTO toDto(Publicacion publicacion);

    Page<PublicacionDTO> toPageDTO(Page<Publicacion> page, Pageable pageable);

    Publicacion toEntityFromRequest(PublicacionCreateRequest request);

    @AfterMapping
    default void setDefaultValues(@MappingTarget Publicacion publicacion) {
        if (publicacion.getFechaCreacion() == null) {
            publicacion.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        }
        if (publicacion.getBoost() == null) {
            publicacion.setBoost(0);
        }
        if (publicacion.getEstado() == null) {
            publicacion.setEstado(EstadoPublicacionEnum.ACTIVO);
        }
    }

}
