SET search_path = land, public;
-- View for messprogramm audit trail
CREATE OR REPLACE VIEW audit_trail_messprogramm AS
SELECT audit_trail.id,
    audit_trail.table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
    audit_trail.row_data,
    audit_trail.changed_fields,
    cast(row_data ->> 'messprogramm_id' AS int) AS mp_id
FROM land.audit_trail;

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES
    ON audit_trail_messprogramm TO lada;

SELECT stamm.audit_table('messprogramm', true, false, '{id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('messprogramm_mmt', true, false, '{id, messprogramm_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('messprogramm_mmt_messgroesse', true, false, '{id, messprogramm_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('messprogramm_proben_zusatz', true, false, '{id, messprogramm_id, tree_modified, letzte_aenderung}'::text[]);
SELECT stamm.audit_table('ortszuordnung_mp', true, false, '{id, messprogramm_id, tree_modified, letzte_aenderung}'::text[]);

-- Add serial ids to messprogramm_mmt_messgroesse and messprogramm_proben_zusatz
ALTER TABLE messprogramm_mmt_messgroesse ADD COLUMN id SERIAL;
ALTER TABLE messprogramm_mmt_messgroesse DROP CONSTRAINT messprogramm_mmt_messgroesse_pkey;
ALTER TABLE messprogramm_mmt_messgroesse ADD PRIMARY KEY (id);
ALTER TABLE messprogramm_mmt_messgroesse ADD CONSTRAINT messprogramm_mmt_messgroesse_mmt_messgroesse_unique UNIQUE (messprogramm_mmt_id, messgroesse_id);
ALTER TABLE messprogramm_mmt_messgroesse ALTER COLUMN messprogramm_mmt_id SET NOT NULL;
ALTER TABLE messprogramm_mmt_messgroesse ALTER COLUMN messgroesse_id SET NOT NULL;
GRANT USAGE, SELECT ON SEQUENCE messprogramm_mmt_messgroesse_id_seq TO lada;

ALTER TABLE messprogramm_proben_zusatz ADD COLUMN id SERIAL;
ALTER TABLE messprogramm_proben_zusatz DROP CONSTRAINT messprogramm_proben_zusatz_pkey;
ALTER TABLE messprogramm_proben_zusatz ADD PRIMARY KEY(id);
ALTER TABLE messprogramm_proben_zusatz ADD CONSTRAINT messprogramm_proben_zusatz_messprogramm_probe_zusatz_unique UNIQUE(proben_zusatz_id, messprogramm_id);
ALTER TABLE messprogramm_proben_zusatz ALTER COLUMN proben_zusatz_id SET NOT NULL;
ALTER TABLE messprogramm_proben_zusatz ALTER COLUMN messprogramm_id SET NOT NULL;
GRANT USAGE, SELECT ON SEQUENCE messprogramm_proben_zusatz_id_seq TO lada;