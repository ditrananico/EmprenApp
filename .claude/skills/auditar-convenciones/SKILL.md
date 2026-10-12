---
name: auditar-convenciones
description: >-
  Audita que el código de EmprenApp cumpla las convenciones de arquitectura de CLAUDE.md:
  controllers que dependen del Adapter y no del Service, baja lógica sin DELETE físico,
  consultas filtradas por activo, listados con Pageable, mappers con INSTANCE, errores con
  ErrorCodeEnum y rutas v1 sin duplicados. Solo informa, no modifica archivos. Usar al revisar
  un módulo nuevo o tocado, antes de un PR, o cuando pidan auditar, revisar convenciones o
  verificar la arquitectura.
---

# auditar-convenciones

Un solo problema: **¿el código sigue las convenciones que `CLAUDE.md` declara?** Hoy nada las hace cumplir (no hay linter ni tests), y con módulos tan repetitivos un desvío se copia fácil.

Solo lee e informa. No corrige archivos.

## Cómo ejecutarla

Desde la raíz del proyecto (PowerShell):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .claude/skills/auditar-convenciones/scripts/Auditar-Convenciones.ps1 -Root .
```

Parámetros opcionales: `-Modulo producto` (un solo módulo), `-SourceDir`.

Código de salida: `0` sin `ERROR`, `1` con `ERROR`, `2` error de uso.

## Reglas que verifica

| Regla | Nivel | Qué detecta |
|---|---|---|
| **C1** controller-usa-service / repository | ERROR | El controller importa un `Service` o `Repository` en vez de depender del `Adapter`. |
| **C2** base-controller | AVISO | El controller no extiende `BaseRestController`. |
| **C3** controller-expone-entidad | AVISO | El controller importa una entidad de `domain`; debería usar request/response/DTO. |
| **C4** ruta-sin-v1 | AVISO | La ruta base no empieza con `v1/`. |
| **C5** ruta-duplicada | ERROR | Mismo verbo y patrón de ruta en un controller (p. ej. `GET /{id}` y `GET /{email}`). |
| **C6** pathvariable-no-coincide | ERROR | `@PathVariable` sin nombre explícito cuyo parámetro no coincide con la variable de la ruta. |
| **C7** listado-sin-paginar | AVISO | `GET` que devuelve `List<...>` sin `Pageable`. |
| **S1** service-sin-adapter | AVISO | Un `Service` no implementa un `Adapter`. |
| **S2** delete-fisico | ERROR | `repository.delete*()` o métodos `deleteBy...` en un repositorio. Debe ser baja lógica. |
| **S3** consulta-sin-filtro-activo | AVISO | `findAll` / `findById` / `existsById` / `count` que ignoran el flag activo. |
| **S4** excepcion-generica | AVISO | `throw new RuntimeException/IllegalArgumentException/...` en vez de `BaseException` con `ErrorCodeEnum`. |
| **M1** mapper-sin-instance | AVISO | `@Mapper` sin `INSTANCE = Mappers.getMapper(...)`. |
| **M2** mapper-inyectado / componentModel spring | AVISO | Mapper inyectado o con `componentModel = "spring"`. |
| **E1** entidad-sin-flag-baja | AVISO | Entidad sin `active` / `activo` / `estado` / `status`. |

Excluye `shared/` y `configuration/`.

## Qué hacer con el resultado

1. Mostrar el reporte **en español**: empezar por el veredicto (cantidad de `ERROR` y `AVISO`), luego los `ERROR` con `archivo:línea`, y después los `AVISO` agrupados por regla y no uno por uno si son muchos.
2. **Triar los `AVISO` antes de presentarlos:** son heurísticas y algunos son intencionales. Para `S3` (`findById` sin filtro) abrir el código: es legítimo cuando se busca por id una entidad relacionada, o cuando la entidad no tiene flag de baja; es un problema real cuando un endpoint puede devolver un registro dado de baja. Decir cuáles confirmaste y cuáles no revisaste.
3. Para cada `ERROR` real, proponer el arreglo en pantalla y **esperar confirmación**. No editar sin que el usuario lo pida.
4. Si un hallazgo coincide con una trampa ya documentada en `CLAUDE.md` (ruta duplicada en `UserController`, `@PathVariable userId` en `PedidoController`), decirlo para que el usuario no lo tome como algo nuevo.
5. Respetar `CLAUDE.md`: al corregir, seguir la convención del módulo y no renombrar sin pedirlo; cambios grandes piden confirmación.

## Límites

- Análisis **estático por regex**: no entiende tipos ni flujo. Un `OK` significa "no se detectó nada", no que el módulo cumpla todo.
- No valida la lógica de negocio, la seguridad ni el SQL (para eso está `verificar-esquema`).
- C6 evalúa la firma en hasta 8 líneas; firmas muy largas o con anotaciones inusuales pueden no analizarse.
- No detecta `findBy...` propios del repositorio que omitan el filtro de activo; solo las llamadas genéricas de JPA.

## Fuera de alcance

Corregir automáticamente, correr la app, tests, revisión de seguridad, documentación OpenAPI (ver `documentar-endpoint`).
