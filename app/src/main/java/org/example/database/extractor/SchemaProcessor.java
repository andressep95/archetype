package org.example.database.extractor;

import org.example.database.extractor.postgres.PostgresSqlCreateTableStatementExtractor;
import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableConstraintData;
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
        System.out.println("\n====== INICIANDO PROCESAMIENTO DE ESQUEMA ======");

        List<TableMetadata> tables = new ArrayList<>();
        List<String> statements = extract.extractCreateTableStatements(schema);

        System.out.println("Se encontraron " + statements.size() + " definiciones CREATE TABLE");

        // Primera pasada: procesar estructura básica y relaciones directas
        int tableCounter = 0;
        for (String statement : statements) {
            tableCounter++;
            System.out.println("\n----- PROCESANDO TABLA #" + tableCounter + " -----");
            System.out.println("Definición SQL: " + statement);

            TableMetadata table = new TableMetadata();
            String tableName = extract.extractTableName(statement).toLowerCase(); // Normalizar nombres
            System.out.println("Nombre de la tabla detectado: " + tableName);
            table.setTableName(tableName);

            // --- NUEVO: Extraer y almacenar las restricciones UNIQUE a nivel de tabla ---
            List<TableConstraintData> uniqueConstraints = extract.extractUniqueConstraints(statement);
            table.setUniqueConstraints(uniqueConstraints); // Asumiendo que TableMetadata tiene un setUniqueConstraints
            System.out.println("Restricciones UNIQUE detectadas para '" + tableName + "': " + uniqueConstraints.size());
            for (TableConstraintData uc : uniqueConstraints) {
                System.out.println("  - Constraint: " + uc.getConstraintName() + ", Columns: " + uc.getTargetColumnNames());
            }

            // Procesar columnas
            List<ColumnMetadata> columns = new ArrayList<>();
            List<String> columnDefinitions = extract.extractColumnDefinitions(statement);
            System.out.println("Se detectaron " + columnDefinitions.size() + " definiciones de columnas");

            int colCounter = 0;
            for (String columnDef : columnDefinitions) {
                colCounter++;
                System.out.println("\n  --- Procesando columna #" + colCounter + " ---");
                System.out.println("  Definición: " + columnDef);

                ColumnMetadata column = new ColumnMetadata();
                String columnName = extract.extractColumnName(columnDef).toLowerCase();
                // Validar que el nombre de la columna no esté vacío
                if (columnName.trim().isEmpty()) {
                    System.err.println("ERROR: Nombre de columna vacío en definición: " + columnDef);
                    throw new IllegalStateException("Nombre de columna vacío en definición: " + columnDef);
                }

                System.out.println("  Nombre de columna: " + columnName);
                column.setColumnName(columnName);

                String columnType = extract.extractColumnType(columnDef);
                System.out.println("  Tipo de columna: " + columnType);
                column.setColumnType(columnType);

                boolean isNotNull = extract.isNotNullColumn(columnDef);
                System.out.println("  Es NOT NULL: " + isNotNull);
                column.setNotNull(isNotNull);

                String defaultValue = extract.extractDefaultValue(columnDef);
                System.out.println("  Valor DEFAULT: " + (defaultValue != null ? defaultValue : "null"));
                column.setDefaultValue(defaultValue);

                columns.add(column);
            }
            table.setColumns(columns);

            // Procesar claves primarias
            List<String> primaryKeys = extract.extractPrimaryKeyColumns(statement).stream()
                .map(String::toLowerCase) // Normalizar claves primarias
                .toList();

            System.out.println("\n  Se detectaron " + primaryKeys.size() + " claves primarias: " +
                primaryKeys.stream().collect(Collectors.joining(", ")));

            // Validación de claves primarias
            for (String pk : primaryKeys) {
                boolean exists = columns.stream()
                    .anyMatch(column -> column.getColumnName().equals(pk));

                if (!exists) {
                    String columnNames = columns.stream()
                        .map(ColumnMetadata::getColumnName)
                        .collect(Collectors.joining(", "));

                    System.err.println("ERROR: La clave primaria '" + pk + "' no coincide con ninguna columna.");
                    System.err.println("DEBUG - Statement: " + statement);
                    System.err.println("DEBUG - Column definitions: " + extract.extractColumnDefinitions(statement));
                    System.err.println("DEBUG - Columnas disponibles: " + columnNames);

                    throw new IllegalArgumentException(
                        String.format("La clave primaria '%s' no coincide con ninguna columna en la tabla '%s'. Columnas disponibles: %s",
                            pk, tableName, columnNames));
                } else {
                    System.out.println("  Validación exitosa: La clave primaria '" + pk + "' existe como columna");
                }
            }
            table.setPrimaryKeys(primaryKeys);

            // Procesar relaciones directas
            List<RelationMetadata> directRelations = extract.extractTableRelations(statement);
            System.out.println("\n  Se detectaron " + directRelations.size() + " relaciones directas");

            List<RelationMetadata> relations = new ArrayList<>();
            int relCounter = 0;

            for (RelationMetadata relation : directRelations) {
                relCounter++;
                String sourceColumn = relation.getSourceColumn().toLowerCase();
                String targetTable = relation.getTargetTable().toLowerCase();
                String targetColumn = relation.getTargetColumn().toLowerCase();

                System.out.println("  --- Procesando relación #" + relCounter + " ---");
                System.out.println("  Relación directa: " + tableName + "." + sourceColumn +
                    " -> " + targetTable + "." + targetColumn +
                    " (ManyToOne: " + relation.isManyToOne() + ")");

                // Verificar que la columna fuente exista
                boolean sourceExists = columns.stream()
                    .anyMatch(column -> column.getColumnName().equals(sourceColumn));
                if (!sourceExists) {
                    System.err.println("ADVERTENCIA: La columna fuente '" + sourceColumn +
                        "' de la relación no existe en la tabla '" + tableName + "'");
                }

                // Agregar relación directa
                relations.add(new RelationMetadata(
                    sourceColumn,
                    targetTable,
                    targetColumn,
                    relation.isManyToOne()));

                // Registrar relación inversa (One-to-Many)
                System.out.println("  Registrando relación inversa: " + targetTable + "." + targetColumn +
                    " -> " + tableName + "." + sourceColumn +
                    " (ManyToOne: " + !relation.isManyToOne() + ")");

                inverseRelationsMap
                    .computeIfAbsent(targetTable, k -> new ArrayList<>())
                    .add(new RelationMetadata(
                        targetColumn,
                        tableName,
                        sourceColumn,
                        !relation.isManyToOne())); // Relación inversa cambia el tipo
            }
            table.setRelations(relations);
            tables.add(table);

            System.out.println("Tabla '" + tableName + "' procesada exitosamente.");
        }

        // Segunda pasada: agregar relaciones inversas
        System.out.println("\n====== PROCESANDO RELACIONES INVERSAS ======");

        for (TableMetadata table : tables) {
            System.out.println("\nProcesando relaciones inversas para tabla: " + table.getTableName());

            List<RelationMetadata> inverseRelations = inverseRelationsMap.get(table.getTableName());
            if (inverseRelations != null && !inverseRelations.isEmpty()) {
                System.out.println("  Se encontraron " + inverseRelations.size() + " relaciones inversas");

                // Agregar las relaciones inversas a las relaciones existentes
                for (RelationMetadata invRel : inverseRelations) {
                    System.out.println("  Añadiendo relación inversa: " + table.getTableName() + "." + invRel.getSourceColumn() +
                        " -> " + invRel.getTargetTable() + "." + invRel.getTargetColumn() +
                        " (ManyToOne: " + invRel.isManyToOne() + ")");
                }

                table.getRelations().addAll(inverseRelations);
            } else {
                System.out.println("  No se encontraron relaciones inversas");
            }
        }

        // Validar relaciones de claves foráneas
        System.out.println("\n====== VALIDANDO CLAVES FORÁNEAS ======");
        try {
            validateForeignKeys(tables);
            System.out.println("Validación de claves foráneas completada con éxito");
        } catch (Exception e) {
            System.err.println("ERROR durante la validación de claves foráneas: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n====== PROCESAMIENTO DE ESQUEMA COMPLETADO ======");
        System.out.println("Total de tablas procesadas: " + tables.size());

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