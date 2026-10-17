package org.emprenApp.role.infrastructure.mapper;

import org.emprenApp.role.application.dto.RoleDTO;
import org.emprenApp.role.infrastructure.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleInfrastructureMapper {
    RoleInfrastructureMapper INSTANCE = Mappers.getMapper(RoleInfrastructureMapper.class);

    RoleResponse toResponse(RoleDTO roleDTO);
}
