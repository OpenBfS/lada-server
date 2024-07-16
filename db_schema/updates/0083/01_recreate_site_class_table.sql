-- Drop dependent views
DROP VIEW stamm.ort;
DROP VIEW stamm.ort_typ;

ALTER TABLE IF EXISTS master.site DROP CONSTRAINT IF EXISTS site_site_class_id_fkey;

DROP TABLE master.site_class;

CREATE TABLE IF NOT EXISTS master.site_class
(
    id smallint NOT NULL,
    name character varying(60),
    ext_id character varying(3),
    last_mod timestamp without time zone DEFAULT timezone('utc'::text, now()),
    CONSTRAINT site_class_pkey PRIMARY KEY (id)
);

INSERT INTO master.site_class (id, name, ext_id) VALUES
    (1, 'dynamischer Messpunkt (nicht vordefiniert)', 'DYN'),
    (2, 'vordefinierter Messpunkt', 'GP'),
    (3, 'REI-Messpunkt','REI'),
    (4, 'Verwaltungseinheit','VE'),
    (5, 'Staat', 'ST');

ALTER TABLE IF EXISTS master.site
    ADD CONSTRAINT site_site_class_id_fkey FOREIGN KEY (site_class_id)
    REFERENCES master.site_class (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE IF EXISTS master.site_class
    OWNER to lada;

REVOKE ALL ON TABLE master.site_class FROM imis_ro;
REVOKE ALL ON TABLE master.site_class FROM readonly;

GRANT SELECT ON TABLE master.site_class TO imis_ro;

GRANT ALL ON TABLE master.site_class TO lada;

GRANT SELECT ON TABLE master.site_class TO readonly;

-- Trigger: last_mod_site_class

-- DROP TRIGGER IF EXISTS last_mod_site_class ON master.site_class;

CREATE TRIGGER last_mod_site_class
    BEFORE UPDATE 
    ON master.site_class
    FOR EACH ROW
    EXECUTE FUNCTION master.update_last_mod();

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

GRANT SELECT ON TABLE stamm.ort TO imis_ro;
GRANT ALL ON TABLE stamm.ort TO lada;
GRANT SELECT ON TABLE stamm.ort TO readonly;
