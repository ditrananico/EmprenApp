package org.emprenApp.user.application.mapper;

import org.emprenApp.shared.application.enums.EstadoUserEnum;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.Instant;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toEntity(UserDTO userDTO);

    @Mapping(target = "telefono", source = "telefonoPersonal")
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = "limpiarBlancosEnElNombre")
    User toEntity(UserCreateRequest userCreateRequest);

    UserDTO toDTO(User user);

    default Page<UserDTO> toPageDTO(Page<User> users){
        return users.map(this::toDTO);
    }
    User toEntity(UserUpdateRequest userUpdateRequest);

    @AfterMapping
    default void setDefaultValues(@MappingTarget User user) {
        if (user.getEstado() == null) {
            user.setEstado(EstadoUserEnum.ACTIVO);
        }
        if (user.getFechaCreacion() == null) {
            Instant ahora = Instant.now();
            user.setFechaCreacion(Timestamp.from(ahora));
        }
    }

    @Named("limpiarBlancosEnElNombre")
    static String limpiarBlancosEnElNombre(String nombre){
        if (nombre == null){
            return null;
        }
        return nombre.trim();
    }
}
