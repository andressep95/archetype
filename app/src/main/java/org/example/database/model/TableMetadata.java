package org.example.database.model;

import java.util.ArrayList;
import java.util.List;

public class TableMetadata {
    private String tableName;
    private List<ColumnMetadata> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<RelationMetadata> relations = new ArrayList<>();

    // Constructores, getters, setters
    public TableMetadata() {
    }

    public TableMetadata(String tableName, List<ColumnMetadata> columns, List<String> primaryKeys,
                         List<RelationMetadata> relations) {
        this.tableName = tableName;
        this.columns = columns;
        this.primaryKeys = primaryKeys;
        this.relations = relations;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMetadata> columns) {
        this.columns = columns;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<RelationMetadata> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationMetadata> relations) {
        this.relations = relations;
    }

    @Override
    public String toString() {
        return "TableMetadata{" +
            "tableName='" + tableName + '\'' +
            ", columns=" + columns +
            ", primaryKeys=" + primaryKeys +
            ", relations=" + relations +
            '}';
    }
}