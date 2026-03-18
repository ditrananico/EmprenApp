package org.emprenApp.user.application;

import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.user.application.dto.UserDTO;

public interface UserAdapter {

    UserDTO createUser(UserDTO userDTO) throws GenericException ;
    UserDTO getUser(String email) throws GenericException, NotFoundException;
    UserDTO updateUser(UserDTO userDTO) throws GenericException;
}
