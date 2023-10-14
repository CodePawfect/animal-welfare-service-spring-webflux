CREATE TABLE IF NOT EXISTS t_dog
(
    id    uuid    NOT NULL,
    version integer NOT NULL,
    created_at timestamp NOT NULL,
    last_modified_at timestamp NOT NULL,
    name  varchar NOT NULL,
    age   integer NOT NULL,
    breed varchar NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_dog_image
(
    id     uuid    NOT NULL,
    dog_id uuid    NOT NULL,
    url    varchar NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (dog_id) REFERENCES t_dog (id)
);

CREATE TABLE IF NOT EXISTS t_app_user
(
    id       uuid    NOT NULL,
    username varchar NOT NULL,
    password varchar NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_role
(
    id   uuid    NOT NULL,
    name varchar NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS t_user_roles
(
    id      uuid NOT NULL,
    user_id uuid,
    role_id uuid,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES t_app_user (id),
    FOREIGN KEY (role_id) REFERENCES t_role (id)
);