package org.emprenApp.shared.application.enums;

/**
 * Nombres de los roles del sistema. Coinciden con roles.name en la base (ver seed_roles_permissions.sql).
 * ANONYMOUS (usuario sin sesión) no se persiste: es acceso público, no hay fila en roles.
 */
public enum UserRoleEnum {

    ADMIN,
    END_USER,
    ENTREPRENEUR,
    MODERATOR,
    RIDER,
    ANONYMOUS
}
