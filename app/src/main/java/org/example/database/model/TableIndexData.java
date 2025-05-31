package org.example.database.model;

import java.util.List;

public class TableIndexData {

    private String tableName;
    private String indexName;
    private List<String> targetColumnName;

    public TableIndexData() {
    }

    public TableIndexData(String tableName, String indexName, List<String> targetColumnName) {
        this.tableName = tableName;
        this.indexName = indexName;
        this.targetColumnName = targetColumnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public List<String> getTargetColumnName() {
        return targetColumnName;
    }

    public void setTargetColumnName(List<String> targetColumnName) {
        this.targetColumnName = targetColumnName;
    }

    @Override
    public String toString() {
        return "TableIndexData{" +
               "tableName='" + tableName + '\'' +
               ", indexName='" + indexName + '\'' +
               ", targetColumnName=" + targetColumnName +
               '}';
    }
}
