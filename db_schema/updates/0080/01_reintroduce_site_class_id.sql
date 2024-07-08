-- Drop dependent views
DROP VIEW stamm.ort;

-- Re-introduce integer primary key
ALTER TABLE master.site_class RENAME id TO ext_id;
ALTER TABLE master.site_class ADD id smallint;
UPDATE master.site_class SET id = 1 WHERE ext_id = 'DYN';
UPDATE master.site_class SET id = 2 WHERE ext_id = 'GP';
UPDATE master.site_class SET id = 3 WHERE ext_id = 'REI';
UPDATE master.site_class SET id = 4 WHERE ext_id = 'VE';
UPDATE master.site_class SET id = 5 WHERE ext_id = 'ST';

ALTER TABLE master.site ADD site_class_id_ smallint;
UPDATE master.site SET site_class_id_ = (
    SELECT id FROM master.site_class WHERE ext_id = site_class_id);
ALTER TABLE master.site DROP site_class_id;
ALTER TABLE master.site RENAME site_class_id_ TO site_class_id;
ALTER TABLE master.site ALTER site_class_id SET NOT NULL;

ALTER TABLE master.site_class
    DROP CONSTRAINT site_class_pkey,
    ADD PRIMARY KEY (id),
    ALTER ext_id SET NOT NULL,
    ADD UNIQUE(ext_id);

ALTER TABLE master.site
    ADD FOREIGN KEY (site_class_id) REFERENCES master.site_class;

-- Update queries
UPDATE master.base_query SET sql = sql
        || ' LEFT JOIN master.site_class ON site.site_class_id = site_class.id'
    WHERE position('site_class_id' IN sql) <> 0;
UPDATE master.base_query SET sql = overlay(
        sql placing 'site_class.ext_id'
        from position('site_class_id' IN sql)
        for length('site_class_id'))
    WHERE position('site_class_id' IN sql) <> 0;

-- Re-create views
CREATE VIEW stamm.ort_typ AS SELECT
	id,
	ext_id AS code,
	last_mod AS letzte_aenderung
FROM master.site_class;

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
