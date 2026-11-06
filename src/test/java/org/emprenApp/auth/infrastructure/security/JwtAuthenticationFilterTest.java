package org.emprenApp.auth.infrastructure.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.emprenApp.permission.domain.Permission;
import org.emprenApp.role.domain.Role;
import org.emprenApp.shared.application.enums.UserRoleEnum;
import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.emprenApp.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String EMAIL = "juan@gmail.com";
    private static final String TOKEN = "token-de-prueba";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // Pone el header "Authorization" con el valor tal cual (para probar con y sin "Bearer ").
    private void withAuthorizationHeader(String value) {
        request.addHeader("Authorization", value);
    }

    // AuthUser tiene constructor privado: se obtiene con AuthUser.from(user), como en AuthUserDetailsServiceTest.
    private AuthUser authUser(UserStatusEnum status) {
        Permission permission = new Permission();
        permission.setName("perm1");
        permission.setActive(true);

        Role role = new Role();
        role.setId(1L);
        role.setName(UserRoleEnum.END_USER.name());
        role.setActive(true);
        role.setPermissions(new HashSet<>(java.util.List.of(permission)));

        User user = new User();
        user.setId(4L);
        user.setEmail(EMAIL);
        user.setPassword("$2a$10$hashDePrueba");
        user.setStatus(status);
        user.setRole(role);
        return AuthUser.from(user);
    }

    // Sin header │ Request sin "Authorization": SecurityContext sin autenticacion, la cadena sigue y
    // no se toca JwtService ni UserDetailsService (verifyNoInteractions).
    @Test
    @DisplayName("FILTER: sin header Authorization no autentica y sigue la cadena")
    void noHeader() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);

        verify(jwtService, never()).extractUsername(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        //verifyNoInteractions(jwtService, userDetailsService); es igual
    }

    // Esquema distinto │ Header "Basic abc123" (no empieza con "Bearer "): no autentica, sigue la cadena,
    // no se llama a JwtService. (Tambien vale probar "bearer token" en minuscula: el codigo es sensible a mayusculas.)
    @ParameterizedTest(name = "Authorization: [{0}] se ignora")
    @ValueSource(strings = {"Basic abc123", "bearer token", "Bearer", "Token abc"})
    void nonBearerHeader(String value) throws Exception {
        request.addHeader("Authorization", value);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);

        verifyNoInteractions(jwtService, userDetailsService);
    }

    // Token invalido │ jwtService.extractUsername lanza JwtException (p. ej. io.jsonwebtoken.security.SignatureException
    // o MalformedJwtException): contexto vacio, la cadena sigue, y NO se llama a userDetailsService.
    @Test
    @DisplayName("FILTER: token con firma invalida o mal formado no autentica y sigue la cadena")
    void invalidToken() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.extractUsername("invalid-token")).thenThrow(new io.jsonwebtoken.security.SignatureException("firma invalida"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(userDetailsService);
    }

    // Token vencido │ extractUsername lanza ExpiredJwtException (es una JwtException): mismo resultado que el anterior.
    // Para construirla: new ExpiredJwtException(null, null, "vencido") (header y claims pueden ir en null).
    @Test
    @DisplayName("FILTER: token vencido no autentica y sigue la cadena")
    void expiredToken() throws Exception {
        request.addHeader("Authorization", "Bearer vencided-token");

        when(jwtService.extractUsername("vencided-token")).thenThrow(new ExpiredJwtException(null, null, "vencido"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(userDetailsService);
    }

    // Header "Bearer " vacio │ "Bearer " (sin token): el codigo hace substring + trim y pasa "" al servicio.
    // Stubbear extractUsername("") para que lance IllegalArgumentException (asi se comporta el JwtService real,
    // ya lo probaste en JwtServiceTest#malformedOrEmptyToken): contexto vacio y la cadena sigue.
    @Test
    @DisplayName("FILTER: 'Bearer ' sin token no autentica y sigue la cadena")
    void emptyBearerToken() throws Exception {
        request.addHeader("Authorization", "Bearer ");

        when(jwtService.extractUsername("")).thenThrow(new IllegalArgumentException("Token vacío"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(userDetailsService);
    }

    // Usuario inexistente │ Token valido pero userDetailsService lanza UsernameNotFoundException
    // (el usuario fue borrado): contexto vacio, la cadena sigue.
    @Test
    @DisplayName("FILTER: token de un usuario que ya no existe no autentica")
    void userNotFound() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUsername("valid-token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("no existe"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);

        verify(userDetailsService).loadUserByUsername(EMAIL);
    }

    // Usuario deshabilitado │ Token valido, el usuario existe pero isEnabled() == false (baja logica):
    // contexto vacio y la cadena sigue. Es el caso que hace que la baja bloquee tokens viejos de inmediato.
    @Test
    @DisplayName("FILTER: token de un usuario deshabilitado no autentica")
    void disabledUser() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUsername("valid-token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(authUser(UserStatusEnum.INACTIVE));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);

        verify(userDetailsService).loadUserByUsername(EMAIL);
    }

    // =============================================================================================
    // CON CREDENCIALES VALIDAS: el contexto queda cargado
    // =============================================================================================

    // Token valido │ extractUsername devuelve EMAIL y userDetailsService devuelve un AuthUser ACTIVE:
    // SecurityContextHolder.getContext().getAuthentication() no es null, isAuthenticated() == true,
    // getPrincipal() es el AuthUser (isSameAs) y la cadena sigue.
    @Test
    @DisplayName("FILTER: token valido carga la autenticacion en el SecurityContext")
    void validToken() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        AuthUser authUser = authUser(UserStatusEnum.ACTIVE);

        when(jwtService.extractUsername("valid-token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(authUser);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(authUser);
        assertThat(chain.getRequest()).isSameAs(request);

    }

    // Authorities │ La Authentication tiene las authorities del usuario (ROLE_END_USER y perm1):
    // extracting(GrantedAuthority::getAuthority).containsExactlyInAnyOrder(...). Es lo que usara @PreAuthorize mas adelante.
    // Y las credenciales son null (no se guarda la clave): getCredentials() == null.
    @Test
    @DisplayName("FILTER: la Authentication lleva las authorities del usuario y sin credenciales")
    void authenticationHasAuthorities() throws Exception {
        request.addHeader("Authorization", "Bearer "+ TOKEN);
        AuthUser authUser = authUser(UserStatusEnum.ACTIVE);

        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(authUser);

        filter.doFilter(request, response, chain);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_END_USER", "perm1");
        assertThat(authentication.getCredentials()).isNull();
    }

    // Se busca por el email del token │ userDetailsService.loadUserByUsername recibe exactamente el subject que
    // devolvio extractUsername (verify con el email), y extractUsername recibe el token SIN el prefijo "Bearer "
    // (probar tambien "Bearer   token  " con espacios: el codigo hace trim).
    @Test
    @DisplayName("FILTER: quita el prefijo Bearer y busca al usuario por el subject del token")
    void stripsPrefixAndLoadsUser() throws Exception {
        request.addHeader("Authorization", "Bearer "+ "  "+TOKEN + "  ");
        AuthUser authUser = authUser(UserStatusEnum.ACTIVE);

        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(authUser);

        filter.doFilter(request, response, chain);

        assertThat(authUser.getEmail()).isEqualTo(EMAIL);
        verify(jwtService).extractUsername(TOKEN);
        verify(userDetailsService).loadUserByUsername(EMAIL);
    }

    // Detalles │ authentication.getDetails() no es null (WebAuthenticationDetails con IP y sessionId) cuando se autentica.
    // Opcional: es poco valor, pero cubre la linea setDetails.
    @Test
    @DisplayName("FILTER: la Authentication trae los detalles del request")
    void authenticationHasDetails() throws Exception {
        request.addHeader("Authorization", "Bearer "+ TOKEN);
        AuthUser authUser = authUser(UserStatusEnum.ACTIVE);

        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(authUser);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication.getDetails()).isNotNull();
    }


    // Nunca rechaza │ En ningun caso anterior el filtro escribe en la respuesta: response.getStatus() sigue en 200 (default de
    // MockHttpServletResponse) y el body esta vacio. Se puede chequear dentro de invalidToken, o aca con un caso representativo.
    @Test
    @DisplayName("FILTER: nunca responde el 401 por su cuenta")
    void neverWritesResponse() throws Exception {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenThrow(new io.jsonwebtoken.security.SignatureException("firma invalida"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEmpty();
        assertThat(response.getErrorMessage()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    // Error inesperado │ Si userDetailsService lanza una RuntimeException que el filtro NO captura (p. ej. IllegalStateException
    // por la base caida), la excepcion sale del filtro y la cadena NO sigue (assertThatThrownBy + chain.getRequest() null).
    // Documenta el comportamiento actual: solo se tragan errores de token/usuario, no los de infraestructura.
    @Test
    @DisplayName("FILTER: un error inesperado no se traga y no sigue la cadena")
    void unexpectedErrorPropagates() throws Exception {
        request.addHeader("Authorization", "Bearer "+ TOKEN);

        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenThrow(new RuntimeException("Error inesperado"));

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error inesperado");
        assertThat(chain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
