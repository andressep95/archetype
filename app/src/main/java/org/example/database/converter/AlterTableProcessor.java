package org.example.database.converter;

import org.example.database.converter.postgres.PostgresSqlAlterTableStatementExtractor;
import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableAlteration;
import org.example.database.model.TableMetadata;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlterTableProcessor {
    private SqlAlterTableStatementExtractor alterExtractor = new PostgresSqlAlterTableStatementExtractor();

    public void processAlterStatements(List<TableMetadata> tables, String schema) {
        List<TableAlteration> alterations = alterExtractor.parseAlterations(schema);

        for (TableAlteration alteration : alterations) {
            String tableName = alteration.getTableName().toLowerCase(); // Normalizar nombre

            TableMetadata table = findTable(tables, tableName);

            switch (alteration.getAlterType()) {
                case ADD_COLUMN:
                    processAddColumn(table, alteration);
                    break;
                case MODIFY_COLUMN:
                    processModifyColumn(table, alteration);
                    break;
                case ADD_CONSTRAINT:
                    processAddConstraint(table, alteration);
                    break;
                case OTHER:
                    // Puedes manejar otros tipos de alteraciones aquí
                    break;
            }
        }
    }

    private TableMetadata findTable(List<TableMetadata> tables, String tableName) {
        return tables.stream()
            .filter(t -> t.getTableName().equalsIgnoreCase(tableName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Tabla '" + tableName + "' referenciada en ALTER TABLE no encontrada"));
    }

    private void processAddColumn(TableMetadata table, TableAlteration alteration) {
        String columnName = alteration.getTargetColumn().toLowerCase();

        // Verificar si la columna ya existe
        if (table.getColumns().stream().anyMatch(c -> c.getColumnName().equalsIgnoreCase(columnName))) {
            throw new IllegalArgumentException(
                "La columna '" + columnName + "' ya existe en la tabla '" + table.getTableName() + "'");
        }

        // Extraer definición de columna
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName(columnName);

        // Extraer tipo y propiedades
        String statement = alteration.getFullStatement();
        column.setColumnType(extractColumnType(statement));
        column.setNotNull(statement.toUpperCase().contains("NOT NULL"));
        column.setDefaultValue(extractDefaultValue(statement));

        table.getColumns().add(column);
    }

    private void processModifyColumn(TableMetadata table, TableAlteration alteration) {
        String columnName = alteration.getTargetColumn().toLowerCase();
        String statement = alteration.getFullStatement();

        ColumnMetadata column = table.getColumns().stream()
            .filter(c -> c.getColumnName().equalsIgnoreCase(columnName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Columna '" + columnName + "' no existe en tabla '" + table.getTableName() + "'"));

        // Extraer tipo NUEVO con el método corregido
        if (statement.toUpperCase().contains("TYPE")) {
            String newType = extractColumnType(statement);
            System.out.println("Changing type of " + columnName + " from " +
                column.getColumnType() + " to " + newType); // Debug
            column.setColumnType(newType);
        }

        // NOT NULL
        if (statement.toUpperCase().contains("SET NOT NULL")) {
            column.setNotNull(true);
        } else if (statement.toUpperCase().contains("DROP NOT NULL")) {
            column.setNotNull(false);
        }

        // DEFAULT
        if (statement.toUpperCase().contains("SET DEFAULT")) {
            column.setDefaultValue(extractDefaultValue(statement));
        } else if (statement.toUpperCase().contains("DROP DEFAULT")) {
            column.setDefaultValue(null);
        }
    }

    private void processAddConstraint(TableMetadata table, TableAlteration alteration) {
        String statement = alteration.getFullStatement().toUpperCase();

        if (statement.contains("PRIMARY KEY")) {
            processPrimaryKeyConstraint(table, alteration);
        } else if (statement.contains("FOREIGN KEY")) {
            processForeignKeyConstraint(table, alteration);
        } /*else if (statement.contains("UNIQUE")) {
            processUniqueConstraint(table, alteration);
        }*/
    }

    private void processPrimaryKeyConstraint(TableMetadata table, TableAlteration alteration) {
        String statement = alteration.getFullStatement();

        // Extraer columnas de la PK
        Pattern pattern = Pattern.compile(
            "(?i)PRIMARY\\s+KEY\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(statement);

        if (matcher.find()) {
            String[] pks = matcher.group(1).split("\\s*,\\s*");
            for (String pk : pks) {
                String cleanPk = pk.replaceAll("[\"']", "").trim().toLowerCase();

                // Verificar que la columna existe
                if (table.getColumns().stream().noneMatch(c -> c.getColumnName().equals(cleanPk))) {
                    throw new IllegalArgumentException(
                        "La columna '" + cleanPk + "' no existe en la tabla '" + table.getTableName() + "'");
                }

                // Agregar si no existe ya
                if (!table.getPrimaryKeys().contains(cleanPk)) {
                    table.getPrimaryKeys().add(cleanPk);
                }
            }
        }
    }

    private void processForeignKeyConstraint(TableMetadata table, TableAlteration alteration) {
        String statement = alteration.getFullStatement();

        Pattern pattern = Pattern.compile(
            "(?i)FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s+REFERENCES\\s+([^\\s(]+)\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(statement);

        if (matcher.find()) {
            String sourceColumn = matcher.group(1).replaceAll("[\"']", "").trim().toLowerCase();
            String targetTable = matcher.group(2).replaceAll("[\"']", "").trim().toLowerCase();
            String targetColumn = matcher.group(3).replaceAll("[\"']", "").trim().toLowerCase();

            // Verificar que la columna fuente existe
            if (table.getColumns().stream().noneMatch(c -> c.getColumnName().equals(sourceColumn))) {
                throw new IllegalArgumentException(
                    "La columna '" + sourceColumn + "' no existe en la tabla '" + table.getTableName() + "'");
            }

            // Verificar si la relación ya existe
            boolean relationExists = table.getRelations().stream()
                .anyMatch(r -> r.getSourceColumn().equals(sourceColumn) &&
                    r.getTargetTable().equals(targetTable) &&
                    r.getTargetColumn().equals(targetColumn));

            if (!relationExists) {
                // Crear la relación solo si no existe
                RelationMetadata relation = new RelationMetadata();
                relation.setSourceColumn(sourceColumn);
                relation.setTargetTable(targetTable);
                relation.setTargetColumn(targetColumn);
                relation.setManyToOne(true);

                table.getRelations().add(relation);
            }
        }
    }

    /*
    private void processUniqueConstraint(TableMetadata table, TableAlteration alteration) {
        String statement = alteration.getFullStatement();
        String columnName = alteration.getTargetColumn().toLowerCase();

        // Marcar la columna como única
        table.getColumns().stream()
            .filter(c -> c.getColumnName().equals(columnName))
            .findFirst()
            .ifPresent(column -> column.setUnique(true));
    }
    */

    // Métodos auxiliares para extraer información
    private String extractColumnType(String statement) {
        // Patrón mejorado para ALTER COLUMN TYPE
        Pattern typePattern = Pattern.compile(
            "(?i)ALTER\\s+COLUMN\\s+[\"']?\\w+[\"']?\\s+TYPE\\s+([^\\s,;]+(?:\\s*\\([^)]+\\))?)",
            Pattern.CASE_INSENSITIVE);
        Matcher typeMatcher = typePattern.matcher(statement);

        if (typeMatcher.find()) {
            return typeMatcher.group(1);
        }

        // Patrón para ADD COLUMN
        Pattern addPattern = Pattern.compile(
            "(?i)ADD\\s+COLUMN\\s+[\"']?\\w+[\"']?\\s+([^\\s(,;]+(?:\\s*\\([^)]+\\))?)",
            Pattern.CASE_INSENSITIVE);
        Matcher addMatcher = addPattern.matcher(statement);

        return addMatcher.find() ? addMatcher.group(1) : "varchar"; // default
    }

    private String extractDefaultValue(String statement) {
        Pattern pattern = Pattern.compile("(?i)DEFAULT\\s+([^\\s,;]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(statement);
        return matcher.find() ? matcher.group(1) : null;
    }
}