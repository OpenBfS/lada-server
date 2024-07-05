SET search_path TO master;

-- cleanup
DELETE FROM auth;
DELETE FROM lada.tag_link_measm;
DELETE FROM lada.tag_link_sample;
DELETE FROM tag;
DELETE FROM lada.geolocat;
DELETE FROM lada.geolocat_mpg;
DELETE FROM site;
DELETE FROM type_regulation;
DELETE FROM poi;
DELETE FROM lada.sample;
DELETE FROM lada.mpg;
DELETE FROM lada.measm;
DELETE FROM oblig_measd_mp;
DELETE FROM mpg_transf;
DELETE FROM regulation;
DELETE FROM env_specif_mp;
DELETE FROM rei_ag_gr_env_medium_mp;
DELETE FROM env_descrip_env_medium_mp;
DELETE FROM env_medium;
DELETE FROM unit_convers;
DELETE FROM meas_unit;
DELETE FROM mmt_measd_gr_mp;
DELETE FROM measd_gr_mp;
DELETE FROM measd_gr;
DELETE FROM measd;
DELETE FROM mmt;
DELETE FROM munic_div;
DELETE FROM dataset_creator;
DELETE FROM import_conf;
DELETE FROM meas_facil;
DELETE FROM sampler;
DELETE FROM mpg_categ;
DELETE FROM network;
DELETE FROM sample_meth;
DELETE FROM sample_specif;
DELETE FROM state;
DELETE FROM spat_ref_sys;
DELETE FROM admin_unit;
DELETE FROM env_descrip;
DELETE FROM opr_mode;
DELETE FROM grid_col_conf;
DELETE FROM grid_col_mp;
DELETE FROM filter;
DELETE FROM disp;
DELETE FROM query_user;
DELETE FROM base_query;
DELETE FROM lada_user;
DELETE FROM targ_act_mmt_gr;
DELETE FROM targ_env_gr;
DELETE FROM rei_ag_gr;
DELETE FROM audit_trail;
DELETE FROM lada.audit_trail;
DELETE FROM lada.status_prot;
DELETE FROM nucl_facil_gr_mp;
DELETE FROM nucl_facil_gr;

-- Reset id sequences to prevent unique constraint violations while creating
-- new objects during the tests
ALTER SEQUENCE lada.sample_id_seq RESTART WITH 1;
ALTER SEQUENCE lada.status_prot_id_seq RESTART WITH 1;
