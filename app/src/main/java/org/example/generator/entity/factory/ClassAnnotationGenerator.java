package org.example.generator.entity.factory;

import org.example.database.model.*;
import org.example.generator.entity.common.UtilsFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ClassAnnotationGenerator {

    private final boolean useLombok;

    public ClassAnnotationGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateClassAnnotations(TableMetadata table, StringBuilder builder) {
        builder.append("@Entity\n");

        if (useLombok) {
            builder.append("@Getter\n");
            builder.append("@Setter\n");

            List<String> foreignKeyFields = table.getRelations().stream()
                .filter(RelationMetadata::isManyToOne)
                .map(relation -> UtilsFactory.generateFieldName(relation.getSourceColumn()))
                .collect(Collectors.toList());

            boolean hasMapsId = table.getRelations().stream()
                .filter(RelationMetadata::isManyToOne)
                .anyMatch(rel -> table.getPrimaryKeys().contains(rel.getSourceColumn()));

            if (!foreignKeyFields.isEmpty() && !hasMapsId) {
                builder.append("@ToString(exclude = {");
                for (int i = 0; i < foreignKeyFields.size(); i++) {
                    builder.append("\"").append(foreignKeyFields.get(i)).append("\"");
                    if (i < foreignKeyFields.size() - 1) builder.append(", ");
                }
                builder.append("})\n");
            } else {
                builder.append("@ToString\n");
            }

            builder.append("@NoArgsConstructor\n");
            builder.append("@AllArgsConstructor\n");
        }

        String tableName = table.getTableName().toLowerCase();

        builder.append("@Table(name = \"").append(tableName).append("\"");


        // ========== UNIQUE CONSTRAINTS ==========
        List<TableConstraintData> uniqueConstraints = table.getUniqueConstraints();

        if (!uniqueConstraints.isEmpty()) {
            builder.append(",\n    uniqueConstraints = {\n");
            for (int i = 0; i < uniqueConstraints.size(); i++) {
                TableConstraintData constraint = uniqueConstraints.get(i);
                builder.append("        @UniqueConstraint(\n")
                    .append("            name = \"")
                    .append(constraint.getConstraintName()) // Usar el nombre real de la restricción
                    .append("\",\n")
                    .append("            columnNames = {");

                // Unir los nombres de las columnas para columnNames = {"col1", "col2"}
                List<String> quotedColumnNames = constraint.getTargetColumnNames().stream()
                    .map(colName -> "\"" + colName + "\"") // Rodear cada nombre de columna con comillas dobles
                    .collect(Collectors.toList());

                builder.append(String.join(", ", quotedColumnNames));

                builder.append("}\n")
                    .append("        )");
                if (i < uniqueConstraints.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("    }");
        }

        Set<String> allUniqueColumnNames = uniqueConstraints.stream()
            .flatMap(uc -> uc.getTargetColumnNames().stream())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // ========== INDEXES ==========
        List<String> indexAnnotations = buildIndexAnnotations(table, allUniqueColumnNames);
        if (!indexAnnotations.isEmpty()) {
            builder.append(",\n    indexes = {\n");
            for (int i = 0; i < indexAnnotations.size(); i++) {
                builder.append(indexAnnotations.get(i));
                if (i < indexAnnotations.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("    }");
        }

        builder.append(")\n");
    }

    /**
     * Construye anotaciones @Index evitando duplicados y redundancias con restricciones únicas.
     *
     * @param table             la tabla con metadatos de índices
     * @param uniqueColumnNames conjunto de nombres de columnas únicos
     * @return lista de anotaciones @Index en forma de String
     */
    private List<String> buildIndexAnnotations(TableMetadata table, Set<String> uniqueColumnNames) {
        List<String> annotations = new ArrayList<>();
        Set<String> seenColumnCombos = new HashSet<>(); // para evitar duplicados exactos

        for (TableIndexData index : table.getIndexes()) {
            List<String> columns = index.getTargetColumnName();
            List<String> lowerColumns = columns.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

            // Evitar redundancia con unique simple
            if (lowerColumns.size() == 1 && uniqueColumnNames.contains(lowerColumns.get(0))) {
                continue;
            }

            String comboKey = String.join(",", lowerColumns);
            if (seenColumnCombos.contains(comboKey)) {
                continue;
            }

            seenColumnCombos.add(comboKey);

            StringBuilder idx = new StringBuilder("        @Index(\n");
            idx.append("            name = \"").append(index.getIndexName()).append("\",\n");
            idx.append("            columnList = \"").append(String.join(", ", columns)).append("\"\n");
            idx.append("        )");
            annotations.add(idx.toString());
        }

        return annotations;
    }
}
