package org.emprenApp.auth.infrastructure.mapper;

import org.emprenApp.auth.application.dto.TokenDTO;
import org.emprenApp.auth.infrastructure.response.LoginResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthInfrastructureMapper {

    AuthInfrastructureMapper INSTANCE = Mappers.getMapper(AuthInfrastructureMapper.class);

    LoginResponse toResponse(TokenDTO tokenDTO);
}
