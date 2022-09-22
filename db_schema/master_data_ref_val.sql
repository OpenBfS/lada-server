--
-- PostgreSQL database dump
--

-- Dumped from database version 12.9
-- Dumped by pg_dump version 12.9

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
-- Data for Name: ref_val_measure; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.ref_val_measure (id, measure, descr) FROM stdin;
1	Aufenthalt in Gebäuden (äußere Exposition, 7d)	\N
2	Aufenthalt in Gebäuden (äußere Exposition und Inhalation, 7d)	\N
3	Einnahme von Iodtabletten (Inhalation, 7d)	\N
4	Evakuierung (äußere Exposition, 7d)	\N
5	Evakuierung (äußere Exposition und Inhalation, 7d)	\N
6	langfristige Umsiedlung (äußere Exposition, 1 Jahr)	\N
7	temporäre Umsiedlung (äußere Exposition, 1 Monat)	\N
8	temporäre Umsiedlung (äußere Exposition und Inhalation, 7d)	\N
9	Vermarktungssperre	\N
10	Verwendungsverbot für Futtermittel	\N
11	Verwendungsverbot für Trinkwasser	\N
\.


--
-- Data for Name: ref_val; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.ref_val (id, env_medium_id, ref_val_meas_id, measd_gr_id, specif, ref_val) FROM stdin;
235	N11	9	30	\N	125
236	N12	9	30	\N	125
237	N13	9	30	\N	125
238	N21	9	30	\N	750
239	N22	9	30	\N	750
240	N23	9	30	\N	750
241	N24	9	30	\N	750
242	N25	9	30	\N	750
243	N2A	9	30	\N	750
244	N42	9	30	\N	750
245	N43	9	30	\N	750
246	N44	9	30	\N	750
247	N51	9	30	\N	750
248	N52	9	30	\N	750
249	N53	9	30	\N	750
250	N54	9	30	\N	750
251	N61	9	30	\N	750
252	N62	9	30	\N	750
253	N71	9	30	\N	125
254	N72	9	30	\N	125
255	N73	9	30	\N	125
256	N82	9	30	\N	75
257	N85	9	30	\N	125
258	NZZ	9	30	\N	7500
259	N3Z	9	31	\N	200
260	N31	9	31	\N	2000
261	N11	9	31	\N	500
262	N12	9	31	\N	500
263	N13	9	31	\N	500
264	N21	9	31	\N	2000
265	N22	9	31	\N	2000
266	N23	9	31	\N	2000
267	N24	9	31	\N	2000
268	N25	9	31	\N	2000
269	N2A	9	31	\N	2000
270	N42	9	31	\N	2000
271	N43	9	31	\N	2000
272	N44	9	31	\N	2000
273	N51	9	31	\N	2000
274	N52	9	31	\N	2000
275	N53	9	31	\N	2000
276	N54	9	31	\N	2000
277	N61	9	31	\N	2000
278	N62	9	31	\N	2000
279	N71	9	31	\N	500
280	N72	9	31	\N	500
281	N73	9	31	\N	500
282	N82	9	31	\N	150
283	N85	9	31	\N	500
284	NZZ	9	31	\N	20000
285	N3Z	9	32	\N	80
286	N31	9	32	\N	80
287	N11	9	32	\N	20
288	N12	9	32	\N	20
289	N13	9	32	\N	20
290	N21	9	32	\N	80
291	N22	9	32	\N	80
292	N23	9	32	\N	80
293	N24	9	32	\N	80
294	N25	9	32	\N	80
295	N2A	9	32	\N	80
296	N42	9	32	\N	80
297	N43	9	32	\N	80
298	N44	9	32	\N	80
299	N51	9	32	\N	80
300	N52	9	32	\N	80
301	N53	9	32	\N	80
302	N54	9	32	\N	80
303	N61	9	32	\N	80
304	N62	9	32	\N	80
305	N71	9	32	\N	20
306	N72	9	32	\N	20
307	N73	9	32	\N	20
308	N82	9	32	\N	1
309	N85	9	32	\N	20
310	NZZ	9	32	\N	800
311	N3Z	9	33	\N	1250
312	N31	9	33	\N	1250
313	F11	10	33	Futter für Milchvieh	5000
314	F12	10	33	Futter für Milchvieh	5000
316	F31	10	33	Mastfutter (Schwein/Rind)	1250
317	F41	10	33	Mastfutter (Schwein)	1250
318	F51	10	33	Futter für Milchvieh	5000
319	F52	10	33	Mastfutter (Schwein/Rind)	1250
320	F5Z	10	33	Mastfutter (Schwein/Rind)	1250
321	F61	10	33	Mastfutter (Schwein/Rind)	1250
322	F62	10	33	Mastfutter (Schwein/Rind)	1250
323	F63	10	33	Mastfutter (Schwein/Rind)	1250
324	F71	10	33	Mastfutter (Rind)	1250
325	N11	9	33	\N	1000
326	N12	9	33	\N	1000
327	N13	9	33	\N	1000
328	N21	9	33	\N	1250
329	N22	9	33	\N	1250
330	N23	9	33	\N	1250
331	N24	9	33	\N	1250
332	N25	9	33	\N	1250
333	N2A	9	33	\N	1250
334	N42	9	33	\N	1250
335	N43	9	33	\N	1250
336	N44	9	33	\N	1250
337	N51	9	33	\N	1250
338	N52	9	33	\N	1250
339	N53	9	33	\N	1250
340	N54	9	33	\N	1250
341	N61	9	33	\N	1250
342	N62	9	33	\N	1250
343	N71	9	33	\N	1000
344	N72	9	33	\N	1000
345	N73	9	33	\N	1000
346	N82	9	33	\N	400
347	N85	9	33	\N	1000
348	NZZ	9	33	\N	12500
349	N3Z	9	34	\N	750
350	N31	9	34	\N	750
351	G11	11	30	\N	125
352	G21	11	30	\N	125
353	G52	11	30	\N	125
354	G11	11	31	\N	500
355	G21	11	31	\N	500
356	G52	11	31	\N	500
357	G11	11	32	\N	20
358	G21	11	32	\N	20
359	G52	11	32	\N	20
360	G11	11	33	\N	1000
361	G21	11	33	\N	1000
362	G52	11	33	\N	1000
315	F21	10	33	Mastfutter (Rind)	5000
363	I13	10	33	\N	5000
364	I19	10	33	\N	5000
365	S31	10	33	\N	5000
366	S32	10	33	\N	5000
\.


--
-- Name: name_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.ref_val_id_seq', 479, true);


--
-- Name: name_massnahme_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.ref_val_measure_id_seq', 21, true);


--
-- PostgreSQL database dump complete
--

