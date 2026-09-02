package org.emprenApp.localidad.infrastructure.mapper;

import org.emprenApp.localidad.application.dto.LocalidadDTO;
import org.emprenApp.localidad.infrastructure.response.LocalidadResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LocalidadInfrastructureMapper {
    LocalidadInfrastructureMapper INSTANCE = Mappers.getMapper(LocalidadInfrastructureMapper.class);

    LocalidadResponse toResponse(LocalidadDTO localidadDTO);
}
