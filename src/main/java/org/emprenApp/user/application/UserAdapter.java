package org.emprenApp.user.application;

import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserAdapter {

    //Todas las busquedas filtran por estado ACTIVO

    UserDTO createUser(UserCreateRequest userCreateRequest) throws GenericException ;
    UserDTO getUser(String email) throws GenericException, NotFoundException;
    UserDTO getUserByID(Long id) throws GenericException, NotFoundException;
    UserDTO updateUser(UserUpdateRequest userUpdateRequest) throws GenericException, NotFoundException;
    Page<UserDTO> getAllUsers(Pageable pageable) throws GenericException;
    String deleteUser(String email) throws GenericException, NotFoundException;
    String deleteUserLogical(String email) throws GenericException, NotFoundException;
    String updateStatusUser(Long id) throws GenericException, NotFoundException;

}
