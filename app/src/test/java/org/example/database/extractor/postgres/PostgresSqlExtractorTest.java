package org.example.database.extractor.postgres;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresSqlExtractorTest {

    private final PostgresSqlExtractor extractor = new PostgresSqlExtractor();

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
        String sql = "CREATE TABLE users (id INT);";
        System.out.println("Table Name: " + extractor.extractTableName(sql));
        assertEquals("users", extractor.extractTableName(sql));
    }

    @Test
    void shouldHandleIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS accounts;";
        System.out.println("Table Name: " + extractor.extractTableName(sql));
        assertEquals("accounts", extractor.extractTableName(sql));
    }

    @Test
    void shouldExtractQuotedNames() {
        String sql = "CREATE TABLE \"MySchema\".\"MyTable\" (id INT);";
        System.out.println("Table Name: " + extractor.extractTableName(sql));
        assertEquals("MyTable", extractor.extractTableName(sql));
    }
}