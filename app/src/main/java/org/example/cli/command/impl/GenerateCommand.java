package org.example.cli.command.impl;

import org.example.cli.command.Command;
import org.example.cli.command.CommandRegistry;
import org.example.configuration.ConfigurationManager;
import org.example.configuration.model.AppConfiguration;
import org.example.database.SqlFileProcessorManager;
import org.example.database.parser.SqlFileContent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GenerateCommand implements Command {

    private final Map<String, Runnable> subCommands = new HashMap<>();
    private final String DEFAULT_CONFIG_PATH = "arch.yml";

    public GenerateCommand() {
        // Register the sub-commands with full names and shorthands
        subCommands.put("models", this::generateModels);
        subCommands.put("m", this::generateModels);

        subCommands.put("repos", this::generateRepositories);
        subCommands.put("r", this::generateRepositories);

        subCommands.put("services", this::generateServices);
        subCommands.put("s", this::generateServices);

        subCommands.put("controllers", this::generateControllers);
        subCommands.put("c", this::generateControllers);

        subCommands.put("all", this::generateAll);
        subCommands.put("a", this::generateAll);
    }

    @Override
    public void execute(String[] args) {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        loadConfiguration();

        // Verificar de nuevo después del intento de carga
        if (!configManager.isConfigured()) {
            System.err.println("❌ Failed to load configuration. Please run 'arch config' first or check your configuration file.");
            return;
        }

        if (args.length == 0) {
            generateAll();
            return;
        }

        String subCommand = args[0].toLowerCase();
        Runnable action = subCommands.get(subCommand);

        if (action != null) {
            action.run();
        } else {
            System.err.println("❌ Unknown generate sub-command: " + subCommand);
            System.err.println("Available options: models (m), repos (r), all (a)");
        }
    }

    private void loadConfiguration() {
        try {
            ConfigurationManager configManager = ConfigurationManager.getInstance();
            Path configPath = Paths.get(DEFAULT_CONFIG_PATH);

            // Intentar cargar la configuración
            CompletableFuture<AppConfiguration> future = configManager.loadConfiguration(configPath.toString());

            // Manejar el resultado
            future.thenAccept(config -> {
                System.out.println("✅ Configuration loaded successfully from " + configPath);
            }).exceptionally(ex -> {
                System.err.println("❌ Failed to load configuration: " + ex.getMessage());
                return null;
            }).join(); // Esperar que se complete la carga

        } catch (Exception e) {
            System.err.println("❌ Error loading configuration: " + e.getMessage());
        }
    }

    private void generateModels() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        AppConfiguration config = configManager.getConfiguration();
        String basePackage = config.getOutput().getBasePackage();

        System.out.println("Generating model classes...");

        // Crear el procesador de SQL
        SqlFileProcessorManager sqlManager = new SqlFileProcessorManager();

        try {
            // Obtener todos los archivos SQL (paths explícitos + directorio)
            CompletableFuture<List<SqlFileContent>> sqlFilesFuture =
                sqlManager.processSqlPaths(config.getSql().getSchema());

            // Procesar los archivos y generar modelos
            sqlFilesFuture.thenApply(sqlFiles -> {
                System.out.println("Found " + sqlFiles.size() + " SQL files to analyze");

                // Aquí iría la lógica real de generación de modelos
                // Por ahora solo mostramos información
                sqlFiles.forEach(file -> {
                    System.out.println("• Analyzing: " + file.getFilePath());
                    System.out.println("  Tables found: " + file.getSqlStatements());
                    // Aquí procesaríamos las tablas para generar las clases modelo
                });

                // Generar las clases modelo (ejemplo simplificado)
                System.out.println("\n✅ Generated model classes at " + basePackage + ".model");
                return sqlFiles;

            }).exceptionally(ex -> {
                System.err.println("❌ Error generating models: " + ex.getMessage());
                ex.printStackTrace();
                return null;
            }).join(); // Esperamos a que termine todo el proceso

        } finally {
            sqlManager.shutdown();
        }
    }

    private void generateRepositories() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        AppConfiguration config = configManager.getConfiguration();
        String basePackage = config.getOutput().getBasePackage();

        System.out.println("Generating repository classes...");

        CompletableFuture.runAsync(() -> {
            try {
                // Simulate some work
                Thread.sleep(500);
                System.out.println("✅ Generated repository classes at " + basePackage + ".repository");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, CommandRegistry.getInstance().getExecutor()).join();
    }

    private void generateServices() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        AppConfiguration config = configManager.getConfiguration();
        String basePackage = config.getOutput().getBasePackage();

        System.out.println("Generating services interfaces...");

        CompletableFuture.runAsync(() -> {
            try {
                // Simulate some work
                Thread.sleep(500);
                System.out.println("✅ Generated services interfaces at " + basePackage + ".service");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, CommandRegistry.getInstance().getExecutor()).join();
    }

    private void generateControllers() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        AppConfiguration config = configManager.getConfiguration();
        String basePackage = config.getOutput().getBasePackage();

        System.out.println("Generating controllers classes...");

        CompletableFuture.runAsync(() -> {
            try {
                // Simulate some work
                Thread.sleep(500);
                System.out.println("✅ Generated controllers classes at " + basePackage + ".controller");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, CommandRegistry.getInstance().getExecutor()).join();
    }

    private void generateAll() {
        System.out.println("Generating all artifacts...");

        CompletableFuture<Void> modelsFuture = CompletableFuture.runAsync(this::generateModels,
            CommandRegistry.getInstance().getExecutor());

        CompletableFuture<Void> reposFuture = CompletableFuture.runAsync(this::generateRepositories,
            CommandRegistry.getInstance().getExecutor());

        // Wait for all tasks to complete
        CompletableFuture.allOf(modelsFuture, reposFuture).join();

        System.out.println("✅ All generation tasks completed!");
    }

    @Override
    public String getDescription() {
        return "Generate code artifacts from SQL schema";
    }
}