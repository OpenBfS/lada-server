\set ON_ERROR_STOP on

SET search_path = master, pg_catalog;

COPY tag (name, meas_facil_id, network_id) FROM stdin;
test_global	\N	\N
test_netz	\N	06
test_mst	06010	\N
\.
