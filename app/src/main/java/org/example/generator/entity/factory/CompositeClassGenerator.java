package org.example.generator.entity.factory;

import org.example.database.model.ColumnMetadata;
import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.PostgreSQLToJavaType;
import org.example.generator.entity.common.UtilsFactory;

public class CompositeClassGenerator {

    private final boolean useLombok;

    public CompositeClassGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateCompositeKeyClass(TableMetadata table, StringBuilder builder) {
        String className = UtilsFactory.generateFieldName(table.getTableName());
        className = className.substring(0, 1).toUpperCase() + className.substring(1);

        // Inicia la clase embebida
        builder.append("\n    @Embeddable\n");

        if (useLombok) {
            builder.append("    @Getter\n");
            builder.append("    @Setter\n");
            builder.append("    @ToString\n");
            builder.append("    @NoArgsConstructor\n");
            builder.append("    @AllArgsConstructor\n");
            builder.append("    @EqualsAndHashCode\n"); // Usa Lombok para generar equals() y hashCode()
        }

        builder.append("    static class ").append(className).append("Id implements Serializable {\n");

        // Genera los campos de la clave compuesta
        for (String primaryKey : table.getPrimaryKeys()) {
            ColumnMetadata column = table.getColumns().stream()
                .filter(c -> c.getColumnName().equals(primaryKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró una columna para la clave primaria: " + primaryKey + " en la tabla: " + table.getTableName()));

            String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
            String fieldName = UtilsFactory.generateFieldName(primaryKey);

            builder.append("        @Column(name = \"")
                .append(primaryKey.toLowerCase())
                .append("\")\n")
                .append("        private ")
                .append(javaType)
                .append(" ")
                .append(fieldName)
                .append(";\n");
        }

        // Genera equals() y hashCode() si Lombok no está habilitado
        if (!useLombok) {
            // Generar equals()
            builder.append("\n        @Override\n")
                .append("        public boolean equals(Object o) {\n")
                .append("            if (this == o) return true;\n")
                .append("            if (o == null || getClass() != o.getClass()) return false;\n")
                .append("            ")
                .append(className).append("Id that = (").append(className).append("Id) o;\n")
                .append("            return ");

            for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
                String fieldName = UtilsFactory.generateFieldName(table.getPrimaryKeys().get(i));
                builder.append("java.util.Objects.equals(").append(fieldName).append(", that.").append(fieldName).append(")");
                if (i < table.getPrimaryKeys().size() - 1) {
                    builder.append(" && ");
                } else {
                    builder.append(";\n");
                }
            }

            builder.append("        }\n");

            // Generar hashCode()
            builder.append("\n        @Override\n")
                .append("        public int hashCode() {\n")
                .append("            return java.util.Objects.hash(");

            for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
                String fieldName = UtilsFactory.generateFieldName(table.getPrimaryKeys().get(i));
                builder.append(fieldName);
                if (i < table.getPrimaryKeys().size() - 1) {
                    builder.append(", ");
                } else {
                    builder.append(");\n");
                }
            }

            builder.append("        }\n");
        }

        builder.append("    }\n");
    }

}