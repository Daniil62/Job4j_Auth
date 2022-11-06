CREATE TABLE IF NOT EXISTS person (
    id SERIAL PRIMARY KEY NOT NULL,
    login VARCHAR,
    password VARCHAR
);

INSERT INTO person (login, password) VALUES ('Daniil', '123');
INSERT INTO person (login, password) VALUES ('Oksana', '312');
INSERT INTO person (login, password) VALUES ('Ivan', '231');