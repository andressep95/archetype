package org.example.configuration.model;

/**
 * Application configuration section.
 */
public class ApplicationConfig {
    private String build;

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
            "build='" + build + '\'' +
            '}';
    }
}