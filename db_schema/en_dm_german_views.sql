--START TRANSACTION;

CREATE VIEW land.audit_trail AS SELECT
	id,
	table_name,
	tstamp,
	action,
	object_id,
	row_data,
	changed_fields
FROM lada.audit_trail;

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

CREATE VIEW land.kommentar_m AS SELECT
	id,
	meas_facil_id AS mst_id,
	date AS datum,
	text,
	measm_id AS messungs_id
FROM lada.comm_measm;

CREATE VIEW land.kommentar_p AS SELECT
	id,
	meas_facil_id AS mst_id,
	date AS datum,
	text,
	sample_id AS probe_id
FROM lada.comm_sample;

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

CREATE VIEW land.messprogramm_mmt AS SELECT
	id,
	mpg_id AS messprogramm_id,
	mmt_id,
	last_mod AS letzte_aenderung
FROM lada.mpg_mmt_mp;

CREATE VIEW land.messprogramm_mmt_messgroesse AS SELECT
	measd_id AS messgroesse_id
FROM lada.mpg_mmt_measd_mp;
/*
CREATE VIEW land.messprogramm_mmt_view AS SELECT
	id,
	network_id AS netzbetreiber_id,
	mpg_id AS messprogramm_id,
	mmt_id,
	measd AS messgroessen,
	last_mod AS letzte_aenderung
FROM lada.mpg_mmt_mp_view;
*/
CREATE VIEW land.messprogramm_proben_zusatz AS SELECT
	mpg_id AS messprogramm_id
FROM lada.mpg_sample_specif;
/*
CREATE VIEW land.messprogramm_view AS SELECT
	id,
	network_id AS netzbetreiber_id,
	comm_mpg_view AS kommentar,
	is_test AS test,
	is_active AS aktiv,
	meas_facil_id AS mst_id,
	appr_lab_id AS labor_mst_id,
	regulation_id AS datenbasis_id,
	opr_mode_id AS ba_id,
	env_descrip_id AS media_desk,
	env_medium_id AS umw_id,
	sample_meth_id AS probenart_id,
	sample_pd AS probenintervall,
	sample_pd_start_date AS teilintervall_von,
	sample_pd_end_date AS teilintervall_bis,
	sample_pd_offset AS intervall_offset,
	valid_start_date AS gueltig_von,
	valid_end_date AS gueltig_bis,
	sampleer_id AS probe_nehmer_id,
	state_mpg_id AS mpl_id,
	comm_sample AS probe_kommentar,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	nucl_facil_gr_id AS kta_gruppe_id,
	unit_id AS meh_id,
	sample_quant AS probenahmemenge,
	last_mod AS letzte_aenderung
FROM lada.mpg_view;
*/
CREATE VIEW land.messung AS SELECT
	id,
	sample_ext_id AS ext_id,
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

CREATE VIEW land.messwert AS SELECT
	id,
	measm_id AS messungs_id,
	measd_id AS messgroesse_id,
	less_than_LOD AS messwert_nwg,
	meas_val AS messwert,
	meas_err AS messfehler,
	detect_lim AS nwg_zu_messwert,
	unit_id AS meh_id,
	is_threshold AS grenzwertueberschreitung,
	last_mod AS letzte_aenderung,
	tree_mod AS tree_modified
FROM lada.meas_val;

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

/*
CREATE VIEW land.ortszuordnung_mp_view AS SELECT
	id,
	network_id AS netzbetreiber_id,
	mpg_id AS messprogramm_id,
	site_id AS ort_id,
	type_regulation AS ortszuordnung_typ,
	add_site_text AS ortszusatztext,
	last_mod AS letzte_aenderung
FROM lada.geolocat_mpg_view;
*/

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

CREATE VIEW land.rueckfrage_messung AS SELECT
	measm_id AS messungs_id
FROM lada.query_measm_view;

CREATE VIEW land.status_protokoll AS SELECT
	id,
	meas_facil_id AS mst_id,
	date AS datum,
	text,
	measm_id AS messungs_id,
	status_comb AS status_kombi,
	tree_mod AS tree_modified
FROM lada.status_prot;

CREATE VIEW land.tagzuordnung AS SELECT
	id,
	sample_id AS probe_id,
	tag_id,
	date AS datum,
	measm_id AS messung_id
FROM lada.tag_link;

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

CREATE VIEW stamm.audit_trail AS SELECT
	id,
	table_name,
	tstamp,
	action,
	object_id,
	row_data,
	changed_fields
FROM master.audit_trail;

CREATE VIEW stamm.audit_trail_ort AS SELECT
	id,
	table_name,
	tstamp AS tstamp,
	action,
	object_id,
	row_data,
	changed_fields,
	site_id AS ort_id
FROM master.audit_trail_site_view;

CREATE VIEW stamm.auth AS SELECT
	id,
	ldap_gr AS ldap_group,
	network_id AS netzbetreiber_id,
	meas_facil_id AS mst_id,
	appr_lab_id AS labor_mst_id,
	auth_funct_id AS funktion_id,
	last_mod AS letzte_aenderung
FROM master.auth;

CREATE VIEW stamm.auth_funktion AS SELECT
	id,
	funct AS funktion,
	last_mod AS letzte_aenderung
FROM master.auth_funct;

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

CREATE VIEW stamm.betriebsart AS SELECT
	id,
	name,
	last_mod AS letzte_aenderung
FROM master.opr_mode;

CREATE VIEW stamm.datenbasis AS SELECT
	id,
	descr AS beschreibung,
	regulation AS datenbasis,
	last_mod AS letzte_aenderung
FROM master.regulation;

CREATE VIEW stamm.datensatz_erzeuger AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS datensatz_erzeuger_id,
	meas_facil_id AS mst_id,
	descr AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.dataset_creator;

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
/*
CREATE VIEW stamm.dom AS SELECT
	dom_unit,
	durat AS duration,
	durat_unit AS duration_unit,
	env_medium AS envmedium,
	dom_text,
	delay,
	durat_text AS duration_text,
	measd_idf_nucl_mp_id AS idf_nuclide
FROM master.idf_dom;

CREATE VIEW stamm.envmedium AS SELECT
	text AS envmedium_text
FROM master.idf_env_medium;
*/
CREATE VIEW stamm.filter AS SELECT
	id,
	sql,
	param AS parameter,
	type,
	name
FROM master.filter;

CREATE VIEW stamm.filter_type AS SELECT
	id,
	type,
	is_multiselect AS multiselect
FROM master.filter_type;

CREATE VIEW stamm.gemeindeuntergliederung AS SELECT
	id,
	network_id AS netzbetreiber_id,
	munic_id AS gem_id,
	site_ext_id AS ozk_id,
	name AS gemeindeuntergliederung,
	last_mod AS letzte_aenderung
FROM master.munic_div;

CREATE VIEW stamm.grid_column AS SELECT
	id,
	base_query,
	grid_col AS name,
	data_index,
	position,
	filter,
	data_type
FROM master.grid_col_mp;

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
/*
CREATE VIEW stamm.idf_country AS SELECT
	name AS idf_country,
	idf_country_ger AS idf_country_ge
FROM master.idf_country;

CREATE VIEW stamm.idf_lada_translation AS SELECT
	id,
	dom,
	env_medium_id AS umw_id,
	mmt_id,
	meas_facil_id AS mst_id,
	val_status_comb AS validated_status_kombi,
	factor AS faktor,
	unit_id AS meh_id
FROM master.idf_lada_mp;

CREATE VIEW stamm.idf_network AS SELECT
	name AS network
FROM master.idf_grid_oper;
*/
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

CREATE VIEW stamm.koordinaten_art AS SELECT
	id,
	name AS koordinatenart,
	idf_geo_key,
	last_mod AS letzte_aenderung
FROM master.spat_ref_sys;

CREATE VIEW stamm.kta AS SELECT
	id,
	ext_id AS code,
	name AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.nucl_facil;

CREATE VIEW stamm.kta_grp_zuord AS SELECT
	id,
	nucl_facil_gr_id AS kta_grp_id,
	nucl_facil_id AS kta_id,
	last_mod AS letzte_aenderung
FROM master.nucl_facil_gr_mp;

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

CREATE VIEW stamm.mass_einheit_umrechnung AS SELECT
	id,
	from_unit_id AS meh_id_von,
	to_unit_id  AS meh_id_zu,
	factor AS faktor,
	last_mod AS letzte_aenderung
FROM master.unit_convers;

CREATE VIEW stamm.mess_einheit AS SELECT
	id,
	name AS beschreibung,
	unit_symbol AS einheit,
	eudf_unit_id AS eudf_messeinheit_id,
	eudf_convers_factor AS umrechnungs_faktor_eudf,
	last_mod AS letzte_aenderung
FROM master.meas_unit;

CREATE VIEW stamm.mess_methode AS SELECT
	id,
	descr AS beschreibung,
	name AS messmethode,
	last_mod AS letzte_aenderung
FROM master.mmt;

CREATE VIEW stamm.mess_stelle AS SELECT
	id,
	network_id AS netzbetreiber_id,
	address AS beschreibung,
	name AS mess_stelle,
	meas_facil_type AS mst_typ,
	trunk_code AS amtskennung,
	last_mod AS letzte_aenderung
FROM master.meas_facil;

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

CREATE VIEW stamm.messgroessen_gruppe AS SELECT
	id,
	name AS bezeichnung,
	ref_nucl_gr AS ist_leitnuklidgruppe,
	last_mod AS letzte_aenderung
FROM master.measd_gr;

CREATE VIEW stamm.messprogramm_kategorie AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS code,
	name AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.mpg_categ;

CREATE VIEW stamm.messprogramm_transfer AS SELECT
	id,
	ext_id AS messprogramm_s,
	name AS messprogramm_c,
	opr_mode_id AS ba_id,
	regulation_id AS datenbasis_id
FROM master.mpg_transf;

CREATE VIEW stamm.mg_grp AS SELECT
	measd_gr_id AS messgroessengruppe_id,
	measd_id AS messgroesse_id,
	last_mod AS letzte_aenderung
FROM master.measd_gr_mp;

CREATE VIEW stamm.mmt_messgroesse AS SELECT
	mmt_id,
	measd_id AS messgroesse_id
FROM master.mmt_measd_view;

CREATE VIEW stamm.mmt_messgroesse_grp AS SELECT
	measd_gr_id AS messgroessengruppe_id,
	mmt_id,
	last_mod AS letzte_aenderung
FROM master.mmt_measd_gr_mp;

CREATE VIEW stamm.netz_betreiber AS SELECT
	id,
	name AS netzbetreiber,
	idf_network_id AS idf_netzbetreiber,
	is_fmn AS is_bmn,
	mail_list AS mailverteiler,
	is_active AS aktiv,
	last_mod AS letzte_aenderung
FROM master.network;
/*
CREATE VIEW stamm.nuclide AS SELECT
	measd AS nuclide,
	text AS nuclide_text,
	idf_ext_id AS idf_nuklid_key
FROM master.measd_idf_nucl_mp;

CREATE VIEW stamm.nuts AS SELECT
	state_id AS staat_id,
	name AS bezeichnung,
	spat_ref_sys_id AS kda_id,
	x_coord_ext AS koord_x_extern,
	y_coord_ext AS koord_y_extern
FROM master.nuts;
*/
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
/*
CREATE VIEW stamm.ort_backup AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS ort_id,
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
	nucl_facil_gr_id AS kta_gruppe_id,
	poi_id AS oz_id,
	height_asl AS hoehe_ueber_nn,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	munic_div_id AS gem_unt_id
FROM master.site_bak;

CREATE VIEW stamm.ort_backup_mgrs AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS ort_id,
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
	nucl_facil_gr_id AS kta_gruppe_id,
	poi_id AS oz_id,
	height_asl AS hoehe_ueber_nn,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	munic_div_id AS gem_unt_id
FROM master.site_bak_mgrs;

CREATE VIEW stamm.ort_bb AS SELECT
	site_class_id AS ort_typ,
	ext_id AS ort_id,
	short_text AS kurztext,
	long_text AS langtext
FROM master.site_bb;

CREATE VIEW stamm.ort_bsh AS SELECT
	appr_lab_id AS mst_labor,
	network AS netzbetreiber,
	ctry AS staat,
	x_coord_ext AS x_koordinate,
	y_coord_ext AS y_koordinate,
	spat_ref_sys AS koordinatenart,
	site_class_ext_id AS ortsklassifizierung,
	site_ext_id AS ort_id,
	short_text AS kurztext,
	long_text AS langtext
FROM master.site_bsh;

CREATE VIEW stamm.ort_th AS SELECT
	descr AS bezeichnung,
	munic_id AS gem_id,
	meas_facil_id AS mst,
	gk_north AS hw,
	gk_east AS rw,
	poi AS ortszusatz,
	wgs_dez_north AS geo_h_w,
	wgs_dez_east AS geo_r_w,
	utm_east AS utm_ost,
	utm_north AS utm_nord,
	utm_32_east AS utm_32_ost,
	utm_32_north AS utm_32_nord
FROM master.site_th;
*/
CREATE VIEW stamm.ort_typ AS SELECT
	id,
	name AS ort_typ,
	ext_id AS code,
	last_mod AS letzte_aenderung
FROM master.site_class;
/*
CREATE VIEW stamm.ort_view AS SELECT
	id,
	network_id AS netzbetreiber_id,
	ext_id AS ort_id,
	long_text AS langtext,
	ctry_id AS staat_id,
	munic_id AS gem_id,
	is_fuzzy AS unscharf,
	nuts_id AS nuts_code,
	spat_ref_sys_id AS kda_id,
	x_coord_ext AS koord_x_extern,
	y_coord_ext AS koord_y_extern,
	alt AS hoehe_land,
	last_mod AS letzte_aenderung,
	site_class AS ort_typ,
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
FROM master.site_view;
*/
CREATE VIEW stamm.ortszuordnung_typ AS SELECT
	id,
	name AS ortstyp,
	last_mod AS letzte_aenderung
FROM master.type_regulation;

CREATE VIEW stamm.ortszusatz AS SELECT
	id as ozs_id,
	name AS ortszusatz,
	last_mod AS letzte_aenderung
FROM master.poi;

CREATE VIEW stamm.pflicht_messgroesse AS SELECT
	id,
	measd_id AS messgroesse_id,
	mmt_id,
	env_medium_id AS umw_id,
	regulation_id AS datenbasis_id,
	last_mod AS letzte_aenderung
FROM master.oblig_measd_mp;

CREATE VIEW stamm.proben_zusatz AS SELECT
	id,
	unit_id AS meh_id,
	name AS beschreibung,
	ext_id AS zusatzwert,
	eudf_keyword,
	last_mod AS letzte_aenderung
FROM master.sample_specif;

CREATE VIEW stamm.probenart AS SELECT
	id,
	name AS beschreibung,
	ext_id AS probenart,
	eudf_sample_meth_id AS probenart_eudf_id,
	last_mod AS letzte_aenderung
FROM master.sample_meth;

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

CREATE VIEW stamm.query_messstelle AS SELECT
	id,
	query,
	meas_facil_id AS mess_stelle
FROM master.query_meas_facil_mp;
/*
CREATE VIEW stamm.query_messstelle_bak AS SELECT
	id,
	query,
	meas_facil_id AS mess_stelle
FROM master.query_meas_facil_mp_bak;
*/
CREATE VIEW stamm.query_user AS SELECT
	id,
	name,
	lada_user_id AS user_id,
	base_query_id AS base_query,
	descr AS description
FROM master.query_user;

CREATE VIEW stamm.rei_progpunkt AS SELECT
	id,
	name AS reiid,
	descr AS rei_prog_punkt,
	last_mod AS letzte_aenderung
FROM master.rei_ag;

CREATE VIEW stamm.rei_progpunkt_grp_umw_zuord AS SELECT
	id,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	env_medium_id AS umw_id,
	last_mod AS letzte_aenderung
FROM master.rei_ag_gr_env_medium_mp;

CREATE VIEW stamm.rei_progpunkt_grp_zuord AS SELECT
	id,
	rei_ag_gr_id AS rei_progpunkt_grp_id,
	rei_ag_id AS rei_progpunkt_id,
	last_mod AS letzte_aenderung
FROM master.rei_ag_gr_mp;

CREATE VIEW stamm.rei_progpunkt_gruppe AS SELECT
	id,
	name AS rei_prog_punkt_gruppe,
	descr AS beschreibung,
	last_mod AS letzte_aenderung
FROM master.rei_ag_gr;

CREATE VIEW stamm.result_type AS SELECT
	id,
	name,
	format
FROM master.disp;

CREATE VIEW stamm.richtwert AS SELECT
	id,
	env_medium_id AS umw_id,
	ref_val_meas_id AS massnahme_id,
	measd_gr_id AS messgroessengruppe_id,
	specif AS zusatztext,
	ref_val AS richtwert
FROM master.ref_val;

CREATE VIEW stamm.richtwert_massnahme AS SELECT
	id,
	measure AS massnahme,
	descr AS beschreibung
FROM master.ref_val_measure;

CREATE VIEW stamm.sollist_mmtgrp AS SELECT
	id,
	name AS bezeichnung,
	descr AS beschreibung
FROM master.targ_act_mmt_gr;

CREATE VIEW stamm.sollist_mmtgrp_zuord AS SELECT
	id,
	mmt_id,
	targ_act_mmt_gr_id AS sollist_mmtgrp_id
FROM master.targ_act_mmt_gr_mp;

CREATE VIEW stamm.sollist_soll AS SELECT
	id,
	network_id AS netzbetreiber_id,
	targ_act_mmt_gr_id AS sollist_mmtgrp_id,
	targ_env_medium_gr_id AS sollist_umwgrp_id,
	is_imp AS imp,
	targ AS soll
FROM master.targ_act_targ;

CREATE VIEW stamm.sollist_umwgrp AS SELECT
	id,
	name AS bezeichnung,
	targ_env__gr_displ AS beschreibung
FROM master.targ_env_gr;

CREATE VIEW stamm.sollist_umwgrp_zuord AS SELECT
	id,
	targ_env__gr_id AS sollist_umwgrp_id,
	env_medium_id AS umw_id
FROM master.targ_env_gr_mp;

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

CREATE VIEW stamm.status_erreichbar AS SELECT
	id,
	status_val_id AS wert_id,
	status_lev_id AS stufe_id,
	cur_val_id AS cur_wert,
	cur_lev_id AS cur_stufe
FROM master.status_access_mp_view;

CREATE VIEW stamm.status_kombi AS SELECT
	id,
	status_lev_id AS stufe_id,
	status_val_id AS wert_id
FROM master.status_mp;
/*
CREATE VIEW stamm.status_kombi_view AS SELECT
	status_comb AS kombi
FROM master.status_mp_view;
*/
CREATE VIEW stamm.status_reihenfolge AS SELECT
	id,
	from_id AS von_id,
	to_id AS zu_id
FROM master.status_ord_mp;

CREATE VIEW stamm.status_stufe AS SELECT
	id,
	lev AS stufe
FROM master.status_lev;

CREATE VIEW stamm.status_wert AS SELECT
	id,
	val AS wert
FROM master.status_val;

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

CREATE VIEW stamm.tag_typ AS SELECT
	id,
	tag_type AS tagtyp
FROM master.tag_type;

CREATE VIEW stamm.tm_fm_umrechnung AS SELECT
	id,
	unit_id AS meh_id,
	to_unit_id  AS meh_id_nach,
	env_medium_id AS umw_id,
	env_descrip_pattern AS media_desk_pattern,
	conv_factor AS faktor,
	last_mod AS letzte_aenderung
FROM master.convers_dm_fm;

CREATE VIEW stamm.umwelt AS SELECT
	id,
	descr AS beschreibung,
	name AS umwelt_bereich,
	unit_1 AS meh_id,
	unit_2 AS meh_id_2,
	coord_ofc AS leitstelle,
	last_mod AS letzte_aenderung
FROM master.env_medium;

CREATE VIEW stamm.umwelt_zusatz AS SELECT
	id,
	sample_specif_id AS pzs_id,
	env_medium_id AS umw_id,
	last_mod AS letzte_aenderung
FROM master.env_specif_mp;
/*
CREATE VIEW stamm.unit AS SELECT
	unit_symbol AS unit,
	name AS unit_text,
	eudf_unit_id AS unit_eudf
FROM master.meas_unit_dom;

CREATE VIEW stamm.user_overview AS SELECT
	meas_facil AS mess_stelle,
	meas_facil_addr AS beschreibung,
	appr_lab_id AS labor_mst_id,
	function AS funktion,
	ldap_gr_uid AS ldap_group
FROM master.user_overv_view;
*/
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
/*
CREATE VIEW stamm.verwaltungseinheit_view AS SELECT
	id,
	admin_unit AS bezeichnung,
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
FROM master.admin_unit_view;

CREATE VIEW stamm.verwaltungsgrenze AS SELECT
	id,
	munic_id AS gem_id,
	is_munic AS is_gemeinde,
	shape
FROM master.admin_border_view;
*/
CREATE VIEW stamm.verwaltungsgrenze AS
 SELECT id,
       munic_id AS gem_id,
       is_munic AS is_gemeinde,
       shape
  FROM master.admin_border_view;

CREATE VIEW stamm.zeitbasis AS SELECT
	id,
	name AS bezeichnung,
	last_mod AS letzte_aenderung
FROM master.tz;

--NEW
CREATE VIEW public.lada_messwert AS
 SELECT meas_val.id,
    meas_val.measm_id AS messungs_id,
    meas_val.measd_id AS messgroesse_id,
    meas_val.less_than_LOD AS messwert_nwg,
    meas_val.meas_val AS messwert,
    meas_val.meas_err AS messfehler,
    meas_val.detect_lim AS nwg_zu_messwert,
    meas_val.unit_id AS meh_id,
    meas_val.is_threshold AS grenzwertueberschreitung,
    status_prot.status_comb AS status_kombi,
    meas_val.last_mod AS letzte_aenderung
   FROM ((lada.meas_val
     JOIN lada.measm ON ((meas_val.measm_id = measm.id)))
     JOIN lada.status_prot ON (((measm.status = status_prot.id) AND (status_prot.status_comb <> 1))));
