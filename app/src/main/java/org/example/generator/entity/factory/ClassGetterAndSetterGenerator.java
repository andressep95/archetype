package org.example.generator.entity.factory;

import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.PostgreSQLToJavaType;
import org.example.generator.entity.common.UtilsFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassGetterAndSetterGenerator {

    private final boolean useLombok;

    public ClassGetterAndSetterGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateGettersAndSetters(TableMetadata table, StringBuilder builder) {
        // Si Lombok está habilitado, no generar getters y setters
        if (useLombok) {
            return;
        }

        if (UtilsFactory.needsCompositeKey(table)) {
            String className = UtilsFactory.generateFieldName(table.getTableName());
            className = className.substring(0, 1).toUpperCase() + className.substring(1);

            builder.append("    public ").append(className).append("Id getId() {\n")
                .append("        return id;\n")
                .append("    }\n\n");

            builder.append("    public void setId(").append(className).append("Id id) {\n")
                .append("        this.id = id;\n")
                .append("    }\n\n");

            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                    .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!table.getPrimaryKeys().contains(column.getColumnName()) && !isForeignKey) {
                    generateGetterAndSetter(column, builder);
                }
            }
        } else {
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                    .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!isForeignKey) {
                    generateGetterAndSetter(column, builder);
                }
            }
        }

        generateRelationGettersAndSetters(table, builder);
    }

    private void generateGetterAndSetter(ColumnMetadata column, StringBuilder builder) {
        String fieldName = UtilsFactory.generateFieldName(column.getColumnName());
        String capitalizedField = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());

        // Getter
        builder.append("    public ").append(javaType).append(" get")
            .append(capitalizedField).append("() {\n")
            .append("        return ").append(fieldName).append(";\n")
            .append("    }\n\n");

        // Setter
        builder.append("    public void set").append(capitalizedField)
            .append("(").append(javaType).append(" ").append(fieldName).append(") {\n")
            .append("        this.").append(fieldName).append(" = ")
            .append(fieldName).append(";\n")
            .append("    }\n\n");
    }

    private void generateRelationGettersAndSetters(TableMetadata table, StringBuilder builder) {
        Set<String> processedFields = new HashSet<>();

        for (RelationMetadata relation : table.getRelations()) {
            String targetClass = Arrays.stream(relation.getTargetTable().toLowerCase().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining());

            if (relation.isManyToOne()) {
                String fieldName = UtilsFactory.generateFieldName(relation.getSourceColumn());
                generateRelationGetterAndSetter(fieldName, targetClass, false, builder);
            } else {
                // Para OneToMany usar el nombre en plural de la clase objetivo
                String pluralField = UtilsFactory.toPlural(relation.getTargetTable().toLowerCase());
                if (!processedFields.contains(pluralField)) {
                    processedFields.add(pluralField);
                    generateRelationGetterAndSetter(pluralField, targetClass, true, builder);
                }
            }
        }
    }

    private void generateRelationGetterAndSetter(String fieldName, String targetClass, boolean isCollection, StringBuilder builder) {
        String capitalizedField = Arrays.stream(fieldName.split("_"))
            .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
            .collect(Collectors.joining());

        String camelCaseField = Character.toLowerCase(capitalizedField.charAt(0)) +
            (capitalizedField.length() > 1 ? capitalizedField.substring(1) : "");


        String type = isCollection ? "Set<" + targetClass + ">" : targetClass;

        // Getter
        builder.append("    public ").append(type).append(" get")
            .append(capitalizedField).append("() {\n")
            .append("        return ").append(camelCaseField).append(";\n")
            .append("    }\n\n");

        // Setter
        builder.append("    public void set").append(capitalizedField)
            .append("(").append(type).append(" ").append(camelCaseField).append(") {\n")
            .append("        this.").append(camelCaseField).append(" = ")
            .append(camelCaseField).append(";\n")
            .append("    }\n\n");
    }
}