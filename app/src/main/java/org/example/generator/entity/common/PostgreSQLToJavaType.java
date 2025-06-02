package org.example.generator.entity.common;

public enum PostgreSQLToJavaType {
    // Numéricos
    SERIAL("Integer"),
    BIGSERIAL("Long"),
    SMALLSERIAL("Short"),
    INT("Integer"),
    INTEGER("Integer"),
    BIGINT("Long"),
    SMALLINT("Short"),
    DECIMAL("BigDecimal"),
    NUMERIC("BigDecimal"),
    REAL("Float"),
    DOUBLE("Double"),

    // Texto
    VARCHAR("String"),
    CHAR("String"),
    TEXT("String"),

    // Fecha y Tiempo
    DATE("LocalDate"),
    TIME("LocalTime"),
    TIMESTAMP("LocalDateTime"),
    // TIMESTAMPTZ("OffsetDateTime"),

    // Booleanos
    BOOLEAN("Boolean"),
    BOOL("Boolean"),

    // Binarios
    BYTE("byte[]"),

    // UUID
    UUID("UUID");

    private final String javaType;

    PostgreSQLToJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaType() {
        return javaType;
    }

    public static String getJavaType(String postgresType) {
        // Remover cualquier precisión o escala (e.g., VARCHAR(255) -> VARCHAR)
        String baseType = postgresType.replaceAll("\\(.*\\)", "").trim().toUpperCase();

        try {
            return valueOf(baseType).getJavaType();
        } catch (IllegalArgumentException e) {
            // Si no encontramos el tipo, retornamos Object
            return "Object";
        }
    }

    public static String getImportStatement(String postgresType) {
        String javaType = getJavaType(postgresType);
        switch (javaType) {
            case "BigDecimal":
                return "import java.math.BigDecimal;";
            case "LocalDate":
                return "import java.time.LocalDate;";
            case "LocalTime":
                return "import java.time.LocalTime;";
            case "LocalDateTime":
                return "import java.time.LocalDateTime;";
            // case "OffsetDateTime":
            // return "import java.time.OffsetDateTime;";
            case "UUID":
                return "import java.util.UUID;";
            default:
                return null;
        }
    }
}