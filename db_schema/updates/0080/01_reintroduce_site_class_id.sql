-- Drop dependent views
DROP VIEW stamm.ort;

-- Re-introduce integer primary key
DO $$
-- Rev. 5a4595811374904b23808959d431a305061312d4 accidentally introduced
-- diverging primary key names in schema script respectively migration:
DECLARE
    pk_name text;
BEGIN
    SELECT column_name INTO STRICT pk_name
        FROM information_schema.table_constraints
            JOIN information_schema.key_column_usage
                USING(table_catalog, table_schema, table_name,
                    constraint_name)
        WHERE table_catalog = current_catalog
            AND table_schema = 'master'
            AND table_name = 'site_class'
            AND constraint_type = 'PRIMARY KEY';
    IF pk_name = 'id' THEN
        EXECUTE format(
            'ALTER TABLE master.site_class RENAME %I TO ext_id',
            pk_name
        );
    END IF;
END;
$$;
ALTER TABLE master.site_class ADD id smallint;
UPDATE master.site_class SET id = 1 WHERE ext_id = 'DYN';
UPDATE master.site_class SET id = 2 WHERE ext_id = 'GP';
UPDATE master.site_class SET id = 3 WHERE ext_id = 'REI';
UPDATE master.site_class SET id = 4 WHERE ext_id = 'VE';
UPDATE master.site_class SET id = 5 WHERE ext_id = 'ST';

ALTER TABLE master.site DISABLE TRIGGER last_mod_site;
ALTER TABLE master.site DISABLE TRIGGER audit_trigger_row;
ALTER TABLE master.site DISABLE TRIGGER audit_trigger_stm;

ALTER TABLE master.site ADD site_class_id_ smallint;
UPDATE master.site SET site_class_id_ = (
    SELECT id FROM master.site_class WHERE ext_id = site_class_id);
ALTER TABLE master.site DROP site_class_id;
ALTER TABLE master.site RENAME site_class_id_ TO site_class_id;
ALTER TABLE master.site ALTER site_class_id SET NOT NULL;

ALTER TABLE master.site ENABLE TRIGGER last_mod_site;
ALTER TABLE master.site ENABLE TRIGGER audit_trigger_row;
ALTER TABLE master.site ENABLE TRIGGER audit_trigger_stm;

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
