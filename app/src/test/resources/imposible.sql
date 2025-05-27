-- impossible.sql
-- Schema con casos extremos de PRIMARY KEYs en PostgreSQL
-- Este archivo contiene casos complejos y desafiantes de definiciones de claves primarias

-- ===================================================================================
-- Tabla 1: PRIMARY KEYs con múltiples definiciones y formatos mixtos
-- ===================================================================================
CREATE TABLE mixed_keys (
    id1 INTEGER     PRIMARY    KEY,
    id2 INTEGER NOT NULL CONSTRAINT pk_mixed UNIQUE PRIMARY KEY,
    id3 INTEGER 
        CONSTRAINT another_pk 
        PRIMARY 
        KEY,
    CONSTRAINT composite_pk PRIMARY KEY (id1, id2, id3)
);

-- ===================================================================================
-- Tabla 2: PRIMARY KEYs con comentarios intercalados
-- ===================================================================================
CREATE TABLE commented_keys (
    user_id SERIAL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action_type VARCHAR(50),
    CONSTRAINT
        pk_commented
        PRIMARY
        KEY
        (user_id,
         timestamp,
         action_type)
);

-- ===================================================================================
-- Tabla 3: PRIMARY KEYs con múltiples estilos de definición
-- ===================================================================================
CREATE TABLE multi_pk_styles (
    col1 INTEGER PRIMARY KEY,
    col2 INTEGER CONSTRAINT pk2 PRIMARY KEY,
    col3 INTEGER,
    col4 INTEGER,
    col5 INTEGER     PRIMARY        KEY,
    PRIMARY KEY (col3),
    CONSTRAINT pk_custom PRIMARY KEY (col4),
    CONSTRAINT "pk-special.name" PRIMARY KEY (col5)
);

-- ===================================================================================
-- Tabla 4: PRIMARY KEYs con nombres especiales y caracteres especiales
-- ===================================================================================
CREATE TABLE "complex.named_table" (
    "user.id" INTEGER,
    "timestamp.created" TIMESTAMP,
    "special-column" VARCHAR(50),
    CONSTRAINT "pk.with.dots" PRIMARY KEY ("user.id"),
    CONSTRAINT "pk-with-hyphens" PRIMARY KEY ("timestamp.created"),
    CONSTRAINT "pk_with_all.types-mixed" PRIMARY KEY ("special-column")
);

-- ===================================================================================
-- Tabla 5: PRIMARY KEYs en diferentes posiciones y con diferentes tipos
-- ===================================================================================
CREATE TABLE mixed_positions (
    id1 UUID PRIMARY KEY,
    id2 BIGINT,
    CONSTRAINT pk_mid PRIMARY KEY (id2),
    id3 DECIMAL(20,2),
    id4 VARCHAR(100),
    PRIMARY KEY (id3, id4),
    CHECK (id2 > 0),
    UNIQUE (id1, id2)
);

-- ===================================================================================
-- Tabla 6: PRIMARY KEYs con tipos de datos complejos y arrays
-- ===================================================================================
CREATE TABLE complex_types (
    id1 INTEGER[],
    id2 NUMERIC(20,5)[],
    id3 VARCHAR(100) ARRAY[3],
    id4 TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_arrays PRIMARY KEY (id1, id2[1], id3[1]),
    CONSTRAINT pk_timestamp PRIMARY KEY (id4)
);