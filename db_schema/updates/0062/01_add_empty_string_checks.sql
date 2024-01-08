-- Replace empty string fields with null and
-- add empty string check constraints to all database tables

-- Master tables
SET search_path to master;

UPDATE spat_ref_sys SET name = NULL WHERE name = '';
UPDATE spat_ref_sys SET idf_geo_key = NULL WHERE idf_geo_key = '';
ALTER table spat_ref_sys
    ADD CONSTRAINT spat_ref_sys_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT spat_ref_sys_idf_geo_key_empty_check CHECK (idf_geo_key <> '');

UPDATE meas_unit SET name = NULL where name = '';
UPDATE meas_unit SET unit_symbol = NULL WHERE unit_symbol = '';
UPDATE meas_unit SET eudf_unit_id = NULL WHERE eudf_unit_id = '';
ALTER TABLE meas_unit
    ADD CONSTRAINT meas_unit_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT meas_unit_unit_symbol_empty_check CHECK (unit_symbol <> ''),
    ADD CONSTRAINT meas_unit_eudf_unit_id CHECK (eudf_unit_id <> '');

ALTER TABLE opr_mode
    ADD CONSTRAINT opr_mode_name_empty_check CHECK (name <> '');

UPDATE state SET iso_3166 = NULL WHERE iso_3166 = '';
UPDATE state SET int_veh_reg_code = NULL WHERE int_veh_reg_code = '';
UPDATE state SET coord_x_ext = NULL WHERE coord_x_ext = '';
UPDATE state SET coord_y_ext = NULL WHERE coord_y_ext = '';
ALTER TABLE state
    ADD CONSTRAINT state_ctry_empty_check CHECK (ctry <> ''),
    ADD CONSTRAINT state_iso_3166_empty_check CHECK (iso_3166 <> ''),
    ADD CONSTRAINT state_coord_x_ext_empty_check CHECK (coord_x_ext <> ''),
    ADD CONSTRAINT state_coord_y_ext_empty_check CHECK (coord_y_ext <> '');

UPDATE admin_unit SET gov_dist_id = NULL WHERE gov_dist_id = '';
UPDATE admin_unit SET rural_dist_id = NULL WHERE rural_dist_id = '';
UPDATE admin_unit SET zip = NULL where zip = '';
ALTER TABLE admin_unit
    ADD CONSTRAINT admin_unit_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT admin_unit_gov_dist_id_empty_check CHECK (gov_dist_id <> ''),
    ADD CONSTRAINT admin_unit_rural_dist_id_empty_check CHECK (rural_dist_id <> ''),
    ADD CONSTRAINT admin_unit_state_id_empty_check CHECK (state_id <> ''),
    ADD CONSTRAINT admin_unit_zip_empty_check CHECK (zip <> '');

UPDATE network SET name = NULL WHERE name = '';
UPDATE network SET idf_network_id = NULL WHERE idf_network_id = '';
UPDATE network SET mail_list = NULL WHERE mail_list = '';
ALTER TABLE network
    ADD CONSTRAINT network_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT network_idf_network_id_empty_check CHECK (idf_network_id <> ''),
    ADD CONSTRAINT netowkr_mail_list_empty_check CHECK (mail_list <> '');

UPDATE meas_facil SET ADDRESS = NULL WHERE ADDRESS = '';
UPDATE meas_facil SET name = NULL WHERE name = '';
UPDATE meas_facil SET meas_facil_type = NULL WHERE meas_facil_type = '';
UPDATE meas_facil SET trunk_code = NULL WHERE trunk_code = '';
ALTER TABLE meas_facil
    ADD CONSTRAINT meas_facil_id_empty_check CHECK (id <> ''),
    ADD CONSTRAINT meas_facil_address_empty_check CHECK (address <> ''),
    ADD CONSTRAINT meas_facil_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT meas_facil_meas_facil_type_empty_check CHECK (meas_facil_type <> ''),
    ADD CONSTRAINT meas_facil_trunk_code_empty_check CHECK (trunk_code <> '');

UPDATE env_medium SET descr = NULL WHERE descr = '';
ALTER TABLE env_medium
    ADD CONSTRAINT env_medium_id_empty_check CHECK (id <> ''),
    ADD CONSTRAINT env_medium_descr_empty_check CHECK (descr <> ''),
    ADD CONSTRAINT env_medium_name_empty_check CHECK (name <> '');

ALTER TABLE auth_funct
    ADD CONSTRAINT auth_funct_funct_empty_check CHECK (funct <> '');

ALTER TABLE auth
    ADD CONSTRAINT auth_ldap_gr_empty_check CHECK (ldap_gr <> '');

UPDATE regulation SET descr = NULL WHERE descr = '';
UPDATE regulation SET name = NULL WHERE name = '';
ALTER TABLE regulation
    ADD CONSTRAINT regulation_descr_empty_check CHECK (descr <> ''),
    ADD CONSTRAINT regulation_name_empty_check CHECK (name <> '');

ALTER TABLE dataset_creator
    ADD CONSTRAINT dataset_creator_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT dataset_creator_descr_empty_check CHECK (descr <> '');

UPDATE env_descrip SET name = NULL WHERE name = '';
UPDATE env_descrip SET implication = NULL WHERE implication = '';
ALTER TABLE env_descrip
    ADD CONSTRAINT env_descrip_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT env_descrip_implication_empty_check CHECK (implication <> '');

ALTER TABLE lada_user
    ADD CONSTRAINT lada_user_name_empty_check CHECK (name <> '');

ALTER TABLE base_query
    ADD CONSTRAINT base_query_sql_empty_check CHECK (sql <> '');

ALTER TABLE query_user
    ADD CONSTRAINT query_user_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT query_user_descr_empty_check CHECK (descr <> '');

ALTER TABLE filter_type
    ADD CONSTRAINT filter_type_type_empty_check CHECK (type <> '');

ALTER TABLE filter
    ADD CONSTRAINT filter_sql_empty_check CHECK (sql <> ''),
    ADD CONSTRAINT filter_param_empty_check CHECK (param <> '');

UPDATE mmt SET descr = NULL WHERE descr = '';
UPDATE mmt SET name = NULL WHERE name = '';
ALTER TABLE mmt
    ADD CONSTRAINT mmt_id_empty_check CHECK (id <> ''),
    ADD CONSTRAINT mmt_descr_empty_check CHECK (descr <> ''),
    ADD CONSTRAINT mmt_name_empty_check CHECK (name <> '');

UPDATE measd SET descr = NULL WHERE descr = '';
UPDATE measd SET def_color = NULL WHERE def_color = '';
UPDATE measd SET idf_ext_id = NULL WHERE idf_ext_id = '';
UPDATE measd SET bvl_format_id = NULL WHERE bvl_format_id = '';
ALTER TABLE measd
    ADD CONSTRAINT measd_descr_empty_check CHECK (descr <> ''),
    ADD CONSTRAINT measd_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT measd_def_color_empty_check CHECK (def_color <> ''),
    ADD CONSTRAINT measd_idf_ext_id_empty_check CHECK (idf_ext_id <> ''),
    ADD CONSTRAINT measd_bvl_format_id_empty_check CHECK (bvl_format_id <> '');

UPDATE measd_gr SET name = NULL WHERE name = '';
ALTER TABLE measd_gr
    ADD CONSTRAINT measd_gr_name_empty_check CHECK (name <> '');

ALTER TABLE mpg_categ
    ADD CONSTRAINT mpg_categ_network_id_empty_check CHECK (network_id <> ''),
    ADD CONSTRAINT mpg_categ_ext_id_empty_check CHECK (ext_id <> '');

UPDATE rei_ag SET descr = NULL WHERE descr = '';
ALTER TABLE rei_ag
    ADD CONSTRAINT rei_ag_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT rei_ag_descr_empty_check CHECK (descr <> '');

UPDATE rei_ag_gr SET name = NULL WHERE name = '';
UPDATE rei_ag_Gr SET descr = NULL WHERE descr = '';
ALTER TABLE rei_ag_gr
    ADD CONSTRAINT rei_ag_gr_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT rei_ag_gr_descr_empty_check CHECK (descr <> '');

UPDATE nucl_facil SET ext_id = NULL WHERE ext_id = '';
UPDATE nucl_facil SET name = NULL WHERE name = '';
ALTER TABLE nucl_facil
    ADD CONSTRAINT nucl_facil_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT nucl_facil_name_empty_check CHECK (name <> '');

UPDATE nucl_facil_gr SET name = NULL WHERE name = '';
ALTER TABLE nucl_facil_gr
    ADD CONSTRAINT nucl_facil_gr_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT nucl_facil_gr_name_empty_check CHECK (name <> '');

UPDATE site_class SET name = NULL WHERE name = '';
UPDATE site_class SET ext_id = NULL WHERE ext_id = '';
ALTER TABLE site_class
    ADD CONSTRAINT site_class_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT site_class_ext_id_empty_check CHECK (ext_id <> '');

ALTER TABLE poi
    ADD CONSTRAINT poi_id_empty_check CHECK (id <> ''),
    ADD CONSTRAINT poi_name_empty_check CHECK (name <> '');

UPDATE munic_div SET name = NULL where name = '';
ALTER TABLE munic_div
    ADD CONSTRAINT munic_div_name_empty_check CHECK (name <> '');

UPDATE site SET rei_report_text = NULL WHERE rei_report_text = '';
UPDATE site SET rei_zone = NULL WHERE rei_zone = '';
UPDATE site SET rei_sector = NULL WHERE rei_sector = '';
UPDATE site SET rei_competence = NULL WHERE rei_competence = '';
UPDATE site SET rei_opr_mode = NULL WHERE rei_opr_mode = '';
UPDATE site SET route = NULL WHERE route = '';
ALTER TABLE site
    ADD CONSTRAINT site_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT site_long_text_empty_check CHECK (long_text <> ''),
    ADD CONSTRAINT site_coord_x_ext_empty_check CHECK (coord_x_ext <> ''),
    ADD CONSTRAINT site_coord_y_ext_empty_check CHECK (coord_y_ext <> ''),
    ADD CONSTRAINT site_short_text_empty_check CHECK (short_text <> ''),
    ADD CONSTRAINT site_rei_report_text_empty_check CHECK (rei_report_text <> ''),
    ADD CONSTRAINT site_rei_zone_empty_check CHECK (rei_zone <> ''),
    ADD CONSTRAINT site_rei_sector_empty_check CHECK (rei_sector <> ''),
    ADD CONSTRAINT site_rei_competence_empty_check CHECK (rei_competence <> ''),
    ADD CONSTRAINT site_rei_opr_mode_empty_check CHECK (rei_opr_mode <> ''),
    ADD CONSTRAINT site_route_empty_check CHECK (route <> '');

UPDATE type_regulation SET name = NULL WHERE name = '';
ALTER TABLE type_regulation
    ADD CONSTRAINT type_regulation_name_empty_check CHECK (name <> '');

UPDATE sample_specif SET eudf_keyword = NULL WHERE eudf_keyword = '';
ALTER TABLE sample_specif
    ADD CONSTRAINT sample_specif_id_empty_check CHECK (id <> ''),
    ADD CONSTRAINT sample_specif_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT sample_specif_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT sample_specif_eudf_keywork_empty_check CHECK (eudf_keyword <> '');

UPDATE sample_meth SET name = NULL WHERE name = '';
ALTER TABLE sample_meth
    ADD CONSTRAINT sample_meth_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT sample_meth_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT sample_meth_eudf_sample_meth_id CHECK (eudf_sample_meth_id <> '');

UPDATE sampler SET editor = NULL WHERE editor = '';
UPDATE sampler SET comm = NULL WHERE comm = '';
UPDATE sampler SET inst = NULL WHERE inst = '';
UPDATE sampler SET city = NULL WHERE city = '';
UPDATE sampler SET street = NULL WHERE street = '';
UPDATE sampler SET phone = NULL WHERE phone = '';
UPDATE sampler SET phone_mobile = NULL WHERE phone_mobile = '';
UPDATE sampler SET email = NULL WHERE email = '';
UPDATE sampler SET route_planning = NULL WHERE route_planning = '';
UPDATE sampler SET type = NULL WHERE type = '';
ALTER TABLE sampler
    ADD CONSTRAINT sampler_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT sampler_editor_empty_check CHECK (editor <> ''),
    ADD CONSTRAINT sampler_comm_empty_check CHECK (comm <> ''),
    ADD CONSTRAINT sampler_inst_empty_check CHECK (inst <> ''),
    ADD CONSTRAINT sampler_descr_empty_check CHECK (descr <> ''),
    ADD CONSTRAINT sampler_short_text_empty_check CHECK (short_text <> ''),
    ADD CONSTRAINT sampler_city_empty_check CHECK (city <> ''),
    ADD CONSTRAINT sampler_zip_empty_check CHECK (zip <> ''),
    ADD CONSTRAINT sampler_street_empty_check CHECK (street <> ''),
    ADD CONSTRAINT sampler_phone_empty_check CHECK (phone <> ''),
    ADD CONSTRAINT sampler_phone_mobile_empty_check CHECK (phone_mobile <> ''),
    ADD CONSTRAINT sampler_email_empty_check CHECK (email <> ''),
    ADD CONSTRAINT sampler_route_planning_empty_check CHECK (route_planning <> ''),
    ADD CONSTRAINT sampler_type_empty_check CHECK (type <> '');

ALTER TABLE disp
    ADD CONSTRAINT disp_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT disp_format_empty_check CHECK (format <> '');

ALTER TABLE status_lev
    ADD CONSTRAINT status_lev_lev_empty_check CHECK (lev <> '');

ALTER TABLE status_val
    ADD CONSTRAINT status_val_val_empty_check CHECK (val <> '');

ALTER TABLE mpg_transf
    ADD CONSTRAINT mpg_transf_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT mpg_transf_name_empty_check CHECK (name <> '');

ALTER TABLE tz
    ADD CONSTRAINT tz_name_empty_check CHECK (name <> '');

UPDATE import_conf SET from_val = NULL WHERE from_val = '';
ALTER TABLE import_conf
    ADD CONSTRAINT import_conf_attribute_empty_check CHECK (attribute <> ''),
    ADD CONSTRAINT import_conf_from_val_empty_check CHECK (from_val <> ''),
    ADD CONSTRAINT import_conf_to_val_empty_check CHECK (to_val <> '');

ALTER TABLE grid_col_mp
    ADD CONSTRAINT grid_col_mp_grid_col_empty_check CHECK (grid_col <> ''),
    ADD CONSTRAINT grid_col_mp_data_index_empty_check CHECK (data_index <> '');

UPDATE grid_col_conf SET sort = NULL WHERE sort = '';
ALTER TABLE grid_col_conf
    ADD CONSTRAINT grid_col_conf_sort_empty_check CHECK (sort <> '');

ALTER TABLE tag
    ADD CONSTRAINT tag_name_empty_check CHECK (name <> '');

UPDATE convers_dm_fm SET env_descrip_pattern = NULL WHERE env_descrip_pattern = '';
ALTER TABLE convers_dm_fm
    ADD CONSTRAINT convers_dm_fm_env_descrip_pattern_empty_check CHECK (env_descrip_pattern <> '');

UPDATE ref_val_measure SET descr = NULL WHERE descr = '';
ALTER TABLE ref_val_measure
    ADD CONSTRAINT ref_val_measure_measure_empty_check CHECK (measure <> ''),
    ADD CONSTRAINT ref_val_measure_descr_empty_check CHECK (descr <> '');

UPDATE ref_val SET specif = NULL WHERE specif = '';
ALTER TABLE ref_val
    ADD CONSTRAINT ref_val_specif_empty_check CHECK (specif <> '');

UPDATE targ_act_mmt_gr SET name = NULL WHERE name = '';
UPDATE targ_act_mmt_gr SET descr = NULL WHERE descr = '';
ALTER TABLE targ_act_mmt_gr
    ADD CONSTRAINT targ_act_mmt_gr_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT targ_act_mmt_gr_descr_empty_check CHECK (descr <> '');

UPDATE targ_env_gr SET name = NULL WHERE name = '';
UPDATE targ_env_gr SET targ_env_gr_displ = NULL WHERE targ_env_gr_displ = '';
ALTER TABLE targ_env_gr
    ADD CONSTRAINT targ_env_gr_name_empty_check CHECK (name <> ''),
    ADD CONSTRAINT targ_env_targ_env_gr_displ_empty_check CHECK (targ_env_gr_displ <> '');

-- Lada tables
set search_path to lada;

UPDATE mpg SET comm_mpg = NULL WHERE comm_mpg = '';
UPDATE mpg SET comm_sample = NULL WHERE comm_sample = '';
UPDATE mpg SET sample_quant = NULL WHERE sample_quant = '';
ALTER TABLE mpg
    ADD CONSTRAINT mpg_comm_mpg_empty_check CHECK (comm_mpg <> ''),
    ADD CONSTRAINT mpg_sample_pd_empty_check CHECK (sample_pd <> ''),
    ADD CONSTRAINT mpg_comm_sample_empty_check CHECK (comm_sample <> ''),
    ADD CONSTRAINT mpg_sample_quant_empty_check CHECK (sample_quant <> '');

UPDATE sample SET main_sample_id = NULL WHERE main_sample_id = '';
UPDATE sample SET env_descrip_name = NULL WHERE env_descrip_name = '';
ALTER TABLE sample
    ADD CONSTRAINT sample_ext_id_empty_check CHECK (ext_id <> ''),
    ADD CONSTRAINT sample_main_sample_id CHECK (main_sample_id <> ''),
    ADD CONSTRAINT sample_env_descrip_name CHECK (env_descrip_name <> '');

UPDATE comm_sample SET text = NULL WHERE text = '';
ALTER TABLE comm_sample
    ADD CONSTRAINT comm_sample_text_empty_check CHECK (text <> '');

UPDATE geolocat SET add_site_text = NULL WHERE add_site_text = '';
ALTER TABLE geolocat
    ADD CONSTRAINT geolocat_add_site_text_empty_check CHECK (add_site_text <> '');

UPDATE geolocat_mpg SET add_site_text = NULL WHERE add_site_text = '';
ALTER TABLE geolocat_mpg
    ADD CONSTRAINT geolocat_mpg_add_site_text_empty_check CHECK (add_site_text <> '');

UPDATE sample_specif_meas_val SET smaller_than = NULL WHERE smaller_than = '';
ALTER TABLE sample_specif_meas_val
    ADD CONSTRAINT sample_specif_meas_val_smaller_than_empty_check CHECK (smaller_than <> '');

UPDATE measm SET min_sample_id = NULL WHERE min_sample_id = '';
ALTER TABLE measm
    ADD CONSTRAINT measm_min_sample_id_empty_check CHECK (min_sample_id <> '');

UPDATE comm_measm SET text = NULL WHERE text = '';
ALTER TABLE comm_measm
    ADD CONSTRAINT comm_measm_text_empty_check CHECK (text <> '');

UPDATE meas_val SET less_than_lod = NULL WHERE less_than_lod = '';
ALTER TABLE meas_val
    ADD CONSTRAINT meas_val_less_than_lod CHECK (less_than_lod <> '');

UPDATE status_prot SET text = NULL WHERE text = '';
ALTER TABLE status_prot
    ADD CONSTRAINT status_prot_text_empty_check CHECK (text <> '');

-- Functions
CREATE OR REPLACE FUNCTION set_measm_status() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE status_id integer;
    BEGIN
        INSERT INTO lada.status_prot
            (meas_facil_id, date, text, measm_id, status_mp_id)
        VALUES ((SELECT meas_facil_id
                     FROM lada.sample
                     WHERE id = NEW.sample_id),
                now() AT TIME ZONE 'utc', NULL, NEW.id, 1)
        RETURNING id into status_id;
        UPDATE lada.measm SET status = status_id where id = NEW.id;
        RETURN NEW;
    END;
$$;
