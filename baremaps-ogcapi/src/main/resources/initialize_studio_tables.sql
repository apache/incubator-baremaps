create schema studio;

create table if not exists studio.entities (
    id uuid primary key,
    entity jsonb,
    kind text
);