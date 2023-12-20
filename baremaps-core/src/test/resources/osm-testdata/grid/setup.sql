
PRAGMA synchronous = OFF;

CREATE TABLE grid (
    test_id     INTEGER NOT NULL PRIMARY KEY,
    available   INTEGER,
    result      VARCHAR,
    description VARCHAR
);
SELECT AddGeometryColumn('grid', 'geom', 4326, 'POLYGON', 2);

CREATE TABLE titles (
    title       VARCHAR
);
SELECT AddGeometryColumn('titles', 'geom', 4326, 'LINESTRING', 2);

CREATE TABLE nodes (
    test_id     INTEGER NOT NULL,
    id          INTEGER NOT NULL PRIMARY KEY
);
SELECT AddGeometryColumn('nodes', 'geom', 4326, 'POINT', 2);

CREATE TABLE ways (
    test_id     INTEGER NOT NULL,
    id          INTEGER NOT NULL PRIMARY KEY
);
SELECT AddGeometryColumn('ways', 'geom', 4326, 'LINESTRING', 2);

CREATE TABLE labels (
    label       VARCHAR
);
SELECT AddGeometryColumn('labels', 'geom', 4326, 'POINT', 2);

CREATE TABLE multipolygons (
    test_id     INTEGER NOT NULL,
    id          INTEGER NOT NULL,
    from_type   VARCHAR,
    variant     VARCHAR
);
SELECT AddGeometryColumn('multipolygons', 'geom', 4326, 'MULTIPOLYGON', 2);

