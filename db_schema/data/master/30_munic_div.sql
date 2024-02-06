--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 10.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: munic_div; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.munic_div (id, network_id, admin_unit_id, site_id, name, last_mod) FROM stdin;
1	11	11000000	28	Johannisthal	2011-11-21 10:35:17
\.


--
-- Name: munic_div_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.munic_div_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

