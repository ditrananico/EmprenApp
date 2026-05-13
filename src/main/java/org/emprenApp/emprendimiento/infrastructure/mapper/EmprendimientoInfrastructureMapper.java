package org.emprenApp.emprendimiento.infrastructure.mapper;

import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.response.EmprendimientoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface EmprendimientoInfrastructureMapper {

    EmprendimientoInfrastructureMapper INSTANCE = Mappers.getMapper(EmprendimientoInfrastructureMapper.class);
    EmprendimientoResponse toResponse(EmprendimientoDTO emprendimientoDTO);

    default Page<EmprendimientoResponse> toResponse(Page<EmprendimientoDTO> emprendimientos) {
        return emprendimientos.map(this::toResponse);
    }

}
