package org.example.database;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SqlFileProcessorManager {

    private final SqlFileProcessor processor;

    public SqlFileProcessorManager() {
        this.processor = new SqlFileProcessor();
    }

    /**
     * Process SQL files specified in the YAML configuration
     *
     * @param sqlSchemaFiles List of SQL file paths from the YAML config
     * @return CompletableFuture that completes with the processed SQL content
     */
    public CompletableFuture<List<SqlFileContent>> processSqlFiles(List<String> sqlSchemaFiles) {
        return processor.processSqlFiles(sqlSchemaFiles)
            .whenComplete((result, error) -> {
                if (error != null) {
                    System.err.println("Error processing SQL files: " + error.getMessage());
                } else {
                    System.out.println("Successfully processed " + result.size() + " SQL files");
                    for (SqlFileContent content : result) {
                        System.out.println("File: " + content.getFilePath() +
                            ", Encoding: " + content.getEncoding() +
                            ", Statements: " + content.getSqlStatements().size());
                    }
                }
            });
    }

    /**
     * Shutdown the processor resources
     */
    public void shutdown() {
        processor.shutdown();
    }

}
