package org.example.database.extractor;

import org.example.database.extractor.postgres.PostgresSqlCreateTableStatementExtractor;
import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SchemaProcessor {

    private Map<String, List<RelationMetadata>> inverseRelationsMap = new HashMap<>();
    private final SqlCreateTableStatementExtractor extract = new PostgresSqlCreateTableStatementExtractor();

    public List<TableMetadata> processSchema(String schema) {
        List<TableMetadata> tables = new ArrayList<>();
        List<String> statements = extract.extractCreateTableStatements(schema);

        // Primera pasada: procesar estructura básica y relaciones directas
        statements.forEach(statement -> {
            TableMetadata table = new TableMetadata();
            String tableName = extract.extractTableName(statement).toLowerCase(); // Normalizar nombres
            table.setTableName(tableName);

            // Procesar columnas
            List<ColumnMetadata> columns = new ArrayList<>();
            extract.extractColumnDefinitions(statement).forEach(columnDef -> {
                ColumnMetadata column = new ColumnMetadata();
                column.setColumnName(extract.extractColumnName(columnDef).toLowerCase());
                column.setColumnType(extract.extractColumnType(columnDef));
                column.setNotNull(extract.isNotNullColumn(columnDef));
                column.setUnique(extract.isUniqueColumn(columnDef, statement));
                column.setDefaultValue(extract.extractDefaultValue(columnDef));
                columns.add(column);
            });
            table.setColumns(columns);

            // Procesar claves primarias
            List<String> primaryKeys = extract.extractPrimaryKeyColumns(statement).stream()
                .map(String::toLowerCase) // Normalizar claves primarias
                .toList();

            // Validar claves primarias con las columnas
            primaryKeys.forEach(pk -> {
                boolean exists = columns.stream()
                    .anyMatch(column -> column.getColumnName().equals(pk));
                if (!exists) {
                    throw new IllegalArgumentException(
                        String.format("La clave primaria '%s' no coincide con ninguna columna en la tabla '%s'. Columnas disponibles: %s",
                            pk, tableName,
                            columns.stream()
                                .map(ColumnMetadata::getColumnName)
                                .collect(Collectors.joining(", "))));
                }
            });
            table.setPrimaryKeys(primaryKeys);

            // Procesar relaciones directas
            List<RelationMetadata> relations = new ArrayList<>();
            extract.extractTableRelations(statement).forEach(relation -> {
                String sourceColumn = relation.getSourceColumn().toLowerCase();
                String targetTable = relation.getTargetTable().toLowerCase();
                String targetColumn = relation.getTargetColumn().toLowerCase();

                // Agregar relación directa
                relations.add(new RelationMetadata(
                    sourceColumn,
                    targetTable,
                    targetColumn,
                    relation.isManyToOne()));

                // Registrar relación inversa (One-to-Many)
                inverseRelationsMap
                    .computeIfAbsent(targetTable, k -> new ArrayList<>())
                    .add(new RelationMetadata(
                        targetColumn,
                        tableName,
                        sourceColumn,
                        !relation.isManyToOne())); // Relación inversa cambia el tipo
            });
            table.setRelations(relations);
            tables.add(table);
        });

        // Segunda pasada: agregar relaciones inversas
        tables.forEach(table -> {
            List<RelationMetadata> inverseRelations = inverseRelationsMap.get(table.getTableName());
            if (inverseRelations != null && !inverseRelations.isEmpty()) {
                // Agregar las relaciones inversas a las relaciones existentes
                table.getRelations().addAll(inverseRelations);
            }
        });

        // Validar relaciones de claves foráneas
        validateForeignKeys(tables);

        return tables;
    }

    private void validateForeignKeys(List<TableMetadata> tables) {
        // Obtener los nombres de las tablas existentes
        List<String> existingTables = tables.stream()
            .map(TableMetadata::getTableName)
            .collect(Collectors.toList());

        // Validar cada relación
        tables.forEach(table -> {
            table.getRelations().forEach(relation -> {
                if (!existingTables.contains(relation.getTargetTable())) {
                    throw new IllegalArgumentException(String.format(
                        "La tabla referenciada '%s' no existe. Referenciada desde: tabla '%s', columna '%s'.",
                        relation.getTargetTable(), table.getTableName(), relation.getSourceColumn()
                                                                    ));
                }
            });
        });
    }
}