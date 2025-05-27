package org.example.generator.entity.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GeneratorUtils {

    /**
     * Writes a Java entity file to the appropriate directory based on the build system
     * This method automatically detects the project structure regardless of where the command is executed
     *
     * @param packageName The package name for the entity
     * @param className   The class name for the entity
     * @param content     The content to write to the file
     * @param buildType   The build system type ("gradle" or "maven")
     * @throws Exception If there is an error creating the file
     */
    public void writeEntityFile(String packageName, String className, String content, String buildType) throws Exception {
        // Capitalize the first letter of the class name if not already
        if (className != null && !className.isEmpty()) {
            className = normalizeClassName(className);
        }

        // Obtener el directorio actual desde donde se ejecuta el comando
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        // Lista de posibles estructuras para buscar en el proyecto
        List<String> possibleStructures = new ArrayList<>();

        // Determinar posibles estructuras según el tipo de build
        if ("gradle".equalsIgnoreCase(buildType)) {
            possibleStructures.add("app/src/main/java");
            possibleStructures.add("src/main/java");
            // Otras posibles estructuras para Gradle
        } else if ("maven".equalsIgnoreCase(buildType)) {
            possibleStructures.add("src/main/java");
            // Otras posibles estructuras para Maven
        } else {
            throw new IllegalArgumentException("Build type must be 'gradle' or 'maven'");
        }

        // Buscar la estructura correcta, empezando por el directorio actual y ascendiendo hasta encontrar una estructura válida
        Path projectRoot = findProjectRoot(currentDirectory);
        if (projectRoot == null) {
            throw new IOException("Could not determine project root directory");
        }

        // Buscar la estructura correcta de directorios
        Path sourceRoot = null;
        for (String structure : possibleStructures) {
            Path potentialSourceRoot = projectRoot.resolve(structure.replace("/", File.separator));
            if (Files.exists(potentialSourceRoot) && Files.isDirectory(potentialSourceRoot)) {
                sourceRoot = potentialSourceRoot;
                break;
            }
        }

        // Si no se encontró ninguna estructura válida, usar el directorio por defecto según el tipo de build
        if (sourceRoot == null) {
            String defaultPath;
            if ("gradle".equalsIgnoreCase(buildType)) {
                defaultPath = "app/src/main/java";
            } else {
                defaultPath = "src/main/java";
            }
            sourceRoot = projectRoot.resolve(defaultPath.replace("/", File.separator));
            Files.createDirectories(sourceRoot); // Crear el directorio si no existe
            System.out.println("Created default source directory: " + sourceRoot);
        }

        // Construir el directorio del paquete
        String packagePath = packageName.replace('.', File.separatorChar);
        Path packageDir = sourceRoot.resolve(packagePath);

        // Asegurarse de que el directorio del paquete existe
        Files.createDirectories(packageDir);

        // Crear el archivo de la entidad
        Path filePath = packageDir.resolve(className + ".java");
        Files.writeString(filePath, content);

        // Verificar permisos de escritura
        if (!Files.isWritable(packageDir)) {
            throw new IOException("Write permission denied for: " + packageDir);
        }

        System.out.println("Entity file created successfully at: " + filePath);
    }

    /**
     * Encuentra el directorio raíz del proyecto buscando marcadores de proyectos
     * como archivos build.gradle, pom.xml, .git, etc.
     *
     * @param startDir El directorio desde donde comenzar la búsqueda
     * @return El directorio raíz del proyecto o null si no se encuentra
     */
    private Path findProjectRoot(Path startDir) {
        // Marcadores que indican la raíz de un proyecto
        String[] projectMarkers = {
            "build.gradle", "build.gradle.kts", "pom.xml", "settings.gradle",
            "settings.gradle.kts", ".git", "gradle", "arch.yml"
        };

        Path current = startDir;
        // Limitar la búsqueda a un número razonable de niveles para evitar llegar hasta la raíz del sistema
        int maxLevels = 10;
        int level = 0;

        while (current != null && level < maxLevels) {
            // Verificar si alguno de los marcadores existe en el directorio actual
            for (String marker : projectMarkers) {
                if (Files.exists(current.resolve(marker))) {
                    return current;
                }
            }

            // Subir un nivel en la jerarquía de directorios
            current = current.getParent();
            level++;
        }

        // Si llegamos aquí, no encontramos la raíz del proyecto
        return startDir; // Devolver el directorio original como fallback
    }

    private String normalizeClassName(String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }

        // Eliminar guiones bajos y capitalizar cada palabra
        StringBuilder normalized = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : className.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    normalized.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    normalized.append(Character.toLowerCase(c));
                }
            }
        }

        return normalized.toString();
    }

}
