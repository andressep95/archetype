package org.example.database.extractor.index;

import java.util.List;

public interface SqlCreateIndexStatementExtractor {

    List<String> extractCreateIndexStatements(String sql);
    String extractTableName(String indexStatement);
    List<String> extractTargetColumnNames(String indexStatement);
    String extractIndexName(String indexStatement);

}

/*
| Tipo de índice           | Sentencia SQL                                                                 |
|--------------------------|------------------------------------------------------------------------------|
| Índice simple            | `CREATE INDEX idx_users_email ON users (email);`                            |
| Índice único             | `CREATE UNIQUE INDEX idx_users_username ON users (username);`               |
| Índice multicolumna      | `CREATE INDEX idx_orders_customer_date ON orders (customer_id, order_date DESC);` |
| Índice con expresión     | `CREATE INDEX idx_lower_email ON users (LOWER(email));`                      |
| Índice en esquema        | `CREATE INDEX idx_schema_table_column ON my_schema.my_table (my_column);`   |
| Índice con tipo GIN      | `CREATE INDEX idx_documents_tags ON documents USING GIN (tags);`            |
| Índice con tipo GiST     | `CREATE INDEX idx_geo_location ON places USING GiST (location);`            |
| Índice parcial           | `CREATE INDEX idx_active_users ON users (last_login) WHERE active = true;`  |
*/