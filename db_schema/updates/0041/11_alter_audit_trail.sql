--change audit trail to reflect english dm changes
--This SQL file is based on output: I:\Daten\Rn1\LAF\scripts_de2en\audit_trail.py (author: sca-ber)

--DROP FUNCTION jsonb_rename_keys;    

\echo start 
SELECT NOW();

CREATE OR REPLACE FUNCTION jsonb_rename_keys(
	jdata JSONB,
	keys TEXT[]
)
RETURNS JSONB AS $$
DECLARE
	result JSONB;
	len INT;
	newkey TEXT;
	oldkey TEXT;
BEGIN
	len = array_length(keys, 1);
	IF len < 1 OR (len % 2) != 0 THEN
		RAISE EXCEPTION 'The length of keys must be even, such as {old1,new1,old2,new2,...}';
	END IF;
	result = jdata;
	FOR i IN 1..len BY 2 LOOP
		oldkey = keys[i];
		IF (jdata ? oldkey) THEN
			newkey = keys[i+1];
			result = (result - oldkey) || jsonb_build_object(newkey, result->oldkey);
		END IF;
	END LOOP;
	RETURN result;
END;
$$ LANGUAGE plpgsql;

--German to English
--keep out for now due to extensive time, German views are sufficient to keep audit_trail working
/*
UPDATE lada.audit_trail SET table_name='sample_specif_meas_val', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'pzs_id', 'sample_specif_id', 'messwert_pzs', 'meas_val', 'messfehler', 'meas_err', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'kleiner_als', 'smaller_than', 'nwg_zu_messwert', 'detect_lim']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'pzs_id', 'sample_specif_id', 'messwert_pzs', 'meas_val', 'messfehler', 'meas_err', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'kleiner_als', 'smaller_than', 'nwg_zu_messwert', 'detect_lim']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='zusatz_wert') as foo WHERE table_name='zusatz_wert';

UPDATE lada.audit_trail SET table_name='comm_measm', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'messungs_id', 'measm_id']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'messungs_id', 'measm_id']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='kommentar_m') as foo WHERE table_name='kommentar_m';

UPDATE lada.audit_trail SET table_name='comm_sample', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'probe_id', 'sample_id']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'probe_id', 'sample_id']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='kommentar_p') as foo WHERE table_name='kommentar_p';

UPDATE lada.audit_trail SET table_name='sample', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['test', 'is_test', 'mst_id', 'meas_facil_id', 'labor_mst_id', 'appr_lab_id', 'hauptproben_nr', 'main_sample_id', 'datenbasis_id', 'regulation_id', 'ba_id', 'opr_mode_id', 'probenart_id', 'sample_meth_id', 'media_desk', 'env_descrip_display', 'media', 'env_descrip_name', 'umw_id', 'env_medium_id', 'probeentnahme_beginn', 'sample_start_date', 'probeentnahme_ende', 'sample_end_date', 'mittelungsdauer', 'mid_sample_date', 'letzte_aenderung', 'last_mod', 'erzeuger_id', 'dataset_creator_id', 'probe_nehmer_id', 'sampler_id', 'mpl_id', 'state_mpg_id', 'mpr_id', 'mpg_id', 'solldatum_beginn', 'sched_start_date', 'solldatum_ende', 'sched_end_date', 'tree_modified', 'tree_mod', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'kta_gruppe_id', 'nucl_facil_gr_id', 'ursprungszeit', 'orig_date', 'mitte_sammelzeitraum', 'mid_coll_pd']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['test', 'is_test', 'mst_id', 'meas_facil_id', 'labor_mst_id', 'appr_lab_id', 'hauptproben_nr', 'main_sample_id', 'datenbasis_id', 'regulation_id', 'ba_id', 'opr_mode_id', 'probenart_id', 'sample_meth_id', 'media_desk', 'env_descrip_display', 'media', 'env_descrip_name', 'umw_id', 'env_medium_id', 'probeentnahme_beginn', 'sample_start_date', 'probeentnahme_ende', 'sample_end_date', 'mittelungsdauer', 'mid_sample_date', 'letzte_aenderung', 'last_mod', 'erzeuger_id', 'dataset_creator_id', 'probe_nehmer_id', 'sampler_id', 'mpl_id', 'state_mpg_id', 'mpr_id', 'mpg_id', 'solldatum_beginn', 'sched_start_date', 'solldatum_ende', 'sched_end_date', 'tree_modified', 'tree_mod', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'kta_gruppe_id', 'nucl_facil_gr_id', 'ursprungszeit', 'orig_date', 'mitte_sammelzeitraum', 'mid_coll_pd']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='probe') as foo WHERE table_name='probe';

UPDATE lada.audit_trail SET table_name='geolocat', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'ort_id', 'site_id', 'ortszuordnung_typ', 'type_regulation', 'ortszusatztext', 'add_site_text', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'oz_id', 'poi_id']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'ort_id', 'site_id', 'ortszuordnung_typ', 'type_regulation', 'ortszusatztext', 'add_site_text', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'oz_id', 'poi_id']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='ortszuordnung') as foo WHERE table_name='ortszuordnung';

UPDATE lada.audit_trail SET table_name='meas_val', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['messungs_id', 'measm_id', 'messgroesse_id', 'measd_id', 'messwert_nwg', 'less_than_LOD', 'messwert', 'meas_val', 'messfehler', 'error', 'nwg_zu_messwert', 'detect_lim', 'meh_id', 'unit_id', 'grenzwertueberschreitung', 'is_threshold', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['messungs_id', 'measm_id', 'messgroesse_id', 'measd_id', 'messwert_nwg', 'less_than_LOD', 'messwert', 'meas_val', 'messfehler', 'error', 'nwg_zu_messwert', 'detect_lim', 'meh_id', 'unit_id', 'grenzwertueberschreitung', 'is_threshold', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='messwert') as foo WHERE table_name='messwert';

UPDATE lada.audit_trail SET table_name='measm', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'nebenproben_nr', 'min_sample_id', 'messdauer', 'meas_pd', 'messzeitpunkt', 'measm_start_date', 'fertig', 'is_completed', 'letzte_aenderung', 'last_mod', 'geplant', 'is_scheduled', 'tree_modified', 'tree_mod']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'nebenproben_nr', 'min_sample_id', 'messdauer', 'meas_pd', 'messzeitpunkt', 'measm_start_date', 'fertig', 'is_completed', 'letzte_aenderung', 'last_mod', 'geplant', 'is_scheduled', 'tree_modified', 'tree_mod']) changed_fields_en 
FROM lada.audit_trail WHERE table_name='messung') as foo WHERE table_name='messung';

UPDATE master.audit_trail SET table_name='site', row_data=row_data_en, changed_fields=changed_fields_en 
FROM 
(SELECT jsonb_rename_keys(row_data, ARRAY['netzbetreiber_id', 'network_id', 'ort_id', 'ext_id', 'langtext', 'long_text', 'staat_id', 'state_id', 'gem_id', 'munic_id', 'unscharf', 'is_fuzzy', 'nuts_code', 'nuts_id', 'kda_id', 'spat_ref_sys_id', 'koord_x_extern', 'x_coord_ext', 'koord_y_extern', 'y_coord_ext', 'hoehe_land', 'alt', 'letzte_aenderung', 'last_mod', 'ort_typ', 'site_class_id', 'kurztext', 'short_text', 'berichtstext', 'rei_report_text', 'zone', 'rei_zone', 'sektor', 'rei_sector', 'zustaendigkeit', 'rei_competence', 'mp_art', 'rei_opr_mode', 'aktiv', 'is_rei_active', 'kta_gruppe_id', 'rei_nucl_facil_gr_id', 'oz_id', 'poi_id', 'hoehe_ueber_nn', 'height_asl', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'gem_unt_id', 'munic_div_id']) row_data_en
, jsonb_rename_keys(changed_fields, ARRAY['netzbetreiber_id', 'network_id', 'ort_id', 'ext_id', 'langtext', 'long_text', 'staat_id', 'state_id', 'gem_id', 'munic_id', 'unscharf', 'is_fuzzy', 'nuts_code', 'nuts_id', 'kda_id', 'spat_ref_sys_id', 'koord_x_extern', 'x_coord_ext', 'koord_y_extern', 'y_coord_ext', 'hoehe_land', 'alt', 'letzte_aenderung', 'last_mod', 'ort_typ', 'site_class_id', 'kurztext', 'short_text', 'berichtstext', 'rei_report_text', 'zone', 'rei_zone', 'sektor', 'rei_sector', 'zustaendigkeit', 'rei_competence', 'mp_art', 'rei_opr_mode', 'aktiv', 'is_rei_active', 'kta_gruppe_id', 'rei_nucl_facil_gr_id', 'oz_id', 'poi_id', 'hoehe_ueber_nn', 'height_asl', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'gem_unt_id', 'munic_div_id']) changed_fields_en 
FROM master.audit_trail WHERE table_name='ort') as foo WHERE table_name='ort';


CREATE OR REPLACE VIEW land.audit_trail
 AS
 SELECT audit_trail.id,
        CASE
            WHEN audit_trail.table_name::text = 'sample_specif_meas_val'::text THEN 'zusatz_wert'::character varying
            WHEN audit_trail.table_name::text = 'comm_measm'::text THEN 'kommentar_m'::character varying
			WHEN audit_trail.table_name::text = 'comm_sample'::text THEN 'kommentar_p'::character varying
			WHEN audit_trail.table_name::text = 'sample'::text THEN 'probe'::character varying
			WHEN audit_trail.table_name::text = 'geolocat'::text THEN 'ortszuordnung'::character varying
			WHEN audit_trail.table_name::text = 'meas_val'::text THEN 'messwert'::character varying
			WHEN audit_trail.table_name::text = 'measm'::text THEN ' messung'::character varying
            ELSE audit_trail.table_name
        END AS table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
        CASE
            WHEN audit_trail.table_name::text = 'sample_specif_meas_val'::text THEN jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'pzs_id', 'sample_specif_id', 'messwert_pzs', 'meas_val', 'messfehler', 'meas_err', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'kleiner_als', 'smaller_than', 'nwg_zu_messwert', 'detect_lim'])
			WHEN audit_trail.table_name::text = 'comm_measm'::text THEN jsonb_rename_keys(row_data, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'messungs_id', 'measm_id'])
			WHEN audit_trail.table_name::text = 'comm_sample'::text THEN jsonb_rename_keys(row_data, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'probe_id', 'sample_id'])
			WHEN audit_trail.table_name::text = 'sample'::text THEN jsonb_rename_keys(row_data, ARRAY['test', 'is_test', 'mst_id', 'meas_facil_id', 'labor_mst_id', 'appr_lab_id', 'hauptproben_nr', 'main_sample_id', 'datenbasis_id', 'regulation_id', 'ba_id', 'opr_mode_id', 'probenart_id', 'sample_meth_id', 'media_desk', 'env_descrip_display', 'media', 'env_descrip_name', 'umw_id', 'env_medium_id', 'probeentnahme_beginn', 'sample_start_date', 'probeentnahme_ende', 'sample_end_date', 'mittelungsdauer', 'mid_sample_date', 'letzte_aenderung', 'last_mod', 'erzeuger_id', 'dataset_creator_id', 'probe_nehmer_id', 'sampler_id', 'mpl_id', 'state_mpg_id', 'mpr_id', 'mpg_id', 'solldatum_beginn', 'sched_start_date', 'solldatum_ende', 'sched_end_date', 'tree_modified', 'tree_mod', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'kta_gruppe_id', 'nucl_facil_gr_id', 'ursprungszeit', 'orig_date', 'mitte_sammelzeitraum', 'mid_coll_pd'])
			WHEN audit_trail.table_name::text = 'geolocat'::text THEN jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'ort_id', 'site_id', 'ortszuordnung_typ', 'type_regulation', 'ortszusatztext', 'add_site_text', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'oz_id', 'poi_id'])
			WHEN audit_trail.table_name::text = 'meas_val'::text THEN jsonb_rename_keys(row_data, ARRAY['messungs_id', 'measm_id', 'messgroesse_id', 'measd_id', 'messwert_nwg', 'less_than_LOD', 'messwert', 'meas_val', 'messfehler', 'error', 'nwg_zu_messwert', 'detect_lim', 'meh_id', 'unit_id', 'grenzwertueberschreitung', 'is_threshold', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod'])
			WHEN audit_trail.table_name::text = 'measm'::text THEN jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'nebenproben_nr', 'min_sample_id', 'messdauer', 'meas_pd', 'messzeitpunkt', 'measm_start_date', 'fertig', 'is_completed', 'letzte_aenderung', 'last_mod', 'geplant', 'is_scheduled', 'tree_modified', 'tree_mod'])
            ELSE audit_trail.row_data
        END AS row_data,
		CASE
            WHEN audit_trail.table_name::text = 'sample_specif_meas_val'::text THEN jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'pzs_id', 'sample_specif_id', 'messwert_pzs', 'meas_val', 'messfehler', 'meas_err', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'kleiner_als', 'smaller_than', 'nwg_zu_messwert', 'detect_lim'])
			WHEN audit_trail.table_name::text = 'comm_measm'::text THEN jsonb_rename_keys(changed_fields, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'messungs_id', 'measm_id'])
			WHEN audit_trail.table_name::text = 'comm_sample'::text THEN jsonb_rename_keys(changed_fields, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'probe_id', 'sample_id'])
			WHEN audit_trail.table_name::text = 'sample'::text THEN jsonb_rename_keys(changed_fields, ARRAY['test', 'is_test', 'mst_id', 'meas_facil_id', 'labor_mst_id', 'appr_lab_id', 'hauptproben_nr', 'main_sample_id', 'datenbasis_id', 'regulation_id', 'ba_id', 'opr_mode_id', 'probenart_id', 'sample_meth_id', 'media_desk', 'env_descrip_display', 'media', 'env_descrip_name', 'umw_id', 'env_medium_id', 'probeentnahme_beginn', 'sample_start_date', 'probeentnahme_ende', 'sample_end_date', 'mittelungsdauer', 'mid_sample_date', 'letzte_aenderung', 'last_mod', 'erzeuger_id', 'dataset_creator_id', 'probe_nehmer_id', 'sampler_id', 'mpl_id', 'state_mpg_id', 'mpr_id', 'mpg_id', 'solldatum_beginn', 'sched_start_date', 'solldatum_ende', 'sched_end_date', 'tree_modified', 'tree_mod', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'kta_gruppe_id', 'nucl_facil_gr_id', 'ursprungszeit', 'orig_date', 'mitte_sammelzeitraum', 'mid_coll_pd'])
			WHEN audit_trail.table_name::text = 'geolocat'::text THEN jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'ort_id', 'site_id', 'ortszuordnung_typ', 'type_regulation', 'ortszusatztext', 'add_site_text', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'oz_id', 'poi_id'])
			WHEN audit_trail.table_name::text = 'meas_val'::text THEN jsonb_rename_keys(changed_fields, ARRAY['messungs_id', 'measm_id', 'messgroesse_id', 'measd_id', 'messwert_nwg', 'less_than_LOD', 'messwert', 'meas_val', 'messfehler', 'error', 'nwg_zu_messwert', 'detect_lim', 'meh_id', 'unit_id', 'grenzwertueberschreitung', 'is_threshold', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod'])
			WHEN audit_trail.table_name::text = 'measm'::text THEN jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'nebenproben_nr', 'min_sample_id', 'messdauer', 'meas_pd', 'messzeitpunkt', 'measm_start_date', 'fertig', 'is_completed', 'letzte_aenderung', 'last_mod', 'geplant', 'is_scheduled', 'tree_modified', 'tree_mod'])
            ELSE audit_trail.row_data
        END AS changed_fields
   FROM lada.audit_trail;

ALTER TABLE land.audit_trail
    OWNER TO lada;

CREATE OR REPLACE VIEW stamm.audit_trail
 AS
 SELECT audit_trail.id,
        CASE
            WHEN audit_trail.table_name::text = 'site'::text THEN 'ort'::character varying
        END AS table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
        CASE
            WHEN audit_trail.table_name::text = 'site'::text THEN jsonb_rename_keys(row_data, ARRAY['netzbetreiber_id', 'network_id', 'ort_id', 'ext_id', 'langtext', 'long_text', 'staat_id', 'state_id', 'gem_id', 'munic_id', 'unscharf', 'is_fuzzy', 'nuts_code', 'nuts_id', 'kda_id', 'spat_ref_sys_id', 'koord_x_extern', 'x_coord_ext', 'koord_y_extern', 'y_coord_ext', 'hoehe_land', 'alt', 'letzte_aenderung', 'last_mod', 'ort_typ', 'site_class_id', 'kurztext', 'short_text', 'berichtstext', 'rei_report_text', 'zone', 'rei_zone', 'sektor', 'rei_sector', 'zustaendigkeit', 'rei_competence', 'mp_art', 'rei_opr_mode', 'aktiv', 'is_rei_active', 'kta_gruppe_id', 'rei_nucl_facil_gr_id', 'oz_id', 'poi_id', 'hoehe_ueber_nn', 'height_asl', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'gem_unt_id', 'munic_div_id'])
        END AS row_data,
		CASE
            WHEN audit_trail.table_name::text = 'site'::text THEN jsonb_rename_keys(changed_fields, ARRAY['netzbetreiber_id', 'network_id', 'ort_id', 'ext_id', 'langtext', 'long_text', 'staat_id', 'state_id', 'gem_id', 'munic_id', 'unscharf', 'is_fuzzy', 'nuts_code', 'nuts_id', 'kda_id', 'spat_ref_sys_id', 'koord_x_extern', 'x_coord_ext', 'koord_y_extern', 'y_coord_ext', 'hoehe_land', 'alt', 'letzte_aenderung', 'last_mod', 'ort_typ', 'site_class_id', 'kurztext', 'short_text', 'berichtstext', 'rei_report_text', 'zone', 'rei_zone', 'sektor', 'rei_sector', 'zustaendigkeit', 'rei_competence', 'mp_art', 'rei_opr_mode', 'aktiv', 'is_rei_active', 'kta_gruppe_id', 'rei_nucl_facil_gr_id', 'oz_id', 'poi_id', 'hoehe_ueber_nn', 'height_asl', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'gem_unt_id', 'munic_div_id'])
			ELSE audit_trail.row_data
        END AS changed_fields
   FROM master.audit_trail;

ALTER TABLE stamm.audit_trail
    OWNER TO lada;

\echo finished 
SELECT NOW();
*/