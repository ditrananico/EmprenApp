package org.emprenApp.role.application.mapper;

import org.emprenApp.permission.domain.Permission;
import org.emprenApp.role.application.dto.RoleDTO;
import org.emprenApp.role.domain.Role;
import org.emprenApp.role.infrastructure.request.RoleCreateRequest;
import org.emprenApp.role.infrastructure.request.RoleUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

// a null field in the request does NOT overwrite the current value of the entity.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    @Mapping(target = "permissionIds", source = "permissions")
    RoleDTO toDTO(Role role);

    // permissions are resolved and assigned by the service.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleCreateRequest request);

    // only copies the non-null fields of the request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntityFromRequest(RoleUpdateRequest request, @MappingTarget Role role);

    default List<Long> toPermissionIds(Set<Permission> permissions) {
        if (permissions == null) {
            return List.of();
        }
        // inactive permissions stay in role_permissions (history) but are not exposed.
        return permissions.stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getActive()))
                .map(Permission::getId)
                .toList();
    }
}
