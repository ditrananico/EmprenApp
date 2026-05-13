package org.emprenApp.user.infrastructure.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.response.UserResponse;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-16T11:57:18-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
public class UserInfrastructureMapperImpl implements UserInfrastructureMapper {

    @Override
    public UserResponse toResponse(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setApellido( userDTO.getApellido() );
        userResponse.setEmail( userDTO.getEmail() );
        userResponse.setFechaCreacion( userDTO.getFechaCreacion() );
        userResponse.setId( userDTO.getId() );
        userResponse.setNombre( userDTO.getNombre() );
        userResponse.setTelefono( userDTO.getTelefono() );

        return userResponse;
    }
}
