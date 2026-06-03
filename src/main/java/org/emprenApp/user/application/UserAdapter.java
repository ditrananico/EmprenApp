package org.emprenApp.user.application;

import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserAdapter {

    //Todas las busquedas filtran por estado ACTIVO

    UserDTO createUser(UserCreateRequest userCreateRequest) throws BaseException;
    UserDTO getUser(String email) throws BaseException;
    UserDTO getUserByID(Long id) throws GenericException,BaseException;
    UserDTO updateUser(UserUpdateRequest userUpdateRequest) throws BaseException;
    Page<UserDTO> getAllUsers(Pageable pageable) throws BaseException;
    void deleteUser(String email) throws BaseException;
    void updateStatusUser(Long id) throws BaseException;

}
