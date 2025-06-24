Claro, aquí tienes el bloque en formato **Markdown** listo para documentación o un README:

## Comandos útiles para instalar y manejar la herramienta `arc`

### 📦 Actualizar build
```bash
./gradlew clean build
```

### 📦 Mover archivo
```bash
sudo mv app/build/libs/arc.jar /usr/local/bin/arc
```

### 📦 Mover archivo2.0

```bash
sudo mv app/build/libs/app.jar /usr/local/bin/arc
```

### 🔐 Dar permisos de ejecutable

```bash
sudo chmod +x /usr/local/bin/arc
```

### 🔍 Verificar ubicación y permisos

```bash
ls -l /usr/local/bin/arc
```

### 🚀 Ejecutar herramienta

```bash
java -jar /usr/local/bin/arc
```

### 🧹 Eliminar versión actual (para actualizar o limpiar)

```bash
sudo rm /usr/local/bin/arc
```

## 🛠️ Generador de Entidades Java desde Esquemas SQL PostgreSQL

Esta herramienta permite generar automáticamente código Java anotado con **JPA** y **Hibernate** a partir de un *
*esquema de base de datos PostgreSQL**. Su objetivo es facilitar la creación de entidades Java totalmente funcionales,
respetando las relaciones, restricciones y configuraciones del esquema SQL original.

---

### 🚀 Características principales

- ✅ **Soporte para múltiples sentencias SQL**:
    - `CREATE TABLE`
    - `ALTER TABLE`
    - `CREATE INDEX`

- 📁 **Entrada flexible**:
    - Procesa archivos SQL individuales especificando la **ruta directa al archivo**.
    - Procesa **carpetas completas**, leyendo automáticamente **todos los archivos `.sql`** contenidos en ellas.

- 🧱 **Generación de entidades Java**:
    - Interpreta las sentencias `CREATE TABLE` para crear clases Java con sus campos correspondientes.
    - Se generan las anotaciones JPA necesarias como `@Entity`, `@Table`, `@Id`, `@Column`, etc.

- 🧩 **Procesamiento de `ALTER TABLE`**:
    - Aplica modificaciones al esquema original como cambios de tipos de datos, adición de restricciones (`NOT NULL`,
      `DEFAULT`, etc.) y relaciones (`FOREIGN KEY`, `PRIMARY KEY`, etc.).

- ⚡ **Creación de índices personalizados**:
    - A partir de sentencias `CREATE INDEX`, se generan las anotaciones `@Index` en las entidades correspondientes,
      evitando duplicados.

- 🔗 **Relaciones entre entidades**:
    - Reconoce claves foráneas y genera automáticamente las relaciones con `@ManyToOne`, `@OneToMany`, y `@JoinColumn`.

- 🌿 **Compatibilidad con Lombok**:
    - Permite incluir automáticamente anotaciones como `@Getter`, `@Setter`, `@NoArgsConstructor`,
      `@AllArgsConstructor`, y `@ToString`, o prescindir de ellas según configuración.

---
