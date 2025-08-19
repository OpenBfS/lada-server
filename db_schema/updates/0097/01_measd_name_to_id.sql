ALTER TABLE lada.mpg_mmt_mp_measd ADD measd_id_ character varying(50);
UPDATE lada.mpg_mmt_mp_measd
    SET measd_id_ = (SELECT name FROM master.measd WHERE id = measd_id);
ALTER TABLE lada.mpg_mmt_mp_measd ALTER measd_id_ SET NOT NULL;
DROP VIEW land.messprogramm_mmt_messgroesse;
ALTER TABLE lada.mpg_mmt_mp_measd DROP measd_id;
ALTER TABLE lada.mpg_mmt_mp_measd RENAME measd_id_ TO measd_id;
ALTER TABLE lada.mpg_mmt_mp_measd ADD UNIQUE (mpg_mmt_mp_id, measd_id);
CREATE VIEW land.messprogramm_mmt_messgroesse AS SELECT
	mpg_mmt_mp_id AS messprogramm_mmt_id,
	measd_id AS messgroesse_id
FROM lada.mpg_mmt_mp_measd;

ALTER TABLE lada.meas_val ADD measd_id_ character varying(50);
UPDATE lada.meas_val
    SET measd_id_ = (SELECT name FROM master.measd WHERE id = measd_id);
ALTER TABLE lada.meas_val ALTER measd_id_ SET NOT NULL;
DROP VIEW public.lada_messwert;
DROP VIEW land.messwert;
DROP VIEW land.messwert_view;
DROP VIEW public.lada_meas_val;
DROP VIEW lada.meas_val_view;
ALTER TABLE lada.meas_val DROP measd_id;
ALTER TABLE lada.meas_val RENAME measd_id_ TO measd_id;
ALTER TABLE lada.meas_val ADD UNIQUE (measm_id, measd_id);
CREATE VIEW public.lada_meas_val AS
 SELECT meas_val.id,
    meas_val.measm_id,
    meas_val.measd_id,
    meas_val.less_than_lod,
    meas_val.meas_val,
    meas_val.error,
    meas_val.detect_lim,
    meas_val.meas_unit_id,
    meas_val.is_threshold,
    status_prot.status_mp_id,
    meas_val.last_mod
   FROM lada.meas_val
     JOIN lada.measm ON meas_val.measm_id = measm.id
     JOIN lada.status_prot ON measm.status = status_prot.id
        AND status_prot.status_mp_id <> 1;
CREATE VIEW lada.meas_val_view
 AS
 SELECT meas_val.id,
    meas_val.measm_id,
    meas_val.measd_id,
    meas_val.less_than_lod,
    meas_val.meas_val,
    meas_val.error,
    meas_val.detect_lim,
    meas_val.meas_unit_id,
    meas_val.is_threshold,
    status_prot.status_mp_id,
    meas_val.last_mod
   FROM lada.meas_val
     JOIN lada.measm ON meas_val.measm_id = measm.id
     JOIN lada.status_prot ON measm.status = status_prot.id
        AND status_prot.status_mp_id <> 1;
CREATE VIEW land.messwert_view AS SELECT
	id,
	measm_id AS messungs_id,
	measd_id AS messgroesse_id,
	less_than_lod AS messwert_nwg,
	meas_val AS messwert,
	error AS messfehler,
	detect_lim AS nwg_zu_messwert,
	meas_unit_id AS meh_id,
	is_threshold AS grenzwertueberschreitung,
	status_mp_id AS status_kombi,
	last_mod AS letzte_aenderung
FROM lada.meas_val_view;
CREATE VIEW land.messwert AS SELECT
	id,
	measm_id AS messungs_id,
	measd_id AS messgroesse_id,
	less_than_lod AS messwert_nwg,
	meas_val AS messwert,
	error AS messfehler,
	detect_lim AS nwg_zu_messwert,
	meas_unit_id AS meh_id,
	is_threshold AS grenzwertueberschreitung,
	last_mod AS letzte_aenderung,
	tree_mod AS tree_modified
FROM lada.meas_val;
CREATE VIEW public.lada_messwert AS
 SELECT meas_val.id,
    meas_val.measm_id AS messungs_id,
    meas_val.measd_id AS messgroesse_id,
    meas_val.less_than_lod AS messwert_nwg,
    meas_val.meas_val AS messwert,
    meas_val.error AS messfehler,
    meas_val.detect_lim AS nwg_zu_messwert,
    meas_val.meas_unit_id AS meh_id,
    meas_val.is_threshold AS grenzwertueberschreitung,
    status_prot.status_mp_id AS status_kombi,
    meas_val.last_mod AS letzte_aenderung
   FROM lada.meas_val
     JOIN lada.measm ON meas_val.measm_id = measm.id
     JOIN lada.status_prot ON measm.status = status_prot.id
        AND status_prot.status_mp_id <> 1;

ALTER TABLE master.measd_gr_mp ADD measd_id_ character varying(50);
UPDATE master.measd_gr_mp
    SET measd_id_ = (SELECT name FROM master.measd WHERE id = measd_id);
DROP VIEW stamm.mmt_messgroesse;
DROP VIEW master.mmt_measd_view;
DROP VIEW stamm.mg_grp;
ALTER TABLE master.measd_gr_mp DROP measd_id;
ALTER TABLE master.measd_gr_mp RENAME measd_id_ TO measd_id;
ALTER TABLE master.measd_gr_mp ADD PRIMARY KEY (measd_gr_id, measd_id);
CREATE VIEW master.mmt_measd_view AS
 SELECT mmt_measd_gr_mp.mmt_id,
    measd_gr_mp.measd_id
   FROM master.mmt_measd_gr_mp, master.measd_gr_mp
  WHERE (measd_gr_mp.measd_gr_id = mmt_measd_gr_mp.measd_gr_id);
CREATE VIEW stamm.mmt_messgroesse AS SELECT
	mmt_id,
	measd_id AS messgroesse_id
FROM master.mmt_measd_view;
CREATE VIEW stamm.mg_grp AS SELECT
	measd_gr_id AS messgroessengruppe_id,
	measd_id AS messgroesse_id,
	last_mod AS letzte_aenderung
FROM master.measd_gr_mp;

ALTER TABLE master.oblig_measd_mp ADD measd_id_ character varying(50);
UPDATE master.oblig_measd_mp
    SET measd_id_ = (SELECT name FROM master.measd WHERE id = measd_id);
ALTER TABLE master.oblig_measd_mp ALTER measd_id_ SET NOT NULL;
DROP VIEW stamm.pflicht_messgroesse;
ALTER TABLE master.oblig_measd_mp DROP measd_id;
ALTER TABLE master.oblig_measd_mp RENAME measd_id_ TO measd_id;
ALTER TABLE master.oblig_measd_mp
    ADD UNIQUE (measd_id, mmt_id, env_medium_id, regulation_id);
CREATE VIEW stamm.pflicht_messgroesse AS SELECT
	id,
	measd_id AS messgroesse_id,
	mmt_id,
	env_medium_id AS umw_id,
	regulation_id AS datenbasis_id,
	last_mod AS letzte_aenderung
FROM master.oblig_measd_mp;

ALTER TABLE master.measd RENAME id TO id_old;
ALTER TABLE master.measd RENAME name TO id;
ALTER TABLE master.measd
    DROP CONSTRAINT measd_pkey,
    DROP CONSTRAINT measd_name_key,
    ADD PRIMARY KEY (id),
    ADD UNIQUE (id_old),
    ALTER id_old SET NOT NULL;
ALTER TABLE lada.mpg_mmt_mp_measd
    ADD FOREIGN KEY (measd_id) REFERENCES master.measd;
ALTER TABLE lada.meas_val
    ADD FOREIGN KEY (measd_id) REFERENCES master.measd;
ALTER TABLE master.measd_gr_mp
    ADD FOREIGN KEY (measd_id) REFERENCES master.measd;
ALTER TABLE master.oblig_measd_mp
    ADD FOREIGN KEY (measd_id) REFERENCES master.measd;

CREATE OR REPLACE VIEW stamm.messgroesse AS SELECT
	id_old AS id,
	descr AS beschreibung,
	id AS messgroesse,
	def_color AS default_farbe,
	idf_ext_id AS idf_nuklid_key,
	is_ref_nucl AS ist_leitnuklid,
	eudf_nucl_id AS eudf_nuklid_id,
	bvl_format_id AS kennung_bvl,
	last_mod AS letzte_aenderung
FROM master.measd;
