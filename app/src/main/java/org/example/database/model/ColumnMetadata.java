package org.example.database.model;

public class ColumnMetadata {
    private String columnName;
    private String columnType;
    private boolean isNotNull;
    private String defaultValue;

    // Constructores
    public ColumnMetadata() {
    }

    public ColumnMetadata(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isNotNull = false;
    }

    // Getters y Setters
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public void setNotNull(boolean notNull) {
        isNotNull = notNull;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "ColumnMetadata{" +
               "columnName='" + columnName + '\'' +
               ", columnType='" + columnType + '\'' +
               ", isNotNull=" + isNotNull +
               ", defaultValue='" + defaultValue + '\'' +
               '}';
    }
}