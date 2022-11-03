\set ON_ERROR_STOP on

--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = master, pg_catalog;

--
-- Data for Name: state; Type: TABLE DATA; Schema: stammdaten; Owner: postgres
--

COPY state (id, ctry, ctry_orig_id, iso_3166, int_veh_reg_code, is_eu_country, x_coord_ext, y_coord_ext, spat_ref_sys_id) FROM stdin;
242	Cabo Verde	242	CV	CV	0	-23,516667	14,916667	4
640	Serbien und Montenegro bis 2006	132	CS	SCG	0	20,412556	44,799683	4
395	Britisch abhängige Gebiete in Amerika bis 2016	395	\N	\N	0	0	0	4
346	Haiti	346	HT	RH	0	-72,343109	18,526617	4
153	Portugal	153	PT	P	1	-9,13	38,73	4
399	übriges Amerika (Grönland, Guadeloupe, Martinique)	399	\N	\N	0	0	0	4
137	Italien	137	IT	I	1	12,52	41,880001	4
145	Malta	145	MT	M	1	14,514722	35,899722	4
152	Polen	152	PL	PL	1	21,011879	52,244946	4
138	Jugoslawien bis 2003	138	YU	YU	0	20,412556	44,799683	4
680	Kosovo	150	XK	RKS	0	20,835278	42,551944	4
681	Montenegro	140	ME	MNE	0	19,216667	42,766667	4
682	Serbien	170	RS	SRB	0	20,933333	43,95	4
195	Britisch abhängige Gebiete in Europa bis 2016	195	\N	\N	0	0	0	4
199	übriges Europa	199	\N	\N	0	0	0	4
994	von/nach See	994	\N	\N	0	0	0	4
231	Cote d'Ivoire	231	CI	CI	0	-5,272778	6,806667	4
365	Uruguay	365	UY	ROU	0	-56,169998	-34,919998	4
355	Jamaika	355	JM	JA	0	-76,797302	18,015713	4
236	Gabun	236	GA	G	0	9,490457	-0,504145	4
238	Ghana	238	GH	GH	0	-0,200924	5,558563	4
243	Kenia	243	KE	EAK	0	36,830002	-1,17	4
247	Liberia	247	LR	LB	0	-10,77	6,517439	4
369	St. Vincent und die Grenadinen	369	VC	WV	0	-61,226111	13,160833	4
125	Bulgarien	125	BG	BG	1	23,331871	42,707264	4
128	Finnland	128	FI	FIN	1	24,9767	60,196423	4
136	Island	136	IS	IS	0	-21,336821	64,313263	4
143	Luxemburg	143	LU	L	1	6,273256	49,740406	4
147	Monaco	147	MC	MC	0	7,416667	43,733333	4
158	Schweiz	158	CH	CH	0	7,445736	46,948208	4
161	Spanien	161	ES	E	1	-3,690969	40,442219	4
167	Vatikanstadt	167	VA	V	0	12,52	41,880001	4
221	Algerien	221	DZ	DZ	0	2,993693	36,596493	4
229	Benin	229	BJ	BJ	0	2,632503	6,601096	4
122	Bosnien und Herzegowina	122	BA	BIH	0	18,43	43,869999	4
224	Eritrea	224	ER	ER	0	38,970001	15,33	4
422	Armenien	422	AM	AM	0	44,532669	40,208023	4
450	Kirgisistan	450	KG	KS	0	74,566667	42,866667	4
537	Palau	537	PW	PAL	0	134,634167	7,493333	4
545	Mikronesien	545	FM	FSM	0	158,158333	6,917778	4
461	Pakistan	461	PK	PK	0	73,060547	33,718151	4
465	Taiwan	465	TW	RC	0	121,506729	25,035091	4
467	Korea, Republik	467	KR	ROK	0	126,935249	37,542351	4
472	Saudi-Arabien	472	SA	KSA	0	46,77	24,65	4
475	Syrien	475	SY	SYR	0	36,313454	33,519302	4
479	China	479	CN	CHN	0	116,388039	39,906193	4
248	Libyen	248	LY	LAR	0	13,211823	32,751617	4
367	Venezuela	367	VE	YV	0	-66,898285	10,496049	4
251	Mali	251	ML	RMM	0	-7,986482	12,65295	4
253	Mauritius	253	MU	MS	0	57,5	-20,166667	4
255	Niger	255	NE	RN	0	2,08345	13,604544	4
256	Malawi	256	MW	MW	0	33,82	-13,92	4
258	Burkina Faso	258	BF	BF	0	-1,67	12,48	4
261	Guinea	261	GN	RG	0	-12,8	9,52	4
262	Kamerun	262	CM	CAM	0	11,513641	3,865123	4
265	Ruanda	265	RW	RWA	0	29,991486	-2,117935	4
269	Senegal	269	SN	SN	0	-16,848095	14,63	4
272	Sierra Leone	272	SL	WAL	0	-12,910276	8,382771	4
274	Äquatorialguinea	274	GQ	GQ	0	8,82	3,644685	4
281	Swasiland	281	SZ	SD	0	31,191298	-26,303381	4
283	Togo	283	TG	TG	0	1,35	6,28	4
284	Tschad	284	TD	TD	0	15,240824	12,104139	4
286	Uganda	286	UG	EAU	0	32,580002	0,32	4
289	Zentralafrikanische Republik	289	CF	RCA	0	18,562342	4,365856	4
320	Antigua und Barbuda	320	AG	AG	0	-61,844722	17,121111	4
323	Argentinien	323	AR	RA	0	-58,409592	-34,665401	4
326	Bolivien	326	BO	BOL	0	-68,146248	-16,499006	4
328	Guyana	328	GY	GUY	0	-58,169998	6,77	4
332	Chile	332	CL	RCH	0	-70,647514	-33,475025	4
334	Costa Rica	334	CR	CR	0	-84,078613	9,930476	4
335	Dominikanische Republik	335	DO	DOM	0	-69,910492	18,499729	4
340	Grenada	340	GD	WG	0	-61,741667	12,044444	4
345	Guatemala	345	GT	GCA	0	-90,524902	14,618008	4
347	Honduras	347	HN	HN	0	-87,203094	14,099051	4
349	Kolumbien	349	CO	CO	0	-74,080513	4,63022	4
361	Peru	361	PE	PE	0	-76,823555	-12,067996	4
364	Suriname	364	SR	SME	0	-55,23	5,93	4
351	Kuba	351	CU	C	0	-82,41645	23,048952	4
543	Samoa	543	WS	WS	0	-171,751811	-13,831386	4
230	Dschibuti	230	DJ	DJI	0	43,099998	11,5	4
237	Gambia	237	GM	WAG	0	-16,494616	13,445272	4
239	Mauretanien	239	MR	RIM	0	-15,782861	18,030001	4
245	Kongo, Republik	245	CG	RCB	0	15,285149	-4,285187	4
0	Deutschland	0	DE	D	1	13,327573	52,516273	4
124	Belgien	124	BE	B	1	4,367612	50,837048	4
126	Dänemark	126	DK	DK	1	12,55	55,720001	4
129	Frankreich	129	FR	F	1	2,432833	48,881554	4
135	Irland	135	IE	IRL	1	-6,257347	53,34156	4
139	Lettland	139	LV	LV	1	24,049999	56,880001	4
142	Litauen	142	LT	LT	1	25,275967	54,688568	4
148	Niederlande	148	NL	NL	1	4,894833	52,373043	4
151	Österreich	151	AT	A	1	16,320986	48,202118	4
154	Rumänien	154	RO	RO	1	26,122976	44,430485	4
157	Schweden	157	SE	S	1	18,084269	59,244633	4
162	Tschechische Republik	164	CZ	CZ	1	14,45652	50,105896	4
165	Ungarn	165	HU	H	1	19,09425	47,514626	4
181	Zypern	181	CY	CY	1	33,385162	35,16507	4
225	Äthiopien	225	ET	ETH	0	38,700001	9,03	4
227	Botsuana	227	BW	RB	0	25,794802	-24,661442	4
130	Kroatien	130	HR	HR	0	15,964386	45,807076	4
131	Slowenien	131	SI	SLO	1	14,639612	46,068302	4
425	Aserbaidschan	425	AZ	AZ	0	49,816238	40,324299	4
470	Tadschikistan	470	TJ	TJ	0	68,900002	38,630001	4
477	Usbekistan	477	UZ	UZ	0	69,349869	41,247932	4
354	Nicaragua	354	NI	NIC	0	-86,273033	12,151473	4
233	Simbabwe	233	ZW	ZW	0	31,02	-17,83	4
244	Komoren	244	KM	COM	0	43,253611	-11,703611	4
121	Albanien	121	AL	AL	0	19,831804	41,331654	4
127	Estland	127	EE	EST	1	24,752056	59,277573	4
159	Russische Föderation	160	RU	RUS	0	37,700001	55,75	4
160	Ukraine	166	UA	UA	0	30,502111	50,448158	4
146	Moldau, Republik	146	MD	MD	0	28,83	47	4
444	Kasachstan	444	KZ	KZ	0	76,912628	43,255062	4
544	Marshallinseln	544	MH	MH	0	171,366667	7,116667	4
620	Weißrussland	169	BY	BY	0	27,575567	53,899937	4
523	Australien	523	AU	AUS	0	149,041626	-35,349926	4
526	Fidschi	526	FJ	FJI	0	178,433333	-18,133333	4
532	Vanuatu	532	VU	VAN	0	168,3	-17,75	4
538	Papua-Neuguinea	538	PG	PNG	0	147,41452	-9,55	4
357	Panama	357	PA	PA	0	-79,400002	8,95	4
541	Tonga	541	TO	TON	0	-175,208331	-21,134644	4
371	Trinidad und Tobago	371	TT	TT	0	-61,490063	10,639734	4
423	Afghanistan	423	AF	AFG	0	69,136757	34,530907	4
427	Myanmar	427	MM	MYA	0	96,124893	16,872223	4
432	Vietnam	432	VN	VN	0	105,819908	21,031948	4
436	Indien	436	IN	IND	0	77,216751	28,568726	4
439	Iran, Islamische Republik	439	IR	IR	0	51,447651	35,774475	4
660	Timor-Leste	483	TL	TL	0	125,75	-8,966667	4
446	Kambodscha	446	KH	K	0	104,913193	11,564736	4
448	Kuwait	448	KW	KWT	0	48,002777	29,19499	4
454	Malediven	454	MV	MV	0	73,509444	4,174444	4
457	Mongolei	457	MN	MGL	0	106,912354	47,928596	4
462	Philippinen	462	PH	RP	0	121,173409	14,55	4
476	Thailand	476	TH	T	0	100,552666	13,745571	4
368	Vereinigte Staaten	368	US	USA	0	-76,953835	38,890911	4
257	Sambia	257	ZM	Z	0	28,17	-15,43	4
263	Südafrika	263	ZA	ZA	0	28,218372	-25,731346	4
273	Somalia	273	SO	SO	0	45,344143	2,041178	4
285	Tunesien	285	TN	TN	0	10,16596	36,818813	4
291	Burundi	291	BI	RU	0	29,533587	-3,269084	4
324	Bahamas	324	BS	BS	0	-77,333333	25,066667	4
330	Belize	330	BZ	BZ	0	-88,800003	17,120001	4
337	El Salvador	337	SV	ES	0	-89,200233	13,701412	4
348	Kanada	348	CA	CDN	0	-75,650749	45,374218	4
353	Mexiko	353	MX	MEX	0	-99,127571	19,427046	4
232	Nigeria	232	NG	NGR	0	3,3	6,45	4
246	Kongo, Demokratische Republik	246	CD	CGO	0	15,469294	-4,388675	4
123	Andorra	123	AD	AND	0	1,5225	42,5075	4
134	Griechenland	134	GR	GR	1	23,654863	38,121601	4
141	Liechtenstein	141	LI	FL	0	9,521804	47,139651	4
149	Norwegen	149	NO	N	0	10,72	59,93	4
156	San Marino	156	SM	RSM	0	12,457123	43,938451	4
163	Türkei	163	TR	TR	0	32,853271	39,929329	4
168	Vereinigtes Königreich	168	GB	GB	1	-0,177998	51,487911	4
226	Lesotho	226	LS	LS	0	27,890388	-29,25671	4
144	Mazedonien	144	MK	MK	0	21,530001	42	4
430	Georgien	430	GE	GE	0	44,783127	41,721809	4
471	Turkmenistan	471	TM	TM	0	58,390133	37,95042	4
169	Slowakei	155	SK	SK	1	17,269806	48,274509	4
524	Salomonen	524	SB	SOL	0	159,95	-9,433333	4
530	Kiribati	530	KI	KIR	0	172,971111	1,4075	4
531	Nauru	531	NR	NAU	0	166,919608	-0,543425	4
536	Neuseeland	536	NZ	NZ	0	175,144943	-41,210396	4
540	Tuvalu	540	TV	TUV	0	179,123056	-8,504167	4
249	Madagaskar	249	MG	RM	0	47,5	-18,870001	4
359	Paraguay	359	PY	PY	0	-57,669998	-25,219999	4
370	St. Kitts und Nevis	370	KN	KAN	0	-62,734167	17,298333	4
421	Jemen	421	YE	YEM	0	44,209503	15,361444	4
499	Übriges Asien	499	\N	\N	0	0	0	4
996	unbekanntes Ausland	996	\N	\N	0	0	0	4
998	ungeklärt	998	\N	\N	0	0	0	4
999	ohne Angabe	999	\N	\N	0	0	0	4
424	Bahrain	424	BH	BRN	0	50,583056	26,236111	4
426	Bhutan	426	BT	BHT	0	89,667328	27,442606	4
429	Brunei Darussalam	429	BN	BRU	0	114,967003	4,933	4
431	Sri Lanka	431	LK	CL	0	80,088333	7,02	4
434	Korea, Demokratische Volksrepublik	434	KP	KP	0	125,757515	39,028515	4
437	Indonesien	437	ID	RI	0	106,762466	-6,293904	4
438	Irak	438	IQ	IRQ	0	44,397835	33,334038	4
441	Israel	441	IL	IL	0	34,856834	31,917198	4
442	Japan	442	JP	J	0	139,809189	35,683056	4
445	Jordanien	445	JO	JOR	0	35,932907	31,949383	4
447	Katar	447	QA	Q	0	51,497234	25,203642	4
449	Laos	449	LA	LAO	0	102,680237	18,001732	4
451	Libanon	451	LB	RL	0	35,657944	33,779999	4
456	Oman	456	OM	OM	0	58,62748	23,51664	4
282	Tansania	282	TZ	EAT	0	39,253349	-6,817359	4
223	Angola	223	AO	ANG	0	13,461779	-9	4
287	Ägypten	287	EG	ET	0	31,250797	30,077911	4
322	Barbados	322	BB	BDS	0	-59,583333	13,2	4
327	Brasilien	327	BR	BR	0	-47,897747	-15,792109	4
333	Dominica	333	DM	WD	0	-61,391111	15,298333	4
336	Ecuador	336	EC	EC	0	-78,524284	-0,229498	4
267	Namibia	267	NA	NAM	0	17,1	-22,57	4
458	Nepal	458	NP	NEP	0	85,31295	27,712017	4
460	Bangladesch	460	BD	BD	0	90,407143	23,709919	4
469	Vereinigte Arabische Emirate	469	AE	UAE	0	54,61927	24,236008	4
474	Singapur	474	SG	SGP	0	104,177116	1,229794	4
482	Malaysia	482	MY	MAL	0	101,707672	3,15021	4
366	St. Lucia	366	LC	WL	0	-60,977222	14,025278	4
252	Marokko	252	MA	MA	0	-6,748041	33,920197	4
254	Mosambik	254	MZ	MOC	0	32,573692	-25,962154	4
259	Guinea-Bissau	259	GW	GUB	0	-15,65	11,91099	4
268	Sao Tome und Principe	268	ST	STP	0	6,731389	0,336111	4
271	Seychellen	271	SC	SY	0	55,461944	-4,630833	4
684	Sudan	277	SD	SUD	0	\N	\N	\N
685	Südsudan	278	SS	SSD	0	\N	\N	\N
299	übriges Afrika (Mayotte, Reunion)	299	\N	\N	0	0	0	4
599	übriges Ozeanien	599	\N	\N	0	0	0	4
683	Britische Überseegebiete	185	\N	\N	0	\N	\N	\N
276	Sudan bis 2011	276	\N	\N	0	32,529999	15,55	4
\.


--
-- Name: state_id_seq; Type: SEQUENCE SET; Schema: stammdaten; Owner: postgres
--

SELECT pg_catalog.setval('state_id_seq', 999, true);


--
-- PostgreSQL database dump complete
--

