ALTER TABLE stamm.umwelt
    ADD COLUMN leitstelle character varying(5) REFERENCES stamm.mess_stelle;