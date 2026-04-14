package org.emprenApp.user.infrastructure.mapper;

import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface
UserInfrastructureMapper {

    UserInfrastructureMapper INSTANCE = Mappers.getMapper(UserInfrastructureMapper.class);

    UserResponse toResponse(UserDTO userDTO);
}
