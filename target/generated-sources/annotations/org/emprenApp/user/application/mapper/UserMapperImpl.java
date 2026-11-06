package org.emprenApp.user.application.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.role.domain.Role;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-05T12:35:08-0300",
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
        user.setCreatedAt( userDTO.getCreatedAt() );
        user.setStatus( userDTO.getStatus() );
        user.setFirstName( userDTO.getFirstName() );
        user.setLastName( userDTO.getLastName() );
        user.setPhone( userDTO.getPhone() );
        user.setEmailVerifiedAt( userDTO.getEmailVerifiedAt() );
        user.setPhoneVerifiedAt( userDTO.getPhoneVerifiedAt() );

        setDefaultValues( user );

        return user;
    }

    @Override
    public User toEntity(UserCreateRequest userCreateRequest) {
        if ( userCreateRequest == null ) {
            return null;
        }

        User user = new User();

        user.setEmail( UserMapper.normalizeEmail( userCreateRequest.getEmail() ) );
        user.setFirstName( UserMapper.trim( userCreateRequest.getFirstName() ) );
        user.setLastName( UserMapper.trim( userCreateRequest.getLastName() ) );
        user.setPhone( UserMapper.trim( userCreateRequest.getPhone() ) );
        user.setPassword( userCreateRequest.getPassword() );

        setDefaultValues( user );

        return user;
    }

    @Override
    public UserDTO toDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setRoleId( userRoleId( user ) );
        userDTO.setRoleName( userRoleName( user ) );
        userDTO.setId( user.getId() );
        userDTO.setEmail( user.getEmail() );
        userDTO.setCreatedAt( user.getCreatedAt() );
        userDTO.setStatus( user.getStatus() );
        userDTO.setFirstName( user.getFirstName() );
        userDTO.setLastName( user.getLastName() );
        userDTO.setPhone( user.getPhone() );
        userDTO.setEmailVerifiedAt( user.getEmailVerifiedAt() );
        userDTO.setPhoneVerifiedAt( user.getPhoneVerifiedAt() );

        return userDTO;
    }

    @Override
    public void updateEntityFromRequest(UserUpdateRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.getFirstName() != null ) {
            user.setFirstName( UserMapper.trim( request.getFirstName() ) );
        }
        if ( request.getLastName() != null ) {
            user.setLastName( UserMapper.trim( request.getLastName() ) );
        }
        if ( request.getPhone() != null ) {
            user.setPhone( UserMapper.trim( request.getPhone() ) );
        }

        setDefaultValues( user );
    }

    private Long userRoleId(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        Long id = role.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userRoleName(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        String name = role.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
