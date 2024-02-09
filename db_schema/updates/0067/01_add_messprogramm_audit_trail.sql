SET search_path = lada, public;
-- View for messprogramm audit trail
-- View for mpg audit trail
CREATE OR REPLACE VIEW audit_trail_mpg_view AS
SELECT audit_trail.id,
    audit_trail.table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
    audit_trail.row_data,
    audit_trail.changed_fields,
    coalesce(cast(row_data ->> 'mpg_id' AS integer),
        (SELECT mpg_id FROM lada.mpg_mmt_mp WHERE id = cast(
            row_data ->> 'mpg_mmt_mp_id' AS integer))) AS mpg_id
FROM audit_trail;

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES
    ON audit_trail_mpg_view TO lada;

SELECT master.audit_table('mpg', true, false, '{id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('mpg_mmt_mp', true, false, '{id, mpg_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('mpg_mmt_mp_measd', true, false, '{id, mpg_mmt_mp, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('mpg_sample_specif', true, false, '{id, mpg_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('geolocat_mpg', true, false, '{id, mpg_id, tree_mod, last_mod}'::text[]);

-- Add serial ids to messprogramm_mmt_messgroesse and messprogramm_proben_zusatz
ALTER TABLE mpg_mmt_mp_measd ADD COLUMN id SERIAL;
ALTER TABLE mpg_mmt_mp_measd DROP CONSTRAINT mpg_mmt_mp_measd_pkey;
ALTER TABLE mpg_mmt_mp_measd ADD PRIMARY KEY (id);
ALTER TABLE mpg_mmt_mp_measd ADD UNIQUE (mpg_mmt_mp_id, measd_id);
ALTER TABLE mpg_mmt_mp_measd ALTER COLUMN mpg_mmt_mp_id SET NOT NULL;
ALTER TABLE mpg_mmt_mp_measd ALTER COLUMN measd_id SET NOT NULL;
GRANT USAGE, SELECT ON SEQUENCE mpg_mmt_mp_measd_id_seq TO lada;

ALTER TABLE mpg_sample_specif ADD COLUMN id SERIAL;
ALTER TABLE mpg_sample_specif DROP CONSTRAINT mpg_sample_specif_pkey;
ALTER TABLE mpg_sample_specif ADD PRIMARY KEY(id);
ALTER TABLE mpg_sample_specif ADD UNIQUE(sample_specif_id, mpg_id);
ALTER TABLE mpg_sample_specif ALTER COLUMN sample_specif_id SET NOT NULL;
ALTER TABLE mpg_sample_specif ALTER COLUMN mpg_id SET NOT NULL;
GRANT USAGE, SELECT ON SEQUENCE mpg_sample_specif_id_seq TO lada;
