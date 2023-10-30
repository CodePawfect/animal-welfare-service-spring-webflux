DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'animal_welfare') THEN
            CREATE DATABASE animal_welfare;
        END IF;
    END $$;