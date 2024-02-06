SET search_path TO lada;

SELECT master.audit_table('sample', true, false, '{id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('measm', true, false, '{id, sample_id, tree_mod, last_mod, status}'::text[]);
SELECT master.audit_table('meas_val', true, false, '{id, measm_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('comm_sample', true, false, '{id, sample_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('comm_measm', true, false, '{id, measm_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('sample_specif_meas_val', true, false, '{id, sample_id, tree_mod, last_mod}'::text[]);
SELECT master.audit_table('geolocat', true, false, '{id, sample_id, tree_mod, last_mod}'::text[]);

SET search_path TO master;

SELECT audit_table('site', true, false, '{id, ext_id, tree_mod, last_mod}'::text[]);
