# CLAUDE.md

## Proyecto

EmprenApp: marketplace / red social de emprendimientos locales (perfil, catálogo, pedidos, logística). **Alcance actual: solo backend**; no hay frontend ni mobile.

Stack: Java + Spring Boot 3.3.2 (web, data-jpa, validation), MySQL 8 en runtime, H2 en tests, MapStruct 1.5.5, Lombok 1.18.30, springdoc-openapi 2.5.0, Maven.

## Comandos (PowerShell)

No hay `mvnw`; se usa `mvn` instalado (Maven 3.9.2).

```powershell
mvn spring-boot:run                 # API en :8080, Swagger en /swagger-ui.html
mvn clean package                   # target\emprenApp-0.0.1.jar
mvn compile                         # regenera los *MapperImpl de MapStruct
mvn test -Dtest=ClaseTest#metodo    # un solo test (perfil H2)
```

- Requiere MySQL en `localhost:3306`; el esquema se crea a mano con `scripts.sql` (crea la base `emprenapp`). `data.sql` está vacío.
- No hay linter. Tests unitarios: solo `categoria` (`CategoriaServiceTest`, 25 casos, JUnit 5 + Mockito + AssertJ); el resto de los módulos no tiene tests todavía. `src/test/resources/application.yml` es el perfil de test.
- Java: el `pom.xml` declara `java.version` 21 pero el `maven-compiler-plugin` fija `source/target` 16 (gana el plugin). Es una inconsistencia conocida; no se corrige sin decisión del usuario.

## Arquitectura

Hexagonal por módulo en `src/main/java/org/emprenApp/<modulo>/`:

- `domain/`: entidad JPA + `XxxRepository` (Spring Data).
- `application/`: `XxxAdapter` (puerto, interfaz), `service/XxxService` (`@Service`, implementa el puerto), `dto/`, `mapper/` (MapStruct entidad↔DTO). Algunos módulos suman `XxxValidationService`.
- `infrastructure/`: `controller/` (`@RestController`, ruta `v1/<recurso>`), `request/`, `response/`, `mapper/` (MapStruct DTO↔request/response).

Convenciones:

- Los controllers extienden `BaseRestController` (`responseOk/Created/Message`) y dependen del `Adapter`, nunca del `Service`.
- Validaciones con `ValidateGeneric` (`validateId`, `validateNotNull`, `validateNotBlank`, `validateEmail`, `validateMaxLength`); validaciones de negocio por módulo en su `ValidationService`.
- Errores: lanzar `BaseException` / `NotFoundException` / `GenericException` / `ValidationException` con un `ErrorCodeEnum`; `GlobalHandlerException` los traduce a `BaseResponse{code}`. Los servicios con `try/catch` relanzan `BaseException` y envuelven el resto en `GenericException`.
- Baja lógica en todo: `estado = INACTIVO` / `active = false` / `activo = false`; las consultas filtran por el flag activo. No hay `DELETE` físico.
- Listados con `Pageable` / `Page<T>`.
- Mappers con `INSTANCE` estático (`Mappers.getMapper`), no inyectados.

Para agregar un módulo nuevo, copiar la estructura de un módulo completo (p. ej. `producto/` o `categoria/`) y registrar sus tablas en `scripts.sql`. Un campo nuevo suele tocar entidad, DTO, request/response y los dos mappers.

## Trampas verificadas (revisar antes de confiar)

- `UserController`: `GET /{id}` y `GET /{email}` tienen el mismo patrón de ruta.
- `PedidoController`: la ruta es `/usuario/{id}` pero el parámetro es `@PathVariable Long userId`.
- `AppConfig` define un `DataSource` con `DriverManagerDataSource` que deja sin efecto `spring.datasource.hikari.*`.
- `PedidoService.savePedido` está vacío y `getAllPedidos` devuelve `null`; `DetallePedidoService` valida stock pero no lo descuenta ni lo repone, y `obtenerDetallesPorPedido` devuelve `null`.
- `Repartidor` es un stub (service y controller vacíos). `Emprendimiento` no tiene relación con `User` en la entidad aunque la tabla tiene `user_id`.
- Hay dos `EmprendimientoAdapter`: el vigente en `emprendimiento/application/` y uno legacy en `emprendimiento/` (sin uso).
- `GlobalHandlerException`: el handler de `ValidationException` está anotado 406 pero responde 500.
- No hay Spring Security ni hash de contraseñas.

## Nombres (inconsistencias existentes)

- Dominio en español (`Pedido`, `Emprendimiento`, `Repartidor`, `Localidad`), pero con mezcla: `User`/`UserService`/ruta `v1/user` y `Producto` con ruta `v1/product`; `ProductCreateRequest`/`ProductResponse` frente a `ProductoDTO`.
- Campos mezclados: `name`/`description` (Emprendimiento) vs `nombre`/`descripcion` (Categoria); flag de baja `active` vs `activo` vs `estado`; `status` (Pedido) vs `estado` (resto).
- Paquete `detalle_pedido` con guion bajo; paquete `shared.application.application`.
- Al tocar un módulo, seguir la convención de ese módulo; no renombrar sin pedirlo.

## Reglas de trabajo

- **Git:** no ejecutes `git commit`, `git push`, `git add`, ni ningún comando de git que modifique el repositorio (ramas, merge, rebase, reset, stash, checkout de ramas, tags, remotos), salvo que el usuario lo pida explícitamente en ese mismo mensaje. Solo lectura (`git status`, `git diff`, `git log`). Si necesitás otro, pedí permiso antes.
- **Base de datos:** `ddl-auto` es `validate`; si entidad y `scripts.sql` difieren, la app no arranca. Si cambiás una entidad, actualizá `scripts.sql` en el mismo cambio y avisá. No toques scripts SQL ya aplicados sin preguntar.
- **Seguridad:** nunca escribas credenciales ni secretos en CLAUDE.md, código ni docs; van por variables de entorno. Si ves credenciales en `application.yml`, no las copies a ningún archivo y no edites ese archivo: el usuario las saca a mano.
- **Entorno:** Windows y PowerShell. Responder siempre en español.
- **Alcance:** solo backend. Pedir confirmación antes de refactors grandes.
- **Tests:** se testea módulo a módulo y a pedido del usuario; no agregues tests a un módulo que el usuario no haya indicado. Plantilla: `categoria/application/service/CategoriaServiceTest.java`. Convenciones:
  - `@ExtendWith(MockitoExtension.class)`, repositorio con `@Mock`, `XxxValidationService` real con `@Spy`, servicio con `@InjectMocks`.
  - `@DisplayName("CREATE|GET|PUT|DELETE: descripción")`.
  - No stubbear lo que el servicio no alcanza a llamar (Mockito estricto falla con `UnnecessaryStubbingException`); si la validación corta antes, usar `verify(repo, never()).save(...)`.
  - Errores: aseverar `((BaseException) e).getCode()` contra `ErrorCodeEnum.X.getCode()`.
  - Baja lógica: capturar con `ArgumentCaptor` el argumento de `save` y verificar `active=false` y que no hubo `delete`/`deleteById`.

## Documentación

- `docs/CONTEXTO_PRODUCTO.md`: idea del producto, modelo de datos objetivo, wishlist funcional y próximos pasos.
- `docs/01-diagrama Entidad-Relacion (2).drawio`: diagrama ER de todas las entidades; el borde violeta marca las del MVP.
- `scripts.sql`: esquema MySQL de referencia.
- `src/main/resources/application.yml`: configuración de runtime; `src/test/resources/application.yml`: perfil de test.
