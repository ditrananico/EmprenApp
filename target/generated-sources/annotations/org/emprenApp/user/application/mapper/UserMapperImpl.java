package org.emprenApp.user.application.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.domain.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-19T17:33:48-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User user = new User();

        user.setId( userDTO.getId() );
        user.setEmail( userDTO.getEmail() );
        user.setPassword( userDTO.getPassword() );
        user.setFechaCreacion( userDTO.getFechaCreacion() );
        user.setEstado( userDTO.getEstado() );
        user.setNombre( userDTO.getNombre() );
        user.setApellido( userDTO.getApellido() );
        user.setTelefono( userDTO.getTelefono() );

        return user;
    }

    @Override
    public UserDTO toDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setId( user.getId() );
        userDTO.setEmail( user.getEmail() );
        userDTO.setPassword( user.getPassword() );
        userDTO.setFechaCreacion( user.getFechaCreacion() );
        userDTO.setEstado( user.getEstado() );
        userDTO.setNombre( user.getNombre() );
        userDTO.setApellido( user.getApellido() );
        userDTO.setTelefono( user.getTelefono() );

        return userDTO;
    }
}
