package org.emprenApp.user.infrastructure.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.response.UserResponse;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-13T18:32:13-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
public class UserInfrastructureMapperImpl implements UserInfrastructureMapper {

    @Override
    public UserResponse toResponse(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setId( userDTO.getId() );
        userResponse.setEmail( userDTO.getEmail() );
        userResponse.setNombre( userDTO.getNombre() );
        userResponse.setApellido( userDTO.getApellido() );
        userResponse.setTelefono( userDTO.getTelefono() );
        userResponse.setFechaCreacion( userDTO.getFechaCreacion() );

        return userResponse;
    }
}
