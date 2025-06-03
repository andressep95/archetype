# Archetype CLI Installer

### Repositorio de la herramienta (¡Issues y estrellas son bienvenidos!)

https://github.com/andressep95/archetype

## Introducción

Archetype es una potente herramienta CLI para generar estructuras de proyectos y código base siguiendo mejores
prácticas. Este instalador te permite configurar Archetype en tu sistema con un solo comando, adaptado a tu sistema
operativo.

## Instalación

### Comandos de instalación por sistema operativo:

#### macOS (Apple Silicon o Intel):

```bash
curl -fsSL https://raw.githubusercontent.com/andressep95/archetype/main/install-macos.sh | bash
```

#### Linux:

```bash
curl -fsSL https://raw.githubusercontent.com/andressep95/archetype/main/install-linux.sh | bash
```

### Descripción:

Estos comandos:

1. Detectan tu arquitectura (x64/arm64)
2. Descargan el binario correspondiente
3. Lo instalan en `~/.arc` (o `%USERPROFILE%\.arc` en Windows)
4. Agregan el directorio a tu PATH

### Verificación:

Prueba la instalación ejecutando:

```bash
arc --version
```

## Uso con Docker

Si prefieres usar Archetype mediante Docker:

```bash
docker pull ghcr.io/andressep95/archetype-cli:latest
```

### Comandos básicos:

1. **Mostrar ayuda:**
   ```bash
   docker run --rm ghcr.io/andressep95/archetype-cli:latest --help
   ```

2. **Generar proyecto base:**
   ```bash
   docker run --rm -v $(pwd):/workspace ghcr.io/andressep95/archetype-cli:latest init my-project
   ```

3. **Modo interactivo:**
   ```bash
   docker run -it -v $(pwd):/workspace ghcr.io/andressep95/archetype-cli:latest
   ```