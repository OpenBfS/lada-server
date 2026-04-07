DROP VIEW stamm.tag;

ALTER TABLE master.tag ALTER COLUMN name TYPE VARCHAR(32);

CREATE VIEW stamm.tag AS SELECT
	id,
	name AS tag,
	meas_facil_id AS mst_id,
	is_auto_tag AS auto_tag,
	network_id AS netzbetreiber_id,
	lada_user_id AS user_id,
	val_until AS gueltig_bis,
	created_at
FROM master.tag;
