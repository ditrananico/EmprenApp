package org.emprenApp.user.application.mapper;

import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toEntity(UserDTO userDTO);
    UserDTO toDTO(User user);
}
