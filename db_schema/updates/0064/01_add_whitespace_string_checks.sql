-- Replace empty string checks with checks that do not allow whitespace strings

-- Remove old checks
-- Master tables
SET search_path to master;

ALTER table spat_ref_sys
    DROP CONSTRAINT IF EXISTS spat_ref_sys_name_check,
    DROP CONSTRAINT IF EXISTS spat_ref_sys_idf_geo_key_check;

ALTER TABLE meas_unit
    DROP CONSTRAINT IF EXISTS meas_unit_name_check,
    DROP CONSTRAINT IF EXISTS meas_unit_unit_symbol_check,
    DROP CONSTRAINT IF EXISTS meas_unit_eudf_unit_id_check;

ALTER TABLE opr_mode
    DROP CONSTRAINT IF EXISTS opr_mode_name_check;

ALTER TABLE state
    DROP CONSTRAINT IF EXISTS state_ctry_check,
    DROP CONSTRAINT IF EXISTS state_iso_3166_check,
    DROP CONSTRAINT IF EXISTS state_coord_x_ext_check,
    DROP CONSTRAINT IF EXISTS state_coord_y_ext_check;

ALTER TABLE admin_unit
    DROP CONSTRAINT IF EXISTS admin_unit_name_check,
    DROP CONSTRAINT IF EXISTS admin_unit_gov_dist_id_check,
    DROP CONSTRAINT IF EXISTS admin_unit_rural_dist_id_check,
    DROP CONSTRAINT IF EXISTS admin_unit_state_id_check,
    DROP CONSTRAINT IF EXISTS admin_unit_zip_check;

ALTER TABLE network
    DROP CONSTRAINT IF EXISTS network_name_check,
    DROP CONSTRAINT IF EXISTS network_idf_network_id_check,
    DROP CONSTRAINT IF EXISTS network_mail_list_check;

ALTER TABLE meas_facil
    DROP CONSTRAINT IF EXISTS meas_facil_id_check,
    DROP CONSTRAINT IF EXISTS meas_facil_address_check,
    DROP CONSTRAINT IF EXISTS meas_facil_name_check,
    DROP CONSTRAINT IF EXISTS meas_facil_meas_facil_type_check,
    DROP CONSTRAINT IF EXISTS meas_facil_trunk_code_check;

ALTER TABLE env_medium
    DROP CONSTRAINT IF EXISTS env_medium_id_check,
    DROP CONSTRAINT IF EXISTS env_medium_descr_check,
    DROP CONSTRAINT IF EXISTS env_medium_name_check;

ALTER TABLE auth_funct
    DROP CONSTRAINT IF EXISTS auth_funct_funct_check;

ALTER TABLE auth
    DROP CONSTRAINT IF EXISTS auth_ldap_gr_check;

ALTER TABLE regulation
    DROP CONSTRAINT IF EXISTS regulation_descr_check,
    DROP CONSTRAINT IF EXISTS regulation_name_check;

ALTER TABLE dataset_creator
    DROP CONSTRAINT IF EXISTS dataset_creator_ext_id_check,
    DROP CONSTRAINT IF EXISTS dataset_creator_descr_check;

ALTER TABLE env_descrip
    DROP CONSTRAINT IF EXISTS env_descrip_name_check,
    DROP CONSTRAINT IF EXISTS env_descrip_implication_check;

ALTER TABLE lada_user
    DROP CONSTRAINT IF EXISTS lada_user_name_check;

ALTER TABLE base_query
    DROP CONSTRAINT IF EXISTS base_query_sql_check;

ALTER TABLE query_user
    DROP CONSTRAINT IF EXISTS query_user_name_check,
    DROP CONSTRAINT IF EXISTS query_user_descr_check;

ALTER TABLE filter_type
    DROP CONSTRAINT IF EXISTS filter_type_type_check;

ALTER TABLE filter
    DROP CONSTRAINT IF EXISTS filter_sql_check,
    DROP CONSTRAINT IF EXISTS filter_param_check;

ALTER TABLE mmt
    DROP CONSTRAINT IF EXISTS mmt_id_check,
    DROP CONSTRAINT IF EXISTS mmt_descr_check,
    DROP CONSTRAINT IF EXISTS mmt_name_check;

ALTER TABLE measd
    DROP CONSTRAINT IF EXISTS measd_descr_check,
    DROP CONSTRAINT IF EXISTS measd_name_check,
    DROP CONSTRAINT IF EXISTS measd_def_color_check,
    DROP CONSTRAINT IF EXISTS measd_idf_ext_id_check,
    DROP CONSTRAINT IF EXISTS measd_bvl_format_id_check;

ALTER TABLE measd_gr
    DROP CONSTRAINT IF EXISTS measd_gr_name_check;

ALTER TABLE mpg_categ
    DROP CONSTRAINT IF EXISTS mpg_categ_ext_id_check;

ALTER TABLE rei_ag
    DROP CONSTRAINT IF EXISTS rei_ag_name_check,
    DROP CONSTRAINT IF EXISTS rei_ag_descr_check;

ALTER TABLE rei_ag_gr
    DROP CONSTRAINT IF EXISTS rei_ag_gr_name_check,
    DROP CONSTRAINT IF EXISTS rei_ag_gr_descr_check;

ALTER TABLE nucl_facil
    DROP CONSTRAINT IF EXISTS nucl_facil_ext_id_check,
    DROP CONSTRAINT IF EXISTS nucl_facil_name_check;

ALTER TABLE nucl_facil_gr
    DROP CONSTRAINT IF EXISTS nucl_facil_gr_ext_id_check,
    DROP CONSTRAINT IF EXISTS nucl_facil_gr_name_check;

ALTER TABLE site_class
    DROP CONSTRAINT IF EXISTS site_class_name_check,
    DROP CONSTRAINT IF EXISTS site_class_ext_id_check;

ALTER TABLE poi
    DROP CONSTRAINT IF EXISTS poi_id_check,
    DROP CONSTRAINT IF EXISTS poi_name_check;

ALTER TABLE munic_div
    DROP CONSTRAINT IF EXISTS munic_div_name_check;

ALTER TABLE site
    DROP CONSTRAINT IF EXISTS site_ext_id_check,
    DROP CONSTRAINT IF EXISTS site_long_text_check,
    DROP CONSTRAINT IF EXISTS site_coord_x_ext_check,
    DROP CONSTRAINT IF EXISTS site_coord_y_ext_check,
    DROP CONSTRAINT IF EXISTS site_short_text_check,
    DROP CONSTRAINT IF EXISTS site_rei_report_text_check,
    DROP CONSTRAINT IF EXISTS site_rei_zone_check,
    DROP CONSTRAINT IF EXISTS site_rei_sector_check,
    DROP CONSTRAINT IF EXISTS site_rei_competence_check,
    DROP CONSTRAINT IF EXISTS site_rei_opr_mode_check,
    DROP CONSTRAINT IF EXISTS site_route_check;

ALTER TABLE type_regulation
    DROP CONSTRAINT IF EXISTS type_regulation_name_check;

ALTER TABLE sample_specif
    DROP CONSTRAINT IF EXISTS sample_specif_id_check,
    DROP CONSTRAINT IF EXISTS sample_specif_name_check,
    DROP CONSTRAINT IF EXISTS sample_specif_ext_id_check,
    DROP CONSTRAINT IF EXISTS sample_specif_eudf_keyword_check;

ALTER TABLE sample_meth
    DROP CONSTRAINT IF EXISTS sample_meth_name_check,
    DROP CONSTRAINT IF EXISTS sample_meth_ext_id_check,
    DROP CONSTRAINT IF EXISTS sample_meth_eudf_sample_meth_id_check;

ALTER TABLE sampler
    DROP CONSTRAINT IF EXISTS sampler_ext_id_check,
    DROP CONSTRAINT IF EXISTS sampler_editor_check,
    DROP CONSTRAINT IF EXISTS sampler_comm_check,
    DROP CONSTRAINT IF EXISTS sampler_inst_check,
    DROP CONSTRAINT IF EXISTS sampler_descr_check,
    DROP CONSTRAINT IF EXISTS sampler_short_text_check,
    DROP CONSTRAINT IF EXISTS sampler_city_check,
    DROP CONSTRAINT IF EXISTS sampler_zip_check,
    DROP CONSTRAINT IF EXISTS sampler_street_check,
    DROP CONSTRAINT IF EXISTS sampler_phone_check,
    DROP CONSTRAINT IF EXISTS sampler_phone_mobile_check,
    DROP CONSTRAINT IF EXISTS sampler_email_check,
    DROP CONSTRAINT IF EXISTS sampler_route_planning_check,
    DROP CONSTRAINT IF EXISTS sampler_type_check;

ALTER TABLE disp
    DROP CONSTRAINT IF EXISTS disp_name_check,
    DROP CONSTRAINT IF EXISTS disp_format_check;

ALTER TABLE status_lev
    DROP CONSTRAINT IF EXISTS status_lev_lev_check;

ALTER TABLE status_val
    DROP CONSTRAINT IF EXISTS status_val_val_check;

ALTER TABLE mpg_transf
    DROP CONSTRAINT IF EXISTS mpg_transf_ext_id_check,
    DROP CONSTRAINT IF EXISTS mpg_transf_name_check;

ALTER TABLE tz
    DROP CONSTRAINT IF EXISTS tz_name_check;

ALTER TABLE import_conf
    DROP CONSTRAINT IF EXISTS import_conf_attribute_check,
    DROP CONSTRAINT IF EXISTS import_conf_from_val_check,
    DROP CONSTRAINT IF EXISTS import_conf_to_val_check;

ALTER TABLE grid_col_mp
    DROP CONSTRAINT IF EXISTS grid_col_mp_grid_col_check,
    DROP CONSTRAINT IF EXISTS grid_col_mp_data_index_check;

ALTER TABLE grid_col_conf
    DROP CONSTRAINT IF EXISTS grid_col_conf_sort_check;

ALTER TABLE tag
    DROP CONSTRAINT IF EXISTS tag_name_check;

ALTER TABLE convers_dm_fm
    DROP CONSTRAINT IF EXISTS convers_dm_fm_env_descrip_pattern_check;

ALTER TABLE ref_val_measure
    DROP CONSTRAINT IF EXISTS ref_val_measure_measure_check,
    DROP CONSTRAINT IF EXISTS ref_val_measure_descr_check;

ALTER TABLE ref_val
    DROP CONSTRAINT IF EXISTS ref_val_specif_check;

ALTER TABLE targ_act_mmt_gr
    DROP CONSTRAINT IF EXISTS targ_act_mmt_gr_name_check,
    DROP CONSTRAINT IF EXISTS targ_act_mmt_gr_descr_check;

ALTER TABLE targ_env_gr
    DROP CONSTRAINT IF EXISTS targ_env_gr_name_check,
    DROP CONSTRAINT IF EXISTS targ_env_gr_targ_env_gr_displ_check;

-- Lada tables
set search_path to lada;

ALTER TABLE mpg
    DROP CONSTRAINT IF EXISTS mpg_comm_mpg_check,
    DROP CONSTRAINT IF EXISTS mpg_sample_pd_check,
    DROP CONSTRAINT IF EXISTS mpg_comm_sample_check,
    DROP CONSTRAINT IF EXISTS mpg_sample_quant_check;

ALTER TABLE sample
    DROP CONSTRAINT IF EXISTS sample_ext_id_check,
    DROP CONSTRAINT IF EXISTS sample_main_sample_id_check,
    DROP CONSTRAINT IF EXISTS sample_env_descrip_name_check;

ALTER TABLE comm_sample
    DROP CONSTRAINT IF EXISTS comm_sample_text_check;

ALTER TABLE geolocat
    DROP CONSTRAINT IF EXISTS geolocat_add_site_text_check;

ALTER TABLE geolocat_mpg
    DROP CONSTRAINT IF EXISTS geolocat_mpg_add_site_text_check;

ALTER TABLE sample_specif_meas_val
    DROP CONSTRAINT IF EXISTS sample_specif_meas_val_smaller_than_check;

ALTER TABLE measm
    DROP CONSTRAINT IF EXISTS measm_min_sample_id_check;

ALTER TABLE comm_measm
    DROP CONSTRAINT IF EXISTS comm_measm_text_check;

ALTER TABLE meas_val
    DROP CONSTRAINT IF EXISTS meas_val_less_than_lod_check;

ALTER TABLE status_prot
    DROP CONSTRAINT IF EXISTS status_prot_text_check;

-- Add new checks
-- Master tables
SET search_path to master;

ALTER table spat_ref_sys
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from idf_geo_key) <> '');

ALTER TABLE meas_unit
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from unit_symbol) <> ''),
    ADD CHECK (trim(both ' ' from eudf_unit_id) <> '');

ALTER TABLE opr_mode
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE state
    ADD CHECK (trim(both ' ' from ctry) <> ''),
    ADD CHECK (trim(both ' ' from iso_3166) <> ''),
    ADD CHECK (trim(both ' ' from coord_x_ext) <> ''),
    ADD CHECK (trim(both ' ' from coord_y_ext) <> '');

ALTER TABLE admin_unit
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from gov_dist_id) <> ''),
    ADD CHECK (trim(both ' ' from rural_dist_id) <> ''),
    ADD CHECK (trim(both ' ' from state_id) <> ''),
    ADD CHECK (trim(both ' ' from zip) <> '');

ALTER TABLE network
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from idf_network_id) <> ''),
    ADD CHECK (trim(both ' ' from mail_list) <> '');

ALTER TABLE meas_facil
    ADD CHECK (trim(both ' ' from id) <> ''),
    ADD CHECK (trim(both ' ' from address) <> ''),
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from meas_facil_type) <> ''),
    ADD CHECK (trim(both ' ' from trunk_code) <> '');

ALTER TABLE env_medium
    ADD CHECK (trim(both ' ' from id) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE auth_funct
    ADD CHECK (trim(both ' ' from funct) <> '');

ALTER TABLE auth
    ADD CHECK (trim(both ' ' from ldap_gr) <> '');

ALTER TABLE regulation
    ADD CHECK (trim(both ' ' from descr) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE dataset_creator
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE env_descrip
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from implication) <> '');

ALTER TABLE lada_user
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE base_query
    ADD CHECK (trim(both ' ' from sql) <> '');

ALTER TABLE query_user
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE filter_type
    ADD CHECK (trim(both ' ' from type) <> '');

ALTER TABLE filter
    ADD CHECK (trim(both ' ' from sql) <> ''),
    ADD CHECK (trim(both ' ' from param) <> '');

ALTER TABLE mmt
    ADD CHECK (trim(both ' ' from id) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE measd
    ADD CHECK (trim(both ' ' from descr) <> ''),
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from def_color) <> ''),
    ADD CHECK (trim(both ' ' from idf_ext_id) <> ''),
    ADD CHECK (trim(both ' ' from bvl_format_id) <> '');

ALTER TABLE measd_gr
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE mpg_categ
    ADD CHECK (trim(both ' ' from ext_id) <> '');

ALTER TABLE rei_ag
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE rei_ag_gr
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE nucl_facil
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE nucl_facil_gr
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE site_class
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from ext_id) <> '');

ALTER TABLE poi
    ADD CHECK (trim(both ' ' from id) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE munic_div
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE site
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from long_text) <> ''),
    ADD CHECK (trim(both ' ' from coord_x_ext) <> ''),
    ADD CHECK (trim(both ' ' from coord_y_ext) <> ''),
    ADD CHECK (trim(both ' ' from short_text) <> ''),
    ADD CHECK (trim(both ' ' from rei_report_text) <> ''),
    ADD CHECK (trim(both ' ' from rei_zone) <> ''),
    ADD CHECK (trim(both ' ' from rei_sector) <> ''),
    ADD CHECK (trim(both ' ' from rei_competence) <> ''),
    ADD CHECK (trim(both ' ' from rei_opr_mode) <> ''),
    ADD CHECK (trim(both ' ' from route) <> '');

ALTER TABLE type_regulation
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE sample_specif
    ADD CHECK (trim(both ' ' from id) <> ''),
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from eudf_keyword) <> '');

ALTER TABLE sample_meth
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from eudf_sample_meth_id) <> '');

ALTER TABLE sampler
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from editor) <> ''),
    ADD CHECK (trim(both ' ' from comm) <> ''),
    ADD CHECK (trim(both ' ' from inst) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> ''),
    ADD CHECK (trim(both ' ' from short_text) <> ''),
    ADD CHECK (trim(both ' ' from city) <> ''),
    ADD CHECK (trim(both ' ' from zip) <> ''),
    ADD CHECK (trim(both ' ' from street) <> ''),
    ADD CHECK (trim(both ' ' from phone) <> ''),
    ADD CHECK (trim(both ' ' from phone_mobile) <> ''),
    ADD CHECK (trim(both ' ' from email) <> ''),
    ADD CHECK (trim(both ' ' from route_planning) <> ''),
    ADD CHECK (trim(both ' ' from type) <> '');

ALTER TABLE disp
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from format) <> '');

ALTER TABLE status_lev
    ADD CHECK (trim(both ' ' from lev) <> '');

ALTER TABLE status_val
    ADD CHECK (trim(both ' ' from val) <> '');

ALTER TABLE mpg_transf
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE tz
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE import_conf
    ADD CHECK (trim(both ' ' from attribute) <> ''),
    ADD CHECK (trim(both ' ' from from_val) <> ''),
    ADD CHECK (trim(both ' ' from to_val) <> '');

ALTER TABLE grid_col_mp
    ADD CHECK (trim(both ' ' from grid_col) <> ''),
    ADD CHECK (trim(both ' ' from data_index) <> '');

ALTER TABLE grid_col_conf
    ADD CHECK (trim(both ' ' from sort) <> '');

ALTER TABLE tag
    ADD CHECK (trim(both ' ' from name) <> '');

ALTER TABLE convers_dm_fm
    ADD CHECK (trim(both ' ' from env_descrip_pattern) <> '');

ALTER TABLE ref_val_measure
    ADD CHECK (trim(both ' ' from measure) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE ref_val
    ADD CHECK (trim(both ' ' from specif) <> '');

ALTER TABLE targ_act_mmt_gr
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from descr) <> '');

ALTER TABLE targ_env_gr
    ADD CHECK (trim(both ' ' from name) <> ''),
    ADD CHECK (trim(both ' ' from targ_env_gr_displ) <> '');

-- Lada tables
set search_path to lada;

ALTER TABLE mpg
    ADD CHECK (trim(both ' ' from comm_mpg) <> ''),
    ADD CHECK (trim(both ' ' from sample_pd) <> ''),
    ADD CHECK (trim(both ' ' from comm_sample) <> ''),
    ADD CHECK (trim(both ' ' from sample_quant) <> '');

ALTER TABLE sample
    ADD CHECK (trim(both ' ' from ext_id) <> ''),
    ADD CHECK (trim(both ' ' from main_sample_id) <> ''),
    ADD CHECK (trim(both ' ' from env_descrip_name) <> '');

ALTER TABLE comm_sample
    ADD CHECK (trim(both ' ' from text) <> '');

ALTER TABLE geolocat
    ADD CHECK (trim(both ' ' from add_site_text) <> '');

ALTER TABLE geolocat_mpg
    ADD CHECK (trim(both ' ' from add_site_text) <> '');

ALTER TABLE sample_specif_meas_val
    ADD CHECK (trim(both ' ' from smaller_than) <> '');

ALTER TABLE measm
    ADD CHECK (trim(both ' ' from min_sample_id) <> '');

ALTER TABLE comm_measm
    ADD CHECK (trim(both ' ' from text) <> '');

ALTER TABLE meas_val
    ADD CHECK (trim(both ' ' from less_than_lod) <> '');

ALTER TABLE status_prot
    ADD CHECK (trim(both ' ' from text) <> '');

-- New constraints for primary keys
ALTER TABLE master.admin_unit
    ADD CHECK (trim(both ' ' from id) <> '');

ALTER TABLE master.network
    ADD CHECK (trim(both ' ' from id) <> '');

ALTER TABLE master.type_regulation
    ADD CHECK (trim(both ' ' from id) <> '');
