\set ON_ERROR_STOP on

SET search_path = master, pg_catalog;

COPY tag (name, meas_facil_id, network_id, tag_type) FROM stdin;
test_global	\N	\N	global
test_netz	\N	06	netz
test_mst	06010	\N	mst
\.
