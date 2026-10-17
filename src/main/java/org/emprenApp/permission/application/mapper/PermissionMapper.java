package org.emprenApp.permission.application.mapper;

import org.emprenApp.permission.application.dto.PermissionDTO;
import org.emprenApp.permission.domain.Permission;
import org.emprenApp.permission.infrastructure.request.PermissionCreateRequest;
import org.emprenApp.permission.infrastructure.request.PermissionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

// a null field in the request does NOT overwrite the current value of the entity.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    PermissionDTO toDTO(Permission permission);

    Permission toEntity(PermissionDTO permissionDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Permission toEntity(PermissionCreateRequest request);

    // only copies the non-null fields of the request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(PermissionUpdateRequest request, @MappingTarget Permission permission);
}
