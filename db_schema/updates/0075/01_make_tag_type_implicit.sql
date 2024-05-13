-- Ensure tags are identifiable
UPDATE master.tag SET network_id = NULL, meas_facil_id = NULL
    WHERE tag_type = 'global';
UPDATE master.tag SET meas_facil_id = NULL
    WHERE tag_type = 'netz';
UPDATE master.tag SET network_id = NULL
    WHERE tag_type = 'mst';

-- Update SQL for queries
UPDATE master.base_query SET sql = replace(
    sql, 'JOIN master.tag_type ON tag.tag_type = tag_type.id',
    'JOIN master.tag_type ON CASE '
    || 'WHEN network_id IS NOT NULL THEN ''netz'' '
    || 'WHEN meas_facil_id IS NOT NULL THEN ''mst'' '
    || 'ELSE ''global'' END = tag_type.id');

UPDATE master.filter SET sql = replace(sql, 'tag.tag_type', 'tag_type.id');

-- Actual schema changes
DROP VIEW stamm.tag;
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

ALTER TABLE master.tag DROP tag_type;
