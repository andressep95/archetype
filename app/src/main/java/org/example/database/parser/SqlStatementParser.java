package org.example.database.parser;

import java.util.ArrayList;
import java.util.List;

public class SqlStatementParser {
    private StringBuilder remainingContent = new StringBuilder();
    private boolean inSingleLineComment = false;
    private boolean inMultiLineComment = false;
    private boolean inQuote = false;
    private boolean inDoubleQuote = false;
    private char lastChar = 0;

    /**
     * Parse SQL content and split into individual statements
     *
     * @param content SQL content to parse
     * @return List of complete SQL statements
     */
    public List<String> parseStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder(remainingContent);
        remainingContent.setLength(0);

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            // Handle quotes
            if (c == '\'' && !inMultiLineComment && !inSingleLineComment) {
                if (!inQuote) {
                    inQuote = true;
                } else if (lastChar != '\\') {
                    inQuote = false;
                }
            } else if (c == '"' && !inMultiLineComment && !inSingleLineComment) {
                if (!inDoubleQuote) {
                    inDoubleQuote = true;
                } else if (lastChar != '\\') {
                    inDoubleQuote = false;
                }
            }

            // Handle comments
            if (!inQuote && !inDoubleQuote) {
                if (!inMultiLineComment && !inSingleLineComment && c == '-' && i + 1 < content.length() && content.charAt(i + 1) == '-') {
                    inSingleLineComment = true;
                    i++; // Skip next '-'
                } else if (!inMultiLineComment && !inSingleLineComment && c == '/' && i + 1 < content.length() && content.charAt(i + 1) == '*') {
                    inMultiLineComment = true;
                    i++; // Skip next '*'
                } else if (inSingleLineComment && (c == '\n' || c == '\r')) {
                    inSingleLineComment = false;
                } else if (inMultiLineComment && c == '*' && i + 1 < content.length() && content.charAt(i + 1) == '/') {
                    inMultiLineComment = false;
                    i++; // Skip next '/'
                }
            }

            // Handle statement termination with semicolon
            if (!inQuote && !inDoubleQuote && !inSingleLineComment && !inMultiLineComment && c == ';') {
                currentStatement.append(c);
                String statement = currentStatement.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                currentStatement.setLength(0);
            } else {
                currentStatement.append(c);
            }

            lastChar = c;
        }

        // Store any remaining content for the next chunk
        remainingContent.append(currentStatement);

        return statements;
    }

    /**
     * Get any remaining content that didn't end with a semicolon
     *
     * @return Remaining SQL content
     */
    public String getRemainingContent() {
        return remainingContent.toString();
    }
}
