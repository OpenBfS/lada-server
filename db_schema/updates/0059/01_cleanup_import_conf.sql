DROP VIEW stamm.importer_config;

ALTER TABLE master.import_conf
    DROP COLUMN name CASCADE,
    ADD UNIQUE(meas_facil_id, attribute, action, from_val);

CREATE VIEW stamm.importer_config AS SELECT
	id,
	attribute,
	meas_facil_id AS mst_id,
	from_val AS from_value,
	to_val AS to_value,
	action,
	last_mod AS letzte_aenderung
FROM master.import_conf;
