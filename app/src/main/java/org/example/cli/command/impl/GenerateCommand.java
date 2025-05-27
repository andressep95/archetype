package org.example.cli.command.impl;

import org.example.cli.command.Command;
import org.example.cli.command.CommandRegistry;
import org.example.configuration.ConfigurationManager;
import org.example.configuration.model.AppConfiguration;
import org.example.database.SqlFileProcessorManager;
import org.example.database.converter.AlterTableProcessor;
import org.example.database.extractor.SchemaProcessor;
import org.example.database.model.TableMetadata;
import org.example.database.parser.SqlFileContent;
import org.example.generator.docs.DocGenerator;
import org.example.generator.entity.EntityGenerator;
import org.example.generator.entity.common.GeneratorUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.example.database.SqlFileProcessor.consolidateSqlContents;

public class GenerateCommand implements Command {

    private final Map<String, Runnable> subCommands = new HashMap<>();
    private final String DEFAULT_CONFIG_PATH = "arch.yml";
    private final GeneratorUtils generatorUtils = new GeneratorUtils();

    public GenerateCommand() {
        // Register the sub-commands with full names and shorthands
        subCommands.put("init", this::generateDocs);
        subCommands.put("i", this::generateDocs);

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

    public void loadConfiguration() {
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
            }).join();

        } catch (Exception e) {
            System.err.println("❌ Error loading configuration: " + e.getMessage());
        }
    }

    public void generateDocs() {
        DocGenerator dg = new DocGenerator();
        dg.run();
    }

    public void generateModels() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        AppConfiguration config = configManager.getConfiguration();
        String build = config.getApplication().getBuild();
        String basePackage = config.getOutput().getBasePackage();
        boolean useLombok = config.getOutput().getOptions().isLombok();
        EntityGenerator generator = new EntityGenerator(useLombok);

        // 1. Inicializar procesadores
        SchemaProcessor extractProcessor = new SchemaProcessor();
        AlterTableProcessor alterProcessor = new AlterTableProcessor();

        System.out.println("Generating model classes...");

        SqlFileProcessorManager sqlManager = new SqlFileProcessorManager();

        try {
            // 2. Obtener y procesar archivos SQL
            List<SqlFileContent> sqlContents = sqlManager.processSqlPaths(config.getSql().getSchema()).join();
            String allSqlStatements = consolidateSqlContents(sqlContents);

            // 3. Extraer y procesar metadatos
            List<TableMetadata> tables = extractProcessor.processSchema(allSqlStatements);

            // 4. Aplicar alter statements
            alterProcessor.processAlterStatements(tables, allSqlStatements);

            // 5. Generar clases de modelo
            for (TableMetadata table : tables) {
                String entity = generator.generateEntity(table, basePackage);
                generatorUtils.writeEntityFile(basePackage, table.getTableName(), entity, build);
            }

            System.out.println("\n✅ Successfully generated " + tables.size() + " model classes");

        } catch (Exception e) {
            System.err.println("❌ Error generating models: " + e.getMessage());
            e.printStackTrace();
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