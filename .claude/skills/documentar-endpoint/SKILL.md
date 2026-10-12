---
name: documentar-endpoint
description: >-
  Revisa toda la app y reporta qué módulos, controllers, endpoints y campos de request/response
  no tienen documentación OpenAPI (springdoc), propone las anotaciones @Tag, @Operation,
  @ApiResponse, @Parameter y @Schema que faltan y las aplica solo después de que el usuario
  confirme. Usar cuando pidan documentar la API, completar Swagger, ver el estado de la
  documentación o documentar un endpoint o módulo nuevo.
---

# documentar-endpoint

Dos fases: **(1) estado**: qué falta documentar en toda la app; **(2) propuesta y aplicación**: proponer los cambios y editar solo lo que el usuario confirme.

Springdoc 2.5.0 ya está en el `pom.xml`, y las anotaciones `io.swagger.v3.oas.annotations.*` vienen incluidas. No hace falta tocar `pom.xml`.

## Fase 1: estado (siempre primero, solo lectura)

Desde la raíz del proyecto (PowerShell):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .claude/skills/documentar-endpoint/scripts/Estado-Documentacion.ps1 -Root . -Detalle
```

Parámetros: `-Modulo categoria` (un solo módulo), `-Detalle` (lista cada faltante con `archivo:línea`), `-SourceDir`.

El script recorre todos los módulos de `src/main/java/org/emprenApp/` y por cada uno informa:

- **Controller:** `@Tag` a nivel clase; por endpoint, `@Operation` y `@ApiResponse`/`@ApiResponses`.
- **Request / Response / DTO expuesto por un controller:** campos con `@Schema`.

Mostrar al usuario, **en español**, una tabla por módulo (o una sola tabla resumen si son muchos):

| Módulo | Controller | @Tag | @Operation | @ApiResponse | Campos con @Schema |
|---|---|---|---|---|---|

más el resumen total y la lista de **entidades/módulos que faltan documentar**, ordenados de mayor a menor cantidad de faltantes. Si el módulo está completo, marcarlo como OK.

Límite: el script es estático y por regex. Detecta presencia de la anotación, no que el texto sea bueno. Un `@Operation(summary = "")` cuenta como documentado.

## Fase 2: propuesta (sin editar todavía)

1. Leer **antes de proponer** cada controller y request/response a documentar. Describir lo que el código **realmente hace**, no lo que debería hacer.
2. Presentar la propuesta **por módulo**, con un ejemplo concreto del controller (un endpoint completo) y la lista de campos con su descripción y ejemplo. No pegar los 260 cambios de una vez: empezar por el módulo con más faltantes o el que el usuario elija.
3. Preguntar: "¿Aplico el módulo `<x>`?". Opciones útiles: aplicar todo el módulo, solo controller, solo request/response, saltear.
4. **No editar nada hasta recibir confirmación explícita.** La confirmación de un módulo no vale para los demás.

## Fase 3: aplicar (solo lo confirmado)

Con `Edit`, **solo agregar** anotaciones e imports. No cambiar firmas, rutas, lógica ni nombres.

Después de aplicar un módulo:

1. `mvn compile` (PowerShell) para confirmar que compila.
2. Volver a correr el script con `-Modulo <x>` y mostrar el nuevo estado.
3. Recordar que se puede ver el resultado en `http://localhost:8080/swagger-ui.html`.
4. No ejecutar comandos `git` que modifiquen el repositorio.

## Convenciones de documentación

Idioma: **español**, igual que el dominio. Frases cortas, sin repetir el nombre del método.

### Controller

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.emprenApp.shared.application.BaseResponse;

@Tag(name = "Categorías", description = "Alta, consulta, modificación y baja lógica de categorías de productos")
@RestController
@RequestMapping("v1/categoria")
public class CategoriaController extends BaseRestController {

    @Operation(summary = "Crear categoría", description = "Crea una categoría activa. El nombre no puede repetirse entre categorías activas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada"),
        @ApiResponse(responseCode = "400", description = "Cuerpo inválido (campo obligatorio ausente)",
                content = @Content(schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error de validación de negocio o error interno",
                content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/")
    public ResponseEntity<CategoriaResponse> createCategoria(...)

    @Operation(summary = "Obtener categoría por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "No existe una categoría activa con ese id",
                content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> getCategoria(
            @Parameter(description = "Id de la categoría", example = "1") @PathVariable Long id)
```

### Request / Response / DTO

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear una categoría")
@Data
public class CategoriaCreateRequest {

    @Schema(description = "Nombre de la categoría. Único entre las activas", example = "Panificados", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String nombre;

    @Schema(description = "Descripción opcional", example = "Pan, facturas y tortas", maxLength = 255)
    private String descripcion;
}
```

### Reglas

- **`@Tag` por controller** (`name` en plural y legible, `description` de una línea).
- **`@Operation`:** `summary` corto en infinitivo ("Crear...", "Listar..."); `description` solo si hay una regla de negocio que el nombre no dice (unicidad, baja lógica, filtro por activos).
- **`@ApiResponse`:** documentar los códigos que **de verdad** devuelve hoy el endpoint (ver abajo). Para cuerpos de error usar `BaseResponse`.
- **`@Parameter`** en cada `@PathVariable` / `@RequestParam`, con `description` y `example`. En endpoints paginados documentar `page`, `size`, `sort`.
- **`@Schema` por campo:** `description` y `example`; `maxLength` copiado de la validación o `@Column(length)`; `requiredMode` si el campo tiene `@NotNull`/`@NotBlank`.
- Valores `example` creíbles para el dominio (emprendimientos locales de Argentina), nunca datos reales de personas, credenciales ni secretos.
- Campos sensibles (`password`): no poner ejemplos con valores reales; indicar solo `description`, y marcar `accessMode = Schema.AccessMode.WRITE_ONLY` en los responses/DTOs si aparece.
- No duplicar `@Schema` en el DTO y el response si el DTO no se expone: documentar request y response, y el DTO solo cuando un controller lo devuelve directamente.

### Códigos HTTP reales hoy (verificar antes de documentar)

Según `GlobalHandlerException`:

| Situación | Excepción | HTTP real |
|---|---|---|
| Recurso inexistente o dado de baja | `NotFoundException` | 404 |
| `@Validated` falla sobre el body | `MethodArgumentNotValidException` (handler por defecto de Spring) | 400 |
| Validación de negocio (`ValidateGeneric`, duplicados) | `BaseException` / `ValidationException` | **500** |
| Error no esperado | `GenericException` / cualquier otra | 500 |

**Trampa conocida:** `ValidationException` está anotada 406 pero responde 500, y `BaseException` (p. ej. `DUPLICATE_KEY`) cae en el handler genérico y también da 500. Documentar el 500 **tal cual** y avisarle al usuario que esos casos deberían ser 400/409: arreglarlo es un cambio de comportamiento aparte y no lo hace esta skill.

## Qué no hace

- No cambia comportamiento, rutas ni códigos de respuesta.
- No agrega dependencias ni configuración global (`OpenAPI` bean, esquemas de seguridad). Si el usuario lo quiere, es otra tarea (y no hay Spring Security todavía).
- No edita nada sin confirmación.
- No toca `application.yml`.
