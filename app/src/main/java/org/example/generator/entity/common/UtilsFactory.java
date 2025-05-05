package org.example.generator.entity.common;

import org.example.database.model.TableMetadata;

import java.util.HashMap;
import java.util.Map;

public class UtilsFactory {

    public static String toPlural(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Casos especiales en inglés para términos comunes en programación
        Map<String, String> specialCases = new HashMap<>();
        specialCases.put("user", "users");
        specialCases.put("person", "people");
        specialCases.put("child", "children");
        // Agregar más casos especiales según sea necesario

        // Revisar si es un caso especial
        if (specialCases.containsKey(input.toLowerCase())) {
            return specialCases.get(input.toLowerCase());
        }

        // Si ya termina en s, retornar como está
        if (input.endsWith("s")) {
            return input;
        }

        // Reglas para español
        if (input.endsWith("z")) {
            return input.substring(0, input.length() - 1) + "ces";
        }

        if (input.endsWith("n") || input.endsWith("l") || input.endsWith("r") ||
            input.endsWith("d") || input.endsWith("j") ||
            input.endsWith("ch") || input.endsWith("sh")) {
            return input + "es";
        }

        // Regla por defecto
        return input + "s";
    }

    public static String generateClassName(String tableName) {
        // Normalizar el nombre: eliminar caracteres no válidos
        String sanitized = tableName.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

        // Transformar a PascalCase
        String[] parts = sanitized.split("_");
        StringBuilder className = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                className.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1));
            }
        }

        return className.toString();
    }

    public static String generateFieldName(String columnName) {
        // 1. Convertir a minúsculas y dividir por guiones bajos
        String[] parts = columnName.toLowerCase().split("_");
        StringBuilder fieldName = new StringBuilder();

        // 2. Determinar si es un campo ID
        boolean isIdField = parts.length > 0 && parts[parts.length - 1].equals("id");

        // 3. Si es solo "id", retornar directamente
        if (parts.length == 1 && isIdField) {
            return "id";
        }

        // 4. Construir el nombre del campo
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            // Ignorar la parte "id" si es el último elemento y el campo es un ID
            if (i == parts.length - 1 && isIdField) {
                continue;
            }

            if (i == 0) {
                // Primera palabra en minúscula
                fieldName.append(part);
            } else {
                // Capitalizar las siguientes palabras
                if (!part.isEmpty()) {
                    fieldName.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
                }
            }
        }

        // 5. Agregar el sufijo "Id" si es un campo de ID foráneo
        if (isIdField && parts.length > 1) {
            fieldName.append("Id");
        }

        return fieldName.toString();
    }

    public static boolean needsCompositeKey(TableMetadata table) {
        return table.getPrimaryKeys().size() > 1;
    }
}