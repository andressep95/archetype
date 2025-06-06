package org.example.configuration.model;

import java.util.List;

/**
 * SQL schema configuration section.
 */
public class SchemaConfig {
    private List<String> path;
    private String directory;

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return "SchemaConfig{" +
            "path=" + path +
            ", directory='" + directory + '\'' +
            '}';
    }
}