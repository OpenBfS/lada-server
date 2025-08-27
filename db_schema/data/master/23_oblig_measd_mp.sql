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
-- TOC entry 4708 (class 0 OID 535805)
-- Dependencies: 274
-- Data for Name: oblig_measd_mp; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY master.oblig_measd_mp (id, mmt_id, env_medium_id, regulation_id, measd_id) FROM stdin;
102	G1	A	2	Ce-144
103	G1	A	2	Cs-134
104	G1	A	2	I-131
105	G1	A	2	Ru-103
110	G1	F	2	Ce-144
111	G1	F	2	Cs-134
112	G1	F	2	I-131
113	G1	F	2	Ru-103
114	G1	G	2	Ce-144
115	G1	G	2	Cs-134
116	G1	G	2	I-131
117	G1	G	2	Ru-103
122	G1	L5	2	Ce-144
123	G1	L5	2	Cs-134
124	G1	L5	2	I-131
125	G1	L5	2	Ru-103
126	G1	N	2	Ce-144
127	G1	N	2	Cs-134
128	G1	N	2	I-131
129	G1	N	2	Ru-103
130	G1	Z	2	Ce-144
131	G1	Z	2	Cs-134
132	G1	Z	2	I-131
133	G1	Z	2	Ru-103
32	A2	A13	4	G-Alpha
33	A3	B2	4	G-Alpha
34	B3	B2	4	G-Beta
35	G1	B3	4	Co-60
36	G1	F1	4	Co-60
37	BH	F1	4	H-3
38	BC	F11	4	C-14
59	G1	G4	4	Co-60
60	A2	G4	4	G-Alpha
61	BH	G4	4	H-3
62	BS	G4	4	Sr-90
39	G1	F2	4	Co-60
40	BC	F2	4	C-14
41	G1	F3	4	Co-60
42	BC	F3	4	C-14
43	BH	F3	4	H-3
44	G1	F4	4	Co-60
45	BS	F4	4	Sr-90
46	G1	F5	4	Co-60
47	BC	F51	4	C-14
48	BH	F51	4	H-3
49	BC	F52	4	C-14
50	BH	F52	4	H-3
51	BS	F5Z	4	Sr-90
52	G1	G1	4	Co-60
53	A2	G11	4	G-Alpha
54	BH	G11	4	H-3
55	G1	G21	4	Co-60
56	A2	G21	4	G-Alpha
57	BH	G21	4	H-3
58	G1	G23	4	Co-60
63	G1	G5	4	Co-60
64	BH	G5	4	H-3
65	BS	G5	4	Sr-90
66	A2	G51	4	G-Alpha
67	A2	G53	4	G-Alpha
68	G1	GZ	4	Co-60
69	A2	GZ	4	G-Alpha
70	BH	GZ	4	H-3
80	O2	L22	4	Neutr-ODL-Netto
83	G1	N11	4	Co-60
84	GI	N11	4	I-131
85	BS	N11	4	Sr-90
86	G1	N12	4	Co-60
87	GI	N12	4	I-131
88	BS	N12	4	Sr-90
89	G1	N2	4	Co-60
90	BS	N2	4	Sr-90
91	G1	N31	4	Co-60
92	BS	N31	4	Sr-90
93	G1	N4	4	Co-60
94	BS	N4	4	Sr-90
95	G1	N55	4	Co-60
96	G1	N55	4	Cs-137
97	G1	N61	4	Co-60
98	G1	N62	4	Co-60
99	G1	N7	4	Co-60
100	BH	N7	4	H-3
101	BS	N7	4	Sr-90
71	G1	I13	4	Co-60
72	BC	I13	4	C-14
73	BH	I13	4	H-3
76	D2	L21	4	Neutr-OD-Netto
77	G1	L31	4	Co-60
78	A2	L31	4	G-Alpha
79	GI	L41	4	I-131
81	G1	L5	4	Co-60
82	A2	L5	4	G-Alpha
1	G1	A	2	K-40
2	G1	A	2	Co-60
3	G1	A	2	Cs-137
4	G1	B	2	K-40
5	G1	B	2	Co-60
6	G1	B	2	Cs-137
7	G1	F	2	K-40
8	G1	F	2	Co-60
9	G1	F	2	Cs-137
10	G1	G	2	K-40
11	G1	G	2	Co-60
12	G1	G	2	Cs-137
13	G1	I	2	K-40
14	G1	I	2	Co-60
15	G1	I	2	Cs-137
16	G1	L3	2	Be-7
17	G1	L3	2	Co-60
18	G1	L3	2	Cs-137
19	G1	L41	2	I-131
20	G1	L5	2	K-40
21	G1	L5	2	Co-60
22	G1	L5	2	Cs-137
23	G1	N	2	K-40
24	G1	N	2	Co-60
25	G1	N	2	Cs-137
26	G1	Z	2	K-40
27	G1	Z	2	Co-60
28	G1	Z	2	Cs-137
29	G1	L3	2	Cs-134
30	G1	L3	2	Pb-212
31	G1	L3	2	Pb-214
106	G1	B	2	Ce-144
107	G1	B	2	Cs-134
108	G1	B	2	I-131
109	G1	B	2	Ru-103
118	G1	I	2	Ce-144
119	G1	I	2	Cs-134
120	G1	I	2	I-131
121	G1	I	2	Ru-103
\.
