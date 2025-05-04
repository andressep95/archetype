package org.example.database.extractor;

import org.example.database.converter.AlterTableProcessor;
import org.example.database.converter.SqlAlterTableStatementExtractor;
import org.example.database.converter.postgres.PostgresSqlAlterTableStatementExtractor;
import org.example.database.model.TableMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaProcessorTest {

    private final SchemaProcessor extractProcessor = new SchemaProcessor();
    private final AlterTableProcessor alterProcessor = new AlterTableProcessor();

    private static final String STUDENT_STATEMENT = """
        CREATE TABLE student (
            id SERIAL PRIMARY KEY,
            full_name VARCHAR(100),
            email VARCHAR(100) UNIQUE
        );
        
        CREATE TABLE course (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100),
            description TEXT
        );
        
        CREATE TABLE enrollment (
            id SERIAL PRIMARY KEY,
            student_id INTEGER,
            course_id INTEGER,
            enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        ALTER TABLE enrollment ADD CONSTRAINT fk_student
            FOREIGN KEY (student_id) REFERENCES student(id);
        
        ALTER TABLE enrollment ADD CONSTRAINT fk_course
            FOREIGN KEY (course_id) REFERENCES course(id);
        """;

    @Test
    void testProcessSchema() {
        List<TableMetadata> tables = extractProcessor.processSchema(STUDENT_STATEMENT);
        System.out.println("Tables: " + tables);

        alterProcessor.processAlterStatements(tables, STUDENT_STATEMENT);
        tables.forEach(
            System.out::println
                      );
    }
}

