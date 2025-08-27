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
-- TOC entry 4697 (class 0 OID 535760)
-- Dependencies: 262
-- Data for Name: measd_gr; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY measd_gr (id, name, ref_nucl_gr) FROM stdin;
57	Messgrössen für nuklidspezifische Gammamessung	0
97	Gesamt-Alpha und Beta	0
30	Sr-Isotope (insbes. Sr 90)	0
31	Iod-Isotope (insbes. I 131)	0
32	Alpha-Teilchen emittierende Pu-Isotope (insbes. Pu 239, Am 241)	0
33	sonst. Nuklide mit Halbwertzeiten > 10d (insbes. Cs 134, Cs 137)	0
34	Cs-Isotope (Cs 134, Cs 137)	0
5	Alphaspektrometrie Messgrößen	0
6	Berechnete Größen	0
7	Beta Größen	0
8	Elementbestimmungsmessgrößen	0
10	Gamma-OD	0
11	Gamma-ODL	0
12	Gamma-Spektrometrie Iod 131	0
13	Gammaspektrometrie Messgrößen	0
14	Gesamt-Alpha-Aktivität	0
15	Gesamt-Alpha-Aktivität, Handmonitor	0
16	Gesamt-Alpha-Aktivität, verzögert	0
17	Gesamt-Beta-Aktivität	0
18	Gesamt-Beta-Aktivität, Handmonitor	0
19	Gesamt-Beta-Aktivität, verzögert / Künstlich Gesamt-Beta	0
20	Gesamt-Cäsium-Aktivität	0
21	Gesamt-Gamma-Aktivität	0
22	Neutronen-Dosisleistung	0
23	Neutronen-Ortsdosis	0
24	Niederschlagsintensität	0
25	Niederschlagsmenge	0
26	nuklidspezifische Dosisleistung	0
3	PARK - Alpha-Strahler	0
2	PARK - Beta-Strahler	0
4	PARK - Edelgase	0
1	PARK - Gamma-Strahler (aerosolgebunden vorkommend)	0
27	Rest-Beta	0
28	Schneehöhe	0
29	sonstige radiologische Meßmethode	0
35	Cs 137	0
36	I 131	0
37	Sr 89/90	0
55	H 3	0
56	C 14	0
77	Edelgas-Messgrößen	0
119	Gamma-ODL (Handmessung)	0
117	Alphaspektrometrie Uran	0
118	Alphaspektrometrie Plutonium	0
\.


--
-- TOC entry 4701 (class 0 OID 535771)
-- Dependencies: 266
-- Data for Name: measd_gr_mp; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY master.measd_gr_mp (measd_gr_id, measd_id) FROM stdin;
29	Ac-228
29	Th-228
29	Pa-234m
29	U-234
29	U-235
29	U-238
29	Gamma-ODL
29	G-Beta
29	G-Alpha
29	Gamma-ODL-Brutto
29	Summe
30	Sr-89
30	Sr-90
30	Sr-91
30	Sr-92
31	I-131
31	I-132
31	I-133
31	I-135
32	Pu-239
32	Am-241
32	Pu-23940
33	Cs-134
33	Cs-137
33	Ba-140
34	Cs-134
34	Cs-137
35	Cs-137
36	I-131
37	Sr-89
37	Sr-90
55	H-3
56	C-14
57	I-131
57	Cs-134
57	Cs-137
77	Kr-85
77	Xe-131m
77	Xe-133
77	Xe-135
97	G-Beta
97	G-Alpha
8	U-238
8	Pu-239
8	Am-241
8	U-235
8	U-234
12	I-132
13	Xe-131m
13	Lu-177
117	U-233
117	U-238
117	U-234
117	U-235
117	U-232
118	Pu-238
118	Pu-23940
118	Pu-236
118	Pu-239
118	Pu-240
119	Gamma-ODL
13	Na-24
1	Be-7
1	Na-22
1	K-40
1	Cr-51
1	Mn-54
1	Fe-59
1	Co-57
1	Co-58
1	Co-60
1	Zn-65
1	Sr-91
1	Sr-92
1	Y-92
1	Y-93
1	Zr-95
1	Zr-97
1	Nb-95
1	Nb-97
1	Mo-99
1	Tc-99m
1	Ru-103
1	Ru-106
1	Ag-110m
1	Sb-124
1	Sb-125
1	Sb-127
1	Sb-129
1	Te-123m
1	Te-129m
1	Te-129
1	Te-131m
1	Te-132
1	I-131
1	I-132
1	I-133
1	I-135
1	Cs-134
1	Cs-136
1	Cs-137
1	Ba-140
1	La-140
1	La-141
1	Ce-141
1	Ce-143
1	Ce-144
1	Nd-147
1	Pm-151
1	Hf-181
1	Tl-208
1	Pb-210
1	Pb-212
1	Pb-214
1	Bi-212
1	Bi-214
1	Ac-228
1	Pa-233
1	U-237
1	Np-239
2	H-3
2	Sr-89
2	Sr-90
3	Th-232
3	U-234
3	U-235
3	U-238
3	Np-237
3	Pu-238
3	Pu-239
3	Am-241
3	Cm-242
3	Cm-244
4	Ar-41
4	Kr-85m
4	Kr-87
4	Kr-88
4	Xe-133m
4	Xe-133
4	Xe-135m
4	Xe-135
5	Ra-226
5	Th-227
5	Th-228
5	Th-230
5	Th-232
5	U-232
5	U-233
5	U-234
5	U-235
5	U-238
5	Np-237
5	Pu-236
5	Pu-238
5	Pu-239
5	Pu-240
5	Am-241
5	Cm-242
5	Cm-243
5	Cm-244
5	Pu-23940
5	Cm-24344
6	Regen
7	H-3
7	C-14
7	Sr-89
7	Sr-90
7	Tc-99
7	Ra-226
8	K-40
8	U-nat
10	Gamma-OD-Brutto
10	Gamma-OD-Netto
11	Gamma-ODL
11	Gamma-ODL-Brutto
11	Gamma-ODL-Netto
11	Gamma-ODL-künstl.
11	Gamma-ODL-min
11	Gamma-ODL-max
12	Cd-113
12	In-115
12	I-129
12	I-131
12	I-131G
12	I-131E
12	I-131O
13	Be-7
13	Na-22
13	Ar-41
13	K-40
13	Sc-46
13	Cr-51
13	Mn-54
13	Fe-59
13	Co-57
13	Co-58
13	Co-60m
13	Co-60
13	Zn-65
13	Ga-65
13	Ga-67
13	Se-75
13	Kr-85m
13	Kr-85
13	Kr-87
13	Kr-88
13	Kr-89
13	Sr-85
13	Sr-91
13	Sr-92
13	Y-88
13	Y-92
13	Y-93
13	Zr-95
13	Zr-97
13	Nb-95
13	Nb-97
13	Mo-99
13	Tc-99m
13	Ru-103
13	Ru-105
13	Ru-106
13	Ag-108m
13	Ag-108
13	Ag-110m
13	Ag-110
13	Ag-111
13	Cd-109
13	In-111
13	Sn-113
13	Sb-124
13	Sb-125
13	Sb-127
13	Sb-129
13	Te-123m
13	Te-129m
13	Te-129
13	Te-131m
13	Te-132
13	I-125
13	I-129
13	I-131
13	I-132
13	I-133
13	I-135
13	Xe-133m
13	Xe-133
13	Xe-135m
13	Xe-135
13	Xe-137
13	Xe-138
13	Cs-134
13	Cs-136
13	Cs-137
13	Ba-133
13	Ba-140
13	La-138
13	La-140
13	La-141
13	Ce-139
13	Ce-141
13	Ce-143
13	Ce-144
13	Nd-147
13	Pm-147
13	Pm-149
13	Pm-151
13	Sm-153
13	Eu-152
13	Eu-154
13	Eu-155
13	Lu-176
13	Hf-181
13	Ta-182
13	Re-186
13	Hg-203
13	Tl-201
13	Tl-202
13	Tl-207
13	Tl-208
13	Pb-210
13	Pb-211
13	Pb-212
13	Pb-214
13	Bi-211
13	Bi-212
13	Bi-214
13	Rn-219
13	Ra-223
13	Ra-224
13	Ra-226
13	Ra-228
13	Ac-223
13	Ac-227
13	Ac-228
13	Th-227
13	Th-228
13	Th-230
13	Th-231
13	Th-232
13	Th-234
13	Pa-231
13	Pa-233
13	Pa-234m
13	Pa-234
13	U-235
13	U-237
13	U-238
13	Np-239
13	Am-241
13	I-131G
13	I-133G
13	I-135G
13	Summe
14	G-Alpha
14	G-Alpha-künstl.
14	G-Alpha-natürl.
15	G-Alpha
16	G-Alpha-2,5h_verz
16	G-Alpha-5h_verz
16	G-Alpha-24h_verz
16	G-Alpha-120h_verz
17	G-Beta
17	G-Beta-künstl.
17	G-Beta-natürl.
18	G-Beta
19	G-Beta-2h_verz
19	G-Beta-10h_verz
19	G-Beta-24h_verz
19	G-Beta-120h_verz
20	Cs-Gesamt
21	G-Gamma
21	G-Gamma-künstl.
22	Neutronen-ODL
22	Neutr-ODL-Brutto
22	Neutr-ODL-Netto
22	Neutr-ODL-min
22	Neutr-ODL-max
23	Neutronen-OD
23	Neutr-OD-Brutto
23	Neutr-OD-Netto
25	Regen
25	Schnee
25	Niederschlag
26	Be-7
26	Na-22
26	Ar-41
26	K-40
26	Cr-51
26	Mn-54
26	Fe-59
26	Co-57
26	Co-58
26	Co-60
26	Zn-65
26	Kr-85m
26	Kr-85
26	Kr-87
26	Kr-88
26	Kr-89
26	Sr-91
26	Sr-92
26	Y-92
26	Y-93
26	Zr-95
26	Zr-97
26	Nb-95
26	Nb-97
26	Mo-99
26	Tc-99m
26	Ru-103
26	Ru-106
26	Ag-110m
26	Sb-124
26	Sb-125
26	Sb-127
26	Sb-129
26	Te-123m
26	Te-129m
26	Te-129
26	Te-131m
26	Te-132
26	I-131
26	I-132
26	I-133
26	I-135
26	Xe-133m
26	Xe-133
26	Xe-135m
26	Xe-135
26	Xe-137
26	Xe-138
26	Cs-134
26	Cs-136
26	Cs-137
26	Ba-140
26	La-140
26	La-141
26	Ce-141
26	Ce-143
26	Ce-144
26	Nd-147
26	Pm-151
26	Hf-181
26	Tl-208
26	Pb-210
26	Pb-212
26	Pb-214
26	Bi-212
26	Bi-214
26	Ra-226
26	Ac-228
26	Pa-233
26	U-235
26	U-237
26	Np-239
27	Rest-Beta
28	Schnee
29	Be-7
29	K-40
29	Fe-55
29	Co-60
29	Ni-63
29	Sr-90
29	Ru-106
29	I-131
29	Cs-134
29	Cs-137
29	Pb-210
29	Ra-224
29	Ra-226
29	Ra-228
\.


--
-- TOC entry 4702 (class 0 OID 535774)
-- Dependencies: 267
-- Data for Name: mmt_measd_gr_mp; Type: TABLE DATA; Schema: stammdaten; Owner: lada
--

COPY mmt_measd_gr_mp (measd_gr_id, mmt_id) FROM stdin;
5	A1
6	S1
7	B1
8	E1
10	D1
11	O1
12	G3
12	GI
13	G1
13	I1
13	I2
14	A2
15	A3
16	A4
17	B2
18	B3
19	B4
21	G4
22	O2
23	D2
24	M2
25	M1
26	I3
27	B5
28	M3
29	S4
37	BS
37	BX
55	BH
56	BC
57	G2
77	BE
97	AB
117	AU
118	AP
119	O3
\.
