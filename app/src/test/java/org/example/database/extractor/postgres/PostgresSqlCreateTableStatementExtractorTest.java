package org.example.database.extractor.postgres;

import org.example.database.model.RelationMetadata;
import org.example.database.model.TableConstraintData;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresSqlCreateTableStatementExtractorTest {

    private final PostgresSqlCreateTableStatementExtractor extractor = new PostgresSqlCreateTableStatementExtractor();

    private final String TEST_SCHEMA = """
            -- Tabla 1: PRIMARY KEY simple básica
            CREATE TABLE users (
                id INTEGER PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                email VARCHAR(100) UNIQUE
            );
        
            -- Tabla 2: PRIMARY KEY con SERIAL
            CREATE TABLE products (
                product_id SERIAL PRIMARY KEY,
                name VARCHAR(100),
                price NUMERIC(10,2),
                CONSTRAINT unique_name UNIQUE (name)
            );
        
            -- Tabla 3: PRIMARY KEY con CONSTRAINT nombrado
            CREATE TABLE categories (
                category_id INTEGER CONSTRAINT pk_category PRIMARY KEY,
                name VARCHAR(50) UNIQUE,
                description TEXT
            );
        
            -- Tabla 4: PRIMARY KEY con NOT NULL y UNIQUE
            CREATE TABLE customers (
                customer_id UUID NOT NULL PRIMARY KEY,
                username VARCHAR(50),
                email VARCHAR(100),
                CONSTRAINT unique_username_email UNIQUE (username, email)
            );
        
            -- Tabla 5: PRIMARY KEY compuesta con CONSTRAINT
            CREATE TABLE order_items (
                order_id INTEGER,
                product_id INTEGER,
                quantity INTEGER,
                price NUMERIC(10,2),
                CONSTRAINT pk_order_items PRIMARY KEY (order_id, product_id)
            );
        
            -- Tabla 6: PRIMARY KEY compuesta sin nombre de CONSTRAINT
            CREATE TABLE inventory_movements (
                product_id INTEGER,
                warehouse_id INTEGER,
                movement_date TIMESTAMP,
                quantity INTEGER,
                PRIMARY KEY (product_id, warehouse_id, movement_date)
            );
        
            -- Tabla 7: PRIMARY KEY con tipo numérico con precisión
            CREATE TABLE financial_records (
                transaction_id NUMERIC(20,0) PRIMARY KEY,
                amount NUMERIC(15,2),
                transaction_date TIMESTAMP
            );
        
            -- Tabla 8: PRIMARY KEY con múltiples constraints
            CREATE TABLE employees (
                employee_id INTEGER NOT NULL CONSTRAINT emp_pk PRIMARY KEY,
                email VARCHAR(100) UNIQUE,
                hire_date DATE NOT NULL
            );
        
            -- Tabla 9: PRIMARY KEY tipo BIGINT con espacios adicionales
            CREATE TABLE logs (
                log_id BIGINT     PRIMARY    KEY,
                log_date TIMESTAMP,
                message TEXT
            );

        """;

    private final String TEST_SCHEMA_COMPOSITE = """
            -- Tabla 1: PK compuesta básica con CONSTRAINT
            CREATE TABLE order_items (
                order_id INTEGER,
                product_id INTEGER,
                quantity INTEGER,
                CONSTRAINT pk_order_items PRIMARY KEY (order_id, product_id)
            );
        
            -- Tabla 2: PK compuesta sin nombre de CONSTRAINT
            CREATE TABLE inventory_log (
                product_id INTEGER,
                date_time TIMESTAMP,
                warehouse_id INTEGER,
                quantity INTEGER,
                PRIMARY KEY (product_id, date_time, warehouse_id)
            );
        
            -- Tabla 3: PK compuesta con columnas inline y constraint
            CREATE TABLE document_versions (
                doc_id INTEGER PRIMARY KEY,
                version_id INTEGER,
                content TEXT,
                CONSTRAINT pk_version PRIMARY KEY (version_id)
            );
        
            -- Tabla 4: PK compuesta con múltiples constraints
            CREATE TABLE shipment_details (
                shipment_id INTEGER,
                container_id INTEGER,
                product_id INTEGER,
                CONSTRAINT pk_shipment PRIMARY KEY (shipment_id),
                CONSTRAINT pk_container PRIMARY KEY (container_id, product_id)
            );
        
            -- Tabla 5: PK compuesta con NOT NULL y otros modificadores
            CREATE TABLE financial_transactions (
                account_id INTEGER NOT NULL,
                transaction_date TIMESTAMP NOT NULL,
                sequence_number INTEGER NOT NULL,
                amount NUMERIC(10,2),
                CONSTRAINT pk_transaction PRIMARY KEY (account_id, transaction_date, sequence_number)
            );
        
            -- Tabla 6: PK compuesta con espaciado irregular
            CREATE TABLE audit_log (
                entity_id     INTEGER,
                action_type   VARCHAR(50),
                timestamp    TIMESTAMP,
                CONSTRAINT    pk_audit    PRIMARY    KEY    (   entity_id   ,    action_type,timestamp   )
            );
        """;

    private final String TEST_SCHEMA_IMPOSSIBLE = """
        -- impossible2.sql
        -- Schema con casos extremos de PRIMARY KEYs en PostgreSQL
        -- Este archivo contiene casos complejos y desafiantes de definiciones de claves primarias
        
        -- ===================================================================================
        -- Tabla 1: PRIMARY KEYs con múltiples definiciones y formatos mixtos
        -- ===================================================================================
        CREATE TABLE mixed_keys (
            id1 INTEGER     PRIMARY    KEY,                                    -- Espaciado irregular
            id2 INTEGER NOT NULL CONSTRAINT pk_mixed UNIQUE PRIMARY KEY,       -- Múltiples constraints inline
            id3 INTEGER 
                CONSTRAINT another_pk 
                PRIMARY 
                KEY,                                                          -- PK multilínea
            CONSTRAINT composite_pk PRIMARY KEY (id1, id2, id3)               -- PK compuesta redundante
        );
        
        -- ===================================================================================
        -- Tabla 2: PRIMARY KEYs con comentarios intercalados
        -- ===================================================================================
        CREATE TABLE commented_keys (
            /* Columna principal */
            user_id SERIAL,                                                   -- Auto-incrementing
            /* Segunda columna */
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            /* Tercera columna */
            action_type VARCHAR(50),
            /* Definición de primary key */
            CONSTRAINT 
                /* Nombre del constraint */
                pk_commented 
                /* Tipo de constraint */
                PRIMARY 
                /* Keyword final */
                KEY 
                /* Columnas */
                (user_id, 
                 -- Primera columna
                 timestamp, 
                 /* Segunda columna */
                 action_type)
        );
        
        -- ===================================================================================
        -- Tabla 3: PRIMARY KEYs con múltiples estilos de definición
        -- ===================================================================================
        CREATE TABLE multi_pk_styles (
            col1 INTEGER PRIMARY KEY,                                         -- Simple inline
            col2 INTEGER CONSTRAINT pk2 PRIMARY KEY,                          -- Con constraint inline
            col3 INTEGER,
            col4 INTEGER,
            col5 INTEGER     PRIMARY        KEY,                             -- Con espaciado extraño
            FOREIGN KEY (col3) REFERENCES other_table(id),                   -- FK para confundir
            PRIMARY KEY (col3),                                              -- PK sin nombre
            CONSTRAINT pk_custom PRIMARY KEY (col4),                         -- PK con nombre
            CONSTRAINT "pk-special.name" PRIMARY KEY (col5)                  -- PK con nombre especial
        );
        
        -- ===================================================================================
        -- Tabla 4: PRIMARY KEYs con nombres especiales y caracteres especiales
        -- ===================================================================================
        CREATE TABLE "complex.named_table" (
            "user.id" INTEGER,
            "timestamp.created" TIMESTAMP,
            "special-column" VARCHAR(50),
            CONSTRAINT "pk.with.dots" PRIMARY KEY ("user.id"),
            CONSTRAINT "pk-with-hyphens" PRIMARY KEY ("timestamp.created"),
            CONSTRAINT "pk_with_all.types-mixed" PRIMARY KEY ("special-column")
        );
        
        -- ===================================================================================
        -- Tabla 5: PRIMARY KEYs en diferentes posiciones y con diferentes tipos
        -- ===================================================================================
        CREATE TABLE mixed_positions (
            id1 UUID PRIMARY KEY,                                            -- UUID como PK
            id2 BIGINT,
            CONSTRAINT pk_mid PRIMARY KEY (id2),                            -- PK en medio
            id3 DECIMAL(20,2),
            id4 VARCHAR(100),
            PRIMARY KEY (id3, id4),                                         -- PK al final sin nombre
            CHECK (id2 > 0),                                                -- Otros constraints
            UNIQUE (id1, id2)
        );
        
        -- ===================================================================================
        -- Tabla 6: PRIMARY KEYs con tipos de datos complejos y arrays
        -- ===================================================================================
        CREATE TABLE complex_types (
            id1 INTEGER[],                                                  -- Array simple
            id2 NUMERIC(20,5)[],                                           -- Array con precisión
            id3 VARCHAR(100) ARRAY[3],                                     -- Array alterno
            id4 TIMESTAMP WITH TIME ZONE,
            CONSTRAINT pk_arrays PRIMARY KEY (id1, id2[1], id3[1]),        -- PK con arrays
            CONSTRAINT pk_timestamp PRIMARY KEY (id4)                       -- PK con timestamp
        );
        """;

    private final String TEST_RELATION = """
        CREATE TABLE customers (
            customer_id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL
        );
        
        CREATE TABLE addresses (
            address_id SERIAL PRIMARY KEY,
            street VARCHAR(100) NOT NULL,
            city VARCHAR(50) NOT NULL
        );
        
        CREATE TABLE orders (
            order_id SERIAL PRIMARY KEY,
            order_date DATE NOT NULL,
            customer_id INTEGER NOT NULL REFERENCES customers(customer_id),
            billing_address_id INTEGER NOT NULL,
            shipping_address_id INTEGER,
            CONSTRAINT fk_billing_address FOREIGN KEY (billing_address_id) REFERENCES addresses(address_id),
            CONSTRAINT fk_shipping_address FOREIGN KEY (shipping_address_id) REFERENCES addresses(address_id)
        );
        """;

    // TEST TO IDENTIFY THE BODY OF THE SCHEMAS
    @Test
    void extractCreateTableStatements_shouldExtractBasicTables() {
        String sql = """
            CREATE TABLE customer (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100)
            );
            
            CREATE TABLE order_table (
                id SERIAL PRIMARY KEY,
                customer_id INTEGER REFERENCES customer(id)
            );""";

        List<String> statements = extractor.extractCreateTableStatements(sql);
        System.out.println(statements);

        assertEquals(2, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE TABLE customer"));
        assertTrue(statements.get(1).startsWith("CREATE TABLE order_table"));
    }

    @Test
    void shouldHandleBasicCreateTable() {
        String sql = "CREATE TABLE test (id INT);";
        List<String> result = extractor.extractCreateTableStatements(sql);

        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals("CREATE TABLE test (id INT);", result.get(0));
    }

    @Test
    void shouldHandleSchemaQualifiedTables() {
        String sql = "CREATE TABLE public.users (id SERIAL);";
        List<String> result = extractor.extractCreateTableStatements(sql);

        System.out.println(result);
        assertTrue(result.get(0).contains("public.users"));
    }

    @Test
    void shouldIgnoreComments() {
        String sql = """
            -- This is a comment
            CREATE TABLE comments_test (
                id INT /* inline comment */
            );
            /* Multi-line
               comment */
            CREATE TABLE another_table (name TEXT);""";

        List<String> result = extractor.extractCreateTableStatements(sql);

        System.out.println(result);
        assertEquals(2, result.size());
        assertFalse(result.get(0).contains("--")); // Verifica que no incluyó comentarios
    }

    @Test
    void shouldExtractComplexRealWorldExample() {
        String realWorldSql = """
            CREATE TABLE customer (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100),
                email VARCHAR(100) UNIQUE
            );
            
            CREATE TABLE order_table (
                id SERIAL PRIMARY KEY,
                customer_id INTEGER REFERENCES customer(id),
                order_date DATE NOT NULL
            );
            
            CREATE TABLE payment (
                id SERIAL PRIMARY KEY,
                order_id INTEGER REFERENCES order_table(id),
                amount DECIMAL(10,2),
                payment_date DATE
            );""";

        List<String> statements = extractor.extractCreateTableStatements(realWorldSql);
        System.out.println(statements);

        assertEquals(3, statements.size());
        assertAll(
            () -> assertTrue(statements.get(0).contains("customer")),
            () -> assertTrue(statements.get(1).contains("order_table")),
            () -> assertTrue(statements.get(2).contains("payment"))
                 );
    }

    @Test
    void shouldHandleLargeInputEfficiently() {
        String largeSql = "CREATE TABLE big (id INT);".repeat(1000);
        assertTimeout(Duration.ofMillis(100),
            () -> extractor.extractCreateTableStatements(largeSql));
    }

    @Test
    void shouldExtractCreateTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY
            );
            
            CREATE TABLE IF NOT EXISTS "schema"."accounts" (
                balance DECIMAL(10,2)
            );""";

        List<String> statements = extractor.extractCreateTableStatements(sql);

        System.out.println(statements);
        assertEquals(2, statements.size());
    }


    // TEST TO IDENTIFY THE SCHEMAS NAME
    @Test
    void shouldExtractSimpleTableName() {
        System.out.println("Table Name: " + extractor.extractTableName(TEST_SCHEMA));
        assertEquals("users", extractor.extractTableName(TEST_SCHEMA));
    }

    @Test
    void shouldHandleIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS accounts;";
        System.out.println("Table Name: " + extractor.extractTableName(sql));
        assertEquals("accounts", extractor.extractTableName(sql));
    }

    @Test
    void shouldExtractQuotedNames() {
        System.out.println("Table Name: " + extractor.extractTableName(TEST_SCHEMA));
        assertEquals("users", extractor.extractTableName(TEST_SCHEMA));
    }


    // TEST TO EXTRACT DEFINITIONS FROM THE BODY OF THE SCHEMAS
    @Test
    void shouldExtractColumnDefinitions() {

        List<String> schemas = extractor.extractCreateTableStatements(TEST_SCHEMA_IMPOSSIBLE);
        for (String schema : schemas) {
            String tableName = extractor.extractTableName(schema);
            List<String> primaryKeys = extractor.extractPrimaryKeyColumns(schema);
            System.out.println("Table Name: " + tableName);
            System.out.println("Primary Keys: " + primaryKeys);
            List<String> definitions = extractor.extractColumnDefinitions(schema);
            for (String def : definitions) {
                System.out.println("Column Definition: " + def);
            }
            System.out.println();
        }
    }


    // TEST TO EXTRACT THE COLUMN NAME
    @Test
    void shouldExtractColumnName() {
        List<String> schemas = extractor.extractCreateTableStatements(TEST_SCHEMA);
        for (String schema : schemas) {
            String tableName = extractor.extractTableName(schema);
            System.out.println("Table Name: " + tableName);
            List<String> definitions = extractor.extractColumnDefinitions(schema);
            for (String def : definitions) {
                String columnName = extractor.extractColumnName(def);
                System.out.println("Column Name: " + columnName);
            }
            System.out.println();
        }
    }


    // TEST TO EXTRACT THE COLUMN TYPE
    @Test
    void shouldExtractColumnType() {
        List<String> schemas = extractor.extractCreateTableStatements(TEST_SCHEMA);
        for (String schema : schemas) {
            String tableName = extractor.extractTableName(schema);
            System.out.println("Table Name: " + tableName);
            List<String> definitions = extractor.extractColumnDefinitions(schema);
            for (String def : definitions) {
                String columnName = extractor.extractColumnName(def);
                String columnType = extractor.extractColumnType(def);

                System.out.println("Column Name: " + columnName + " it´s type: " + columnType);
            }
            System.out.println();
        }
    }


    // TEST TO EXTRACT THE COLUMN NULLABLE OR/AND UNIQUE
    @Test
    void shouldExtractColumnNullableOrUniqueOrDefault() {
        List<String> schemas = extractor.extractCreateTableStatements(TEST_SCHEMA);

        for (String schema : schemas) {
            String tableName = extractor.extractTableName(schema);
            List<String> primaryKeys = extractor.extractPrimaryKeyColumns(schema);
            List<RelationMetadata> relations = extractor.extractTableRelations(schema);

            System.out.println("Table Name: " + tableName);
            System.out.println("Primary Keys: " + primaryKeys);
            System.out.println("Relations: " + relations);
            List<String> definitions = extractor.extractColumnDefinitions(schema);
            for (String def : definitions) {
                String columnName = extractor.extractColumnName(def);
                String columnType = extractor.extractColumnType(def);
                boolean columnNotNull = extractor.isNotNullColumn(def);
                boolean columnUnique = extractor.isUniqueColumn(def, schema);
                String defaultValue = extractor.extractDefaultValue(def);

                System.out.println("Column Name: " + columnName);
                System.out.println("Column Type: " + columnType);
                System.out.println("Column Not Null: " + columnNotNull);
                System.out.println("Column Unique: " + columnUnique);
                System.out.println("Column Default Value: " + defaultValue);
                System.out.println();
            }
            System.out.println();
        }
    }

    @Test
    void testExtractUniqueConstraintsFromSchemas() {
        List<String> schemas = extractor.extractCreateTableStatements(TEST_SCHEMA);
        for (String schema : schemas) {
            String tableName = extractor.extractTableName(schema);
            List<TableConstraintData> uniqueConstraints = extractor.extractUniqueConstraints(schema);

            System.out.println("Schema: " + schema);
            System.out.println("Table Name: " + tableName);
            System.out.println("Unique Constraints: " + uniqueConstraints);
            for (TableConstraintData constraint : uniqueConstraints) {
                System.out.println("Constraint Name: " + constraint.getConstraintName());
                System.out.println("Target Columns: " + constraint.getTargetColumnNames());
            }
            System.out.println();
        }
    }
}