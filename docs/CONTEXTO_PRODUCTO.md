# EmprenApp — Contexto de producto, estado del backend y hoja de ruta

Documento de referencia. Las secciones **Estado actual** y **Hoja de ruta** son una foto del código al **2026-10-11**, rama `feature/nuevas-entidades` (último commit `1304d24`). Si el código cambia, revisar contra el código. Para reglas de trabajo, ver `CLAUDE.md`.

## Idea

Red social / marketplace de emprendimientos locales para administrar una PyME, potenciar ventas y automatizar tareas. Destinada a **emprendedores**, para visualizar su emprendimiento (perfil, catálogo, publicaciones) y permitir el contacto con usuarios finales, gestión de pedidos y logística de entrega.

Roles previstos: Administrador del sistema · Usuario final · Usuario emprendedor · Usuario sin sesión · Moderador · Repartidor.

---

## 1. Estado actual por módulo

Leyenda: ✅ completo · 🟡 parcial / con bugs · 🔴 stub o vacío · ⬜ no existe.

### 1.1 Entidades JPA existentes (9)

| Módulo | Ruta base | Estado | Qué hay | Qué falta / problemas |
|---|---|---|---|---|
| `categoria` | `v1/categoria` | ✅ | CRUD + listado de activas, baja lógica (`active`) | Sin paginación (devuelve `List`); sin datos semilla (`data.sql` vacío) → nadie puede crear un producto hasta cargar categorías. Sin rol admin |
| `localidad` | `v1/localidad` | ✅ | Get por id, provincias, partidos, búsqueda, create/update/delete | Comentarios `// Rol Admin` sin enforcement. Seed solo con 25 de Mayo |
| `ubicacion` | `v1/ubicacion` | ✅ | Create, get, listar por usuario, update, delete lógico, dirección "predeterminada" | Cualquiera lee/edita ubicaciones de cualquier usuario (sin auth). No hay flujo que cree el *snapshot* de ubicación para un pedido ni que asigne ubicación a un emprendimiento |
| `producto` | `v1/product` | 🟡 | Create, get, update, delete lógico, por categoría, por emprendimiento, búsqueda | `update`/`delete` usan `findById` sin filtrar `active` (se puede editar/borrar uno ya dado de baja); `createProducto` no exige emprendimiento ACTIVO; `POST` devuelve 200 en vez de 201; el `PUT` del controller se traga `GenericException` y devuelve 500 vacío; sin imagen; sin filtro por precio/rating; no hay endpoint de ajuste de stock; `getProductosByCategoria` y `getProductosByEmprendimiento` devuelven `ProductoDTO` directo (no Response) |
| `emprendimiento` | `v1/emprendimiento` | 🟡 | Create, get, listar, update (name/description), delete lógico | **Bloqueante:** la entidad no mapea `user_id` pero la columna es `NOT NULL` en `scripts.sql` → el `INSERT` falla. No asigna `ubicacion` al crear/actualizar. Falta endpoint de cambio de estado (`// AGREGAR MÉTODO CAMBIAR ESTADO`). El controller no extiende `BaseRestController`. Hay un `EmprendimientoAdapter` legacy sin uso. Valida `id < 0` (permite 0). Sin `@Transactional`. Sin ownership (cualquiera edita cualquier emprendimiento) |
| `user` | `v1/user` | 🟡 | Create, get por id/email, listar, delete lógico, update, "update status" | Ver bugs abajo (§1.3) |
| `pedido` | `v1/pedido` | 🔴/🟡 | Get por id, listar por usuario y por emprendimiento (filtro opcional de estado), `PATCH` estado, cancelar | **`savePedido` vacío y `getAllPedidos` devuelve `null`: no se puede crear un pedido.** No hay `PedidoCreateRequest`/`Response`. `updateStatus` acepta cualquier transición y no setea `fechaFinProceso`/`fechaFinalizacion`. `cancelPedido` no repone stock. `getAllPedidoByEmprendimientoIDAndStatus` no valida que el emprendimiento exista. `/usuario/{id}` con `@PathVariable Long userId` → falla en runtime. `total`/`costoEnvio` nunca se calculan |
| `detalle_pedido` | `v1/detalle-pedido` | 🟡 | Get detalles por pedido, agregar ítems, baja lógica de ítem | `obtenerDetallesPorPedido` devuelve `null`. Valida stock pero **no lo descuenta ni lo repone**. No recalcula `total` del pedido. No valida que el producto pertenezca al emprendimiento del pedido ni que esté activo. El `catch (Exception)` de `agregarDetallePedido` convierte los `BaseException` (stock insuficiente, duplicado) en `GENERIC_ERROR`. No hay modificar cantidad |
| `repartidor` | `v1/repartidor` | 🔴 | Entidad completa (usuario, vehículo, emprendimiento, modalidad de cobro/liquidación, variable de cobro), repositorio, enums | `RepartidorService`, `RepartidorAdapter` y `RepartidorController` están **vacíos**. No hay DTO, mappers ni requests/responses. Sin flag de baja ni de disponibilidad |

### 1.2 Entidades del modelo objetivo que **no existen todavía**

Nada de esto tiene entidad, tabla en `scripts.sql`, service ni controller.

| Entidad | Para qué | Depende de |
|---|---|---|
| `Rol` / `RolUsuario` | Autorización por rol (admin, moderador, emprendedor, repartidor, final) | `User` |
| `Archivo` | Imágenes de producto, perfil, emprendimiento, publicación | `Emprendimiento`, `Producto`, `User` |
| `Horarios` | Días/horas de atención del emprendimiento | `Emprendimiento` |
| `DetalleEmprendimiento` | Zona y tipo de cobertura (Partido / Distancia) | `Emprendimiento`, `Localidad` |
| `Publicacion` / `DetallePublicacion` | Posteos con producto/promoción, boost | `Emprendimiento`, `Producto`. **Existe implementación en `origin/feature/publicacion` sin mergear** (ver §1.4) |
| `Promocion` | Descuentos (porcentual, unidad, fijo, envío gratis, medio de pago) | `Producto`, `Publicacion`, `Pedido` |
| `Resena` | Puntaje 1–5 a emprendimiento/repartidor por pedido | `Pedido` finalizado |
| `Comentario` | Comentarios en publicaciones | `Publicacion`, `User` |
| `Like` | +1/−1 sobre reseña/publicación/comentario | las tres anteriores |
| `Follow` | Seguir emprendimientos | `User`, `Emprendimiento` |
| `Reporte` | Denuncia de publicaciones + moderación | `Publicacion`, `Rol` moderador |
| `Notificacion` | Avisos (pedido, promo, comentario, reporte) | casi todo lo anterior |
| `Balance` / `HistoricoBalance` | Liquidaciones emprendimiento–repartidor por pedido | `Pedido`, `Repartidor` |
| `Moneda` ("Monedita") | Saldo/puntos internos | `User` |
| `Propiedad` / `PropiedadUsuario` | Parámetros genéricos (p. ej. límite de distancia) | `User`, `DetalleEmprendimiento` |
| `Carrito` | **No hace falta tabla propia**: se resuelve con `Pedido` en estado `BORRADOR` (ya soportado por `DetallePedido`). Decidir y documentar | `Pedido` |

> `EXCEPCIONES` del `.drawio` se cubre hoy con `ErrorCodeEnum`; no requiere entidad.

### 1.3 Bugs y deuda transversal detectados (afectan a "todo el back al 100%")

**Usuarios**
- `UserService.updateUser` convierte el request en una entidad nueva y la guarda: no carga la existente, por lo que puede pisar/perder `id`, `password`, `fechaCreacion`. Verificar y corregir.
- `UserService.updateStatusUser` busca solo `ACTIVO` y vuelve a setear `ACTIVO`: es un no-op y **no permite reactivar** un usuario `INACTIVO`.
- `createUser` no controla email duplicado (existe `DUPLICATE_KEY` sin uso) y guarda la contraseña en texto plano.
- Rutas `GET /{id}` y `GET /{email}` colisionan; update por email en el body y por id en otra ruta (`/edit`, `/edit/{id}`).

**Manejo de errores**
- `GlobalHandlerException` no tiene handler para `BaseException` genérica: `new BaseException(INVALID_PARAMETERS)` (usado en `DetallePedidoService`) responde 500 `GENERIC_ERROR`.
- El handler de `ValidationException` está anotado 406 pero responde 500.
- `ErrorCodeEnum` es chico (10 códigos). Faltan códigos específicos: stock insuficiente, transición de estado inválida, pedido sin ítems, emprendimiento inactivo, email duplicado, etc.

**Datos / configuración**
- `scripts.sql` ↔ entidades: `emprendimientos.user_id` sin mapear (ver arriba). `Pedido.total`/`costoEnvio` son nullable en la entidad y `NOT NULL` en SQL. `Producto.stock`/`active` nullable en entidad vs `NOT NULL` en SQL.
- `AppConfig` define un `DataSource` con `DriverManagerDataSource` que ignora `spring.datasource.hikari.*`.
- `data.sql` vacío: no hay categorías ni roles ni localidades salvo el seed de `scripts.sql`.
- Inconsistencia Java 21 (pom) vs 16 (compiler plugin); decisión del usuario pendiente.
- Falta `@Transactional` en `EmprendimientoService`, `PedidoService`, `UserService` (no hay operaciones multi-escritura hoy, pero sí las habrá con el ciclo de stock).

**Seguridad**
- Sin Spring Security, sin autenticación, sin autorización, sin hash de contraseñas, sin ownership: cualquier cliente puede leer/modificar cualquier dato. Las credenciales de `application.yml` no deben estar en el repo (las saca el usuario a mano).

### 1.4 Ramas

| Rama | Contenido | Nota |
|---|---|---|
| `origin/feature/publicacion` | Módulo `publicacion` completo-ish: entidad, repo, service, validation, mapper, controller (create/get/put), `EstadoPublicacionEnum`; también toca `UserService`, `UserController`, `UserMapper`, `GlobalHandlerException`, `ValidateGeneric`, `ErrorCodeEnum` | Divergió de `main`; el merge va a chocar en User y handler. Traer **solo** el módulo `publicacion` y revisar los cambios compartidos a mano |
| `origin/feature/emprendimiento` | Solo merges de `develop` | Sin contenido propio relevante |

---

## 2. Hoja de ruta priorizada hasta "backend 100%"

El orden respeta dependencias: nada de lo social/monetario sirve si no se puede crear un pedido de punta a punta.

### Prioridad 0 — Desbloqueantes (horas, no días)

1. **Emprendimiento ↔ User:** agregar `User usuario` (`user_id`) a la entidad, al DTO y al `CreateRequest` (`userId`); validar que el usuario exista y esté `ACTIVO`. Hoy el create falla contra MySQL.
2. **Seed mínimo** en `scripts.sql`/`data.sql`: categorías base (y roles cuando existan) para poder crear productos.
3. **`PedidoController`:** corregir `@PathVariable Long userId` ↔ `/usuario/{userId}`.
4. **`GlobalHandlerException`:** handler para `BaseException` con status correcto (400 por defecto); `ValidationException` → 400/406 real; ampliar `ErrorCodeEnum`.
5. **`DetallePedidoService.agregarDetallePedido`:** relanzar `BaseException` antes del `catch (Exception)`.
6. **`UserService`:** corregir `updateUser` (cargar entidad y mapear sobre ella) y `updateStatusUser` (reactivar/desactivar con estado explícito); validar email duplicado.

### Prioridad 1 — Núcleo: flujo de pedido completo (el "100%" real del marketplace)

1. **`PedidoService.savePedido` / crear pedido:** `PedidoCreateRequest` (usuarioId, emprendimientoId, ubicacionDestinoId, metodoPago, ítems). Validar usuario y emprendimiento activos, productos del mismo emprendimiento. Crear en `BORRADOR` (hace de carrito) o `ACEPTADO`.
2. **Snapshot de ubicación destino:** copiar la `Ubicacion` de la libreta a una fila sin `usuario_id` para que editar la libreta no altere pedidos pasados (el diseño de `UbicacionService` ya lo prevé).
3. **Cálculo de `total` y `costoEnvio`:** sumar `precioUnitario × cantidad` de ítems activos; recalcular al agregar/quitar detalle. Definir regla de `costoEnvio` (fijo, por distancia, por `Repartidor.variableDeCobro`).
4. **Ciclo de stock:** descontar al confirmar (`ACEPTADO`), reponer al cancelar o eliminar ítem; con `@Transactional` y control de concurrencia (lock optimista/pesimista en `Producto`). Alerta/flag cuando `stock < stockMinimo`.
5. **Máquina de estados de `Pedido`:** `BORRADOR → ACEPTADO → EN_PROCESO → EN_CAMINO → FINALIZADO` y `CANCELADO` desde estados permitidos; setear `fechaFinProceso`/`fechaFinalizacion`; rechazar transiciones inválidas con código propio.
6. **Completar** `getAllPedidos`, `obtenerDetallesPorPedido`, modificar cantidad de un ítem, y validar emprendimiento en el listado por emprendimiento.
7. Introducir `PedidoCreateRequest`/`PedidoResponse` y su mapper de infraestructura (hoy el controller expone `PedidoDTO`).

### Prioridad 2 — Repartidor (logística)

1. DTO, mappers, requests/responses, `RepartidorValidationService`.
2. Alta (usuario existente + vehículo + modalidades), get, listar (por emprendimiento), update, baja lógica (**agregar columna `activo`** a `repartidores`; actualizar `scripts.sql`).
3. Disponibilidad ("buscando pedidos") — columna nueva `disponible`.
4. Asignar / aceptar / rechazar repartidor en un `Pedido` (`repartidor_id`), validando estado del pedido.
5. Cálculo de comisión del repartidor según `modalidadDeCobro` (fijo, % del pedido, por distancia) — insumo del balance (P7).

### Prioridad 3 — Autenticación y autorización

1. Entidades `Rol` y `RolUsuario` (+ tablas y seed).
2. Spring Security (JWT o sesión), hash de contraseñas (BCrypt; la columna ya es `VARCHAR(60)`), login/registro, recuperar contraseña.
3. Reglas por rol: admin (localidades, categorías, moderación), emprendedor dueño (sus emprendimientos/productos/pedidos), usuario (sus ubicaciones y pedidos), repartidor.
4. Ownership en cada endpoint (hoy ninguno lo verifica). Corregir exposición de `UserDTO` con `password` en `getAllUsers`/`createUser`.

### Prioridad 4 — Emprendimiento completo

1. Endpoint de cambio de estado (`ACTIVO`/`INACTIVO`/otros).
2. Asignar/actualizar `ubicacion` (reutilizar `UbicacionService`).
3. `Horarios` (día, horaInicio, horaFin, estado) + validación "abierto ahora" al crear pedido.
4. `DetalleEmprendimiento` (zona y tipo de cobertura) y validar que la localidad destino esté dentro de cobertura al crear pedido.
5. Resolver duda abierta: ¿múltiples sucursales? (impacta si `ubicacion` es 1:1 o 1:N).
6. Listar emprendimientos por usuario, filtros (localidad, categoría), y eliminar el `EmprendimientoAdapter` legacy.

### Prioridad 5 — Archivos / imágenes

`Archivo` (url, tipo, componente, emprendimiento) + upload (storage local o S3), asociar a producto, emprendimiento, perfil de usuario y publicación. Límites de tamaño/tipo.

### Prioridad 6 — Publicaciones y promociones

1. Traer el módulo `publicacion` desde `origin/feature/publicacion` (sin los cambios compartidos) y completar: listar, filtrar (categoría, palabra, precio, método de pago, calificación), editar, baja lógica, boost.
2. `DetallePublicacion` (producto + promoción).
3. `Promocion` con sus 5 tipos, vigencia y límites; aplicar en el cálculo de total del pedido (P1.3).

### Prioridad 7 — Capa social

`Resena` (solo sobre pedido `FINALIZADO` del usuario; puntaje 1–5; promedio por emprendimiento/repartidor; sello "emprendedor confiable") → `Comentario` → `Like` → `Follow` → `Reporte` (+ endpoints de moderación, estados `pendiente | en_revision | aceptado | desestimado`).

### Prioridad 8 — Notificaciones

`Notificacion` (usuario, fecha, mensaje, url, leído, tipo). Primero emitir por eventos internos (pedido creado/cambió de estado, nuevo comentario, reporte resuelto); push/WhatsApp/email queda para después.

### Prioridad 9 — Dinero interno y liquidaciones

`Balance` / `HistoricoBalance` (liquidación emprendimiento–repartidor por pedido, según `modalidadLiquidacion`), `Moneda` ("Monedita"), medios de pago online (`MERCADO_PAGO`, `TARJETA_ONLINE` están en el enum pero sin integración), recibos/comprobantes.

### Prioridad 10 — Cierre

1. `Propiedad` / `PropiedadUsuario` (límites de distancia, preferencias).
2. Buscador general (productos + emprendimientos + publicaciones), hoy solo existe búsqueda de productos.
3. Tests (unitarios de servicios y de integración con H2) — fuera de alcance hasta pedido explícito.
4. Unificar nomenclatura (`product`→`producto`, `active/activo/estado`, `status/estado`) **solo con aprobación**; unificar `DataSource` y Java 21/16.

---

## 3. Modelo de datos objetivo

Del `.drawio` original. Entre paréntesis, estado actual en el código.

- **USUARIOS** (✅) · **ROL** / **ROL_USUARIO** (⬜)
- **EMPRENDIMIENTOS** (🟡: falta `user_id` en la entidad) · **DETALLE_EMPRENDIMIENTO**: zonaCobertura, tipoCobertura Partido/Distancia (⬜) · **HORARIOS**: dia, horaInicio, horaFin, estado, emprendimiento_id (⬜)
- **CATEGORIAS** (✅) · **PRODUCTOS** (🟡) · **ARCHIVO**: id, emprendimiento_id, url, tipo, id_componente (⬜)
- **LOCALIDADES** (✅) · **UBICACIONES** (✅)
- **PEDIDOS** (🔴) · **DETALLE_PEDIDO** (🟡) · **REPARTIDORES** (🔴)
- **PUBLICACIÓN** / **DETALLE_PUBLICACION**: título, descripción, fecha, boost, estado (⬜ en esta rama; existe en `origin/feature/publicacion`)
- **PROMOCIONES**: tipo (DESCUENTO_PORCENTUAL | DESCUENTO_UNIDAD | DESCUENTO_FIJO | ENVIO_GRATIS | DESCUENTO_MEDIO_PAGO), variable, fecha_inicio/fin, limite_cantidad, limite_monto (⬜)
- **BALANCE** / **HISTORICO_BALANCE** (⬜)
- **RESEÑA**: tipo, puntaje 1–5, fecha, usuario_id, pedido_id, repartidor_id, emprendimiento_id (⬜)
- **COMENTARIO**: usuario_id, publicacion_id, texto, fecha (⬜)
- **LIKES**: usuario_id, tipo, objetivo_id, like −1/+1 (⬜)
- **FOLLOW**: usuario_id, emprendimiento_id (⬜)
- **REPORTE**: motivo, publicacion_id, usuario_id, estado (⬜)
- **NOTIFICACION**: usuario_id, fecha, mensaje, url, leído, tipo (publicacion | descuento | pedido | comentario | reporte | otros) (⬜)
- **MONEDITA**: user_id, saldo (⬜)
- **PROPIEDADES** / **PROPIEDAD_USUARIO**: pares nombre/valor genéricos (⬜)
- **EXCEPCIONES**: cubierto por `ErrorCodeEnum` (no requiere tabla)

### Dudas abiertas

- ¿Qué pasa si un emprendimiento tiene más de una sucursal?
- ¿Dónde se guarda el límite de distancia de un producto/emprendimiento? (Se plantea como propiedad genérica; el filtro lo administra el usuario final.)
- ¿El carrito es un `Pedido` en `BORRADOR` o una entidad aparte? (Recomendado: `BORRADOR`.)
- ¿Un pedido puede mezclar productos de más de un emprendimiento? (Hoy el modelo dice que no: `pedidos.emprendimiento_id`.)
- ¿Cómo se calcula `costoEnvio` (fijo, por distancia, por repartidor)?
- ¿Autenticación por JWT o sesión? ¿Se usa proveedor externo (Google, etc.)?

---

## 4. Alcance funcional objetivo (wishlist)

- **Usuarios**: alta/baja/modificación, recuperar contraseña, perfil con foto, compartir perfil en redes.
- **Publicaciones**: crear (título, categoría, descripción, fotos, precio, ubicación, repartidor opcional), listar, filtrar (categoría, palabra, precio, método de pago, calificación del vendedor), reseñar, comentar, reportar, moderar reportes, editar, eliminar.
- **Pedidos**: seleccionar pedido, contactar al emprendedor por WhatsApp, solicitar repartidor, cancelar, devolver; ver pendientes/finalizados.
- **Carrito**: añadir/eliminar ítem, contador, métodos de pago, historial.
- **Notificaciones**: promociones, descuentos, info útil, "se generó un pedido".
- **Emprendedores**: reseñas, sello "emprendedor confiable", seguir, % histórico de reseñas, administración de stock (cantidad, precio, descripción, estado, tipo, tamaño/peso, ubicación), recibos/comprobantes.
- **Repartidores**: alta/baja de usuario repartidor, elegir vehículo, activar/desactivar "buscando pedidos", aceptar/rechazar pedido.
- **Logística**: estado del envío, número de pedido.
- **Buscador**: búsqueda general en la plataforma.

### Cobertura de la wishlist hoy

| Ítem | Estado |
|---|---|
| Usuarios: alta/baja/modificación | 🟡 (con bugs) |
| Usuarios: recuperar contraseña, foto, compartir | ⬜ |
| Publicaciones (cualquier ítem) | ⬜ en esta rama |
| Pedidos: ver pendientes/finalizados, cancelar | 🟡 |
| Pedidos: crear, solicitar repartidor, devolver, WhatsApp | ⬜ / 🔴 |
| Carrito | ⬜ (usar `BORRADOR`) |
| Notificaciones | ⬜ |
| Emprendedores: stock | 🟡 (se valida, no se descuenta) |
| Emprendedores: reseñas, sello, seguir, recibos | ⬜ |
| Repartidores (todo) | 🔴 |
| Logística: estado del envío | 🟡 (solo `EstadoPedidoEnum`) |
| Buscador: productos | ✅ · general ⬜ |

---

## 5. Resumen ejecutivo

- **Completos (3):** Categoría, Localidad, Ubicación.
- **Con funcionalidad pero incompletos o con bugs (5):** Producto, Emprendimiento, User, Pedido, DetallePedido.
- **Entidad definida sin funcionalidad (1):** Repartidor.
- **Sin nada (≈16):** Rol/RolUsuario, Archivo, Horarios, DetalleEmprendimiento, Publicacion/DetallePublicacion (hay rama), Promocion, Resena, Comentario, Like, Follow, Reporte, Notificacion, Balance/HistoricoBalance, Moneda, Propiedad/PropiedadUsuario.
- **Camino crítico:** P0 (bugs) → P1 (pedido de punta a punta) → P2 (repartidor) → P3 (seguridad). Recién después conviene abrir el frente social y monetario.

---

## 6. Cómo encarar seguridad y pruebas unitarias (criterio de diseño)

Este proyecto es, además de un producto, un vehículo de aprendizaje. Por eso el criterio de trabajo es: **cada cambio viene con su explicación y su porqué**, en pasos chicos y revisables, y las decisiones de diseño se discuten antes de escribirse. El rol esperado es el de un backend senior Java que enseña mientras construye: antes de tocar código explica qué problema resuelve, qué alternativas había y por qué se elige una; después del cambio indica cómo verificarlo y qué errores típicos evitar.

### 6.1 Seguridad (usuario / contraseña)

**Qué proteger y en qué orden.** La seguridad se piensa en tres capas que conviene no mezclar: *autenticación* (quién sos), *autorización* (qué podés hacer) y *protección del dato* (qué se guarda y qué se devuelve). Hoy las tres están en cero: la contraseña se guarda en texto plano, `UserDTO` la devuelve en las respuestas y ningún endpoint verifica quién llama. El primer paso, antes incluso de Spring Security, es de bajo costo y alto valor: **hashear contraseñas con BCrypt** (la columna `password VARCHAR(60)` ya tiene el tamaño exacto de un hash BCrypt) y **sacar `password` de todo DTO/Response**. Un hash es de un solo sentido: aunque se filtre la base, no se puede recuperar la clave original, y BCrypt agrega sal y un factor de costo configurable que vuelve caro el ataque por fuerza bruta. Nunca se usan hashes rápidos como MD5 o SHA-256 para contraseñas por esa misma razón.

**Autenticación: JWT sin estado.** Para una API REST sin frontend de sesión, la opción recomendada es **Spring Security 6 con tokens JWT firmados** (access token de vida corta, p. ej. 15–30 minutos, y más adelante un refresh token). Se elige sin estado porque el backend no necesita guardar sesiones en memoria ni en base, escala sin sticky sessions y encaja con un futuro cliente mobile o web. La alternativa, sesión con cookie, es válida pero obliga a pensar en CSRF y almacenamiento de sesión; se descarta por ahora, no por ser peor sino por no aportar a este contexto. La clave de firma del JWT es un secreto: va por variable de entorno, jamás en el código ni en `application.yml` versionado. Dos advertencias de diseño: el JWT *no se puede revocar* sin infraestructura extra (por eso vive poco), y no debe llevar datos sensibles en el payload porque va codificado, no cifrado.

**Autorización: roles y propiedad del recurso.** Cuando exista la entidad `Rol`/`RolUsuario`, la autorización se expresa en dos niveles. El primero es por rol (`ADMIN` administra categorías y localidades, `MODERADOR` resuelve reportes, etc.), con `@PreAuthorize` sobre los métodos del controller o del adapter. El segundo, y el que más se olvida, es **ownership**: un emprendedor no solo debe tener rol `EMPRENDEDOR`, debe ser dueño *de ese* emprendimiento antes de editarlo; un usuario solo ve *sus* pedidos y *sus* ubicaciones. Esto se resuelve en la capa de servicio comparando el usuario autenticado con el dueño del recurso y devolviendo 403 o 404 según corresponda. Es la causa número uno de vulnerabilidades reales en APIs (IDOR / broken object level authorization), así que se prueba explícitamente.

**Orden de implementación sugerido.** (1) BCrypt + quitar `password` de las respuestas + validar email duplicado; (2) endpoint de registro y login que devuelve el JWT; (3) filtro de Spring Security que valida el token y deja todo lo demás cerrado por defecto (*deny by default*) con una lista corta de rutas públicas (login, registro, catálogo en lectura); (4) roles; (5) ownership recurso por recurso, empezando por Pedido y Emprendimiento; (6) endurecimiento: CORS acotado, límites de intentos de login, mensajes de error que no revelen si el email existe, y bitácora de eventos de seguridad. Cada paso es desplegable por separado y deja la app funcionando. Hay que tener presente que activar Spring Security rompe todos los endpoints actuales hasta que tengan reglas, por lo que conviene hacerlo **después** de tener tests que detecten qué se rompió (ver 6.2).

### 6.2 Pruebas unitarias

**Qué se prueba y con qué.** Hoy `src/test` no tiene clases de test. La regla de `CLAUDE.md` ("no agregar tests salvo pedido explícito") queda superada por este pedido y debería actualizarse cuando se empiece. El enfoque recomendado sigue la pirámide de pruebas: muchas pruebas **unitarias** rápidas de la lógica de negocio, algunas de **integración** de persistencia y pocas de **extremo a extremo**. Las herramientas son las que ya trae `spring-boot-starter-test` (verificar que esté en el `pom.xml`): **JUnit 5** como runner, **Mockito** para aislar dependencias y **AssertJ** para aserciones legibles.

**Dónde está el valor.** No tiene sentido testear getters ni mappers generados. El retorno está en la lógica con reglas: `PedidoService` (transiciones de estado válidas e inválidas, cálculo de total), el ciclo de stock (descontar, reponer, stock insuficiente), `ProductoValidationService`, y más adelante las promociones y la autorización. Un test unitario de servicio instancia la clase con sus repositorios *mockeados* y verifica comportamiento: dado este estado, cuando ejecuto esto, entonces obtengo este resultado o esta excepción con este `ErrorCodeEnum`. Se escribe con el patrón **Given / When / Then**, un caso por test y nombres que describen la regla (`cancelarPedido_finalizado_noPermite`). Esto además sirve como documentación viva del negocio.

**Persistencia y controllers.** Las consultas propias (`@Query` de `PedidoRepository`, `searchProductosActivos`) se prueban con `@DataJpaTest` sobre H2, que ya está configurado en el perfil de test; ojo con que H2 no es MySQL y algunas diferencias de dialecto pueden dar falsos verdes, por lo que a futuro **Testcontainers con MySQL 8** es la opción más fiel si se tiene Docker. Los controllers se prueban con `@WebMvcTest` + `MockMvc`, que valida rutas, serialización, códigos HTTP y el `GlobalHandlerException` sin levantar la base (esto habría detectado el conflicto `/{id}` vs `/{email}` y el `@PathVariable userId`).

**Cómo se integra al trabajo diario.** Regla práctica: *todo bug que se corrige arranca con un test que lo reproduce y falla*, y *toda regla de negocio nueva entra con su test*. Esto convierte los bugs ya listados en §1.3 en los primeros casos de prueba (por ejemplo: "reactivar un usuario inactivo", "stock insuficiente devuelve el código de error correcto", "actualizar usuario no pisa la contraseña"). Se agrega **JaCoCo** para medir cobertura, sin perseguir un número fijo: una cobertura alta de servicios con reglas vale más que 90 % global inflado por código trivial. Cuando haya CI, el build corre `mvn test` en cada push.

### 6.3 Orden recomendado entre los dos frentes

1. **P0 con tests** (§2): corregir los bugs abiertos escribiendo primero el test que los reproduce. Es la mejor forma de aprender la herramienta con casos reales y chicos.
2. **Infraestructura de test** mínima: dependencias, perfil H2, un par de tests de servicio de referencia que sirvan de plantilla para el resto.
3. **P1 (pedido de punta a punta) con tests incluidos**, porque es el módulo con más reglas y el que más se beneficia.
4. **Seguridad en pasos** (6.1), apoyada en la red de tests creada antes, para poder activar Spring Security y saber de inmediato qué se rompió.
5. Recién entonces el resto de la hoja de ruta (§2, P4 en adelante), con tests y reglas de acceso desde el primer commit de cada módulo.

La lógica detrás de este orden: los tests son la red de seguridad que permite cambiar cosas grandes (seguridad, estados de pedido) sin miedo, por eso van primero; y la seguridad se agrega antes de sumar más superficie (publicaciones, reseñas, notificaciones), porque proteger un módulo al nacer es mucho más barato que retro-instalarlo después.
