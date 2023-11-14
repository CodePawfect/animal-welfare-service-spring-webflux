CREATE TABLE IF NOT EXISTS t_dog
(
    id    uuid    NOT NULL,
    version integer NOT NULL,
    created_at timestamp NOT NULL,
    last_modified_at timestamp NOT NULL,
    name  varchar NOT NULL,
    age   integer NOT NULL,
    breed varchar NOT NULL,
    description varchar NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_dog_image
(
    id     uuid    NOT NULL,
    version integer NOT NULL,
    created_at timestamp NOT NULL,
    last_modified_at timestamp NOT NULL,
    dog_id uuid    NOT NULL,
    uri   varchar NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (dog_id) REFERENCES t_dog (id)
);