-- Replace empty string checks with checks that do not allow whitespace strings

-- Remove old checks
-- Master tables
SET search_path to master;

ALTER table spat_ref_sys
    DROP CONSTRAINT spat_ref_sys_name_empty_check,
    DROP CONSTRAINT spat_ref_sys_idf_geo_key_empty_check;

ALTER TABLE meas_unit
    DROP CONSTRAINT meas_unit_name_empty_check,
    DROP CONSTRAINT meas_unit_unit_symbol_empty_check,
    DROP CONSTRAINT meas_unit_eudf_unit_id;

ALTER TABLE opr_mode
    DROP CONSTRAINT opr_mode_name_empty_check;

ALTER TABLE state
    DROP CONSTRAINT state_ctry_empty_check,
    DROP CONSTRAINT state_iso_3166_empty_check,
    DROP CONSTRAINT state_coord_x_ext_empty_check,
    DROP CONSTRAINT state_coord_y_ext_empty_check;

ALTER TABLE admin_unit
    DROP CONSTRAINT admin_unit_name_empty_check,
    DROP CONSTRAINT admin_unit_gov_dist_id_empty_check,
    DROP CONSTRAINT admin_unit_rural_dist_id_empty_check,
    DROP CONSTRAINT admin_unit_state_id_empty_check,
    DROP CONSTRAINT admin_unit_zip_empty_check;

ALTER TABLE network
    DROP CONSTRAINT network_name_empty_check,
    DROP CONSTRAINT network_idf_network_id_empty_check,
    DROP CONSTRAINT netowkr_mail_list_empty_check;

ALTER TABLE meas_facil
    DROP CONSTRAINT meas_facil_id_empty_check,
    DROP CONSTRAINT meas_facil_address_empty_check,
    DROP CONSTRAINT meas_facil_name_empty_check,
    DROP CONSTRAINT meas_facil_meas_facil_type_empty_check,
    DROP CONSTRAINT meas_facil_trunk_code_empty_check;

ALTER TABLE env_medium
    DROP CONSTRAINT env_medium_id_empty_check,
    DROP CONSTRAINT env_medium_descr_empty_check,
    DROP CONSTRAINT env_medium_name_empty_check;

ALTER TABLE auth_funct
    DROP CONSTRAINT auth_funct_funct_empty_check;

ALTER TABLE auth
    DROP CONSTRAINT auth_ldap_gr_empty_check;

ALTER TABLE regulation
    DROP CONSTRAINT regulation_descr_empty_check,
    DROP CONSTRAINT regulation_name_empty_check;

ALTER TABLE dataset_creator
    DROP CONSTRAINT dataset_creator_ext_id_empty_check,
    DROP CONSTRAINT dataset_creator_descr_empty_check;

ALTER TABLE env_descrip
    DROP CONSTRAINT env_descrip_name_empty_check,
    DROP CONSTRAINT env_descrip_implication_empty_check;

ALTER TABLE lada_user
    DROP CONSTRAINT lada_user_name_empty_check;

ALTER TABLE base_query
    DROP CONSTRAINT base_query_sql_empty_check;

ALTER TABLE query_user
    DROP CONSTRAINT query_user_name_empty_check,
    DROP CONSTRAINT query_user_descr_empty_check;

ALTER TABLE filter_type
    DROP CONSTRAINT filter_type_type_empty_check;

ALTER TABLE filter
    DROP CONSTRAINT filter_sql_empty_check,
    DROP CONSTRAINT filter_param_empty_check;

ALTER TABLE mmt
    DROP CONSTRAINT mmt_id_empty_check,
    DROP CONSTRAINT mmt_descr_empty_check,
    DROP CONSTRAINT mmt_name_empty_check;

ALTER TABLE measd
    DROP CONSTRAINT measd_descr_empty_check,
    DROP CONSTRAINT measd_name_empty_check,
    DROP CONSTRAINT measd_def_color_empty_check,
    DROP CONSTRAINT measd_idf_ext_id_empty_check,
    DROP CONSTRAINT measd_bvl_format_id_empty_check;

ALTER TABLE measd_gr
    DROP CONSTRAINT measd_gr_name_empty_check;

ALTER TABLE mpg_categ
    DROP CONSTRAINT mpg_categ_network_id_empty_check,
    DROP CONSTRAINT mpg_categ_ext_id_empty_check;

ALTER TABLE rei_ag
    DROP CONSTRAINT rei_ag_name_empty_check,
    DROP CONSTRAINT rei_ag_descr_empty_check;

ALTER TABLE rei_ag_gr
    DROP CONSTRAINT rei_ag_gr_name_empty_check,
    DROP CONSTRAINT rei_ag_gr_descr_empty_check;

ALTER TABLE nucl_facil
    DROP CONSTRAINT nucl_facil_ext_id_empty_check,
    DROP CONSTRAINT nucl_facil_name_empty_check;

ALTER TABLE nucl_facil_gr
    DROP CONSTRAINT nucl_facil_gr_ext_id_empty_check,
    DROP CONSTRAINT nucl_facil_gr_name_empty_check;

ALTER TABLE site_class
    DROP CONSTRAINT site_class_name_empty_check,
    DROP CONSTRAINT site_class_ext_id_empty_check;

ALTER TABLE poi
    DROP CONSTRAINT poi_id_empty_check,
    DROP CONSTRAINT poi_name_empty_check;

ALTER TABLE munic_div
    DROP CONSTRAINT munic_div_name_empty_check;

ALTER TABLE site
    DROP CONSTRAINT site_ext_id_empty_check,
    DROP CONSTRAINT site_long_text_empty_check,
    DROP CONSTRAINT site_coord_x_ext_empty_check,
    DROP CONSTRAINT site_coord_y_ext_empty_check,
    DROP CONSTRAINT site_short_text_empty_check,
    DROP CONSTRAINT site_rei_report_text_empty_check,
    DROP CONSTRAINT site_rei_zone_empty_check,
    DROP CONSTRAINT site_rei_sector_empty_check,
    DROP CONSTRAINT site_rei_competence_empty_check,
    DROP CONSTRAINT site_rei_opr_mode_empty_check,
    DROP CONSTRAINT site_route_empty_check;

ALTER TABLE type_regulation
    DROP CONSTRAINT type_regulation_name_empty_check;

ALTER TABLE sample_specif
    DROP CONSTRAINT sample_specif_id_empty_check,
    DROP CONSTRAINT sample_specif_name_empty_check,
    DROP CONSTRAINT sample_specif_ext_id_empty_check,
    DROP CONSTRAINT sample_specif_eudf_keywork_empty_check;

ALTER TABLE sample_meth
    DROP CONSTRAINT sample_meth_name_empty_check,
    DROP CONSTRAINT sample_meth_ext_id_empty_check,
    DROP CONSTRAINT sample_meth_eudf_sample_meth_id;

ALTER TABLE sampler
    DROP CONSTRAINT sampler_ext_id_empty_check,
    DROP CONSTRAINT sampler_editor_empty_check,
    DROP CONSTRAINT sampler_comm_empty_check,
    DROP CONSTRAINT sampler_inst_empty_check,
    DROP CONSTRAINT sampler_descr_empty_check,
    DROP CONSTRAINT sampler_short_text_empty_check,
    DROP CONSTRAINT sampler_city_empty_check,
    DROP CONSTRAINT sampler_zip_empty_check,
    DROP CONSTRAINT sampler_street_empty_check,
    DROP CONSTRAINT sampler_phone_empty_check,
    DROP CONSTRAINT sampler_phone_mobile_empty_check,
    DROP CONSTRAINT sampler_email_empty_check,
    DROP CONSTRAINT sampler_route_planning_empty_check,
    DROP CONSTRAINT sampler_type_empty_check;

ALTER TABLE disp
    DROP CONSTRAINT disp_name_empty_check,
    DROP CONSTRAINT disp_format_empty_check;

ALTER TABLE status_lev
    DROP CONSTRAINT status_lev_lev_empty_check;

ALTER TABLE status_val
    DROP CONSTRAINT status_val_val_empty_check;

ALTER TABLE mpg_transf
    DROP CONSTRAINT mpg_transf_ext_id_empty_check,
    DROP CONSTRAINT mpg_transf_name_empty_check;

ALTER TABLE tz
    DROP CONSTRAINT tz_name_empty_check;

ALTER TABLE import_conf
    DROP CONSTRAINT import_conf_attribute_empty_check,
    DROP CONSTRAINT import_conf_from_val_empty_check,
    DROP CONSTRAINT import_conf_to_val_empty_check;

ALTER TABLE grid_col_mp
    DROP CONSTRAINT grid_col_mp_grid_col_empty_check,
    DROP CONSTRAINT grid_col_mp_data_index_empty_check;

ALTER TABLE grid_col_conf
    DROP CONSTRAINT grid_col_conf_sort_empty_check;

ALTER TABLE tag
    DROP CONSTRAINT tag_name_empty_check;

ALTER TABLE convers_dm_fm
    DROP CONSTRAINT convers_dm_fm_env_descrip_pattern_empty_check;

ALTER TABLE ref_val_measure
    DROP CONSTRAINT ref_val_measure_measure_empty_check,
    DROP CONSTRAINT ref_val_measure_descr_empty_check;

ALTER TABLE ref_val
    DROP CONSTRAINT ref_val_specif_empty_check;

ALTER TABLE targ_act_mmt_gr
    DROP CONSTRAINT targ_act_mmt_gr_name_empty_check,
    DROP CONSTRAINT targ_act_mmt_gr_descr_empty_check;

ALTER TABLE targ_env_gr
    DROP CONSTRAINT targ_env_gr_name_empty_check,
    DROP CONSTRAINT targ_env_targ_env_gr_displ_empty_check;

-- Lada tables
set search_path to lada;

ALTER TABLE mpg
    DROP CONSTRAINT mpg_comm_mpg_empty_check,
    DROP CONSTRAINT mpg_sample_pd_empty_check,
    DROP CONSTRAINT mpg_comm_sample_empty_check,
    DROP CONSTRAINT mpg_sample_quant_empty_check;

ALTER TABLE sample
    DROP CONSTRAINT sample_ext_id_empty_check,
    DROP CONSTRAINT sample_main_sample_id,
    DROP CONSTRAINT sample_env_descrip_name;

ALTER TABLE comm_sample
    DROP CONSTRAINT comm_sample_text_empty_check;

ALTER TABLE geolocat
    DROP CONSTRAINT geolocat_add_site_text_empty_check;

ALTER TABLE geolocat_mpg
    DROP CONSTRAINT geolocat_mpg_add_site_text_empty_check;

ALTER TABLE sample_specif_meas_val
    DROP CONSTRAINT sample_specif_meas_val_smaller_than_empty_check;

ALTER TABLE measm
    DROP CONSTRAINT measm_min_sample_id_empty_check;

ALTER TABLE comm_measm
    DROP CONSTRAINT comm_measm_text_empty_check;

ALTER TABLE meas_val
    DROP CONSTRAINT meas_val_less_than_lod;

ALTER TABLE status_prot
    DROP CONSTRAINT status_prot_text_empty_check;

-- Add new checks
-- Master tables
SET search_path to master;

ALTER table spat_ref_sys
    ADD CONSTRAINT spat_ref_sys_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT spat_ref_sys_idf_geo_key_empty_check CHECK (trim(both ' ' from idf_geo_key) <> '');

ALTER TABLE meas_unit
    ADD CONSTRAINT meas_unit_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT meas_unit_unit_symbol_empty_check CHECK (trim(both ' ' from unit_symbol) <> ''),
    ADD CONSTRAINT meas_unit_eudf_unit_id CHECK (trim(both ' ' from eudf_unit_id) <> '');

ALTER TABLE opr_mode
    ADD CONSTRAINT opr_mode_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE state
    ADD CONSTRAINT state_ctry_empty_check CHECK (trim(both ' ' from ctry) <> ''),
    ADD CONSTRAINT state_iso_3166_empty_check CHECK (trim(both ' ' from iso_3166) <> ''),
    ADD CONSTRAINT state_coord_x_ext_empty_check CHECK (trim(both ' ' from coord_x_ext) <> ''),
    ADD CONSTRAINT state_coord_y_ext_empty_check CHECK (trim(both ' ' from coord_y_ext) <> '');

ALTER TABLE admin_unit
    ADD CONSTRAINT admin_unit_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT admin_unit_gov_dist_id_empty_check CHECK (trim(both ' ' from gov_dist_id) <> ''),
    ADD CONSTRAINT admin_unit_rural_dist_id_empty_check CHECK (trim(both ' ' from rural_dist_id) <> ''),
    ADD CONSTRAINT admin_unit_state_id_empty_check CHECK (trim(both ' ' from state_id) <> ''),
    ADD CONSTRAINT admin_unit_zip_empty_check CHECK (trim(both ' ' from zip) <> '');

ALTER TABLE network
    ADD CONSTRAINT network_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT network_idf_network_id_empty_check CHECK (trim(both ' ' from idf_network_id) <> ''),
    ADD CONSTRAINT netowkr_mail_list_empty_check CHECK (trim(both ' ' from mail_list) <> '');

ALTER TABLE meas_facil
    ADD CONSTRAINT meas_facil_id_empty_check CHECK (trim(both ' ' from id) <> ''),
    ADD CONSTRAINT meas_facil_address_empty_check CHECK (trim(both ' ' from address) <> ''),
    ADD CONSTRAINT meas_facil_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT meas_facil_meas_facil_type_empty_check CHECK (trim(both ' ' from meas_facil_type) <> ''),
    ADD CONSTRAINT meas_facil_trunk_code_empty_check CHECK (trim(both ' ' from trunk_code) <> '');

ALTER TABLE env_medium
    ADD CONSTRAINT env_medium_id_empty_check CHECK (trim(both ' ' from id) <> ''),
    ADD CONSTRAINT env_medium_descr_empty_check CHECK (trim(both ' ' from descr) <> ''),
    ADD CONSTRAINT env_medium_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE auth_funct
    ADD CONSTRAINT auth_funct_funct_empty_check CHECK (trim(both ' ' from funct) <> '');

ALTER TABLE auth
    ADD CONSTRAINT auth_ldap_gr_empty_check CHECK (trim(both ' ' from ldap_gr) <> '');

ALTER TABLE regulation
    ADD CONSTRAINT regulation_descr_empty_check CHECK (trim(both ' ' from descr) <> ''),
    ADD CONSTRAINT regulation_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE dataset_creator
    ADD CONSTRAINT dataset_creator_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT dataset_creator_descr_empty_check CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE env_descrip
    ADD CONSTRAINT env_descrip_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT env_descrip_implication_empty_check CHECK (trim(both ' ' from implication) <> '');

ALTER TABLE lada_user
    ADD CONSTRAINT lada_user_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE base_query
    ADD CONSTRAINT base_query_sql_empty_check CHECK (trim(both ' ' from sql) <> '');

ALTER TABLE query_user
    ADD CONSTRAINT query_user_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT query_user_descr_empty_check CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE filter_type
    ADD CONSTRAINT filter_type_type_empty_check CHECK (trim(both ' ' from type) <> '');

ALTER TABLE filter
    ADD CONSTRAINT filter_sql_empty_check CHECK (trim(both ' ' from sql) <> ''),
    ADD CONSTRAINT filter_param_empty_check CHECK (trim(both ' ' from param) <> '');

ALTER TABLE mmt
    ADD CONSTRAINT mmt_id_empty_check CHECK (trim(both ' ' from id) <> ''),
    ADD CONSTRAINT mmt_descr_empty_check CHECK (trim(both ' ' from descr) <> ''),
    ADD CONSTRAINT mmt_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE measd
    ADD CONSTRAINT measd_descr_empty_check CHECK (trim(both ' ' from descr) <> ''),
    ADD CONSTRAINT measd_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT measd_def_color_empty_check CHECK (trim(both ' ' from def_color) <> ''),
    ADD CONSTRAINT measd_idf_ext_id_empty_check CHECK (trim(both ' ' from idf_ext_id) <> ''),
    ADD CONSTRAINT measd_bvl_format_id_empty_check CHECK (trim(both ' ' from bvl_format_id) <> '');

ALTER TABLE measd_gr
    ADD CONSTRAINT measd_gr_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE mpg_categ
    ADD CONSTRAINT mpg_categ_network_id_empty_check CHECK (trim(both ' ' from network_id) <> ''),
    ADD CONSTRAINT mpg_categ_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> '');

ALTER TABLE rei_ag
    ADD CONSTRAINT rei_ag_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT rei_ag_descr_empty_check CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE rei_ag_gr
    ADD CONSTRAINT rei_ag_gr_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT rei_ag_gr_descr_empty_check CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE nucl_facil
    ADD CONSTRAINT nucl_facil_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT nucl_facil_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE nucl_facil_gr
    ADD CONSTRAINT nucl_facil_gr_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT nucl_facil_gr_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE site_class
    ADD CONSTRAINT site_class_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT site_class_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> '');

ALTER TABLE poi
    ADD CONSTRAINT poi_id_empty_check CHECK (trim(both ' ' from id) <> ''),
    ADD CONSTRAINT poi_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE munic_div
    ADD CONSTRAINT munic_div_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE site
    ADD CONSTRAINT site_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT site_long_text_empty_check CHECK (trim(both ' ' from long_text) <> ''),
    ADD CONSTRAINT site_coord_x_ext_empty_check CHECK (trim(both ' ' from coord_x_ext) <> ''),
    ADD CONSTRAINT site_coord_y_ext_empty_check CHECK (trim(both ' ' from coord_y_ext) <> ''),
    ADD CONSTRAINT site_short_text_empty_check CHECK (trim(both ' ' from short_text) <> ''),
    ADD CONSTRAINT site_rei_report_text_empty_check CHECK (trim(both ' ' from rei_report_text) <> ''),
    ADD CONSTRAINT site_rei_zone_empty_check CHECK (trim(both ' ' from rei_zone) <> ''),
    ADD CONSTRAINT site_rei_sector_empty_check CHECK (trim(both ' ' from rei_sector) <> ''),
    ADD CONSTRAINT site_rei_competence_empty_check CHECK (trim(both ' ' from rei_competence) <> ''),
    ADD CONSTRAINT site_rei_opr_mode_empty_check CHECK (trim(both ' ' from rei_opr_mode) <> ''),
    ADD CONSTRAINT site_route_empty_check CHECK (trim(both ' ' from route) <> '');

ALTER TABLE type_regulation
    ADD CONSTRAINT type_regulation_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE sample_specif
    ADD CONSTRAINT sample_specif_id_empty_check CHECK (trim(both ' ' from id) <> ''),
    ADD CONSTRAINT sample_specif_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT sample_specif_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT sample_specif_eudf_keywork_empty_check CHECK (trim(both ' ' from eudf_keyword) <> '');

ALTER TABLE sample_meth
    ADD CONSTRAINT sample_meth_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT sample_meth_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT sample_meth_eudf_sample_meth_id CHECK (trim(both ' ' from eudf_sample_meth_id) <> '');

ALTER TABLE sampler
    ADD CONSTRAINT sampler_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT sampler_editor_empty_check CHECK (trim(both ' ' from editor) <> ''),
    ADD CONSTRAINT sampler_comm_empty_check CHECK (trim(both ' ' from comm) <> ''),
    ADD CONSTRAINT sampler_inst_empty_check CHECK (trim(both ' ' from inst) <> ''),
    ADD CONSTRAINT sampler_descr_empty_check CHECK (trim(both ' ' from descr) <> ''),
    ADD CONSTRAINT sampler_short_text_empty_check CHECK (trim(both ' ' from short_text) <> ''),
    ADD CONSTRAINT sampler_city_empty_check CHECK (trim(both ' ' from city) <> ''),
    ADD CONSTRAINT sampler_zip_empty_check CHECK (trim(both ' ' from zip) <> ''),
    ADD CONSTRAINT sampler_street_empty_check CHECK (trim(both ' ' from street) <> ''),
    ADD CONSTRAINT sampler_phone_empty_check CHECK (trim(both ' ' from phone) <> ''),
    ADD CONSTRAINT sampler_phone_mobile_empty_check CHECK (trim(both ' ' from phone_mobile) <> ''),
    ADD CONSTRAINT sampler_email_empty_check CHECK (trim(both ' ' from email) <> ''),
    ADD CONSTRAINT sampler_route_planning_empty_check CHECK (trim(both ' ' from route_planning) <> ''),
    ADD CONSTRAINT sampler_type_empty_check CHECK (trim(both ' ' from type) <> '');

ALTER TABLE disp
    ADD CONSTRAINT disp_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT disp_format_empty_check CHECK (trim(both ' ' from format) <> '');

ALTER TABLE status_lev
    ADD CONSTRAINT status_lev_lev_empty_check CHECK (trim(both ' ' from lev) <> '');

ALTER TABLE status_val
    ADD CONSTRAINT status_val_val_empty_check CHECK (trim(both ' ' from val) <> '');

ALTER TABLE mpg_transf
    ADD CONSTRAINT mpg_transf_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT mpg_transf_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE tz
    ADD CONSTRAINT tz_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE import_conf
    ADD CONSTRAINT import_conf_attribute_empty_check CHECK (trim(both ' ' from attribute) <> ''),
    ADD CONSTRAINT import_conf_from_val_empty_check CHECK (trim(both ' ' from from_val) <> ''),
    ADD CONSTRAINT import_conf_to_val_empty_check CHECK (trim(both ' ' from to_val) <> '');

ALTER TABLE grid_col_mp
    ADD CONSTRAINT grid_col_mp_grid_col_empty_check CHECK (trim(both ' ' from grid_col) <> ''),
    ADD CONSTRAINT grid_col_mp_data_index_empty_check CHECK (trim(both ' ' from data_index) <> '');

ALTER TABLE grid_col_conf
    ADD CONSTRAINT grid_col_conf_sort_empty_check CHECK (trim(both ' ' from sort) <> '');

ALTER TABLE tag
    ADD CONSTRAINT tag_name_empty_check CHECK (trim(both ' ' from name) <> '');

ALTER TABLE convers_dm_fm
    ADD CONSTRAINT convers_dm_fm_env_descrip_pattern_empty_check CHECK (trim(both ' ' from env_descrip_pattern) <> '');

ALTER TABLE ref_val_measure
    ADD CONSTRAINT ref_val_measure_measure_empty_check CHECK (trim(both ' ' from measure) <> ''),
    ADD CONSTRAINT ref_val_measure_descr_empty_check CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE ref_val
    ADD CONSTRAINT ref_val_specif_empty_check CHECK (trim(both ' ' from specif) <> '');

ALTER TABLE targ_act_mmt_gr
    ADD CONSTRAINT targ_act_mmt_gr_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT targ_act_mmt_gr_descr_empty_check CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE targ_env_gr
    ADD CONSTRAINT targ_env_gr_name_empty_check CHECK (trim(both ' ' from name) <> ''),
    ADD CONSTRAINT targ_env_targ_env_gr_displ_empty_check CHECK (trim(both ' ' from targ_env_gr_displ) <> '');

-- Lada tables
set search_path to lada;

ALTER TABLE mpg
    ADD CONSTRAINT mpg_comm_mpg_empty_check CHECK (trim(both ' ' from comm_mpg) <> ''),
    ADD CONSTRAINT mpg_sample_pd_empty_check CHECK (trim(both ' ' from sample_pd) <> ''),
    ADD CONSTRAINT mpg_comm_sample_empty_check CHECK (trim(both ' ' from comm_sample) <> ''),
    ADD CONSTRAINT mpg_sample_quant_empty_check CHECK (trim(both ' ' from sample_quant) <> '');

ALTER TABLE sample
    ADD CONSTRAINT sample_ext_id_empty_check CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CONSTRAINT sample_main_sample_id CHECK (trim(both ' ' from main_sample_id) <> ''),
    ADD CONSTRAINT sample_env_descrip_name CHECK (trim(both ' ' from env_descrip_name) <> '');

ALTER TABLE comm_sample
    ADD CONSTRAINT comm_sample_text_empty_check CHECK (trim(both ' ' from text) <> '');

ALTER TABLE geolocat
    ADD CONSTRAINT geolocat_add_site_text_empty_check CHECK (trim(both ' ' from add_site_text) <> '');

ALTER TABLE geolocat_mpg
    ADD CONSTRAINT geolocat_mpg_add_site_text_empty_check CHECK (trim(both ' ' from add_site_text) <> '');

ALTER TABLE sample_specif_meas_val
    ADD CONSTRAINT sample_specif_meas_val_smaller_than_empty_check CHECK (trim(both ' ' from smaller_than) <> '');

ALTER TABLE measm
    ADD CONSTRAINT measm_min_sample_id_empty_check CHECK (trim(both ' ' from min_sample_id) <> '');

ALTER TABLE comm_measm
    ADD CONSTRAINT comm_measm_text_empty_check CHECK (trim(both ' ' from text) <> '');

ALTER TABLE meas_val
    ADD CONSTRAINT meas_val_less_than_lod CHECK (trim(both ' ' from less_than_lod) <> '');

ALTER TABLE status_prot
    ADD CONSTRAINT status_prot_text_empty_check CHECK (trim(both ' ' from text) <> '');

-- New constraints for primary keys
ALTER TABLE master.admin_unit
    ADD CONSTRAINT admin_unit_id_check CHECK (trim(both ' ' from id) <> '');

ALTER TABLE master.network
    ADD CONSTRAINT network_id_check CHECK (trim(both ' ' from id) <> '');
