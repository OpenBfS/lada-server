
\timing 
\echo zusatz_wert
UPDATE lada.audit_trail
SET table_name = 'sample_specif_meas_val',
  row_data = jsonb_rename_keys(row_data, ARRAY ['probe_id', 'sample_id', 'pzs_id', 'sample_specif_id', 'messwert_pzs', 'meas_val', 'messfehler', 'meas_err', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'kleiner_als', 'smaller_than', 'nwg_zu_messwert', 'detect_lim']),
  changed_fields = jsonb_rename_keys(changed_fields, ARRAY ['probe_id', 'sample_id', 'pzs_id', 'sample_specif_id', 'messwert_pzs', 'meas_val', 'messfehler', 'meas_err', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'kleiner_als', 'smaller_than', 'nwg_zu_messwert', 'detect_lim'])
WHERE table_name = 'zusatz_wert';

\echo kommentar_m
UPDATE lada.audit_trail 
SET table_name='comm_measm', 
row_data=jsonb_rename_keys(row_data, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'messungs_id', 'measm_id']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'messungs_id', 'measm_id'])
WHERE table_name='kommentar_m';

\echo komentar_p
UPDATE lada.audit_trail 
SET table_name='comm_sample',
row_data=jsonb_rename_keys(row_data, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'probe_id', 'sample_id']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['mst_id', 'meas_facil_id', 'datum', 'date', 'probe_id', 'sample_id'])
WHERE table_name='kommentar_p';

\echo probe
UPDATE lada.audit_trail
SET table_name='sample',
row_data=jsonb_rename_keys(row_data, ARRAY['test', 'is_test', 'mst_id', 'meas_facil_id', 'labor_mst_id', 'appr_lab_id', 'hauptproben_nr', 'main_sample_id', 'datenbasis_id', 'regulation_id', 'ba_id', 'opr_mode_id', 'probenart_id', 'sample_meth_id', 'media_desk', 'env_descrip_display', 'media', 'env_descrip_name', 'umw_id', 'env_medium_id', 'probeentnahme_beginn', 'sample_start_date', 'probeentnahme_ende', 'sample_end_date', 'mittelungsdauer', 'mid_sample_date', 'letzte_aenderung', 'last_mod', 'erzeuger_id', 'dataset_creator_id', 'probe_nehmer_id', 'sampler_id', 'mpl_id', 'state_mpg_id', 'mpr_id', 'mpg_id', 'solldatum_beginn', 'sched_start_date', 'solldatum_ende', 'sched_end_date', 'tree_modified', 'tree_mod', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'kta_gruppe_id', 'nucl_facil_gr_id', 'ursprungszeit', 'orig_date', 'mitte_sammelzeitraum', 'mid_coll_pd']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['test', 'is_test', 'mst_id', 'meas_facil_id', 'labor_mst_id', 'appr_lab_id', 'hauptproben_nr', 'main_sample_id', 'datenbasis_id', 'regulation_id', 'ba_id', 'opr_mode_id', 'probenart_id', 'sample_meth_id', 'media_desk', 'env_descrip_display', 'media', 'env_descrip_name', 'umw_id', 'env_medium_id', 'probeentnahme_beginn', 'sample_start_date', 'probeentnahme_ende', 'sample_end_date', 'mittelungsdauer', 'mid_sample_date', 'letzte_aenderung', 'last_mod', 'erzeuger_id', 'dataset_creator_id', 'probe_nehmer_id', 'sampler_id', 'mpl_id', 'state_mpg_id', 'mpr_id', 'mpg_id', 'solldatum_beginn', 'sched_start_date', 'solldatum_ende', 'sched_end_date', 'tree_modified', 'tree_mod', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'kta_gruppe_id', 'nucl_facil_gr_id', 'ursprungszeit', 'orig_date', 'mitte_sammelzeitraum', 'mid_coll_pd'])
WHERE table_name='probe';

\echo ortszuordnung
UPDATE lada.audit_trail
SET table_name='geolocat',
row_data=jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'ort_id', 'site_id', 'ortszuordnung_typ', 'type_regulation', 'ortszusatztext', 'add_site_text', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'oz_id', 'poi_id']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'ort_id', 'site_id', 'ortszuordnung_typ', 'type_regulation', 'ortszusatztext', 'add_site_text', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod', 'oz_id', 'poi_id'])
WHERE table_name='ortszuordnung';

\echo messwert
UPDATE lada.audit_trail
SET table_name='meas_val',
row_data=jsonb_rename_keys(row_data, ARRAY['messungs_id', 'measm_id', 'messgroesse_id', 'measd_id', 'messwert_nwg', 'less_than_LOD', 'messwert', 'meas_val', 'messfehler', 'error', 'nwg_zu_messwert', 'detect_lim', 'meh_id', 'unit_id', 'grenzwertueberschreitung', 'is_threshold', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['messungs_id', 'measm_id', 'messgroesse_id', 'measd_id', 'messwert_nwg', 'less_than_LOD', 'messwert', 'meas_val', 'messfehler', 'error', 'nwg_zu_messwert', 'detect_lim', 'meh_id', 'unit_id', 'grenzwertueberschreitung', 'is_threshold', 'letzte_aenderung', 'last_mod', 'tree_modified', 'tree_mod'])
WHERE table_name='messwert';

\echo messung
UPDATE lada.audit_trail
SET table_name='measm',
row_data=jsonb_rename_keys(row_data, ARRAY['probe_id', 'sample_id', 'nebenproben_nr', 'min_sample_id', 'messdauer', 'meas_pd', 'messzeitpunkt', 'measm_start_date', 'fertig', 'is_completed', 'letzte_aenderung', 'last_mod', 'geplant', 'is_scheduled', 'tree_modified', 'tree_mod']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['probe_id', 'sample_id', 'nebenproben_nr', 'min_sample_id', 'messdauer', 'meas_pd', 'messzeitpunkt', 'measm_start_date', 'fertig', 'is_completed', 'letzte_aenderung', 'last_mod', 'geplant', 'is_scheduled', 'tree_modified', 'tree_mod'])
WHERE table_name='messung';

\echo ort
UPDATE master.audit_trail
SET table_name='site',
row_data=jsonb_rename_keys(row_data, ARRAY['netzbetreiber_id', 'network_id', 'ort_id', 'ext_id', 'langtext', 'long_text', 'staat_id', 'state_id', 'gem_id', 'munic_id', 'unscharf', 'is_fuzzy', 'nuts_code', 'nuts_id', 'kda_id', 'spat_ref_sys_id', 'koord_x_extern', 'x_coord_ext', 'koord_y_extern', 'y_coord_ext', 'hoehe_land', 'alt', 'letzte_aenderung', 'last_mod', 'ort_typ', 'site_class_id', 'kurztext', 'short_text', 'berichtstext', 'rei_report_text', 'zone', 'rei_zone', 'sektor', 'rei_sector', 'zustaendigkeit', 'rei_competence', 'mp_art', 'rei_opr_mode', 'aktiv', 'is_rei_active', 'kta_gruppe_id', 'rei_nucl_facil_gr_id', 'oz_id', 'poi_id', 'hoehe_ueber_nn', 'height_asl', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'gem_unt_id', 'munic_div_id']),
changed_fields=jsonb_rename_keys(changed_fields, ARRAY['netzbetreiber_id', 'network_id', 'ort_id', 'ext_id', 'langtext', 'long_text', 'staat_id', 'state_id', 'gem_id', 'munic_id', 'unscharf', 'is_fuzzy', 'nuts_code', 'nuts_id', 'kda_id', 'spat_ref_sys_id', 'koord_x_extern', 'x_coord_ext', 'koord_y_extern', 'y_coord_ext', 'hoehe_land', 'alt', 'letzte_aenderung', 'last_mod', 'ort_typ', 'site_class_id', 'kurztext', 'short_text', 'berichtstext', 'rei_report_text', 'zone', 'rei_zone', 'sektor', 'rei_sector', 'zustaendigkeit', 'rei_competence', 'mp_art', 'rei_opr_mode', 'aktiv', 'is_rei_active', 'kta_gruppe_id', 'rei_nucl_facil_gr_id', 'oz_id', 'poi_id', 'hoehe_ueber_nn', 'height_asl', 'rei_progpunkt_grp_id', 'rei_ag_gr_id', 'gem_unt_id', 'munic_div_id'])
WHERE table_name='ort';

\timing