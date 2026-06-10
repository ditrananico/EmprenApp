
CREATE DATABASE emprenapp ;
USE emprenapp ;

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

CREATE TABLE emprenapp.emprendimientos (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           name VARCHAR(40) NOT NULL,
                                           description VARCHAR(250),
                                           estado VARCHAR(20) NOT NULL,
                                           PRIMARY KEY (id),
                                           CONSTRAINT fk_emprendimientos_user
                                               FOREIGN KEY (user_id)
                                                   REFERENCES emprenapp.usuarios(id),
                                           INDEX idx_emprendimientos_user (user_id),
                                           INDEX idx_emprendimientos_estado (estado),
                                           INDEX idx_emprendimientos_name (name)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emprenapp.categorias (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      nombre VARCHAR(100) NOT NULL,
                                      descripcion VARCHAR(255),
                                      tipo VARCHAR(50),
                                      active BOOLEAN NOT NULL DEFAULT TRUE,
                                      PRIMARY KEY (id),
                                      INDEX idx_categorias_active (active),
                                      INDEX idx_categorias_nombre (nombre)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emprenapp.productos (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     titulo VARCHAR(100) NOT NULL,
                                     descripcion VARCHAR(100),
                                     precio DECIMAL(10,2) NOT NULL,
                                     stock INT NOT NULL DEFAULT 0,
                                     stock_minimo INT NOT NULL DEFAULT 0,
                                     active BOOLEAN NOT NULL DEFAULT TRUE,
                                     emprendimiento_id BIGINT NOT NULL,
                                     categoria_id BIGINT NOT NULL,
                                     PRIMARY KEY (id),
                                     CONSTRAINT fk_productos_emprendimiento FOREIGN KEY (emprendimiento_id)
                                         REFERENCES emprenapp.emprendimientos (id) ON DELETE RESTRICT ON UPDATE CASCADE,
                                     CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id)
                                         REFERENCES emprenapp.categorias (id) ON DELETE RESTRICT ON UPDATE CASCADE,
                                     INDEX idx_productos_active (active),
                                     INDEX idx_productos_titulo (titulo),
                                     INDEX idx_productos_stock (stock),
                                     INDEX idx_productos_emprendimiento (emprendimiento_id),
                                     INDEX idx_productos_categoria (categoria_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE repartidores (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              usuario_id BIGINT NOT NULL,
                              vehiculo VARCHAR(30) NOT NULL,
                              emprendimiento_id BIGINT NULL,
                              modalidad_liquidacion VARCHAR(30) NOT NULL,
                              modalidad_de_cobro VARCHAR(30) NOT NULL,
                              variable_de_cobro DECIMAL(12,2) NOT NULL,
                              PRIMARY KEY (id),

                              CONSTRAINT fk_repartidores_usuario FOREIGN KEY (usuario_id)
                                  REFERENCES usuarios (id) ON DELETE RESTRICT ON UPDATE CASCADE,

                              CONSTRAINT fk_repartidores_emprendimiento FOREIGN KEY (emprendimiento_id)
                                  REFERENCES emprendimientos (id) ON DELETE RESTRICT ON UPDATE CASCADE,

                              INDEX idx_repartidores_usuario_id (usuario_id),
                              INDEX idx_repartidores_emprendimiento_id (emprendimiento_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE localidades (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             provincia VARCHAR(100) NOT NULL,
                             partido VARCHAR(100) NOT NULL,
                             localidad VARCHAR(100) NOT NULL,
                             codigo_postal VARCHAR(20) NULL,
                             PRIMARY KEY (id),
                             INDEX idx_localidades_busqueda (provincia, partido, localidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ubicaciones (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             calle VARCHAR(150) NOT NULL,
                             numero INT NOT NULL,
                             piso VARCHAR(10) NULL,
                             depto VARCHAR(10) NULL,
                             referencia VARCHAR(255) NULL,
                             localidad_id BIGINT NOT NULL,
                             PRIMARY KEY (id),
                             CONSTRAINT fk_ubicaciones_localidad FOREIGN KEY (localidad_id)
                                 REFERENCES localidades (id) ON DELETE RESTRICT ON UPDATE CASCADE,
                             INDEX idx_ubicaciones_localidad_id (localidad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE emprenapp.pedidos (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL,
                                   repartidor_id BIGINT NULL,
                                   emprendimiento_id BIGINT NOT NULL,
                                   ubicacion_destino_id BIGINT NOT NULL,
                                   fecha_creacion DATETIME NOT NULL,
                                   fecha_fin_proceso DATETIME NULL,
                                   fecha_finalizacion DATETIME NULL,
                                   metodo_pago VARCHAR(30) NOT NULL,
                                   total DECIMAL(12,2) NOT NULL,
                                   costo_envio DECIMAL(10,2) NOT NULL,
                                   estado VARCHAR(20) NOT NULL,
                                   PRIMARY KEY (id),

                                   CONSTRAINT fk_pedidos_usuario FOREIGN KEY (user_id)
                                       REFERENCES usuarios (id) ON DELETE RESTRICT ON UPDATE CASCADE,

                                   CONSTRAINT fk_pedidos_repartidor FOREIGN KEY (repartidor_id)
                                       REFERENCES repartidores (id) ON DELETE RESTRICT ON UPDATE CASCADE,

                                   CONSTRAINT fk_pedidos_emprendimiento FOREIGN KEY (emprendimiento_id)
                                       REFERENCES emprendimientos (id) ON DELETE RESTRICT ON UPDATE CASCADE,

                                   CONSTRAINT fk_pedidos_ubicacion_destino FOREIGN KEY (ubicacion_destino_id)
                                       REFERENCES ubicaciones (id) ON DELETE RESTRICT ON UPDATE CASCADE,

                                   INDEX idx_pedidos_user_id (user_id),
                                   INDEX idx_pedidos_repartidor_id (repartidor_id),
                                   INDEX idx_pedidos_emprendimiento_id (emprendimiento_id),
                                   INDEX idx_pedidos_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
