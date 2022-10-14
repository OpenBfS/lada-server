ALTER TABLE IF EXISTS stamm.pflicht_messgroesse
    ALTER COLUMN mmt_id SET NOT NULL;

ALTER TABLE IF EXISTS stamm.pflicht_messgroesse
    ALTER COLUMN umw_id SET NOT NULL;

ALTER TABLE IF EXISTS stamm.pflicht_messgroesse
    ADD CONSTRAINT pflicht_messgroesse_unique UNIQUE (messgroesse_id, mmt_id, umw_id, datenbasis_id);