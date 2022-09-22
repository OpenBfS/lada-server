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
-- TOC entry 4707 (class 0 OID 535802)
-- Dependencies: 273
-- Data for Name: type_regulation; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY type_regulation (id, name) FROM stdin;
U	Ursprungsort
R	Entnahmeort = Ursprungsort / REI-Messpunkt
E	Entnahmeort
\.
