# Guía de tests unitarios con JUnit 5 y Mockito (aplicada a EmprenApp)

Guía práctica: solo los conceptos que vamos a usar en este proyecto, con ejemplos tomados del código real. Complementa la sección 6 de `CONTEXTO_PRODUCTO.md`.

---

## 0. Rol de Claude en este proyecto (no perder al cambiar de contexto)

> Claude actúa como **programador backend Java senior y mentor**. EmprenApp es, además de un producto, un proyecto de aprendizaje del dueño. Por lo tanto:
>
> 1. **Explica el porqué de cada cosa**: qué problema resuelve, qué alternativas había y por qué elige una. Nunca entrega solo código.
> 2. **Trabaja en pasos chicos y revisables.** Un cambio por vez, con cómo verificarlo.
> 3. **Plantea las decisiones de diseño antes de escribirlas** y deja que el dueño elija cuando hay trade-offs reales.
> 4. **Es honesto con el estado real**: si algo falla, no se testeó o se asumió, lo dice.
> 5. **Respeta `CLAUDE.md`**: responde en español, no hace operaciones git que modifiquen el repo sin pedido explícito, no toca `application.yml`, actualiza `scripts.sql` si cambia una entidad y consulta antes de refactors grandes.
> 6. **Tests:** el dueño pidió explícitamente incorporarlos (este documento). La regla de `CLAUDE.md` "no agregues tests salvo pedido" queda cubierta por ese pedido; conviene actualizar `CLAUDE.md` cuando se empiece a escribir tests.
> 7. **Regla de oro de testing:** todo bug se corrige empezando por un test que lo reproduce y falla; toda regla de negocio nueva entra con su test.

---

## 1. Qué es un test unitario y qué no

Un test unitario verifica **una unidad de lógica** (normalmente una clase o un método) **aislada** de sus dependencias, rápido (milisegundos) y sin base de datos, red ni Spring levantado.

En este proyecto, la unidad típica es un `XxxService`. Sus dependencias (repositorios, otros servicios) se reemplazan por **dobles de prueba** (mocks). El test responde: *"dado este estado de las dependencias, cuando llamo a este método, ¿devuelve/lanza lo que corresponde?"*.

| Tipo | Qué prueba | Levanta Spring | Herramienta aquí |
|---|---|---|---|
| **Unitario** | Un service/validator con dependencias falsas | No | JUnit 5 + Mockito |
| Persistencia | Consultas `@Query`, constraints | Parcial (JPA) | `@DataJpaTest` + H2 |
| Web | Rutas, JSON, códigos HTTP, handler de errores | Parcial (MVC) | `@WebMvcTest` + MockMvc |
| Integración | Varias capas juntas | Sí | `@SpringBootTest` |

Esta guía cubre el primer nivel. Los otros se introducen después.

**Por qué empezar por unitarios:** son los más baratos de escribir y ejecutar, localizan el error con precisión (falla el método X, no "algo en la app"), y la lógica de negocio importante (estados de pedido, stock, validaciones) vive en los services.

---

## 2. Qué trae el proyecto ya

El `pom.xml` ya incluye `spring-boot-starter-test` con scope `test`. Eso trae, sin agregar nada:

- **JUnit 5 (Jupiter)**: runner y anotaciones (`@Test`, `@BeforeEach`, `@Nested`, ...).
- **Mockito**: crear mocks, definir su comportamiento y verificar interacciones.
- **AssertJ**: aserciones legibles (`assertThat(...)`).
- **Spring Test / MockMvc**: para tests web más adelante.

También existe `src/test/resources/application.yml` (perfil H2). Los tests unitarios puros **no lo usan** porque no levantan Spring.

Ubicación y nombre de los tests: mismo paquete que la clase testeada, bajo `src/test/java`, con sufijo `Test`.

```
src/main/java/org/emprenApp/user/application/service/UserService.java
src/test/java/org/emprenApp/user/application/service/UserServiceTest.java
```

Comando para correr uno solo (PowerShell):

```powershell
mvn test -Dtest=UserServiceTest
mvn test -Dtest=UserServiceTest#updateStatusUser_usuarioInactivo_loReactiva
```

---

## 3. Conceptos de JUnit 5 que vamos a usar

**`@Test`**: marca un método como caso de prueba. Un test = un comportamiento.

**`@BeforeEach`**: método que corre antes de cada test, para armar datos comunes. Cada test arranca limpio: no dependas del orden ni de estado compartido.

**`@DisplayName`**: descripción legible del caso, útil en reportes.

**`@Nested`**: agrupa tests de un mismo método bajo una clase interna (`class CancelarPedido { ... }`). Mantiene ordenado un test de service con muchos casos.

**`@ParameterizedTest` + `@ValueSource` / `@CsvSource`**: el mismo test con varias entradas. Ideal para validaciones (`id = null, 0, -1`; estados inválidos; emails mal formados).

**`assertThrows`** (o `assertThatThrownBy` de AssertJ): verifica que se lance una excepción. En este proyecto es clave porque los errores de negocio son `BaseException` con un `ErrorCodeEnum`, y hay que comprobar **cuál**.

### Patrón Given / When / Then

Cada test se estructura en tres bloques (también llamado Arrange / Act / Assert):

```java
@Test
void cancelarPedido_yaFinalizado_devuelveFalse() throws Exception {
    // Given: un pedido FINALIZADO existente
    Pedido pedido = new Pedido();
    pedido.setStatus(EstadoPedidoEnum.FINALIZADO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

    // When: intento cancelarlo
    boolean resultado = pedidoService.cancelPedido(1L);

    // Then: no se cancela y no se guarda nada
    assertThat(resultado).isFalse();
    verify(pedidoRepository, never()).save(any());
}
```

**Convención de nombres:** `metodo_condicion_resultadoEsperado`. El nombre es documentación: debe leerse como una regla de negocio.

---

## 4. Conceptos de Mockito que vamos a usar

### 4.1 Mock

Un objeto falso que reemplaza a una dependencia. Por defecto no hace nada: devuelve `null`, `0`, `false`, `Optional.empty()` o lista vacía según el tipo. Se crea con `@Mock`.

### 4.2 `@InjectMocks`

Crea la clase bajo prueba e inyecta en ella los `@Mock` declarados. En EmprenApp los services usan `@RequiredArgsConstructor` de Lombok (inyección por constructor), y Mockito lo resuelve bien.

### 4.3 `@ExtendWith(MockitoExtension.class)`

Activa Mockito en JUnit 5 e inicializa las anotaciones. Además activa el modo **strict stubs**: si definís un `when(...)` que el test nunca usa, falla con `UnnecessaryStubbingException`. Eso es deseable: evita tests con preparación inútil o que no prueban lo que creés.

### 4.4 Stubbing: `when(...).thenReturn(...)` / `thenThrow(...)`

Define qué responde un mock ante una llamada concreta.

```java
when(userRepository.findByIdAndEstado(1L, EstadoUserEnum.ACTIVO))
        .thenReturn(Optional.of(user));

when(userRepository.save(any(User.class)))
        .thenThrow(new RuntimeException("DB caída"));
```

Cuando hay que testear un método `void`, el stubbing se escribe al revés: `doThrow(...).when(mock).metodo(...)`.

### 4.5 `verify(...)`: comprobar interacciones

Verifica que el service *llamó* a su dependencia como se espera. Es el modo de probar efectos laterales (`save`, `delete`).

```java
verify(userRepository).save(user);                 // exactamente 1 vez
verify(userRepository, never()).save(any());       // nunca
verify(userRepository, times(2)).findById(any());  // 2 veces
```

**Cuándo usar `verify` y cuándo `assertThat`:** si el método *devuelve* algo, afirmá sobre el resultado. Usá `verify` para efectos que no se ven en el retorno (guardar, no guardar, reponer stock). No verifiques absolutamente todas las llamadas internas: acoplás el test a la implementación y se rompe con cualquier refactor.

### 4.6 Matchers: `any()`, `eq()`, `anyLong()`

Permiten flexibilidad en el argumento. Regla importante: **si usás un matcher en un argumento, todos los argumentos de esa llamada deben ser matchers**.

```java
when(repo.findByIdAndEstado(eq(1L), any(EstadoUserEnum.class)))...   // correcto
when(repo.findByIdAndEstado(1L, any(EstadoUserEnum.class)))...        // ERROR
```

### 4.7 `ArgumentCaptor`

Captura el objeto que el service pasó a un mock, para inspeccionarlo. Es muy útil para comprobar *qué se guardó*.

```java
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(userRepository).save(captor.capture());
assertThat(captor.getValue().getEstado()).isEqualTo(EstadoUserEnum.ACTIVO);
```

### 4.8 Lo que NO hace falta ahora

`@Spy`, `mockStatic`, `@MockBean` (es de Spring, para tests web), `Answer` personalizados. Se explican cuando aparezcan.

---

## 5. AssertJ: aserciones que vamos a usar

```java
assertThat(dto.getEmail()).isEqualTo("a@b.com");
assertThat(resultado).isTrue();
assertThat(lista).isEmpty();
assertThat(page.getContent()).hasSize(2);
assertThat(dto).isNotNull();

assertThatThrownBy(() -> service.getUserByID(99L))
        .isInstanceOf(NotFoundException.class);
```

Para comparar el código de error de una `BaseException`:

```java
BaseException ex = assertThrows(BaseException.class, () -> service.metodo(...));
assertThat(ex.getCode()).isEqualTo(ErrorCodeEnum.INVALID_PARAMETERS.getCode());
```

---

## 6. Particularidades de EmprenApp que condicionan cómo testeamos

Estas cuatro cosas salen del código real y evitan frustraciones:

1. **Los mappers son `INSTANCE` estático (`Mappers.getMapper`), no se inyectan.** Por lo tanto **no se pueden mockear con `@Mock`**. En los tests se usa el mapper real: es código generado por MapStruct, rápido y determinista. Consecuencia: el test de un service ejerce también el mapeo. Si un campo no mapea, el test lo detecta, lo cual es bueno.
2. **Los services capturan `Exception` y la envuelven en `GenericException`.** Un test que espera `NotFoundException` pasa solo si el service la relanza correctamente; si no, verás `GenericException`. Eso ya reveló un bug en `DetallePedidoService` (§1.3 de `CONTEXTO_PRODUCTO.md`). Siempre afirmá el **tipo** de excepción y su **código**.
3. **Los controllers usan `@Autowired` de campo y dependen del `Adapter`.** Para testearlos con `@WebMvcTest` se mockea el adapter con `@MockBean`. Eso queda para una etapa posterior.
4. **El logger es un `static final`** en cada service: no hay que hacer nada especial, no interfiere.

Además, el target de compilación es Java 16: se puede usar `var`, `record` y `Optional.isEmpty()`, pero no features de Java 21 (p. ej. `switch` con patrones).

---

## 7. Anatomía de un test de service (plantilla)

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User usuarioInactivo;

    @BeforeEach
    void setUp() {
        usuarioInactivo = new User();
        usuarioInactivo.setId(1L);
        usuarioInactivo.setEmail("test@mail.com");
        usuarioInactivo.setEstado(EstadoUserEnum.INACTIVO);
    }

    @Nested
    @DisplayName("updateStatusUser")
    class UpdateStatusUser {

        @Test
        void usuarioInexistente_lanzaNotFound() {
            when(userRepository.findByIdAndEstado(any(), any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.updateStatusUser(99L));
            verify(userRepository, never()).save(any());
        }
    }
}
```

Puntos a notar: sin Spring, sin base, un `@Mock` por dependencia directa del service, datos mínimos en `@BeforeEach`, y cada caso en su `@Nested` con un nombre que describe la regla.

---

## 8. Qué testear y qué no

**Sí:**
- Reglas de negocio y ramas: "pedido finalizado no se cancela", "stock insuficiente rechaza", "usuario inexistente → NotFound".
- Cada **camino de error** relevante de un método (id inválido, no encontrado, estado no permitido, fallo inesperado → `GenericException`).
- Efectos: qué se guarda (`ArgumentCaptor`), qué **no** se guarda.
- Valores límite: `null`, `0`, negativo, stock justo igual a la cantidad pedida.

**No (por ahora):**
- Getters/setters, constructores Lombok, DTOs sin lógica.
- Clases generadas (`*MapperImpl`).
- Que Spring Data funcione (`save`, `findById`): ya está probado por Spring.
- Cada método de cada service "por completitud": se prioriza por riesgo.

**Criterio de prioridad (riesgo × complejidad):**

| Prioridad | Módulo | Motivo |
|---|---|---|
| Alta | `UserService` (update/status/create) | Bugs conocidos; base de la seguridad |
| Alta | `PedidoService`, `DetallePedidoService` | Más reglas, estados y stock |
| Media | `ProductoService` | Validaciones y filtros de activos |
| Media | `ValidateGeneric`, `*ValidationService` | Reglas puras, tests muy baratos |
| Baja | `CategoriaService`, `LocalidadService`, `UbicacionService` | CRUD simple; probar solo la lógica de "predeterminada" y las bajas |

---

## 9. Errores típicos de principiante

- **Mockear lo que no hay que mockear** (el objeto bajo prueba, un DTO, un mapper estático).
- **Testear la implementación y no el comportamiento**: verificar cada llamada interna hace el test frágil.
- **Un test con muchas aserciones y varios `When`**: si falla, no se sabe qué regla se rompió. Un test, una regla.
- **Tests dependientes entre sí** o con estado compartido mutable entre métodos.
- **`when()` innecesarios**: strict stubs los marca; no los silencies con `lenient()` salvo causa justificada.
- **Test que nunca puede fallar**: un `assertThrows` que no verifica el tipo correcto, o un `verify` sobre algo que siempre ocurre. Un buen hábito: **ver el test fallar primero** (rojo) antes de arreglar el código (verde).
- **Usar `@SpringBootTest` para todo**: es lento y oculta qué falla; reservarlo para integración real.

---

## 10. Flujo de trabajo para corregir un bug con test

1. **Reproducir** el bug como test unitario que **falla** por la razón correcta.
2. **Ejecutarlo** y confirmar el rojo (`mvn test -Dtest=...`).
3. **Corregir** el código con el cambio mínimo.
4. **Ejecutarlo** y confirmar el verde.
5. **Agregar** los casos vecinos (otros caminos del mismo método) para que quede cubierto.
6. **Correr toda la suite** para verificar que nada más se rompió.

Este ciclo es el mismo que se sigue en TDD (Test-Driven Development); aquí lo usamos como disciplina para bugs, sin exigirlo para todo el código.

---

## 11. Próximos pasos

1. Elegir el primer bug (`UserService.updateStatusUser`) y analizar por qué falla, qué métodos testear y cuáles no.
2. Crear `UserServiceTest` con ese caso como plantilla.
3. Replicar el patrón en `PedidoService` y `DetallePedidoService`.
4. Luego: `@DataJpaTest` para `PedidoRepository`, `@WebMvcTest` para controllers, JaCoCo para cobertura.
