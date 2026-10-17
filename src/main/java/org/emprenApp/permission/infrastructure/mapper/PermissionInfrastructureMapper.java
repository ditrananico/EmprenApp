package org.emprenApp.permission.infrastructure.mapper;

import org.emprenApp.permission.application.dto.PermissionDTO;
import org.emprenApp.permission.infrastructure.response.PermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PermissionInfrastructureMapper {
    PermissionInfrastructureMapper INSTANCE = Mappers.getMapper(PermissionInfrastructureMapper.class);

    PermissionResponse toResponse(PermissionDTO permissionDTO);
}
