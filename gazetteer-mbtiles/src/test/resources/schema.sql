-- mbtiles schema

BEGIN;

CREATE TABLE indexdata (name text, value text);
CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob);
CREATE TABLE grids (zoom_level integer, tile_column integer, tile_row integer, grid blob);
CREATE TABLE grid_data (zoom_level integer, tile_column integer, tile_row integer, key_name text, key_json text);

COMMIT;