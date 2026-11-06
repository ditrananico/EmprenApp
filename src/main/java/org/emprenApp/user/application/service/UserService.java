package org.emprenApp.user.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.role.domain.RoleRepository;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.UserRoleEnum;
import org.emprenApp.shared.application.enums.UserStatusEnum;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserAdapter {
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final String DEFAULT_ROLE_NAME = UserRoleEnum.END_USER.name();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest createRequest) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(createRequest);
            userValidationService.validateCreate(createRequest.getEmail(), createRequest.getPassword(),
                    createRequest.getFirstName(), createRequest.getLastName(), createRequest.getPhone());

            User user = UserMapper.INSTANCE.toEntity(createRequest);
            if (userRepository.existsByEmailIgnoreCase(user.getEmail()) || userRepository.existsByPhone(user.getPhone())) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            // Todo registro nace como END_USER; el cambio de rol lo hace solo el Administrador.
            user.setRole(roleRepository.findByNameIgnoreCaseAndActiveTrue(DEFAULT_ROLE_NAME)
                    .orElseThrow(() -> {
                        logger.error("No existe el rol por defecto '{}': falta sembrar los roles", DEFAULT_ROLE_NAME);
                        return new GenericException();
                    }));

            user.setPassword(passwordEncoder.encode(createRequest.getPassword()));

            User created = userRepository.save(user);
            logger.info("Usuario creado ID: {}", created.getId());
            return UserMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            // carrera entre dos altas con el mismo email/telefono: lo frena el UNIQUE de la base.
            logger.warn("Alta rechazada por clave unica: {}", e.getMessage());
            throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("Error inesperado al crear un usuario:", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(String email) throws BaseException {
        userValidationService.validateEmail(email);
        return UserMapper.INSTANCE.toDTO(userRepository.findByEmailAndStatus(UserMapper.normalizeEmail(email), UserStatusEnum.ACTIVE).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByID(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return UserMapper.INSTANCE.toDTO(userRepository.findByIdAndStatus(id, UserStatusEnum.ACTIVE).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) throws BaseException {
        try {
            Page<User> users = userRepository.findAllByStatus(UserStatusEnum.ACTIVE, pageable);
            return UserMapper.INSTANCE.toPageDTO(users);
        } catch (Exception e) {
            logger.error("Error de acceso a datos al obtener usuarios:", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            ValidateGeneric.validateNotNull(request);

            User user = userRepository.findByIdAndStatus(id, UserStatusEnum.ACTIVE).orElseThrow(NotFoundException::new);

            userValidationService.validateUpdate(request.getFirstName(), request.getLastName(), request.getPhone());

            if (request.getPhone() != null && userRepository.existsByPhoneAndIdNot(UserMapper.trim(request.getPhone()), id)) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            UserMapper.INSTANCE.updateEntityFromRequest(request, user);

            User updated = userRepository.save(user);
            logger.info("Usuario actualizado ID: {}", id);
            return UserMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            logger.warn("Actualizacion rechazada por clave unica: {}", e.getMessage());
            throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar un usuario:", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            User user = userRepository.findByIdAndStatus(id, UserStatusEnum.ACTIVE).orElseThrow(NotFoundException::new);
            user.setStatus(UserStatusEnum.INACTIVE);
            userRepository.save(user);
            logger.info("Usuario dado de baja (baja logica) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar un usuario:", e);
            throw new GenericException();
        }
    }

    // Reactiva una baja logica (INACTIVE). Un usuario BLOCKED o PENDING_VERIFICATION no se reactiva por aca.
    @Override
    @Transactional
    public UserDTO activateUser(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
            if (user.getStatus() == UserStatusEnum.ACTIVE) {
                return UserMapper.INSTANCE.toDTO(user);
            }
            if (user.getStatus() != UserStatusEnum.INACTIVE) {
                throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
            }
            user.setStatus(UserStatusEnum.ACTIVE);
            User activated = userRepository.save(user);
            logger.info("Usuario reactivado ID: {}", id);
            return UserMapper.INSTANCE.toDTO(activated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al reactivar un usuario:", e);
            throw new GenericException();
        }
    }
}
