SET role lada;

ALTER SEQUENCE IF EXISTS lada.kommentar_m_id_seq RENAME TO comm_measm_id_seq;
ALTER SEQUENCE IF EXISTS lada.kommentar_p_id_seq RENAME TO comm_sample_id_seq;
ALTER SEQUENCE IF EXISTS lada.messprogramm_id_seq RENAME TO mpg_id_seq;
ALTER SEQUENCE IF EXISTS lada.messprogramm_mmt_id_seq RENAME TO mpg_mmt_mp_id_seq;
ALTER SEQUENCE IF EXISTS lada.messung_id_seq RENAME TO measm_id_seq;
ALTER SEQUENCE IF EXISTS lada.messung_messung_ext_id_seq RENAME TO measm_measm_ext_id_seq;
ALTER SEQUENCE IF EXISTS lada.messwert_id_seq RENAME TO meas_val_id_seq;
ALTER SEQUENCE IF EXISTS lada.ortszuordnung_id_seq RENAME TO geolocat_id_seq;
ALTER SEQUENCE IF EXISTS lada.ortszuordnung_mp_id_seq RENAME TO geolocat_mpg_id_seq;
ALTER SEQUENCE IF EXISTS lada.probe_id_seq RENAME TO sample_id_seq;
ALTER SEQUENCE IF EXISTS lada.probe_probe_id_seq RENAME TO sample_sample_id_seq;
ALTER SEQUENCE IF EXISTS lada.status_protokoll_id_seq RENAME TO status_prot_id_seq;
ALTER SEQUENCE IF EXISTS lada.tagzuordnung_id_seq RENAME TO tag_link_id_seq;
ALTER SEQUENCE IF EXISTS lada.zusatz_wert_id_seq RENAME TO sample_specif_meas_val_id_seq;

ALTER SEQUENCE IF EXISTS master.auth_lst_umw_id_seq RENAME TO auth_coord_ofc_env_medium_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.datenbasis_id_seq RENAME TO regulation_id_seq;
ALTER SEQUENCE IF EXISTS master.datensatz_erzeuger_id_seq RENAME TO dataset_creator_id_seq;
ALTER SEQUENCE IF EXISTS master.deskriptor_umwelt_id_seq RENAME TO env_descrip_env_medium_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.deskriptoren_id_seq RENAME TO env_descrip_id_seq;
ALTER SEQUENCE IF EXISTS master.deskriptoren_s_xx_seq RENAME TO env_descrip_s_xx_seq;
ALTER SEQUENCE IF EXISTS master.gemeindeuntergliederung_id_seq RENAME TO munic_div_id_seq;
ALTER SEQUENCE IF EXISTS master.grid_column_id_seq RENAME TO grid_col_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.grid_column_values_id_seq RENAME TO grid_col_conf_id_seq;
ALTER SEQUENCE IF EXISTS master.importer_config_id_seq RENAME TO import_conf_id_seq;
ALTER SEQUENCE IF EXISTS master.koordinaten_art_id_seq RENAME TO spat_ref_sys_id_seq;
ALTER SEQUENCE IF EXISTS master.kta_grp_zuord_id_seq RENAME TO nucl_facil_gr_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.kta_gruppe_id_seq RENAME TO nucl_facil_gr_id_seq;
ALTER SEQUENCE IF EXISTS master.kta_id_seq RENAME TO nucl_facil_id_seq;
ALTER SEQUENCE IF EXISTS master.mass_einheit_umrechnung_id_seq RENAME TO unit_convers_id_seq;
ALTER SEQUENCE IF EXISTS master.mess_einheit_id_seq RENAME TO meas_unit_id_seq;
ALTER SEQUENCE IF EXISTS master.messgroesse_id_seq RENAME TO measd_id_seq;
ALTER SEQUENCE IF EXISTS master.messgroessen_gruppe_id_seq RENAME TO measd_gr_id_seq;
ALTER SEQUENCE IF EXISTS master.messprogramm_kategorie_id_seq RENAME TO mpg_categ_id_seq;
ALTER SEQUENCE IF EXISTS master.messprogramm_transfer_id_seq RENAME TO mpg_transf_id_seq;
ALTER SEQUENCE IF EXISTS master.ort_id_seq RENAME TO site_id_seq;
ALTER SEQUENCE IF EXISTS master.pflicht_messgroesse_id_seq RENAME TO oblig_measd_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.probenart_id_seq RENAME TO sample_meth_id_seq;
ALTER SEQUENCE IF EXISTS master.probenehmer_id_seq RENAME TO sampler_id_seq;
ALTER SEQUENCE IF EXISTS master.query_messstelle_id_seq RENAME TO query_meas_facil_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.rei_progpunkt_grp_umw_zuord_id_seq RENAME TO rei_ag_gr_env_medium_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.rei_progpunkt_grp_zuord_id_seq RENAME TO rei_ag_gr_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.rei_progpunkt_gruppe_id_seq RENAME TO rei_ag_gr_id_seq;
ALTER SEQUENCE IF EXISTS master.rei_progpunkt_id_seq RENAME TO rei_ag_id_seq;
ALTER SEQUENCE IF EXISTS master.result_type_id_seq RENAME TO disp_id_seq;
ALTER SEQUENCE IF EXISTS master.richtwert_id_seq RENAME TO ref_val_id_seq;
ALTER SEQUENCE IF EXISTS master.richtwert_massnahme_id_seq RENAME TO ref_val_measure_id_seq;
ALTER SEQUENCE IF EXISTS master.sollist_mmtgrp_id_seq RENAME TO targ_act_mmt_gr_id_seq;
ALTER SEQUENCE IF EXISTS master.sollist_mmtgrp_zuord_id_seq RENAME TO targ_act_mmt_gr_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.sollist_soll_id_seq RENAME TO targ_act_targ_id_seq;
ALTER SEQUENCE IF EXISTS master.sollist_umwgrp_id_seq RENAME TO targ_env_gr_id_seq;
ALTER SEQUENCE IF EXISTS master.sollist_umwgrp_zuord_id_seq RENAME TO targ_env_gr_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.staat_id_seq RENAME TO state_id_seq;
ALTER SEQUENCE IF EXISTS master.status_reihenfolge_id_seq RENAME TO status_ord_mp_id_seq;
ALTER SEQUENCE IF EXISTS master.tm_fm_umrechnung_id_seq RENAME TO convers_dm_fm_id_seq;
ALTER SEQUENCE IF EXISTS master.umwelt_zusatz_id_seq RENAME TO env_specif_mp_id_seq;


