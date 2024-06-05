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
-- TOC entry 4508 (class 0 OID 1992976)
-- Dependencies: 283
-- Data for Name: nucl_facil; Type: TABLE DATA; Schema: stammdaten; Owner: postgres
--

COPY nucl_facil (ext_id, name) FROM stdin;
U01A	Helmholtz-Zentrum Geesthacht
U01B	KKW Krümmel
U01C	KKW Brunsbüttel
U01D	KKW Brokdorf
U01I	Interimslager Krümmel
U01K	Standortzwischenlager Krümmel
U01L	Standortzwischenlager Brunsbüttel
U01M	Standortzwischenlager Brokdorf
U03A	Standortzwischenlager Grohnde
U03B	Brennelementefertigungsanl. Lingen
U03C	Standortzwischenlager Unterweser
U03E	KKW Emsland
U03F	Forschungsbergwerk Asse
U03G	KKW Grohnde
U03K	Endlager Konrad
U03L	KKW Lingen
U03P	GNS - Werk Gorleben -
U03S	KKW Stade
U03U	KKW Unterweser
U03Z	Standortzwischenlager Lingen
U05B	Brennelement-Zwischenl. Ahaus
U05F	Forschungszentrum Jülich
U05G	AVR-Versuchskernkraftwerk Jülich
U05K	KKW Würgassen
U05T	Thorium-Hochtemp.reakt. Hamm-Uentrop
U05U	Urananreicherungsanlage Gronau
U06B	KKW Biblis und BE-Zwischenlager
U07M	KKW Mülheim-Kärlich
U07U	Uni Mainz
U08H	DKFZ Heidelberg
U08K	Karlsruher Institut für Technologie - Campus Nord
U08M	Abraumhalde Menz.
U08N	EnKK Neckarwestheim
U08O	EnKK Obrigheim
U08P	EnKK Philippsburg
U08W	KKW Wyhl
U09A	KKW Isar 1+2
U09B	KKW Isar1
U09C	KKW Isar2
U09D	KKW Grafenrheinfeld
U09E	KKW Gundremmingen Block B/C
U09F	Versuchs-AKW Kahl a.M.
U09G	Forschungsreaktor München
U09H	Siemens Brennelementewerk Hanau, Standort Karlstein
U09I	Siemens AG - AREVA NP GmbH, Standort Karlstein
U09J	AREVA NP GmbH, Standort Erlangen
U09K	Forschungsneutronenquelle Heinz Maier-Leibnitz
U11B	Experimentierreakt. II Berlin
U12R	KKW Rheinsberg
U13A	KKW Lubmin/Greifswald
U13B	Zwischenlager Nord
U14R	Forschungszentrum Rossendorf
U15M	nicht benutzen, jetzt UELM, Endlager für radioaktive Abfälle Morsleben (ERAM)
UCHL	KTA Leibstadt mit Beznau und Villigen
UELA	Endlager für radioaktive Abfälle Asse
UELM	Endlager für radioaktive Abfälle Morsleben (ERAM)
UFRC	KKW Cattenom
UFRF	KKW Fessenheim
\.


COPY nucl_facil_gr (id, ext_id, name) FROM stdin;
1	U01A	Helmholtz-Zentrum Geesthacht
2	U01A/B	Helmholtz-Zentrum Geesthacht / KKW Krümmel
3	U01B	KKW Krümmel
4	U01B/K	KKW Krümmel / Standortzwischenlager Krümmel
5	U01C	KKW Brunsbüttel
6	U01C/D	KKW Brunsbüttel / KKW Brokdorf
7	U01D	KKW Brokdorf
8	U01D/M	KKW Brokdorf / Standortzwischenlager Brokdorf
9	U01I	Interimslager Krümmel
10	U01I/K	Interimslager Krümmel /Standortzwischenlager Brokdorf
11	U01K	Standortzwischenlager Krümmel
12	U01L	Standortzwischenlager Brunsbüttel
13	U01M	Standortzwischenlager Brokdorf
14	U03A	Standortzwischenlager Grohnde
15	U03B	Brennelementefertigungsanl. Lingen
16	U03C	Standortzwischenlager Unterweser
17	U03C/U	KKW Unterweser / Standortzwischenlager Unterweser / 
18	U03E	KKW Emsland
19	U03F	Forschungsbergwerk Asse
20	U03G	KKW Grohnde
21	U03K	Endlager Konrad
22	U03L	KKW Lingen
23	U03P	GNS - Werk Gorleben -
24	U03S	KKW Stade
25	U03U	KKW Unterweser
26	U03Z	Standortzwischenlager Lingen
27	U05B	Brennelement-Zwischenl. Ahaus
28	U05F	Forschungszentrum Jülich
29	U05G	AVR-Versuchskernkraftwerk Jülich
30	U05K	KKW Würgassen
31	U05T	Thorium-Hochtemp.reakt. Hamm-Uentrop
32	U05U	Urananreicherungsanlage Gronau
33	U06B	KKW Biblis und BE-Zwischenlager
34	U07M	KKW Mülheim-Kärlich
35	U07U	Uni Mainz
36	U08H	DKFZ Heidelberg
37	U08K	Karlsruher Institut für Technologie - Campus Nord
38	U08M	Abraumhalde Menz.
39	U08N	EnKK Neckarwestheim
40	U08O	EnKK Obrigheim
41	U08P	EnKK Philippsburg
42	U08W	KKW Wyhl
43	U09A	KKW Isar 1+2
44	U09A/B	KKW Isar 1+2 / KKW Isar 1
45	U09B	KKW Isar1
46	U09C	KKW Isar2
47	U09D	KKW Grafenrheinfeld
48	U09E	KKW Gundremmingen Block B/C
49	U09F	Versuchs-AKW Kahl a.M.
50	U09G	Forschungsreaktor München
51	U09H	Siemens Brennelementewerk Hanau, Standort Karlstein
52	U09I	Siemens AG - AREVA NP GmbH, Standort Karlstein
53	U09J	AREVA NP GmbH, Standort Erlangen
54	U09K	Forschungsneutronenquelle Heinz Maier-Leibnitz
55	U09K/G	Forschungsreaktor München / Forschungsneutronenquelle Heinz Maier-Leibnitz
56	U11B	Experimentierreakt. II Berlin
57	U12R	KKW Rheinsberg
58	U13A	KKW Lubmin/Greifswald
59	U13B	Zwischenlager Nord
60	U14R	Forschungszentrum Rossendorf
61	U15M	nicht benutzen, jetzt UELM, Endlager für radioaktive Abfälle Morsleben (ERAM)
62	UCHL	KTA Leibstadt mit Beznau und Villigen
63	UELA	Endlager für radioaktive Abfälle Asse
64	UELM	Endlager für radioaktive Abfälle Morsleben (ERAM)
65	UFRC	KKW Cattenom
66	UFRF	KKW Fessenheim
\.

SELECT pg_catalog.setval('nucl_facil_gr_id_seq', (SELECT max(id) FROM nucl_facil_gr), true);


COPY nucl_facil_gr_mp (id, nucl_facil_gr_id, nucl_facil_ext_id) FROM stdin;
1	1	U01A
2	3	U01B
3	5	U01C
4	7	U01D
5	9	U01I
6	11	U01K
7	12	U01L
8	13	U01M
9	14	U03A
10	15	U03B
11	16	U03C
12	18	U03E
13	19	U03F
14	20	U03G
15	21	U03K
16	22	U03L
17	23	U03P
18	24	U03S
19	25	U03U
20	26	U03Z
21	27	U05B
22	28	U05F
23	29	U05G
24	30	U05K
25	31	U05T
26	32	U05U
27	33	U06B
28	34	U07M
29	35	U07U
30	36	U08H
31	37	U08K
32	38	U08M
33	39	U08N
34	40	U08O
35	41	U08P
36	42	U08W
37	43	U09A
38	45	U09B
39	46	U09C
40	47	U09D
41	48	U09E
42	49	U09F
43	50	U09G
44	51	U09H
45	52	U09I
46	53	U09J
47	54	U09K
48	56	U11B
49	57	U12R
50	58	U13A
51	59	U13B
52	60	U14R
53	61	U15M
54	62	UCHL
55	63	UELA
56	64	UELM
57	65	UFRC
58	66	UFRF
59	2	U01A
60	2	U01B
61	4	U01B
62	4	U01K
63	6	U01C
64	6	U01D
65	8	U01D
66	8	U01M
67	10	U01I
68	10	U01K
69	17	U03C
70	17	U03U
71	44	U09A
72	44	U09B
73	55	U09G
74	55	U09K
\.

SELECT pg_catalog.setval('nucl_facil_gr_mp_id_seq', (SELECT max(id) FROM nucl_facil_gr_mp), true);
