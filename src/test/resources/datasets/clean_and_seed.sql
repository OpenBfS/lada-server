SET search_path TO master;

-- cleanup
DELETE FROM auth;
DELETE FROM lada.tag_link;
DELETE FROM tag;
DELETE FROM lada.geolocat;
DELETE FROM lada.geolocat_mpg;
DELETE FROM site;
DELETE FROM site_class;
DELETE FROM type_regulation;
DELETE FROM poi;
DELETE FROM lada.sample;
DELETE FROM lada.mpg;
DELETE FROM lada.measm;
DELETE FROM oblig_measd_mp;
DELETE FROM mpg_transf;
DELETE FROM regulation;
DELETE FROM env_specif_mp;
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
DELETE FROM meas_facil;
DELETE FROM sampler;
DELETE FROM mpg_categ;
DELETE FROM network;
DELETE FROM sample_meth;
DELETE FROM sample_specif;
DELETE FROM spat_ref_sys;
DELETE FROM state;
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

-- seed
-- minimal master data to make interface tests runnable
INSERT INTO opr_mode (id, name) VALUES (1, 'Normal-/Routinebetrieb');
INSERT INTO site_class (id) VALUES (1);
INSERT INTO regulation (id) VALUES (9);
INSERT INTO regulation (id) VALUES (2);
INSERT INTO meas_unit (id) VALUES (207);
INSERT INTO meas_unit (id) VALUES (208);
INSERT INTO unit_convers (from_unit_id, to_unit_id, factor)
       VALUES (207, 208, 2);
INSERT INTO measd (id, name) VALUES (56, 'Mangan');
INSERT INTO measd (id, name) VALUES (57, 'Mangan');
INSERT INTO mmt (id) VALUES ('A3'), ('B3');
INSERT INTO measd_gr (id) VALUES (1);
INSERT INTO measd_gr_mp (measd_gr_id, measd_id) VALUES (1, 56);
INSERT INTO mmt_measd_gr_mp (measd_gr_id, mmt_id) VALUES (1, 'A3');
INSERT INTO network (id) VALUES ('06');
INSERT INTO network (id) VALUES ('01');
INSERT INTO meas_facil (id, network_id) VALUES ('06010', '06');
INSERT INTO meas_facil (id, network_id) VALUES ('01010', '01');
INSERT INTO env_medium (id, name, unit_1) VALUES
    ('L6', 'Spurenmessung Luft', 208),
    ('A6', 'Umweltbereich für test', null);
INSERT INTO oblig_measd_mp (
        id, measd_id, mmt_id, env_medium_id, regulation_id
    ) VALUES (33, 56, 'A3', 'A6', 9);
INSERT INTO sample_meth (id, ext_id, eudf_sample_meth_id) VALUES (1, 'E', 'A');
INSERT INTO sample_meth (id, ext_id, eudf_sample_meth_id) VALUES (2, 'S', 'B');
INSERT INTO sample_specif (id, name, ext_id)
       VALUES ('A74', 'Volumenstrom', 'VOLSTR');
INSERT INTO sample_specif (id, name, ext_id)
       VALUES ('A75', 'Volumenstrom', 'VOLSTR');
INSERT INTO sample_specif (id, name, ext_id)
       VALUES ('A76', 'Volumenstrom', 'VOLSTR');
INSERT INTO env_specif_mp (id, sample_specif_id, env_medium_id) VALUES (101, 'A74', 'A6');
INSERT INTO spat_ref_sys (id) VALUES (4), (5);
INSERT INTO state (id, ctry, ctry_orig_id, iso_3166, coord_x_ext, coord_y_ext)
       VALUES (0, 'Deutschland', 0, 'DE', '123123', '321321');
INSERT INTO admin_unit (
            id, state_id, name,
            is_state, is_munic, is_rural_dist, is_gov_dist)
       VALUES ('11000000', '11000000', 'Berlin', true, true, true, false);
INSERT INTO sampler (
			id, network_id, ext_id, descr, short_text)
		VALUES (726, '06', 'prn', 'test', 'test');
INSERT INTO mpg_transf VALUES (1, 1, 'Routinemessprogramm', 1, 2);
INSERT INTO targ_act_mmt_gr VALUES (1, 'descr', 'name');
INSERT INTO targ_env_gr VALUES (1, 'name', 'display');
INSERT INTO rei_ag_gr (id, name, descr) VALUES (101, 'name', 'descr');
-- authorization data needed for tests
INSERT INTO lada_user (id, name) VALUES (2, 'testeins');
INSERT INTO auth (ldap_gr, network_id, meas_facil_id, auth_funct_id)
       VALUES ('mst_06_status', '06', '06010', 1);
INSERT INTO auth (ldap_gr, network_id, meas_facil_id, auth_funct_id)
       VALUES ('land_06_stamm', '06', '06010', 4);

/*
 We have to use SQL to add ort data because geometry field does not work
 with @UsingDataSet
 Keep this in sync with dbUnit_ort.json, which is still used
 to verify test results!
*/
INSERT INTO master.site (
    id,
    network_id,
    ext_id,
    long_text,
    state_id,
    admin_unit_id,
    is_fuzzy,
    spat_ref_sys_id,
    coord_x_ext,
    coord_y_ext,
    last_mod,
    geom,
    site_class_id,
    short_text,
    rei_report_text
) VALUES (
    1000,
    '06',
    'D_ 00191',
    'Langer Text',
    0,
    '11000000',
    TRUE,
    5,
    '32487017',
    '5519769',
    '2015-03-01 12:00:00',
    'SRID=4326;POINT(49.83021 8.81948)',
    1,
    'kurz',
    'bericht'
);
