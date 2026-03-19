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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public UserDTO updateUser(UserDTO userDTO) throws GenericException {
        return null;
    }

    //Tarea: actualizar el estado de un usuario por id
}
