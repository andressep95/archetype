package org.example.database.converter;

import org.example.database.converter.postgres.PostgresSqlAlterTableStatementExtractor;
import org.example.database.extractor.SqlCreateTableStatementExtractor;
import org.example.database.extractor.postgres.PostgresSqlCreateTableStatementExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableMergerTest {

    private final SqlCreateTableStatementExtractor extractor = new PostgresSqlCreateTableStatementExtractor();
    private final SqlAlterTableStatementExtractor alter = new PostgresSqlAlterTableStatementExtractor();

    String createSql = """
            CREATE TABLE usuarios (
                id SERIAL PRIMARY KEY,
                nombre VARCHAR(100)
            );
            ALTER TABLE usuarios ADD COLUMN email VARCHAR(255);
            ALTER TABLE usuarios ADD CONSTRAINT usuarios_email_unique UNIQUE (email);
            ALTER TABLE usuarios ADD COLUMN edad INT;
        """;

    @Test
    void extractAndAlterStatements() {

    }

}