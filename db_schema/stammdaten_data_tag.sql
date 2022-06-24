\set ON_ERROR_STOP on

SET search_path = stamm, pg_catalog;

COPY tag (tag, mst_id, netzbetreiber_id, tag_typ) FROM stdin;
test_global	\N	\N	global
test_netz	\N	06	netz
test_mst	06010	\N	mst
\.
