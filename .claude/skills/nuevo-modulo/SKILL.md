---
name: nuevo-modulo
description: >-
  Genera un módulo hexagonal CRUD completo de EmprenApp (entidad, repositorio, Adapter, Service,
  ValidationService, DTO, mappers MapStruct, request/response y controller) copiando la estructura
  de categoria/ y agrega la tabla al final de scripts.sql. Usar cuando el usuario pida crear un
  módulo, entidad o recurso nuevo, por ejemplo "nuevo módulo Cliente" o "agregá la entidad Reseña".
---

# nuevo-modulo

Crea un módulo nuevo en `src/main/java/org/emprenApp/<modulo>/` con el CRUD básico y baja lógica,
siguiendo exactamente las convenciones de `categoria/` (el módulo de referencia).

Las plantillas con el código están en [plantillas.md](plantillas.md). Leerlas antes de generar.

## Paso 1: reunir datos (preguntar solo lo que falte)

Necesito del usuario, y si no lo dijo lo pregunto en **un solo mensaje**:

| Dato | Ejemplo | Default si no responde |
|---|---|---|
| Nombre de la entidad (singular, PascalCase) | `Cliente` | obligatorio |
| Campos: nombre, tipo Java, nullable, longitud | `nombre String 100 not null` | obligatorio |
| Flag de baja lógica | `active` / `activo` / `estado` | `active` (Boolean), como `Categoria` |
| Ruta REST | `v1/cliente` | `v1/<entidad en minúsculas>` |
| Campo con unicidad (validar duplicados) | `nombre` | ninguno |
| Relaciones `@ManyToOne` hacia otras entidades | `emprendimiento_id` | ninguna |

Derivar: `<modulo>` = entidad en minúsculas (paquete), `<tabla>` = plural en minúsculas con
snake_case (`categorias`, `productos`). Si la entidad ya existe en `src/main/java/org/emprenApp/`,
**parar y avisar**: no sobrescribir un módulo existente.

## Paso 2: generar los archivos

Crear (usando las plantillas) bajo `src/main/java/org/emprenApp/<modulo>/`:

```
domain/<Entidad>.java
domain/<Entidad>Repository.java
application/<Entidad>Adapter.java
application/service/<Entidad>Service.java
application/service/<Entidad>ValidationService.java
application/dto/<Entidad>DTO.java
application/mapper/<Entidad>Mapper.java
infrastructure/controller/<Entidad>Controller.java
infrastructure/request/<Entidad>CreateRequest.java
infrastructure/request/<Entidad>UpdateRequest.java
infrastructure/response/<Entidad>Response.java
infrastructure/mapper/<Entidad>InfrastructureMapper.java
```

Reglas que no se negocian (vienen de `CLAUDE.md`):

- El controller depende del **`Adapter`**, nunca del `Service`, y extiende `BaseRestController`.
- Mappers con `INSTANCE` estático (`Mappers.getMapper`), no inyectados.
- Baja lógica: `DELETE` marca el flag en falso; todas las consultas filtran por el flag activo. **Nada de `DELETE` físico.**
- Errores con `BaseException` / `NotFoundException` / `GenericException` y un `ErrorCodeEnum`. Si hace falta un código nuevo, **agregarlo a `ErrorCodeEnum`** con el siguiente número libre (el `99` queda para `GENERIC_ERROR`) y avisarlo.
- Validaciones con `ValidateGeneric` y las de negocio en `<Entidad>ValidationService`.
- Listado paginado con `Pageable` / `Page<T>`.
- Los `Service` con `try/catch` relanzan `BaseException` y envuelven el resto en `GenericException`.
- Imports reales: `ValidateGeneric` está en `org.emprenApp.shared.application.application`; las excepciones en `org.emprenApp.shared.application.exception`; `ErrorCodeEnum` en `org.emprenApp.shared.application.enums`.
- Nombres de campos en español, igual que `Categoria`. No renombrar nada existente.
- No agregar tests (están fuera de alcance según `CLAUDE.md`).

## Paso 3: tabla en `scripts.sql`

1. Leer `scripts.sql` y respetar su estilo (esquema `emprenapp.`, `ENGINE=InnoDB`, `utf8mb4`, `utf8mb4_unicode_ci`, índice por flag y por nombre).
2. **Solo agregar al final** un `CREATE TABLE`. No modificar ni reordenar sentencias existentes (`CLAUDE.md`: no tocar SQL ya aplicado sin preguntar).
3. Si el módulo tiene FK, la tabla referenciada debe crearse antes: el nuevo `CREATE` va al final, así que se cumple siempre.
4. Avisar al usuario que editar el archivo **no cambia la base**: debe ejecutar el `CREATE TABLE` en MySQL, o `ddl-auto: validate` impedirá arrancar.

## Paso 4: verificar

1. Ejecutar `mvn compile` (PowerShell) para regenerar los `*MapperImpl`. Si falla, leer el error y corregir el módulo nuevo, no tocar otros.
2. Invocar la skill `verificar-esquema` para confirmar que entidad y `scripts.sql` coinciden.
3. No ejecutar `git add` ni `git commit` (regla de `CLAUDE.md`).

## Paso 5: informar

Responder en español con: lista de archivos creados, tabla agregada a `scripts.sql`, códigos de error nuevos si hubo, resultado de `mvn compile`, y los endpoints resultantes:

```
POST   /v1/<ruta>/        crear
GET    /v1/<ruta>/{id}    obtener
GET    /v1/<ruta>/list    listar activos (paginado)
PUT    /v1/<ruta>/        actualizar
DELETE /v1/<ruta>/{id}    baja lógica
```

Sugerir correr `documentar-endpoint` y `auditar-convenciones` sobre el módulo nuevo.

## Fuera de alcance

Lógica de negocio específica del módulo, relaciones `@OneToMany`, seguridad, tests, migraciones versionadas.
