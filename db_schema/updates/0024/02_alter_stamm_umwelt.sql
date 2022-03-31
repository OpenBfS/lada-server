ALTER TABLE stamm.umwelt
    ADD COLUMN IF NOT EXISTS leitstelle character varying(5) REFERENCES stamm.mess_stelle;