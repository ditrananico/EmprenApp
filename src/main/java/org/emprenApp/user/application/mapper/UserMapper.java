package org.emprenApp.user.application.mapper;

import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.Instant;

// un campo null del request NO pisa el valor actual de la entidad.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "role", ignore = true)
    User toEntity(UserDTO userDTO);

    // el rol lo asigna el service (END_USER por defecto).
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "firstName", qualifiedByName = "trim")
    @Mapping(target = "lastName", qualifiedByName = "trim")
    @Mapping(target = "phone", qualifiedByName = "trim")
    User toEntity(UserCreateRequest userCreateRequest);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    UserDTO toDTO(User user);

    default Page<UserDTO> toPageDTO(Page<User> users){
        return users.map(this::toDTO);
    }

    // solo copia los campos no nulos del request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerifiedAt", ignore = true)
    @Mapping(target = "phoneVerifiedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "firstName", qualifiedByName = "trim")
    @Mapping(target = "lastName", qualifiedByName = "trim")
    @Mapping(target = "phone", qualifiedByName = "trim")
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);

    @AfterMapping
    default void setDefaultValues(@MappingTarget User user) {
        if (user.getStatus() == null) {
            user.setStatus(UserStatusEnum.ACTIVE);
        }
        if (user.getCreatedAt() == null) {
            Instant now = Instant.now();
            user.setCreatedAt(Timestamp.from(now));
        }
    }

    @Named("trim")
    static String trim(String text) {
        return text == null ? null : text.trim();
    }

    // El email se guarda sin espacios y en minusculas; el service lo usa tambien para buscar.
    @Named("normalizeEmail")
    static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
