package org.emprenApp.auth.application.service;

import io.jsonwebtoken.security.SignatureException;
import org.emprenApp.auth.application.dto.TokenDTO;
import org.emprenApp.auth.infrastructure.request.LoginRequest;
import org.emprenApp.auth.infrastructure.request.RegisterRequest;
import org.emprenApp.auth.infrastructure.security.JwtService;
import org.emprenApp.role.domain.Role;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.UserRoleEnum;
import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.user.application.UserAdapter;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService no toca la base ni hashea nada: orquesta tres colaboradores, todos mockeados.
 *  - AuthenticationManager: decide si las credenciales son validas (en la app real es el DaoAuthenticationProvider).
 *  - JwtService: firma el token.
 *  - UserAdapter: crea el usuario en register.
 * Lo que se prueba es la logica propia de AuthService: que llama a cada uno con lo correcto
 * y que traduce las excepciones al codigo de error correcto.
 *
 * Los metodos de AuthService lanzan BaseException (checked): los tests que los llaman directo
 * llevan "throws Exception" en la firma.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "juan@gmail.com";
    private static final String PASSWORD = "Clave1234";

    @Mock
    private UserAdapter userAdapter;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest(EMAIL, PASSWORD, "Juan", "Perez", "1155550000");
    }

    static Stream<AuthenticationException> authenticationExceptions() {
        return Stream.of(
                new LockedException("cuenta bloqueada"),
                new AccountExpiredException("cuenta vencida"),
                new CredentialsExpiredException("clave vencida"),
                new InternalAuthenticationServiceException("fallo interno"));
    }


    // Login correcto │ authenticate OK: devuelve TokenDTO con el token que genero JwtService,
    // tokenType "Bearer" y expiresIn igual a jwtService.getExpirationSeconds().
    @Test
    @DisplayName("LOGIN: credenciales correctas devuelven el token, el tipo Bearer y el vencimiento")
    void loginOk() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(authentication.getName())).thenReturn("token123");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        TokenDTO login = authService.login(loginRequest);

        assertThat(login.getTokenType()).isEqualTo("Bearer");
        assertThat(login.getToken()).isEqualTo("token123");
        assertThat(login.getExpiresIn()).isEqualTo(3600L);
    }

    // Subject del token │ El token se genera con authentication.getName() (el email que devolvio el manager),
    // no con el texto crudo del request. Stubbear el manager devolviendo un Authentication con otro nombre
    // (p. ej. "normalizado@gmail.com") y verificar con verify(jwtService).generateToken(...) que se uso ese.
    @Test
    @DisplayName("LOGIN: el token se genera con el nombre de la Authentication devuelta")
    void tokenUsesAuthenticationName() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);
        Authentication authentication = new UsernamePasswordAuthenticationToken("normalizado@gmail.com", null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(authentication.getName())).thenReturn("token123");

        TokenDTO login = authService.login(loginRequest);

        assertThat(login.getToken()).isEqualTo("token123");
        verify(jwtService).generateToken(authentication.getName());
        verify(jwtService, never()).generateToken(EMAIL);
    }

    // Se autentica con lo ingresado │ authenticate recibe un token no autenticado con email y password del request
    // (ArgumentCaptor<Authentication>: getPrincipal(), getCredentials() e isAuthenticated() == false).
    @Test
    @DisplayName("LOGIN: se delega en el AuthenticationManager con el email y la clave ingresados")
    void authenticatesWithRequestCredentials() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        authService.login(loginRequest);

        ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());
        Authentication capturedAuthentication = authenticationCaptor.getValue();
        assertThat(capturedAuthentication.getPrincipal()).isEqualTo(EMAIL);
        assertThat(capturedAuthentication.getCredentials()).isEqualTo(PASSWORD);
        assertThat(capturedAuthentication.isAuthenticated()).isFalse();
    }

    // Clave incorrecta │ El manager lanza BadCredentialsException: BaseException con INVALID_CREDENTIALS
    // y jwtService.generateToken nunca se llama.
    @Test
    @DisplayName("LOGIN: clave incorrecta devuelve INVALID_CREDENTIALS")
    void badCredentials() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, "claveincorrecta");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_CREDENTIALS.getCode());

        verify(jwtService, never()).generateToken(any());
    }

    // Usuario deshabilitado │ El manager lanza DisabledException (baja logica / rol inactivo): mismo INVALID_CREDENTIALS,
    // para no revelar que la cuenta existe pero esta dada de baja.
    @Test
    @DisplayName("LOGIN: usuario deshabilitado devuelve INVALID_CREDENTIALS")
    void disabledUser() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(DisabledException.class);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_CREDENTIALS.getCode());

        verify(jwtService, never()).generateToken(any());
    }

    // Cualquier AuthenticationException │ Otra subclase (p. ej. LockedException o InternalAuthenticationServiceException)
    // tambien se traduce a INVALID_CREDENTIALS, no a error generico.
    @ParameterizedTest(name = "LOGIN: {0} devuelve INVALID_CREDENTIALS")
    @MethodSource("authenticationExceptions")
    void anyAuthenticationException(AuthenticationException exception) throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(exception);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_CREDENTIALS.getCode());

        verify(jwtService, never()).generateToken(any());
    }

    // Request null │ loginRequest null: BaseException con INVALID_PARAMETERS y el manager nunca se llama.
    // (Ojo: en el catch de AuthenticationException el codigo usa loginRequest.getEmail(); con null no llega ahi
    // porque la validacion lanza BaseException antes. El test lo confirma.)
    @Test
    @DisplayName("LOGIN: request null devuelve INVALID_PARAMETERS")
    void nullRequest() throws Exception {
        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(authenticationManager, never()).authenticate(any());
    }

    // Email vacio │ email null, "" o "   ": INVALID_PARAMETERS y el manager nunca se llama
    // (verify(authenticationManager, never()).authenticate(any())). Se puede hacer con @ParameterizedTest + @NullAndEmptySource
    // o con tres asserts en el mismo test.
    @ParameterizedTest
    @NullAndEmptySource()
    @ValueSource(strings = {"   "})
    @DisplayName("LOGIN: email null o en blanco devuelve INVALID_PARAMETERS")
    void blankEmail(String email) throws Exception {
        LoginRequest loginRequest = loginRequest(email, PASSWORD);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(authenticationManager, never()).authenticate(any());
    }

    // Clave vacia │ password null, "" o "   ": INVALID_PARAMETERS y el manager nunca se llama.
    @ParameterizedTest
    @NullAndEmptySource()
    @ValueSource(strings = {"   "})
    @DisplayName("LOGIN: clave null o en blanco devuelve INVALID_PARAMETERS")
    void blankPassword(String password) throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, password);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(authenticationManager, never()).authenticate(any());
    }

    // Error inesperado │ El manager lanza una RuntimeException que NO es AuthenticationException
    // (p. ej. IllegalStateException): se envuelve en GenericException con GENERIC_ERROR.
    @Test
    @DisplayName("LOGIN: error inesperado devuelve GENERIC_ERROR")
    void unexpectedError() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new IllegalStateException("fallo inesperado"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getCode())
                .isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());

        verify(jwtService, never()).generateToken(any());
    }

    // Falla el JwtService │ authenticate OK pero generateToken lanza una RuntimeException: GENERIC_ERROR
    // (no INVALID_CREDENTIALS: las credenciales eran validas, el problema es interno).
    @Test
    @DisplayName("LOGIN: falla al generar el token devuelve GENERIC_ERROR")
    void tokenGenerationFails() throws Exception {
        LoginRequest loginRequest = loginRequest(EMAIL, PASSWORD);
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(authentication.getName())).thenThrow(new RuntimeException("fallo interno"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(GenericException.class)
                .extracting(e -> ((GenericException) e).getCode())
                .isEqualTo(ErrorCodeEnum.GENERIC_ERROR.getCode());

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    // Register correcto │ Llama a userAdapter.createUser con los mismos 5 datos del request
    // (ArgumentCaptor<UserCreateRequest>: email, password, firstName, lastName, phone) y devuelve el UserDTO que
    // devolvio el adapter (assertThat(result).isSameAs(dto)).
    @Test
    @DisplayName("REGISTER: delega en createUser con los datos del request y devuelve el UserDTO")
    void registerOk() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail(EMAIL);
        userDTO.setStatus(UserStatusEnum.ACTIVE);
        userDTO.setFirstName("Juan");
        userDTO.setLastName("Perez");
        userDTO.setPhone("1155550000");

        when(userAdapter.createUser(any())).thenReturn(userDTO);

        UserDTO result = authService.register(registerRequest());

        assertThat(result).isSameAs(userDTO);

        ArgumentCaptor<UserCreateRequest> captor = ArgumentCaptor.forClass(UserCreateRequest.class);
        verify(userAdapter).createUser(captor.capture());
        UserCreateRequest sent = captor.getValue();
        assertThat(sent.getEmail()).isEqualTo(EMAIL);
        assertThat(sent.getPassword()).isEqualTo(PASSWORD);
        assertThat(sent.getFirstName()).isEqualTo("Juan");
        assertThat(sent.getLastName()).isEqualTo("Perez");
        assertThat(sent.getPhone()).isEqualTo("1155550000");
    }

    // Register null │ registerRequest null: INVALID_PARAMETERS y userAdapter.createUser nunca se llama.
    @Test
    @DisplayName("REGISTER: request null devuelve INVALID_PARAMETERS")
    void registerNullRequest() throws Exception {
        assertThatThrownBy(() -> authService.register(null))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());

        verify(userAdapter, never()).createUser(any());
    }

    // Error del adapter │ Si createUser lanza BaseException (p. ej. email duplicado), register la deja pasar
    // con el mismo codigo (no la envuelve ni la cambia). Usar ErrorCodeEnum.DUPLICATE_KEY y
    // when(userAdapter.createUser(any())).thenThrow(new BaseException(ErrorCodeEnum.DUPLICATE_KEY)).
    @Test
    @DisplayName("REGISTER: propaga la BaseException de createUser")
    void registerPropagatesAdapterError() throws Exception {

        when(userAdapter.createUser(any())).thenThrow(new BaseException(ErrorCodeEnum.DUPLICATE_KEY));

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getCode())
                .isEqualTo(ErrorCodeEnum.DUPLICATE_KEY.getCode());
    }

    // Register no genera token │ register no toca ni el AuthenticationManager ni el JwtService
    // (verifyNoInteractions(authenticationManager, jwtService)): el usuario se registra y despues hace login.
    @Test
    @DisplayName("REGISTER: no autentica ni genera token")
    void registerDoesNotAuthenticate() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail(EMAIL);
        userDTO.setStatus(UserStatusEnum.ACTIVE);

        when(userAdapter.createUser(any())).thenReturn(userDTO);

        authService.register(registerRequest());

        verifyNoInteractions(authenticationManager, jwtService);
    }
}
