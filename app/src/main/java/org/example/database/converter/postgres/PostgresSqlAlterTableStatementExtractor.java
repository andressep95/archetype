package org.example.database.converter.postgres;

import org.example.database.converter.SqlAlterTableStatementExtractor;
import org.example.database.model.AlterType;
import org.example.database.model.TableAlteration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgresSqlAlterTableStatementExtractor implements SqlAlterTableStatementExtractor {

    @Override
    public List<String> extractAlterTableStatements(String sql) {
        List<String> statements = new ArrayList<>();
        Pattern pattern = Pattern.compile(
            "ALTER\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(?:ONLY\\s+)?[\\w.]+\\s+.*?(?:;|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            String statement = matcher.group().trim();
            // Asegurar que termina con punto y coma
            if (!statement.endsWith(";")) {
                statement += ";";
            }
            statements.add(statement);
        }

        return statements;
    }

    @Override
    public String extractTableName(String sql) {
        Pattern pattern = Pattern.compile(
            "(?i)ALTER\\s+TABLE\\s+" +
                "(?:IF\\s+EXISTS\\s+)?" +  // Opcional IF EXISTS
                "(?:ONLY\\s+)?" +          // Opcional ONLY (PostgreSQL)
                "((?:\"[^\"]+\"|\\w+)(?:\\.(?:\"[^\"]+\"|\\w+))?)" // Nombre de tabla
        );

        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            // Devuelve todo el nombre encontrado (puede incluir esquema o no)
            String tableName = matcher.group(1);

            // Remover comillas si las hubiera y extraer solo el nombre de tabla (sin esquema)
            return tableName.replaceAll("\"", "").replaceAll(".*\\.", "");
        }
        return null;
    }

    @Override
    public List<TableAlteration> parseAlterations(String sql) {
        List<String> statements = extractAlterTableStatements(sql);
        List<TableAlteration> alterations = new ArrayList<>();

        for (String stmt : statements) {
            String tableName = extractTableName(stmt);
            AlterType type = determineAlterType(stmt);
            String targetColumn = extractTargetColumnName(stmt);
            alterations.add(new TableAlteration(tableName, type, stmt, targetColumn));
        }

        return alterations;
    }

    private AlterType determineAlterType(String alterStatement) {
        String normalized = alterStatement.toUpperCase();
        if (normalized.contains("ADD COLUMN")) {
            return AlterType.ADD_COLUMN;
        } else if (normalized.contains("DROP COLUMN")) {
            return AlterType.DROP_COLUMN;
        } else if (normalized.contains("ALTER COLUMN") || normalized.contains("MODIFY COLUMN")) {
            return AlterType.MODIFY_COLUMN;
        } else if (normalized.contains("RENAME COLUMN") ||
            (normalized.contains("RENAME") && normalized.contains("TO") && normalized.contains("COLUMN"))) {
            return AlterType.RENAME_COLUMN;
        } else if (normalized.contains("ADD CONSTRAINT")) {
            return AlterType.ADD_CONSTRAINT;
        }
        return AlterType.OTHER;
    }

    /**
     * Extracts the name of the column being modified in an ALTER TABLE statement.
     *
     * @param alterColumnStatement The ALTER TABLE statement or an alteration clause
     * @return The name of the column being modified, or null if not found
     */
    public String extractTargetColumnName(String alterColumnStatement) {
        // First, extract the actual alteration part after the table name
        String alterationClause = alterColumnStatement;
        Pattern tablePattern = Pattern.compile(
            "ALTER\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(?:ONLY\\s+)?[\"']?[\\w\\.]+[\"']?\\s+(.*)",
            Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(alterColumnStatement);
        if (tableMatcher.find()) {
            alterationClause = tableMatcher.group(1).trim();
            // Remove trailing semicolon if present
            if (alterationClause.endsWith(";")) {
                alterationClause = alterationClause.substring(0, alterationClause.length() - 1);
            }
        }

        // --- FIRST: Handle constraints BEFORE column alterations ---

        String constraintStr = "ADD\\s+CONSTRAINT\\s+[\"']?[\\w]+[\"']?\\s+";

        // Handle ADD CONSTRAINT PRIMARY KEY or UNIQUE
        Pattern pkUqPattern = Pattern.compile(
            constraintStr + "(?:PRIMARY\\s+KEY|UNIQUE)(?:\\s+USING\\s+\\w+)?\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher pkUqMatcher = pkUqPattern.matcher(alterationClause);
        if (pkUqMatcher.find() && pkUqMatcher.group(1) != null) {
            String[] columns = pkUqMatcher.group(1).split("\\s*,\\s*");
            if (columns.length > 0) {
                return cleanIdentifier(columns[0]);
            }
        }

        // Handle ADD CONSTRAINT FOREIGN KEY
        Pattern fkPattern = Pattern.compile(
            constraintStr + "FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s+REFERENCES",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher fkMatcher = fkPattern.matcher(alterationClause);
        if (fkMatcher.find() && fkMatcher.group(1) != null) {
            String[] columns = fkMatcher.group(1).split("\\s*,\\s*");
            if (columns.length > 0) {
                return cleanIdentifier(columns[0]);
            }
        }

        // Handle ADD CONSTRAINT CHECK
        Pattern checkPattern = Pattern.compile(
            constraintStr + "CHECK\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher checkMatcher = checkPattern.matcher(alterationClause);
        if (checkMatcher.find() && checkMatcher.group(1) != null) {
            String checkExpression = checkMatcher.group(1);
            // Extract column name from expression
            Pattern columnInCheck = Pattern.compile("[\"']?([\\w]+)[\"']?\\s*(?:[<>=!]|IS|IN|LIKE|BETWEEN)",
                Pattern.CASE_INSENSITIVE);
            Matcher columnCheckMatcher = columnInCheck.matcher(checkExpression);
            if (columnCheckMatcher.find()) {
                return cleanIdentifier(columnCheckMatcher.group(1));
            }
        }

        // Handle ADD CONSTRAINT EXCLUDE
        Pattern excludePattern = Pattern.compile(
            constraintStr + "EXCLUDE(?:\\s+USING\\s+\\w+)?\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher excludeMatcher = excludePattern.matcher(alterationClause);
        if (excludeMatcher.find() && excludeMatcher.group(1) != null) {
            String excludeElements = excludeMatcher.group(1);
            Pattern excludeColPattern = Pattern.compile("([\\w\"']+)\\s+WITH");
            Matcher excludeColMatcher = excludeColPattern.matcher(excludeElements);
            if (excludeColMatcher.find()) {
                return cleanIdentifier(excludeColMatcher.group(1));
            }
        }

        // Handle ALTER CONSTRAINT
        Pattern alterConstraintPattern = Pattern.compile(
            "ALTER\\s+CONSTRAINT\\s+[\"']?([\\w]+)[\"']?\\s+(?:DEFERRABLE|NOT\\s+DEFERRABLE|INITIALLY\\s+(?:DEFERRED|IMMEDIATE))",
            Pattern.CASE_INSENSITIVE);
        Matcher alterConstraintMatcher = alterConstraintPattern.matcher(alterationClause);
        if (alterConstraintMatcher.find()) {
            // Would require metadata access to map constraint -> columns
            return null;
        }

        // Handle DROP CONSTRAINT
        Pattern dropConstraintPattern = Pattern.compile(
            "DROP\\s+CONSTRAINT\\s+(?:IF\\s+EXISTS\\s+)?[\"']?([\\w]+)[\"']?",
            Pattern.CASE_INSENSITIVE);
        Matcher dropConstraintMatcher = dropConstraintPattern.matcher(alterationClause);
        if (dropConstraintMatcher.find()) {
            // Would require metadata access
            return null;
        }

        // --- SECOND: Now handle COLUMN level operations ---

        // ADD COLUMN
        Pattern addPattern = Pattern.compile(
            "ADD\\s+(?:COLUMN\\s+)?[\"']?([\\w]+)[\"']?\\s+",
            Pattern.CASE_INSENSITIVE);
        Matcher addMatcher = addPattern.matcher(alterationClause);
        if (addMatcher.find()) {
            return cleanIdentifier(addMatcher.group(1));
        }

        // DROP COLUMN
        Pattern dropPattern = Pattern.compile(
            "DROP\\s+(?:COLUMN\\s+)?(?:IF\\s+EXISTS\\s+)?[\"']?([\\w]+)[\"']?",
            Pattern.CASE_INSENSITIVE);
        Matcher dropMatcher = dropPattern.matcher(alterationClause);
        if (dropMatcher.find()) {
            return cleanIdentifier(dropMatcher.group(1));
        }

        // ALTER COLUMN
        Pattern alterPattern = Pattern.compile(
            "ALTER\\s+(?:COLUMN\\s+)?[\"']?([\\w]+)[\"']?\\s+(?:SET|DROP|TYPE)",
            Pattern.CASE_INSENSITIVE);
        Matcher alterMatcher = alterPattern.matcher(alterationClause);
        if (alterMatcher.find()) {
            return cleanIdentifier(alterMatcher.group(1));
        }

        // RENAME COLUMN
        Pattern renamePattern = Pattern.compile(
            "RENAME\\s+(?:COLUMN\\s+)?[\"']?([\\w]+)[\"']?\\s+TO\\s+[\"']?[\\w]+[\"']?",
            Pattern.CASE_INSENSITIVE);
        Matcher renameMatcher = renamePattern.matcher(alterationClause);
        if (renameMatcher.find()) {
            return cleanIdentifier(renameMatcher.group(1));
        }

        // If no match
        return null;
    }

    /**
     * Cleans an identifier by removing invalid characters and handling quotes
     */
    private String cleanIdentifier(String identifier) {
        if (identifier == null) return null;

        // Remove quotes
        String cleaned = identifier.replaceAll("[\"']", "");

        // Replace invalid characters with underscores
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9_]", "_");

        // Consolidate multiple underscores
        cleaned = cleaned.replaceAll("_+", "_");

        // Remove leading and trailing underscores
        cleaned = cleaned.replaceAll("^_+|_+$", "");

        return cleaned;
    }
}
