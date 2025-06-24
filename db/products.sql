CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT 'Unnamed Product',
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    category_id INTEGER REFERENCES category(id)
);

-- Agregar una columna de descripción a la tabla category
ALTER TABLE category
ADD COLUMN description TEXT;

-- Cambiar el tipo de dato de la columna name en category para permitir nombres más largos
ALTER TABLE category
ALTER COLUMN name TYPE VARCHAR(200);

-- Agregar una restricción de unicidad en el nombre de las categorías
ALTER TABLE category
ADD CONSTRAINT uq_category_name UNIQUE (name);

-- Agregar una columna created_at con valor por defecto en product
ALTER TABLE product
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Establecer que stock no pueda ser negativo
ALTER TABLE product
ADD CONSTRAINT chk_product_stock_positive CHECK (stock >= 0);

-- Renombrar la columna price a unit_price en product
ALTER TABLE product
RENAME COLUMN price TO unit_price;

-- Agregar una clave foránea explícita con nombre propio
ALTER TABLE product
ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE;

-- Eliminar la columna description de category (por si quieres ejemplos de DROP)
ALTER TABLE category
DROP COLUMN description;

-- =============================
-- ÍNDICES agregados
-- =============================

-- Índice simple sobre product.name
CREATE INDEX idx_product_name ON product(name);

-- Índice simple sobre product.unit_price
CREATE INDEX idx_product_unit_price ON product(unit_price);

-- Índice compuesto sobre product.name y stock
CREATE INDEX idx_product_name_stock ON product(name, stock);

-- Índice simple sobre product.created_at
CREATE INDEX idx_product_created_at ON product(created_at);

-- Índice en category.name (ya tiene un UNIQUE pero lo hacemos explícito como índice también)
CREATE UNIQUE INDEX idx_category_name_unique ON category(name);

-- Índice compuesto sobre product.category_id y created_at
CREATE INDEX idx_product_category_created ON product(category_id, created_at);
