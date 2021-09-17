create table if not exists collections (
    id uuid primary key,
    title text,
    description text,
    links jsonb[],
    extent jsonb,
    item_type text default 'feature',
    crs text[]
);

create table if not exists styles (
    id uuid primary key,
    style jsonb
);

create table if not exists tilesets (
    id uuid primary key,
    tileset jsonb
);
