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
-- Data for Name: tm_fm_umrechnung; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

SET SESSION AUTHORIZATION DEFAULT;

ALTER TABLE stamm.tm_fm_umrechnung DISABLE TRIGGER ALL;

COPY stamm.tm_fm_umrechnung (id, meh_id, meh_id_nach, umw_id, media_desk_pattern, faktor) FROM stdin;
1	67	65	A13	D: 08 02 02 %	3.000000000000
\.


ALTER TABLE stamm.tm_fm_umrechnung ENABLE TRIGGER ALL;

--
-- Name: tm_fm_umrechnung_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.tm_fm_umrechnung_id_seq', 1, true);


--
-- PostgreSQL database dump complete
--

