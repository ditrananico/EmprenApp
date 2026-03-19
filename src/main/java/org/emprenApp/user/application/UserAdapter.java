package org.emprenApp.user.application;

import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;

public interface UserAdapter {

    //Todas las busquedas filtran por estado ACTIVO

    UserDTO createUser(UserCreateRequest userCreateRequest) throws GenericException ;
    UserDTO getUser(String email) throws GenericException, NotFoundException;
    UserDTO getUserByID(Long id) throws GenericException, NotFoundException;
    UserDTO updateUser(UserDTO userDTO) throws GenericException;
}
