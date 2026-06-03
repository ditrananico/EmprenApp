CREATE TABLE emprenapp.productos (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     titulo VARCHAR(100) NOT NULL,
                                     descripcion VARCHAR(100),
                                     precio DECIMAL(10,2) NOT NULL,
                                     stock INT NOT NULL DEFAULT 0,
                                     stock_minimo INT NOT NULL DEFAULT 0,
                                     active BOOLEAN NOT NULL DEFAULT TRUE,
                                     PRIMARY KEY (id),
                                     INDEX idx_productos_active (active),
                                     INDEX idx_productos_titulo (titulo),
                                     INDEX idx_productos_stock (stock)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emprenapp.pedidos (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL,
                                   emprendimiento_id BIGINT NOT NULL,
                                   fecha_creacion DATETIME NOT NULL,
                                   fecha_fin_proceso DATETIME NULL,
                                   fecha_finalizacion DATETIME NULL,
                                   metodo_pago VARCHAR(30),
                                   total DECIMAL(12,2),
                                   costo_envio DECIMAL(10,2),
                                   estado VARCHAR(20) NOT NULL,
                                   PRIMARY KEY (id),
                                   CONSTRAINT fk_pedidos_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES emprenapp.users(id),
                                   CONSTRAINT fk_pedidos_emprendimiento
                                       FOREIGN KEY (emprendimiento_id)
                                           REFERENCES emprenapp.emprendimientos(id),
                                   INDEX idx_pedidos_user (user_id),
                                   INDEX idx_pedidos_emprendimiento (emprendimiento_id),
                                   INDEX idx_pedidos_estado (estado),
                                   INDEX idx_pedidos_fecha_creacion (fecha_creacion),
                                   INDEX idx_pedidos_estado_fecha (estado, fecha_creacion)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emprenapp.emprendimientos (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           name VARCHAR(40) NOT NULL,
                                           description VARCHAR(250),
                                           estado VARCHAR(20) NOT NULL,
                                           PRIMARY KEY (id),
                                           CONSTRAINT fk_emprendimientos_user
                                               FOREIGN KEY (user_id)
                                                   REFERENCES emprenapp.users(id),
                                           INDEX idx_emprendimientos_user (user_id),
                                           INDEX idx_emprendimientos_estado (estado),
                                           INDEX idx_emprendimientos_name (name)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emprenapp.detalle_pedido (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          cantidad INT NOT NULL,
                                          precio_unitario DECIMAL(10,2) NOT NULL,
                                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                                          pedido_id BIGINT NOT NULL,
                                          producto_id BIGINT NOT NULL,
                                          PRIMARY KEY (id),
                                          CONSTRAINT fk_detalle_pedido_pedido
                                              FOREIGN KEY (pedido_id)
                                                  REFERENCES emprenapp.pedidos(id),
                                          CONSTRAINT fk_detalle_pedido_producto
                                              FOREIGN KEY (producto_id)
                                                  REFERENCES emprenapp.productos(id),
                                          INDEX idx_detalle_pedido_pedido (pedido_id),
                                          INDEX idx_detalle_pedido_producto (producto_id),
                                          INDEX idx_detalle_pedido_pedido_producto (pedido_id, producto_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emprenapp.usuarios (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    email VARCHAR(100) NOT NULL,
                                    password VARCHAR(60) NOT NULL,
                                    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    estado VARCHAR(20) NOT NULL,
                                    nombre VARCHAR(100) NOT NULL,
                                    apellido VARCHAR(100) NOT NULL,
                                    telefono VARCHAR(15),
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_users_email (email),
                                    INDEX idx_users_estado (estado),
                                    INDEX idx_users_nombre_apellido (nombre, apellido)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;