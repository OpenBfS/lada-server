\set ON_ERROR_STOP on

SET search_path = stamm, pg_catalog;

COPY tag (tag, mst_id, netzbetreiber, typ) FROM stdin;
test_global	\N	\N	global
test_netz	\N	06	netzbetreiber
test_mst	06010	\N	mst
\.
