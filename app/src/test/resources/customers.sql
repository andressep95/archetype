CREATE TABLE customer (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE
);

CREATE TABLE order_table (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customer(id),
    order_date DATE NOT NULL
);

CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES order_table(id),
    amount DECIMAL(10,2),
    payment_date DATE
);

ALTER TABLE customer
ADD CONSTRAINT unique_name UNIQUE (name);
