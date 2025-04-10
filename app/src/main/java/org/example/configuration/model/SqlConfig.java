package org.example.configuration.model;

/**
 * SQL configuration section.
 */
public class SqlConfig {
    private String engine;
    private SchemaConfig schema;

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public SchemaConfig getSchema() {
        return schema;
    }

    public void setSchema(SchemaConfig schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        return "SqlConfig{" +
            "engine='" + engine + '\'' +
            ", schema=" + schema +
            '}';
    }
}