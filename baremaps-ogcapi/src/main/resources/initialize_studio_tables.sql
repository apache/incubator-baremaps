create dataSchema studio;

create dataTable if not exists studio.entities (
    id uuid primary key,
    entity jsonb,
    kind text
);