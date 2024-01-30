-- Replace empty string fields with null and
-- add empty string check constraints to all database tables

-- Master tables
SET search_path to master;

UPDATE spat_ref_sys SET name = NULL WHERE name = '';
UPDATE spat_ref_sys SET idf_geo_key = NULL WHERE idf_geo_key = '';
ALTER table spat_ref_sys
    ADD CHECK (name <> ''),
    ADD CHECK (idf_geo_key <> '');

UPDATE meas_unit SET name = NULL where name = '';
UPDATE meas_unit SET unit_symbol = NULL WHERE unit_symbol = '';
UPDATE meas_unit SET eudf_unit_id = NULL WHERE eudf_unit_id = '';
ALTER TABLE meas_unit
    ADD CHECK (name <> ''),
    ADD CHECK (unit_symbol <> ''),
    ADD CHECK (eudf_unit_id <> '');

ALTER TABLE opr_mode
    ADD CHECK (name <> '');

UPDATE state SET iso_3166 = NULL WHERE iso_3166 = '';
UPDATE state SET int_veh_reg_code = NULL WHERE int_veh_reg_code = '';
UPDATE state SET coord_x_ext = NULL WHERE coord_x_ext = '';
UPDATE state SET coord_y_ext = NULL WHERE coord_y_ext = '';
ALTER TABLE state
    ADD CHECK (ctry <> ''),
    ADD CHECK (iso_3166 <> ''),
    ADD CHECK (coord_x_ext <> ''),
    ADD CHECK (coord_y_ext <> '');

UPDATE admin_unit SET gov_dist_id = NULL WHERE gov_dist_id = '';
UPDATE admin_unit SET rural_dist_id = NULL WHERE rural_dist_id = '';
UPDATE admin_unit SET zip = NULL where zip = '';
ALTER TABLE admin_unit
    ADD CHECK (name <> ''),
    ADD CHECK (gov_dist_id <> ''),
    ADD CHECK (rural_dist_id <> ''),
    ADD CHECK (state_id <> ''),
    ADD CHECK (zip <> '');

UPDATE network SET name = NULL WHERE name = '';
UPDATE network SET idf_network_id = NULL WHERE idf_network_id = '';
UPDATE network SET mail_list = NULL WHERE mail_list = '';
ALTER TABLE network
    ADD CHECK (name <> ''),
    ADD CHECK (idf_network_id <> ''),
    ADD CHECK (mail_list <> '');

UPDATE meas_facil SET ADDRESS = NULL WHERE ADDRESS = '';
UPDATE meas_facil SET name = NULL WHERE name = '';
UPDATE meas_facil SET meas_facil_type = NULL WHERE meas_facil_type = '';
UPDATE meas_facil SET trunk_code = NULL WHERE trunk_code = '';
ALTER TABLE meas_facil
    ADD CHECK (id <> ''),
    ADD CHECK (address <> ''),
    ADD CHECK (name <> ''),
    ADD CHECK (meas_facil_type <> ''),
    ADD CHECK (trunk_code <> '');

UPDATE env_medium SET descr = NULL WHERE descr = '';
ALTER TABLE env_medium
    ADD CHECK (id <> ''),
    ADD CHECK (descr <> ''),
    ADD CHECK (name <> '');

ALTER TABLE auth_funct
    ADD CHECK (funct <> '');

ALTER TABLE auth
    ADD CHECK (ldap_gr <> '');

UPDATE regulation SET descr = NULL WHERE descr = '';
UPDATE regulation SET name = NULL WHERE name = '';
ALTER TABLE regulation
    ADD CHECK (descr <> ''),
    ADD CHECK (name <> '');

ALTER TABLE dataset_creator
    ADD CHECK (ext_id <> ''),
    ADD CHECK (descr <> '');

UPDATE env_descrip SET name = NULL WHERE name = '';
UPDATE env_descrip SET implication = NULL WHERE implication = '';
ALTER TABLE env_descrip
    ADD CHECK (name <> ''),
    ADD CHECK (implication <> '');

ALTER TABLE lada_user
    ADD CHECK (name <> '');

ALTER TABLE base_query
    ADD CHECK (sql <> '');

ALTER TABLE query_user
    ADD CHECK (name <> ''),
    ADD CHECK (descr <> '');

ALTER TABLE filter_type
    ADD CHECK (type <> '');

ALTER TABLE filter
    ADD CHECK (sql <> ''),
    ADD CHECK (param <> '');

UPDATE mmt SET descr = NULL WHERE descr = '';
UPDATE mmt SET name = NULL WHERE name = '';
ALTER TABLE mmt
    ADD CHECK (id <> ''),
    ADD CHECK (descr <> ''),
    ADD CHECK (name <> '');

UPDATE measd SET descr = NULL WHERE descr = '';
UPDATE measd SET def_color = NULL WHERE def_color = '';
UPDATE measd SET idf_ext_id = NULL WHERE idf_ext_id = '';
UPDATE measd SET bvl_format_id = NULL WHERE bvl_format_id = '';
ALTER TABLE measd
    ADD CHECK (descr <> ''),
    ADD CHECK (name <> ''),
    ADD CHECK (def_color <> ''),
    ADD CHECK (idf_ext_id <> ''),
    ADD CHECK (bvl_format_id <> '');

UPDATE measd_gr SET name = NULL WHERE name = '';
ALTER TABLE measd_gr
    ADD CHECK (name <> '');

ALTER TABLE mpg_categ
    ADD CHECK (network_id <> ''),
    ADD CHECK (ext_id <> '');

UPDATE rei_ag SET descr = NULL WHERE descr = '';
ALTER TABLE rei_ag
    ADD CHECK (name <> ''),
    ADD CHECK (descr <> '');

UPDATE rei_ag_gr SET name = NULL WHERE name = '';
UPDATE rei_ag_Gr SET descr = NULL WHERE descr = '';
ALTER TABLE rei_ag_gr
    ADD CHECK (name <> ''),
    ADD CHECK (descr <> '');

UPDATE nucl_facil SET ext_id = NULL WHERE ext_id = '';
UPDATE nucl_facil SET name = NULL WHERE name = '';
ALTER TABLE nucl_facil
    ADD CHECK (ext_id <> ''),
    ADD CHECK (name <> '');

UPDATE nucl_facil_gr SET name = NULL WHERE name = '';
ALTER TABLE nucl_facil_gr
    ADD CHECK (ext_id <> ''),
    ADD CHECK (name <> '');

UPDATE site_class SET name = NULL WHERE name = '';
UPDATE site_class SET ext_id = NULL WHERE ext_id = '';
ALTER TABLE site_class
    ADD CHECK (name <> ''),
    ADD CHECK (ext_id <> '');

ALTER TABLE poi
    ADD CHECK (id <> ''),
    ADD CHECK (name <> '');

UPDATE munic_div SET name = NULL where name = '';
ALTER TABLE munic_div
    ADD CHECK (name <> '');

UPDATE site SET rei_report_text = NULL WHERE rei_report_text = '';
UPDATE site SET rei_zone = NULL WHERE rei_zone = '';
UPDATE site SET rei_sector = NULL WHERE rei_sector = '';
UPDATE site SET rei_competence = NULL WHERE rei_competence = '';
UPDATE site SET rei_opr_mode = NULL WHERE rei_opr_mode = '';
UPDATE site SET route = NULL WHERE route = '';
ALTER TABLE site
    ADD CHECK (ext_id <> ''),
    ADD CHECK (long_text <> ''),
    ADD CHECK (coord_x_ext <> ''),
    ADD CHECK (coord_y_ext <> ''),
    ADD CHECK (short_text <> ''),
    ADD CHECK (rei_report_text <> ''),
    ADD CHECK (rei_zone <> ''),
    ADD CHECK (rei_sector <> ''),
    ADD CHECK (rei_competence <> ''),
    ADD CHECK (rei_opr_mode <> ''),
    ADD CHECK (route <> '');

UPDATE type_regulation SET name = NULL WHERE name = '';
ALTER TABLE type_regulation
    ADD CHECK (name <> '');

UPDATE sample_specif SET eudf_keyword = NULL WHERE eudf_keyword = '';
ALTER TABLE sample_specif
    ADD CHECK (id <> ''),
    ADD CHECK (name <> ''),
    ADD CHECK (ext_id <> ''),
    ADD CHECK (eudf_keyword <> '');

UPDATE sample_meth SET name = NULL WHERE name = '';
ALTER TABLE sample_meth
    ADD CHECK (name <> ''),
    ADD CHECK (ext_id <> ''),
    ADD CHECK (eudf_sample_meth_id <> '');

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
    ADD CHECK (ext_id <> ''),
    ADD CHECK (editor <> ''),
    ADD CHECK (comm <> ''),
    ADD CHECK (inst <> ''),
    ADD CHECK (descr <> ''),
    ADD CHECK (short_text <> ''),
    ADD CHECK (city <> ''),
    ADD CHECK (zip <> ''),
    ADD CHECK (street <> ''),
    ADD CHECK (phone <> ''),
    ADD CHECK (phone_mobile <> ''),
    ADD CHECK (email <> ''),
    ADD CHECK (route_planning <> ''),
    ADD CHECK (type <> '');

ALTER TABLE disp
    ADD CHECK (name <> ''),
    ADD CHECK (format <> '');

ALTER TABLE status_lev
    ADD CHECK (lev <> '');

ALTER TABLE status_val
    ADD CHECK (val <> '');

ALTER TABLE mpg_transf
    ADD CHECK (ext_id <> ''),
    ADD CHECK (name <> '');

ALTER TABLE tz
    ADD CHECK (name <> '');

UPDATE import_conf SET from_val = NULL WHERE from_val = '';
ALTER TABLE import_conf
    ADD CHECK (attribute <> ''),
    ADD CHECK (from_val <> ''),
    ADD CHECK (to_val <> '');

ALTER TABLE grid_col_mp
    ADD CHECK (grid_col <> ''),
    ADD CHECK (data_index <> '');

UPDATE grid_col_conf SET sort = NULL WHERE sort = '';
ALTER TABLE grid_col_conf
    ADD CHECK (sort <> '');

ALTER TABLE tag
    ADD CHECK (name <> '');

UPDATE convers_dm_fm SET env_descrip_pattern = NULL WHERE env_descrip_pattern = '';
ALTER TABLE convers_dm_fm
    ADD CHECK (env_descrip_pattern <> '');

UPDATE ref_val_measure SET descr = NULL WHERE descr = '';
ALTER TABLE ref_val_measure
    ADD CHECK (measure <> ''),
    ADD CHECK (descr <> '');

UPDATE ref_val SET specif = NULL WHERE specif = '';
ALTER TABLE ref_val
    ADD CHECK (specif <> '');

UPDATE targ_act_mmt_gr SET name = NULL WHERE name = '';
UPDATE targ_act_mmt_gr SET descr = NULL WHERE descr = '';
ALTER TABLE targ_act_mmt_gr
    ADD CHECK (name <> ''),
    ADD CHECK (descr <> '');

UPDATE targ_env_gr SET name = NULL WHERE name = '';
UPDATE targ_env_gr SET targ_env_gr_displ = NULL WHERE targ_env_gr_displ = '';
ALTER TABLE targ_env_gr
    ADD CHECK (name <> ''),
    ADD CHECK (targ_env_gr_displ <> '');

-- Lada tables
set search_path to lada;

UPDATE mpg SET comm_mpg = NULL WHERE comm_mpg = '';
UPDATE mpg SET comm_sample = NULL WHERE comm_sample = '';
UPDATE mpg SET sample_quant = NULL WHERE sample_quant = '';
ALTER TABLE mpg
    ADD CHECK (comm_mpg <> ''),
    ADD CHECK (sample_pd <> ''),
    ADD CHECK (comm_sample <> ''),
    ADD CHECK (sample_quant <> '');

UPDATE sample SET main_sample_id = NULL WHERE main_sample_id = '';
UPDATE sample SET env_descrip_name = NULL WHERE env_descrip_name = '';
ALTER TABLE sample
    ADD CHECK (ext_id <> ''),
    ADD CHECK (main_sample_id <> ''),
    ADD CHECK (env_descrip_name <> '');

UPDATE comm_sample SET text = NULL WHERE text = '';
ALTER TABLE comm_sample
    ADD CHECK (text <> '');

UPDATE geolocat SET add_site_text = NULL WHERE add_site_text = '';
ALTER TABLE geolocat
    ADD CHECK (add_site_text <> '');

UPDATE geolocat_mpg SET add_site_text = NULL WHERE add_site_text = '';
ALTER TABLE geolocat_mpg
    ADD CHECK (add_site_text <> '');

UPDATE sample_specif_meas_val SET smaller_than = NULL WHERE smaller_than = '';
ALTER TABLE sample_specif_meas_val
    ADD CHECK (smaller_than <> '');

UPDATE measm SET min_sample_id = NULL WHERE min_sample_id = '';
ALTER TABLE measm
    ADD CHECK (min_sample_id <> '');

UPDATE comm_measm SET text = NULL WHERE text = '';
ALTER TABLE comm_measm
    ADD CHECK (text <> '');

UPDATE meas_val SET less_than_lod = NULL WHERE less_than_lod = '';
ALTER TABLE meas_val
    ADD CHECK (less_than_lod <> '');

UPDATE status_prot SET text = NULL WHERE text = '';
ALTER TABLE status_prot
    ADD CHECK (text <> '');

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
