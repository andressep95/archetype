#!/bin/bash
set -e

VERSION="1.1.3"
BASE_URL="https://github.com/andressep95/archetype/releases/download/v${VERSION}"

log() { echo -e "\033[1;34mℹ\033[0m $1"; }

ARCH=""
if [[ "$(uname -m)" == "aarch64" ]]; then
    ARCH="arm64"
elif [[ "$(uname -m)" == "x86_64" ]]; then
    ARCH="x64"
else
    echo -e "\033[0;31m✗ Arquitectura no soportada\033[0m"
    exit 1
fi

log "Detectado Linux (${ARCH})"
URL="${BASE_URL}/arc-linux-${ARCH}"
INSTALL_DIR="$HOME/.arc"
mkdir -p "$INSTALL_DIR"

log "Descargando arc desde: $URL"
curl -L -o "$INSTALL_DIR/arc" "$URL"

chmod +x "$INSTALL_DIR/arc"
echo 'export PATH="$HOME/.arc:$PATH"' >> "$HOME/.bashrc"

log "Instalación completada. Reinicia tu terminal o ejecuta: source ~/.bashrc"
log "Prueba ejecutando: arc --version"