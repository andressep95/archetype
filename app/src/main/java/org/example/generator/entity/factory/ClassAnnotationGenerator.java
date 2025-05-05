package org.example.generator.entity.factory;

import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.UtilsFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ClassAnnotationGenerator {

    private final boolean useLombok;

    public ClassAnnotationGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateClassAnnotations(TableMetadata table, StringBuilder builder) {
        builder.append("@Entity\n");

        // Agregar anotaciones de Lombok si está habilitado
        if (useLombok) {
            builder.append("@Getter\n");
            builder.append("@Setter\n");

            // Generar @ToString con exclude para las relaciones ManyToOne
            List<String> foreignKeyFields = table.getRelations().stream()
                .filter(RelationMetadata::isManyToOne)
                .map(relation -> UtilsFactory.generateFieldName(relation.getSourceColumn())) // Usar generateFieldName para consistencia
                .collect(Collectors.toList());

            boolean hasMapsId = table.getRelations().stream()
                .filter(RelationMetadata::isManyToOne)
                .anyMatch(rel -> table.getPrimaryKeys().contains(rel.getSourceColumn()));

            if (!foreignKeyFields.isEmpty() && !hasMapsId) {
                builder.append("@ToString(exclude = {");
                for (int i = 0; i < foreignKeyFields.size(); i++) {
                    builder.append("\"").append(foreignKeyFields.get(i)).append("\"");
                    if (i < foreignKeyFields.size() - 1) {
                        builder.append(", ");
                    }
                }
                builder.append("})\n");
            } else {
                builder.append("@ToString\n");
            }

            builder.append("@NoArgsConstructor\n");
            builder.append("@AllArgsConstructor\n");
        }

        // Asegurar que el nombre de la tabla siempre esté en plural
        String tableName = table.getTableName().toLowerCase();
        if (!tableName.endsWith("s")) {
            tableName = UtilsFactory.toPlural(tableName);
        }

        builder.append("@Table(name = \"").append(tableName).append("\"");

        // Agregar restricciones de unicidad si existen
        List<ColumnMetadata> uniqueColumns = table.getColumns().stream()
            .filter(ColumnMetadata::isUnique)
            .collect(Collectors.toList());

        // Añadir restricciones de unicidad (uniqueConstraints)
        if (!uniqueColumns.isEmpty()) {
            builder.append(",\n    uniqueConstraints = {\n");
            for (int i = 0; i < uniqueColumns.size(); i++) {
                ColumnMetadata column = uniqueColumns.get(i);
                builder.append("        @UniqueConstraint(\n")
                    .append("            name = \"uk_")
                    .append(tableName)
                    .append("_")
                    .append(column.getColumnName().toLowerCase())
                    .append("\",\n")
                    .append("            columnNames = {\"")
                    .append(column.getColumnName())
                    .append("\"}\n")
                    .append("        )");
                if (i < uniqueColumns.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("    }");
        }

        // Añadir índices (indexes)
        if (!uniqueColumns.isEmpty()) {
            builder.append(",\n    indexes = {\n");
            for (int i = 0; i < uniqueColumns.size(); i++) {
                ColumnMetadata column = uniqueColumns.get(i);
                builder.append("        @Index(\n")
                    .append("            name = \"idx_")
                    .append(tableName)
                    .append("_")
                    .append(column.getColumnName().toLowerCase())
                    .append("\",\n")
                    .append("            columnList = \"")
                    .append(column.getColumnName())
                    .append("\"\n")
                    .append("        )");
                if (i < uniqueColumns.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("    }");
        }

        builder.append(")\n");
    }
}