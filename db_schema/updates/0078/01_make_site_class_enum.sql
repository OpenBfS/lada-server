-- Drop dependent views
DROP VIEW stamm.ort;
DROP VIEW stamm.ort_typ;

-- Update queries
UPDATE master.base_query SET sql = replace(
    sql, 'site_class.ext_id', 'site_class_id');
UPDATE master.base_query SET sql = replace(
    sql,
    'LEFT JOIN master.site_class ON site.site_class_id = site_class.id',
    ' ');

-- Move to enum
CREATE TYPE site_class AS ENUM ('DYN', 'GP', 'REI', 'VE', 'ST');
CREATE CAST (varchar AS site_class) WITH INOUT AS ASSIGNMENT;

ALTER TABLE master.site ADD site_class_id_ site_class;
UPDATE master.site SET site_class_id_ = (
    SELECT ext_id FROM master.site_class WHERE id = site_class_id);
ALTER TABLE master.site DROP site_class_id;
ALTER TABLE master.site RENAME site_class_id_ TO site_class_id;
ALTER TABLE master.site ALTER site_class_id SET NOT NULL;

DROP TABLE master.site_class;

-- Recreate view
CREATE VIEW stamm.ort AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS ort_id,
	long_text AS langtext,
	state_id AS staat_id,
	admin_unit_id AS gem_id,
	is_fuzzy AS unscharf,
	spat_ref_sys_id AS kda_id,
	coord_x_ext AS koord_x_extern,
	coord_y_ext AS koord_y_extern,
	alt AS hoehe_land,
	last_mod AS letzte_aenderung,
	geom,
	shape,
	site_class_id AS ort_typ,
	short_text AS kurztext,
	rei_report_text AS berichtstext,
	rei_zone AS zone,
	rei_sector AS sektor,
	rei_competence AS zustaendigkeit,
	rei_opr_mode AS mp_art,
	is_rei_active AS aktiv,
	nucl_facil_gr_id AS kta_gruppe_id,
	poi_id AS oz_id,
	height_asl AS hoehe_ueber_nn,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	munic_div_id AS gem_unt_id
FROM master.site;
