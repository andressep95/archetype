package org.example.database.converter.postgres;

import org.example.database.converter.SqlAlterTableStatementExtractor;
import org.junit.jupiter.api.Test;

class PostgresSqlAlterTableStatementExtractorTest {

    private final SqlAlterTableStatementExtractor alterExtractor = new PostgresSqlAlterTableStatementExtractor();

    private final String sqlWithAlters = """
        ALTER TABLE users ADD COLUMN name VARCHAR(100);
        ALTER TABLE IF EXISTS users DROP COLUMN age;
        ALTER TABLE ONLY schema.users ADD CONSTRAINT pk_id PRIMARY KEY (id);
        """;

    private final String sqlWithCreateAndAlters = """
        CREATE TABLE users (id INT, name VARCHAR(100));
        ALTER TABLE users ADD COLUMN email VARCHAR(255);
        ALTER TABLE users ADD COLUMN age INT;
        """;

    @Test
    void testExtractAlterTableStatement() {
        alterExtractor.extractAlterTableStatements(sqlWithAlters).forEach(
            System.out::println
        );
    }

    @Test
    void testExtractAlterTableStatementAndName() {
        alterExtractor.extractAlterTableStatements(sqlWithAlters).forEach(
           statement -> {
               String tableName = alterExtractor.extractTableName(statement);
               System.out.println("Statement: " + statement);
               System.out.println("Table Name: " + tableName);
           }
        );
    }

    @Test
    void testParseAlterations() {
        alterExtractor.extractAlterTableStatements(sqlWithAlters).forEach(
            statement -> {
                System.out.println(alterExtractor.extractTableName(statement));
                System.out.println(alterExtractor.parseAlterations(statement));
                System.out.println("COLUMN NAME: " + alterExtractor.extractTargetColumnName(statement));
                System.out.println();
            }
        );
    }

}