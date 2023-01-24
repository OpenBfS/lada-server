--
-- PostgreSQL database dump
--

-- Dumped from database version 10.5
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
-- Data for Name: mpg; Type: TABLE DATA; Schema: lada; Owner: postgres
--

COPY lada.mpg (id, comm_mpg, is_test, is_active, meas_facil_id, appr_lab_id, regulation_id, opr_mode_id, admin_unit_id, env_descrip_display, env_medium_id, sample_meth_id, sample_pd, sample_pd_start_date, sample_pd_end_date, sample_pd_offset, valid_start_date, valid_end_date, sampler_id, mpg_categ_id, comm_sample, rei_ag_gr_id, nucl_facil_gr_id, meas_unit_id, last_mod) FROM stdin;
3474	falls nicht lieferbar bitte Ersatzprobe aus gleichem Umweltbereich	f	t	06010	06010	2	1	\N	D: 01 25 01 01 00 00 00 00 02 00 00 00	N21	1	J	150	210	0	1	365	78	\N	\N	\N	\N	67	2011-11-21 10:36:30
3513	falls nicht lieferbar bitte Ersatzprobe anderes Fruchtgemüse	f	t	06010	06010	2	1	\N	D: 01 25 03 02 00 00 00 00 02 00 00 00	N23	1	J	1	365	0	1	365	734	\N	\N	\N	\N	67	2011-11-21 10:36:30
3558	falls nicht lieferbar bitte Ersatzprobe aus gleichem Umweltbereich	f	t	06010	06010	2	1	\N	D: 01 29 01 02 00 00 00 00 02 00 00 00	N44	1	J	180	365	0	1	365	92	\N	\N	\N	\N	67	2011-11-21 10:36:30
3564	falls nicht lieferbar bitte Ersatzprobe aus gleichem Umweltbereich	f	t	06010	06010	2	1	\N	D: 01 29 03 07 00 00 00 00 00 00 00 00	N43	1	J	150	365	0	1	365	78	\N	\N	\N	\N	67	2011-11-21 10:36:40
3580	\N	f	t	06010	06010	2	1	\N	D: 01 24 01 00 00 00 00 00 00 00 00 00	N25	1	J	90	365	0	1	365	734	\N	\N	\N	\N	67	2011-11-21 10:36:30
3582	Dieter's Wurstladen GbR, Brunnengasse 4, 54397 Modautal OT Lützelbach	f	t	06060	06060	2	1	\N	D: 01 06 02 00 00 00 00 00 00 00 00 00	N51	1	Q	30	92	0	1	365	714	\N	\N	\N	\N	67	2012-11-08 14:33:01
3626	Schwälbchen Molkerei, Bad Schwalbach	f	t	06060	06060	2	1	\N	D: 01 01 01 07 02 00 00 00 00 00 00 00	N11	1	M	15	21	0	1	90	727	\N	\N	\N	\N	63	2017-10-12 13:21:00
3676	\N	f	t	06010	06010	2	1	\N	D: 04 01 24 84 00 00 60 15 00 31 07 00	B34	1	J	92	112	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:41:24
3678	\N	f	t	06010	06010	2	1	\N	D: 04 01 42 28 00 00 10 08 00 31 03 00	B31	1	J	92	112	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:41:55
3710	\N	f	t	06010	06010	2	1	\N	D: 04 02 42 26 00 00 01 25 00 31 03 00	B32	1	J	92	112	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:42:25
3716	\N	f	t	06010	06010	2	1	\N	D: 04 01 98 01 00 00 23 07 00 72 03 00	B33	1	J	92	112	0	1	365	60	\N	A76, A0-Horizont: Probenahmetiefe (Mächtigkeit der Humusschicht) notieren!	\N	\N	65	2018-10-17 10:43:53
3746	WW Eschollbrücken	f	t	06010	06010	2	1	\N	D: 01 59 03 01 01 02 05 01 02 00 00 00	N71	1	H	127	183	0	1	365	\N	\N	\N	\N	\N	63	2018-11-06 09:20:41.520944
3730	\N	f	t	06060	06060	2	1	\N	D: 06 02 00 01 00 00 01 02 02 00 00 00	G11	1	Q	43	56	0	1	365	60	\N	\N	\N	\N	63	2018-10-17 10:45:25
3734	\N	f	t	06010	06010	2	1	\N	D: 06 05 00 01 00 00 00 06 00 00 01 00	G12	3	Q	22	49	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:46:17
3749	WW Wiesbaden-Schierstein	f	t	06010	06010	2	1	\N	D: 01 59 03 01 01 03 03 01 02 00 00 00	N71	1	H	127	133	0	1	365	726	\N	\N	\N	\N	63	2018-10-17 12:58:42
3755	Wasserwerk 'Neue Mühle'	f	t	06010	06010	2	1	\N	D: 01 59 05 01 05 05 05 01 02 00 00 00	N73	1	Q	36	92	0	1	365	724	\N	\N	\N	\N	63	2013-10-30 12:43:23
3775	\N	f	t	06010	06010	2	1	\N	D: 08 02 04 01 00 00 04 02 00 00 00 00	A13	1	Q	8	92	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:52:51
3782	\N	f	t	06010	06010	2	1	\N	D: 08 01 03 01 00 00 00 06 00 00 00 00	A11	1	Q	8	92	0	1	365	60	\N	\N	\N	\N	63	2018-10-17 10:54:41
3786	\N	f	t	06010	06010	2	1	\N	D: 09 02 08 00 00 00 00 00 00 00 00 00	A31	1	H	127	183	0	1	365	60	\N	\N	\N	\N	63	2018-10-17 10:55:35
3792	\N	f	t	06010	06010	2	1	\N	D: 09 01 24 05 00 00 00 00 00 00 00 00	A21	1	H	64	183	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:55:53
3796	\N	f	t	06010	06010	2	1	\N	D: 09 01 07 01 00 00 00 00 00 00 00 00	A22	1	H	64	183	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:56:38
3797	\N	f	t	06010	06010	2	1	\N	D: 09 01 16 01 00 00 00 00 00 00 00 00	A23	1	H	64	183	0	1	365	60	\N	\N	\N	\N	65	2018-10-17 10:56:55
3798	Filtrat der Waschwasserreinigung	f	t	06010	06010	2	1	\N	D: 09 01 14 05 00 00 00 00 00 00 00 00	A24	1	H	64	183	0	1	365	60	\N	\N	\N	\N	63	2018-10-17 10:57:11
3801	\N	f	t	06060	06060	2	1	\N	D: 09 03 02 00 00 00 00 00 00 00 00 00	A41	1	H	127	183	0	1	365	60	\N	\N	\N	\N	65	2015-05-12 11:38:19
7051	Gesamtnahrung JVA	f	t	11010	11010	2	1	\N	D: 01 50 90 01 06 02 05 00 00 00 00 00	N81	3	W2	1	14	0	1	365	\N	\N	\N	\N	\N	69	2011-11-21 10:36:52
7053	\N	f	t	11010	11010	2	1	\N	D: 01 01 01 06 01 02 00 20 00 00 00 00	N12	1	M	1	31	0	1	365	\N	\N	\N	\N	\N	63	2014-10-07 11:38:22
7057	Gras; Friedenau	f	t	11010	11010	2	1	\N	D: 03 09 01 00 00 00 00 00 00 00 00 00	I13	1	J	127	273	0	1	365	\N	\N	\N	\N	\N	65	2011-11-21 10:36:52
7063	Weidegras; Lübars I	f	t	11010	11010	2	1	\N	D: 02 01 05 00 00 00 00 00 00 00 00 00	F11	1	J	106	168	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:36:52
7069	Sonnenblumenkernschrot, Import	f	t	11010	11010	2	1	\N	D: 02 06 48 00 00 00 00 00 00 00 00 00	F62	1	J	1	365	0	1	365	\N	\N	\N	\N	\N	65	2011-11-21 10:36:52
7077	Müggelsee	f	t	11010	11010	2	1	\N	D: 06 02 00 02 00 00 01 06 02 00 00 00	G21	3	Q	1	92	0	1	365	\N	\N	\N	\N	\N	63	2011-11-21 10:36:52
7083	Müggelsee	f	t	11010	11010	2	1	\N	D: 06 04 05 02 00 00 00 02 01 01 00 00	G23	1	Q	1	92	0	1	365	\N	\N	\N	\N	\N	65	2011-11-21 10:36:52
7086	Spree; Sophienwerder	f	t	11010	11010	2	1	\N	D: 06 04 05 01 00 00 00 02 01 01 00 00	G13	1	Q	1	92	0	1	365	\N	\N	\N	\N	\N	65	2011-11-21 10:36:52
7089	Müggelsee	f	t	11010	11010	2	1	\N	D: 06 05 00 02 00 00 00 01 00 00 00 00	G22	1	Q	1	92	0	1	365	\N	\N	\N	\N	\N	65	2011-11-21 10:36:52
7095	Beelitzhof; Rohwasser	f	t	11010	11010	2	1	\N	D: 01 59 01 01 05 02 03 03 01 01 00 00	G51	1	J	8	91	0	1	365	\N	\N	\N	\N	\N	63	2011-11-21 10:36:57
7101	Ringbahnstraße	f	t	11010	11010	2	1	\N	D: 06 03 00 01 00 00 01 03 00 00 00 00	G41	1	H	1	183	0	1	365	\N	\N	\N	\N	\N	63	2011-11-21 10:36:57
7107	Barsch; Mueggelsee	f	t	11010	11010	2	1	\N	D: 01 10 32 05 01 02 02 01 02 00 00 00	N61	1	J	85	343	0	1	365	\N	\N	\N	\N	\N	67	2012-07-19 07:31:01
10303	Import	f	t	12020	12020	2	1	\N	D: 01 03 52 00 00 00 00 00 00 00 00 00	N92	1	J	1	30	0	1	365	784	\N	n_im04	\N	\N	67	2011-11-21 10:37:04
10786	Prestewitz	f	t	12020	12020	2	1	\N	D: 02 01 10 00 00 00 00 00 00 00 00 00	F12	1	J	90	180	0	1	365	\N	\N	n_pf07	\N	\N	65	2018-11-06 09:20:41.520944
12671	Fa. Milupa, Werk Fulda	f	t	06010	06010	2	1	\N	D: 01 48 00 00 00 00 00 00 00 00 00 00	N82	1	H	90	183	0	1	365	729	\N	\N	\N	\N	67	2011-11-21 10:36:37
13165	Spinat	f	t	11010	11010	2	1	\N	D: 01 25 01 14 01 02 02 00 02 00 00 00	N21	1	J	141	182	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:36:44
13187	Gtreide, Import	f	t	11010	11010	2	1	\N	D: 01 15 06 01 01 02 01 00 00 00 00 00	N3Z	1	J	1	365	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:36:49
13189	Hirse; Afrika	f	t	11010	11010	2	1	\N	D: 01 15 08 01 01 02 01 00 00 00 00 00	N3Z	1	J	1	365	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:36:44
13191	Äpfel	f	t	11010	11010	2	1	\N	D: 01 29 02 01 01 02 02 00 02 00 00 00	N42	1	J	218	294	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:36:44
13197	Birnen; Argentinien	f	t	11010	11010	2	1	\N	D: 01 29 02 02 01 02 02 00 00 00 00 00	N42	1	J	1	365	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:37:04
13198	Pflaumen; Ungarn	f	t	11010	11010	2	1	\N	D: 01 29 03 05 01 02 02 00 00 00 00 00	N43	1	J	1	365	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:37:04
13211	Schweinefleisch	f	t	11010	11010	2	1	\N	D: 01 06 16 99 01 02 02 20 00 00 00 00	N53	1	Q	1	92	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:37:04
13218	Kalbfleisch; FU	f	t	11010	11010	2	1	\N	D: 01 06 09 99 01 02 02 11 00 00 00 00	N52	1	H	1	183	0	1	365	\N	\N	\N	\N	\N	67	2013-01-17 08:30:06
13219	Pourlade	f	t	11010	11010	2	1	\N	D: 01 06 35 01 01 02 02 20 00 00 00 00	N54	1	J	1	365	0	1	365	\N	\N	\N	\N	\N	67	2011-11-21 10:37:04
18607	\N	f	t	12010	12010	2	1	\N	D: 01 02 07 03 00 00 00 00 00 00 00 00	N94	1	J	60	90	0	1	365	1199	\N	o_mi02	\N	\N	64	2018-11-06 09:22:55.017814
18310	aus hessischer Aufzucht, ersatzweise Wild aus Hessen 	f	t	06010	06010	2	1	\N	D: 01 06 23 00 00 00 00 00 00 00 00 00	N55	1	H	1	183	0	1	365	734	\N	\N	\N	\N	67	2011-11-21 10:36:37
18312	Tiefkühlware; bitte Fanggebiet und Produktionsmethode angeben	f	t	06010	06010	2	1	\N	D: 01 12 01 00 00 00 00 00 00 00 00 00	N63	1	H	1	183	0	1	365	734	\N	\N	\N	\N	67	2011-11-21 10:36:37
18313	Frischfisch, ersatzw. Tiefkühlware; Fanggebiet und Produktionsmethode angeben	f	t	06010	06010	2	1	\N	D: 01 10 65 00 00 00 00 00 00 00 00 00	N61	1	J	1	365	0	1	365	734	\N	\N	\N	\N	67	2011-11-21 10:36:37
18319	falls nicht lieferbar bitte Ersatzprobe and. Frischkäse Fettstufe	f	t	06010	06010	2	1	\N	D: 01 03 26 03 00 00 00 00 00 00 00 00	N91	1	J	1	365	0	1	365	734	\N	\N	\N	\N	67	2011-11-21 10:36:37
25399	\N	f	t	06010	06010	2	1	\N	D: 00 00 00 00 00 00 00 00 00 00 00 00	N	1	T	1	1	0	1	365	977	716	\N	\N	\N	67	2018-01-02 07:41:34
37464	falls nicht lieferbar bitte Ersatzprobe aus gleichem Umweltbereich	f	t	06010	06010	2	1	\N	D: 01 25 02 25 00 00 00 00 02 00 00 00	N24	1	J	150	365	0	1	365	92	\N	\N	\N	\N	67	2011-11-21 10:37:24
\.


--
-- Data for Name: mpg_mmt_mp; Type: TABLE DATA; Schema: lada; Owner: postgres
--

COPY lada.mpg_mmt_mp (id, mpg_id, mmt_id, last_mod) FROM stdin;
16	3558	G1	2000-01-01 00:00:00
25	3582	G1	2000-01-01 00:00:00
37	3626	BS	2000-01-01 00:00:00
56	3676	G1	2000-01-01 00:00:00
58	3710	BS	2000-01-01 00:00:00
65	3730	G1	2000-01-01 00:00:00
71	3746	BS	2000-01-01 00:00:00
73	3749	A1	2000-01-01 00:00:00
74	3749	G1	2000-01-01 00:00:00
87	3775	G1	2000-01-01 00:00:00
1148	10786	G1	2000-01-01 00:00:00
1573	7077	BH	2000-01-01 00:00:00
2595	7053	BS	2000-01-01 00:00:00
2601	7063	G1	2000-01-01 00:00:00
2605	7069	G1	2000-01-01 00:00:00
2610	7077	BS	2000-01-01 00:00:00
2611	7077	G1	2000-01-01 00:00:00
2625	7095	A1	2000-01-01 00:00:00
3327	3513	G1	2000-01-01 00:00:00
3361	3564	G1	2000-01-01 00:00:00
3372	3580	G1	2000-01-01 00:00:00
3392	3626	G1	2000-01-01 00:00:00
3424	3676	BS	2000-01-01 00:00:00
3426	3678	G1	2000-01-01 00:00:00
3428	3710	G1	2000-01-01 00:00:00
3432	3716	BS	2000-01-01 00:00:00
3433	3716	G1	2000-01-01 00:00:00
3440	3730	BH	2000-01-01 00:00:00
3445	3734	G1	2000-01-01 00:00:00
3451	3746	A1	2000-01-01 00:00:00
3452	3746	G1	2000-01-01 00:00:00
3456	3749	BS	2000-01-01 00:00:00
3462	3755	G1	2000-01-01 00:00:00
4887	13191	G1	2000-01-01 00:00:00
4891	13198	G1	2000-01-01 00:00:00
4897	13211	G1	2000-01-01 00:00:00
4899	13219	G1	2000-01-01 00:00:00
5714	18310	G1	2000-01-01 00:00:00
6586	3746	BH	2000-01-01 00:00:00
6594	18607	G1	2000-01-01 00:00:00
7598	3749	BH	2000-01-01 00:00:00
7600	18312	G1	2000-01-01 00:00:00
7601	18313	G1	2000-01-01 00:00:00
7603	18319	G1	2000-01-01 00:00:00
7687	7101	BH	2000-01-01 00:00:00
7789	25399	G1	2000-01-01 00:00:00
9276	10303	G1	2000-01-01 00:00:00
10067	3782	G1	2000-01-01 00:00:00
10071	3786	BH	2000-01-01 00:00:00
10072	3786	G1	2000-01-01 00:00:00
10073	3792	G1	2000-01-01 00:00:00
10077	3796	G1	2000-01-01 00:00:00
10078	3797	G1	2000-01-01 00:00:00
10079	3798	G1	2000-01-01 00:00:00
10081	3801	G1	2000-01-01 00:00:00
10224	7051	G1	2000-01-01 00:00:00
10225	7053	G1	2000-01-01 00:00:00
10226	7057	G1	2000-01-01 00:00:00
10228	7063	BS	2000-01-01 00:00:00
10233	7077	A1	2000-01-01 00:00:00
10238	7083	G1	2000-01-01 00:00:00
10239	7086	G1	2000-01-01 00:00:00
10240	7089	G1	2000-01-01 00:00:00
10242	7095	BS	2000-01-01 00:00:00
10243	7095	G1	2000-01-01 00:00:00
10252	7101	A1	2000-01-01 00:00:00
10253	7101	BS	2000-01-01 00:00:00
10254	7101	G1	2000-01-01 00:00:00
10262	7107	G1	2000-01-01 00:00:00
10471	13165	G1	2000-01-01 00:00:00
10481	13187	G1	2000-01-01 00:00:00
10482	13189	G1	2000-01-01 00:00:00
10486	13197	G1	2000-01-01 00:00:00
10492	13218	G1	2000-01-01 00:00:00
14375	12671	BS	2000-01-01 00:00:00
14376	12671	G1	2000-01-01 00:00:00
14660	7095	BH	2000-01-01 00:00:00
15198	3474	G1	2000-01-01 00:00:00
16266	37464	G1	1970-01-01 00:00:00
\.


--
-- Data for Name: geolocat_mpg; Type: TABLE DATA; Schema: lada; Owner: postgres
--

COPY lada.geolocat_mpg (id, mpg_id, site_id, type_regulation, add_site_text, last_mod, tree_mod) FROM stdin;
11455	10303	97	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11456	18607	69	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11457	18319	23	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11458	13219	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11459	13218	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11460	7053	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11461	3626	138	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11462	13211	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11463	7107	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11464	18312	23	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11465	13187	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11466	13189	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11467	3558	129	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11468	3580	23	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11469	3474	76	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11470	3513	23	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11471	13165	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11472	25399	29	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11473	13198	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11474	13197	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11475	3564	68	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11476	13191	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11477	7051	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11478	7095	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11479	3746	161	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11480	3749	12	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11481	3755	87	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11482	3716	74	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11483	3678	131	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11484	3676	87	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11485	7086	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11486	3734	5	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11487	3730	26	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11488	7089	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11489	7083	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11490	7077	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11491	7101	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11492	3782	12	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11493	3775	12	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11494	3797	87	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11495	3796	87	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11496	3798	29	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11497	3792	29	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11498	3710	127	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11499	12671	162	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11500	7063	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11501	37464	129	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11502	7069	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11503	7057	86	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11504	3801	131	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11505	3786	4	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11506	18313	23	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11507	18310	102	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11508	3582	190	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
11509	10786	42	E	\N	2018-11-06 09:59:51.131609	2018-11-06 09:59:51.131609
\.


--
-- Name: mpg_id_seq; Type: SEQUENCE SET; Schema: lada; Owner: postgres
--

SELECT pg_catalog.setval('lada.mpg_id_seq', 37464, true);


--
-- Name: mpg_mmt_id_seq; Type: SEQUENCE SET; Schema: lada; Owner: postgres
--

SELECT pg_catalog.setval('lada.mpg_mmt_mp_id_seq', 16266, true);


--
-- Name: geolocat_mpg_mp_id_seq; Type: SEQUENCE SET; Schema: lada; Owner: postgres
--

SELECT pg_catalog.setval('lada.geolocat_mpg_id_seq', 11509, true);


--
-- PostgreSQL database dump complete
--

