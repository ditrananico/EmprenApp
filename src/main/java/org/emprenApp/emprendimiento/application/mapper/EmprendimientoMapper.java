package org.emprenApp.emprendimiento.application.mapper;

import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.emprendimiento.domain.EmprendimientoRepository;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.shared.application.enums.EstadoEmprendimientoEnum;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface EmprendimientoMapper {

    EmprendimientoMapper INSTANCE = Mappers.getMapper(EmprendimientoMapper.class);

    Emprendimiento toEntity(EmprendimientoDTO emprendimientoDTO);

    EmprendimientoDTO toDTO(Emprendimiento emprendimiento);

    Page<EmprendimientoDTO> toPageDTO(Page<Emprendimiento> emprendimientos);

    @AfterMapping
    default void setDefaultValues(@MappingTarget Emprendimiento emprendimiento) {
        if (emprendimiento.getEstado() == null) emprendimiento.setEstado(EstadoEmprendimientoEnum.ACTIVO);
    }

    Emprendimiento toEntityFromRequest(EmprendimientoCreateRequest request);

}
