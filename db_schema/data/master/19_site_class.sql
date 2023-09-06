\set ON_ERROR_STOP on

--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.1
-- Dumped by pg_dump version 9.5.0

-- Started on 2016-03-31 11:38:13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = master, pg_catalog;


--
-- TOC entry 4706 (class 0 OID 535799)
-- Dependencies: 272
-- Data for Name: site_class; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY site_class (id, name, ext_id) FROM stdin;
1	dynamischer Messpunkt (nicht vordefiniert)	DYN
2	vordefinierter Messpunkt	GP
3	REI-Messpunkt	REI
4	Verwaltungseinheit	VE
5	Staat	ST
\.
--
-- PostgreSQL database dump complete
--
