package org.example.database.model;

public class TableAlteration {
    private String tableName;
    private AlterType alterType;
    private String fullStatement;
    private String targetColumn;

    public TableAlteration() {
    }

    public TableAlteration(String tableName, AlterType alterType, String fullStatement, String targetColumn) {
        this.tableName = tableName;
        this.alterType = alterType;
        this.fullStatement = fullStatement;
        this.targetColumn = targetColumn;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public AlterType getAlterType() {
        return alterType;
    }

    public void setAlterType(AlterType alterType) {
        this.alterType = alterType;
    }

    public String getFullStatement() {
        return fullStatement;
    }

    public void setFullStatement(String fullStatement) {
        this.fullStatement = fullStatement;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    @Override
    public String toString() {
        return "TableAlteration{" +
            "tableName='" + tableName + '\'' +
            ", alterType=" + alterType +
            ", targetColumn='" + targetColumn + '\'' +
            ", fullStatement='" + fullStatement + '\'' +
            '}';
    }
}
