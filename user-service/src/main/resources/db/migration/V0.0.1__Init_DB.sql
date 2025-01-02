CREATE TABLE IF NOT EXISTS public."user"
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "name"     VARCHAR NOT NULL,
    avatar_url VARCHAR,
    email      VARCHAR NOT NULL
)