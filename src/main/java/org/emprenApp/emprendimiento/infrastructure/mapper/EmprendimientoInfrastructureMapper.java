package org.emprenApp.emprendimiento.infrastructure.mapper;

import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.response.EmprendimientoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmprendimientoInfrastructureMapper {

    EmprendimientoInfrastructureMapper INSTANCE = Mappers.getMapper(EmprendimientoInfrastructureMapper.class);
    EmprendimientoResponse toResponse(EmprendimientoDTO emprendimientoDTO);

}
