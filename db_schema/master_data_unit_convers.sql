--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 11.2

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
-- Data for Name: unit_convers; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.unit_convers (id, from_unit_id, to_unit_id , factor) FROM stdin;
1	1	3	0.00100000000000000002
2	2	3	0.0100000000000000002
3	4	3	1000
4	6	7	0.000100000000000000005
5	8	7	1000000
6	9	7	10000
7	10	11	9.99999999999999955e-07
8	14	13	60
9	15	13	3600
10	16	13	86400
11	17	13	604800
12	18	13	2629800
13	19	13	31557600
14	20	21	0.00100000000000000002
15	22	21	1000
16	25	27	0.00100000000000000002
17	26	27	0.0100000000000000002
18	28	27	100
19	31	30	0.0166666669999999996
20	32	30	0.000277778000000000009
21	33	30	1.15740999999999999e-05
22	35	36	0.0100000000000000002
23	37	36	0.0166666669999999996
24	38	36	1.15739999999999999e-08
25	42	43	60
26	44	43	0.0416666669999999975
27	47	46	0.00100000000000000002
28	48	45	0.100000000000000006
29	53	49	1000
30	54	49	2.73785078700000017
31	70	80	1.00000000000000006e-09
32	71	80	9.99999999999999955e-07
33	72	80	0.00100000000000000002
34	73	81	1.00000000000000006e-09
35	74	81	9.99999999999999955e-07
36	75	81	0.00100000000000000002
37	76	82	9.99999999999999955e-07
38	77	82	4.16669999999999985e-08
39	78	83	9.99999999999999955e-07
40	79	83	4.16669999999999985e-08
41	86	82	1.14076999999999996e-07
42	97	61	1000
43	25	11	9.99999999999999955e-07
44	26	11	1.00000000000000008e-05
45	27	11	0.00100000000000000002
46	28	11	0.100000000000000006
47	42	30	1.66667000000000009e-05
48	43	30	2.77777999999999988e-07
49	44	30	1.15739999999999999e-08
50	63	62	1000
51	73	80	1.00000000000000006e-09
52	74	80	9.99999999999999955e-07
53	75	80	0.00100000000000000002
54	78	76	1
55	79	82	4.16669999999999985e-08
56	83	82	1
97	88	62	0.00100000000000000002
98	99	63	0.00100000000000000002
99	100	76	0.00100000000000000002
100	84	76	0.00100000000000000002
101	102	62	1000
102	105	62	9.99999999999999955e-07
103	107	62	1.00000000000000006e-09
104	162	64	0.00100000000000000002
105	62	63	0.00100000000000000002
106	103	63	1000
107	102	63	1
108	88	63	9.99999999999999955e-07
109	108	63	9.99999999999999955e-07
110	107	63	1.00000000000000006e-09
113	64	63	1
114	160	63	1000
115	161	63	1000
116	162	63	0.00100000000000000002
117	159	63	9.99999999999999955e-07
118	64	161	0.00100000000000000002
119	64	162	1000
121	101	67	1000
122	104	67	1000
123	109	67	0.00100000000000000002
124	106	67	9.99999999999999955e-07
125	64	159	1000000
126	161	64	1000
127	111	61	1000000
128	112	61	1
129	110	61	0.00100000000000000002
130	159	64	9.99999999999999955e-07
131	76	78	1
132	76	100	1000
133	76	84	1000
134	62	102	0.00100000000000000002
135	62	88	1000
136	62	105	1000000
137	62	107	1000000000
138	163	65	9.99999999999999955e-07
140	63	103	0.00100000000000000002
141	63	102	1
142	63	99	1000
143	63	88	1000000
144	63	108	1000000
145	63	107	1000000000
146	164	65	1000
148	63	64	1
150	63	104	0.00100000000000000002
151	63	109	1000
152	63	106	1000000
153	65	139	0.00100000000000000002
154	65	166	1000
155	65	163	1000000
157	67	104	0.00100000000000000002
158	67	109	1000
159	67	106	1000000
160	166	65	0.00100000000000000002
161	160	64	1000
162	61	97	0.00100000000000000002
163	61	111	9.99999999999999955e-07
164	61	112	1
165	61	110	1000
166	64	160	0.00100000000000000002
177	139	65	1000
178	1	2	0.100000000000000006
239	86	76	0.114199999999999996
259	247	61	10000
260	61	247	0.000100000000000000005
278	267	63	0.00100000000000000002
298	71	72	0.00100000000000000002
\.


--
-- Name: unit_convers_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.unit_convers_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

