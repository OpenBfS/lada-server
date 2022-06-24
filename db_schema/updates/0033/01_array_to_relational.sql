CREATE TABLE land.messprogramm_mmt_messgroesse (
    messprogramm_mmt_id integer REFERENCES land.messprogramm_mmt
        ON DELETE CASCADE,
    messgroesse_id integer REFERENCES stamm.messgroesse,
    PRIMARY KEY (messprogramm_mmt_id, messgroesse_id)
);

INSERT INTO land.messprogramm_mmt_messgroesse
    SELECT mmt.id, messgroessen.id
    FROM land.messprogramm_mmt AS mmt,
        unnest(mmt.messgroessen) AS messgroessen (id);

ALTER TABLE land.messprogramm_mmt DROP COLUMN messgroessen CASCADE;

-- TODO: Does not work for other users than "lada":
GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES
    ON land.messprogramm_mmt_messgroesse TO lada;
