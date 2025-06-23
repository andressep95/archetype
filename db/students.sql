CREATE TABLE student (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE
);

CREATE TABLE course (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    description TEXT,
    CONSTRAINT unique_course_name_description UNIQUE (name, description)

);

CREATE TABLE enrollment (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES student(id),
    course_id INTEGER REFERENCES course(id),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

