package org.example.database.extractor;

import org.example.database.model.RelationMetadata;
import org.example.database.model.TableConstraintData;

import java.util.List;

public interface SqlCreateTableStatementExtractor {

    List<String> extractCreateTableStatements(String sql);
    List<String> extractColumnDefinitions(String sql);
    String extractTableName(String sql);
    String extractColumnName(String columnDefinition);
    String extractColumnType(String sql);
    boolean isNotNullColumn(String columnDefinition);
    boolean isUniqueColumn(String columnDefinition, String fullTableDDL);
    String extractDefaultValue(String columnDefinition);
    List<String> extractPrimaryKeyColumns(String sql);
    List<RelationMetadata> extractTableRelations(String sql);
    List<TableConstraintData> extractUniqueConstraints(String sql);

}
