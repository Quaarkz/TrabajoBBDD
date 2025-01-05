
DROP SEQUENCE IF EXISTS SEQ_MENUS CASCADE;
DROP SEQUENCE IF EXISTS SEQ_PLATOS CASCADE;
DROP SEQUENCE IF EXISTS SEQ_INGREDIENTES CASCADE;

DROP TABLE IF EXISTS MenuPlato CASCADE;
DROP TABLE IF EXISTS PlatoIngrediente CASCADE;
DROP TABLE IF EXISTS Ingredientes CASCADE;
DROP TABLE IF EXISTS Platos CASCADE;
DROP TABLE IF EXISTS Menus CASCADE;

CREATE SEQUENCE SEQ_MENUS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PLATOS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_INGREDIENTES START WITH 1 INCREMENT BY 1;

CREATE TABLE Menus (
                       id INT PRIMARY KEY DEFAULT nextval('SEQ_MENUS'),
                       nombre VARCHAR(100) NOT NULL,
                       precio NUMERIC(5, 2) NOT NULL CHECK (precio > 0),
                       desde DATE NOT NULL,
                       hasta DATE NOT NULL,
                       CHECK (desde <= hasta)
);

CREATE TABLE Platos (
                        id INT PRIMARY KEY DEFAULT nextval('SEQ_PLATOS'),
                        nombre VARCHAR(150) NOT NULL UNIQUE,
                        descripcion VARCHAR(450) NOT NULL,
                        precio NUMERIC(5, 2) NOT NULL CHECK (precio > 0),
                        tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRANTE', 'PRINCIPAL', 'POSTRE', 'INFANTIL'))
);

CREATE TABLE Ingredientes (
                              id INT PRIMARY KEY DEFAULT nextval('SEQ_INGREDIENTES'),
                              nombre VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE PlatoIngrediente (
                                  plato_id INT NOT NULL REFERENCES Platos(id) ON DELETE CASCADE,
                                  ingrediente_id INT NOT NULL REFERENCES Ingredientes(id) ON DELETE CASCADE,
                                  cantidad INT NOT NULL CHECK (cantidad > 0 AND cantidad <= 9999),
                                  unidad_medida VARCHAR(20) NOT NULL CHECK (unidad_medida IN ('GRAMOS', 'UNIDADES', 'CENTILITROS')),
                                  PRIMARY KEY (plato_id, ingrediente_id)
);

CREATE TABLE MenuPlato (
                           menu_id INT NOT NULL REFERENCES Menus(id) ON DELETE CASCADE,
                           plato_id INT NOT NULL REFERENCES Platos(id) ON DELETE CASCADE,
                           PRIMARY KEY (menu_id, plato_id)
);

CREATE INDEX idx_menu_nombre ON Menus(nombre);
CREATE INDEX idx_plato_nombre ON Platos(nombre);
CREATE INDEX idx_ingrediente_nombre ON Ingredientes(nombre);


