DROP SEQUENCE IF EXISTS SEQ_MENUS;
DROP SEQUENCE IF EXISTS SEQ_PLATOS;
DROP SEQUENCE IF EXISTS SEQ_INGREDIENTES;

DROP TABLE IF EXISTS MenuPlato;
DROP TABLE IF EXISTS PlatoIngrediente;
DROP TABLE IF EXISTS Ingrediente;
DROP TABLE IF EXISTS Plato;
DROP TABLE IF EXISTS Menu;

CREATE SEQUENCE SEQ_MENUS
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999;

CREATE SEQUENCE SEQ_PLATOS
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999;

CREATE SEQUENCE SEQ_INGREDIENTES
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9999;

CREATE TABLE Menu (
    id VARCHAR(15),
    nombre VARCHAR(100),
    precio NUMERIC(5, 2),
    desde DATE,
    hasta DATE,
    CONSTRAINT "PK_MENU" PRIMARY KEY (id),
    CONSTRAINT "NN_MENU.NOMBRE" CHECK ( nombre IS NOT NULL ),
    CONSTRAINT "NN_MENU.PRECIO" CHECK ( precio IS NOT NULL),
    CONSTRAINT "NN_MENU.DESDE" CHECK ( desde IS NOT NULL ),
    CONSTRAINT "NN_MENU.HASTA" CHECK ( hasta IS NOT NULL ),
    CONSTRAINT "CK_MENU.PRECIO_GT0" CHECK (precio > 0),
    CONSTRAINT "CK_MENU.FECHAS" CHECK (desde < hasta)
);

CREATE TABLE Plato (
    id VARCHAR(15),
    nombre VARCHAR(150),
    descripcion VARCHAR(450),
    precio NUMERIC(5, 2),
    tipo VARCHAR(20),
    CONSTRAINT "PK_PLATO" PRIMARY KEY (id),
    CONSTRAINT "NN_PLATO.NOMBRE" CHECK ( nombre IS NOT NULL ),
    CONSTRAINT "UK_PLATO.NOMBRE" UNIQUE(nombre),
    CONSTRAINT "NN_PLATO.DESCRIPCION" CHECK ( descripcion IS NOT NULL ),
    CONSTRAINT "NN_PLATO.PRECIO" CHECK ( precio IS NOT NULL ),
    CONSTRAINT "CK_PLATO.PRECIO_GT0" CHECK (precio > 0),
    CONSTRAINT "CH_PLATO.TIPO" CHECK (tipo IN ('ENTRANTE', 'PRINCIPAL', 'POSTRE', 'INFANTIL'))
);

CREATE TABLE Ingrediente (
    id VARCHAR(15),
    nombre VARCHAR(150),
    CONSTRAINT "PK_INGREDIENTE" PRIMARY KEY (id),
    CONSTRAINT "NN_INGREDIENTE.NOMBRE" CHECK ( nombre IS NOT NULL ),
    CONSTRAINT "UK_INGREDIENTE.NOMBRE" UNIQUE(nombre)
);

CREATE TABLE PlatoIngrediente (
    plato_id VARCHAR(15),
    ingrediente_id VARCHAR(15),
    cantidad INT,
    unidad_medida VARCHAR(20),

    CONSTRAINT "PK_PLATOINGREDIENTE" PRIMARY KEY (plato_id, ingrediente_id),

    CONSTRAINT "FK_PLATOINGREDIENTE.PLATO" FOREIGN KEY (plato_id)
      REFERENCES Plato(id)
      ON DELETE CASCADE,
    CONSTRAINT "FK_PLATOINGREDIENTE.INGREDIENTE" FOREIGN KEY (ingrediente_id)
      REFERENCES Ingrediente(id)
      ON DELETE CASCADE,

    CONSTRAINT "NN_PLATOINGREDIENTE.CANTIDAD" CHECK ( cantidad IS NOT NULL ),
    CONSTRAINT "CH_PLATOINGREDIENTE.CANTIDAD" CHECK (cantidad BETWEEN 1 AND 9999),
    CONSTRAINT "CH_PLATOINGREDIENTE.UNIDAD" CHECK (unidad_medida IN ('GRAMOS', 'UNIDADES', 'CENTILITROS')),
    CONSTRAINT "NN_PLATOINGREDIENTE.UNIDAD" CHECK ( unidad_medida IS NOT NULL )
);

CREATE TABLE MenuPlato (
    menu_id VARCHAR(15),
    plato_id VARCHAR(15),
    CONSTRAINT "PK_MENUPLATO" PRIMARY KEY (menu_id, plato_id),

    CONSTRAINT "FK_MENUPLATO.MENU" FOREIGN KEY (menu_id)
        REFERENCES Menu(id)
        ON DELETE CASCADE,
    CONSTRAINT "FK_MENUPLATO.PLATO" FOREIGN KEY (plato_id)
        REFERENCES Plato(id)
        ON DELETE CASCADE
);
INSERT INTO Menu(id,nombre,precio,desde,hasta)
VALUES(nextval('seq_menus'), 'David', 132.3, '2025-01-18', '2025-10-24');

INSERT INTO Menu(id,nombre,precio,desde,hasta)
VALUES(nextval('seq_menus'), 'Claudia', 224.2, '2025-01-16', '2025-10-25');

INSERT INTO Plato(id,nombre,descripcion,precio,tipo)
VALUES(nextval('seq_platos'), 'Macarrones boloñesa', 'Macarrones a la boloñesa', 12.5, 'PRINCIPAL');

INSERT INTO Plato(id,nombre,descripcion,precio,tipo)
VALUES(nextval('seq_platos'), 'Atun con tomate', 'Atun plato completo', 15.5, 'ENTRANTE');

INSERT INTO Ingrediente(id,nombre)
VALUES(nextval('seq_ingredientes'), 'Pasta');

INSERT INTO PlatoIngrediente(plato_id, ingrediente_id, cantidad, unidad_medida)
VALUES
    ('1', '1', 200, 'GRAMOS'), -- Macarrones con 200 gramos de Pasta
    ('2', '1', 150, 'CENTILITROS'); -- Atun con 150 centilitros de Pasta

INSERT INTO MenuPlato(menu_id, plato_id)
VALUES
    ('1', '1'), -- Menu David y Macarrones
    ('1', '2'), -- Menu David y Atun
    ('2', '2'); -- Menu Claudia y Atun