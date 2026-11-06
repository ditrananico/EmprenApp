package org.emprenApp.user.application;

import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserAdapter {

    UserDTO createUser(UserCreateRequest userCreateRequest) throws BaseException;
    UserDTO getUser(String email) throws BaseException;
    UserDTO getUserByID(Long id) throws BaseException;
    Page<UserDTO> getAllUsers(Pageable pageable) throws BaseException;
    UserDTO updateUser(Long id, UserUpdateRequest userUpdateRequest) throws BaseException;
    boolean deleteUser(Long id) throws BaseException;
    UserDTO activateUser(Long id) throws BaseException;

}
