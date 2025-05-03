public class TableAlteration {
private String tableName;
private AlterType alterType;
private String fullStatement;
private String targetColumn;
}
public class TableMetadata {
private String tableName;
private List<ColumnMetadata> columns;
private List<String> primaryKeys;
private List<RelationMetadata> relations;
}
public enum AlterType {
ADD_COLUMN,
DROP_COLUMN,
MODIFY_COLUMN,
ADD_CONSTRAINT,
OTHER
}
public class ColumnMetadata {
private String columnName;
private String columnType;
private boolean isNotNull;
private boolean isUnique;
private String defaultValue;
}
public class RelationMetadata {
private String sourceColumn;
private String targetTable;
private String targetColumn;
private boolean isManyToOne;
}