package org.example.database;

import org.example.common.exception.ConfigurationException;
import org.example.database.parser.SqlFileContent;
import org.example.database.parser.SqlStatementParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SqlFileProcessor {

    private final ExecutorService fileProcessorPool;
    private static final int DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024; // 4MB
    private static final Charset[] COMMON_ENCODINGS = {
        StandardCharsets.UTF_8,
        StandardCharsets.UTF_16,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.US_ASCII
    };

    public SqlFileProcessor() {
        int processors = Runtime.getRuntime().availableProcessors(); // Obtiene el numero de hilos del equipo
        this.fileProcessorPool = Executors.newFixedThreadPool(
            Math.max(2, processors / 2), // Define el uso de al menos 2 hilos hasta la mitad del maximo
            r -> {
                Thread t = new Thread(r, "sql-file-processor");
                t.setDaemon(true);
                return t;
            }
        );
    }

    /**
     * Convierte múltiples SqlFileContent en un solo String SQL
     *
     * @param sqlFiles Lista de contenidos SQL a consolidar
     * @return String con todas las sentencias SQL combinadas
     */
    public static String consolidateSqlContents(List<SqlFileContent> sqlFiles) {
        return sqlFiles.stream()
            .flatMap(file -> file.getSqlStatements().stream())
            .map(String::trim)
            .filter(statement -> !statement.isEmpty())
            .collect(Collectors.joining("\n\n"));
    }

    /**
     * Process multiple SQL files asynchronously and return the combined results
     *
     * @param filePaths List of file paths to process
     * @return CompletableFuture that completes with the list of SQL content from all files
     */
    public CompletableFuture<List<SqlFileContent>> processSqlFiles(List<String> filePaths) {
        List<CompletableFuture<SqlFileContent>> futures = new ArrayList<>();

        for (String filePath : filePaths) {
            futures.add(processSqlFile(filePath));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<SqlFileContent> results = new ArrayList<>();
                for (CompletableFuture<SqlFileContent> future : futures) {
                    try {
                        results.add(future.get());
                    } catch (Exception e) {
                        throw new ConfigurationException("Error processing SQL file", e);
                    }
                }
                return results;
            }
        );
    }

    /**
     * Process a single SQL file asynchronously
     *
     * @param filePath Path to the SQL file
     * @return CompletableFuture that completes with the file content
     */
    public CompletableFuture<SqlFileContent> processSqlFile(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = Paths.get(filePath);

            try {
                // Detect file encoding
                Charset encoding = detectFileEncoding(path);

                // Create a content container for SQL statements
                SqlFileContent content = new SqlFileContent(path.toString(), encoding);

                // Process the file in chunks
                processFileInChunks(path, encoding, content::addSqlStatement);

                return content;
            } catch (IOException e) {
                throw new RuntimeException("Error processing SQL file: " + filePath, e);
            }
        }, fileProcessorPool);
    }

    /**
     * Detects the encoding of a file by sampling the beginning
     *
     * @param path Path to the file
     * @return Detected charset
     */
    private Charset detectFileEncoding(Path path) throws IOException {
        // Check for BOM (Byte Order Mark) first
        byte[] bom = Files.readAllBytes(path);
        if (bom.length >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        } else if (bom.length >= 2 && bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
            return StandardCharsets.UTF_16BE;
        } else if (bom.length >= 2 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            return StandardCharsets.UTF_16LE;
        }

        // No BOM detected, try to read sample with different encodings
        byte[] sample = new byte[Math.min(4096, bom.length)];
        System.arraycopy(bom, 0, sample, 0, sample.length);

        for (Charset charset : COMMON_ENCODINGS) {
            String decoded = new String(sample, charset);
            // Simple heuristic: if decoded has too many replacement characters, it's probably not the right encoding
            if (decoded.indexOf('\uFFFD') < 0) {
                return charset;
            }
        }

        // Default to UTF-8 if we can't confidently detect
        return StandardCharsets.UTF_8;
    }

    /**
     * Process a file in chunks to handle very large files efficiently
     *
     * @param path              Path to the file
     * @param charset           Detected charset
     * @param statementConsumer Consumer for processed SQL statements
     */
    private void processFileInChunks(Path path, Charset charset, Consumer<String> statementConsumer) throws IOException {
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = Files.size(path);
            SqlStatementParser parser = new SqlStatementParser();

            // Use AtomicInteger for thread-safe position tracking
            AtomicInteger position = new AtomicInteger(0);

            // Process file in chunks
            while (position.get() < fileSize) {
                ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_CHUNK_SIZE);

                // Read chunk from file
                int bytesRead = channel.read(buffer, position.get()).get();
                if (bytesRead <= 0) break;

                buffer.flip();
                byte[] data = new byte[bytesRead];
                buffer.get(data);

                // Convert bytes to string using detected charset
                String content = new String(data, charset);

                // Parse and process SQL statements
                List<String> statements = parser.parseStatements(content);
                statements.forEach(statementConsumer);

                // Update position for next chunk
                position.addAndGet(bytesRead);
            }

            // Process any remaining partial statement
            String remaining = parser.getRemainingContent();
            if (!remaining.trim().isEmpty()) {
                statementConsumer.accept(remaining);
            }
        } catch (Exception e) {
            throw new IOException("Error reading file: " + path, e);
        }
    }

    /**
     * Shutdown the processor thread pool
     */
    public void shutdown() {
        fileProcessorPool.shutdown();
    }
}
