-- impossible.sql
-- Schema con casos extremos de PRIMARY KEYs en PostgreSQL
-- Este archivo contiene casos complejos y desafiantes de definiciones de claves primarias

-- ===================================================================================
-- Tabla 1: PRIMARY KEYs con múltiples definiciones y formatos mixtos
-- ===================================================================================
CREATE TABLE mixed_keys (
    id1 INTEGER     PRIMARY    KEY,                                    -- Espaciado irregular
    id2 INTEGER NOT NULL CONSTRAINT pk_mixed UNIQUE PRIMARY KEY,       -- Múltiples constraints inline
    id3 INTEGER
        CONSTRAINT another_pk
        PRIMARY
        KEY,                                                          -- PK multilínea
    CONSTRAINT composite_pk PRIMARY KEY (id1, id2, id3)               -- PK compuesta redundante
);

-- ===================================================================================
-- Tabla 2: PRIMARY KEYs con comentarios intercalados
-- ===================================================================================
CREATE TABLE commented_keys (
    /* Columna principal */
    user_id SERIAL,                                                   -- Auto-incrementing
    /* Segunda columna */
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    /* Tercera columna */
    action_type VARCHAR(50),
    /* Definición de primary key */
    CONSTRAINT
        /* Nombre del constraint */
        pk_commented
        /* Tipo de constraint */
        PRIMARY
        /* Keyword final */
        KEY
        /* Columnas */
        (user_id,
         -- Primera columna
         timestamp,
         /* Segunda columna */
         action_type)
);

-- ===================================================================================
-- Tabla 3: PRIMARY KEYs con múltiples estilos de definición
-- ===================================================================================
CREATE TABLE multi_pk_styles (
    col1 INTEGER PRIMARY KEY,                                         -- Simple inline
    col2 INTEGER CONSTRAINT pk2 PRIMARY KEY,                          -- Con constraint inline
    col3 INTEGER,
    col4 INTEGER,
    col5 INTEGER     PRIMARY        KEY,                             -- Con espaciado extraño
    PRIMARY KEY (col3),                                              -- PK sin nombre
    CONSTRAINT pk_custom PRIMARY KEY (col4),                         -- PK con nombre
    CONSTRAINT "pk-special.name" PRIMARY KEY (col5)                  -- PK con nombre especial
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
    id1 UUID PRIMARY KEY,                                            -- UUID como PK
    id2 BIGINT,
    CONSTRAINT pk_mid PRIMARY KEY (id2),                            -- PK en medio
    id3 DECIMAL(20,2),
    id4 VARCHAR(100),
    PRIMARY KEY (id3, id4),                                         -- PK al final sin nombre
    CHECK (id2 > 0),                                                -- Otros constraints
    UNIQUE (id1, id2)
);

-- ===================================================================================
-- Tabla 6: PRIMARY KEYs con tipos de datos convencionales
-- ===================================================================================
CREATE TABLE complex_types (
    id1 INTEGER NOT NULL,                                           -- Valor simple
    id2 NUMERIC(20,5) NOT NULL,                                     -- Valor con precisión
    id3 VARCHAR(100) NOT NULL,                                      -- Cadena de texto
    id4 TIMESTAMP WITH TIME ZONE NOT NULL,                          -- Timestamp con zona horaria
    CONSTRAINT pk_complex_types PRIMARY KEY (id1, id2, id3, id4)    -- Clave primaria compuesta
);
