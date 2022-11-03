SET role lada;

ALTER TABLE lada.comm_measm RENAME CONSTRAINT kommentar_m_pkey TO comm_measm_pkey;
ALTER TABLE lada.comm_measm RENAME CONSTRAINT kommentar_m_mst_id_fkey TO comm_measm_meas_facil_id_fkey;
ALTER TABLE lada.comm_measm RENAME CONSTRAINT kommentar_m_messungs_id_fkey TO comm_measm_measm_id_fkey;
ALTER TABLE lada.comm_sample RENAME CONSTRAINT kommentar_p_pkey TO comm_sample_pkey;
ALTER TABLE lada.comm_sample RENAME CONSTRAINT kommentar_p_mst_id_fkey TO comm_sample_meas_facil_id_fkey;
ALTER TABLE lada.comm_sample RENAME CONSTRAINT kommentar_p_probe_id_fkey TO comm_sample_sample_id_fkey;
ALTER TABLE lada.geolocat RENAME CONSTRAINT ortszuordnung_pkey TO geolocat_pkey;
ALTER TABLE lada.geolocat RENAME CONSTRAINT ortszuordnung_probe_id_excl TO geolocat_sample_id_excl;
ALTER TABLE lada.geolocat RENAME CONSTRAINT ortszuordnung_probe_id_fkey TO geolocat_sample_id_fkey;
ALTER TABLE lada.geolocat RENAME CONSTRAINT ortszuordnung_ort_id_fkey TO geolocat_site_id_fkey;
ALTER TABLE lada.geolocat RENAME CONSTRAINT ortszuordnung_ortszuordnung_typ_fkey TO geolocat_type_regulation_fkey;
ALTER TABLE lada.geolocat RENAME CONSTRAINT ortszuordnung_oz_id_fkey TO geolocat_poi_id_fkey;
ALTER TABLE lada.geolocat_mpg RENAME CONSTRAINT ortszuordnung_mp_pkey TO geolocat_mpg_pkey;
ALTER TABLE lada.geolocat_mpg RENAME CONSTRAINT ortszuordnung_mp_messprogramm_id_excl TO geolocat_mpg_mpg_id_excl;
ALTER TABLE lada.geolocat_mpg RENAME CONSTRAINT ortszuordnung_mp_messprogramm_id_fkey TO geolocat_mpg_mpg_id_fkey;
ALTER TABLE lada.geolocat_mpg RENAME CONSTRAINT ortszuordnung_mp_ort_id_fkey TO geolocat_mpg_site_id_fkey;
ALTER TABLE lada.geolocat_mpg RENAME CONSTRAINT ortszuordnung_mp_ortszuordnung_typ_fkey TO geolocat_mpg_type_regulation_fkey;
ALTER TABLE lada.geolocat_mpg RENAME CONSTRAINT ortszuordnung_mp_oz_id_fkey TO geolocat_mpg_poi_id_fkey;
ALTER TABLE lada.meas_val RENAME CONSTRAINT messwert_pkey TO meas_val_pkey;
ALTER TABLE lada.meas_val RENAME CONSTRAINT messwert_messungs_id_messgroesse_id_key TO meas_val_measm_id_measd_id_key;
ALTER TABLE lada.meas_val RENAME CONSTRAINT messwert_messungs_id_fkey TO meas_val_measm_id_fkey;
ALTER TABLE lada.meas_val RENAME CONSTRAINT messwert_messgroesse_id_fkey TO meas_val_measd_id_fkey;
ALTER TABLE lada.meas_val RENAME CONSTRAINT messwert_meh_id_fkey TO meas_val_unit_id_fkey;
ALTER TABLE lada.measm RENAME CONSTRAINT messung_pkey TO measm_pkey;
ALTER TABLE lada.measm RENAME CONSTRAINT messung_id_ext_id_key TO measm_id_sample_id_key;
ALTER TABLE lada.measm RENAME CONSTRAINT messung_id_nebenproben_nr_key TO measm_id_min_sample_id_key;
ALTER TABLE lada.measm RENAME CONSTRAINT messung_probe_id_fkey TO measm_sample_id_fkey;
ALTER TABLE lada.measm RENAME CONSTRAINT messung_mmt_id_fkey TO measm_mmt_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_media_desk_check TO mpg_env_descrip_id_check;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_gueltig_von_check TO mpg_valid_start_date_check;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_gueltig_bis_check TO mpg_valid_end_date_check;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_check TO mpg_check;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_check1 TO mpg_check1;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_pkey TO mpg_pkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_mst_id_fkey TO mpg_meas_facil_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_labor_mst_id_fkey TO mpg_appr_lab_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_datenbasis_id_fkey TO mpg_regulation_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_ba_id_fkey TO mpg_opr_mode_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_gem_id_fkey TO mpg_munic_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_umw_id_fkey TO mpg_env_medium_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_probenart_id_fkey TO mpg_sample_meth_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_probe_nehmer_id_fkey TO mpg_sampler_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_mpl_id_fkey TO mpg_state_mpg_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_rei_progpunkt_grp_id_fkey TO mpg_rei_ag_gr_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_kta_gruppe_id_fkey TO mpg_nucl_facil_gr_id_fkey;
ALTER TABLE lada.mpg RENAME CONSTRAINT messprogramm_meh_id_fkey TO mpg_unit_id_fkey;
ALTER TABLE lada.mpg_mmt_measd_mp RENAME CONSTRAINT messprogramm_mmt_messgroesse_pkey TO mpg_mmt_measd_mp_pkey;
ALTER TABLE lada.mpg_mmt_measd_mp RENAME CONSTRAINT messprogramm_mmt_messgroesse_messprogramm_mmt_id_fkey TO mpg_mmt_measd_mp_mpg_mmt_id_fkey;
ALTER TABLE lada.mpg_mmt_measd_mp RENAME CONSTRAINT messprogramm_mmt_messgroesse_messgroesse_id_fkey TO mpg_mmt_measd_mp_measd_id_fkey;
ALTER TABLE lada.mpg_mmt_mp RENAME CONSTRAINT messprogramm_mmt_pkey TO mpg_mmt_mp_pkey;
ALTER TABLE lada.mpg_mmt_mp RENAME CONSTRAINT messprogramm_mmt_messprogramm_id_fkey TO mpg_mmt_mp_mpg_id_fkey;
ALTER TABLE lada.mpg_mmt_mp RENAME CONSTRAINT messprogramm_mmt_mmt_id_fkey TO mpg_mmt_mp_mmt_id_fkey;
ALTER TABLE lada.mpg_sample_specif RENAME CONSTRAINT messprogramm_proben_zusatz_pkey TO mpg_sample_specif_pkey;
ALTER TABLE lada.mpg_sample_specif RENAME CONSTRAINT messprogramm_proben_zusatz_proben_zusatz_id_fkey TO mpg_sample_specif_sample_specif_id_fkey;
ALTER TABLE lada.mpg_sample_specif RENAME CONSTRAINT messprogramm_proben_zusatz_messprogramm_id_fkey TO mpg_sample_specif_mpg_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_media_desk_check TO sample_env_descrip_display_check;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_check TO sample_check;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_pkey TO sample_pkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_ext_id_key TO sample_ext_id_key;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_test_mst_id_hauptproben_nr_key TO sample_is_test_meas_facil_id_main_sample_id_key;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_mst_id_fkey TO sample_meas_facil_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_labor_mst_id_fkey TO sample_appr_lab_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_datenbasis_id_fkey TO sample_regulation_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_ba_id_fkey TO sample_opr_mode_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_probenart_id_fkey TO sample_sample_meth_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_umw_id_fkey TO sample_env_medium_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_erzeuger_id_fkey TO sample_dataset_creator_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_probe_nehmer_id_fkey TO sample_sampler_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_mpl_id_fkey TO sample_state_mpg_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_mpr_id_fkey TO sample_mpg_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_rei_progpunkt_grp_id_fkey TO sample_rei_ag_gr_id_fkey;
ALTER TABLE lada.sample RENAME CONSTRAINT probe_kta_gruppe_id_fkey TO sample_nucl_facil_gr_id_fkey;
ALTER TABLE lada.sample_specif_meas_val RENAME CONSTRAINT zusatz_wert_pkey TO sample_specif_meas_val_pkey;
ALTER TABLE lada.sample_specif_meas_val RENAME CONSTRAINT zusatz_wert_probe_id_pzs_id_key TO sample_specif_meas_val_sample_id_sample_specif_id_key;
ALTER TABLE lada.sample_specif_meas_val RENAME CONSTRAINT zusatz_wert_probe_id_fkey TO sample_specif_meas_val_sample_id_fkey;
ALTER TABLE lada.sample_specif_meas_val RENAME CONSTRAINT zusatz_wert_pzs_id_fkey TO sample_specif_meas_val_sample_specif_id_fkey;
ALTER TABLE lada.status_prot RENAME CONSTRAINT status_protokoll_pkey TO status_prot_pkey;
ALTER TABLE lada.status_prot RENAME CONSTRAINT status_protokoll_mst_id_fkey TO status_prot_meas_facil_id_fkey;
ALTER TABLE lada.status_prot RENAME CONSTRAINT status_protokoll_messungs_id_fkey TO status_prot_measm_id_fkey;
ALTER TABLE lada.status_prot RENAME CONSTRAINT status_protokoll_status_kombi_fkey TO status_prot_status_comb_fkey;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_check TO tag_link_check;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_pkey TO tag_link_pkey;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_probe_id_tag_id_key TO tag_link_sample_id_tag_id_key;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_messung_id_tag_id_key TO tag_link_measm_id_tag_id_key;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_probe_id_fkey TO tag_link_sample_id_fkey;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_messung_id_fkey TO tag_link_measm_id_fkey;
ALTER TABLE lada.tag_link RENAME CONSTRAINT tagzuordnung_tag_id_fkey TO tag_link_tag_id_fkey;
ALTER TABLE master.admin_unit RENAME CONSTRAINT verwaltungseinheit_pkey TO admin_unit_pkey;
ALTER TABLE master.auth RENAME CONSTRAINT auth_netzbetreiber_id_fkey TO auth_network_id_fkey;
ALTER TABLE master.auth RENAME CONSTRAINT auth_mst_id_fkey TO auth_meas_facil_id_fkey;
ALTER TABLE master.auth RENAME CONSTRAINT auth_labor_mst_id_fkey TO auth_appr_lab_id_fkey;
ALTER TABLE master.auth RENAME CONSTRAINT auth_funktion_id_fkey TO auth_auth_funct_id_fkey;
ALTER TABLE master.auth_coord_ofc_env_medium_mp RENAME CONSTRAINT auth_lst_umw_pkey TO auth_coord_ofc_env_medium_mp_pkey;
ALTER TABLE master.auth_coord_ofc_env_medium_mp RENAME CONSTRAINT auth_lst_umw_mst_id_fkey TO auth_coord_ofc_env_medium_mp_meas_facil_id_fkey;
ALTER TABLE master.auth_coord_ofc_env_medium_mp RENAME CONSTRAINT auth_lst_umw_umw_id_fkey TO auth_coord_ofc_env_medium_mp_env_medium_id_fkey;
ALTER TABLE master.auth_funct RENAME CONSTRAINT auth_funktion_pkey TO auth_funct_pkey;
ALTER TABLE master.auth_funct RENAME CONSTRAINT auth_funktion_funktion_key TO auth_funct_funct_key;
ALTER TABLE master.convers_dm_fm RENAME CONSTRAINT tm_fm_umrechnung_pkey TO convers_dm_fm_pkey;
ALTER TABLE master.convers_dm_fm RENAME CONSTRAINT tm_fm_umrechnung_meh_id_fkey TO convers_dm_fm_unit_id_fkey;
ALTER TABLE master.convers_dm_fm RENAME CONSTRAINT tm_fm_umrechnung_meh_id_nach_fkey TO convers_dm_fm_to_unit_id_fkey;
ALTER TABLE master.convers_dm_fm RENAME CONSTRAINT tm_fm_umrechnung_umw_id_fkey TO convers_dm_fm_env_medium_id_fkey;
ALTER TABLE master.dataset_creator RENAME CONSTRAINT datensatz_erzeuger_pkey TO dataset_creator_pkey;
ALTER TABLE master.dataset_creator RENAME CONSTRAINT datensatz_erzeuger_datensatz_erzeuger_id_netzbetreiber_id_m_key TO dataset_creator_ext_id_network_id_meas_facil_id_key;
ALTER TABLE master.dataset_creator RENAME CONSTRAINT datensatz_erzeuger_netzbetreiber_id_fkey TO dataset_creator_network_id_fkey;
ALTER TABLE master.dataset_creator RENAME CONSTRAINT datensatz_erzeuger_mst_id_fkey TO dataset_creator_meas_facil_id_fkey;
ALTER TABLE master.disp RENAME CONSTRAINT result_type_pkey TO disp_pkey;
ALTER TABLE master.env_descrip RENAME CONSTRAINT deskriptoren_pkey TO env_descrip_pkey;
ALTER TABLE master.env_descrip RENAME CONSTRAINT deskriptoren_vorgaenger_fkey TO env_descrip_pred_id_fkey;
ALTER TABLE master.env_descrip_env_medium_mp RENAME CONSTRAINT deskriptor_umwelt_pkey TO env_descrip_env_medium_mp_pkey;
ALTER TABLE master.env_descrip_env_medium_mp RENAME CONSTRAINT deskriptor_umwelt_umw_id_fkey TO env_descrip_env_medium_mp_env_medium_id_fkey;
ALTER TABLE master.env_medium RENAME CONSTRAINT umwelt_pkey TO env_medium_pkey;
ALTER TABLE master.env_medium RENAME CONSTRAINT umwelt_umwelt_bereich_key TO env_medium_name_key;
ALTER TABLE master.env_medium RENAME CONSTRAINT umwelt_meh_id_fkey TO env_medium_unit_1_fkey;
ALTER TABLE master.env_medium RENAME CONSTRAINT umwelt_meh_id_2_fkey TO env_medium_unit_2_fkey;
ALTER TABLE master.env_medium RENAME CONSTRAINT umwelt_leitstelle_fkey TO env_medium_coord_ofc_fkey;
ALTER TABLE master.env_specif_mp RENAME CONSTRAINT umwelt_zusatz_pkey TO env_specif_mp_pkey;
ALTER TABLE master.env_specif_mp RENAME CONSTRAINT umwelt_zusatz_pzs_id_umw_id_key TO env_specif_mp_sample_specif_id_env_medium_id_key;
ALTER TABLE master.env_specif_mp RENAME CONSTRAINT umwelt_zusatz_pzs_id_fkey TO env_specif_mp_sample_specif_id_fkey;
ALTER TABLE master.env_specif_mp RENAME CONSTRAINT umwelt_zusatz_umw_id_fkey TO env_specif_mp_env_medium_id_fkey;
ALTER TABLE master.grid_col_conf RENAME CONSTRAINT grid_column_values_pkey TO grid_col_conf_pkey;
ALTER TABLE master.grid_col_conf RENAME CONSTRAINT grid_column_values_user_id_fkey TO grid_col_conf_user_id_fkey;
ALTER TABLE master.grid_col_conf RENAME CONSTRAINT grid_column_values_grid_column_fkey TO grid_col_conf_grid_col_mp_id_fkey;
ALTER TABLE master.grid_col_conf RENAME CONSTRAINT grid_column_values_query_user_fkey TO grid_col_conf_query_user_id_fkey;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_position_check TO grid_col_mp_position_check;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_pkey TO grid_col_mp_pkey;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_base_query_name_key TO grid_col_mp_base_query_grid_col_key;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_base_query_data_index_key TO grid_col_mp_base_query_data_index_key;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_base_query_position_key TO grid_col_mp_base_query_position_key;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_base_query_fkey TO grid_col_mp_base_query_fkey;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_filter_fkey TO grid_col_mp_filter_fkey;
ALTER TABLE master.grid_col_mp RENAME CONSTRAINT grid_column_data_type_fkey TO grid_col_mp_data_type_fkey;
ALTER TABLE master.import_conf RENAME CONSTRAINT importer_config_action_check TO import_conf_action_check;
ALTER TABLE master.import_conf RENAME CONSTRAINT importer_config_pkey TO import_conf_pkey;
ALTER TABLE master.import_conf RENAME CONSTRAINT importer_config_mst_id_fkey TO import_conf_meas_facil_id_fkey;
ALTER TABLE master.meas_facil RENAME CONSTRAINT mess_stelle_pkey TO meas_facil_pkey;
ALTER TABLE master.meas_facil RENAME CONSTRAINT mess_stelle_netzbetreiber_id_fkey TO meas_facil_network_id_fkey;
ALTER TABLE master.meas_unit RENAME CONSTRAINT mess_einheit_pkey TO meas_unit_pkey;
ALTER TABLE master.measd RENAME CONSTRAINT messgroesse_pkey TO measd_pkey;
ALTER TABLE master.measd_gr RENAME CONSTRAINT messgroessen_gruppe_pkey TO measd_gr_pkey;
ALTER TABLE master.measd_gr_mp RENAME CONSTRAINT mg_grp_messgroessengruppe_id_fkey TO measd_gr_mp_measd_gr_id_fkey;
ALTER TABLE master.measd_gr_mp RENAME CONSTRAINT mg_grp_messgroesse_id_fkey TO measd_gr_mp_measd_id_fkey;
ALTER TABLE master.mmt RENAME CONSTRAINT mess_methode_pkey TO mmt_pkey;
ALTER TABLE master.mmt_measd_gr_mp RENAME CONSTRAINT mmt_messgroesse_grp_messgroessengruppe_id_fkey TO mmt_measd_gr_mp_measd_gr_id_fkey;
ALTER TABLE master.mmt_measd_gr_mp RENAME CONSTRAINT mmt_messgroesse_grp_mmt_id_fkey TO mmt_measd_gr_mp_mmt_id_fkey;
ALTER TABLE master.mpg_categ RENAME CONSTRAINT messprogramm_kategorie_pkey TO mpg_categ_pkey;
ALTER TABLE master.mpg_categ RENAME CONSTRAINT messprogramm_kategorie_code_netzbetreiber_id_key TO mpg_categ_ext_id_network_id_key;
ALTER TABLE master.mpg_categ RENAME CONSTRAINT messprogramm_kategorie_netzbetreiber_id_fkey TO mpg_categ_network_id_fkey;
ALTER TABLE master.mpg_transf RENAME CONSTRAINT messprogramm_transfer_pkey TO mpg_transf_pkey;
ALTER TABLE master.mpg_transf RENAME CONSTRAINT messprogramm_transfer_messprogramm_s_key TO mpg_transf_ext_id_key;
ALTER TABLE master.mpg_transf RENAME CONSTRAINT messprogramm_transfer_ba_id_fkey TO mpg_transf_opr_mode_id_fkey;
ALTER TABLE master.mpg_transf RENAME CONSTRAINT messprogramm_transfer_datenbasis_id_fkey TO mpg_transf_regulation_id_fkey;
ALTER TABLE master.munic_div RENAME CONSTRAINT gemeindeuntergliederung_pkey TO munic_div_pkey;
ALTER TABLE master.munic_div RENAME CONSTRAINT gemeindeuntergliederung_netzbetreiber_id_fkey TO munic_div_network_id_fkey;
ALTER TABLE master.munic_div RENAME CONSTRAINT gemeindeuntergliederung_gem_id_fkey TO munic_div_munic_id_fkey;
ALTER TABLE master.network RENAME CONSTRAINT netz_betreiber_pkey TO network_pkey;
ALTER TABLE master.nucl_facil_gr RENAME CONSTRAINT kta_gruppe_pkey TO nucl_facil_gr_pkey;
ALTER TABLE master.nucl_facil_gr_mp RENAME CONSTRAINT kta_grp_zuord_pkey TO nucl_facil_gr_mp_pkey;
ALTER TABLE master.nucl_facil_gr_mp RENAME CONSTRAINT kta_grp_zuord_kta_grp_id_fkey TO nucl_facil_gr_mp_nucl_facil_gr_id_fkey;
ALTER TABLE master.nucl_facil_gr_mp RENAME CONSTRAINT kta_grp_zuord_kta_id_fkey TO nucl_facil_gr_mp_nucl_facil_id_fkey;
ALTER TABLE master.oblig_measd_mp RENAME CONSTRAINT pflicht_messgroesse_pkey TO oblig_measd_mp_pkey;
ALTER TABLE master.oblig_measd_mp RENAME CONSTRAINT pflicht_messgroesse_messgroesse_id_fkey TO oblig_measd_mp_measd_id_fkey;
ALTER TABLE master.oblig_measd_mp RENAME CONSTRAINT pflicht_messgroesse_mmt_id_fkey TO oblig_measd_mp_mmt_id_fkey;
ALTER TABLE master.oblig_measd_mp RENAME CONSTRAINT pflicht_messgroesse_umw_id_fkey TO oblig_measd_mp_env_medium_id_fkey;
ALTER TABLE master.oblig_measd_mp RENAME CONSTRAINT pflicht_messgroesse_datenbasis_id_fkey TO oblig_measd_mp_regulation_id_fkey;
ALTER TABLE master.opr_mode RENAME CONSTRAINT betriebsart_pkey TO opr_mode_pkey;
ALTER TABLE master.poi RENAME CONSTRAINT ortszusatz_pkey TO poi_pkey;
ALTER TABLE master.query_meas_facil_mp RENAME CONSTRAINT query_messstelle_pkey TO query_meas_facil_mp_pkey;
ALTER TABLE master.query_meas_facil_mp RENAME CONSTRAINT query_messstelle_query_fkey TO query_meas_facil_mp_query_fkey;
ALTER TABLE master.query_meas_facil_mp RENAME CONSTRAINT query_messstelle_mess_stelle_fkey TO query_meas_facil_mp_meas_facil_id_fkey;
ALTER TABLE master.query_user RENAME CONSTRAINT query_user_user_id_fkey TO query_user_lada_user_id_fkey;
ALTER TABLE master.query_user RENAME CONSTRAINT query_user_base_query_fkey TO query_user_base_query_id_fkey;
ALTER TABLE master.ref_val RENAME CONSTRAINT richtwert_messgroessengruppe_id_fkey TO name_messgroessengruppe_id_fkey;
ALTER TABLE master.ref_val RENAME CONSTRAINT richtwert_umw_id_fkey TO name_umw_id_fkey;
ALTER TABLE master.ref_val RENAME CONSTRAINT richtwert_pkey TO ref_val_pkey;
ALTER TABLE master.ref_val RENAME CONSTRAINT richtwert_massnahme_id_fkey TO name_massnahme_id_fkey;
ALTER TABLE master.ref_val_measure RENAME CONSTRAINT richtwert_massnahme_pkey TO ref_val_measure_pkey;
ALTER TABLE master.regulation RENAME CONSTRAINT datenbasis_pkey TO regulation_pkey;
ALTER TABLE master.rei_ag RENAME CONSTRAINT rei_progpunkt_pkey TO rei_ag_pkey;
ALTER TABLE master.rei_ag_gr RENAME CONSTRAINT rei_progpunkt_gruppe_pkey TO rei_ag_gr_pkey;
ALTER TABLE master.rei_ag_gr_env_medium_mp RENAME CONSTRAINT rei_progpunkt_grp_umw_zuord_pkey TO rei_ag_gr_env_medium_mp_pkey;
ALTER TABLE master.rei_ag_gr_env_medium_mp RENAME CONSTRAINT rei_progpunkt_grp_umw_zuord_rei_progpunkt_grp_id_fkey TO rei_ag_gr_env_medium_mp_rei_ag_gr_id_fkey;
ALTER TABLE master.rei_ag_gr_env_medium_mp RENAME CONSTRAINT rei_progpunkt_grp_umw_zuord_umw_id_fkey TO rei_ag_gr_env_medium_mp_env_medium_id_fkey;
ALTER TABLE master.rei_ag_gr_mp RENAME CONSTRAINT rei_progpunkt_grp_zuord_pkey TO rei_ag_gr_mp_pkey;
ALTER TABLE master.rei_ag_gr_mp RENAME CONSTRAINT rei_progpunkt_grp_zuord_rei_progpunkt_grp_id_fkey TO rei_ag_gr_mp_rei_ag_gr_id_fkey;
ALTER TABLE master.rei_ag_gr_mp RENAME CONSTRAINT rei_progpunkt_grp_zuord_rei_progpunkt_id_fkey TO rei_ag_gr_mp_rei_ag_id_fkey;
ALTER TABLE master.sample_meth RENAME CONSTRAINT probenart_pkey TO sample_meth_pkey;
ALTER TABLE master.sample_specif RENAME CONSTRAINT proben_zusatz_pkey TO sample_specif_pkey;
ALTER TABLE master.sample_specif RENAME CONSTRAINT proben_zusatz_eudf_keyword_key TO sample_specif_eudf_keyword_key;
ALTER TABLE master.sample_specif RENAME CONSTRAINT proben_zusatz_meh_id_fkey TO sample_specif_unit_id_fkey;
ALTER TABLE master.sampler RENAME CONSTRAINT probenehmer_pkey TO sampler_pkey;
ALTER TABLE master.sampler RENAME CONSTRAINT probenehmer_prn_id_netzbetreiber_id_key TO sampler_ext_id_network_id_key;
ALTER TABLE master.sampler RENAME CONSTRAINT probenehmer_netzbetreiber_id_fkey TO sampler_network_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_pkey TO site_pkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_ort_id_netzbetreiber_id_key TO site_ext_id_network_id_key;
ALTER TABLE master.site RENAME CONSTRAINT ort_netzbetreiber_id_fkey TO site_network_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_staat_id_fkey TO site_state_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_gem_id_fkey TO site_munic_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_kda_id_fkey TO site_spat_ref_sys_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_ort_typ_fkey TO site_site_class_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_kta_gruppe_id_fkey TO site_rei_nucl_facil_gr_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_oz_id_fkey TO site_poi_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_rei_progpunkt_grp_id_fkey TO site_rei_ag_gr_id_fkey;
ALTER TABLE master.site RENAME CONSTRAINT ort_gem_unt_id_fkey TO site_munic_div_id_fkey;
ALTER TABLE master.site_class RENAME CONSTRAINT ort_typ_pkey TO site_class_pkey;
ALTER TABLE master.spat_ref_sys RENAME CONSTRAINT koordinaten_art_pkey TO spat_ref_sys_pkey;
ALTER TABLE master.state RENAME CONSTRAINT staat_pkey TO state_pkey;
ALTER TABLE master.state RENAME CONSTRAINT staat_staat_key TO state_ctry_key;
ALTER TABLE master.state RENAME CONSTRAINT staat_hkl_id_key TO state_ctry_orig_id_key;
ALTER TABLE master.state RENAME CONSTRAINT staat_staat_iso_key TO state_iso_3166_key;
ALTER TABLE master.state RENAME CONSTRAINT staat_staat_kurz_key TO state_int_veh_reg_code_key;
ALTER TABLE master.state RENAME CONSTRAINT staat_kda_id_fkey TO state_spat_ref_sys_id_fkey;
ALTER TABLE master.status_lev RENAME CONSTRAINT status_stufe_pkey TO status_lev_pkey;
ALTER TABLE master.status_lev RENAME CONSTRAINT status_stufe_stufe_key TO status_lev_lev_key;
ALTER TABLE master.status_mp RENAME CONSTRAINT status_kombi_pkey TO status_mp_pkey;
ALTER TABLE master.status_mp RENAME CONSTRAINT status_kombi_stufe_id_wert_id_key TO status_mp_status_lev_id_status_val_id_key;
ALTER TABLE master.status_mp RENAME CONSTRAINT status_kombi_stufe_id_fkey TO status_mp_status_lev_id_fkey;
ALTER TABLE master.status_mp RENAME CONSTRAINT status_kombi_wert_id_fkey TO status_mp_status_val_id_fkey;
ALTER TABLE master.status_ord_mp RENAME CONSTRAINT status_reihenfolge_pkey TO status_ord_mp_pkey;
ALTER TABLE master.status_ord_mp RENAME CONSTRAINT status_reihenfolge_von_id_zu_id_key TO status_ord_mp_from_id_to_id_key;
ALTER TABLE master.status_ord_mp RENAME CONSTRAINT status_reihenfolge_von_id_fkey TO status_ord_mp_from_id_fkey;
ALTER TABLE master.status_ord_mp RENAME CONSTRAINT status_reihenfolge_zu_id_fkey TO status_ord_mp_to_id_fkey;
ALTER TABLE master.status_val RENAME CONSTRAINT status_wert_pkey TO status_val_pkey;
ALTER TABLE master.status_val RENAME CONSTRAINT status_wert_wert_key TO status_val_val_key;
ALTER TABLE master.tag RENAME CONSTRAINT tag_tag_netzbetreiber_id_mst_id_key TO tag_name_network_id_meas_facil_id_key;
ALTER TABLE master.tag RENAME CONSTRAINT tag_mst_id_fkey TO tag_meas_facil_id_fkey;
ALTER TABLE master.tag RENAME CONSTRAINT tag_netzbetreiber_id_fkey TO tag_network_id_fkey;
ALTER TABLE master.tag RENAME CONSTRAINT tag_user_id_fkey TO tag_lada_user_id_fkey;
ALTER TABLE master.tag RENAME CONSTRAINT tag_tag_typ_fkey TO tag_tag_type_fkey;
ALTER TABLE master.tag_type RENAME CONSTRAINT tag_typ_pkey TO tag_type_pkey;
ALTER TABLE master.targ_act_mmt_gr RENAME CONSTRAINT sollist_mmtgrp_pkey TO targ_act_mmt_gr_pkey;
ALTER TABLE master.targ_act_mmt_gr_mp RENAME CONSTRAINT sollist_mmtgrp_zuord_pkey TO targ_act_mmt_gr_mp_pkey;
ALTER TABLE master.targ_act_targ RENAME CONSTRAINT sollist_soll_pkey TO targ_act_targ_pkey;
ALTER TABLE master.targ_env_gr RENAME CONSTRAINT sollist_umwgrp_pkey TO targ_env_gr_pkey;
ALTER TABLE master.targ_env_gr_mp RENAME CONSTRAINT sollist_umwgrp_zuord_pkey TO targ_env_gr_mp_pkey;
ALTER TABLE master.type_regulation RENAME CONSTRAINT ortszuordnung_typ_pkey TO type_regulation_pkey;
ALTER TABLE master.tz RENAME CONSTRAINT zeitbasis_pkey TO tz_pkey;
ALTER TABLE master.unit_convers RENAME CONSTRAINT mass_einheit_umrechnung_pkey TO unit_convers_pkey;
ALTER TABLE master.unit_convers RENAME CONSTRAINT mass_einheit_umrechnung_meh_id_von_meh_id_zu_key TO unit_convers_from_unit_id_to_unit_id_key;
ALTER TABLE master.unit_convers RENAME CONSTRAINT mass_einheit_umrechnung_meh_id_von_fkey TO unit_convers_from_unit_id_fkey;
ALTER TABLE master.unit_convers RENAME CONSTRAINT mass_einheit_umrechnung_meh_id_zu_fkey TO unit_convers_to_unit_id_fkey;

