package org.example.config.model;

import java.util.List;

/**
 * SQL schema configuration section.
 */
public class SchemaConfig {
    private List<String> path;

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "SchemaConfig{" +
            "path=" + path +
            '}';
    }
}