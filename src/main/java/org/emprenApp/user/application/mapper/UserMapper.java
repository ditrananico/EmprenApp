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

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toEntity(UserDTO userDTO);

    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "trimName")
    User toEntity(UserCreateRequest userCreateRequest);

    UserDTO toDTO(User user);

    default Page<UserDTO> toPageDTO(Page<User> users){
        return users.map(this::toDTO);
    }
    User toEntity(UserUpdateRequest userUpdateRequest);

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

    @Named("trimName")
    static String trimName(String name){
        if (name == null){
            return null;
        }
        return name.trim();
    }
}
