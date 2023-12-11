CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop and create schema
DROP SCHEMA IF EXISTS baremaps CASCADE;
CREATE SCHEMA baremaps;

-- Create table baremaps.tree_type
CREATE TABLE IF NOT EXISTS baremaps.tree_type (
    id SERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL
);

-- Populate baremaps.tree_type with 10 different types of trees
INSERT INTO baremaps.tree_type (type) VALUES
    ('Oak'),
    ('Maple'),
    ('Pine'),
    ('Birch'),
    ('Spruce'),
    ('Willow'),
    ('Cherry'),
    ('Poplar'),
    ('Cypress'),
    ('Cedar');

-- Create table baremaps.point_trees
CREATE TABLE IF NOT EXISTS baremaps.point_trees (
    id SERIAL PRIMARY KEY,
    geom GEOMETRY(Point, 3857) NOT NULL,
    size INTEGER,
    tree_type_id INTEGER REFERENCES baremaps.tree_type(id)
);

-- Populate baremaps.point_trees with 1000 random points in France
INSERT INTO baremaps.point_trees (geom, size, tree_type_id)
SELECT
    ST_Transform(
      ST_SetSRID(ST_MakePoint(
        random() * (5.5 - 0.5) + 0.5,  -- Longitude range for France
        random() * (51.1 - 41.1) + 41.1  -- Latitude range for France
      ), 4326)
    ,3857),
    floor(random() * 100) + 1,  -- Random size between 1 and 100
    floor(random() * 10) + 1    -- Random tree_type_id between 1 and 10
FROM generate_series(1, 1000);  -- Number of points to generate

