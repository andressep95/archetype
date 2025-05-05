package org.example.generator.entity;

import org.example.database.model.TableMetadata;
import org.example.generator.entity.common.UtilsFactory;
import org.example.generator.entity.factory.*;

public class EntityGenerator {

    private final boolean useLombok;
    private final ImportGenerator importGenerator;
    private final ClassAnnotationGenerator classAnnotationGenerator;
    private final ClassCoreGenerator classCoreGenerator;
    private final ClassConstructorsGenerator classConstructorsGenerator;
    private final ClassGetterAndSetterGenerator classGetterAndSetterGenerator;
    private final CompositeClassGenerator compositeClassGenerator;

    public EntityGenerator(boolean useLombok) {
        this.useLombok = useLombok;
        this.importGenerator = new ImportGenerator(useLombok);
        this.classAnnotationGenerator = new ClassAnnotationGenerator(useLombok);
        this.classConstructorsGenerator = new ClassConstructorsGenerator(useLombok);
        this.classGetterAndSetterGenerator = new ClassGetterAndSetterGenerator(useLombok);
        this.compositeClassGenerator = new CompositeClassGenerator(useLombok);
        this.classCoreGenerator = new ClassCoreGenerator();
    }

    public String generateEntity(TableMetadata table, String packageName) {
        StringBuilder entityBuilder = new StringBuilder();

        // 0. Agregar declaración del paquete
        entityBuilder.append("package ").append(packageName).append(";\n\n");

        // 1. Generar imports
        importGenerator.generateImports(table, entityBuilder);

        // 2. Generar anotaciones de clase
        //generateClassAnnotations(table, entityBuilder);
        classAnnotationGenerator.generateClassAnnotations(table, entityBuilder);

        // 3. Generar declaración de clase
        String className = UtilsFactory.generateClassName(table.getTableName());
        classCoreGenerator.generateClassDeclaration(className, table, entityBuilder);

        // 4. Generar campos con sus anotaciones
        classCoreGenerator.generateFields(table, entityBuilder);

        // 5. Generar constructores
        classConstructorsGenerator.generateConstructors(table, className, entityBuilder);

        // 6. Generar getters y setters
        classGetterAndSetterGenerator.generateGettersAndSetters(table, entityBuilder);

        // 7. Si tiene clave primaria compuesta, generar clase estática al final
        if (UtilsFactory.needsCompositeKey(table)) {
            compositeClassGenerator.generateCompositeKeyClass(table, entityBuilder);
        }

        // Cerrar la clase
        entityBuilder.append("}");

        return entityBuilder.toString();
    }
}