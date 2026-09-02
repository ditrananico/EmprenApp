package org.emprenApp.ubicacion.infrastructure.mapper;

import org.emprenApp.localidad.infrastructure.mapper.LocalidadInfrastructureMapper;
import org.emprenApp.ubicacion.application.dto.UbicacionDTO;
import org.emprenApp.ubicacion.infrastructure.response.UbicacionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = LocalidadInfrastructureMapper.class)
public interface UbicacionInfrastructureMapper {
    UbicacionInfrastructureMapper INSTANCE = Mappers.getMapper(UbicacionInfrastructureMapper.class);

    UbicacionResponse toResponse(UbicacionDTO ubicacionDTO);
}
