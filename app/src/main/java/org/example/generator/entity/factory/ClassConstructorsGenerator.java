package org.example.generator.entity.factory;

import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.PostgreSQLToJavaType;
import org.example.generator.entity.common.UtilsFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassConstructorsGenerator {

    private final boolean useLombok;

    public ClassConstructorsGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateConstructors(TableMetadata table, String className, StringBuilder builder) {
        // Si Lombok está habilitado, no generar constructores
        if (useLombok) {
            return;
        }

        // Constructor vacío
        builder.append("    public ").append(className).append("() {}\n\n");

        // Constructor con todos los campos
        builder.append("    public ").append(className).append("(");

        List<String> constructorParams = new ArrayList<>();

        if (UtilsFactory.needsCompositeKey(table)) {
            constructorParams.add(className + "Id id");

            // Agregar campos no-FK y no-PK
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                    .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!table.getPrimaryKeys().contains(column.getColumnName()) && !isForeignKey) {
                    String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
                    String fieldName = UtilsFactory.generateFieldName(column.getColumnName());
                    constructorParams.add(javaType + " " + fieldName);
                }
            }

            // Agregar campos FK (relaciones ManyToOne)
            for (RelationMetadata relation : table.getRelations()) {
                if (relation.isManyToOne()) {
                    String targetClass = Arrays.stream(relation.getTargetTable().toLowerCase().split("_"))
                        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                        .collect(Collectors.joining());
                    String fieldName = UtilsFactory.generateFieldName(relation.getSourceColumn());
                    constructorParams.add(targetClass + " " + fieldName);
                }
            }

            builder.append(String.join(", ", constructorParams));
            builder.append(") {\n");

            builder.append("        this.id = id;\n");

            // Asignar campos no-FK y no-PK
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                    .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!table.getPrimaryKeys().contains(column.getColumnName()) && !isForeignKey) {
                    String fieldName = UtilsFactory.generateFieldName(column.getColumnName());
                    builder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                }
            }

            // Asignar campos FK
            for (RelationMetadata relation : table.getRelations()) {
                if (relation.isManyToOne()) {
                    String fieldName = UtilsFactory.generateFieldName(relation.getSourceColumn());
                    builder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                }
            }
        } else {
            // Agregar campos no-FK
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                    .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!isForeignKey) {
                    String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
                    String fieldName = UtilsFactory.generateFieldName(column.getColumnName());
                    constructorParams.add(javaType + " " + fieldName);
                }
            }

            // Agregar campos FK (relaciones ManyToOne)
            for (RelationMetadata relation : table.getRelations()) {
                if (relation.isManyToOne()) {
                    String targetClass = Arrays.stream(relation.getTargetTable().toLowerCase().split("_"))
                        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                        .collect(Collectors.joining());
                    String fieldName = UtilsFactory.generateFieldName(relation.getSourceColumn());
                    constructorParams.add(targetClass + " " + fieldName);
                }
            }

            builder.append(String.join(", ", constructorParams));
            builder.append(") {\n");

            // Asignar campos no-FK
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                    .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!isForeignKey) {
                    String fieldName = UtilsFactory.generateFieldName(column.getColumnName());
                    builder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                }
            }

            // Asignar campos FK
            for (RelationMetadata relation : table.getRelations()) {
                if (relation.isManyToOne()) {
                    String fieldName = UtilsFactory.generateFieldName(relation.getSourceColumn());
                    builder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                }
            }
        }

        builder.append("    }\n\n");
    }
}