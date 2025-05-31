package org.example.database.extractor.index.postgres;

import org.junit.jupiter.api.Test;

import java.util.List;

class PostgresSqlCreateIndexStatementExtractorTest {

    private final PostgresSqlCreateIndexStatementExtractor extractor = new PostgresSqlCreateIndexStatementExtractor();

    private static final String SQL_TEST =
        """
            -- ============================================
            -- CREACIÓN DE TABLAS
            -- ============================================
            
            -- Tabla de usuarios
            CREATE TABLE users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                birth_date DATE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_active BOOLEAN DEFAULT true,
                profile_image_url TEXT,
                phone_number VARCHAR(20)
            );
            
            -- Tabla de productos
            CREATE TABLE products (
                id SERIAL PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                description TEXT,
                price DECIMAL(10,2) NOT NULL,
                category_id INTEGER,
                sku VARCHAR(50) UNIQUE,
                stock_quantity INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_available BOOLEAN DEFAULT true,
                weight DECIMAL(8,3),
                dimensions VARCHAR(50)
            );
            
            -- Tabla de órdenes
            CREATE TABLE orders (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL,
                order_number VARCHAR(50) UNIQUE NOT NULL,
                total_amount DECIMAL(12,2) NOT NULL,
                status VARCHAR(20) DEFAULT 'pending',
                shipping_address TEXT,
                billing_address TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                shipped_at TIMESTAMP,
                delivered_at TIMESTAMP,
                payment_method VARCHAR(50),
                notes TEXT
            );
            
            -- Tabla de artículos de orden
            CREATE TABLE order_items (
                id SERIAL PRIMARY KEY,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                unit_price DECIMAL(10,2) NOT NULL,
                total_price DECIMAL(10,2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            -- ============================================
            -- ALTERACIONES DE TABLAS
            -- ============================================
            
            -- Agregar columnas
            ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
            ALTER TABLE users ADD COLUMN login_attempts INTEGER DEFAULT 0;
            ALTER TABLE products ADD COLUMN brand VARCHAR(100);
            ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10,2) DEFAULT 0.00;
            
            -- Modificar columnas
            ALTER TABLE users ALTER COLUMN email SET NOT NULL;
            ALTER TABLE products ALTER COLUMN description SET DEFAULT 'Sin descripción';
            
            -- Agregar restricciones
            ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id);
            ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id);
            ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id);
            
            -- ============================================
            -- ÍNDICES SIMPLES
            -- ============================================
            
            -- Índices básicos en columnas individuales
            CREATE INDEX idx_users_email ON users (email);
            CREATE INDEX idx_users_username ON users (username);
            CREATE INDEX idx_users_created_at ON users (created_at);
            CREATE INDEX idx_products_name ON products (name);
            CREATE INDEX idx_products_price ON products (price);
            CREATE INDEX idx_products_category ON products (category_id);
            CREATE INDEX idx_orders_user_id ON orders (user_id);
            CREATE INDEX idx_orders_status ON orders (status);
            CREATE INDEX idx_orders_created_at ON orders (created_at);
            
            -- ============================================
            -- ÍNDICES ÚNICOS
            -- ============================================
            
            -- Índices únicos
            CREATE UNIQUE INDEX idx_users_email_unique ON users (email);
            CREATE UNIQUE INDEX idx_products_sku_unique ON products (sku);
            CREATE UNIQUE INDEX idx_orders_number_unique ON orders (order_number);
            
            -- ============================================
            -- ÍNDICES CON IF NOT EXISTS
            -- ============================================
            
            -- Índices con verificación de existencia
            CREATE INDEX IF NOT EXISTS idx_users_last_name ON users (last_name);
            CREATE INDEX IF NOT EXISTS idx_products_stock ON products (stock_quantity);
            CREATE INDEX IF NOT EXISTS idx_orders_shipped_at ON orders (shipped_at);
            
            -- ============================================
            -- ÍNDICES COMPUESTOS (MÚLTIPLES COLUMNAS)
            -- ============================================
            
            -- Índices en múltiples columnas
            CREATE INDEX idx_users_name_composite ON users (first_name, last_name);
            CREATE INDEX idx_products_category_price ON products (category_id, price);
            CREATE INDEX idx_orders_user_status ON orders (user_id, status);
            CREATE INDEX idx_orders_date_status ON orders (created_at, status);
            CREATE INDEX idx_order_items_composite ON order_items (order_id, product_id);
            
            -- ============================================
            -- ÍNDICES CON ORDENAMIENTO
            -- ============================================
            
            -- Índices con especificación de orden
            CREATE INDEX idx_users_created_desc ON users (created_at DESC);
            CREATE INDEX idx_products_price_desc ON products (price DESC);
            CREATE INDEX idx_orders_total_desc ON orders (total_amount DESC);
            CREATE INDEX idx_users_name_asc ON users (last_name ASC, first_name ASC);
            
            -- ============================================
            -- ÍNDICES PARCIALES (CON WHERE)
            -- ============================================
            
            -- Índices parciales con condiciones
            CREATE INDEX idx_users_active_email ON users (email) WHERE is_active = true;
            CREATE INDEX idx_products_available ON products (name) WHERE is_available = true;
            CREATE INDEX idx_orders_pending ON orders (created_at) WHERE status = 'pending';
            CREATE INDEX idx_products_in_stock ON products (name, price) WHERE stock_quantity > 0;
            
            -- ============================================
            -- ÍNDICES CON FUNCIONES
            -- ============================================
            
            -- Índices con funciones
            CREATE INDEX idx_users_email_lower ON users (LOWER(email));
            CREATE INDEX idx_users_full_name_lower ON users (LOWER(first_name || ' ' || last_name));
            CREATE INDEX idx_products_name_upper ON products (UPPER(name));
            CREATE INDEX idx_orders_year ON orders (EXTRACT(YEAR FROM created_at));
            
            -- ============================================
            -- ÍNDICES ÚNICOS COMPUESTOS
            -- ============================================
            
            -- Índices únicos en múltiples columnas
            CREATE UNIQUE INDEX idx_order_items_unique ON order_items (order_id, product_id);
            CREATE UNIQUE INDEX idx_users_phone_active ON users (phone_number) WHERE is_active = true;
            
            -- ============================================
            -- ÍNDICES CON NOMBRES LARGOS Y CARACTERES ESPECIALES
            -- ============================================
            
            -- Índices con nombres más complejos
            CREATE INDEX idx_users_profile_data_search ON users (first_name, last_name, email) WHERE is_active = true;
            CREATE INDEX idx_products_full_text_search ON products (name, description) WHERE is_available = true;
            
            -- ============================================
            -- COMENTARIOS Y SENTENCIAS MEZCLADAS
            -- ============================================
            
            /* Este es un comentario de bloque */
            CREATE INDEX idx_test_comment ON users (username); -- Comentario de línea
            
            -- Más índices después de comentarios
            CREATE INDEX idx_users_login_data ON users (last_login DESC, login_attempts);
            
            -- Sentencias en una sola línea
            CREATE INDEX idx_single_line ON products (brand); CREATE INDEX idx_another_single ON orders (payment_method);
            
            -- ============================================
            -- ÍNDICES HASH (PostgreSQL específico)
            -- ============================================
            
            -- Índices con método específico
            CREATE INDEX idx_users_id_hash ON users USING hash (id);
            CREATE INDEX idx_products_sku_hash ON products USING hash (sku);
            
            -- ============================================
            -- FINAL DEL SCRIPT
            -- ============================================
        """;

    @Test
    void testExtractCreateIndexStatements() {
        List<String> statements = extractor.extractCreateIndexStatements(SQL_TEST);
        statements.forEach(System.out::println);
    }

    @Test
    void testExtractTableNameFromIndexStatement() {
        List<String> statements = extractor.extractCreateIndexStatements(SQL_TEST);
        statements.forEach(statement -> {
            String tableName = extractor.extractTableName(statement);
            System.out.println("Statement: " + statement);
            System.out.println("Table Name: " + tableName);
        });
    }

    @Test
    void testExtractColumnNameFromIndexStatement() {
        List<String> statements = extractor.extractCreateIndexStatements(SQL_TEST);
        statements.forEach(statement -> {
            System.out.println("Statement: " + statement);
            List<String> columnNames = extractor.extractTargetColumnNames(statement);
            System.out.println("Column Names: " + columnNames);
        });
    }

    @Test
    void testExtractIndexNameFromIndexStatement() {
        List<String> statements = extractor.extractCreateIndexStatements(SQL_TEST);
        statements.forEach(statement -> {
            System.out.println("Statement: " + statement);
            String indexName = extractor.extractIndexName(statement);
            System.out.println("Index Names: " + indexName);
        });
    }
}