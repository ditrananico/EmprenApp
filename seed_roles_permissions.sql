-- Seed de roles, permisos y role_permissions segun docs/PERMISOS.md.
-- Ejecutar UNA vez, despues de scripts.sql. Los nombres de rol coinciden con UserRoleEnum (ANONYMOUS no se persiste). No incluye al primer Administrador (se crea a mano, fuera del repo).
USE emprenapp;

INSERT INTO permissions (name, description) VALUES
    ('ORDER_CREATE',          'Comprar'),
    ('ORDER_VIEW_OWN',        'Ver mis pedidos'),
    ('ORDER_CANCEL_OWN',      'Cancelar mi pedido'),
    ('ORDER_VIEW_BUSINESS',   'Ver pedidos de mi emprendimiento'),
    ('ORDER_UPDATE_STATUS',   'Preparar y despachar pedidos'),
    ('ORDER_DELIVER',         'Marcar entrega de un pedido'),
    ('PRODUCT_CREATE',        'Crear productos'),
    ('PRODUCT_UPDATE',        'Editar productos'),
    ('PRODUCT_DELETE',        'Dar de baja productos'),
    ('BUSINESS_UPDATE_OWN',   'Editar mi emprendimiento'),
    ('BUSINESS_APPROVE',      'Aprobar emprendimientos'),
    ('COURIER_APPROVE',       'Aprobar repartidores'),
    ('USER_UPDATE_OWN',       'Editar mi perfil'),
    ('LOCATION_MANAGE_OWN',   'Gestionar mis direcciones'),
    ('USER_VIEW_ALL',         'Ver todos los usuarios'),
    ('USER_BLOCK',            'Bloquear usuarios'),
    ('USER_UPDATE_ROLE',      'Cambiar el rol de un usuario'),
    ('CATEGORY_MANAGE',       'Gestionar categorias'),
    ('ROLE_MANAGE',           'Gestionar roles'),
    ('PERMISSION_MANAGE',     'Gestionar permisos');

INSERT INTO roles (name, description) VALUES
    ('END_USER',      'Comprador'),
    ('ENTREPRENEUR',  'Duenio de un emprendimiento'),
    ('RIDER',         'Repartidor'),
    ('MODERATOR',     'Moderador (alcance a definir)'),
    ('ADMIN',         'Administrador del sistema');

-- Permisos de comprador: los tienen todos los roles.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN ('ORDER_CREATE', 'ORDER_VIEW_OWN', 'ORDER_CANCEL_OWN',
                                          'USER_UPDATE_OWN', 'LOCATION_MANAGE_OWN');

-- Emprendedor.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN ('ORDER_VIEW_BUSINESS', 'ORDER_UPDATE_STATUS', 'PRODUCT_CREATE',
                                          'PRODUCT_UPDATE', 'PRODUCT_DELETE', 'BUSINESS_UPDATE_OWN')
WHERE r.name = 'ENTREPRENEUR';

-- Repartidor.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name = 'ORDER_DELIVER'
WHERE r.name = 'RIDER';

-- Administrador: todos los permisos que no tenia por comprador.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);