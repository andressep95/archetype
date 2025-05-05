package org.example.database;

import org.example.configuration.model.SchemaConfig;
import org.example.configuration.model.SqlConfig;
import org.example.database.parser.SqlFileContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SqlFileProcessorManager {
    private final SqlFileProcessor processor;
    private final SqlDirectoryScanner sqlDirectoryScanner;

    public SqlFileProcessorManager() {
        this.processor = new SqlFileProcessor();
        this.sqlDirectoryScanner = new SqlDirectoryScanner();
    }

    public CompletableFuture<List<SqlFileContent>> processSqlPaths(SchemaConfig schemaConfig) {
        try {
            List<String> allPaths = new ArrayList<>();

            // Agregar paths explícitos
            if (schemaConfig.getPath() != null) {
                allPaths.addAll(schemaConfig.getPath());
            }

            // Agregar archivos del directorio si está especificado
            if (schemaConfig.getDirectory() != null && !schemaConfig.getDirectory().isEmpty()) {
                try {
                    allPaths.addAll(sqlDirectoryScanner.listSqlFilesInDirectory(schemaConfig.getDirectory()));
                } catch (IOException e) {
                    throw new IOException("Error reading SQL directory: " + schemaConfig.getDirectory(), e);
                }
            }

            // Expandir cualquier directorio que pueda estar en los paths
            List<String> sqlFilePaths = sqlDirectoryScanner.expandDirectoriesToSqlFiles(allPaths);

            if (sqlFilePaths.isEmpty()) {
                return CompletableFuture.completedFuture(new ArrayList<>());
            }

            return processor.processSqlFiles(sqlFilePaths);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new RuntimeException("Error scanning SQL paths", e));
        }
    }

    public void shutdown() {
        processor.shutdown();
    }
}