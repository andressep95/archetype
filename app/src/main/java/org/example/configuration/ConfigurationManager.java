package org.example.configuration;

import org.example.configuration.loader.YmlConfigurationLoader;
import org.example.configuration.model.AppConfiguration;
import org.example.configuration.validator.ConfigurationValidator;
import org.example.common.exception.ConfigurationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe singleton configuration manager that provides access to
 * application configuration loaded from YAML files.
 */
public class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private final YmlConfigurationLoader loader;
    private final ConfigurationValidator validator;
    private final Executor configExecutor;
    private final AtomicReference<AppConfiguration> currentConfig = new AtomicReference<>();

    // Private constructor
    private ConfigurationManager() {
        this.loader = new YmlConfigurationLoader();
        this.validator = new ConfigurationValidator();
        // Use a dedicated single thread executor for configuration operations
        this.configExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "config-processor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Gets the singleton instance of the ConfigurationManager.
     * Thread-safe using double-checked locking.
     *
     * @return The ConfigurationManager instance
     */
    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the configuration manager by loading the specified YAML file.
     * This method is non-blocking and returns a CompletableFuture.
     *
     * @param configFilePath Path to the configuration file
     * @return CompletableFuture that completes when configuration is loaded
     */
    public CompletableFuture<AppConfiguration> loadConfiguration(String configFilePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find configuration file
                Path path = Paths.get(configFilePath);
                if (!Files.exists(path)) {
                    throw new ConfigurationException("Configuration file not found: " + configFilePath);
                }

                // Load and parse configuration
                AppConfiguration config = loader.loadFromFile(path);

                // Validate configuration
                validator.validate(config);

                // Store the configuration
                currentConfig.set(config);

                return config;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load configuration: " + e.getMessage(), e);
            }
        }, configExecutor);
    }

    /**
     * Gets the current application configuration.
     *
     * @return The current configuration
     * @throws ConfigurationException if configuration hasn't been loaded
     */
    public AppConfiguration getConfiguration() throws ConfigurationException {
        AppConfiguration config = currentConfig.get();
        if (config == null) {
            throw new ConfigurationException("Configuration has not been loaded");
        }
        return config;
    }

    /**
     * Checks if the configuration has been loaded.
     *
     * @return true if configuration is loaded, false otherwise
     */
    public boolean isConfigured() {
        return currentConfig.get() != null;
    }

}