package org.example.config;

import javax.naming.ConfigurationException;

import org.example.config.model.AppConfiguration;

import java.util.List;

/**
 * Validates configuration structures to ensure they meet required constraints.
 */
public class ConfigurationValidator {

    /**
     * Validates an AppConfiguration instance.
     *
     * @param config The configuration to validate
     * @throws ConfigurationException if validation fails
     */
    public void validate(AppConfiguration config) throws ConfigurationException {
        if (config == null) {
            throw new ConfigurationException("Configuration cannot be null");
        }

        // Validate version
        if (config.getVersion() == null || config.getVersion().trim().isEmpty()) {
            throw new ConfigurationException("Configuration version is required");
        }

        // Validate SQL section
        if (config.getSql() == null) {
            throw new ConfigurationException("SQL configuration section is required");
        }

        List<String> paths = config.getSql().getSchema().getPath();
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) {
                throw new ConfigurationException("SQL schema path entries cannot be empty");
            }
        }

        // Validate SQL engine
        String engine = config.getSql().getEngine();
        if (engine == null || engine.trim().isEmpty()) {
            throw new ConfigurationException("SQL engine is required");
        }

        if (!isValidEngine(engine)) {
            throw new ConfigurationException("Invalid SQL engine: " + engine +
                ". Supported engines: postgresql, mysql, oracle, sqlserver");
        }

        // Validate output section
        if (config.getOutput() == null) {
            throw new ConfigurationException("Output configuration section is required");
        }

        if (config.getOutput().getBasePackage() == null ||
            config.getOutput().getBasePackage().trim().isEmpty()) {
            throw new ConfigurationException("Output base package is required");
        }
    }

    /**
     * Checks if the specified engine is supported.
     *
     * @param engine The engine name
     * @return true if supported, false otherwise
     */
    private boolean isValidEngine(String engine) {
        return "postgresql".equalsIgnoreCase(engine) ||
            "mysql".equalsIgnoreCase(engine) ||
            "oracle".equalsIgnoreCase(engine) ||
            "sqlserver".equalsIgnoreCase(engine);
    }
}