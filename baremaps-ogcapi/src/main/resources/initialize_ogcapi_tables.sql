create table if not exists collections (
    id uuid primary key,
    collection jsonb
);

create table if not exists styles (
    id uuid primary key,
    style jsonb
);

create table if not exists tilesets (
    id uuid primary key,
    tileset jsonb
);
