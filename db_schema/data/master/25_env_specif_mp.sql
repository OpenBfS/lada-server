--
-- PostgreSQL database dump
--

-- Dumped from database version 12.7
-- Dumped by pg_dump version 12.7

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: env_specif_mp; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.env_specif_mp (id, sample_specif_id, env_medium_id) FROM stdin;
1	A01	G11
2	A01	G31
3	A01	S41
4	A02	G11
5	A02	G31
6	A02	S41
7	A03	G31
8	A04	G31
9	A05	G11
10	A05	G21
11	A05	G31
12	A05	S41
13	A05	S42
14	A06	G11
15	A06	G21
16	A06	G31
17	A06	S41
18	A06	S42
19	A07	G11
20	A07	G21
21	A07	G31
22	A07	G32
23	A07	G33
24	A07	S41
25	A07	S42
26	A08	G31
27	A09	G11
28	A09	G21
29	A09	G31
30	A09	L12
31	A09	L54
32	A09	S11
33	A09	S41
34	A09	S42
35	A10	B11
36	A10	B12
37	A10	L11
38	A10	L12
39	A10	L21
40	A10	L22
41	A10	S11
42	A10	S14
43	A10	S21
44	A10	S23
45	A11	B31
46	A11	B32
47	A11	S22
48	A12	B31
49	A12	B32
50	A12	B35
51	A12	B36
52	A12	S22
53	A17	G12
54	A17	G13
55	A17	G22
56	A17	G23
57	A17	G32
58	A17	G33
59	A18	A13
60	A18	A21
61	A18	A22
62	A18	A23
63	A18	A41
64	A18	A42
65	A18	A51
66	A18	A52
67	A18	A53
68	A18	A54
69	A18	A55
70	A18	A56
71	A18	B31
72	A18	B32
73	A18	B33
74	A18	B34
75	A18	B35
76	A18	B36
77	A18	F11
78	A18	F12
79	A18	F21
80	A18	F31
81	A18	F41
82	A18	F51
83	A18	F52
84	A18	F5Z
85	A18	F61
86	A18	F62
87	A18	F63
88	A18	F71
89	A18	G12
90	A18	G13
91	A18	G22
92	A18	G23
93	A18	G32
94	A18	G33
95	A18	I11
96	A18	I12
97	A18	I13
98	A18	I14
99	A18	I15
100	A18	I19
101	A18	I21
102	A18	I22
103	A18	I31
104	A18	I32
105	A18	N21
106	A18	N22
107	A18	N23
108	A18	N24
109	A18	N25
110	A18	N26
111	A18	N27
112	A18	N28
113	A18	N29
114	A18	N2A
115	A18	N2B
116	A18	N2C
117	A18	N2Y
118	A18	N2Z
119	A18	N31
120	A18	N3Z
121	A18	N41
122	A18	N42
123	A18	N43
124	A18	N44
125	A18	N45
126	A18	N46
127	A18	N4Z
128	A18	N51
129	A18	N52
130	A18	N53
131	A18	N54
132	A18	N55
133	A18	N56
134	A18	N5Z
135	A18	N61
136	A18	N62
137	A18	N63
138	A18	N64
139	A18	N82
140	A18	N83
141	A18	N84
142	A18	N86
143	A18	N91
144	A18	N92
145	A18	N93
146	A18	N94
147	A18	N95
148	A18	N96
149	A18	N97
150	A18	N98
151	A18	N99
152	A18	N9A
153	A18	N9B
154	A18	N9C
155	A18	N9D
156	A18	N9E
157	A18	N9F
158	A18	N9G
159	A18	N9H
160	A18	N9I
161	A18	N9J
162	A18	N9K
163	A18	N9L
164	A18	N9M
165	A18	N9N
166	A18	N9O
167	A18	N9P
168	A18	N9Q
169	A18	N9R
170	A18	NZ1
171	A18	NZ2
172	A18	NZZ
173	A18	S22
174	A18	S31
175	A18	S32
176	A18	Z11
177	A18	Z12
178	A18	Z13
179	A18	Z14
180	A18	Z31
181	A18	Z32
182	A18	Z33
183	A18	Z34
184	A18	Z35
185	A19	A13
186	A19	A21
187	A19	A22
188	A19	A23
189	A19	A41
190	A19	A42
191	A19	A51
192	A19	A52
193	A19	A53
194	A19	A54
195	A19	A55
196	A19	A56
197	A19	B31
198	A19	B32
199	A19	B33
200	A19	B34
201	A19	B35
202	A19	B36
203	A19	F11
204	A19	F12
205	A19	F21
206	A19	F31
207	A19	F41
208	A19	F51
209	A19	F52
210	A19	F5Z
211	A19	F61
212	A19	F62
213	A19	F63
214	A19	F71
215	A19	G12
216	A19	G13
217	A19	G22
218	A19	G23
219	A19	G32
220	A19	G33
221	A19	I11
222	A19	I12
223	A19	I13
224	A19	I14
225	A19	I15
226	A19	I19
227	A19	I21
228	A19	I22
229	A19	I31
230	A19	I32
231	A19	N21
232	A19	N22
233	A19	N23
234	A19	N24
235	A19	N25
236	A19	N26
237	A19	N27
238	A19	N28
239	A19	N29
240	A19	N2A
241	A19	N2B
242	A19	N2C
243	A19	N2Y
244	A19	N2Z
245	A19	N31
246	A19	N3Z
247	A19	N41
248	A19	N42
249	A19	N43
250	A19	N44
251	A19	N45
252	A19	N46
253	A19	N4Z
254	A19	N51
255	A19	N52
256	A19	N53
257	A19	N54
258	A19	N55
259	A19	N56
260	A19	N5Z
261	A19	N61
262	A19	N62
263	A19	N63
264	A19	N64
265	A19	N82
266	A19	N83
267	A19	N84
268	A19	N86
269	A19	N91
270	A19	N92
271	A19	N93
272	A19	N94
273	A19	N95
274	A19	N96
275	A19	N97
276	A19	N98
277	A19	N99
278	A19	N9A
279	A19	N9B
280	A19	N9C
281	A19	N9D
282	A19	N9E
283	A19	N9F
284	A19	N9G
285	A19	N9H
286	A19	N9I
287	A19	N9J
288	A19	N9K
289	A19	N9L
290	A19	N9M
291	A19	N9N
292	A19	N9O
293	A19	N9P
294	A19	N9Q
295	A19	N9R
296	A19	NZ1
297	A19	NZ2
298	A19	NZZ
299	A19	S22
300	A19	S31
301	A19	S32
302	A19	Z11
303	A19	Z12
304	A19	Z13
305	A19	Z14
306	A19	Z31
307	A19	Z32
308	A19	Z33
309	A19	Z34
310	A19	Z35
311	A20	G31
312	A20	G32
313	A20	G33
314	A21	B11
315	A21	B12
316	A21	L11
317	A21	L12
318	A21	L21
319	A21	L22
320	A21	L31
321	A21	L41
322	A21	L51
323	A21	L52
324	A21	L54
325	A21	S11
326	A21	S12
327	A21	S13
328	A21	S14
329	A21	S21
330	A21	S23
331	A22	B11
332	A22	B12
333	A22	L11
334	A22	L12
335	A22	L21
336	A22	L22
337	A22	L31
338	A22	L41
339	A22	L51
340	A22	L52
341	A22	L54
342	A22	S11
343	A22	S12
344	A22	S13
345	A22	S14
346	A22	S21
347	A22	S23
348	A23	B11
349	A23	B12
350	A23	L11
351	A23	L12
352	A23	L21
353	A23	L22
354	A23	L31
355	A23	L41
356	A23	L51
357	A23	L52
358	A23	L54
359	A23	S11
360	A23	S12
361	A23	S13
362	A23	S14
363	A23	S21
364	A23	S23
365	A25	B11
366	A25	B12
367	A25	L11
368	A25	L12
369	A25	L21
370	A25	L22
371	A25	L31
372	A25	L41
373	A25	L51
374	A25	L52
375	A25	L54
376	A25	S11
377	A25	S12
378	A25	S13
379	A25	S14
380	A25	S21
381	A25	S23
382	A26	B11
383	A26	B12
384	A26	L11
385	A26	L12
386	A26	L21
387	A26	L22
388	A26	L31
389	A26	L41
390	A26	L51
391	A26	L52
392	A26	L54
393	A26	S11
394	A26	S12
395	A26	S13
396	A26	S14
397	A26	S21
398	A26	S23
399	A27	G11
400	A27	G12
401	A27	G13
402	A27	S41
403	A29	G11
404	A29	G12
405	A29	G13
406	A29	G21
407	A29	G22
408	A29	G23
409	A29	N61
410	A29	S41
411	A29	S42
412	A30	B11
413	A30	B12
414	A30	S21
415	A30	S23
416	A32	B11
417	A32	B12
418	A32	L11
419	A32	L12
420	A32	L21
421	A32	L22
422	A32	L31
423	A32	L41
424	A32	L51
425	A32	L52
426	A32	L54
427	A32	S11
428	A32	S12
429	A32	S13
430	A32	S14
431	A32	S21
432	A32	S23
433	A33	B11
434	A33	B12
435	A33	L11
436	A33	L12
437	A33	L21
438	A33	L22
439	A33	L31
440	A33	L41
441	A33	L51
442	A33	L52
443	A33	L54
444	A33	S11
445	A33	S12
446	A33	S13
447	A33	S14
448	A33	S21
449	A33	S23
450	A34	B31
451	A34	B32
452	A34	B33
453	A34	G13
454	A34	G23
455	A34	G33
456	A34	S22
457	A35	G51
458	A35	G52
459	A35	N71
460	A35	N72
461	A36	G51
462	A36	G52
463	A36	N71
464	A36	N72
465	A38	G11
466	A38	G21
467	A38	G31
468	A38	G51
469	A38	G52
470	A38	N71
471	A38	N72
472	A38	S41
473	A38	S42
474	A39	G11
475	A39	G12
476	A39	G21
477	A39	G22
478	A39	G41
479	A39	G51
480	A39	G52
481	A39	N71
482	A39	N72
483	A39	S41
484	A39	S42
485	A40	G13
486	A40	G23
487	A40	G51
488	A40	G52
489	A40	N71
490	A40	N72
491	A41	G13
492	A41	G23
493	A41	G51
494	A41	G52
495	A41	N71
496	A41	N72
497	A45	A11
498	A45	A12
499	A45	A24
500	A45	A31
501	A46	A11
502	A46	A12
503	A46	A24
504	A46	A31
505	A47	A21
506	A47	A22
507	A47	A23
508	A47	A24
509	A48	A21
510	A48	A22
511	A48	A23
512	A48	A24
513	A49	A21
514	A49	A22
515	A49	A23
516	A49	A24
517	A50	A21
518	A50	A22
519	A50	A23
520	A50	A24
521	A51	A31
522	A52	A31
523	A53	A41
524	A54	A51
525	A55	A51
526	A56	A51
527	A60	B11
528	A60	B12
529	A60	S21
530	A60	S23
531	A61	B11
532	A61	B12
533	A61	S21
534	A61	S23
535	A62	B11
536	A62	B12
537	A62	B21
538	A62	S21
539	A62	S23
540	A72	A12
541	A75	B33
542	A75	S22
543	A76	B33
544	A76	S22
545	A77	B33
546	A77	S22
547	A78	B33
548	A78	S22
549	A79	B33
550	A79	S22
551	A80	F11
552	A80	F12
553	A80	F21
554	A80	F31
555	A80	F41
556	A80	F51
557	A80	F52
558	A80	F5Z
559	A80	F61
560	A80	F62
561	A80	F63
562	A80	F71
563	A80	N21
564	A80	N22
565	A80	N23
566	A80	N24
567	A80	N25
568	A80	N26
569	A80	N27
570	A80	N28
571	A80	N29
572	A80	N2A
573	A80	N2B
574	A80	N2C
575	A80	N2Y
576	A80	N2Z
577	A80	N31
578	A80	N3Z
579	A80	N41
580	A80	N42
581	A80	N43
582	A80	N44
583	A80	N45
584	A80	N46
585	A80	N4Z
586	A80	N51
587	A80	N52
588	A80	N53
589	A80	N54
590	A80	N55
591	A80	N56
592	A80	N5Z
593	A80	N61
594	A80	N62
595	A80	N63
596	A80	N64
597	A80	N82
598	A80	N83
599	A80	N84
600	A80	N86
601	A80	N91
602	A80	N92
603	A80	N93
604	A80	N94
605	A80	N95
606	A80	N96
607	A80	N97
608	A80	N98
609	A80	N99
610	A80	N9A
611	A80	N9B
612	A80	N9C
613	A80	N9D
614	A80	N9E
615	A80	N9F
616	A80	N9G
617	A80	N9H
618	A80	N9I
619	A80	N9J
620	A80	N9K
621	A80	N9L
622	A80	N9M
623	A80	N9N
624	A80	N9O
625	A80	N9P
626	A80	N9Q
627	A80	N9R
628	A80	NZ1
629	A80	NZ2
630	A80	NZZ
631	A81	N11
632	A81	N12
633	A81	N13
634	A81	N14
635	A81	N15
636	A81	N71
637	A81	N72
638	A81	N73
639	A81	N74
640	A81	N75
641	A81	N85
642	A81	N87
643	E03	L12
644	E03	L54
645	E03	S11
646	E54	L31
647	E54	L41
648	E54	S12
649	E54	S13
650	E74	L12
651	E74	S11
652	E78	L12
653	E78	L54
654	E78	M
655	E78	S11
656	E81	L12
657	E81	L31
658	E81	S11
659	E81	S12
660	E82	L12
661	E82	L54
662	E82	S11
663	E83	L12
664	E83	S11
665	E84	L12
666	E84	S11
667	A39	G31
668	A11	G33
669	A19	A14
\.


--
-- Name: env_specif_mp_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.env_specif_mp_id_seq', 667, true);


--
-- PostgreSQL database dump complete
--
