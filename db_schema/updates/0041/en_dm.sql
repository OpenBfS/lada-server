SET role lada;

ALTER SCHEMA land RENAME TO lada;
ALTER SCHEMA stamm RENAME TO master;
CREATE SCHEMA IF NOT EXISTS land;
CREATE SCHEMA IF NOT EXISTS stamm;

CREATE VIEW land.audit_trail AS SELECT
	id,
	table_name,
	tstamp,
	action,
	object_id,
	row_data,
	changed_fields
FROM lada.audit_trail;

ALTER TABLE lada.audit_trail_messung RENAME TO audit_trail_measm_view;
ALTER TABLE lada.audit_trail_measm_view RENAME COLUMN messungs_id TO measm_id;
CREATE VIEW land.audit_trail_messung AS SELECT
	id,
	table_name,
	tstamp,
	action,
	object_id,
	row_data,
	changed_fields,
	measm_id AS messungs_id
FROM lada.audit_trail_measm_view;

ALTER TABLE lada.audit_trail_probe RENAME TO audit_trail_sample_view;
ALTER TABLE lada.audit_trail_sample_view RENAME COLUMN messungs_id TO measm_id;
ALTER TABLE lada.audit_trail_sample_view RENAME COLUMN probe_id TO sample_id;
ALTER TABLE lada.audit_trail_sample_view RENAME COLUMN ort_id TO site_id;
CREATE VIEW land.audit_trail_probe AS SELECT
	id,
	table_name,
	action,
	object_id,
	tstamp,
	measm_id AS messungs_id,
	sample_id AS probe_id,
	row_data,
	changed_fields,
	site_id AS ort_id
FROM lada.audit_trail_sample_view;

ALTER TABLE lada.kommentar_m RENAME TO comm_measm;
ALTER TABLE lada.comm_measm RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE lada.comm_measm RENAME COLUMN datum TO date;
ALTER TABLE lada.comm_measm RENAME COLUMN messungs_id TO measm_id;
CREATE VIEW land.kommentar_m AS SELECT
	id,
	meas_facil_id AS mst_id,
	date AS datum,
	text,
	measm_id AS messungs_id
FROM lada.comm_measm;

ALTER TABLE lada.kommentar_p RENAME TO comm_sample;
ALTER TABLE lada.comm_sample RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE lada.comm_sample RENAME COLUMN datum TO date;
ALTER TABLE lada.comm_sample RENAME COLUMN probe_id TO sample_id;
CREATE VIEW land.kommentar_p AS SELECT
	id,
	meas_facil_id AS mst_id,
	date AS datum,
	text,
	sample_id AS probe_id
FROM lada.comm_sample;

ALTER TABLE lada.messprogramm RENAME TO mpg;
ALTER TABLE lada.mpg RENAME COLUMN kommentar TO comm_mpg;
ALTER TABLE lada.mpg RENAME COLUMN test TO is_test;
ALTER TABLE lada.mpg RENAME COLUMN aktiv TO is_active;
ALTER TABLE lada.mpg RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE lada.mpg RENAME COLUMN labor_mst_id TO appr_lab_id;
ALTER TABLE lada.mpg RENAME COLUMN datenbasis_id TO regulation_id;
ALTER TABLE lada.mpg RENAME COLUMN ba_id TO opr_mode_id;
ALTER TABLE lada.mpg RENAME COLUMN gem_id TO munic_id;
ALTER TABLE lada.mpg RENAME COLUMN media_desk TO env_descrip_id;
ALTER TABLE lada.mpg RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE lada.mpg RENAME COLUMN probenart_id TO sample_meth_id;
ALTER TABLE lada.mpg RENAME COLUMN probenintervall TO sample_pd;
ALTER TABLE lada.mpg RENAME COLUMN teilintervall_von TO sample_pd_start_date;
ALTER TABLE lada.mpg RENAME COLUMN teilintervall_bis TO sample_pd_end_date;
ALTER TABLE lada.mpg RENAME COLUMN intervall_offset TO sample_pd_offset;
ALTER TABLE lada.mpg RENAME COLUMN gueltig_von TO valid_start_date;
ALTER TABLE lada.mpg RENAME COLUMN gueltig_bis TO valid_end_date;
ALTER TABLE lada.mpg RENAME COLUMN probe_nehmer_id TO sampler_id;
ALTER TABLE lada.mpg RENAME COLUMN mpl_id TO state_mpg_id;
ALTER TABLE lada.mpg RENAME COLUMN probe_kommentar TO comm_sample;
ALTER TABLE lada.mpg RENAME COLUMN rei_progpunkt_grp_id TO rei_ag_gr_id;
ALTER TABLE lada.mpg RENAME COLUMN kta_gruppe_id TO nucl_facil_gr_id;
ALTER TABLE lada.mpg RENAME COLUMN meh_id TO unit_id;
ALTER TABLE lada.mpg RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.mpg RENAME COLUMN probenahmemenge TO sample_quant;
CREATE VIEW land.messprogramm AS SELECT
	id,
	comm_mpg AS kommentar,
	is_test AS test,
	is_active AS aktiv,
	meas_facil_id AS mst_id,
	appr_lab_id AS labor_mst_id,
	regulation_id AS datenbasis_id,
	opr_mode_id AS ba_id,
	munic_id AS gem_id,
	env_descrip_id AS media_desk,
	env_medium_id AS umw_id,
	sample_meth_id AS probenart_id,
	sample_pd AS probenintervall,
	sample_pd_start_date AS teilintervall_von,
	sample_pd_end_date AS teilintervall_bis,
	sample_pd_offset AS intervall_offset,
	valid_start_date AS gueltig_von,
	valid_end_date AS gueltig_bis,
	sampler_id AS probe_nehmer_id,
	state_mpg_id AS mpl_id,
	comm_sample AS probe_kommentar,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	nucl_facil_gr_id AS kta_gruppe_id,
	unit_id AS meh_id,
	last_mod AS letzte_aenderung,
	sample_quant AS probenahmemenge
FROM lada.mpg;

ALTER TABLE lada.messprogramm_mmt RENAME TO mpg_mmt_mp;
ALTER TABLE lada.mpg_mmt_mp RENAME COLUMN messprogramm_id TO mpg_id;
ALTER TABLE lada.mpg_mmt_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW land.messprogramm_mmt AS SELECT
	id,
	mpg_id AS messprogramm_id,
	mmt_id,
	last_mod AS letzte_aenderung
FROM lada.mpg_mmt_mp;

ALTER TABLE lada.messprogramm_mmt_messgroesse RENAME TO mpg_mmt_measd_mp;
ALTER TABLE lada.mpg_mmt_measd_mp RENAME COLUMN messprogramm_mmt_id TO mpg_mmt_id;
ALTER TABLE lada.mpg_mmt_measd_mp RENAME COLUMN messgroesse_id TO measd_id;
CREATE VIEW land.messprogramm_mmt_messgroesse AS SELECT
	measd_id AS messgroesse_id
FROM lada.mpg_mmt_measd_mp;

ALTER TABLE lada.messprogramm_proben_zusatz RENAME TO mpg_sample_specif;
ALTER TABLE lada.mpg_sample_specif RENAME COLUMN proben_zusatz_id TO sample_specif_id;
ALTER TABLE lada.mpg_sample_specif RENAME COLUMN messprogramm_id TO mpg_id;
CREATE VIEW land.messprogramm_proben_zusatz AS SELECT
	mpg_id AS messprogramm_id
FROM lada.mpg_sample_specif;

ALTER TABLE lada.messung RENAME TO measm;
ALTER TABLE lada.measm RENAME COLUMN ext_id TO measm_ext_id;
ALTER TABLE lada.measm RENAME COLUMN probe_id TO sample_id;
ALTER TABLE lada.measm RENAME COLUMN nebenproben_nr TO min_sample_id;
ALTER TABLE lada.measm RENAME COLUMN messdauer TO meas_pd;
ALTER TABLE lada.measm RENAME COLUMN messzeitpunkt TO measm_start_date;
ALTER TABLE lada.measm RENAME COLUMN fertig TO is_completed;
ALTER TABLE lada.measm RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.measm RENAME COLUMN geplant TO is_scheduled;
ALTER TABLE lada.measm RENAME COLUMN tree_modified TO tree_mod;
CREATE VIEW land.messung AS SELECT
	id,
	measm_ext_id AS ext_id,
	sample_id AS probe_id,
	min_sample_id AS nebenproben_nr,
	mmt_id,
	meas_pd AS messdauer,
	measm_start_date AS messzeitpunkt,
	is_completed AS fertig,
	status,
	last_mod AS letzte_aenderung,
	is_scheduled AS geplant,
	tree_mod AS tree_modified
FROM lada.measm;

ALTER TABLE lada.messwert RENAME TO meas_val;
ALTER TABLE lada.meas_val RENAME COLUMN messungs_id TO measm_id;
ALTER TABLE lada.meas_val RENAME COLUMN messgroesse_id TO measd_id;
ALTER TABLE lada.meas_val RENAME COLUMN messwert_nwg TO less_than_LOD;
ALTER TABLE lada.meas_val RENAME COLUMN messwert TO meas_val;
ALTER TABLE lada.meas_val RENAME COLUMN messfehler TO error;
ALTER TABLE lada.meas_val RENAME COLUMN nwg_zu_messwert TO detect_lim;
ALTER TABLE lada.meas_val RENAME COLUMN meh_id TO unit_id;
ALTER TABLE lada.meas_val RENAME COLUMN grenzwertueberschreitung TO is_threshold;
ALTER TABLE lada.meas_val RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.meas_val RENAME COLUMN tree_modified TO tree_mod;
CREATE VIEW land.messwert AS SELECT
	id,
	measm_id AS messungs_id,
	measd_id AS messgroesse_id,
	less_than_LOD AS messwert_nwg,
	meas_val AS messwert,
	error AS messfehler,
	detect_lim AS nwg_zu_messwert,
	unit_id AS meh_id,
	is_threshold AS grenzwertueberschreitung,
	last_mod AS letzte_aenderung,
	tree_mod AS tree_modified
FROM lada.meas_val;

ALTER TABLE lada.messwert_view RENAME TO meas_val_view;
ALTER TABLE lada.meas_val_view RENAME COLUMN messungs_id TO measm_id;
ALTER TABLE lada.meas_val_view RENAME COLUMN messgroesse_id TO measd_id;
ALTER TABLE lada.meas_val_view RENAME COLUMN messwert_nwg TO less_than_LOD;
ALTER TABLE lada.meas_val_view RENAME COLUMN messwert TO meas_val;
ALTER TABLE lada.meas_val_view RENAME COLUMN messfehler TO meas_err;
ALTER TABLE lada.meas_val_view RENAME COLUMN nwg_zu_messwert TO detect_lim;
ALTER TABLE lada.meas_val_view RENAME COLUMN meh_id TO unit_id;
ALTER TABLE lada.meas_val_view RENAME COLUMN grenzwertueberschreitung TO is_threshold;
ALTER TABLE lada.meas_val_view RENAME COLUMN status_kombi TO status_comb;
ALTER TABLE lada.meas_val_view RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW land.messwert_view AS SELECT
	id,
	measm_id AS messungs_id,
	measd_id AS messgroesse_id,
	less_than_LOD AS messwert_nwg,
	meas_val AS messwert,
	meas_err AS messfehler,
	detect_lim AS nwg_zu_messwert,
	unit_id AS meh_id,
	is_threshold AS grenzwertueberschreitung,
	status_comb AS status_kombi,
	last_mod AS letzte_aenderung
FROM lada.meas_val_view;

ALTER TABLE lada.ortszuordnung RENAME TO geolocat;
ALTER TABLE lada.geolocat RENAME COLUMN probe_id TO sample_id;
ALTER TABLE lada.geolocat RENAME COLUMN ort_id TO site_ext_id;
ALTER TABLE lada.geolocat RENAME COLUMN ortszuordnung_typ TO type_regulation;
ALTER TABLE lada.geolocat RENAME COLUMN ortszusatztext TO add_site_text;
ALTER TABLE lada.geolocat RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.geolocat RENAME COLUMN tree_modified TO tree_mod;
ALTER TABLE lada.geolocat RENAME COLUMN oz_id TO poi_id;
CREATE VIEW land.ortszuordnung AS SELECT
	id,
	sample_id AS probe_id,
	site_ext_id AS ort_id,
	type_regulation AS ortszuordnung_typ,
	add_site_text AS ortszusatztext,
	last_mod AS letzte_aenderung,
	tree_mod AS tree_modified,
	poi_id AS oz_id
FROM lada.geolocat;

ALTER TABLE lada.ortszuordnung_mp RENAME TO geolocat_mpg;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN messprogramm_id TO mpg_id;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN ort_id TO site_id;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN ortszuordnung_typ TO type_regulation;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN ortszusatztext TO add_site_text;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN tree_modified TO tree_mod;
ALTER TABLE lada.geolocat_mpg RENAME COLUMN oz_id TO poi_id;
CREATE VIEW land.ortszuordnung_mp AS SELECT
	id,
	mpg_id AS messprogramm_id,
	site_id AS ort_id,
	type_regulation AS ortszuordnung_typ,
	add_site_text AS ortszusatztext,
	last_mod AS letzte_aenderung,
	tree_mod AS tree_modified,
	poi_id AS oz_id
FROM lada.geolocat_mpg;

ALTER TABLE lada.probe RENAME TO sample;
ALTER TABLE lada.sample RENAME COLUMN ext_id TO sample_ext_id;
ALTER TABLE lada.sample RENAME COLUMN test TO is_test;
ALTER TABLE lada.sample RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE lada.sample RENAME COLUMN labor_mst_id TO appr_lab_id;
ALTER TABLE lada.sample RENAME COLUMN hauptproben_nr TO main_sample_id;
ALTER TABLE lada.sample RENAME COLUMN datenbasis_id TO regulation_id;
ALTER TABLE lada.sample RENAME COLUMN ba_id TO opr_mode_id;
ALTER TABLE lada.sample RENAME COLUMN probenart_id TO sample_meth_id;
ALTER TABLE lada.sample RENAME COLUMN media_desk TO env_descrip_display;
ALTER TABLE lada.sample RENAME COLUMN media TO env_descrip_name;
ALTER TABLE lada.sample RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE lada.sample RENAME COLUMN probeentnahme_beginn TO sample_start_date;
ALTER TABLE lada.sample RENAME COLUMN probeentnahme_ende TO sample_end_date;
ALTER TABLE lada.sample RENAME COLUMN mittelungsdauer TO mid_sample_date;
ALTER TABLE lada.sample RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.sample RENAME COLUMN erzeuger_id TO dataset_creator_id;
ALTER TABLE lada.sample RENAME COLUMN probe_nehmer_id TO sampler_id;
ALTER TABLE lada.sample RENAME COLUMN mpl_id TO state_mpg_id;
ALTER TABLE lada.sample RENAME COLUMN mpr_id TO mpg_id;
ALTER TABLE lada.sample RENAME COLUMN solldatum_beginn TO sched_start_date;
ALTER TABLE lada.sample RENAME COLUMN solldatum_ende TO sched_end_date;
ALTER TABLE lada.sample RENAME COLUMN tree_modified TO tree_mod;
ALTER TABLE lada.sample RENAME COLUMN rei_progpunkt_grp_id TO rei_ag_gr_id;
ALTER TABLE lada.sample RENAME COLUMN kta_gruppe_id TO nucl_facil_gr_id;
ALTER TABLE lada.sample RENAME COLUMN ursprungszeit TO orig_date;
ALTER TABLE lada.sample RENAME COLUMN mitte_sammelzeitraum TO mid_coll_pd;
CREATE VIEW land.probe AS SELECT
	id,
	sample_ext_id AS ext_id,
	is_test AS test,
	meas_facil_id AS mst_id,
	appr_lab_id AS labor_mst_id,
	main_sample_id AS hauptproben_nr,
	regulation_id AS datenbasis_id,
	opr_mode_id AS ba_id,
	sample_meth_id AS probenart_id,
	env_descrip_display AS media_desk,
	env_descrip_name AS media,
	env_medium_id AS umw_id,
	sample_start_date AS probeentnahme_beginn,
	sample_end_date AS probeentnahme_ende,
	mid_sample_date AS mittelungsdauer,
	last_mod AS letzte_aenderung,
	dataset_creator_id AS erzeuger_id,
	sampler_id AS probe_nehmer_id,
	state_mpg_id AS mpl_id,
	mpg_id AS mpr_id,
	sched_start_date AS solldatum_beginn,
	sched_end_date AS solldatum_ende,
	tree_mod AS tree_modified,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	nucl_facil_gr_id AS kta_gruppe_id,
	orig_date AS ursprungszeit,
	mid_coll_pd AS mitte_sammelzeitraum
FROM lada.sample;

ALTER TABLE lada.rueckfrage_messung RENAME TO query_measm_view;
ALTER TABLE lada.query_measm_view RENAME COLUMN messungs_id TO measm_id;
CREATE VIEW land.rueckfrage_messung AS SELECT
FROM lada.query_measm_view;

ALTER TABLE lada.status_protokoll RENAME TO status_prot;
ALTER TABLE lada.status_prot RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE lada.status_prot RENAME COLUMN datum TO date;
ALTER TABLE lada.status_prot RENAME COLUMN messungs_id TO measm_id;
ALTER TABLE lada.status_prot RENAME COLUMN status_kombi TO status_comb;
ALTER TABLE lada.status_prot RENAME COLUMN tree_modified TO tree_mod;
CREATE VIEW land.status_protokoll AS SELECT
	id,
	meas_facil_id AS mst_id,
	date AS datum,
	text,
	measm_id AS messungs_id,
	status_comb AS status_kombi,
	tree_mod AS tree_modified
FROM lada.status_prot;

ALTER TABLE lada.tagzuordnung RENAME TO tag_link;
ALTER TABLE lada.tag_link RENAME COLUMN probe_id TO sample_id;
ALTER TABLE lada.tag_link RENAME COLUMN datum TO date;
ALTER TABLE lada.tag_link RENAME COLUMN messung_id TO measm_id;
CREATE VIEW land.tagzuordnung AS SELECT
	id,
	sample_id AS probe_id,
	tag_id,
	date AS datum,
	measm_id AS messung_id
FROM lada.tag_link;

ALTER TABLE lada.zusatz_wert RENAME TO sample_specif_meas_val;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN probe_id TO sample_id;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN pzs_id TO sample_specif_id;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN messwert_pzs TO meas_val;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN messfehler TO meas_err;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN tree_modified TO tree_mod;
ALTER TABLE lada.sample_specif_meas_val RENAME COLUMN kleiner_als TO smaller_than;
CREATE VIEW land.zusatz_wert AS SELECT
	id,
	sample_id AS probe_id,
	sample_specif_id AS pzs_id,
	meas_val AS messwert_pzs,
	meas_err AS messfehler,
	last_mod AS letzte_aenderung,
	tree_mod AS tree_modified,
	smaller_than AS kleiner_als
FROM lada.sample_specif_meas_val;

CREATE FUNCTION stamm.get_media_from_media_desk(media_desk character varying)
	RETURNS character varying
	LANGUAGE sql
	AS $$
		SELECT master.get_media_from_media_desk(media_desk)
	$$;

ALTER TABLE master.audit_trail RENAME COLUMN object_id TO site_id;
CREATE VIEW stamm.audit_trail AS SELECT
	id,
	table_name,
	tstamp,
	action,
	site_id AS object_id,
	row_data,
	changed_fields
FROM master.audit_trail;

ALTER TABLE master.audit_trail_ort RENAME TO audit_trail_site_view;
ALTER TABLE master.audit_trail_site_view RENAME COLUMN tstamp TO last_mod;
ALTER TABLE master.audit_trail_site_view RENAME COLUMN ort_id TO site_id;
CREATE VIEW stamm.audit_trail_ort AS SELECT
	id,
	table_name,
	last_mod AS tstamp,
	action,
	object_id,
	row_data,
	changed_fields,
	site_id AS ort_id
FROM master.audit_trail_site_view;


ALTER TABLE master.auth RENAME COLUMN ldap_group TO ldap_gr;
ALTER TABLE master.auth RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.auth RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE master.auth RENAME COLUMN labor_mst_id TO appr_lab_id;
ALTER TABLE master.auth RENAME COLUMN funktion_id TO auth_funct_id;
ALTER TABLE master.auth RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.auth AS SELECT
	id,
	ldap_gr AS ldap_group,
	network_id AS netzbetreiber_id,
	meas_facil_id AS mst_id,
	appr_lab_id AS labor_mst_id,
	auth_funct_id AS funktion_id,
	last_mod AS letzte_aenderung
FROM master.auth;

ALTER TABLE master.auth_funktion RENAME TO auth_funct;
ALTER TABLE master.auth_funct RENAME COLUMN funktion TO funct;
ALTER TABLE master.auth_funct RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.auth_funktion AS SELECT
	id,
	funct AS funktion,
	last_mod AS letzte_aenderung
FROM master.auth_funct;

ALTER TABLE master.auth_lst_umw RENAME TO auth_coord_ofc_env_medium_mp;
ALTER TABLE master.auth_coord_ofc_env_medium_mp RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE master.auth_coord_ofc_env_medium_mp RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.auth_coord_ofc_env_medium_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.auth_lst_umw AS SELECT
	id,
	meas_facil_id AS mst_id,
	env_medium_id AS umw_id,
	last_mod AS letzte_aenderung
FROM master.auth_coord_ofc_env_medium_mp;


CREATE VIEW stamm.base_query AS SELECT
	id,
	sql
FROM master.base_query;

ALTER TABLE master.betriebsart RENAME TO opr_mode;
ALTER TABLE master.opr_mode RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.betriebsart AS SELECT
	id,
	name,
	last_mod AS letzte_aenderung
FROM master.opr_mode;

ALTER TABLE master.datenbasis RENAME TO regulation;
ALTER TABLE master.regulation RENAME COLUMN beschreibung TO descr;
ALTER TABLE master.regulation RENAME COLUMN datenbasis TO regulation;
ALTER TABLE master.regulation RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.datenbasis AS SELECT
	id,
	descr AS beschreibung,
	regulation AS datenbasis,
	last_mod AS letzte_aenderung
FROM master.regulation;

ALTER TABLE master.datensatz_erzeuger RENAME TO dataset_creator;
ALTER TABLE master.dataset_creator RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.dataset_creator RENAME COLUMN datensatz_erzeuger_id TO ext_id;
ALTER TABLE master.dataset_creator RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE master.dataset_creator RENAME COLUMN bezeichnung TO descr;
ALTER TABLE master.dataset_creator RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.datensatz_erzeuger AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS datensatz_erzeuger_id,
	meas_facil_id AS mst_id,
	descr AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.dataset_creator;

ALTER TABLE master.deskriptor_umwelt RENAME TO env_descrip_env_medium_mp;
ALTER TABLE master.env_descrip_env_medium_mp RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.env_descrip_env_medium_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.deskriptor_umwelt AS SELECT
	id,
	s00,
	s01,
	s02,
	s03,
	s04,
	s05,
	s06,
	s07,
	s08,
	s09,
	s10,
	s11,
	env_medium_id AS umw_id,
	last_mod AS letzte_aenderung
FROM master.env_descrip_env_medium_mp;

ALTER TABLE master.deskriptoren RENAME TO env_descrip;
ALTER TABLE master.env_descrip RENAME COLUMN vorgaenger TO pred_id;
ALTER TABLE master.env_descrip RENAME COLUMN ebene TO lev;
ALTER TABLE master.env_descrip RENAME COLUMN sn TO lev_val;
ALTER TABLE master.env_descrip RENAME COLUMN beschreibung TO name;
ALTER TABLE master.env_descrip RENAME COLUMN bedeutung TO implication;
ALTER TABLE master.env_descrip RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.deskriptoren AS SELECT
	id,
	pred_id AS vorgaenger,
	lev AS ebene,
	s_xx,
	lev_val AS sn,
	name AS beschreibung,
	implication AS bedeutung,
	last_mod AS letzte_aenderung
FROM master.env_descrip;


ALTER TABLE master.filter RENAME COLUMN parameter TO param;
CREATE VIEW stamm.filter AS SELECT
	id,
	sql,
	param AS parameter,
	type,
	name
FROM master.filter;


ALTER TABLE master.filter_type RENAME COLUMN multiselect TO is_multiselect;
CREATE VIEW stamm.filter_type AS SELECT
	id,
	type,
	is_multiselect AS multiselect
FROM master.filter_type;

ALTER TABLE master.gemeindeuntergliederung RENAME TO munic_div;
ALTER TABLE master.munic_div RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.munic_div RENAME COLUMN gem_id TO munic_id;
ALTER TABLE master.munic_div RENAME COLUMN ozk_id TO site_ext_id;
ALTER TABLE master.munic_div RENAME COLUMN gemeindeuntergliederung TO name;
ALTER TABLE master.munic_div RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.gemeindeuntergliederung AS SELECT
	id,
	network_id AS netzbetreiber_id,
	munic_id AS gem_id,
	site_ext_id AS ozk_id,
	name AS gemeindeuntergliederung,
	last_mod AS letzte_aenderung
FROM master.munic_div;

ALTER TABLE master.grid_column RENAME TO grid_col_mp;
ALTER TABLE master.grid_col_mp RENAME COLUMN name TO grid_col;
CREATE VIEW stamm.grid_column AS SELECT
	id,
	base_query,
	grid_col AS name,
	data_index,
	position,
	filter,
	data_type
FROM master.grid_col_mp;

ALTER TABLE master.grid_column_values RENAME TO grid_col_conf;
ALTER TABLE master.grid_col_conf RENAME COLUMN grid_column TO grid_col_mp_id;
ALTER TABLE master.grid_col_conf RENAME COLUMN query_user TO query_user_id;
ALTER TABLE master.grid_col_conf RENAME COLUMN filter_value TO filter_val;
ALTER TABLE master.grid_col_conf RENAME COLUMN filter_active TO is_filter_active;
ALTER TABLE master.grid_col_conf RENAME COLUMN visible TO is_visible;
ALTER TABLE master.grid_col_conf RENAME COLUMN column_index TO col_index;
ALTER TABLE master.grid_col_conf RENAME COLUMN filter_negate TO is_filter_negate;
ALTER TABLE master.grid_col_conf RENAME COLUMN filter_regex TO is_filter_regex;
ALTER TABLE master.grid_col_conf RENAME COLUMN filter_is_null TO is_filter_null;
CREATE VIEW stamm.grid_column_values AS SELECT
	id,
	user_id,
	grid_col_mp_id AS grid_column,
	query_user_id AS query_user,
	sort,
	sort_index,
	filter_val AS filter_value,
	is_filter_active AS filter_active,
	is_visible AS visible,
	col_index AS column_index,
	width,
	is_filter_negate AS filter_negate,
	is_filter_regex AS filter_regex,
	is_filter_null AS filter_is_null
FROM master.grid_col_conf;


ALTER TABLE master.importer_config RENAME TO import_conf;
ALTER TABLE master.import_conf RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE master.import_conf RENAME COLUMN from_value TO from_val;
ALTER TABLE master.import_conf RENAME COLUMN to_value TO to_val;
ALTER TABLE master.import_conf RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.importer_config AS SELECT
	id,
	name,
	attribute,
	meas_facil_id AS mst_id,
	from_val AS from_value,
	to_val AS to_value,
	action,
	last_mod AS letzte_aenderung
FROM master.import_conf;

ALTER TABLE master.koordinaten_art RENAME TO spat_ref_sys;
ALTER TABLE master.spat_ref_sys RENAME COLUMN koordinatenart TO name;
ALTER TABLE master.spat_ref_sys RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.koordinaten_art AS SELECT
	id,
	name AS koordinatenart,
	idf_geo_key,
	last_mod AS letzte_aenderung
FROM master.spat_ref_sys;

ALTER TABLE master.kta RENAME TO nucl_facil;
ALTER TABLE master.nucl_facil RENAME COLUMN code TO ext_id;
ALTER TABLE master.nucl_facil RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.nucl_facil RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.kta AS SELECT
	id,
	ext_id AS code,
	name AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.nucl_facil;

ALTER TABLE master.kta_grp_zuord RENAME TO nucl_facil_gr_mp;
ALTER TABLE master.nucl_facil_gr_mp RENAME COLUMN kta_grp_id TO nucl_facil_gr_id;
ALTER TABLE master.nucl_facil_gr_mp RENAME COLUMN kta_id TO nucl_facil_id;
ALTER TABLE master.nucl_facil_gr_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.kta_grp_zuord AS SELECT
	id,
	nucl_facil_gr_id AS kta_grp_id,
	nucl_facil_id AS kta_id,
	last_mod AS letzte_aenderung
FROM master.nucl_facil_gr_mp;

ALTER TABLE master.kta_gruppe RENAME TO nucl_facil_gr;
ALTER TABLE master.nucl_facil_gr RENAME COLUMN kta_gruppe TO ext_id;
ALTER TABLE master.nucl_facil_gr RENAME COLUMN beschreibung TO name;
ALTER TABLE master.nucl_facil_gr RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.kta_gruppe AS SELECT
	id,
	ext_id AS kta_gruppe,
	name AS beschreibung,
	last_mod AS letzte_aenderung
FROM master.nucl_facil_gr;


CREATE VIEW stamm.lada_user AS SELECT
	id,
	name
FROM master.lada_user;

ALTER TABLE master.mass_einheit_umrechnung RENAME TO unit_convers;
ALTER TABLE master.unit_convers RENAME COLUMN meh_id_von TO from_unit_id;
ALTER TABLE master.unit_convers RENAME COLUMN meh_id_zu TO to_unit_id ;
ALTER TABLE master.unit_convers RENAME COLUMN faktor TO factor;
ALTER TABLE master.unit_convers RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.mass_einheit_umrechnung AS SELECT
	id,
	from_unit_id AS meh_id_von,
	to_unit_id  AS meh_id_zu,
	factor AS faktor,
	last_mod AS letzte_aenderung
FROM master.unit_convers;

ALTER TABLE master.mess_einheit RENAME TO meas_unit;
ALTER TABLE master.meas_unit RENAME COLUMN beschreibung TO name;
ALTER TABLE master.meas_unit RENAME COLUMN einheit TO unit_symbol;
ALTER TABLE master.meas_unit RENAME COLUMN eudf_messeinheit_id TO eudf_unit_id;
ALTER TABLE master.meas_unit RENAME COLUMN umrechnungs_faktor_eudf TO eudf_convers_factor;
ALTER TABLE master.meas_unit RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.mess_einheit AS SELECT
	id,
	name AS beschreibung,
	unit_symbol AS einheit,
	eudf_unit_id AS eudf_messeinheit_id,
	eudf_convers_factor AS umrechnungs_faktor_eudf,
	last_mod AS letzte_aenderung
FROM master.meas_unit;

ALTER TABLE master.mess_methode RENAME TO mmt;
ALTER TABLE master.mmt RENAME COLUMN beschreibung TO descr;
ALTER TABLE master.mmt RENAME COLUMN messmethode TO name;
ALTER TABLE master.mmt RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.mess_methode AS SELECT
	id,
	descr AS beschreibung,
	name AS messmethode,
	last_mod AS letzte_aenderung
FROM master.mmt;

ALTER TABLE master.mess_stelle RENAME TO meas_facil;
ALTER TABLE master.meas_facil RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.meas_facil RENAME COLUMN beschreibung TO address;
ALTER TABLE master.meas_facil RENAME COLUMN mess_stelle TO name;
ALTER TABLE master.meas_facil RENAME COLUMN mst_typ TO meas_facil_type;
ALTER TABLE master.meas_facil RENAME COLUMN amtskennung TO trunk_code;
ALTER TABLE master.meas_facil RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.mess_stelle AS SELECT
	id,
	network_id AS netzbetreiber_id,
	address AS beschreibung,
	name AS mess_stelle,
	meas_facil_type AS mst_typ,
	trunk_code AS amtskennung,
	last_mod AS letzte_aenderung
FROM master.meas_facil;

ALTER TABLE master.messgroesse RENAME TO measd;
ALTER TABLE master.measd RENAME COLUMN beschreibung TO descr;
ALTER TABLE master.measd RENAME COLUMN messgroesse TO name;
ALTER TABLE master.measd RENAME COLUMN default_farbe TO def_color;
ALTER TABLE master.measd RENAME COLUMN idf_nuklid_key TO idf_ext_id;
ALTER TABLE master.measd RENAME COLUMN ist_leitnuklid TO is_ref_nucl;
ALTER TABLE master.measd RENAME COLUMN eudf_nuklid_id TO eudf_nucl_id;
ALTER TABLE master.measd RENAME COLUMN kennung_bvl TO bvl_format_id;
ALTER TABLE master.measd RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.messgroesse AS SELECT
	id,
	descr AS beschreibung,
	name AS messgroesse,
	def_color AS default_farbe,
	idf_ext_id AS idf_nuklid_key,
	is_ref_nucl AS ist_leitnuklid,
	eudf_nucl_id AS eudf_nuklid_id,
	bvl_format_id AS kennung_bvl,
	last_mod AS letzte_aenderung
FROM master.measd;

ALTER TABLE master.messgroessen_gruppe RENAME TO measd_gr;
ALTER TABLE master.measd_gr RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.measd_gr RENAME COLUMN ist_leitnuklidgruppe TO ref_nucl_gr;
ALTER TABLE master.measd_gr RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.messgroessen_gruppe AS SELECT
	id,
	name AS bezeichnung,
	ref_nucl_gr AS ist_leitnuklidgruppe,
	last_mod AS letzte_aenderung
FROM master.measd_gr;

ALTER TABLE master.messprogramm_kategorie RENAME TO mpg_categ;
ALTER TABLE master.mpg_categ RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.mpg_categ RENAME COLUMN code TO ext_id;
ALTER TABLE master.mpg_categ RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.mpg_categ RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.messprogramm_kategorie AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS code,
	name AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.mpg_categ;

ALTER TABLE master.messprogramm_transfer RENAME TO mpg_transf;
ALTER TABLE master.mpg_transf RENAME COLUMN messprogramm_s TO ext_id;
ALTER TABLE master.mpg_transf RENAME COLUMN messprogramm_c TO name;
ALTER TABLE master.mpg_transf RENAME COLUMN ba_id TO opr_mode_id;
ALTER TABLE master.mpg_transf RENAME COLUMN datenbasis_id TO regulation_id;
CREATE VIEW stamm.messprogramm_transfer AS SELECT
	id,
	ext_id AS messprogramm_s,
	name AS messprogramm_c,
	opr_mode_id AS ba_id,
	regulation_id AS datenbasis_id
FROM master.mpg_transf;

ALTER TABLE master.mg_grp RENAME TO measd_gr_mp;
ALTER TABLE master.measd_gr_mp RENAME COLUMN messgroessengruppe_id TO measd_gr_id;
ALTER TABLE master.measd_gr_mp RENAME COLUMN messgroesse_id TO measd_id;
ALTER TABLE master.measd_gr_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.mg_grp AS SELECT
	measd_gr_id AS messgroessengruppe_id,
	measd_id AS messgroesse_id,
	last_mod AS letzte_aenderung
FROM master.measd_gr_mp;

ALTER TABLE master.mmt_messgroesse RENAME TO mmt_measd_view;
ALTER TABLE master.mmt_measd_view RENAME COLUMN messgroesse_id TO measd_id;
CREATE VIEW stamm.mmt_messgroesse AS SELECT
	mmt_id,
	measd_id AS messgroesse_id
FROM master.mmt_measd_view;

ALTER TABLE master.mmt_messgroesse_grp RENAME TO mmt_measd_gr_mp;
ALTER TABLE master.mmt_measd_gr_mp RENAME COLUMN messgroessengruppe_id TO measd_gr_id;
ALTER TABLE master.mmt_measd_gr_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.mmt_messgroesse_grp AS SELECT
	measd_gr_id AS messgroessengruppe_id,
	mmt_id,
	last_mod AS letzte_aenderung
FROM master.mmt_measd_gr_mp;

ALTER TABLE master.netz_betreiber RENAME TO network;
ALTER TABLE master.network RENAME COLUMN netzbetreiber TO name;
ALTER TABLE master.network RENAME COLUMN idf_netzbetreiber TO idf_network_id;
ALTER TABLE master.network RENAME COLUMN is_bmn TO is_fmn;
ALTER TABLE master.network RENAME COLUMN mailverteiler TO mail_list;
ALTER TABLE master.network RENAME COLUMN aktiv TO is_active;
ALTER TABLE master.network RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.netz_betreiber AS SELECT
	id,
	name AS netzbetreiber,
	idf_network_id AS idf_netzbetreiber,
	is_fmn AS is_bmn,
	mail_list AS mailverteiler,
	is_active AS aktiv,
	last_mod AS letzte_aenderung
FROM master.network;


ALTER TABLE master.ort RENAME TO site;
ALTER TABLE master.site RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.site RENAME COLUMN ort_id TO site_ext_id;
ALTER TABLE master.site RENAME COLUMN langtext TO long_text;
ALTER TABLE master.site RENAME COLUMN staat_id TO state_id;
ALTER TABLE master.site RENAME COLUMN gem_id TO munic_id;
ALTER TABLE master.site RENAME COLUMN unscharf TO is_fuzzy;
ALTER TABLE master.site RENAME COLUMN nuts_code TO nuts_id;
ALTER TABLE master.site RENAME COLUMN kda_id TO spat_ref_sys_id;
ALTER TABLE master.site RENAME COLUMN koord_x_extern TO x_coord_ext;
ALTER TABLE master.site RENAME COLUMN koord_y_extern TO y_coord_ext;
ALTER TABLE master.site RENAME COLUMN hoehe_land TO alt;
ALTER TABLE master.site RENAME COLUMN letzte_aenderung TO last_mod;
ALTER TABLE master.site RENAME COLUMN ort_typ TO site_class_id;
ALTER TABLE master.site RENAME COLUMN kurztext TO short_text;
ALTER TABLE master.site RENAME COLUMN berichtstext TO rei_report_text;
ALTER TABLE master.site RENAME COLUMN zone TO rei_zone;
ALTER TABLE master.site RENAME COLUMN sektor TO rei_sector;
ALTER TABLE master.site RENAME COLUMN zustaendigkeit TO rei_competence;
ALTER TABLE master.site RENAME COLUMN mp_art TO rei_opr_mode;
ALTER TABLE master.site RENAME COLUMN aktiv TO is_rei_active;
ALTER TABLE master.site RENAME COLUMN kta_gruppe_id TO rei_nucl_facil_gr_id;
ALTER TABLE master.site RENAME COLUMN oz_id TO poi_id;
ALTER TABLE master.site RENAME COLUMN hoehe_ueber_nn TO height_asl;
ALTER TABLE master.site RENAME COLUMN rei_progpunkt_grp_id TO rei_ag_gr_id;
ALTER TABLE master.site RENAME COLUMN gem_unt_id TO munic_div_id;
CREATE VIEW stamm.ort AS SELECT
	id,
	network_id AS netzbetreiber_id,
	site_ext_id AS ort_id,
	long_text AS langtext,
	state_id AS staat_id,
	munic_id AS gem_id,
	is_fuzzy AS unscharf,
	nuts_id AS nuts_code,
	spat_ref_sys_id AS kda_id,
	x_coord_ext AS koord_x_extern,
	y_coord_ext AS koord_y_extern,
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
	rei_nucl_facil_gr_id AS kta_gruppe_id,
	poi_id AS oz_id,
	height_asl AS hoehe_ueber_nn,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	munic_div_id AS gem_unt_id
FROM master.site;


ALTER TABLE master.ort_typ RENAME TO site_class;
ALTER TABLE master.site_class RENAME COLUMN ort_typ TO name;
ALTER TABLE master.site_class RENAME COLUMN code TO ext_id;
ALTER TABLE master.site_class RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.ort_typ AS SELECT
	id,
	name AS ort_typ,
	ext_id AS code,
	last_mod AS letzte_aenderung
FROM master.site_class;

ALTER TABLE master.ortszuordnung_typ RENAME TO type_regulation;
ALTER TABLE master.type_regulation RENAME COLUMN ortstyp TO name;
ALTER TABLE master.type_regulation RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.ortszuordnung_typ AS SELECT
	id,
	name AS ortstyp,
	last_mod AS letzte_aenderung
FROM master.type_regulation;

ALTER TABLE master.ortszusatz RENAME TO poi;
ALTER TABLE master.poi RENAME COLUMN ozs_id TO id;
ALTER TABLE master.poi RENAME COLUMN ortszusatz TO name;
ALTER TABLE master.poi RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.ortszusatz AS SELECT
	id as ozs_id,
	name AS ortszusatz,
	last_mod AS letzte_aenderung
FROM master.poi;

ALTER TABLE master.pflicht_messgroesse RENAME TO oblig_measd_mp;
ALTER TABLE master.oblig_measd_mp RENAME COLUMN messgroesse_id TO measd_id;
ALTER TABLE master.oblig_measd_mp RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.oblig_measd_mp RENAME COLUMN datenbasis_id TO regulation_id;
ALTER TABLE master.oblig_measd_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.pflicht_messgroesse AS SELECT
	id,
	measd_id AS messgroesse_id,
	mmt_id,
	env_medium_id AS umw_id,
	regulation_id AS datenbasis_id,
	last_mod AS letzte_aenderung
FROM master.oblig_measd_mp;

ALTER TABLE master.proben_zusatz RENAME TO sample_specif;
ALTER TABLE master.sample_specif RENAME COLUMN meh_id TO unit_id;
ALTER TABLE master.sample_specif RENAME COLUMN beschreibung TO name;
ALTER TABLE master.sample_specif RENAME COLUMN zusatzwert TO ext_id;
ALTER TABLE master.sample_specif RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.proben_zusatz AS SELECT
	id,
	unit_id AS meh_id,
	name AS beschreibung,
	ext_id AS zusatzwert,
	eudf_keyword,
	last_mod AS letzte_aenderung
FROM master.sample_specif;

ALTER TABLE master.probenart RENAME TO sample_meth;
ALTER TABLE master.sample_meth RENAME COLUMN beschreibung TO name;
ALTER TABLE master.sample_meth RENAME COLUMN probenart TO ext_id;
ALTER TABLE master.sample_meth RENAME COLUMN probenart_eudf_id TO eudf_sample_meth_id;
ALTER TABLE master.sample_meth RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.probenart AS SELECT
	id,
	name AS beschreibung,
	ext_id AS probenart,
	eudf_sample_meth_id AS probenart_eudf_id,
	last_mod AS letzte_aenderung
FROM master.sample_meth;

ALTER TABLE master.probenehmer RENAME TO sampler;
ALTER TABLE master.sampler RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.sampler RENAME COLUMN prn_id TO sampler_ext_id;
ALTER TABLE master.sampler RENAME COLUMN bearbeiter TO editor;
ALTER TABLE master.sampler RENAME COLUMN bemerkung TO comm;
ALTER TABLE master.sampler RENAME COLUMN betrieb TO inst;
ALTER TABLE master.sampler RENAME COLUMN bezeichnung TO descr;
ALTER TABLE master.sampler RENAME COLUMN kurz_bezeichnung TO short_text;
ALTER TABLE master.sampler RENAME COLUMN ort TO city;
ALTER TABLE master.sampler RENAME COLUMN plz TO zip;
ALTER TABLE master.sampler RENAME COLUMN strasse TO street;
ALTER TABLE master.sampler RENAME COLUMN telefon TO phone;
ALTER TABLE master.sampler RENAME COLUMN tourenplan TO route_planning;
ALTER TABLE master.sampler RENAME COLUMN typ TO type;
ALTER TABLE master.sampler RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.probenehmer AS SELECT
	id,
	network_id AS netzbetreiber_id,
	sampler_ext_id AS prn_id,
	editor AS bearbeiter,
	comm AS bemerkung,
	inst AS betrieb,
	descr AS bezeichnung,
	short_text AS kurz_bezeichnung,
	city AS ort,
	zip AS plz,
	street AS strasse,
	phone AS telefon,
	route_planning AS tourenplan,
	type AS typ,
	last_mod AS letzte_aenderung
FROM master.sampler;

ALTER TABLE master.query_messstelle RENAME TO query_meas_facil_mp;
ALTER TABLE master.query_meas_facil_mp RENAME COLUMN mess_stelle TO meas_facil_id;
CREATE VIEW stamm.query_messstelle AS SELECT
	id,
	query,
	meas_facil_id AS mess_stelle
FROM master.query_meas_facil_mp;


ALTER TABLE master.query_user RENAME COLUMN user_id TO lada_user_id;
ALTER TABLE master.query_user RENAME COLUMN base_query TO base_query_id;
ALTER TABLE master.query_user RENAME COLUMN description TO descr;
CREATE VIEW stamm.query_user AS SELECT
	id,
	name,
	lada_user_id AS user_id,
	base_query_id AS base_query,
	descr AS description
FROM master.query_user;

ALTER TABLE master.rei_progpunkt RENAME TO rei_ag;
ALTER TABLE master.rei_ag RENAME COLUMN reiid TO name;
ALTER TABLE master.rei_ag RENAME COLUMN rei_prog_punkt TO descr;
ALTER TABLE master.rei_ag RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.rei_progpunkt AS SELECT
	id,
	name AS reiid,
	descr AS rei_prog_punkt,
	last_mod AS letzte_aenderung
FROM master.rei_ag;

ALTER TABLE master.rei_progpunkt_grp_umw_zuord RENAME TO rei_ag_gr_env_medium_mp;
ALTER TABLE master.rei_ag_gr_env_medium_mp RENAME COLUMN rei_progpunkt_grp_id TO rei_ag_gr_id;
ALTER TABLE master.rei_ag_gr_env_medium_mp RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.rei_ag_gr_env_medium_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.rei_progpunkt_grp_umw_zuord AS SELECT
	id,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	env_medium_id AS umw_id,
	last_mod AS letzte_aenderung
FROM master.rei_ag_gr_env_medium_mp;

ALTER TABLE master.rei_progpunkt_grp_zuord RENAME TO rei_ag_gr_mp;
ALTER TABLE master.rei_ag_gr_mp RENAME COLUMN rei_progpunkt_grp_id TO rei_ag_gr_id;
ALTER TABLE master.rei_ag_gr_mp RENAME COLUMN rei_progpunkt_id TO rei_ag_id;
ALTER TABLE master.rei_ag_gr_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.rei_progpunkt_grp_zuord AS SELECT
	id,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	rei_ag_id AS rei_progpunkt_id,
	last_mod AS letzte_aenderung
FROM master.rei_ag_gr_mp;

ALTER TABLE master.rei_progpunkt_gruppe RENAME TO rei_ag_gr;
ALTER TABLE master.rei_ag_gr RENAME COLUMN rei_prog_punkt_gruppe TO name;
ALTER TABLE master.rei_ag_gr RENAME COLUMN beschreibung TO descr;
ALTER TABLE master.rei_ag_gr RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.rei_progpunkt_gruppe AS SELECT
	id,
	name AS rei_prog_punkt_gruppe,
	descr AS beschreibung,
	last_mod AS letzte_aenderung
FROM master.rei_ag_gr;

ALTER TABLE master.result_type RENAME TO disp;
CREATE VIEW stamm.result_type AS SELECT
	id,
	name,
	format
FROM master.disp;

ALTER TABLE master.richtwert RENAME TO ref_val;
ALTER TABLE master.ref_val RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.ref_val RENAME COLUMN massnahme_id TO ref_val_meas_id;
ALTER TABLE master.ref_val RENAME COLUMN messgroessengruppe_id TO measd_gr_id;
ALTER TABLE master.ref_val RENAME COLUMN zusatztext TO specif;
ALTER TABLE master.ref_val RENAME COLUMN richtwert TO ref_val;
CREATE VIEW stamm.richtwert AS SELECT
	id,
	env_medium_id AS umw_id,
	ref_val_meas_id AS massnahme_id,
	measd_gr_id AS messgroessengruppe_id,
	specif AS zusatztext,
	ref_val AS richtwert
FROM master.ref_val;

ALTER TABLE master.richtwert_massnahme RENAME TO ref_val_measure;
ALTER TABLE master.ref_val_measure RENAME COLUMN massnahme TO measure;
ALTER TABLE master.ref_val_measure RENAME COLUMN beschreibung TO descr;
CREATE VIEW stamm.richtwert_massnahme AS SELECT
	id,
	measure AS massnahme,
	descr AS beschreibung
FROM master.ref_val_measure;

ALTER TABLE master.sollist_mmtgrp RENAME TO targ_act_mmt_gr;
ALTER TABLE master.targ_act_mmt_gr RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.targ_act_mmt_gr RENAME COLUMN beschreibung TO descr;
CREATE VIEW stamm.sollist_mmtgrp AS SELECT
	id,
	name AS bezeichnung,
	descr AS beschreibung
FROM master.targ_act_mmt_gr;

ALTER TABLE master.sollist_mmtgrp_zuord RENAME TO targ_act_mmt_gr_mp;
ALTER TABLE master.targ_act_mmt_gr_mp RENAME COLUMN sollist_mmtgrp_id TO targ_act_mmt_gr_id;
CREATE VIEW stamm.sollist_mmtgrp_zuord AS SELECT
	id,
	mmt_id,
	targ_act_mmt_gr_id AS sollist_mmtgrp_id
FROM master.targ_act_mmt_gr_mp;

ALTER TABLE master.sollist_soll RENAME TO targ_act_targ;
ALTER TABLE master.targ_act_targ RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.targ_act_targ RENAME COLUMN sollist_mmtgrp_id TO targ_act_mmt_gr_id;
ALTER TABLE master.targ_act_targ RENAME COLUMN sollist_umwgrp_id TO targ_env_medium_gr_id;
ALTER TABLE master.targ_act_targ RENAME COLUMN imp TO is_imp;
ALTER TABLE master.targ_act_targ RENAME COLUMN soll TO targ;
CREATE VIEW stamm.sollist_soll AS SELECT
	id,
	network_id AS netzbetreiber_id,
	targ_act_mmt_gr_id AS sollist_mmtgrp_id,
	targ_env_medium_gr_id AS sollist_umwgrp_id,
	is_imp AS imp,
	targ AS soll
FROM master.targ_act_targ;

ALTER TABLE master.sollist_umwgrp RENAME TO targ_env_gr;
ALTER TABLE master.targ_env_gr RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.targ_env_gr RENAME COLUMN beschreibung TO targ_env__gr_displ;
CREATE VIEW stamm.sollist_umwgrp AS SELECT
	id,
	name AS bezeichnung,
	targ_env__gr_displ AS beschreibung
FROM master.targ_env_gr;

ALTER TABLE master.sollist_umwgrp_zuord RENAME TO targ_env_gr_mp;
ALTER TABLE master.targ_env_gr_mp RENAME COLUMN sollist_umwgrp_id TO targ_env__gr_id;
ALTER TABLE master.targ_env_gr_mp RENAME COLUMN umw_id TO env_medium_id;
CREATE VIEW stamm.sollist_umwgrp_zuord AS SELECT
	id,
	targ_env__gr_id AS sollist_umwgrp_id,
	env_medium_id AS umw_id
FROM master.targ_env_gr_mp;

ALTER TABLE master.staat RENAME TO state;
ALTER TABLE master.state RENAME COLUMN staat TO ctry;
ALTER TABLE master.state RENAME COLUMN hkl_id TO ctry_orig_id;
ALTER TABLE master.state RENAME COLUMN staat_iso TO iso_3166;
ALTER TABLE master.state RENAME COLUMN staat_kurz TO int_veh_reg_code;
ALTER TABLE master.state RENAME COLUMN eu TO is_eu_country;
ALTER TABLE master.state RENAME COLUMN koord_x_extern TO x_coord_ext;
ALTER TABLE master.state RENAME COLUMN koord_y_extern TO y_coord_ext;
ALTER TABLE master.state RENAME COLUMN kda_id TO spat_ref_sys_id;
ALTER TABLE master.state RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.staat AS SELECT
	id,
	ctry AS staat,
	ctry_orig_id AS hkl_id,
	iso_3166 AS staat_iso,
	int_veh_reg_code AS staat_kurz,
	is_eu_country AS eu,
	x_coord_ext AS koord_x_extern,
	y_coord_ext AS koord_y_extern,
	spat_ref_sys_id AS kda_id,
	last_mod AS letzte_aenderung
FROM master.state;

ALTER TABLE master.status_erreichbar RENAME TO status_access_mp_view;
ALTER TABLE master.status_access_mp_view RENAME COLUMN wert_id TO status_val_id;
ALTER TABLE master.status_access_mp_view RENAME COLUMN stufe_id TO status_lev_id;
ALTER TABLE master.status_access_mp_view RENAME COLUMN cur_wert TO cur_val_id;
ALTER TABLE master.status_access_mp_view RENAME COLUMN cur_stufe TO cur_lev_id;
CREATE VIEW stamm.status_erreichbar AS SELECT
	id,
	status_val_id AS wert_id,
	status_lev_id AS stufe_id,
	cur_val_id AS cur_wert,
	cur_lev_id AS cur_stufe
FROM master.status_access_mp_view;

ALTER TABLE master.status_kombi RENAME TO status_mp;
ALTER TABLE master.status_mp RENAME COLUMN stufe_id TO status_lev_id;
ALTER TABLE master.status_mp RENAME COLUMN wert_id TO status_val_id;
CREATE VIEW stamm.status_kombi AS SELECT
	id,
	status_lev_id AS stufe_id,
	status_val_id AS wert_id
FROM master.status_mp;

ALTER TABLE master.status_reihenfolge RENAME TO status_ord_mp;
ALTER TABLE master.status_ord_mp RENAME COLUMN von_id TO from_id;
ALTER TABLE master.status_ord_mp RENAME COLUMN zu_id TO to_id;
CREATE VIEW stamm.status_reihenfolge AS SELECT
	id,
	from_id AS von_id,
	to_id AS zu_id
FROM master.status_ord_mp;

ALTER TABLE master.status_stufe RENAME TO status_lev;
ALTER TABLE master.status_lev RENAME COLUMN stufe TO lev;
CREATE VIEW stamm.status_stufe AS SELECT
	id,
	lev AS stufe
FROM master.status_lev;

ALTER TABLE master.status_wert RENAME TO status_val;
ALTER TABLE master.status_val RENAME COLUMN wert TO val;
CREATE VIEW stamm.status_wert AS SELECT
	id,
	val AS wert
FROM master.status_val;


ALTER TABLE master.tag RENAME COLUMN tag TO name;
ALTER TABLE master.tag RENAME COLUMN mst_id TO meas_facil_id;
ALTER TABLE master.tag RENAME COLUMN auto_tag TO is_auto_tag;
ALTER TABLE master.tag RENAME COLUMN netzbetreiber_id TO network_id;
ALTER TABLE master.tag RENAME COLUMN user_id TO lada_user_id;
ALTER TABLE master.tag RENAME COLUMN gueltig_bis TO val_until;
CREATE VIEW stamm.tag AS SELECT
	id,
	name AS tag,
	meas_facil_id AS mst_id,
	is_auto_tag AS auto_tag,
	network_id AS netzbetreiber_id,
	lada_user_id AS user_id,
	tag_typ,
	val_until AS gueltig_bis,
	created_at
FROM master.tag;

ALTER TABLE master.tag_typ RENAME TO tag_type;
ALTER TABLE master.tag_type RENAME COLUMN tagtyp TO tag_type;
CREATE VIEW stamm.tag_typ AS SELECT
	id,
	tag_type AS tagtyp
FROM master.tag_type;

ALTER TABLE master.tm_fm_umrechnung RENAME TO convers_dm_fm;
ALTER TABLE master.convers_dm_fm RENAME COLUMN meh_id TO unit_id;
ALTER TABLE master.convers_dm_fm RENAME COLUMN meh_id_nach TO to_unit_id ;
ALTER TABLE master.convers_dm_fm RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.convers_dm_fm RENAME COLUMN media_desk_pattern TO env_descrip_pattern;
ALTER TABLE master.convers_dm_fm RENAME COLUMN faktor TO conv_factor;
ALTER TABLE master.convers_dm_fm RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.tm_fm_umrechnung AS SELECT
	id,
	unit_id AS meh_id,
	to_unit_id  AS meh_id_nach,
	env_medium_id AS umw_id,
	env_descrip_pattern AS media_desk_pattern,
	conv_factor AS faktor,
	last_mod AS letzte_aenderung
FROM master.convers_dm_fm;

ALTER TABLE master.umwelt RENAME TO env_medium;
ALTER TABLE master.env_medium RENAME COLUMN beschreibung TO descr;
ALTER TABLE master.env_medium RENAME COLUMN umwelt_bereich TO name;
ALTER TABLE master.env_medium RENAME COLUMN meh_id TO unit_1;
ALTER TABLE master.env_medium RENAME COLUMN meh_id_2 TO unit_2;
ALTER TABLE master.env_medium RENAME COLUMN leitstelle TO coord_ofc;
ALTER TABLE master.env_medium RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.umwelt AS SELECT
	id,
	descr AS beschreibung,
	name AS umwelt_bereich,
	unit_1 AS meh_id,
	unit_2 AS meh_id_2,
	coord_ofc AS leitstelle,
	last_mod AS letzte_aenderung
FROM master.env_medium;

ALTER TABLE master.umwelt_zusatz RENAME TO env_specif_mp;
ALTER TABLE master.env_specif_mp RENAME COLUMN pzs_id TO sample_specif_id;
ALTER TABLE master.env_specif_mp RENAME COLUMN umw_id TO env_medium_id;
ALTER TABLE master.env_specif_mp RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.umwelt_zusatz AS SELECT
	id,
	sample_specif_id AS pzs_id,
	env_medium_id AS umw_id,
	last_mod AS letzte_aenderung
FROM master.env_specif_mp;

ALTER TABLE master.verwaltungseinheit RENAME TO admin_unit;
ALTER TABLE master.admin_unit RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.admin_unit RENAME COLUMN regbezirk TO gov_dist_id;
ALTER TABLE master.admin_unit RENAME COLUMN kreis TO rural_dist_id;
ALTER TABLE master.admin_unit RENAME COLUMN bundesland TO state_id;
ALTER TABLE master.admin_unit RENAME COLUMN is_gemeinde TO is_munic;
ALTER TABLE master.admin_unit RENAME COLUMN is_landkreis TO is_rural_dist;
ALTER TABLE master.admin_unit RENAME COLUMN is_regbezirk TO is_gov_dist;
ALTER TABLE master.admin_unit RENAME COLUMN is_bundesland TO is_state;
ALTER TABLE master.admin_unit RENAME COLUMN plz TO zip;
ALTER TABLE master.admin_unit RENAME COLUMN mittelpunkt TO geom_center;
CREATE VIEW stamm.verwaltungseinheit AS SELECT
	id,
	name AS bezeichnung,
	gov_dist_id AS regbezirk,
	rural_dist_id AS kreis,
	state_id AS bundesland,
	is_munic AS is_gemeinde,
	is_rural_dist AS is_landkreis,
	is_gov_dist AS is_regbezirk,
	is_state AS is_bundesland,
	zip AS plz,
	nuts,
	geom_center AS mittelpunkt
FROM master.admin_unit;

ALTER TABLE master.verwaltungsgrenze RENAME TO admin_border_view;
ALTER TABLE master.admin_border_view RENAME COLUMN gem_id TO munic_id;
ALTER TABLE master.admin_border_view RENAME COLUMN is_gemeinde TO is_munic;
CREATE VIEW stamm.verwaltungsgrenze AS SELECT
	id,
	munic_id AS gem_id,
	is_munic AS is_gemeinde,
	shape
FROM master.admin_border_view;

ALTER TABLE master.zeitbasis RENAME TO tz;
ALTER TABLE master.tz RENAME COLUMN bezeichnung TO name;
ALTER TABLE master.tz RENAME COLUMN letzte_aenderung TO last_mod;
CREATE VIEW stamm.zeitbasis AS SELECT
	id,
	name AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.tz;
