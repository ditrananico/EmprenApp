# Plantillas de `nuevo-modulo`

Basadas en `categoria/`. Placeholders:

- `{{modulo}}`: paquete en minúsculas (`cliente`)
- `{{Entidad}}`: PascalCase (`Cliente`)
- `{{entidad}}`: camelCase (`cliente`)
- `{{tabla}}`: nombre de tabla (`clientes`)
- `{{ruta}}`: ruta REST sin `v1/` (`cliente`)
- `{{flag}}` / `{{Flag}}`: nombre del flag de baja y su versión PascalCase (`active` / `Active`)
- `{{campos}}`: los campos de negocio del usuario

Cada plantilla muestra el campo ejemplo `nombre` (único, obligatorio) y `descripcion` (opcional). Reemplazarlos por los campos reales.

## domain/{{Entidad}}.java

```java
package org.emprenApp.{{modulo}}.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "{{tabla}}")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class {{Entidad}} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "ACTIVE")
    private Boolean active = true;
}
```

Relación hacia otra entidad (si aplica):

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "categoria_id", nullable = false)
private Categoria categoria;
```

## domain/{{Entidad}}Repository.java

```java
package org.emprenApp.{{modulo}}.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface {{Entidad}}Repository extends JpaRepository<{{Entidad}}, Long> {

    Optional<{{Entidad}}> findByIdAndActiveTrue(Long id);

    Page<{{Entidad}}> findAllByActiveTrue(Pageable pageable);

    boolean existsByNombreIgnoreCaseAndActiveTrue(String nombre);

    boolean existsByNombreIgnoreCaseAndActiveTrueAndIdNot(String nombre, Long id);
}
```

Si no hay campo único, quitar los dos `exists...`.

## application/{{Entidad}}Adapter.java

```java
package org.emprenApp.{{modulo}}.application;

import org.emprenApp.{{modulo}}.application.dto.{{Entidad}}DTO;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}CreateRequest;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}UpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface {{Entidad}}Adapter {

    {{Entidad}}DTO create{{Entidad}}({{Entidad}}CreateRequest request) throws BaseException;

    {{Entidad}}DTO get{{Entidad}}ById(Long id) throws BaseException;

    Page<{{Entidad}}DTO> get{{Entidad}}sActivos(Pageable pageable) throws BaseException;

    {{Entidad}}DTO update{{Entidad}}({{Entidad}}UpdateRequest request) throws BaseException;

    boolean delete{{Entidad}}(Long id) throws BaseException;
}
```

## application/service/{{Entidad}}Service.java

```java
package org.emprenApp.{{modulo}}.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.{{modulo}}.application.{{Entidad}}Adapter;
import org.emprenApp.{{modulo}}.application.dto.{{Entidad}}DTO;
import org.emprenApp.{{modulo}}.application.mapper.{{Entidad}}Mapper;
import org.emprenApp.{{modulo}}.domain.{{Entidad}};
import org.emprenApp.{{modulo}}.domain.{{Entidad}}Repository;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}CreateRequest;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}UpdateRequest;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class {{Entidad}}Service implements {{Entidad}}Adapter {

    private final static Logger logger = LoggerFactory.getLogger({{Entidad}}Service.class);

    private final {{Entidad}}Repository {{entidad}}Repository;
    private final {{Entidad}}ValidationService {{entidad}}ValidationService;

    @Override
    @Transactional
    public {{Entidad}}DTO create{{Entidad}}({{Entidad}}CreateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            {{entidad}}ValidationService.validarCampos(request.getNombre(), request.getDescripcion());

            String nombre = request.getNombre().trim();
            if ({{entidad}}Repository.existsByNombreIgnoreCaseAndActiveTrue(nombre)) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            {{Entidad}} {{entidad}} = {{Entidad}}Mapper.INSTANCE.toEntity(request);
            {{Entidad}} created = {{entidad}}Repository.save({{entidad}});
            logger.info("{{Entidad}} creado: {}", created.getId());
            return {{Entidad}}Mapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear {{entidad}}", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public {{Entidad}}DTO get{{Entidad}}ById(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return {{Entidad}}Mapper.INSTANCE.toDTO({{entidad}}Repository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<{{Entidad}}DTO> get{{Entidad}}sActivos(Pageable pageable) throws BaseException {
        try {
            return {{entidad}}Repository.findAllByActiveTrue(pageable).map({{Entidad}}Mapper.INSTANCE::toDTO);
        } catch (Exception e) {
            logger.error("Error al listar {{entidad}}s activos", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public {{Entidad}}DTO update{{Entidad}}({{Entidad}}UpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateId(request.getId());

            {{Entidad}} {{entidad}} = {{entidad}}Repository.findByIdAndActiveTrue(request.getId()).orElseThrow(NotFoundException::new);

            {{entidad}}ValidationService.validarCampos(request.getNombre(), request.getDescripcion());

            if (request.getNombre() != null) {
                String nombre = request.getNombre().trim();
                if ({{entidad}}Repository.existsByNombreIgnoreCaseAndActiveTrueAndIdNot(nombre, {{entidad}}.getId())) {
                    throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
                }
            }
            {{Entidad}}Mapper.INSTANCE.updateEntityFromRequest(request, {{entidad}});

            {{Entidad}} updated = {{entidad}}Repository.save({{entidad}});
            logger.info("{{Entidad}} actualizado: {}", updated.getId());
            return {{Entidad}}Mapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar {{entidad}}", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean delete{{Entidad}}(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            {{Entidad}} {{entidad}} = {{entidad}}Repository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new);
            {{entidad}}.setActive(false);
            {{entidad}}Repository.save({{entidad}});
            logger.info("{{Entidad}} eliminado (baja lógica) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar {{entidad}} ID: {}", id, e);
            throw new GenericException();
        }
    }
}
```

## application/service/{{Entidad}}ValidationService.java

```java
package org.emprenApp.{{modulo}}.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Service;

@Service
public class {{Entidad}}ValidationService {

    private static final int NOMBRE_MAX = 100;
    private static final int DESCRIPCION_MAX = 255;

    public void validarCampos(String nombre, String descripcion) throws BaseException {
        if (nombre != null) {
            ValidateGeneric.validateNotBlank(nombre);
            ValidateGeneric.validateMaxLength(nombre.trim(), NOMBRE_MAX);
        }
        ValidateGeneric.validateMaxLength(descripcion, DESCRIPCION_MAX);
    }
}
```

## application/dto/{{Entidad}}DTO.java

```java
package org.emprenApp.{{modulo}}.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class {{Entidad}}DTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean active;
}
```

## application/mapper/{{Entidad}}Mapper.java

```java
package org.emprenApp.{{modulo}}.application.mapper;

import org.emprenApp.{{modulo}}.application.dto.{{Entidad}}DTO;
import org.emprenApp.{{modulo}}.domain.{{Entidad}};
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}CreateRequest;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}UpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

// un campo null del request NO pisa el valor actual de la entidad.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface {{Entidad}}Mapper {

    {{Entidad}}Mapper INSTANCE = Mappers.getMapper({{Entidad}}Mapper.class);

    {{Entidad}}DTO toDTO({{Entidad}} {{entidad}});

    {{Entidad}} toEntity({{Entidad}}DTO {{entidad}}DTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    {{Entidad}} toEntity({{Entidad}}CreateRequest request);

    // solo copia los campos no nulos del request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest({{Entidad}}UpdateRequest request, @MappingTarget {{Entidad}} {{entidad}});
}
```

Nota: si el flag no es `active` (`activo` / `estado`), ajustar los `@Mapping`, el repositorio y el service.
Si `estado` es un enum, usar el enum en lugar de `constant = "true"`.

## infrastructure/request/{{Entidad}}CreateRequest.java

```java
package org.emprenApp.{{modulo}}.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class {{Entidad}}CreateRequest {

    @NotNull
    private String nombre;

    private String descripcion;
}
```

## infrastructure/request/{{Entidad}}UpdateRequest.java

```java
package org.emprenApp.{{modulo}}.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class {{Entidad}}UpdateRequest {

    @NotNull
    private Long id;

    private String nombre;

    private String descripcion;
}
```

## infrastructure/response/{{Entidad}}Response.java

```java
package org.emprenApp.{{modulo}}.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class {{Entidad}}Response {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean active;
}
```

## infrastructure/mapper/{{Entidad}}InfrastructureMapper.java

```java
package org.emprenApp.{{modulo}}.infrastructure.mapper;

import org.emprenApp.{{modulo}}.application.dto.{{Entidad}}DTO;
import org.emprenApp.{{modulo}}.infrastructure.response.{{Entidad}}Response;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface {{Entidad}}InfrastructureMapper {
    {{Entidad}}InfrastructureMapper INSTANCE = Mappers.getMapper({{Entidad}}InfrastructureMapper.class);

    {{Entidad}}Response toResponse({{Entidad}}DTO {{entidad}}DTO);
}
```

## infrastructure/controller/{{Entidad}}Controller.java

```java
package org.emprenApp.{{modulo}}.infrastructure.controller;

import org.emprenApp.{{modulo}}.application.{{Entidad}}Adapter;
import org.emprenApp.{{modulo}}.application.dto.{{Entidad}}DTO;
import org.emprenApp.{{modulo}}.infrastructure.mapper.{{Entidad}}InfrastructureMapper;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}CreateRequest;
import org.emprenApp.{{modulo}}.infrastructure.request.{{Entidad}}UpdateRequest;
import org.emprenApp.{{modulo}}.infrastructure.response.{{Entidad}}Response;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/{{ruta}}")
public class {{Entidad}}Controller extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger({{Entidad}}Controller.class);

    @Autowired
    private {{Entidad}}Adapter {{entidad}}Adapter;

    @PostMapping("/")
    public ResponseEntity<{{Entidad}}Response> create{{Entidad}}(@RequestBody @Validated {{Entidad}}CreateRequest request) throws BaseException {
        logger.info("Creando {{entidad}}: {}", request.getNombre());
        {{Entidad}}DTO created = {{entidad}}Adapter.create{{Entidad}}(request);
        return responseCreated({{Entidad}}InfrastructureMapper.INSTANCE.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<{{Entidad}}Response> get{{Entidad}}(@PathVariable Long id) throws BaseException {
        {{Entidad}}DTO dto = {{entidad}}Adapter.get{{Entidad}}ById(id);
        return responseOk({{Entidad}}InfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<{{Entidad}}Response>> get{{Entidad}}sActivos(Pageable pageable) throws BaseException {
        Page<{{Entidad}}DTO> page = {{entidad}}Adapter.get{{Entidad}}sActivos(pageable);
        return responseOk(page.map({{Entidad}}InfrastructureMapper.INSTANCE::toResponse));
    }

    @PutMapping("/")
    public ResponseEntity<{{Entidad}}Response> update{{Entidad}}(@RequestBody @Validated {{Entidad}}UpdateRequest request) throws BaseException {
        logger.info("Actualizando {{entidad}}: {}", request.getId());
        {{Entidad}}DTO updated = {{entidad}}Adapter.update{{Entidad}}(request);
        return responseOk({{Entidad}}InfrastructureMapper.INSTANCE.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete{{Entidad}}(@PathVariable Long id) throws BaseException {
        logger.info("Eliminando {{entidad}}: {}", id);
        return responseOk({{entidad}}Adapter.delete{{Entidad}}(id));
    }
}
```

## Bloque para scripts.sql (agregar al final)

```sql
CREATE TABLE emprenapp.{{tabla}} (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      nombre VARCHAR(100) NOT NULL,
                                      descripcion VARCHAR(255),
                                      active BOOLEAN NOT NULL DEFAULT TRUE,
                                      PRIMARY KEY (id),
                                      INDEX idx_{{tabla}}_active (active),
                                      INDEX idx_{{tabla}}_nombre (nombre)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

Con FK, sumar la columna y el constraint siguiendo `productos`:

```sql
    categoria_id BIGINT NOT NULL,
    CONSTRAINT fk_{{tabla}}_categoria FOREIGN KEY (categoria_id)
        REFERENCES emprenapp.categorias (id) ON DELETE RESTRICT ON UPDATE CASCADE,
```

## Diferencias conscientes con `categoria/`

- `getXxxsActivos` devuelve `Page` con `Pageable` (regla de `CLAUDE.md`); `Categoria` hoy devuelve `List`.
- El controller de listado responde `Page<Response>`, no `List<DTO>` (no filtra el DTO al cliente).
- `delete` en el controller no loguea dos veces.
- Se omite el `@AfterMapping` que normaliza espacios en el nombre; agregarlo solo si el usuario lo pide.
