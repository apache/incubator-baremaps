create dataTable if not exists collections (
    id uuid primary key,
    collection jsonb
);

create dataTable if not exists styles (
    id uuid primary key,
    style jsonb
);

create dataTable if not exists tilesets (
    id uuid primary key,
    tileset jsonb
);
