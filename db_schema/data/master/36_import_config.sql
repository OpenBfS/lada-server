--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 10.1

-- Started on 2017-11-23 10:33:56 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = master, pg_catalog;

--
-- TOC entry 5304 (class 0 OID 17026474)
-- Dependencies: 314
-- Data for Name: import_conf; Type: TABLE DATA; Schema: stammdaten; Owner: postgres
--

COPY import_conf (id, attribute, meas_facil_id, from_val, to_val, action) FROM stdin;
1	messEinheit	06010	BQ	Bq	CONVERT
2	messEinheit	06010	BQ/KG	Bq/kg	CONVERT
3	messEinheit	06010	Bq/kgFM	Bq/kg(FM)	CONVERT
4	messEinheit	06010	BQ/kgFM	Bq/kg(FM)	CONVERT
5	messEinheit	06010	Bq/KGFM	Bq/kg(FM)	CONVERT
6	messEinheit	06010	Bq/kgGR	Bq/kg(GR)	CONVERT
7	messEinheit	06010	BQ/KGGR	Bq/kg(GR)	CONVERT
8	messEinheit	06010	Bq/kgTM	Bq/kg(TM)	CONVERT
9	messEinheit	06010	BQ/KGTM	Bq/kg(TM)	CONVERT
10	messEinheit	06010	Bq/m2	Bq/m²	CONVERT
11	messEinheit	06010	Bq/m3	Bq/m³	CONVERT
12	messEinheit	06010	KBQ/Kg	kBq/kg	CONVERT
13	messEinheit	06010	KBQ/KG	kBq/kg	CONVERT
14	messEinheit	06010	KBQ/Kgtm	kBq/kg(TM)	CONVERT
16	messgroesse	06010	20	2d	TRANSFORM
18	deskriptoren	06010	2d	30	TRANSFORM
19	messgroesse	06060	20	2d	TRANSFORM
20	messEinheit	11010	Bq/d*P	Bq/(d*p)	CONVERT
21	messEinheit	11010	Bq/ d*P	Bq/(d*p)	CONVERT
22	messEinheit	11010	Bq/ (d*P)	Bq/(d*p)	CONVERT
23	messEinheit	11010	Bq/(d*P)	Bq/(d*p)	CONVERT
25	messgroesse	11010	20	2d	TRANSFORM
26	messstelle	12010	\N	12020	DEFAULT
27	messgroesse	12010	20	2d	TRANSFORM
29	messgroesse	12020	20	2d	TRANSFORM
30	zeitbasis	06010	\N	ges. Zeit	DEFAULT
\.


--
-- TOC entry 5309 (class 0 OID 0)
-- Dependencies: 313
-- Name: import_conf_id_seq; Type: SEQUENCE SET; Schema: stammdaten; Owner: postgres
--

SELECT pg_catalog.setval('import_conf_id_seq', (SELECT max(id) FROM import_conf), true);


-- Completed on 2017-11-23 10:33:59 CET

--
-- PostgreSQL database dump complete
--

