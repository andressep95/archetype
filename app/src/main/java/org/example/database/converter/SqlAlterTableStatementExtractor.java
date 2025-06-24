package org.example.database.converter;


import org.example.database.model.TableAlteration;
import org.example.database.model.TableMetadata;

import java.util.List;

public interface SqlAlterTableStatementExtractor {

    List<String> extractAlterTableStatements(String sql);
    String extractTableName(String alterStatement);
    List<TableAlteration> parseAlterations(String sql);
    String extractTargetColumnName(String alterColumnStatement);
}