package org.example.database.model;

import java.util.List;
import java.util.Objects;

public class TableConstraintData {

    private String tableName;
    private String constraintName;
    private List<String> targetColumnNames;

    public TableConstraintData() {
    }

    public TableConstraintData(String tableName, String constraintName, List<String> targetColumnNames) {
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.targetColumnNames = targetColumnNames;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public List<String> getTargetColumnNames() {
        return targetColumnNames;
    }

    public void setTargetColumnNames(List<String> targetColumnNames) {
        this.targetColumnNames = targetColumnNames;
    }

    // Métodos equals y hashCode para comparar objetos TableConstraintData
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableConstraintData that = (TableConstraintData) o;
        return Objects.equals(tableName, that.tableName) &&
            Objects.equals(constraintName, that.constraintName) &&
            Objects.equals(targetColumnNames, that.targetColumnNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, constraintName, targetColumnNames);
    }

    @Override
    public String toString() {
        return "TableConstraintData{" +
            "tableName='" + tableName + '\'' +
            ", constraintname='" + constraintName + '\'' +
            ", targetColumnName=" + targetColumnNames +
            '}';
    }
}
