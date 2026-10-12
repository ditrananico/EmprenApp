---
name: verificar-esquema
description: >-
  Compara las entidades JPA del proyecto con los archivos .sql (scripts.sql y los que se sumen)
  y reporta qué tabla, columna, tipo, FK o unique falta o no coincide para que la app levante
  con ddl-auto=validate, respetando el orden de ejecución. Solo informa y propone el SQL faltante
  en pantalla; no modifica archivos. Usar al tocar una entidad o un .sql, antes de levantar la app,
  o ante errores de Schema-validation (missing table, missing column, wrong column type).
---

# verificar-esquema

Un solo problema: **¿las entidades JPA y los `.sql` son coherentes para que la app arranque con `ddl-auto: validate`?**

No genera entidades, no corre la app, no escribe datos semilla ni modifica archivos. Solo lee, compara e informa.

## Alcance real: los `.sql`, no la base viva

Al arrancar, Hibernate compara las entidades contra la **base de datos real** vía JDBC. Esta skill compara las entidades contra los **archivos `.sql`**. Es una verificación previa y barata: sirve para detectar problemas antes de tocar la base, pero **no ve** qué se ejecutó de verdad.

Si la base se creó con una versión anterior de `scripts.sql`, o se le aplicó un `ALTER` a mano que no está en los archivos, un veredicto OK acá no garantiza que `validate` pase. Decírselo al usuario cuando el veredicto sea OK y haya dudas de que la base esté sincronizada con los archivos. La prueba definitiva es el arranque real contra MySQL.

## Cómo ejecutarla

Desde la raíz del proyecto (PowerShell):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .claude/skills/verificar-esquema/scripts/Verificar-Esquema.ps1 -Root .
```

Parámetros opcionales:

- `-SqlFiles a.sql,b.sql`: archivos **en orden de ejecución** (relativos a la raíz, o rutas absolutas). Por defecto `scripts.sql` y, si no está vacío, `data.sql`. Si el usuario agrega `alter.sql`, `inserts.sql`, etc., pasarlos acá en el orden en que se ejecutan.
- `-SourceDir src/main/java`: carpeta con las entidades.

Código de salida: `0` sin bloqueantes, `1` con bloqueantes, `2` error de uso o fallo interno del verificador (el mensaje empieza con `ERROR`; no es un hallazgo sobre el esquema).

## Qué hace el script

1. **Esquema efectivo.** Reproduce en orden los `CREATE` / `ALTER` / `DROP` / `INSERT` de los `.sql`. Respeta `SET FOREIGN_KEY_CHECKS`, `IF NOT EXISTS` y que las columnas de la PK son `NOT NULL`. Detecta FKs a tablas que todavía no existen en ese punto, `ALTER` sobre tablas inexistentes y duplicados.
2. **INSERT.** Verifica que tabla y columnas existan, que la cantidad de valores por fila coincida con las columnas, que no falten NOT NULL sin default y que los valores de columnas enum existan en el enum Java (o el ordinal esté en rango).
3. **Entidades.** Lee `@Entity` / `@MappedSuperclass` (`@Table`, `@Column`, `@JoinColumn`, `@ManyToOne`, `@OneToOne`, `@OneToMany+@JoinColumn`, `@Enumerated`, `@GeneratedValue` y generadores, superclases).
4. **Naming.** Aplica la naming strategy de Spring (`CamelCaseToUnderscores`) a nombres implícitos **y explícitos**, y detecta en `application.yml/properties` si se configuró otra (`PhysicalNamingStrategyStandardImpl`). De la configuración solo se leen `ddl-auto`, `physical-strategy` y `sql.init`; no se imprime nada más.
5. **Comparación** tabla por tabla, clasificando cada hallazgo:

| Nivel | Significa |
|---|---|
| `BLOQUEA` | La app no arranca o el script SQL falla: tabla/columna faltante, tipo incompatible, FK a tabla inexistente o de tipo distinto, orden de creación inválido, cantidad de valores incorrecta en un INSERT, tabla de secuencia inexistente. |
| `RUNTIME` | Arranca, pero falla al operar: columna `NOT NULL` sin default que la entidad no mapea, valor de enum insertado que no existe en el enum Java, constante del enum que no entra en el `VARCHAR`. |
| `AVISO` | No impide arrancar: FK/UNIQUE sin declarar, longitud o precisión menor, nullabilidad cruzada, mayúsculas del nombre de tabla (rompe en MySQL Linux/Docker), `IF NOT EXISTS` duplicado, columna `ENUM` nativa. |
| `INFO` | Columnas extra inocuas, tablas sin entidad, `@Embedded`/`@Inheritance`, y el contexto de ejecución detectado (`sql.init.mode`, `docker initdb.d`, `schema-locations`). |

6. **Salida.** Imprime el orden de creación sugerido (orden topológico por FK, con ciclos) y un **parche SQL propuesto** (`CREATE TABLE` en orden de dependencia con la misma collation que las tablas existentes, `ADD COLUMN`, `MODIFY`, `FOREIGN KEY`, `UNIQUE`).

## Qué hacer con el resultado

1. Mostrar el reporte **en español**, empezando por el veredicto y los `BLOQUEA`. No volcar el `INFO` completo si es largo: resumirlo.
2. Mostrar el parche propuesto **en pantalla**. **No escribirlo en ningún archivo** salvo que el usuario lo pida explícitamente.
3. Respetar `CLAUDE.md`: no tocar `scripts.sql` ya aplicado sin preguntar. Si el usuario quiere aplicar el parche, sugerir un archivo aparte (ej. `alter_<tema>.sql`) o editar `scripts.sql` solo con su confirmación, y avisar que `ddl-auto: validate` exige que la base y las entidades queden alineadas (editar el archivo no cambia la base: hay que ejecutar el SQL).
4. Para hallazgos `RUNTIME` y `AVISO` donde hay **dos arreglos válidos** (mapear la columna en la entidad vs. relajar el SQL), explicar ambos y recomendar uno; la decisión es del usuario. Ejemplo típico: `emprendimientos.user_id` NOT NULL sin campo en `Emprendimiento`.
5. El parche es un punto de partida: el script no conoce los `DEFAULT` que viven en la entidad (ej. `= true`) ni los índices. Revisarlo antes de recomendarlo y decirlo.
6. Si el script termina con `ERROR interno`, reportarlo como falla del verificador, no como problema del esquema.

## Límites (decirlo si afectan la conclusión)

- Es análisis **estático**: no se conecta a MySQL ni arranca Spring. Un veredicto OK significa "no se detectó nada", no una garantía.
- **Enums `STRING`:** se espera `VARCHAR`. Desde Hibernate 6.2 cambió el mapeo de enums en MySQL; si el SQL usa `ENUM(...)` nativo se avisa como `AVISO` porque no se puede confirmar estáticamente qué exige `validate` en la versión usada. Ante la duda, el arranque real lo resuelve.
- No expande `@Embedded`, `@Inheritance`, `@ElementCollection` ni `@ManyToMany` con tabla intermedia: los lista como `INFO`, y las columnas sin mapear de esas entidades también bajan a `INFO` (no se puede afirmar que falten).
- `ALTER TABLE` con formas poco comunes (`ADD COLUMN (a, b)`, `RENAME COLUMN`, etc.) puede salir como `INFO` "no soportado"; leer ese `ALTER` manualmente.
- No valida índices (Hibernate validate no los chequea) ni qué proceso ejecuta los `.sql` más allá de lo detectable en la configuración y en `docker-compose`.
- Un nombre de `physical-strategy` propio (clase custom) no se puede emular: se asume snake_case y se avisa.

## Fuera de alcance (otras skills, no mezclar)

Generar entidades desde el SQL, generar migraciones versionadas, correr/levantar la app, cargar datos de prueba, revisar lógica de negocio. Verificar contra la base viva (INFORMATION_SCHEMA) sería una extensión del mismo problema, pero requiere cliente MySQL y credenciales por variables de entorno; hoy no está implementada.
