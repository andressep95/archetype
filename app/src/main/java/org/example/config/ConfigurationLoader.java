package org.example.config;

import org.example.config.model.*;
import org.example.exception.ConfigurationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
        Stack<Object> contextStack = new Stack<>();
        Stack<Integer> indentStack = new Stack<>();
        Stack<String> keyStack = new Stack<>();

        contextStack.push(rootMap);
        indentStack.push(-1);
        keyStack.push(null); // Sin clave en el root

        String[] lines = yamlContent.split("\\r?\\n");

        for (String line : lines) {
            String cleanLine = line.split("#")[0].trim();
            if (cleanLine.isEmpty()) continue;

            int currentIndent = line.indexOf(cleanLine);

            // Manejo de indentación
            while (currentIndent <= indentStack.peek() && contextStack.size() > 1) {
                contextStack.pop();
                indentStack.pop();
                keyStack.pop();
            }

            if (cleanLine.startsWith("-")) {
                // Item de lista
                String itemValue = cleanLine.substring(1).trim();
                Object currentContext = contextStack.peek();

                if (currentContext instanceof List<?>) {
                    ((List<Object>) currentContext).add(processValue(itemValue));
                } else if (currentContext instanceof Map<?, ?>) {
                    String parentKey = keyStack.peek();
                    Map<String, Object> mapContext = (Map<String, Object>) currentContext;

                    Object existing = mapContext.get(parentKey);
                    List<Object> list;
                    if (existing instanceof List<?>) {
                        list = (List<Object>) existing;
                    } else {
                        list = new ArrayList<>();
                        mapContext.put(parentKey, list);
                    }

                    Object processedItem = processValue(itemValue);
                    list.add(processedItem);

                    contextStack.push(list);
                    indentStack.push(currentIndent);
                    keyStack.push(null); // No hay clave para items individuales
                }
            } else {
                String[] parts = cleanLine.split(":", 2);
                String key = parts[0].trim();

                Object currentContext = contextStack.peek();

                if (parts.length == 1 || parts[1].trim().isEmpty()) {
                    // Sección anidada vacía
                    Map<String, Object> newMap = new ConcurrentHashMap<>();
                    if (currentContext instanceof Map<?, ?>) {
                        ((Map<String, Object>) currentContext).put(key, newMap);
                    }
                    contextStack.push(newMap);
                    indentStack.push(currentIndent);
                    keyStack.push(key);
                } else {
                    String value = parts[1].trim();

                    if (currentContext instanceof Map<?, ?>) {
                        ((Map<String, Object>) currentContext).put(key, processValue(value));
                        keyStack.push(key);
                        contextStack.push(currentContext); // mantener el contexto actual para futuras líneas hijas
                        indentStack.push(currentIndent);
                    }
                }
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

        // Mapear las diferentes secciones
        config.setVersion(parseVersion(yamlMap));
        config.setApplication(parseApplicationConfig(yamlMap));
        config.setSql(parseSqlConfig(yamlMap));
        config.setOutput(parseOutputConfig(yamlMap));

        return config;
    }

    private String parseVersion(Map<String, Object> yamlMap) {
        Object version = yamlMap.get("version");
        if (version == null) {
            throw new ConfigurationException("Missing required 'version' field");
        }
        return version.toString();
    }

    private ApplicationConfig parseApplicationConfig(Map<String, Object> yamlMap) {
        if (!yamlMap.containsKey("application")) {
            return null;
        }

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

        return appConfig;
    }

    private SqlConfig parseSqlConfig(Map<String, Object> yamlMap) {
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
        sqlConfig.setSchema(parseSchemaConfig(sqlMap));

        return sqlConfig;
    }

    private SchemaConfig parseSchemaConfig(Map<?, ?> sqlMap) {
        Object schemaObj = sqlMap.get("schema");
        if (!(schemaObj instanceof Map)) {
            return null;
        }

        Map<?, ?> schemaMap = (Map<?, ?>) schemaObj;
        SchemaConfig schemaConfig = new SchemaConfig();

        Object path = schemaMap.get("path");

        if (path != null) {
            // Primer caso: path es directamente una lista
            if (path instanceof List<?>) {
                List<?> pathList = (List<?>) path;
                List<String> stringPaths = pathList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
                schemaConfig.setPath(stringPaths);
            }
            // Segundo caso: path es un mapa que contiene otra clave 'path' con la lista real
            else if (path instanceof Map && ((Map<?, ?>) path).containsKey("path")) {
                Map<?, ?> pathMap = (Map<?, ?>) path;
                Object nestedPath = pathMap.get("path");

                if (nestedPath instanceof List<?>) {
                    List<?> pathList = (List<?>) nestedPath;
                    List<String> stringPaths = pathList.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                    schemaConfig.setPath(stringPaths);
                } else if (nestedPath != null) {
                    schemaConfig.setPath(Collections.singletonList(nestedPath.toString()));
                }
            }
            // Tercer caso: path es un string único
            else if (path instanceof String) {
                schemaConfig.setPath(Collections.singletonList(path.toString()));
            }
            // Cualquier otro caso
            else {
                schemaConfig.setPath(Collections.singletonList(path.toString()));
            }
        } else {
            // Inicializar como lista vacía si no hay valor
            schemaConfig.setPath(new ArrayList<>());
        }

        return schemaConfig;
    }

    private OutputConfig parseOutputConfig(Map<String, Object> yamlMap) {
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
        outputConfig.setOptions(parseOutputOptions(outputMap));

        return outputConfig;
    }

    private OutputOptions parseOutputOptions(Map<?, ?> outputMap) {
        Object optionsObj = outputMap.get("options");
        if (!(optionsObj instanceof Map)) {
            return null;
        }

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

        return options;
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