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
-- TOC entry 4676 (class 0 OID 535694)
-- Dependencies: 239
-- Data for Name: dataset_creator; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY dataset_creator (id, network_id, ext_id, meas_facil_id, descr, last_mod) FROM stdin;
453	06	KS	06010	Messstelle HLUG Kassel	2000-01-01 00:00:00
553	06	DA	06060	Messstelle HLUG Darmstadt	2000-01-01 00:00:00
\.
