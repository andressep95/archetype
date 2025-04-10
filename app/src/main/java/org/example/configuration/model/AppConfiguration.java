package org.example.configuration.model;

/**
 * Root configuration class that maps to the YAML structure.
 */
public class AppConfiguration {
    private String version;
    private ApplicationConfig application;
    private SqlConfig sql;
    private OutputConfig output;

    // Getters and setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ApplicationConfig getApplication() {
        return application;
    }

    public void setApplication(ApplicationConfig application) {
        this.application = application;
    }

    public SqlConfig getSql() {
        return sql;
    }

    public void setSql(SqlConfig sql) {
        this.sql = sql;
    }

    public OutputConfig getOutput() {
        return output;
    }

    public void setOutput(OutputConfig output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "AppConfiguration{" +
            "version='" + version + '\'' +
            ", application=" + application +
            ", sql=" + sql +
            ", output=" + output +
            '}';
    }
}









