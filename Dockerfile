# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jre

# Crear el directorio de trabajo
WORKDIR /workspace

# Etiqueta de la versión
ARG VERSION=latest
LABEL version="${VERSION}"

# Copiar el JAR
COPY app/build/libs/app.jar /app.jar

# Definir el volumen en /workspace para mantener compatibilidad
VOLUME /workspace

# Comando predeterminado
ENTRYPOINT ["java", "-jar", "/app.jar"]