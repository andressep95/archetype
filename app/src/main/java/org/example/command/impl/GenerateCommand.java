package org.example.command.impl;

import org.example.command.Command;
import org.example.command.CommandRegistry;
import org.example.config.ConfigurationManager;
import org.example.config.model.AppConfiguration;
import org.example.exception.ConfigurationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GenerateCommand implements Command {

    private final Map<String, Runnable> subCommands = new HashMap<>();

    public GenerateCommand() {
        // Register the sub-commands with full names and shorthands
        subCommands.put("models", this::generateModels);
        subCommands.put("m", this::generateModels);

        subCommands.put("repos", this::generateRepositories);
        subCommands.put("r", this::generateRepositories);

        subCommands.put("all", this::generateAll);
        subCommands.put("a", this::generateAll);
    }

    @Override
    public void execute(String[] args) {
        ConfigurationManager configManager = ConfigurationManager.getInstance();

        if (!configManager.isConfigured()) {
            throw new ConfigurationException("Configuration not loaded. Run 'arch config' first.");
        }

        if (args.length == 0) {
            // Default to generating all if no sub-command provided
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

    private void generateModels() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        AppConfiguration config = configManager.getConfiguration();

        System.out.println("Generating model classes...");

        // In a real implementation, this would use the configuration to read the schema
        // and generate the model classes
        List<String> sqlPath = config.getSql().getSchema().getPath();
        String basePackage = config.getOutput().getBasePackage();

        CompletableFuture.runAsync(() -> {
            try {
                // Simulate some work
                Thread.sleep(500);
                System.out.println("✅ Generated model classes at " + basePackage + ".model");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, CommandRegistry.getInstance().getExecutor()).join();
    }

    private void generateRepositories() {
        System.out.println("Generating repository classes...");

        CompletableFuture.runAsync(() -> {
            try {
                // Simulate some work
                Thread.sleep(500);
                System.out.println("✅ Generated repository classes");
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