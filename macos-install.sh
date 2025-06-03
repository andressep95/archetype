#!/bin/bash
set -e

VERSION="1.0.0"
BASE_URL="https://github.com/andressep95/archetype/releases/download/v${VERSION}"

log() { echo -e "\033[1;34mℹ\033[0m $1"; }
error() { echo -e "\033[0;31m✗\033[0m $1"; }
success() { echo -e "\033[0;32m✓\033[0m $1"; }

# Detectar arquitectura
ARCH=""
if [[ "$(uname -m)" == "arm64" ]]; then
    ARCH="arm64"
elif [[ "$(uname -m)" == "x86_64" ]]; then
    ARCH="x64"
else
    error "Arquitectura no soportada: $(uname -m)"
    exit 1
fi

log "Detectada macOS (${ARCH})"

# Verificar si curl está disponible
if ! command -v curl &> /dev/null; then
    error "curl no está instalado. Por favor instálalo primero."
    exit 1
fi

URL="${BASE_URL}/arc-macos-${ARCH}"
INSTALL_DIR="$HOME/.arc"

log "Creando directorio de instalación: $INSTALL_DIR"
mkdir -p "$INSTALL_DIR"

log "Descargando Archetype desde: $URL"

# Intentar descargar con mejor manejo de errores
if ! curl -L --fail --show-error --silent -o "$INSTALL_DIR/arc" "$URL"; then
    error "Error al descargar el archivo. Verifica que la versión $VERSION esté disponible en:"
    error "$URL"
    error ""
    error "Puedes verificar las versiones disponibles en:"
    error "https://github.com/andressep95/archetype/releases"
    exit 1
fi

# Verificar si el archivo fue descargado
if [[ ! -f "$INSTALL_DIR/arc" ]]; then
    error "El archivo no fue descargado correctamente"
    exit 1
fi

# Verificar si el archivo tiene contenido
if [[ ! -s "$INSTALL_DIR/arc" ]]; then
    error "El archivo descargado está vacío"
    rm -f "$INSTALL_DIR/arc"
    exit 1
fi

# Verificar si el archivo es un binario ejecutable válido
if ! file "$INSTALL_DIR/arc" | grep -q "executable\|Mach-O"; then
    error "El archivo descargado no es un binario válido"
    error "Contenido del archivo:"
    head -n 5 "$INSTALL_DIR/arc"
    rm -f "$INSTALL_DIR/arc"
    exit 1
fi

chmod +x "$INSTALL_DIR/arc"

# Función para agregar PATH si no existe
add_to_path() {
    local shell_config="$1"
    local path_line='export PATH="$HOME/.arc:$PATH"'

    if [[ -f "$shell_config" ]]; then
        if ! grep -q "$HOME/.arc" "$shell_config"; then
            echo "$path_line" >> "$shell_config"
            log "PATH agregado a $shell_config"
        else
            log "PATH ya existe en $shell_config"
        fi
    else
        echo "$path_line" >> "$shell_config"
        log "Creado $shell_config con PATH"
    fi
}

# Añadir el PATH a los archivos de configuración del shell
add_to_path "$HOME/.zshrc"
add_to_path "$HOME/.bash_profile"

# También agregar a .bashrc si existe
if [[ -f "$HOME/.bashrc" ]]; then
    add_to_path "$HOME/.bashrc"
fi

# Actualizar PATH para la sesión actual
export PATH="$HOME/.arc:$PATH"

success "Instalación completada exitosamente!"
log ""
log "Para usar Archetype:"
log "1. Reinicia tu terminal o ejecuta: source ~/.zshrc"
log "2. Prueba con: arc --version"
log ""
log "Si el comando 'arc' no se encuentra, ejecuta manualmente:"
log "export PATH=\"\$HOME/.arc:\$PATH\""