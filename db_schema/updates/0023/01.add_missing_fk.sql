ALTER TABLE stamm.pflicht_messgroesse
    ALTER COLUMN messgroesse_id SET NOT NULL,
    ADD FOREIGN KEY (messgroesse_id) REFERENCES stamm.messgroesse;
