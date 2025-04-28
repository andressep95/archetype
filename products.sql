CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
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
