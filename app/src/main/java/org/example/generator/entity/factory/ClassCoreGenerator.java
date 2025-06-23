package org.example.generator.entity.factory;

import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.PostgreSQLToJavaType;
import org.example.generator.entity.common.UtilsFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ClassCoreGenerator {

    public void generateClassDeclaration(String className, TableMetadata table, StringBuilder builder) {
        // Si la clase tiene clave compuesta, debe implementar Serializable
        if (UtilsFactory.needsCompositeKey(table)) {
            builder.append("public class ").append(className)
                .append(" {\n\n");
        } else {
            builder.append("public class ").append(className).append(" {\n\n");
        }
    }

    public void generateFields(TableMetadata table, StringBuilder builder) {
        Set<String> foreignKeyColumns = new HashSet<>();
        Set<String> processedOneToManyFields = new HashSet<>();

        // Recolectar las columnas que son FKs de relaciones ManyToOne
        for (RelationMetadata relation : table.getRelations()) {
            if (relation.isManyToOne()) {
                foreignKeyColumns.add(relation.getSourceColumn());
            }
        }

        if (UtilsFactory.needsCompositeKey(table)) {
            String className = UtilsFactory.generateClassName(table.getTableName());
            builder.append("    @EmbeddedId\n")
                .append("    private ")
                .append(className)
                .append("Id id;\n\n");
        }

        // Generar campos para las columnas
        for (ColumnMetadata column : table.getColumns()) {
            boolean isForeignKey = foreignKeyColumns.contains(column.getColumnName());
            boolean isPartOfCompositeKey = UtilsFactory.needsCompositeKey(table)
                && table.getPrimaryKeys().contains(column.getColumnName());
            boolean isForeignKeyInCompositeKey = UtilsFactory.needsCompositeKey(table)
                && isForeignKey;

            if (!isForeignKey && !isPartOfCompositeKey && !isForeignKeyInCompositeKey) {
                generateFieldAnnotations(column, table.getPrimaryKeys(), builder);
                generateFieldDeclaration(column, builder);
            }
        }

        // Generar campos para las relaciones
        for (RelationMetadata relation : table.getRelations()) {
            String targetClass = UtilsFactory.generateClassName(relation.getTargetTable());

            if (relation.isManyToOne()) {
                // Generar nombre del campo basado en la columna de origen
                String fieldName = UtilsFactory.generateFieldName(relation.getSourceColumn());

                // Generar nombre único para la clave foránea
                String foreignKeyName = String.format(
                    "fk_%s_%s",
                    table.getTableName().toLowerCase(),
                    relation.getSourceColumn().toLowerCase()
                                                     );

                // Buscar la ColumnMetadata correspondiente a la columna fuente de la relación
                Optional<ColumnMetadata> sourceColumnMetadata = table.getColumns().stream()
                    .filter(col -> col.getColumnName().equalsIgnoreCase(relation.getSourceColumn()))
                    .findFirst();

                boolean isNullable = true; // Por defecto, una FK podría ser nullable
                if (sourceColumnMetadata.isPresent()) {
                    isNullable = !sourceColumnMetadata.get().isNotNull();
                }


                builder.append("    @ManyToOne\n");

                if (UtilsFactory.needsCompositeKey(table)) {
                    builder.append("    @MapsId(\"")
                        .append(UtilsFactory.generateFieldName(relation.getSourceColumn()))
                        .append("\")\n");
                }

                builder.append("    @JoinColumn(\n")
                    .append("        name = \"")
                    .append(relation.getSourceColumn())
                    .append("\",\n")
                    .append("        nullable = ").append(isNullable).append(",\n")
                    .append("        foreignKey = @ForeignKey(name = \"")
                    .append(foreignKeyName)
                    .append("\")\n")
                    .append("    )\n")
                    .append("    private ")
                    .append(targetClass)
                    .append(" ")
                    .append(fieldName)
                    .append(";\n\n");
            } else {
                // Relaciones OneToMany
                String pluralFieldName = UtilsFactory.toPlural(UtilsFactory.generateFieldName(relation.getTargetTable()));

                if (!processedOneToManyFields.contains(pluralFieldName)) {
                    processedOneToManyFields.add(pluralFieldName);

                    String targetFieldName = UtilsFactory.generateFieldName(relation.getTargetColumn());

                    builder.append("    @OneToMany(\n")
                        .append("        mappedBy = \"")
                        .append(targetFieldName) // Usar el campo relacionado en la clase destino
                        .append("\",\n")
                        .append("        cascade = CascadeType.ALL,\n")
                        .append("        orphanRemoval = true\n")
                        .append("    )\n")
                        .append("    private Set<")
                        .append(targetClass)
                        .append("> ")
                        .append(pluralFieldName)
                        .append(" = new HashSet<>();\n\n");
                }
            }
        }
    }

    private void generateFieldAnnotations(ColumnMetadata column, List<String> primaryKeys, StringBuilder builder) {
        // Si es parte de la clave primaria
        if (primaryKeys.contains(column.getColumnName())) {
            builder.append("    @Id\n");
            if (column.getColumnType().toUpperCase().contains("SERIAL")) {
                builder.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
            }
            if (column.getColumnType().toUpperCase().contains("UUID")) {
                builder.append("    @GeneratedValue(strategy = GenerationType.UUID)\n");
            }
        }

        // Si es de tipo BYTEA
        if (column.getColumnType().equalsIgnoreCase("BYTEA")) {
            builder.append("    @Lob\n");
        }

        // Generar @Column con sus propiedades
        builder.append("    @Column(name = \"").append(column.getColumnName()).append("\"");

        if (column.isNotNull()) {
            builder.append(", nullable = false");
        }

        // Si el tipo es VARCHAR, agregar length
        if (column.getColumnType().toUpperCase().startsWith("VARCHAR")) {
            String length = column.getColumnType().replaceAll("\\D+", "");
            builder.append(", length = ").append(length);
        }

        // Si tiene valor por defecto, incluirlo en columnDefinition
        if (column.getDefaultValue() != null) {
            builder.append(", columnDefinition = \"")
                .append(column.getColumnType())
                .append(" DEFAULT ")
                .append(column.getDefaultValue())
                .append("\"");
        }

        builder.append(")\n");
    }

    private void generateFieldDeclaration(ColumnMetadata column, StringBuilder builder) {
        String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
        String fieldName = UtilsFactory.generateFieldName(column.getColumnName());

        builder.append("    private ").append(javaType).append(" ")
            .append(fieldName).append(";\n\n");
    }

}
