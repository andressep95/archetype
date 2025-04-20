package org.example.database;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SqlFileContent {

    private final String filePath;
    private final Charset encoding;
    private final List<String> sqlStatements;

    public SqlFileContent(String filePath, Charset encoding) {
        this.filePath = filePath;
        this.encoding = encoding;
        this.sqlStatements = new ArrayList<>();
    }

    public synchronized void addSqlStatement(String statement) {
        sqlStatements.add(statement);
    }

    public String getFilePath() {
        return filePath;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public List<String> getSqlStatements() {
        return new ArrayList<>(sqlStatements);
    }
}

