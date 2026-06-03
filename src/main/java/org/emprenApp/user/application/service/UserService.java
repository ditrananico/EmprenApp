package org.emprenApp.user.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.EstadoUserEnum;
import org.emprenApp.shared.application.exception.BaseException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserAdapter {
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    private ValidateGeneric validate;

    public UserDTO createUser(UserCreateRequest createRequest) throws BaseException{
        try {
            validate.validateNotNull(createRequest);
            validate.validateEmail(createRequest.getEmail());

            User usuarioCreado = userRepository.save(UserMapper.INSTANCE.toEntity(createRequest));
            logger.info("Usuario creado: {}", usuarioCreado.getEmail());
            return UserMapper.INSTANCE.toDTO(usuarioCreado);
        }catch (BaseException e){
            throw e;
        } catch (Exception e){
            logger.error("ERROR inesperado:",e);
            throw new GenericException();
        }
    }

    public UserDTO getUser(String email) throws BaseException {
        validate.validateEmail(email);
        return UserMapper.INSTANCE.toDTO(
                userRepository.findByEmailAndEstado(email, EstadoUserEnum.ACTIVO)
                        .orElseThrow(NotFoundException::new));
    }

    @Override
    public UserDTO getUserByID(Long id) throws BaseException {
        Optional<User> user = userRepository.findByIdAndEstado(id, EstadoUserEnum.ACTIVO);
        user.orElseThrow(NotFoundException::new);
        return UserMapper.INSTANCE.toDTO(user.get());
    }

    @Override
    public Page<UserDTO>  getAllUsers(Pageable pageable) throws BaseException {
        try {
            Page<User> users = userRepository.findAllByEstado(EstadoUserEnum.ACTIVO, pageable);
            return UserMapper.INSTANCE.toPageDTO(users);
        }catch(Exception e){
            logger.error("Error de acceso a datos al obtener usuarios: {}", e.getMessage());
            throw new GenericException();
        }

    }

    @Override
    public void deleteUser(String email) throws BaseException {
        try {
            validate.validateEmail(email);
            Optional<User> userOptional = userRepository.findByEmailAndEstado(email, EstadoUserEnum.ACTIVO);
            if (userOptional.isEmpty()) {
                throw new NotFoundException();
            }
            User user = userOptional.get();
            user.setEstado(EstadoUserEnum.INACTIVO);
            userRepository.save(user);

            logger.info("Usuario eliminado exitosamente: {}", email);
       } catch (BaseException e){
            throw e;
        }catch (Exception e) {
            logger.error("Error inesperado al eliminar un usuario:", e);
            throw new GenericException();
        }
    }

    @Override
    public UserDTO updateUser(UserUpdateRequest userUpdateRequest) throws BaseException {

        try {
            validate.validateNotNull(userUpdateRequest);
            validate.validateEmail(userUpdateRequest.getEmail());

            Optional<User> userOptional = userRepository.findByEmailAndEstado(userUpdateRequest.getEmail(), EstadoUserEnum.ACTIVO);
            if (userOptional.isEmpty()) {
                throw new NotFoundException();
            }
            User user = UserMapper.INSTANCE.toEntity(userUpdateRequest);

            userRepository.save(user);
            logger.info("Usuario actualizado: {}", userUpdateRequest.getEmail());
            return UserMapper.INSTANCE.toDTO(user);

        } catch (BaseException e){
            throw e;
        }catch (Exception e) {
            logger.error("Error inesperado al actualizar un usuario:", e);
            throw new GenericException();
        }
    }

    @Override
    public void updateStatusUser(Long id) throws BaseException {
        try {
            validate.validateId(id);
            Optional<User> userOptional = userRepository.findByIdAndEstado(id, EstadoUserEnum.ACTIVO);
            if (userOptional.isEmpty()) {
                throw new NotFoundException();
            }

            User user = userOptional.get();
            user.setEstado(EstadoUserEnum.ACTIVO);
            userRepository.save(user);

            logger.info("Estado del usuario actualizado: {}", user.getEstado());
        } catch (BaseException e){
            throw e;
        }catch (Exception e) {
            logger.error("Error inesperado al actualizar un usuario:", e);
            throw new GenericException();
        }
    }

}
