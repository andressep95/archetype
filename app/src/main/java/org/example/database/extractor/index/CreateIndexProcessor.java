package org.example.database.extractor.index;

import org.example.database.extractor.index.postgres.PostgresSqlCreateIndexStatementExtractor;
import org.example.database.model.TableIndexData;
import org.example.database.model.TableMetadata;

import java.util.List;

public class CreateIndexProcessor {

    private SqlCreateIndexStatementExtractor extractor = new PostgresSqlCreateIndexStatementExtractor();

    public void processCreateIndexStatements(List<TableMetadata> tables, String schema) {
        List<TableIndexData> indexDataList = indexData(schema);

        indexDataList.forEach(indexData -> {
            // Busca la tabla correspondiente
            tables.stream()
                .filter(table -> table.getTableName().equalsIgnoreCase(indexData.getTableName()))
                .findFirst()
                .ifPresent(table -> table.getIndexes().add(indexData));
        });
    }

    private List<TableIndexData> indexData(String schema) {
        List<String> statements = extractor.extractCreateIndexStatements(schema);

        return statements.stream()
            .map(statement -> {
                String tableName = extractor.extractTableName(statement);
                String indexName = extractor.extractIndexName(statement);
                List<String> columnNames = extractor.extractTargetColumnNames(statement);

                TableIndexData data = new TableIndexData();
                data.setTableName(tableName);
                data.setIndexName(indexName);
                data.setTargetColumnName(columnNames);
                return data;
            })
            .filter(data -> data.getTableName() != null && data.getIndexName() != null)
            .toList();
    }

}
