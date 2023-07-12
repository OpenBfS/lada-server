DROP VIEW stamm.tag;

ALTER TABLE master.tag ALTER COLUMN meas_facil_id TYPE character varying(5);

CREATE VIEW stamm.tag AS SELECT
	id,
	name AS tag,
	meas_facil_id AS mst_id,
	is_auto_tag AS auto_tag,
	network_id AS netzbetreiber_id,
	lada_user_id AS user_id,
	tag_type as tag_typ,
	val_until AS gueltig_bis,
	created_at
FROM master.tag;
