--change audit trail to reflect english dm changes

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

DROP VIEW land.audit_trail;
CREATE VIEW land.audit_trail
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
			WHEN audit_trail.table_name::text = 'sample_specif_meas_val'::text THEN jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
            WHEN audit_trail.table_name::text = 'comm_measm'::text THEN jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
			WHEN audit_trail.table_name::text = 'comm_sample'::text THEN jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
			WHEN audit_trail.table_name::text = 'sample'::text THEN jsonb_rename_keys(row_data, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'state_mpg_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
			WHEN audit_trail.table_name::text = 'geolocat'::text THEN jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
			WHEN audit_trail.table_name::text = 'meas_val'::text THEN jsonb_rename_keys(row_data, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_LOD', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
			WHEN audit_trail.table_name::text = 'measm'::text THEN jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
            ELSE audit_trail.row_data
        END AS row_data,
		CASE
            WHEN audit_trail.table_name::text = 'sample_specif_meas_val'::text THEN jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
            WHEN audit_trail.table_name::text = 'comm_measm'::text THEN jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
			WHEN audit_trail.table_name::text = 'comm_sample'::text THEN jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
			WHEN audit_trail.table_name::text = 'sample'::text THEN jsonb_rename_keys(changed_fields, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'state_mpg_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
			WHEN audit_trail.table_name::text = 'geolocat'::text THEN jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
			WHEN audit_trail.table_name::text = 'meas_val'::text THEN jsonb_rename_keys(changed_fields, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_LOD', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
			WHEN audit_trail.table_name::text = 'measm'::text THEN jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
            ELSE audit_trail.changed_fields
        END AS changed_fields
   FROM lada.audit_trail;

ALTER TABLE land.audit_trail
    OWNER TO lada;

DROP VIEW stamm.audit_trail;
CREATE VIEW stamm.audit_trail
 AS
 SELECT audit_trail.id,
        CASE
            WHEN audit_trail.table_name::text = 'site'::text THEN 'ort'::character varying
        END AS table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
        CASE
            WHEN audit_trail.table_name::text = 'site'::text THEN jsonb_rename_keys(row_data, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'x_coord_ext', 'koord_x_extern', 'y_coord_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
        END AS row_data,
		CASE
            WHEN audit_trail.table_name::text = 'site'::text THEN jsonb_rename_keys(changed_fields, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'x_coord_ext', 'koord_x_extern', 'y_coord_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
			ELSE audit_trail.changed_fields
        END AS changed_fields
   FROM master.audit_trail;

ALTER TABLE stamm.audit_trail
    OWNER TO lada;
