--
-- PostgreSQL database dump
--

-- Dumped from database version 12.6
-- Dumped by pg_dump version 12.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: convers_dm_fm; Type: TABLE DATA; Schema: master; Owner: postgres
--

SET SESSION AUTHORIZATION DEFAULT;

ALTER TABLE master.convers_dm_fm DISABLE TRIGGER ALL;

COPY master.convers_dm_fm (id, meas_unit_id, to_meas_unit_id , env_medium_id, env_descrip_pattern, conv_factor) FROM stdin;
1	67	65	A13	D: 08 02 02 %	3.000000000000
\.


ALTER TABLE master.convers_dm_fm ENABLE TRIGGER ALL;

--
-- Name: convers_dm_fm_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.convers_dm_fm_id_seq', 1, true);


--
-- PostgreSQL database dump complete
--

