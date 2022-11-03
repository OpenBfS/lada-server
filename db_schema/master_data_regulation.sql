--
-- PostgreSQL database dump
--

-- Dumped from database version 10.2
-- Dumped by pg_dump version 10.2

-- Started on 2018-02-20 08:42:18 CET

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
-- TOC entry 5409 (class 0 OID 3408523)
-- Dependencies: 235
-- Data for Name: regulation; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY regulation (id, descr, regulation) FROM stdin;
9	Europa	Europa
13	SPARSE NETWORK	SPARSE
14	DENSE NETWORK	DENSE
4	REI-Immissionsdaten	REI-I
6	KFÜ	KFÜ
7	Landesdaten	Land
2	§162-Daten	§162
1	§161-Daten	§161
10	§161-Daten/SPARS	§162/SPARSE
\.


--
-- TOC entry 5414 (class 0 OID 0)
-- Dependencies: 234
-- Name: regulation_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('regulation_id_seq', 14, true);


-- Completed on 2018-02-20 08:42:19 CET

--
-- PostgreSQL database dump complete
--

