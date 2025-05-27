CREATE TABLE estudiante(
    id_estudiante SERIAL,
    rut_estudiante VARCHAR(20),
    nombre_estudiante VARCHAR(100),
    fecha_nacimiento DATE,
    direccion VARCHAR(200),
    correo VARCHAR(100),
    telefono VARCHAR(20),
    CONSTRAINT pk_estudiantes PRIMARY KEY (id_estudiante),
    CONSTRAINT unique_rut UNIQUE (rut_estudiante)
);

CREATE TABLE programa(
    id_programa SERIAL,
    nombre_programa VARCHAR(100),
    cantidad_horas INTEGER,
    CONSTRAINT pk_programas PRIMARY KEY (id_programa)
);

CREATE TABLE modulo(
    id_modulo SERIAL,
    nombre_modulo VARCHAR(100),
    cantidad_horas INTEGER,
    CONSTRAINT pk_modulos PRIMARY KEY (id_modulo)
);

CREATE TABLE relator(
    id_relator SERIAL,
    rut_relator VARCHAR(20),
    nombre_relator VARCHAR(100),
    titulo_relator VARCHAR(100),
    anios_experiencia INTEGER,
    valor_hora INTEGER,
    CONSTRAINT pk_relatores PRIMARY KEY (id_relator),
    CONSTRAINT unique_rut_relator UNIQUE (rut_relator)
);

CREATE TABLE curso(
    id_curso SERIAL,
    codigo_curso VARCHAR(20),
    cantidad_estudiantes INTEGER,
    fecha_inicio DATE,
    fecha_termino DATE,
    id_programa INTEGER,
    CONSTRAINT pk_cursos PRIMARY KEY (id_curso),
    CONSTRAINT unique_codigo_curso UNIQUE (codigo_curso),
    CONSTRAINT fk_cursos_programas FOREIGN KEY (id_programa) REFERENCES programa(id_programa) ON DELETE RESTRICT
);

CREATE TABLE cursos_estudiante(
    id_cursos_estudiantes SERIAL,
    id_estudiante INTEGER,
    id_curso INTEGER,
    CONSTRAINT pk_cursos_estudiantes PRIMARY KEY (id_cursos_estudiantes),
    CONSTRAINT fk_cursos_estudiantes_estudiantes FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante) ON DELETE RESTRICT,
    CONSTRAINT fk_cursos_estudiantes_cursos FOREIGN KEY (id_curso) REFERENCES curso(id_curso) ON DELETE RESTRICT
);

CREATE TABLE cursos_relatore(
    id_cursos_relatores SERIAL,
    id_curso INTEGER,
    id_relator INTEGER,
    CONSTRAINT pk_cursos_relatores PRIMARY KEY (id_cursos_relatores),
    CONSTRAINT fk_cursos_relatores_relatores FOREIGN KEY (id_relator) REFERENCES relator(id_relator) ON DELETE RESTRICT,
    CONSTRAINT fk_cursos_relatores_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso) ON DELETE RESTRICT
);

CREATE TABLE modulos_programa(
    id_modulos_programas SERIAL,
    id_programa INTEGER,
    id_modulo INTEGER,
    CONSTRAINT pk_modulos_programas PRIMARY KEY (id_modulos_programas),
    CONSTRAINT fk_modulos_programas_programas FOREIGN KEY (id_programa) REFERENCES programa(id_programa) ON DELETE RESTRICT,
    CONSTRAINT fk_modulos_programas_modulos FOREIGN KEY (id_modulo) REFERENCES modulo(id_modulo) ON DELETE RESTRICT
);

CREATE TABLE modulos_curso(
    id_modulos_cursos SERIAL,
    id_curso INTEGER,
    id_modulo INTEGER,
    CONSTRAINT pk_modulos_cursos PRIMARY KEY (id_modulos_cursos),
    CONSTRAINT fk_modulos_cursos_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso) ON DELETE RESTRICT,
    CONSTRAINT fk_modulos_cursos_modulo FOREIGN KEY (id_modulo) REFERENCES modulo(id_modulo) ON DELETE RESTRICT
);