package org.example.cli.command.impl;

import org.example.cli.command.Command;
import org.example.configuration.ConfigurationManager;
import org.example.configuration.model.AppConfiguration;
import org.example.database.parser.SqlFileContent;
import org.example.database.SqlFileProcessorManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CheckCommand implements Command {

    @Override
    public void execute(String[] args) {
        // Get the configuration manager instance
        ConfigurationManager configManager = ConfigurationManager.getInstance();

        // Define the default config file location
        String defaultConfigPath = "arch.yml";

        try {
            // If a config file was specified as an argument, use that instead
            String configPath = args.length > 0 ? args[0] : defaultConfigPath;
            System.out.println("Loading configuration from: " + configPath);

            // Load the configuration asynchronously
            CompletableFuture<AppConfiguration> configFuture =
                configManager.loadConfiguration(configPath);

            // Process configuration and SQL files
            configFuture.thenCompose(config -> {
                // Display configuration info
                System.out.println("✅ Configuration loaded successfully");
                System.out.println("• App builder: " + config.getApplication().getBuild());
                System.out.println("• SQL Engine: " + config.getSql().getEngine());

                // Display SQL Schema paths
                System.out.println("• SQL Schema paths:");
                config.getSql().getSchema().getPath().forEach(path -> {
                    System.out.println("    - " + path);
                });

                System.out.println("• Base Package: " + config.getOutput().getBasePackage());
                System.out.println("• lombok enabled: " + config.getOutput().getOptions().isLombok());

                // Now process the SQL files
                System.out.println("\nProcessing SQL schema files...");
                SqlFileProcessorManager sqlManager = new SqlFileProcessorManager();

                // Process SQL files and return the future for composition
                return sqlManager.processSqlFiles(config.getSql().getSchema().getPath())
                    .thenApply(results -> {
                        // Display SQL file processing results
                        System.out.println("\n✅ SQL Schema Analysis:");
                        displaySqlSummary(results);

                        // Return the original config to continue the chain if needed
                        return config;
                    })
                    .whenComplete((result, error) -> {
                        // Ensure resources are cleaned up
                        sqlManager.shutdown();
                    });
            }).exceptionally(ex -> {
                System.err.println("❌ Error during processing: " + ex.getMessage());
                ex.printStackTrace();
                return null;
            }).join(); // Wait for everything to complete

        } catch (Exception e) {
            System.err.println("❌ Configuration error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "Load application configuration from specified path or default";
    }

    private void displaySqlSummary(List<SqlFileContent> sqlContents) {
        for (SqlFileContent content : sqlContents) {
            System.out.println("• File: " + content.getFilePath());
            System.out.println("  - Encoding: " + content.getEncoding());
            System.out.println("  - Statements: " + content.getSqlStatements().size());

            // Display statement types summary
            displayStatementTypesSummary(content.getSqlStatements());

            // Display content in a more readable format
            System.out.println("  - Content: ");
            System.out.println("    [");

            // Print each statement with proper indentation and separation
            for (int i = 0; i < content.getSqlStatements().size(); i++) {
                String stmt = content.getSqlStatements().get(i);

                // Format the statement with proper indentation
                String formattedStmt = formatSqlStatement(stmt);

                // Print with indentation
                System.out.println("      " + formattedStmt);

                // Add a separator between statements except for the last one
                if (i < content.getSqlStatements().size() - 1) {
                    System.out.println("\n      -----------------------------\n");
                }
            }

            System.out.println("    ]");
            System.out.println();
        }
    }

    /**
     * Format an SQL statement for display
     *
     * @param statement The SQL statement to format
     * @return Formatted SQL statement
     */
    private String formatSqlStatement(String statement) {
        // Split the statement by common SQL keywords to add line breaks
        String[] keywords = {"SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY",
            "HAVING", "JOIN", "LEFT JOIN", "RIGHT JOIN",
            "INNER JOIN", "CREATE TABLE", "ALTER TABLE",
            "ADD CONSTRAINT", "REFERENCES"};

        // Replace keywords with line break + keyword
        String result = statement.trim();
        for (String keyword : keywords) {
            // Use case-insensitive replacement but preserve the original case
            String pattern = "(?i)\\s+" + keyword + "\\s+";
            result = result.replaceAll(pattern, "\n" + keyword + " ");
        }

        // Add indentation to each line
        String[] lines = result.split("\n");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                formatted.append("\n      "); // Additional indentation for continuation lines
            }
            formatted.append(lines[i].trim());
        }

        return formatted.toString();
    }

    /**
     * Analyze and display the types of SQL statements found
     */
    private void displayStatementTypesSummary(List<String> statements) {
        int createTableCount = 0;
        int createIndexCount = 0;
        int alterTableCount = 0;
        int insertCount = 0;
        int otherCount = 0;

        for (String stmt : statements) {
            String normalized = stmt.trim().toUpperCase();
            if (normalized.startsWith("CREATE TABLE")) {
                createTableCount++;
            } else if (normalized.startsWith("CREATE INDEX") || normalized.startsWith("CREATE UNIQUE INDEX")) {
                createIndexCount++;
            } else if (normalized.startsWith("ALTER TABLE")) {
                alterTableCount++;
            } else if (normalized.startsWith("INSERT")) {
                insertCount++;
            } else {
                otherCount++;
            }
        }

        System.out.println("  - Statement Types:");
        if (createTableCount > 0) System.out.println("    • CREATE TABLE: " + createTableCount);
        if (createIndexCount > 0) System.out.println("    • CREATE INDEX: " + createIndexCount);
        if (alterTableCount > 0) System.out.println("    • ALTER TABLE: " + alterTableCount);
        if (insertCount > 0) System.out.println("    • INSERT: " + insertCount);
        if (otherCount > 0) System.out.println("    • Other statements: " + otherCount);
    }
}