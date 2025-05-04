package org.example.database;

import org.example.database.converter.AlterTableProcessor;
import org.example.database.extractor.SchemaProcessor;
import org.example.database.model.TableMetadata;
import org.example.database.parser.SqlFileContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class SqlFileProcessorTest {

    private final SchemaProcessor extractProcessor = new SchemaProcessor();
    private final AlterTableProcessor alterProcessor = new AlterTableProcessor();


    @Test
    void shouldProcessSqlFilesFromResources() throws Exception {
        // 1. Configurar procesador
        SqlFileProcessor processor = new SqlFileProcessor();

        // 2. Obtener rutas de los archivos de recursos
        String[] resourceFiles = {
            "customers.sql",
            "products.sql",
            "students.sql"
        };

        // 3. Procesar cada archivo
        List<CompletableFuture<SqlFileContent>> futures = List.of(
            processSqlResource(processor, resourceFiles[0]),
            processSqlResource(processor, resourceFiles[1]),
            processSqlResource(processor, resourceFiles[2])
                                                                 );

        // 4. Combinar resultados
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));

        List<SqlFileContent> results = allFutures.thenApply(v ->
                futures.stream()
                    .map(CompletableFuture::join)
                    .toList()
                ).get();

        String statements = SqlFileProcessor.consolidateSqlContents(results);
        System.out.println(statements);

        List<TableMetadata> tables = extractProcessor.processSchema(statements);
        System.out.println("PRE-PROCESSOR");
        tables.forEach(
            System.out::println
        );

        System.out.println();
        System.out.println("POST-PROCESSOR");
        alterProcessor.processAlterStatements(tables, statements);
        tables.forEach(
            System.out::println
        );
    }


    private CompletableFuture<SqlFileContent> processSqlResource(SqlFileProcessor processor, String resourceName)
        throws IOException, URISyntaxException {

        // Obtener la URL del recurso
        URL resourceUrl = getClass().getClassLoader().getResource(resourceName);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Recurso no encontrado: " + resourceName);
        }

        // Convertir a Path
        Path filePath = Paths.get(resourceUrl.toURI());

        // Procesar con tu SqlFileProcessor
        return processor.processSqlFile(filePath.toString());
    }
}