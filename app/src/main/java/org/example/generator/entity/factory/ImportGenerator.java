package org.example.generator.entity.factory;

import org.example.database.model.ColumnMetadata;
import org.example.database.model.RelationMetadata;
import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.PostgreSQLToJavaType;
import org.example.generator.entity.common.UtilsFactory;

import java.util.HashSet;
import java.util.Set;

public class ImportGenerator {

    private final boolean useLombok;

    public ImportGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateImports(TableMetadata table, StringBuilder builder) {
        Set<String> imports = new HashSet<>();

        // Agregar imports básicos
        imports.add("import jakarta.persistence.Entity;");
        imports.add("import jakarta.persistence.Table;");
        imports.add("import jakarta.persistence.Column;");

        // Agregar imports de Lombok si está habilitado
        if (useLombok) {
            imports.add("import lombok.Getter;");
            imports.add("import lombok.Setter;");
            imports.add("import lombok.ToString;");
            imports.add("import lombok.NoArgsConstructor;");
            imports.add("import lombok.AllArgsConstructor;");

            // Solo añadir EqualsAndHashCode si se necesita una clase compuesta
            if (UtilsFactory.needsCompositeKey(table)) {
                imports.add("import lombok.EqualsAndHashCode;");
            }
        }

        // Otros imports relacionados con JPA según el esquema
        if (UtilsFactory.needsCompositeKey(table)) {
            imports.add("import jakarta.persistence.EmbeddedId;");
            imports.add("import jakarta.persistence.Embeddable;");
            imports.add("import java.io.Serializable;");
        } else {
            imports.add("import jakarta.persistence.Id;");
            if (table.getColumns().stream()
                .filter(c -> table.getPrimaryKeys().contains(c.getColumnName()))
                .anyMatch(c -> c.getColumnType().toUpperCase().contains("SERIAL"))) {
                imports.add("import jakarta.persistence.GeneratedValue;");
                imports.add("import jakarta.persistence.GenerationType;");
            }
        }

        boolean usesMapsId = table.getRelations().stream()
            .anyMatch(RelationMetadata::isManyToOne); // Si hay relaciones ManyToOne que usan @MapsId

        if (usesMapsId && UtilsFactory.needsCompositeKey(table)) {
            imports.add("import jakarta.persistence.MapsId;");
        }

        for (RelationMetadata relation : table.getRelations()) {
            if (relation.isManyToOne()) {
                imports.add("import jakarta.persistence.ManyToOne;");
                imports.add("import jakarta.persistence.JoinColumn;");
                imports.add("import jakarta.persistence.ForeignKey;");
            } else {
                imports.add("import jakarta.persistence.OneToMany;");
                imports.add("import jakarta.persistence.CascadeType;");
                imports.add("import java.util.Set;");
                imports.add("import java.util.HashSet;");
            }
        }

        if (table.getColumns().stream().anyMatch(ColumnMetadata::isUnique)) {
            imports.add("import jakarta.persistence.UniqueConstraint;");
        }

        boolean needsIndexImport = table.getIndexes().stream()
            .anyMatch(index -> {

                if (index.getTargetColumnName().size() == 1) {
                    String columnName = index.getTargetColumnName().get(0);
                    boolean isColumnUnique = table.getColumns().stream()
                        .filter(col -> col.getColumnName().equals(columnName))
                        .anyMatch(ColumnMetadata::isUnique);
                    return !isColumnUnique;
                }
                return true;
            });

        if (needsIndexImport) {
            imports.add("import jakarta.persistence.Index;");
        }

        for (ColumnMetadata column : table.getColumns()) {
            String importStatement = PostgreSQLToJavaType.getImportStatement(column.getColumnType());
            if (importStatement != null && !importStatement.contains("java.lang.")) {
                imports.add(importStatement);
            }
        }

        // Ordenar y agregar imports al builder
        imports.stream().sorted().forEach(imp -> builder.append(imp).append("\n"));
        builder.append("\n");
    }
}