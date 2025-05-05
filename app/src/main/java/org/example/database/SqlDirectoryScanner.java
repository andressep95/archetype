package org.example.database;

import org.example.database.parser.SqlFileContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlDirectoryScanner {

    /**
     * Expande los paths de configuración, detectando si son directorios o archivos
     * y devolviendo una lista plana de todos los archivos SQL a procesar.
     */
    public List<String> expandDirectoriesToSqlFiles(List<String> configPaths) throws IOException {
        List<String> sqlFilePaths = new ArrayList<>();

        for (String configPath : configPaths) {
            Path path = Paths.get(configPath);

            if (Files.isDirectory(path)) {
                try (Stream<Path> pathStream = Files.walk(path)) {
                    pathStream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().toLowerCase().endsWith(".sql"))
                        .map(Path::toString)
                        .forEach(sqlFilePaths::add);
                }
            } else if (Files.isRegularFile(path) || configPath.toLowerCase().endsWith(".sql")) {
                sqlFilePaths.add(path.toString());
            }
        }

        return sqlFilePaths;
    }

    /**
     * Escanea un directorio en busca de archivos SQL.
     */
    public List<String> listSqlFilesInDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path is not a directory: " + directoryPath);
        }

        try (Stream<Path> pathStream = Files.walk(path)) {
            return pathStream.filter(Files::isRegularFile)
                .filter(p -> p.toString().toLowerCase().endsWith(".sql"))
                .map(Path::toString)
                .collect(Collectors.toList());
        }
    }
}