package org.example.database.model;

import java.util.ArrayList;
import java.util.List;

public class TableMetadata {
    private String tableName;
    private List<TableIndexData> indexes = new ArrayList<>();
    private List<ColumnMetadata> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<RelationMetadata> relations = new ArrayList<>();
    private List<TableConstraintData> uniqueConstraints = new ArrayList<>();


    // Constructores, getters, setters
    public TableMetadata() {
    }

    public TableMetadata(String tableName, List<TableIndexData> indexes, List<ColumnMetadata> columns, List<String> primaryKeys, List<RelationMetadata> relations, List<TableConstraintData> uniqueConstraints) {
        this.tableName = tableName;
        this.indexes = indexes;
        this.columns = columns;
        this.primaryKeys = primaryKeys;
        this.relations = relations;
        this.uniqueConstraints = uniqueConstraints;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TableIndexData> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<TableIndexData> indexes) {
        this.indexes = indexes;
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

    public List<TableConstraintData> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public void setUniqueConstraints(List<TableConstraintData> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    @Override
    public String toString() {
        return "TableMetadata{" +
            "tableName='" + tableName + '\'' +
            ", indexes=" + indexes +
            ", columns=" + columns +
            ", primaryKeys=" + primaryKeys +
            ", relations=" + relations +
            ", uniqueConstraints=" + uniqueConstraints +
            '}';
    }
}