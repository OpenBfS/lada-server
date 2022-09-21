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
SELECT stamm.audit_table('ortszuordnung_mp', true, false, '{id, messprogramm_id, tree_modified, letzte_aenderung}'::text[]);
