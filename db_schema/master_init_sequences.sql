SET search_path TO master;

SELECT pg_catalog.setval('auth_id_seq', (SELECT max(id) FROM auth), true);

SELECT pg_catalog.setval('auth_coord_ofc_env_medium_mp_id_seq', (SELECT max(id) FROM auth_coord_ofc_env_medium_mp) , true);

SELECT pg_catalog.setval('regulation_id_seq', (SELECT max(id) FROM regulation), true);

SELECT pg_catalog.setval('dataset_creator_id_seq', (SELECT max(id) FROM dataset_creator), true);

SELECT pg_catalog.setval('env_descrip_env_medium_mp_id_seq', (SELECT max(id) FROM env_descrip_env_medium_mp), true);

SELECT pg_catalog.setval('env_descrip_id_seq', (SELECT max(id) FROM env_descrip), true);
SELECT pg_catalog.setval('master.env_descrip_s_xx_seq', (SELECT max(s_xx) FROM master.env_descrip), true);

SELECT pg_catalog.setval('filter_id_seq', (SELECT max(id) FROM filter), true);

SELECT pg_catalog.setval('spat_ref_sys_id_seq', (SELECT max(id) FROM spat_ref_sys), true);

SELECT pg_catalog.setval('nucl_facil_id_seq', (SELECT max(id) FROM nucl_facil), true);

SELECT pg_catalog.setval('lada_user_id_seq', (SELECT max(id) FROM lada_user)+1, false);

SELECT pg_catalog.setval('meas_unit_id_seq', (SELECT max(id) FROM meas_unit), true);

SELECT pg_catalog.setval('measd_id_seq', (SELECT max(id) FROM measd), true);

SELECT pg_catalog.setval('measd_gr_id_seq', (SELECT max(id) FROM measd_gr), true);

SELECT pg_catalog.setval('mpg_categ_id_seq', (SELECT max(id) FROM mpg_categ), true);

SELECT pg_catalog.setval('site_id_seq', (SELECT max(id) FROM site), true);

SELECT pg_catalog.setval('oblig_measd_mp_id_seq', (SELECT max(id) FROM oblig_measd_mp), true);

SELECT pg_catalog.setval('sample_meth_id_seq', (SELECT max(id) FROM sample_meth), true);

SELECT pg_catalog.setval('sampler_id_seq', (SELECT max(id) FROM sampler), true);

SELECT pg_catalog.setval('base_query_id_seq', (SELECT max(id) FROM base_query), true);

SELECT pg_catalog.setval('state_id_seq', (SELECT max(id) FROM state), true);
