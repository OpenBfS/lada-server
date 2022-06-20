CREATE TABLE land.messprogramm_proben_zusatz (
    proben_zusatz_id character varying(3) REFERENCES stamm.proben_zusatz,
    messprogramm_id INTEGER REFERENCES land.messprogramm ON DELETE CASCADE,
    PRIMARY KEY (proben_zusatz_id, messprogramm_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES
    ON land.messprogramm_proben_zusatz TO lada;
