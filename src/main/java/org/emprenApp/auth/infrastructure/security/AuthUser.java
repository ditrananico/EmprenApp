package org.emprenApp.auth.infrastructure.security;

import lombok.AccessLevel;
import lombok.Getter;
import org.emprenApp.permission.domain.Permission;
import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.emprenApp.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Getter
public class AuthUser implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Long id;
    private final String email;
    private final String roleName;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;
    @Getter(AccessLevel.NONE)
    private final String passwordHash;

    private AuthUser(Long id, String email, String passwordHash, String roleName, boolean enabled,
                     List<GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleName = roleName;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static AuthUser from(User user) {
        boolean roleActive = Boolean.TRUE.equals(user.getRole().getActive());

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roleActive) {
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().getName()));
            for (Permission permission : user.getRole().getPermissions()) {
                if (Boolean.TRUE.equals(permission.getActive())) {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
        }
        // DaoAuthenticationProvider llama a isEnabled()
        boolean enabled = user.getStatus() == UserStatusEnum.ACTIVE && roleActive;
        return new AuthUser(user.getId(), user.getEmail(), user.getPassword(), user.getRole().getName(),
                enabled, List.copyOf(authorities));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
