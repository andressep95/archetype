package org.example.command.impl;

import org.example.command.Command;
import org.example.config.ConfigurationManager;
import org.example.config.model.AppConfiguration;

import java.util.concurrent.CompletableFuture;

public class ConfigCommand implements Command {

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

            // Add a handler for successful loading
            configFuture.thenAccept(config -> {
                System.out.println("✅ Configuration loaded successfully");
                System.out.println("• App builder: " + config.getApplication().getBuild());
                System.out.println("• SQL Engine: " + config.getSql().getEngine());
                System.out.println("• Schema Path: " + config.getSql().getSchema().getPath());
                System.out.println("• Base Package: " + config.getOutput().getBasePackage());
                System.out.println("• lombok enabled: " + config.getOutput().getOptions().isLombok());
            });

            // Add error handling
            configFuture.exceptionally(ex -> {
                System.err.println("❌ Failed to load configuration: " + ex.getMessage());
                return null;
            });

            // Wait for loading to complete
            configFuture.join();

        } catch (Exception e) {
            System.err.println("❌ Configuration error: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Load application configuration from specified path or default";
    }
}