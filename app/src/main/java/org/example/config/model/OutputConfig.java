package org.example.config.model;

/**
 * Output configuration section.
 */
public class OutputConfig {
    private String basePackage;
    private OutputOptions options;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public OutputOptions getOptions() {
        return options;
    }

    public void setOptions(OutputOptions options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "OutputConfig{" +
            "basePackage='" + basePackage + '\'' +
            ", options=" + options +
            '}';
    }
}