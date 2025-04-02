package org.example.config;

import org.example.config.model.*;
import org.example.exception.ConfigurationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles loading and parsing YAML configuration files without external dependencies.
 */
public class ConfigurationLoader {
    private final AtomicReference<Path> lastLoadedPath = new AtomicReference<>();

    /**
     * Loads configuration from a YAML file.
     *
     * @param path Path to the YAML file
     * @return Parsed AppConfiguration
     * @throws ConfigurationException if loading fails
     */
    public AppConfiguration loadFromFile(Path path) throws ConfigurationException {
        try {
            lastLoadedPath.set(path);
            String yamlContent = Files.readString(path);
            Map<String, Object> yamlMap = parseYaml(yamlContent);
            AppConfiguration config = mapToAppConfiguration(yamlMap);
            validateConfig(config);
            return config;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file: " + e.getMessage());
        }
    }

    /**
     * Simple YAML parser implementation without external dependencies
     *
     * @param yamlContent YAML content as string
     * @return Map representation of YAML
     */
    private Map<String, Object> parseYaml(String yamlContent) {
        Map<String, Object> rootMap = new ConcurrentHashMap<>();
        Stack<Map<String, Object>> contextStack = new Stack<>();
        Stack<Integer> indentStack = new Stack<>();

        contextStack.push(rootMap);
        indentStack.push(-1); // Nivel base

        String[] lines = yamlContent.split("\n");

        for (String line : lines) {
            String cleanLine = line.split("#")[0].trim();
            if (cleanLine.isEmpty()) continue;

            int currentIndent = line.indexOf(cleanLine);
            String[] parts = cleanLine.split(":", 2);
            String key = parts[0].trim();

            // Manejar niveles de indentación
            while (currentIndent <= indentStack.peek() && contextStack.size() > 1) {
                contextStack.pop();
                indentStack.pop();
            }

            Map<String, Object> currentMap = contextStack.peek();

            if (parts.length == 1 || parts[1].trim().isEmpty()) {
                // Nuevo nivel anidado
                Map<String, Object> newMap = new ConcurrentHashMap<>();
                currentMap.put(key, newMap);
                contextStack.push(newMap);
                indentStack.push(currentIndent);
            } else {
                // Valor directo
                String value = parts[1].trim();
                currentMap.put(key, processValue(value));
            }
        }

        return rootMap;
    }

    /**
     * Process YAML value to convert to appropriate Java type
     */
    private Object processValue(String value) {
        // Limpiar comentarios primero
        String cleanValue = value.split("#")[0].trim();

        if (cleanValue.isEmpty()) {
            return "";
        }

        // Manejar valores entre comillas
        if ((cleanValue.startsWith("\"") && cleanValue.endsWith("\""))) {
            return cleanValue.substring(1, cleanValue.length() - 1);
        }
        if ((cleanValue.startsWith("'") && cleanValue.endsWith("'"))) {
            return cleanValue.substring(1, cleanValue.length() - 1);
        }

        // Manejar booleanos
        if (cleanValue.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (cleanValue.equalsIgnoreCase("false")) return Boolean.FALSE;

        // Manejar números
        try {
            if (cleanValue.contains(".")) {
                return Double.parseDouble(cleanValue);
            } else {
                return Integer.parseInt(cleanValue);
            }
        } catch (NumberFormatException e) {
            return cleanValue;
        }
    }

    /**
     * Maps the YAML structure to AppConfiguration objects
     *
     * @param yamlMap The parsed YAML as a Map
     * @return AppConfiguration object
     */
    @SuppressWarnings("unchecked")
    private AppConfiguration mapToAppConfiguration(Map<String, Object> yamlMap) {
        AppConfiguration config = new AppConfiguration();

        // Versión (requerida)
        Object version = yamlMap.get("version");
        if (version == null) {
            throw new ConfigurationException("Missing required 'version' field");
        }
        config.setVersion(version.toString());

        // Application
        if (yamlMap.containsKey("application")) {
            Object appObj = yamlMap.get("application");
            if (!(appObj instanceof Map)) {
                throw new ConfigurationException("'application' should be a map");
            }

            Map<?, ?> appMap = (Map<?, ?>) appObj;
            ApplicationConfig appConfig = new ApplicationConfig();

            Object build = appMap.get("build");
            if (build != null) {
                appConfig.setBuild(build.toString());
            }
            config.setApplication(appConfig);
        }

        // SQL (requerido)
        Object sqlObj = yamlMap.get("sql");
        if (sqlObj == null) {
            throw new ConfigurationException("Missing required 'sql' section");
        }
        if (!(sqlObj instanceof Map)) {
            throw new ConfigurationException("'sql' should be a map");
        }

        Map<?, ?> sqlMap = (Map<?, ?>) sqlObj;
        SqlConfig sqlConfig = new SqlConfig();

        // Engine (requerido)
        Object engine = sqlMap.get("engine");
        if (engine == null) {
            throw new ConfigurationException("Missing required 'sql.engine' field");
        }
        sqlConfig.setEngine(engine.toString());

        // Schema
        Object schemaObj = sqlMap.get("schema");
        if (schemaObj instanceof Map) {
            Map<?, ?> schemaMap = (Map<?, ?>) schemaObj;
            SchemaConfig schemaConfig = new SchemaConfig();

            Object path = schemaMap.get("path");
            if (path != null) {
                schemaConfig.setPath(path.toString());
            }
            sqlConfig.setSchema(schemaConfig);
        }
        config.setSql(sqlConfig);

        // Output (requerido)
        Object outputObj = yamlMap.get("output");
        if (outputObj == null) {
            throw new ConfigurationException("Missing required 'output' section");
        }
        if (!(outputObj instanceof Map)) {
            throw new ConfigurationException("'output' should be a map");
        }

        Map<?, ?> outputMap = (Map<?, ?>) outputObj;
        OutputConfig outputConfig = new OutputConfig();

        // BasePackage (requerido)
        Object basePackage = outputMap.get("basePackage");
        if (basePackage == null) {
            throw new ConfigurationException("Missing required 'output.basePackage' field");
        }
        outputConfig.setBasePackage(basePackage.toString());

        // Options
        Object optionsObj = outputMap.get("options");
        if (optionsObj instanceof Map) {
            Map<?, ?> optionsMap = (Map<?, ?>) optionsObj;
            OutputOptions options = new OutputOptions();

            Object lombok = optionsMap.get("lombok");
            if (lombok != null) {
                if (lombok instanceof Boolean) {
                    options.setLombok((Boolean) lombok);
                } else {
                    options.setLombok(Boolean.parseBoolean(lombok.toString()));
                }
            }
            outputConfig.setOptions(options);
        }
        config.setOutput(outputConfig);

        return config;
    }

    /**
     * Reloads configuration from the last used file.
     *
     * @return Updated AppConfiguration
     * @throws ConfigurationException if reloading fails
     */
    public AppConfiguration reloadConfiguration() throws ConfigurationException {
        Path path = lastLoadedPath.get();
        if (path == null) {
            throw new ConfigurationException("No configuration file has been loaded");
        }
        return loadFromFile(path);
    }

    private void validateConfig(AppConfiguration config) throws ConfigurationException {
        if (config.getVersion() == null) {
            throw new ConfigurationException("Version is required");
        }

        if (config.getSql() == null) {
            throw new ConfigurationException("SQL configuration is required");
        }

        if (config.getSql().getEngine() == null) {
            throw new ConfigurationException("Database engine is required");
        }

        if (config.getOutput() == null || config.getOutput().getBasePackage() == null) {
            throw new ConfigurationException("Output configuration is required");
        }
    }
}