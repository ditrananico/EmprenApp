package org.emprenApp.auth.infrastructure.security;

import org.emprenApp.permission.domain.Permission;
import org.emprenApp.role.domain.Role;
import org.emprenApp.shared.application.enums.UserRoleEnum;
import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {

    private static final String EMAIL = "juan@gmail.com";
    private static final String PASSWORD_HASH = "$2a$10$hashDePrueba";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthUserDetailsService authUserDetailsService;

    private Permission permission(String name, boolean active) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setActive(active);
        return permission;
    }

    private Role role(String name, boolean active, Permission... permissions) {
        Role role = new Role();
        role.setId(1L);
        role.setName(name);
        role.setActive(active);
        role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
        return role;
    }

    private User user(UserStatusEnum status, Role role) {
        User user = new User();
        user.setId(4L);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD_HASH);
        user.setStatus(status);
        user.setRole(role);
        return user;
    }

    // ---------------------------------------------------------------------------------------------
    // Usuario existente │ Devuelve un AuthUser con id, email (getUsername), hash (getPassword) y roleName.
    // ---------------------------------------------------------------------------------------------
    @Test
    @DisplayName("LOAD: usuario existente devuelve un AuthUser con sus datos")
    void loadExistingUser() {

        Role role = role(UserRoleEnum.END_USER.name(), true, permission("perm1", true));
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.getId()).isEqualTo(4L);
        assertThat(authUser.getUsername()).isEqualTo(EMAIL);
        assertThat(authUser.getPassword()).isEqualTo(PASSWORD_HASH);
        assertThat(authUser.getRoleName()).isEqualTo(UserRoleEnum.END_USER.name());

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Authorities │ Contiene ROLE_<rol> mas los permisos activos del rol.
    @Test
    @DisplayName("LOAD: authorities incluye ROLE_<rol> y los permisos")
    void authoritiesIncludeRoleAndPermissions() {

        Role role = role(UserRoleEnum.END_USER.name(), true, permission("perm1", true), permission("perm2", true));
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_END_USER", "perm1", "perm2");

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Normalizacion │ Con "  JUAN@Gmail.com " el repositorio se llama con "juan@gmail.com" (ArgumentCaptor).
    @Test
    @DisplayName("LOAD: normaliza el email antes de consultar el repositorio")
    void emailIsNormalized() {
        String rawEmail = "    JUAN@GMail.com  ";
        Role role = role(UserRoleEnum.END_USER.name(), true, permission("perm1", true), permission("perm2", true));
        User user = user(UserStatusEnum.ACTIVE, role);

        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(rawEmail);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).findWithRoleAndPermissionsByEmail(captor.capture());
        assertThat(captor.getValue()).isEqualTo(EMAIL);

        assertThat(authUser.getUsername()).isEqualTo(EMAIL);
    }

    // Inexistente │ Si el repositorio devuelve Optional.empty() lanza UsernameNotFoundException.
    @Test
    @DisplayName("LOAD: usuario inexistente lanza UsernameNotFoundException")
    void userNotFound() {
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUserDetailsService.loadUserByUsername(EMAIL))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // Usuario inactivo │ Con status INACTIVE el AuthUser tiene isEnabled() == false.
    @Test
    @DisplayName("LOAD: usuario INACTIVE queda deshabilitado")
    void inactiveUserIsDisabled() {
        Role role = role(UserRoleEnum.END_USER.name(), true, permission("perm1", true));
        User user = user(UserStatusEnum.INACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.isEnabled()).isFalse();

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Rol inactivo │ Con el rol active=false, isEnabled() == false y no hay authorities.
    @Test
    @DisplayName("LOAD: rol inactivo deshabilita al usuario y no da authorities")
    void inactiveRoleIsDisabled() {
        Role role = role(UserRoleEnum.END_USER.name(), false, permission("perm1", true));
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.isEnabled()).isFalse();
        assertThat(authUser.getAuthorities()).isEmpty();

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Permisos inactivos │ Un permiso active=false no aparece en las authorities.
    @Test
    @DisplayName("LOAD: permisos inactivos no aparecen en las authorities")
    void inactivePermissionsAreExcluded() {
        Role role = role(UserRoleEnum.END_USER.name(), true, permission("perm1", false), permission("perm2", true));
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_END_USER", "perm2");

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Usuario activo │ Con status ACTIVE y rol activo, isEnabled() == true (el caso feliz del flag).
    @Test
    @DisplayName("LOAD: usuario ACTIVE con rol activo queda habilitado")
    void activeUserIsEnabled() {
        Role role = role(UserRoleEnum.END_USER.name(), true);
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.isEnabled()).isTrue();

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Otros estados │ PENDING_VERIFICATION tambien queda deshabilitado (solo ACTIVE habilita).
    @Test
    @DisplayName("LOAD: usuario PENDING_VERIFICATION queda deshabilitado")
    void pendingVerificationUserIsDisabled() {
        Role role = role(UserRoleEnum.END_USER.name(), true);
        User user = user(UserStatusEnum.PENDING_VERIFICATION, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.isEnabled()).isFalse();

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Rol sin permisos │ Un rol sin permisos solo aporta ROLE_<rol> (una sola authority, sin errores).
    @Test
    @DisplayName("LOAD: rol sin permisos devuelve solo ROLE_<rol>")
    void roleWithoutPermissions() {
        Role role = role(UserRoleEnum.END_USER.name(), true);
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(authUser.getAuthorities()).hasSize(1);
        assertThat(authUser.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_END_USER");

        verify(userRepository).findWithRoleAndPermissionsByEmail(EMAIL);
    }

    // Email null │ loadUserByUsername(null) no explota con NullPointerException: lanza UsernameNotFoundException.
    @Test
    @DisplayName("LOAD: email null lanza UsernameNotFoundException")
    void nullEmail() {
        assertThatThrownBy(() -> authUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // Authorities inmutables │ La lista de authorities del AuthUser no se puede modificar (List.copyOf).
    @Test
    @DisplayName("LOAD: las authorities devueltas son inmutables")
    void authoritiesAreImmutable() {
        Role role = role(UserRoleEnum.END_USER.name(), true, permission("perm1", true), permission("perm2", true));
        User user = user(UserStatusEnum.ACTIVE, role);
        when(userRepository.findWithRoleAndPermissionsByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthUser authUser = (AuthUser) authUserDetailsService.loadUserByUsername(EMAIL);

        assertThatThrownBy(() -> authUser.getAuthorities().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
