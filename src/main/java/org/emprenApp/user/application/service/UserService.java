package org.emprenApp.user.application.service;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.EstadoUserEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.user.application.UserAdapter;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.application.mapper.UserMapper;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.domain.UserRepository;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserAdapter {
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserValidationService userValidationService;
    private UserRepository userRepository;

    public UserDTO createUser(UserCreateRequest createRequest) throws GenericException{
        try {
            if (createRequest ==null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

            userValidationService.validateEmail(createRequest.getEmail());

            User usuarioCreado = userRepository.save(UserMapper.INSTANCE.toEntity(createRequest));
            logger.info("Usuario creado: {}", usuarioCreado.getEmail());
            return UserMapper.INSTANCE.toDTO(usuarioCreado);

        }catch (GenericException ez){
            logger.error("ERROR No paso el filtro: {}, con el mail: {}", "email", createRequest.getEmail());
            throw ez;
        }catch (NullPointerException e){
            logger.error("ERROR de nullpointer:",e);
            throw new GenericException();
        }catch (Exception e){
            logger.error("ERROR inesperado:",e);
            throw new GenericException();
        }
    }

    public UserDTO getUser(String email) throws GenericException, NotFoundException {
        if (email == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

        return UserMapper.INSTANCE.toDTO(
                userRepository.findByEmailAndEstado(email, EstadoUserEnum.ACTIVO)
                        .orElseThrow(NotFoundException::new));
    }

    @Override
    public UserDTO getUserByID(Long id) throws GenericException, NotFoundException {
        if (id == null || id < 0) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

        Optional<User> user = userRepository.findByIdAndEstado(id, EstadoUserEnum.ACTIVO);
        user.orElseThrow(NotFoundException::new);
        return UserMapper.INSTANCE.toDTO(user.get());

    }
    @Override
    public Page<UserDTO>  getAllUsers(Pageable pageable) throws GenericException {
        try {
            Page<User> users = userRepository.findAllByEstado(EstadoUserEnum.ACTIVO, pageable);
            return UserMapper.INSTANCE.toPageDTO(users);
        }catch(Exception e){
            logger.error("Error al obtener los usuarios", e);
            throw new GenericException();
        }

    }

    @Override
    public void deleteUser(String email) throws GenericException, NotFoundException {
        try {
            if (email == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

            Optional<User> userOptional = userRepository.findByEmailAndEstado(email, EstadoUserEnum.ACTIVO);
            if (userOptional.isEmpty()) {
                throw new NotFoundException();
            }
            User user = userOptional.get();
            user.setEstado(EstadoUserEnum.INACTIVO);
            userRepository.save(user);

            logger.info("Usuario eliminado exitosamente: {}", email);
       } catch (Exception e) {
            logger.error("Error inesperado al eliminar usuario:", e);
            throw new GenericException();
        }
    }

    @Override
    public UserDTO updateUser(UserUpdateRequest userUpdateRequest) throws GenericException, NotFoundException {

        if (userUpdateRequest == null || userUpdateRequest.getEmail() == null) {
            throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        }

        Optional<User> userOptional = userRepository.findByEmailAndEstado(userUpdateRequest.getEmail(), EstadoUserEnum.ACTIVO);
        if (userOptional.isEmpty()) {
            throw new NotFoundException();
        }
        User user = UserMapper.INSTANCE.toEntity(userUpdateRequest);

        userRepository.save(user);
        logger.info("Usuario actualizado: {}", userUpdateRequest.getEmail());
        return UserMapper.INSTANCE.toDTO(user);
    }

    @Override
    public void updateStatusUser(Long id) throws GenericException, NotFoundException {

        if (id == null || id < 0) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

        Optional<User> userOptional = userRepository.findByIdAndEstado(id, EstadoUserEnum.ACTIVO);
        userOptional.orElseThrow(NotFoundException::new);

        User user = userOptional.get();
        user.setEstado(EstadoUserEnum.ACTIVO);
        userRepository.save(user);

        logger.info("Estado del usuario actualizado: {}", user.getEstado());
    }

}
