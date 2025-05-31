package org.example.database.model;

public class ColumnMetadata {
    private String columnName;
    private String columnType;
    private boolean isNotNull;
    private boolean isUnique;
    private String indexName;
    private boolean hasIndex;
    private String defaultValue;

    // Constructores
    public ColumnMetadata() {
    }

    public ColumnMetadata(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isNotNull = false;
        this.isUnique = false;
        this.hasIndex = false;
        this.indexName = null;
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

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public boolean isHasIndex() {
        return hasIndex;
    }

    public void setHasIndex(boolean hasIndex) {
        this.hasIndex = hasIndex;
    }

    @Override
    public String toString() {
        return "ColumnMetadata{" +
            "columnName='" + columnName + '\'' +
            ", columnType='" + columnType + '\'' +
            ", isNotNull=" + isNotNull +
            ", isUnique=" + isUnique +
            ", indexName='" + indexName + '\'' +
            ", hasIndex=" + hasIndex +
            ", defaultValue='" + defaultValue + '\'' +
            '}';
    }
}