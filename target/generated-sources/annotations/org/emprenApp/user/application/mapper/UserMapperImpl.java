package org.emprenApp.user.application.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T19:47:38-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
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
        user.setFechaCreacion( userDTO.getFechaCreacion() );
        user.setEstado( userDTO.getEstado() );
        user.setNombre( userDTO.getNombre() );
        user.setApellido( userDTO.getApellido() );
        user.setTelefono( userDTO.getTelefono() );

        setDefaultValues( user );

        return user;
    }

    @Override
    public User toEntity(UserCreateRequest userCreateRequest) {
        if ( userCreateRequest == null ) {
            return null;
        }

        User user = new User();

        user.setTelefono( userCreateRequest.getTelefonoPersonal() );
        user.setNombre( UserMapper.limpiarBlancosEnElNombre( userCreateRequest.getNombre() ) );
        user.setEmail( userCreateRequest.getEmail() );
        user.setPassword( userCreateRequest.getPassword() );
        user.setApellido( userCreateRequest.getApellido() );

        setDefaultValues( user );

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
        userDTO.setFechaCreacion( user.getFechaCreacion() );
        userDTO.setEstado( user.getEstado() );
        userDTO.setNombre( user.getNombre() );
        userDTO.setApellido( user.getApellido() );
        userDTO.setTelefono( user.getTelefono() );

        return userDTO;
    }

    @Override
    public User toEntity(UserUpdateRequest userUpdateRequest) {
        if ( userUpdateRequest == null ) {
            return null;
        }

        User user = new User();

        user.setEmail( userUpdateRequest.getEmail() );
        user.setNombre( userUpdateRequest.getNombre() );
        user.setApellido( userUpdateRequest.getApellido() );
        user.setTelefono( userUpdateRequest.getTelefono() );

        setDefaultValues( user );

        return user;
    }
}
