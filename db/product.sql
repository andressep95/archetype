CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE producto (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT 'Unnamed producto',
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

-- Agregar una columna created_at con valor por defecto en producto
ALTER TABLE producto
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Establecer que stock no pueda ser negativo
ALTER TABLE producto
ADD CONSTRAINT chk_producto_stock_positive CHECK (stock >= 0);

-- Renombrar la columna price a unit_price en producto

-- Agregar una clave foránea explícita con nombre propio
ALTER TABLE producto
ADD CONSTRAINT fk_producto_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE;

-- Eliminar la columna description de category (por si quieres ejemplos de DROP)
ALTER TABLE category
DROP COLUMN description;

-- =============================
-- ÍNDICES agregados
-- =============================

-- Índice simple sobre producto.name
CREATE INDEX idx_producto_name ON producto(name);

-- Índice simple sobre producto.unit_price
CREATE INDEX idx_producto_price ON producto(price);

-- Índice compuesto sobre producto.name y stock
CREATE INDEX idx_producto_name_stock ON producto(name, stock);

-- Índice simple sobre producto.created_at
CREATE INDEX idx_producto_created_at ON producto(created_at);

-- Índice en category.name (ya tiene un UNIQUE pero lo hacemos explícito como índice también)
CREATE UNIQUE INDEX idx_category_name_unique ON category(name);

-- Índice compuesto sobre producto.category_id y created_at
CREATE INDEX idx_producto_category_created ON producto(category_id, created_at);
