--
-- PostgreSQL database dump
--

-- Dumped from database version 10.4
-- Dumped by pg_dump version 10.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

TRUNCATE TABLE stamm.base_query, stamm.filter, stamm.result_type CASCADE;
TRUNCATE TABLE stamm.lada_user CASCADE;

--
-- Data for Name: lada_user; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.lada_user (id, name) FROM stdin;
0	Default
1	i_admin
\.

--
-- Data for Name: base_query; Type: TABLE DATA; Schema: stamm; Owner: postgres
--
-- Help in debugging failing check constraint:
SET client_min_messages = notice;

COPY stamm.base_query (id, sql) FROM stdin;
1	SELECT probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  datenbasis.datenbasis AS dBasis,\n  stamm.mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.ort_id AS ortId,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  probe.ext_id AS externeProbeId,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.test,\n  betriebsart.name AS messRegime,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  probe.media_desk AS deskriptoren,\n  probe.media AS medium,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n  ortszuordnung.ortszusatztext,\n  stamm.ort.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  array_to_string(tags.tags, ',', '') AS tags,\n  coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn) AS mitteSammelzeitraum,\n  staat_uo.staat AS uStaat,\n  ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit\n  FROM land.probe\nLEFT JOIN (\n  SELECT probe.id,\n    array_agg(tag.tag) AS tags\n  FROM land.probe\n  JOIN land.tagzuordnung ON probe.id = tagzuordnung.probe_id\n  JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n  GROUP BY probe.id\n  ) tags ON probe.id = tags.id\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz ON (ort.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)
11	SELECT messung.id,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE\n   WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  CASE\n WHEN h3.messwert_nwg = '<'\n THEN h3.messwert_nwg || to_char(h3.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(h3.messwert, '0.99eeee')\n END AS h3,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n  CASE\n WHEN co60.messwert_nwg = '<'\n THEN co60.messwert_nwg || to_char(co60.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(co60.messwert, '0.99eeee')\n END AS co60,\n  CASE\n WHEN sr89.messwert_nwg = '<'\n THEN sr89.messwert_nwg || to_char(sr89.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr89.messwert, '0.99eeee')\n END AS sr89,\n CASE\n WHEN sr90.messwert_nwg = '<'\n THEN sr90.messwert_nwg || to_char(sr90.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr90.messwert, '0.99eeee')\n END AS sr90,  \n CASE\n WHEN ru103.messwert_nwg = '<'\n THEN ru103.messwert_nwg || to_char(ru103.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ru103.messwert, '0.99eeee')\n END AS ru103,\n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,  \n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN ce144.messwert_nwg = '<'\n THEN ce144.messwert_nwg || to_char(ce144.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ce144.messwert, '0.99eeee')\n END AS ce144,\n CASE\n WHEN u234.messwert_nwg = '<'\n THEN u234.messwert_nwg || to_char(u234.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u234.messwert, '0.99eeee')\n END AS u234,  \n CASE\n WHEN u235.messwert_nwg = '<'\n THEN u235.messwert_nwg || to_char(u235.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u235.messwert, '0.99eeee')\n END AS u235,\n CASE\n WHEN u238.messwert_nwg = '<'\n THEN u238.messwert_nwg || to_char(u238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u238.messwert, '0.99eeee')\n END AS u238,\n CASE\n WHEN pu238.messwert_nwg = '<'\n THEN pu238.messwert_nwg || to_char(pu238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu238.messwert, '0.99eeee')\n END AS pu238,  \n CASE\n WHEN pu239.messwert_nwg = '<'\n THEN pu239.messwert_nwg || to_char(pu239.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu239.messwert, '0.99eeee')\n END AS pu239,\n CASE\n WHEN pu23940.messwert_nwg = '<'\n THEN pu23940.messwert_nwg || to_char(pu23940.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu23940.messwert, '0.99eeee')\n END AS pu23940,\n CASE\n WHEN te132.messwert_nwg = '<'\n THEN te132.messwert_nwg || to_char(te132.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(te132.messwert, '0.99eeee')\n END AS te132,  \n CASE\n WHEN pb212.messwert_nwg = '<'\n THEN pb212.messwert_nwg || to_char(pb212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb212.messwert, '0.99eeee')\n END AS pb212,\n CASE\n WHEN pb214.messwert_nwg = '<'\n THEN pb214.messwert_nwg || to_char(pb214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb214.messwert, '0.99eeee')\n END AS pb214,\n CASE\n WHEN bi212.messwert_nwg = '<'\n THEN bi212.messwert_nwg || to_char(bi212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi212.messwert, '0.99eeee')\n END AS bi212,  \n CASE\n WHEN bi214.messwert_nwg = '<'\n THEN bi214.messwert_nwg || to_char(bi214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi214.messwert, '0.99eeee')\n END AS bi214,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE\n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n stamm.ort.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  messung.fertig AS fertig,\n  (rueckfrage_messung.messungs_id IS NOT NULL) AS hatRueckfrage,\n     staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags\nFROM land.probe\nLEFT JOIN stamm.mess_stelle\n  ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nINNER JOIN land.messung\n  ON probe.id = messung.probe_id\nINNER JOIN land.status_protokoll\n  ON messung.STATUS = status_protokoll.id\nLEFT JOIN stamm.status_kombi\n  ON status_protokoll.status_kombi = stamm.status_kombi.id\nLEFT JOIN stamm.status_wert\n  ON stamm.status_wert.id = stamm.status_kombi.wert_id\nLEFT JOIN stamm.status_stufe\n  ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN land.rueckfrage_messung\n  ON rueckfrage_messung.messungs_id = messung.id\nLEFT JOIN stamm.datenbasis\n  ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart\n  ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung\n  ON (\n      probe.id = ortszuordnung.probe_id\n      AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n      )\nLEFT JOIN stamm.ort\n  ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit\n  ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz\n  ON (ort.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe\n  ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe\n  ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.messprogramm_kategorie\n  ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.betriebsart\n  ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN public.lada_messwert h3\n  ON (h3.messungs_id = messung.id AND h3.messgroesse_id = 1)\nLEFT JOIN public.lada_messwert k40\n  ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\nLEFT JOIN public.lada_messwert co60\n  ON (co60.messungs_id = messung.id AND co60.messgroesse_id = 68)\nLEFT JOIN public.lada_messwert sr89\n  ON (sr89.messungs_id = messung.id AND sr89.messgroesse_id = 164)\nLEFT JOIN public.lada_messwert sr90\n  ON (sr90.messungs_id = messung.id AND sr90.messgroesse_id = 165)\nLEFT JOIN public.lada_messwert ru103\n  ON (ru103.messungs_id = messung.id AND ru103.messgroesse_id = 220)\nLEFT JOIN public.lada_messwert i131\n  ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\nLEFT JOIN public.lada_messwert cs134\n  ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\nLEFT JOIN public.lada_messwert cs137\n  ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\nLEFT JOIN public.lada_messwert ce144\n  ON (ce144.messungs_id = messung.id AND ce144.messgroesse_id = 404)\nLEFT JOIN public.lada_messwert u234\n  ON (u234.messungs_id = messung.id AND u234.messgroesse_id = 746)\nLEFT JOIN public.lada_messwert u235\n  ON (u235.messungs_id = messung.id AND u235.messgroesse_id = 747)\nLEFT JOIN public.lada_messwert u238\n  ON (u238.messungs_id = messung.id AND u238.messgroesse_id = 750)\nLEFT JOIN public.lada_messwert pu238\n  ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\nLEFT JOIN public.lada_messwert pu239\n  ON (pu239.messungs_id = messung.id AND pu239.messgroesse_id = 769)\nLEFT JOIN public.lada_messwert pu23940\n  ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\nLEFT JOIN public.lada_messwert te132\n  ON (te132.messungs_id = messung.id AND te132.messgroesse_id = 325)\nLEFT JOIN public.lada_messwert pb212\n  ON (pb212.messungs_id = messung.id AND pb212.messgroesse_id = 672)\nLEFT JOIN public.lada_messwert pb214\n  ON (pb214.messungs_id = messung.id AND pb214.messgroesse_id = 673)\nLEFT JOIN public.lada_messwert bi212\n  ON (bi212.messungs_id = messung.id AND bi212.messgroesse_id = 684)\nLEFT JOIN public.lada_messwert bi214  ON (bi214.messungs_id = messung.id AND bi214.messgroesse_id = 686) \nLEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\nLEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)
12	SELECT probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n datenbasis.datenbasis AS dBasis,\n stamm.mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.solldatum_beginn AS sollBegin,\n probe.solldatum_ende AS sollEnd,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n ort.ort_id AS ortId,\n ort.gem_id AS eGemId,\n verwaltungseinheit.bezeichnung AS eGem,\n probe.ext_id AS externeProbeId,\n probe.mpr_id AS mprId,\n messprogramm_kategorie.code AS mplCode,\n messprogramm_kategorie.bezeichnung AS mpl,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n kta_gruppe.kta_gruppe AS anlage,\n kta_gruppe.beschreibung AS anlagebeschr,\n rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n probe.test,\n betriebsart.name AS messRegime,\n land.messung.geplant,\n land.messung.messzeitpunkt AS messbeginn,\n land.messung.mmt_id AS mmtId,\n stamm.mess_methode.messmethode AS mmt,\n stamm.probenehmer.bezeichnung AS prnBezeichnung,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n pkommentar.pkommentar AS pKommentar,\n pzs.pzs,\n  stamm.ort.oz_id AS ozId,\n staat_uo.staat AS uStaat,\n status_kombi.id AS statusK,\n stamm.probenehmer.prn_id AS prnId,\n stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n  land.messung.id,\n  land.messprogramm.probenahmemenge AS probemenge,\n land.messung.messdauer,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  verwaltungseinheit.regbezirk AS eRbezId,\n  messung.ext_id AS externeMessungsId,\n  stamm.probenehmer.betrieb AS prnBetrieb,\n stamm.probenehmer.plz AS prnPlz,\n stamm.probenehmer.ort AS prnOrt,\n stamm.probenehmer.strasse AS prnStrasse,\n stamm.probenehmer.telefon AS prnTelefon,\n stamm.probenehmer.bemerkung AS prnBemerkung,\n  messprogramm.kommentar AS mprKommentar,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  verwaltungseinheit_kreis.bezeichnung AS eKreis,\n  verwaltungseinheit_rbez.bezeichnung AS eRbez,\n  stamm.mess_stelle.beschreibung AS mstBeschr,\nstamm.get_desk_beschreibung(probe.media_desk, 2) AS desk2Beschr\nFROM land.probe\n LEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\n LEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\n LEFT JOIN stamm.verwaltungseinheit AS verwaltungseinheit_kreis ON (stamm.verwaltungseinheit.kreis = verwaltungseinheit_kreis.id)\n LEFT JOIN stamm.verwaltungseinheit AS verwaltungseinheit_rbez ON (stamm.verwaltungseinheit.regbezirk = verwaltungseinheit_rbez.id)\n LEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN stamm.messprogramm_kategorie\n ON (probe.mpl_id = messprogramm_kategorie.id)\n LEFT JOIN stamm.umwelt\n ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber\n ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.kta_gruppe\n ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\n LEFT JOIN stamm.rei_progpunkt_gruppe\n ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\n LEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\n INNER JOIN land.messung ON probe.id = messung.probe_id\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (\n probe.id = ortszuordnung_uo.probe_id\n AND ortszuordnung_uo.ortszuordnung_typ IN ('U')\n) \n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id) \n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN land.messprogramm ON (land.probe.mpr_id = land.messprogramm.id)\n LEFT JOIN (\n SELECT probe.id,\n array_to_string(array_agg(land.zusatz_wert.pzs_id || ' ' || stamm.proben_zusatz.beschreibung || ': ' ||  coalesce(land.zusatz_wert.kleiner_als,'') || ' ' || land.zusatz_wert.messwert_pzs || ' ' || coalesce(pzsmeh.einheit,'') || CASE WHEN land.zusatz_wert.messfehler IS NOT NULL THEN ' +/-' ELSE '' END || coalesce(to_char(land.zusatz_wert.messfehler,'99.9'),'') || CASE WHEN land.zusatz_wert.messfehler IS NOT NULL THEN ' %' ELSE '' END), ' # ', '') AS pzs\n FROM land.probe\n LEFT JOIN land.zusatz_wert ON (land.probe.id = land.zusatz_wert.probe_id)\n LEFT JOIN stamm.proben_zusatz ON (land.zusatz_wert.pzs_id = stamm.proben_zusatz.id)\n LEFT JOIN stamm.mess_einheit AS pzsmeh ON (stamm.proben_zusatz.meh_id = pzsmeh.id)\n GROUP BY probe.id\n) pzs ON probe.id = pzs.id\nLEFT JOIN (\n SELECT land.probe.id,\n string_agg(land.kommentar_p.text,' # ') AS pkommentar\n FROM land.probe\nLEFT JOIN land.kommentar_p ON land.probe.id = land.kommentar_p.probe_id GROUP BY probe.id) pkommentar ON land.probe.id = pkommentar.id
14	SELECT probe.id AS id,\n probe.ext_id AS externeProbeId,\n stamm.mess_stelle.netzbetreiber_id AS netzId,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n probe.mst_id AS mstId,\n probe.datenbasis_id AS datenbasisId,\n probe.test,\n probe.ba_id AS baId,\n probenart.probenart AS probenartId,\n probe.solldatum_beginn AS solldatumBeginn,\n probe.solldatum_ende AS solldatumEnde,\n probe.mpr_id AS mprId,\n probe.media_desk AS mediaDesk,\n probe.umw_id AS umwId,\n array_to_string(mmtid.mmt, ', ', '') AS mmt,\n ort.gem_id AS gemId, \n stamm.probenehmer.id AS probeNehmerId, \n array_to_string(tags.tags, ',', '') AS tags\n FROM land.probe\n LEFT JOIN (\n SELECT probe.id,\n array_agg(tag.tag) AS tags\n FROM land.probe\n JOIN land.tagzuordnung ON probe.id = tagzuordnung.probe_id\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id\n) tags ON probe.id = tags.id\n LEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN (\n SELECT probe.id as pid,\n array_agg(messung.mmt_id) AS mmt\n FROM land.probe\n JOIN land.messung ON (probe.id = messung.probe_id)\n GROUP BY probe.id\n) mmtid ON probe.id = mmtid.pid
15	SELECT messung.id,\n probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n messung.nebenproben_nr AS npNr,\n status_protokoll.datum AS statusD,\n datenbasis.datenbasis AS dBasis,\n mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n ort.gem_id AS eGemId,\n verwaltungseinheit.bezeichnung AS eGem,\n CASE\n WHEN h3.messwert_nwg = '<'\n THEN h3.messwert_nwg || to_char(h3.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(h3.messwert, '0.99eeee')\n END AS h3,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n CASE\n WHEN co60.messwert_nwg = '<'\n THEN co60.messwert_nwg || to_char(co60.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(co60.messwert, '0.99eeee')\n END AS co60,\n CASE\n WHEN sr89.messwert_nwg = '<'\n THEN sr89.messwert_nwg || to_char(sr89.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr89.messwert, '0.99eeee')\n END AS sr89,\n CASE\n WHEN sr90.messwert_nwg = '<'\n THEN sr90.messwert_nwg || to_char(sr90.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr90.messwert, '0.99eeee')\n END AS sr90,  \n CASE\n WHEN ru103.messwert_nwg = '<'\n THEN ru103.messwert_nwg || to_char(ru103.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ru103.messwert, '0.99eeee')\n END AS ru103,\n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,  \n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN ce144.messwert_nwg = '<'\n THEN ce144.messwert_nwg || to_char(ce144.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ce144.messwert, '0.99eeee')\n END AS ce144,\n CASE\n WHEN u234.messwert_nwg = '<'\n THEN u234.messwert_nwg || to_char(u234.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u234.messwert, '0.99eeee')\n END AS u234,  \n CASE\n WHEN u235.messwert_nwg = '<'\n THEN u235.messwert_nwg || to_char(u235.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u235.messwert, '0.99eeee')\n END AS u235,\n CASE\n WHEN u238.messwert_nwg = '<'\n THEN u238.messwert_nwg || to_char(u238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u238.messwert, '0.99eeee')\n END AS u238,\n CASE\n WHEN pu238.messwert_nwg = '<'\n THEN pu238.messwert_nwg || to_char(pu238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu238.messwert, '0.99eeee')\n END AS pu238,  \n CASE\n WHEN pu239.messwert_nwg = '<'\n THEN pu239.messwert_nwg || to_char(pu239.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu239.messwert, '0.99eeee')\n END AS pu239,\n CASE\n WHEN pu23940.messwert_nwg = '<'\n THEN pu23940.messwert_nwg || to_char(pu23940.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu23940.messwert, '0.99eeee')\n END AS pu23940,\n CASE\n WHEN te132.messwert_nwg = '<'\n THEN te132.messwert_nwg || to_char(te132.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(te132.messwert, '0.99eeee')\n END AS te132,  \n CASE\n WHEN pb212.messwert_nwg = '<'\n THEN pb212.messwert_nwg || to_char(pb212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb212.messwert, '0.99eeee')\n END AS pb212,\n CASE\n WHEN pb214.messwert_nwg = '<'\n THEN pb214.messwert_nwg || to_char(pb214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb214.messwert, '0.99eeee')\n END AS pb214,\n CASE\n WHEN bi212.messwert_nwg = '<'\n THEN bi212.messwert_nwg || to_char(bi212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi212.messwert, '0.99eeee')\n END AS bi212,  \n CASE\n WHEN bi214.messwert_nwg = '<'\n THEN bi214.messwert_nwg || to_char(bi214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi214.messwert, '0.99eeee')\n END AS bi214,\n messung.mmt_id AS mmtId,\n probe.ext_id AS externeProbeId,\n messung.ext_id AS externeMessungsId,\n status_kombi.id AS statusK,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n public.st_asgeojson(ort.geom) AS entnahmeGeom,\n probe.mpr_id AS mprId,\n messprogramm_kategorie.code AS mplCode,\n messprogramm_kategorie.bezeichnung AS mpl,\n probe.test,\n betriebsart.name AS messRegime,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n messung.geplant,\n probe.solldatum_beginn AS sollBegin,\n probe.solldatum_ende AS sollEnd,\n ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n land.messung.messdauer,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n stamm.ort.oz_id AS ozId,\n stamm.ortszusatz.ortszusatz AS oz,\n verwaltungseinheit.kreis AS eKreisId,\n verwaltungseinheit.bundesland AS eBlId,\n stamm.staat.staat AS eStaat,\n messung.fertig AS fertig,\n staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags,\n pkommentar.pkommentar AS pKommentar,\n mkommentar.mkommentar AS mKommentar,\n stamm.get_desk_beschreibung(probe.media_desk, 0) AS desk0Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 1) AS desk1Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 2) AS desk2Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 3) AS desk3Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 4) AS desk4Beschr\n FROM land.probe\n LEFT JOIN stamm.mess_stelle\n ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\n INNER JOIN land.messung\n ON probe.id = messung.probe_id\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN stamm.status_wert\n ON stamm.status_wert.id = stamm.status_kombi.wert_id\n LEFT JOIN stamm.status_stufe\n ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\n LEFT JOIN stamm.datenbasis\n ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart\n ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung\n ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort\n ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.verwaltungseinheit\n ON (ort.gem_id = verwaltungseinheit.id)\n LEFT JOIN stamm.ortszusatz\n ON (ort.oz_id = ortszusatz.ozs_id)\n LEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\n LEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\n LEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.messprogramm_kategorie\n ON (probe.mpl_id = messprogramm_kategorie.id)\n LEFT JOIN stamm.betriebsart\n ON (probe.ba_id = stamm.betriebsart.id)\n LEFT JOIN public.lada_messwert h3\n ON (h3.messungs_id = messung.id AND h3.messgroesse_id = 1)\n LEFT JOIN public.lada_messwert k40\n ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\n LEFT JOIN public.lada_messwert co60\n ON (co60.messungs_id = messung.id AND co60.messgroesse_id = 68)\n LEFT JOIN public.lada_messwert sr89\n ON (sr89.messungs_id = messung.id AND sr89.messgroesse_id = 164)\n LEFT JOIN public.lada_messwert sr90\n ON (sr90.messungs_id = messung.id AND sr90.messgroesse_id = 165)\n LEFT JOIN public.lada_messwert ru103\n ON (ru103.messungs_id = messung.id AND ru103.messgroesse_id = 220)\n LEFT JOIN public.lada_messwert i131\n ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\n LEFT JOIN public.lada_messwert cs134\n ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\n LEFT JOIN public.lada_messwert cs137\n ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\n LEFT JOIN public.lada_messwert ce144\n ON (ce144.messungs_id = messung.id AND ce144.messgroesse_id = 404)\n LEFT JOIN public.lada_messwert u234\n ON (u234.messungs_id = messung.id AND u234.messgroesse_id = 746)\n LEFT JOIN public.lada_messwert u235\n ON (u235.messungs_id = messung.id AND u235.messgroesse_id = 747)\n LEFT JOIN public.lada_messwert u238\n ON (u238.messungs_id = messung.id AND u238.messgroesse_id = 750)\n LEFT JOIN public.lada_messwert pu238\n ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\n LEFT JOIN public.lada_messwert pu239\n ON (pu239.messungs_id = messung.id AND pu239.messgroesse_id = 769)\n LEFT JOIN public.lada_messwert pu23940\n ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\n LEFT JOIN public.lada_messwert te132\n ON (te132.messungs_id = messung.id AND te132.messgroesse_id = 325)\n LEFT JOIN public.lada_messwert pb212\n ON (pb212.messungs_id = messung.id AND pb212.messgroesse_id = 672)\n LEFT JOIN public.lada_messwert pb214\n ON (pb214.messungs_id = messung.id AND pb214.messgroesse_id = 673)\n LEFT JOIN public.lada_messwert bi212\n ON (bi212.messungs_id = messung.id AND bi212.messgroesse_id = 684)\n LEFT JOIN public.lada_messwert bi214  \n ON (bi214.messungs_id = messung.id AND bi214.messgroesse_id = 686) \n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n LEFT JOIN (\n SELECT land.probe.id,\n string_agg(land.kommentar_p.text,' # ') AS pkommentar\n FROM land.probe\n LEFT JOIN land.kommentar_p \n ON land.probe.id = land.kommentar_p.probe_id GROUP BY probe.id) pkommentar \n ON land.probe.id = pkommentar.id\n LEFT JOIN (\n SELECT land.messung.id,\n string_agg(land.kommentar_m.TEXT, ' # ') AS mkommentar\n FROM land.messung\n LEFT JOIN land.kommentar_m \n ON land.messung.id = land.kommentar_m.messungs_id GROUP BY messung.id) mkommentar \n ON land.messung.id = mkommentar.id
21	SELECT messprogramm.id AS mpNr,\n  stamm.mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN messprogramm.mst_id = messprogramm.labor_mst_id\n      THEN messprogramm.mst_id\n    ELSE messprogramm.mst_id || '-' || messprogramm.labor_mst_id\n    END AS mstLaborId,\n  datenbasis.datenbasis AS dBasis,\n  betriebsart.name AS messRegime,\n  probenart.probenart AS pArt,\n  messprogramm.umw_id AS umwId,\n  messprogramm.media_desk AS deskriptoren,\n  messprogramm.probenintervall AS intervall,\n  ort.ort_id AS ortId,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  CASE \n    WHEN messprogramm.mst_id = messprogramm.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messprogramm.aktiv AS aktiv,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n umwelt.umwelt_bereich AS umw,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  messprogramm.test,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  ort_uo.ort_id AS uOrtId,\n messprogramm.kommentar AS mprKommentar,\n  messprogramm.probe_kommentar AS mprPKommentar,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n stamm.probenehmer.prn_id AS prnId,\n stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung\nFROM land.messprogramm\nLEFT JOIN stamm.mess_stelle\n  ON (messprogramm.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle as labormessstelle\n  ON (messprogramm.labor_mst_id = labormessstelle.id)\nLEFT JOIN stamm.datenbasis\n  ON (messprogramm.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart\n  ON (messprogramm.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung_mp\n  ON (\n      messprogramm.id = ortszuordnung_mp.messprogramm_id\n      AND ortszuordnung_mp.ortszuordnung_typ IN ('E', 'R')\n      )\nLEFT JOIN stamm.ort\n  ON (ortszuordnung_mp.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit\n  ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (messprogramm.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN land.ortszuordnung_mp AS ortszuordnung_mp_uo\n ON (\n messprogramm.id = ortszuordnung_mp_uo.messprogramm_id\n AND ortszuordnung_mp_uo.ortszuordnung_typ IN ('U')\n)\n LEFT JOIN stamm.ort AS ort_uo\n ON (ortszuordnung_mp_uo.ort_id = ort_uo.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.umwelt\n  ON (messprogramm.umw_id = umwelt.id)\nLEFT JOIN stamm.kta_gruppe\n  ON (messprogramm.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe\n  ON (messprogramm.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.betriebsart ON (messprogramm.ba_id = stamm.betriebsart.id)\nLEFT JOIN stamm.probenehmer ON (land.messprogramm.probe_nehmer_id = stamm.probenehmer.id)
31	SELECT ort.id,\n  ort.netzbetreiber_id AS netzId,\n  ort.ort_id AS ortId,\n  ort_typ.code AS ortTyp,\n  ort.kurztext,\n  ort.langtext,\n  staat.staat AS staat,\n  verwaltungseinheit.bezeichnung AS verwaltungseinheit,\n  ort.nuts_code AS nutsCode,\n  ort.oz_id AS ozId,\n  kta_gruppe.kta_gruppe AS anlage,\n  ort.mp_art AS mpArt,\n  koordinaten_art.koordinatenart AS koordinatenArt,\n  ort.koord_x_extern AS koordXExtern,\n  ort.koord_y_extern AS koordYExtern,\n  PUBLIC.ST_X(ort.geom) AS longitude,\n  PUBLIC.ST_Y(ort.geom) AS latitude,\n  ort.hoehe_ueber_nn AS hoeheUeberNn,\n  ort.hoehe_land AS hoeheLand,\n  ort.aktiv,\n  ort.letzte_aenderung AS letzteAenderung,\n  ort.zone,\n  ort.sektor,\n  ort.zustaendigkeit,\n  ort.berichtstext,\n  ort.unscharf,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  ort.gem_id AS verwid\nFROM stamm.ort\nLEFT JOIN stamm.verwaltungseinheit\n  ON ort.gem_id = verwaltungseinheit.id\nLEFT JOIN stamm.staat\n  ON stamm.staat.id = ort.staat_id\nINNER JOIN stamm.koordinaten_art\n  ON stamm.koordinaten_art.id = ort.kda_id\nLEFT JOIN stamm.ort_typ\n  ON ort.ort_typ = ort_typ.id\nLEFT JOIN stamm.kta_gruppe\n  ON kta_gruppe.id = ort.kta_gruppe_id\nLEFT JOIN stamm.netz_betreiber\n  ON (ort.netzbetreiber_id = netz_betreiber.id)
32	SELECT probenehmer.id, netzbetreiber_id AS netzId, prn_id AS prnId, bearbeiter, bemerkung, betrieb, bezeichnung, kurz_bezeichnung AS kurzBezeichnung, ort, plz, strasse, telefon, tourenplan, typ, letzte_aenderung AS letzteAenderung, netz_betreiber.netzbetreiber AS netzbetreiber FROM stamm.probenehmer LEFT JOIN stamm.netz_betreiber ON (stamm.probenehmer.netzbetreiber_id = netz_betreiber.id)
33	SELECT datensatz_erzeuger.id, datensatz_erzeuger.netzbetreiber_id AS netzId, datensatz_erzeuger_id AS datensatzErzeugerId, mst_id AS mstId, bezeichnung, letzte_aenderung AS letzteAenderung, netz_betreiber.netzbetreiber AS netzbetreiber, stamm.mess_stelle.mess_stelle AS mst FROM stamm.datensatz_erzeuger LEFT JOIN stamm.netz_betreiber ON (stamm.datensatz_erzeuger.netzbetreiber_id = netz_betreiber.id) LEFT JOIN stamm.mess_stelle ON (stamm.datensatz_erzeuger.mst_id = stamm.mess_stelle.id)
34	SELECT messprogramm_kategorie.id, netzbetreiber_id AS netzId, code, bezeichnung, letzte_aenderung AS letzteAenderung, netz_betreiber.netzbetreiber AS netzbetreiber FROM stamm.messprogramm_kategorie LEFT JOIN stamm.netz_betreiber ON (stamm.messprogramm_kategorie.netzbetreiber_id = netz_betreiber.id)
35	SELECT messgroesse.id AS mgrId,\n messgroesse,\n beschreibung AS mgrBeschreibung,\n idf_nuklid_key AS idfNuklidKey,\n ist_leitnuklid AS istLeitnuklid,\n eudf_nuklid_id AS eudfNuklidId,\n kennung_bvl AS kennungBvl\n FROM stamm.messgroesse
36	SELECT mess_einheit.id AS meId,\n einheit AS me,\n beschreibung AS meBeschreibung,\n eudf_messeinheit_id AS eudfMesseinheitId,\n umrechnungs_faktor_eudf AS umrEudf\n FROM stamm.mess_einheit
37	SELECT mess_methode.id AS mmtId,\n messmethode AS mmt,\n beschreibung AS mmtBeschreibung\n FROM stamm.mess_methode
38	SELECT mess_stelle.id AS mstId,\n mess_stelle AS mst,\n mess_stelle.netzbetreiber_id AS netzId,\n beschreibung AS mstBeschreibung,\n mst_typ AS mstTyp,\n amtskennung AS mstAmtskennung,\n netz_betreiber.netzbetreiber AS netzbetreiber\n  FROM stamm.mess_stelle\n LEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)
39	SELECT umwelt.id AS umwId,\n umwelt_bereich AS umw,\n mess_einheit.einheit AS me,\n mess_einheit_2.einheit AS me2,\n umwelt.beschreibung AS umwBeschreibung\n FROM stamm.umwelt\n LEFT JOIN stamm.mess_einheit ON (stamm.umwelt.meh_id = stamm.mess_einheit.id)\n LEFT JOIN stamm.mess_einheit AS mess_einheit_2 ON (stamm.umwelt.meh_id_2 = mess_einheit_2.id)
41	SELECT lada_messwert.id,\n probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messgroesse.messgroesse AS messgroesse,\n  coalesce(lada_messwert.messwert_nwg, ' ') AS nwg,\n  lada_messwert.messwert AS wert,\n  lada_messwert.nwg_zu_messwert AS nwgZuMesswert,\n  lada_messwert.messfehler AS fehler,\n  mess_einheit.einheit AS einheit,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n    ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n stamm.ort.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  coalesce(lada_messwert.messwert,lada_messwert.nwg_zu_messwert) AS wertNwg,\n  ort.ort_id AS ortId,\n  staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags\nFROM land.probe\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nJOIN land.messung ON probe.id = messung.probe_id\nJOIN land.status_protokoll ON messung.STATUS = status_protokoll.id\nJOIN stamm.status_kombi ON status_protokoll.status_kombi = stamm.status_kombi.id\nJOIN stamm.status_wert ON stamm.status_wert.id = stamm.status_kombi.wert_id\nJOIN stamm.status_stufe ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz\n  ON (ort.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id\n)LEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nJOIN public.lada_messwert ON (lada_messwert.messungs_id = messung.id)\nJOIN stamm.messgroesse ON (messgroesse.id = lada_messwert.messgroesse_id)\nLEFT JOIN stamm.mess_einheit ON (mess_einheit.id = lada_messwert.meh_id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n
42	SELECT concat(messung.id, '_', lada_messwert.id) AS id,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  ort.ort_id AS ortId,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messgroesse.messgroesse AS messgroesse,\n  coalesce(lada_messwert.messwert_nwg, ' ') AS nwg,\n  lada_messwert.messwert AS wert,\n  lada_messwert.nwg_zu_messwert AS nwgZuMesswert,\n  lada_messwert.messfehler AS fehler,\n  mess_einheit.einheit AS einheit,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  ort.berichtstext AS berichtstext,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  stamm.mess_methode.messmethode AS mmt,\n  land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  probe.media AS medium,\n  coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn) AS mitteSammelzeitraum,\n  extract(quarter FROM (coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn))) AS qmitteSammelzeitraum,\n  extract(year FROM (coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn))) AS jmitteSammelzeitraum,\n  pzs.pzs,\n  pkommentar.pkommentar AS pKommentar,\n  mkommentar.mkommentar AS mKommentar,\n  array_to_string(tags.tags, ', ', '') AS tags,\n  mess_stelle.beschreibung AS messstellenadr,\n  messgroesse.id AS mgrId,\n  labormessstelle.beschreibung AS messlaboradr,\n probe.labor_mst_id AS laborMstId,\n  probe.mst_id AS mstId\n FROM land.probe\nLEFT JOIN (\n  SELECT probe.id,\n    array_agg(tag.tag) AS tags\n  FROM land.probe\n  JOIN land.tagzuordnung ON probe.id = tagzuordnung.probe_id\n  JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n  GROUP BY probe.id\n  ) tags ON probe.id = tags.id\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nJOIN land.messung ON probe.id = messung.probe_id\nJOIN land.status_protokoll ON messung.STATUS = status_protokoll.id\nJOIN stamm.status_kombi ON status_protokoll.status_kombi = stamm.status_kombi.id\nJOIN stamm.status_wert ON stamm.status_wert.id = stamm.status_kombi.wert_id\nJOIN stamm.status_stufe ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN public.lada_messwert ON (lada_messwert.messungs_id = messung.id)\nLEFT JOIN stamm.messgroesse ON (messgroesse.id = lada_messwert.messgroesse_id)\nLEFT JOIN stamm.mess_einheit ON (mess_einheit.id = lada_messwert.meh_id)\nLEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\nLEFT JOIN (\n  SELECT probe.id,\n    array_to_string(array_agg(stamm.proben_zusatz.beschreibung || ': ' || translate(to_char(land.zusatz_wert.messwert_pzs,'9999.9'),'. ',',') || ' ' || pzsmeh.einheit), ' # ', '') AS pzs\n  FROM land.probe\n  LEFT JOIN land.zusatz_wert ON (\n      land.probe.id = land.zusatz_wert.probe_id\n      AND land.zusatz_wert.pzs_id = 'A25'\n      )\n  LEFT JOIN stamm.proben_zusatz ON (land.zusatz_wert.pzs_id = stamm.proben_zusatz.id)\n  LEFT JOIN stamm.mess_einheit AS pzsmeh ON (stamm.proben_zusatz.meh_id = pzsmeh.id)\n  GROUP BY probe.id\n  ) pzs ON probe.id = pzs.id\nLEFT JOIN (\n  SELECT land.probe.id,\n    string_agg(land.kommentar_p.TEXT, ' # ') AS pkommentar\n  FROM land.probe\n  LEFT JOIN land.kommentar_p ON land.probe.id = land.kommentar_p.probe_id\n  WHERE NOT kommentar_p.TEXT LIKE '(X)%'\n  GROUP BY probe.id\n  ) pkommentar ON land.probe.id = pkommentar.id\nLEFT JOIN (\n  SELECT land.messung.id,\n    string_agg(land.kommentar_m.TEXT, ' # ') AS mkommentar\n  FROM land.messung\n  LEFT JOIN land.kommentar_m ON land.messung.id = land.kommentar_m.messungs_id\n  WHERE NOT kommentar_m.TEXT LIKE '(X)%'\n  GROUP BY messung.id\n  ) mkommentar ON land.messung.id = mkommentar.id
51	SELECT deskriptor_umwelt.umw_id AS umwId,\n umwelt.umwelt_bereich AS umw,\n s00.sn AS s00Sn,\n s00.beschreibung AS s00Beschr,\n s01.sn AS s01Sn,\n s01.beschreibung AS s01Beschr,\n s02.sn AS s02Sn,\n s02.beschreibung AS s02Beschr,\n s03.sn AS s03Sn,\n s03.beschreibung AS s03Beschr,\n s04.sn AS s04Sn,\n s04.beschreibung AS s04Beschr,\n s05.sn AS s05Sn,\n s05.beschreibung AS s05Beschr,\n s06.sn AS s06Sn,\n s06.beschreibung AS s06Beschr,\n s07.sn AS s07Sn,\n s07.beschreibung AS s07Beschr,\n s08.sn AS s08Sn,\n s08.beschreibung AS s08Beschr,\n s09.sn AS s09Sn,\n s09.beschreibung AS s09Beschr,\n s10.sn AS s10Sn,\n s10.beschreibung AS s10Beschr,\n s11.sn AS s11Sn,\n s11.beschreibung AS s11Beschr\n FROM stamm.deskriptor_umwelt\n LEFT JOIN stamm.umwelt ON (stamm.deskriptor_umwelt.umw_id = stamm.umwelt.id)\n LEFT JOIN stamm.deskriptoren AS s00 ON (stamm.deskriptor_umwelt.s00 = s00.id)\n LEFT JOIN stamm.deskriptoren AS s01 ON (stamm.deskriptor_umwelt.s01 = s01.id)\n LEFT JOIN stamm.deskriptoren AS s02 ON (stamm.deskriptor_umwelt.s02 = s02.id)\n LEFT JOIN stamm.deskriptoren AS s03 ON (stamm.deskriptor_umwelt.s03 = s03.id)\n LEFT JOIN stamm.deskriptoren AS s04 ON (stamm.deskriptor_umwelt.s04 = s04.id)\n LEFT JOIN stamm.deskriptoren AS s05 ON (stamm.deskriptor_umwelt.s05 = s05.id)\n LEFT JOIN stamm.deskriptoren AS s06 ON (stamm.deskriptor_umwelt.s06 = s06.id)\n LEFT JOIN stamm.deskriptoren AS s07 ON (stamm.deskriptor_umwelt.s07 = s07.id)\n LEFT JOIN stamm.deskriptoren AS s08 ON (stamm.deskriptor_umwelt.s08 = s08.id)\n LEFT JOIN stamm.deskriptoren AS s09 ON (stamm.deskriptor_umwelt.s09 = s09.id)\n LEFT JOIN stamm.deskriptoren AS s10 ON (stamm.deskriptor_umwelt.s10 = s10.id)\n LEFT JOIN stamm.deskriptoren AS s11 ON (stamm.deskriptor_umwelt.s11 = s11.id)\n
52	SELECT\n deskriptoren.id AS deskId,\n deskriptoren.ebene AS deskEbene,\n deskriptoren.sn AS deskSn,\n deskriptoren.beschreibung AS deskBeschr,\n deskriptoren.bedeutung AS deskBedeutung,\n deskriptoren.vorgaenger AS deskVorgId,\n deskriptorenvorgaenger.ebene AS deskVorgEbene,\n deskriptorenvorgaenger.sn AS deskVorgSN,\n deskriptorenvorgaenger.beschreibung AS deskVorgBeschr\n FROM stamm.deskriptoren\n LEFT JOIN stamm.deskriptoren AS deskriptorenvorgaenger ON (deskriptoren.vorgaenger = deskriptorenvorgaenger.id)
53	SELECT verwaltungseinheit.id AS verwId,\n verwaltungseinheit.bezeichnung AS verwBez,\n verwaltungseinheit.plz AS plz,\n verwaltungseinheit.is_gemeinde AS isGem,\n verwaltungseinheit.is_landkreis AS isKreis,\n verwaltungseinheit.is_regbezirk AS isRbez,\n verwaltungseinheit.is_bundesland AS isBundesland,\n verwaltungseinheit.nuts AS nuts,\n verwaltungseinheit.bundesland AS bundeslandId,\n bl.bezeichnung AS bundesland,\n verwaltungseinheit.regbezirk AS rbezId,\n rb.bezeichnung AS rbez,\n verwaltungseinheit.kreis AS kreisId,\n lk.bezeichnung AS kreis\n FROM stamm.verwaltungseinheit\n LEFT JOIN stamm.verwaltungseinheit AS bl ON (stamm.verwaltungseinheit.bundesland = bl.id)\n LEFT JOIN stamm.verwaltungseinheit AS rb ON (stamm.verwaltungseinheit.regbezirk = rb.id)\n LEFT JOIN stamm.verwaltungseinheit AS lk ON (stamm.verwaltungseinheit.kreis = lk.id)
54	SELECT kta_gruppe.id AS anlageId,\n kta_gruppe.kta_gruppe AS anlageBez,\n kta_gruppe.beschreibung AS anlageBeschr\n FROM stamm.kta_gruppe
55	SELECT staat.id AS staatId,\n  staat.hkl_id AS hklId, staat.staat AS staatBez,\n staat.staat_kurz AS staatKurz,\n staat.staat_iso AS staatIso,\n staat.eu AS staatEu,\n koordinaten_art.koordinatenart AS kda,\n staat.koord_x_extern AS staatKoordX,\n staat.koord_y_extern AS staatKoordY\n FROM stamm.staat\n LEFT JOIN stamm.koordinaten_art ON (staat.kda_id = koordinaten_art.id)
56	SELECT ortszusatz.ozs_id AS ozId,\n ortszusatz.ortszusatz AS oz\n FROM stamm.ortszusatz
101	SELECT land.probe.ext_id AS externeProbeId,\n  datenbasis.datenbasis AS dBasis,\n  land.messung.mmt_id AS mmtId,\n  land.messung.ext_id AS externeMessungsId,\n  land.kommentar_p.TEXT AS pKommentar,\n  land.messprogramm.kommentar AS mprKommentar,\n  stamm.betriebsart.id AS messRegime,\n  CASE \n    WHEN substring(land.probe.media_desk, 4, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 4, 2)\n    END AS S00,\n  CASE \n    WHEN substring(land.probe.media_desk, 7, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 7, 2)\n    END AS S01,\n  CASE \n    WHEN substring(land.probe.media_desk, 10, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 10, 2)\n    END AS S02,\n  CASE \n    WHEN substring(land.probe.media_desk, 13, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 13, 2)\n    END AS S03,\n  CASE \n    WHEN substring(land.probe.media_desk, 16, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 16, 2)\n    END AS S04,\n  CASE \n    WHEN substring(land.probe.media_desk, 19, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 19, 2)\n    END AS S05,\n  CASE \n    WHEN substring(land.probe.media_desk, 22, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 22, 2)\n    END AS S06,\n  CASE \n    WHEN substring(land.probe.media_desk, 25, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 25, 2)\n    END AS S07,\n  CASE \n    WHEN substring(land.probe.media_desk, 28, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 28, 2)\n    END AS S08,\n  CASE \n    WHEN substring(land.probe.media_desk, 31, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 31, 2)\n    END AS S09,\n  CASE \n    WHEN substring(land.probe.media_desk, 34, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 34, 2)\n    END AS S10,\n  CASE \n    WHEN substring(land.probe.media_desk, 37, 2) = '00'\n      THEN ''\n    ELSE substring(land.probe.media_desk, 37, 2)\n    END AS S11,\n  CAST('' AS TEXT) AS S12,\n  land.probe.umw_id AS umw,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN rei_messpunkt.koord_x_extern\n    ELSE entnahmeort.koord_x_extern\n    END,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN rei_messpunkt.koord_y_extern\n    ELSE entnahmeort.koord_y_extern\n    END,\n  stamm.messprogramm_kategorie.code AS mplCode,\n  stamm.staat.hkl_id AS hklId,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN rei_messpunkt.gem_id\n    ELSE ursprungsort.gem_id\n    END AS uGemId,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN rei_messpunkt.gem_id\n    ELSE entnahmeort.gem_id\n    END AS eGemId,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN rei_messpunkt.oz_id\n    ELSE entnahmeort.oz_id\n    END AS ozId,\n  land.probe.solldatum_beginn AS sollBegin,\n  land.probe.solldatum_ende AS sollEnd,\n  stamm.probenart.probenart AS pArt,\n  stamm.probenehmer.prn_id AS prnId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  stamm.rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN rei_messpunkt.ort_id\n    ELSE entnahmeort.ort_id\n    END AS ortId,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN reimp_kreis.bezeichnung\n    ELSE entnahme_kreis.bezeichnung\n    END AS landkreis,\n  probe.mpr_id AS mprId,\n  probe.media AS medium,\n  messprogramm.probenahmemenge AS probemenge,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  CASE \n    WHEN probe.datenbasis_id = 4\n      THEN reimp_gem.bezeichnung\n    ELSE entnahme_gem.bezeichnung\n    END AS eGem,\n  stamm.messprogramm_kategorie.bezeichnung AS mpl,\n CASE\n WHEN probe.datenbasis_id = 4\n THEN rei_messpunkt.kda_id\n ELSE entnahmeort.kda_id\n END AS kdaId\nFROM land.probe\nLEFT JOIN stamm.datenbasis ON (land.probe.datenbasis_id = stamm.datenbasis.id)\nJOIN land.messung ON (land.probe.id = land.messung.probe_id)\nJOIN land.kommentar_p ON (\n    land.probe.id = land.kommentar_p.probe_id\n    AND (\n      land.kommentar_p.TEXT LIKE 'o_%'\n      OR land.kommentar_p.TEXT LIKE 'n_%'\n      )\n    )\nJOIN land.messprogramm ON (land.probe.mpr_id = land.messprogramm.id)\nJOIN land.messprogramm_mmt ON (\n    land.messprogramm.id = land.messprogramm_mmt.messprogramm_id\n    AND land.messprogramm_mmt.mmt_id = land.messung.mmt_id\n    )\nJOIN stamm.mess_stelle ON (\n    land.messprogramm.mst_id = stamm.mess_stelle.id\n    AND stamm.mess_stelle.netzbetreiber_id = '12'\n    )\nJOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (land.probe.mpl_id = stamm.messprogramm_kategorie.id)\nLEFT JOIN land.ortszuordnung oz_e ON (\n    land.probe.id = oz_e.probe_id\n    AND oz_e.ortszuordnung_typ = 'E'\n    )\nLEFT JOIN stamm.ort AS entnahmeort ON (oz_e.ort_id = entnahmeort.id)\nLEFT JOIN land.ortszuordnung oz_u ON (\n    land.probe.id = oz_u.probe_id\n    AND oz_u.ortszuordnung_typ = 'U'\n    )\nLEFT JOIN stamm.ort AS ursprungsort ON (oz_u.ort_id = ursprungsort.id)\nLEFT JOIN land.ortszuordnung oz_r ON (\n    land.probe.id = oz_r.probe_id\n    AND oz_r.ortszuordnung_typ = 'R'\n    )\nLEFT JOIN stamm.ort AS rei_messpunkt ON (oz_r.ort_id = rei_messpunkt.id)\nLEFT JOIN stamm.verwaltungseinheit AS entnahme_gem ON (entnahmeort.gem_id = entnahme_gem.id)\nLEFT JOIN stamm.verwaltungseinheit AS entnahme_kreis ON (entnahme_gem.kreis = entnahme_kreis.id)\nLEFT JOIN stamm.verwaltungseinheit AS reimp_gem ON (rei_messpunkt.gem_id = reimp_gem.id)\nLEFT JOIN stamm.verwaltungseinheit AS reimp_kreis ON (reimp_gem.kreis = reimp_kreis.id)\nLEFT JOIN stamm.probenart ON (land.probe.probenart_id = stamm.probenart.id)\nLEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (land.probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.staat ON (ursprungsort.staat_id = stamm.staat.id)\nLEFT JOIN stamm.betriebsart ON (land.probe.ba_id = stamm.betriebsart.id)
102	SELECT messung.id,\n probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n messung.nebenproben_nr AS npNr,\n status_protokoll.datum AS statusD,\n datenbasis.datenbasis AS dBasis,\n mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,\n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN be7.messwert_nwg = '<'\n THEN be7.messwert_nwg || to_char(be7.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(be7.messwert, '0.99eeee')\n END AS be7,\n CASE\n WHEN na22.messwert_nwg = '<'\n THEN na22.messwert_nwg || to_char(na22.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(na22.messwert, '0.99eeee')\n END AS na22,\n CASE\n WHEN pb210.messwert_nwg = '<'\n THEN pb210.messwert_nwg || to_char(pb210.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb210.messwert, '0.99eeee')\n END AS pb210,\n CASE\n WHEN am241.messwert_nwg = '<'\n THEN am241.messwert_nwg || to_char(am241.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(am241.messwert, '0.99eeee')\n END AS am241,\n messung.mmt_id AS mmtId,\n probe.ext_id AS externeProbeId,\n messung.ext_id AS externeMessungsId,\n status_kombi.id AS statusK,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n public.st_asgeojson(ort.geom) AS entnahmeGeom,\n probe.test,\n betriebsart.name AS messRegime,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n stamm.mess_methode.messmethode AS mmt,\n (rueckfrage_messung.messungs_id IS NOT NULL) AS hatRueckfrage,\n k40.nwg_zu_messwert AS k40nwgZuMesswert,\n k40.messfehler AS k40fehler,\n  i131.nwg_zu_messwert AS i131nwgZuMesswert,\n i131.messfehler AS i131fehler,\n  cs134.nwg_zu_messwert AS cs134nwgZuMesswert,\n cs134.messfehler AS c134fehler,\n  cs137.nwg_zu_messwert AS cs137nwgZuMesswert,\n cs137.messfehler AS c137fehler,\n  be7.nwg_zu_messwert AS be7nwgZuMesswert,\n be7.messfehler AS be7fehler,\n  na22.nwg_zu_messwert AS na22nwgZuMesswert,\n na22.messfehler AS na22fehler,\n  pb210.nwg_zu_messwert AS pb210nwgZuMesswert,\n pb210.messfehler AS pb210fehler,\n  am241.nwg_zu_messwert AS am241nwgZuMesswert,\n am241.messfehler AS am24fehler\n  FROM land.probe\n LEFT JOIN stamm.mess_stelle\n ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle\n ON (probe.labor_mst_id = labormessstelle.id)\n INNER JOIN land.messung\n ON probe.id = messung.probe_id\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN stamm.status_wert\n ON stamm.status_wert.id = stamm.status_kombi.wert_id\n LEFT JOIN stamm.status_stufe\n ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\n LEFT JOIN land.rueckfrage_messung\n ON rueckfrage_messung.messungs_id = messung.id\n LEFT JOIN stamm.datenbasis\n ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart\n ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung\n ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort \n ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.umwelt \n ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber\n ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.betriebsart\n ON (probe.ba_id = stamm.betriebsart.id)\n LEFT JOIN stamm.mess_methode\n ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN public.lada_messwert k40\n ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\n LEFT JOIN public.lada_messwert i131\n ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\n LEFT JOIN public.lada_messwert cs134\n ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\n LEFT JOIN public.lada_messwert cs137\n ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\n LEFT JOIN public.lada_messwert be7\n ON (be7.messungs_id = messung.id AND be7.messgroesse_id = 2)\n LEFT JOIN public.lada_messwert na22\n ON (na22.messungs_id = messung.id AND na22.messgroesse_id = 10)\n LEFT JOIN public.lada_messwert pb210\n ON (pb210.messungs_id = messung.id AND pb210.messgroesse_id = 670)\n LEFT JOIN public.lada_messwert am241\n ON (am241.messungs_id = messung.id AND am241.messgroesse_id = 781)
103	SELECT messung.id,\n probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n messung.nebenproben_nr AS npNr,\n status_protokoll.datum AS statusD,\n datenbasis.datenbasis AS dBasis,\n mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n ort.gem_id AS eGemId,\n verwaltungseinheit.bezeichnung AS eGem,\n CASE\n WHEN h3.messwert_nwg = '<'\n THEN h3.messwert_nwg || to_char(h3.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(h3.messwert, '0.99eeee')\n END AS h3,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n CASE\n WHEN co60.messwert_nwg = '<'\n THEN co60.messwert_nwg || to_char(co60.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(co60.messwert, '0.99eeee')\n END AS co60,\n CASE\n WHEN sr89.messwert_nwg = '<'\n THEN sr89.messwert_nwg || to_char(sr89.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr89.messwert, '0.99eeee')\n END AS sr89,\n CASE\n WHEN sr90.messwert_nwg = '<'\n THEN sr90.messwert_nwg || to_char(sr90.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr90.messwert, '0.99eeee')\n END AS sr90,  \n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,\n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN u234.messwert_nwg = '<'\n THEN u234.messwert_nwg || to_char(u234.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u234.messwert, '0.99eeee')\n END AS u234,  \n CASE\n WHEN u235.messwert_nwg = '<'\n THEN u235.messwert_nwg || to_char(u235.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u235.messwert, '0.99eeee')\n END AS u235,\n CASE\n WHEN u238.messwert_nwg = '<'\n THEN u238.messwert_nwg || to_char(u238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u238.messwert, '0.99eeee')\n END AS u238,\n CASE\n WHEN pu238.messwert_nwg = '<'\n THEN pu238.messwert_nwg || to_char(pu238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu238.messwert, '0.99eeee')\n END AS pu238,  \n CASE\n WHEN pu239.messwert_nwg = '<'\n THEN pu239.messwert_nwg || to_char(pu239.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu239.messwert, '0.99eeee')\n END AS pu239,\n CASE\n WHEN pu23940.messwert_nwg = '<'\n THEN pu23940.messwert_nwg || to_char(pu23940.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu23940.messwert, '0.99eeee')\n END AS pu23940,\n CASE\n WHEN am241.messwert_nwg = '<'\n THEN am241.messwert_nwg || to_char(am241.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(am241.messwert, '0.99eeee')\n END AS am241,  \n CASE\n WHEN cm242.messwert_nwg = '<'\n THEN cm242.messwert_nwg || to_char(cm242.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cm242.messwert, '0.99eeee')\n END AS cm242,\n CASE\n WHEN cm244.messwert_nwg = '<'\n THEN cm244.messwert_nwg || to_char(cm244.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cm244.messwert, '0.99eeee')\n END AS cm244,\n CASE\n WHEN cm24344.messwert_nwg = '<'\n THEN cm24344.messwert_nwg || to_char(cm24344.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cm24344.messwert, '0.99eeee')\n END AS cm24344,\n CASE\n WHEN c14.messwert_nwg = '<'\n THEN c14.messwert_nwg || to_char(c14.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(c14.messwert, '0.99eeee')\n END AS c14,\n CASE\n WHEN mn54.messwert_nwg = '<'\n THEN mn54.messwert_nwg || to_char(mn54.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(mn54.messwert, '0.99eeee')\n END AS mn54,\n CASE\n WHEN gammaodbrutto.messwert_nwg = '<'\n THEN gammaodbrutto.messwert_nwg || to_char(gammaodbrutto.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(gammaodbrutto.messwert, '0.99eeee')\n END AS gammaodbrutto,\n CASE\n WHEN gammaodlbrutto.messwert_nwg = '<'\n THEN gammaodlbrutto.messwert_nwg || to_char(gammaodlbrutto.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(gammaodlbrutto.messwert, '0.99eeee')\n END AS gammaodlbrutto,\n CASE\n WHEN gammaodlmin.messwert_nwg = '<'\n THEN gammaodlmin.messwert_nwg || to_char(gammaodlmin.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(gammaodlmin.messwert, '0.99eeee')\n END AS gammaodlmin,\n CASE\n WHEN gammaodlmax.messwert_nwg = '<'\n THEN gammaodlmax.messwert_nwg || to_char(gammaodlmax.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(gammaodlmax.messwert, '0.99eeee')\n END AS gammaodlmax,\n CASE\n WHEN neutrodbrutto.messwert_nwg = '<'\n THEN neutrodbrutto.messwert_nwg || to_char(neutrodbrutto.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(neutrodbrutto.messwert, '0.99eeee')\n END AS neutrodbrutto,\n CASE\n WHEN neutrodlbrutto.messwert_nwg = '<'\n THEN neutrodlbrutto.messwert_nwg || to_char(neutrodlbrutto.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(neutrodlbrutto.messwert, '0.99eeee')\n END AS neutrodlbrutto,\n CASE\n WHEN neutrodlmin.messwert_nwg = '<'\n THEN neutrodlmin.messwert_nwg || to_char(neutrodlmin.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(neutrodlmin.messwert, '0.99eeee')\n END AS neutrodlmin,\n CASE\n WHEN neutrodlmax.messwert_nwg = '<'\n THEN neutrodlmax.messwert_nwg || to_char(neutrodlmax.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(neutrodlmax.messwert, '0.99eeee')\n END AS neutrodlmax,\n messung.mmt_id AS mmtId,\n probe.ext_id AS externeProbeId,\n messung.ext_id AS externeMessungsId,\n status_kombi.id AS statusK,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n public.st_asgeojson(ort.geom) AS entnahmeGeom,\n kta_gruppe.kta_gruppe AS anlage,\n kta_gruppe.beschreibung AS anlagebeschr,\n rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n probe.test,\n betriebsart.name AS messRegime,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n probe.solldatum_beginn AS sollBegin,\n probe.solldatum_ende AS sollEnd,\n ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n array_to_string(tags.tags, ',', '') AS tags,\n pkommentar.pkommentar AS pKommentar,\n mkommentar.mkommentar AS mKommentar,\n probe.mpr_id AS mprId\n FROM land.probe\n LEFT JOIN stamm.mess_stelle\n ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle \n ON (probe.labor_mst_id = labormessstelle.id)\n INNER JOIN land.messung\n ON probe.id = messung.probe_id\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN stamm.status_wert\n ON stamm.status_wert.id = stamm.status_kombi.wert_id\n LEFT JOIN stamm.status_stufe\n ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\n LEFT JOIN land.rueckfrage_messung\n ON rueckfrage_messung.messungs_id = messung.id\n LEFT JOIN stamm.datenbasis\n ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart\n ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung\n ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort\n ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.verwaltungseinheit\n ON (ort.gem_id = verwaltungseinheit.id)\n LEFT JOIN stamm.umwelt \n ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber \n ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.kta_gruppe\n ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\n LEFT JOIN stamm.rei_progpunkt_gruppe\n ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\n LEFT JOIN stamm.betriebsart\n ON (probe.ba_id = stamm.betriebsart.id)\n LEFT JOIN public.lada_messwert h3\n ON (h3.messungs_id = messung.id AND h3.messgroesse_id = 1)\n LEFT JOIN public.lada_messwert k40\n ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\n LEFT JOIN public.lada_messwert co60\n ON (co60.messungs_id = messung.id AND co60.messgroesse_id = 68)\n LEFT JOIN public.lada_messwert sr89\n ON (sr89.messungs_id = messung.id AND sr89.messgroesse_id = 164)\n LEFT JOIN public.lada_messwert sr90\n ON (sr90.messungs_id = messung.id AND sr90.messgroesse_id = 165)\n LEFT JOIN public.lada_messwert i131\n ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\n LEFT JOIN public.lada_messwert cs134\n ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\n LEFT JOIN public.lada_messwert cs137\n ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\n LEFT JOIN public.lada_messwert u234\n ON (u234.messungs_id = messung.id AND u234.messgroesse_id = 746)\n LEFT JOIN public.lada_messwert u235\n ON (u235.messungs_id = messung.id AND u235.messgroesse_id = 747)\n LEFT JOIN public.lada_messwert u238\n ON (u238.messungs_id = messung.id AND u238.messgroesse_id = 750)\n LEFT JOIN public.lada_messwert pu238\n ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\n LEFT JOIN public.lada_messwert pu239\n ON (pu239.messungs_id = messung.id AND pu239.messgroesse_id = 769)\n LEFT JOIN public.lada_messwert pu23940\n ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\n LEFT JOIN public.lada_messwert am241\n ON (am241.messungs_id = messung.id AND am241.messgroesse_id = 781)\n LEFT JOIN public.lada_messwert cm242\n ON (cm242.messungs_id = messung.id AND cm242.messgroesse_id = 793)\n LEFT JOIN public.lada_messwert cm244\n ON (cm244.messungs_id = messung.id AND cm244.messgroesse_id = 795)\n LEFT JOIN public.lada_messwert cm24344\n ON (cm24344.messungs_id = messung.id AND cm24344.messgroesse_id = 978)\n LEFT JOIN public.lada_messwert c14\n ON (c14.messungs_id = messung.id AND c14.messgroesse_id = 5)\n LEFT JOIN public.lada_messwert mn54\n ON (mn54.messungs_id = messung.id AND mn54.messgroesse_id = 56)\n LEFT JOIN public.lada_messwert gammaodbrutto\n ON (gammaodbrutto.messungs_id = messung.id AND gammaodbrutto.messgroesse_id = 925)\n LEFT JOIN public.lada_messwert gammaodlbrutto\n ON (gammaodlbrutto.messungs_id = messung.id AND gammaodlbrutto.messgroesse_id = 909)\n LEFT JOIN public.lada_messwert gammaodlmin\n ON (gammaodlmin.messungs_id = messung.id AND gammaodlmin.messgroesse_id = 998)\n LEFT JOIN public.lada_messwert gammaodlmax\n ON (gammaodlmax.messungs_id = messung.id AND gammaodlmax.messgroesse_id = 999)\n LEFT JOIN public.lada_messwert neutrodbrutto\n ON (neutrodbrutto.messungs_id = messung.id AND neutrodbrutto.messgroesse_id = 927)\n LEFT JOIN public.lada_messwert neutrodlbrutto\n ON (neutrodlbrutto.messungs_id = messung.id AND neutrodlbrutto.messgroesse_id = 929)\n LEFT JOIN public.lada_messwert neutrodlmin\n ON (neutrodlmin.messungs_id = messung.id AND neutrodlmin.messgroesse_id = 1000)\n LEFT JOIN public.lada_messwert neutrodlmax\n ON (neutrodlmax.messungs_id = messung.id AND neutrodlmax.messgroesse_id = 1001)\n LEFT JOIN stamm.mess_methode\n ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n LEFT JOIN (\n SELECT land.probe.id,\n string_agg(land.kommentar_p.text,' # ') AS pkommentar\n FROM land.probe\n LEFT JOIN land.kommentar_p\n ON land.probe.id = land.kommentar_p.probe_id GROUP BY probe.id) pkommentar\n ON land.probe.id = pkommentar.id\n LEFT JOIN (\n SELECT land.messung.id,\n string_agg(land.kommentar_m.TEXT, ' # ') AS mkommentar\n FROM land.messung\n LEFT JOIN land.kommentar_m\n ON land.messung.id = land.kommentar_m.messungs_id GROUP BY messung.id) mkommentar\n ON land.messung.id = mkommentar.id
104	SELECT messung.id,\n probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n messung.nebenproben_nr AS npNr,\n datenbasis.datenbasis AS dBasis,\n mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n messung.mmt_id AS mmtId,\n probe.ext_id AS externeProbeId,\n messung.ext_id AS externeMessungsId,\n status_kombi.id AS statusK,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n public.st_asgeojson(ort.geom) AS entnahmeGeom,\n probe.test,\n betriebsart.name AS messRegime,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n ort.ort_id AS ortId,\n stamm.mess_methode.messmethode AS mmt,\n pu238.messwert_nwg AS pu238nwg,\n pu238.messwert AS pu238wert,\n pu238.nwg_zu_messwert AS pu238nwgZuMesswert,\n pu238einheit.einheit AS pu238einheit,\n pu238.messfehler AS pu238fehler,\n pu23940.messwert_nwg AS pu23940nwg,\n pu23940.messwert AS pu23940wert,\n pu23940.nwg_zu_messwert AS pu23940nwgZuMesswert,\n pu23940einheit.einheit AS pu23940einheit,\n pu23940.messfehler AS pu23940fehler,\n am241.messwert_nwg AS a241nwg,\n am241.messwert AS a241wert,\n am241.nwg_zu_messwert AS am241nwgZuMesswert,\n am241einheit.einheit AS a241einheit,\n am241.messfehler AS am241fehler,\n cm244.messwert_nwg AS cm244nwg,\n cm244.messwert AS cm244wert,\n cm244.nwg_zu_messwert AS cm244nwgZuMesswert,\n cm244einheit.einheit AS cm244einheit,\n cm244.messfehler AS cm244fehler,\n pzs_a07.messwert_pzs AS pzsA07wert,\n pzs_a11.messwert_pzs AS pzsA11wert\n FROM land.probe\n LEFT JOIN stamm.mess_stelle\n ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\n INNER JOIN land.messung\n ON probe.id = messung.probe_id\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN stamm.status_wert\n ON stamm.status_wert.id = stamm.status_kombi.wert_id\n LEFT JOIN stamm.status_stufe\n ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\n LEFT JOIN land.rueckfrage_messung\n ON rueckfrage_messung.messungs_id = messung.id\n LEFT JOIN stamm.datenbasis\n ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart\n ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung\n ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort\n ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.umwelt \n ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber \n ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.betriebsart\n ON (probe.ba_id = stamm.betriebsart.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN public.lada_messwert pu238\n ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\n LEFT JOIN public.lada_messwert pu23940\n ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\n LEFT JOIN public.lada_messwert am241\n ON (am241.messungs_id = messung.id AND am241.messgroesse_id = 781)\n LEFT JOIN public.lada_messwert cm244\n ON (cm244.messungs_id = messung.id AND cm244.messgroesse_id = 795) \n LEFT JOIN stamm.mess_einheit pu238einheit\n ON (pu238.meh_id = pu238einheit.id)\n LEFT JOIN stamm.mess_einheit pu23940einheit\n ON (pu23940.meh_id = pu23940einheit.id)\n LEFT JOIN stamm.mess_einheit am241einheit\n ON (am241.meh_id = am241einheit.id)\n LEFT JOIN stamm.mess_einheit cm244einheit\n ON (cm244.meh_id = cm244einheit.id)\n LEFT JOIN land.zusatz_wert AS pzs_a07 \n ON (probe.id = pzs_a07.probe_id AND pzs_a07.pzs_id = 'A07')\n LEFT JOIN land.zusatz_wert AS pzs_a11 \n ON (probe.id = pzs_a11.probe_id AND pzs_a11.pzs_id = 'A11')
\.
-- End help in debugging failing check constraint:
SET client_min_messages = warning;

--
-- Data for Name: filter; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.filter (id, sql, parameter, type, name) FROM stdin;
1	probe.ext_id ~ :externeProbeId	externeProbeId	0	probe_ext_id
2	probe.hauptproben_nr ~ :hauptprobenNr	hauptprobenNr	0	probe_hauptproben_nr
3	(probe.mst_id IN ( :mstId ) OR probe.labor_mst_id IN ( :mstId ))	mstId	4	probe_mst_id
4	probe.umw_id IN ( :umwId )	umwId	4	probe_umw_id
5	probe.test = cast(:test AS boolean)	test	2	probe_test
6	probe.probeentnahme_beginn >= to_timestamp(cast(:timeBegin AS DOUBLE PRECISION))	timeBegin	3	probe_entnahme_beginn
7	probe.probeentnahme_ende <= to_timestamp(cast(:timeEnd AS DOUBLE PRECISION))	timeEnd	3	probe_entnahme_beginn
8	probe.datenbasis_id IN ( :datenbasis )	datenbasis	5	datenbasis
9	probe.probenart_id IN ( :probenart )	probenart	5	probenart
10	ort.gem_id IN (:gemId)	gemId	4	ort_gem_id
11	ort.ort_id ~ :ortId	ortId	0	ort_ort_id
12	verwaltungseinheit.bezeichnung ~ :bezeichnung	bezeichnung	0	verwaltungseinheit_bezeichnung
13	mess_stelle.netzbetreiber_id IN ( :netzId )	netzId	4	netzbetreiber_id
14	probe.probeentnahme_beginn BETWEEN to_timestamp(cast(:fromPeBegin AS DOUBLE PRECISION)) AND to_timestamp(cast(:toPeBegin AS DOUBLE PRECISION))	fromPeBegin,toPeBegin	6	Entnahmebeginn von-bis
15	probe.probeentnahme_ende BETWEEN to_timestamp(cast(:fromPeEnd AS DOUBLE PRECISION)) AND to_timestamp(cast(:toPeEnd AS DOUBLE PRECISION))	fromPeEnd,toPeEnd	6	Entnahmeende von-bis
16	probe.letzte_aenderung BETWEEN to_timestamp(cast(probeLetzteAenderungFrom AS DOBULE PRECISION)) AND to_timestamp(cast(probeLetzteAenderungTo AS DOUBLE PRECISION))	probeLetzteAenderungFrom,probeLetzteAenderungTo	3	Letzte Aenderung von-bis
17	probe.id = cast(:probeId AS INTEGER)	probeId	0	ProbeID
18	:genTextParam ~ :genTextValue	genText	7	generic_text_filter
19	status_stufe.id IN :statusStufe	statusStufe	5	statusStufe_filter
20	probe.solldatum_beginn BETWEEN to_timestamp(cast(:fromSollBegin AS DOUBLE PRECISION)) AND to_timestamp(cast(:toSollBegin AS DOUBLE PRECISION))	fromSollBegin,toSollBegin	6	Sollbeginn von-bis
21	probe.solldatum_ende BETWEEN to_timestamp(cast(:fromSollEnde AS DOUBLE PRECISION)) AND to_timestamp(cast(:toSollEnde AS DOUBLE PRECISION))	fromSollEnde,toSollEnde	6	Sollend von-bis
22	messung.nebenproben_nr ~ :nebenproben_nr	nebenproben_nr	0	messung_nebenproben_nr
23	ort.netzbetreiber_id IN ( :netzId )	netzId	4	netzbetreiber_id
24	probenehmer.netzbetreiber_id IN ( :netzId )	netzId	4	netzbetreiber_id
25	datensatz_erzeuger.netzbetreiber_id IN ( :netzId )	netzId	4	netzbetreiber_id
26	messprogramm_kategorie.netzbetreiber_id IN ( :netzId )	netzId	4	netzbetreiber_id
27	(messprogramm.mst_id IN ( :mstId ) OR messprogramm.labor_mst_id IN ( :mstId ))	mstId	4	messprogramm_mst_id
28	umwelt.umwelt_bereich ~ ( :umwelt_filter )	umwelt_filter	0	Umweltbereich
29	probe.labor_mst_id IN ( :mlaborId )	mlaborId	4	probe_mlabor_id
30	betriebsart.id IN ( :messRegime )	messRegime	5	messRegime
31	messprogramm.aktiv = cast(:aktiv AS boolean)	aktiv	2	mpr_aktiv
32	status_wert.id IN :statusWert	statusWert	5	statusWert_filter
33	k40.messwert BETWEEN :messwertFrom AND :messwertTo	messwertFrom,messwertTo	1	k40_messwert
34	status_kombi.id IN ( :statusK )	statusK	5	statusK
35	messprogramm.datenbasis_id IN ( :datenbasis )	datenbasis	5	datenbasis
36	messprogramm.probenart_id IN ( :probenart )	probenart	5	probenart
37	messprogramm.umw_id IN ( :umwId )	umwId	4	mpr_umw_id
38	kta_gruppe.id IN ( :anlage )	anlage	5	kta_gruppe
39	rei_progpunkt_gruppe.id IN ( :reiproggrp )	reiproggrp	5	rei_prog_grp
40	messprogramm.test = cast(:test AS boolean)	test	2	mpr_test
41	probenehmer.prn_id ~ :prnId	prnId	0	probenehmer_prnId
42	messprogramm_kategorie.id IN ( :mplcode )	mplcode	5	mplcode
43	messung.geplant= cast(:geplant AS boolean)	geplant	2	geplant
44	lada_messwert.messgroesse_id IN ( :messgroesseId )	messgroesseId	5	messgroesse
45	probenehmer.id IN ( :prnId )	prnId	5	prn_id
46	mess_methode.id IN ( :mmtId )	mmtId	4	mmt_id
47	messung.messzeitpunkt BETWEEN to_timestamp(cast(:fromMessBegin AS DOUBLE PRECISION)) AND to_timestamp(cast(:toMessBegin AS DOUBLE PRECISION))	fromMessBegin,toMessBegin	6	Messbeginn von-bis
48	messung.messdauer BETWEEN :messdauerFrom AND :messdauerTo	messdauerFrom,messdauerTo	1	messdauer
49	lada_messwert.messwert BETWEEN :messwertFrom AND :messwertTo	messwertFrom,messwertTo	1	messwert
50	lada_messwert.nwg_zu_messwert BETWEEN :nwgzumesswertFrom AND :nwgzumesswertTo	nwgzumesswertFrom,nwgzumesswertTo	1	nwg_zu_messwert
51	(coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn)/2)),probe.probeentnahme_beginn)) BETWEEN to_timestamp(cast(:fromMitteSZeitraum AS DOUBLE PRECISION)) AND to_timestamp(cast(:toMitteSZeitraum AS DOUBLE PRECISION))	fromMitteSZeitraum,toMitteSZeitraum	6	mitte_sammelzeitraum
52	ort.aktiv = cast(:ortAktiv AS boolean)	ortAktiv	2	ort_aktiv
53	verwaltungseinheit.kreis IN (:kreisId)	kreisId	4	ort_lk_id
54	verwaltungseinheit.bundesland IN (:bundeslandId)	bundeslandId	4	ort_bl_id
55	ort.staat_id IN (:staatId)	staatId	5	ort_staat_id
56	messung.fertig = cast(:fertig AS boolean)	fertig	2	messung_fertig
57	verwaltungseinheit.regbezirk IN (:rbezId)	rbezId	4	ort_rbez_id
58	ort_uo.staat_id IN (:staatId)	staatId	5	ort_uo_staat_id
59	tags && CAST(ARRAY[ :tags ] AS text[])	tags	8	tags
60	messgroesse.messgroesse ~ :messgroesse	messgroesse	0	messgroesse_text
61	(rueckfrage_messung.messungs_id IS NOT NULL) = cast(:rueckfrage_messung AS boolean)	rueckfrage_messung	2	hat_rueckfrage
62	cast(probe.mpr_id AS TEXT) ~ :mprId	mprId	0	mpr_id
63	cast(messprogramm.id AS TEXT) ~ :mpNr	mpNr	0	mpNr
64	mess_einheit.einheit ~ :messEinheit	messEinheit	0	messEinheit_text
65	mess_methode.messmethode ~ :messmethode	messmethode	0	messmethode_text
66	umwelt.id ~ :umwId	umwId	0	umwId_text
67	umwelt.umwelt_bereich ~ :umw	umw	0	umw_text
68	deskriptor_umwelt.umw_id ~ :umwId	umwId	0	umwId_desk_text
69	cast(deskriptoren.id AS TEXT) ~ :deskId	deskId	0	deskId
70	cast(deskriptoren.ebene AS TEXT) ~ :deskEbene	deskEbene	0	deskEbene
71	cast(deskriptoren.vorgaenger AS TEXT) ~ :deskVorgId	deskVorgId	0	deskVorgId
72	cast(deskriptoren.sn AS TEXT) ~ :deskSn	deskSn	0	deskSn
73	cast(deskriptorenvorgaenger.ebene AS TEXT) ~ :deskVorgEbene	deskVorgEbene	0	deskVorgEbene
74	cast(deskriptorenvorgaenger.sn AS TEXT) ~ :deskVorgSn	deskVorgSn	0	deskVorgSn
75	cast(deskriptoren.beschreibung AS TEXT) ~ :deskBeschr	deskBeschr	0	deskBeschr_text
76	verwaltungseinheit.id ~ :verwId	verwId	0	verwId
77	verwaltungseinheit.bezeichnung ~ :verwBez	verwBez	0	verwBez
78	verwaltungseinheit.is_gemeinde = cast(:isGem AS boolean)	isGem	2	isGem
79	verwaltungseinheit.is_landkreis = cast(:isKreis AS boolean)	isKreis	2	isKreis
80	verwaltungseinheit.is_regbezirk = cast(:isRbez AS boolean)	isRbez	2	isRbez
81	verwaltungseinheit.is_bundesland = cast(:isBundesland AS boolean)	isBundesland	2	isBundesland
82	kta_gruppe.kta_gruppe ~ :anlageBez	anlageBez	0	anlage_text
83	staat.staat ~ :staatBez	staatBez	0	staat_text
84	ortszusatz.ozs_id ~ :ozId	ozId	0	ozId_text
85	ortszusatz.ortszusatz ~:oz	oz	0	oz_text
86	(lada_messwert.messgroesse_id IN ( :messgroesseId ) OR lada_messwert.messgroesse_id IS NULL)	messgroesseId	5	messgroesse
87	ort_uo.ort_id ~ :uOrtId	uOrtId	0	ort_uo_ort_id
88	status_protokoll.datum BETWEEN to_timestamp(cast(:fromStatusDatum AS DOUBLE PRECISION)) AND to_timestamp(cast(:toStatusDatum AS DOUBLE PRECISION))	fromStatusDatum,toStatusDatum	6	StatusDatum
89	ort.mp_art ~ :mpArt	mpArt	0	mpArt
90	stamm.sollist_umwgrp.id IN ( :sollistUmwgrpId )	sollistUmwgrpId	5	sollistUmwgrpId
91	stamm.sollist_mmtgrp.id IN ( :sollistMmtgrpId )	sollistMmtgrpId	5	sollistMmtgrpId
92	probe.mpr_id BETWEEN :mpr_idFrom AND :mpr_idTo	mpr_idFrom,mpr_idTo	1	mpr_id_number
93	sollist_soll.netzbetreiber_id IN ( :netzId )	netzId	4	sollist netzbetreiber_id
94	stamm.sollist_soll.sollist_mmtgrp_id IN ( :sollistMmtgrpId )	sollistMmtgrpId	5	soll sollistMmtgrpId
95	stamm.sollist_soll.sollist_umwgrp_id IN ( :sollistUmwgrpId )	sollistUmwgrpId	5	soll sollistUmwgrpId
96	stamm.sollist_umwgrp_zuord.umw_id IN ( :umwId )	umwId	4	sollist umw_id
\.


--
-- Data for Name: result_type; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.result_type (id, name, format) FROM stdin;
1	text	\N
2	date	d.m.Y H:i
3	number	\N
4	probeId	\N
5	messungId	\N
6	ortId	\N
7	geom	\N
8	mpId	\N
9	land	\N
10	messstelle	\N
11	boolean	\N
12	umwbereich	\N
13	statusstufe	\N
14	statuswert	\N
15	number	e
16	egem	\N
17	datenbasis	\N
18	netzbetr	\N
19	probenart	\N
20	staat	\N
21	probenehmer	\N
22	dsatzerz	\N
23	mprkat	\N
24	statuskombi	\N
25	anlage	\N
26	reiproggrp	\N
27	mpl	\N
28	messRegime	\N
29	number	##.#
30	messgroesse	\N
31	prnId	\N
32	mmtId	\N
33	number	###########
34	landkreis	\N
35	bundesland	\N
36	regbezirk	\N
37	date	d.m.Y
38	tag	\N
39	number	########.########
40	sollistUmwGr	\N
41	sollistMmtGr	\N
42	id	\N
\.


--
-- Data for Name: grid_column; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.grid_column (id, base_query, name, data_index, "position", filter, data_type) FROM stdin;
101	1	interne PID	probeId	1	\N	4
102	1	HP-Nr	hpNr	2	2	1
103	1	Datenbasis	dBasis	3	8	17
104	1	Netz-ID	netzId	4	\N	18
105	1	MST/Labor-ID	mstLaborId	5	\N	10
106	1	Umw-ID	umwId	6	\N	12
107	1	Probenart	pArt	7	9	19
108	1	Solldatum Beginn	sollBegin	8	20	2
109	1	Solldatum Ende	sollEnd	9	21	2
110	1	Probenahme Beginn	peBegin	10	14	2
111	1	Probenahme Ende	peEnd	11	15	2
112	1	Ort-ID	ortId	12	11	1
113	1	E-Gem-ID	eGemId	13	\N	16
114	1	E-Gemeinde	eGem	14	10	16
115	1	externe PID	externeProbeId	15	1	1
116	1	MPR-ID	mprId	16	62	8
117	1	MPL-ID	mplCode	17	\N	23
118	1	Messprogramm-Land	mpl	18	42	27
119	1	Umweltbereich	umw	19	4	12
120	1	Netzbetreiber	netzbetreiber	20	13	18
121	1	MST/Labor	mstLabor	21	3	10
122	1	Anlage	anlage	22	38	25
123	1	Anlage-Beschr	anlagebeschr	23	\N	25
124	1	REI-Prog-GRP	reiproggrp	24	39	26
125	1	REI-Prog-GRP-Beschr	reiproggrpbeschr	25	\N	26
126	1	Test	test	26	5	11
127	1	Messregime	messRegime	27	30	28
128	1	Ort-Kurztext	ortKurztext	28	18	1
129	1	Ort-Langtext	ortLangtext	29	18	1
130	1	Deskriptoren	deskriptoren	30	\N	1
131	1	Medium	medium	31	18	1
132	1	PRN-Bezeichnung	prnBezeichnung	32	\N	1
133	1	PRN	prnId	33	45	31
134	1	PRN-Kurzbezeichnung	prnKurzBezeichnung	34	\N	1
135	1	Orts-Zusatztext	ortszusatztext	35	\N	1
136	1	OZ-ID	ozId	36	\N	1
137	1	Ortszusatz	oz	37	\N	1
138	1	E-LK-ID	eKreisId	38	53	34
139	1	E-BL-ID	eBlId	39	54	35
140	1	E-Staat	eStaat	40	55	20
141	1	Tags	tags	41	59	38
142	1	Mitte Sammelzeitraum	mitteSammelzeitraum	42	51	2
143	1	U-Staat	uStaat	43	58	20
144	1	U-Ort-ID	uOrtId	44	87	1
145	1	Ursprungszeit	uZeit	45	\N	2
1101	11	interne MID	id	1	\N	5
1102	11	interne PID	probeId	2	\N	4
1103	11	HP-Nr	hpNr	3	2	1
1104	11	NP-Nr	npNr	4	22	1
1105	11	Status Datum	statusD	5	88	2
1106	11	Datenbasis	dBasis	6	8	17
1107	11	Netz-ID	netzId	7	\N	18
1108	11	MST/Labor-ID	mstLaborId	8	\N	10
1109	11	Umw-ID	umwId	9	\N	12
1110	11	Probenart	pArt	10	9	19
1111	11	Probenahme Beginn	peBegin	11	14	2
1112	11	Probenahme Ende	peEnd	12	15	2
1113	11	E-Gem-ID	eGemId	13	\N	16
1114	11	E-Gemeinde	eGem	14	10	16
1115	11	H-3	h3	15	\N	1
1116	11	K-40	k40	16	\N	1
1117	11	Co-60	co60	17	\N	1
1118	11	Sr-89	sr89	18	\N	1
1119	11	Sr-90	sr90	19	\N	1
1120	11	Ru-103	ru103	20	\N	1
1121	11	I-131	i131	21	\N	1
1122	11	Cs-134	cs134	22	\N	1
1123	11	Cs-137	cs137	23	\N	1
1124	11	Ce-144	ce144	24	\N	1
1125	11	U-234	u234	25	\N	1
1126	11	U-235	u235	26	\N	1
1127	11	U-238	u238	27	\N	1
1128	11	Pu-238	pu238	28	\N	1
1129	11	Pu-239	pu239	29	\N	1
1130	11	Pu-239/240	pu23940	30	\N	1
1131	11	Te-132	te132	31	\N	1
1132	11	Pb-212	pb212	32	\N	1
1133	11	Pb-214	pb214	33	\N	1
1134	11	Bi-212	bi212	34	\N	1
1135	11	Bi-214	bi214	35	\N	1
1136	11	MMT-ID	mmtId	36	\N	1
1137	11	externe PID	externeProbeId	37	1	1
1138	11	externe MID	externeMessungsId	38	\N	1
1139	11	Status	statusK	39	34	24
1140	11	Umweltbereich	umw	40	4	12
1141	11	Netzbetreiber	netzbetreiber	41	13	18
1142	11	E_GEOM	entnahmeGeom	42	\N	7
1143	11	Anlage	anlage	43	38	25
1144	11	Anlage-Beschr	anlagebeschr	44	\N	25
1145	11	REI-Prog-GRP	reiproggrp	45	39	26
1146	11	REI-Prog-GRP-Beschr	reiproggrpbeschr	46	\N	26
1147	11	MPR-ID	mprId	47	62	8
1148	11	MPL-ID	mplCode	48	\N	23
1149	11	Messprogramm-Land	mpl	49	42	27
1150	11	Test	test	50	5	11
1151	11	Messregime	messRegime	51	30	28
1152	11	MST/Labor	mstLabor	52	3	10
1153	11	geplant	geplant	53	43	11
1154	11	Solldatum Beginn	sollBegin	54	20	2
1155	11	Solldatum Ende	sollEnd	55	21	2
1156	11	Ort-ID	ortId	56	11	1
1157	11	Ort-Kurztext	ortKurztext	57	18	1
1158	11	Ort-Langtext	ortLangtext	58	18	1
1159	11	PRN-Bezeichnung	prnBezeichnung	59	\N	1
1160	11	PRN	prnId	60	45	31
1161	11	PRN-Kurzbezeichnung	prnKurzBezeichnung	61	\N	1
1162	11	MMT	mmt	62	46	32
1163	11	Messbeginn	messbeginn	63	47	2
1164	11	Messdauer (s)	messdauer	64	\N	33
1165	11	Deskriptoren	deskriptoren	65	\N	1
1166	11	Medium	medium	66	18	1
1167	11	OZ-ID	ozId	67	\N	1
1168	11	Ortszusatz	oz	68	\N	1
1169	11	E-LK-ID	eKreisId	69	53	34
1170	11	E-BL-ID	eBlId	70	54	35
1171	11	E-Staat	eStaat	71	55	20
1172	11	Fertig	fertig	72	56	11
1173	11	hat Rückfrage	hatRueckfrage	73	61	11
1174	11	U-Staat	uStaat	74	58	20
1175	11	U-Ort-ID	uOrtId	75	87	1
1176	11	Ursprungszeit	uZeit	76	\N	2
1177	11	Tags	tags	77	59	38
1201	12	interne PID	probeId	1	\N	4
1202	12	HP-Nr	hpNr	2	2	1
1203	12	Datenbasis	dBasis	3	8	17
1204	12	Netz-ID	netzId	4	\N	18
1205	12	MST/Labor-ID	mstLaborId	5	\N	10
1206	12	Umw-ID	umwId	6	\N	12
1207	12	Probenart	pArt	7	9	19
1208	12	Solldatum Beginn	sollBegin	8	20	37
1209	12	Solldatum Ende	sollEnd	9	21	37
1210	12	Probenahme Beginn	peBegin	10	14	2
1211	12	Probenahme Ende	peEnd	11	15	2
1212	12	Ort-ID	ortId	12	11	1
1213	12	E-Gem-ID	eGemId	13	\N	16
1214	12	E-Gemeinde	eGem	14	10	16
1215	12	externe PID	externeProbeId	15	1	1
1216	12	MPR-ID	mprId	16	62	1
1217	12	MPL-ID	mplCode	17	\N	1
1218	12	Messprogramm-Land	mpl	18	42	27
1219	12	Umweltbereich	umw	19	4	12
1220	12	Netzbetreiber	netzbetreiber	20	13	18
1221	12	MST/Labor	mstLabor	21	3	10
1222	12	Anlage	anlage	22	38	25
1223	12	Anlage-Beschr	anlagebeschr	23	\N	25
1224	12	REI-Prog-GRP	reiproggrp	24	39	26
1225	12	REI-Prog-GRP-Beschr	reiproggrpbeschr	25	\N	26
1226	12	Test	test	26	5	11
1227	12	Messregime	messRegime	27	30	28
1228	12	geplant	geplant	28	43	11
1229	12	Messbeginn	messbeginn	29	47	2
1230	12	MMT-ID	mmtId	30	\N	32
1231	12	MMT	mmt	31	46	32
1232	12	PRN-Bezeichnung	prnBezeichnung	32	\N	1
1233	12	Deskriptoren	deskriptoren	33	\N	1
1234	12	Medium	medium	34	18	1
1235	12	Proben Kommentare	pKommentar	35	\N	1
1236	12	PZB	pzs	36	\N	1
1237	12	OZ-ID	ozId	37	\N	1
1238	12	U-Staat	uStaat	38	58	20
1239	12	Status	statusK	39	34	24
1240	12	PRN	prnId	40	45	31
1241	12	PRN-Kurzbezeichnung	prnKurzBezeichnung	41	\N	1
1242	12	interne MID	id	42	\N	1
1243	12	Probemenge	probemenge	43	\N	1
1244	12	Messdauer (s)	messdauer	44	\N	33
1245	12	E-LK-ID	eKreisId	45	53	34
1246	12	E-BL-ID	eBlId	46	54	35
1247	12	E-Staat	eStaat	47	55	20
1248	12	E-RBez-ID	eRbezId	48	57	36
1249	12	externe MID	externeMessungsId	49	\N	1
1250	12	PRN-Betrieb	prnBetrieb	50	\N	1
1251	12	PRN-PLZ	prnPlz	51	\N	1
1252	12	PRN-Ort	prnOrt	52	\N	1
1253	12	PRN-Strasse	prnStrasse	53	\N	1
1254	12	PRN-Telefon	prnTelefon	54	\N	1
1255	12	PRN-Bemerkung	prnBemerkung	55	\N	1
1256	12	MPR-Kommentar	mprKommentar	56	\N	1
1257	12	Ort-Kurztext	ortKurztext	57	18	1
1258	12	Ort-Langtext	ortLangtext	58	18	1
1259	12	E-Landkreis	eKreis	59	\N	16
1260	12	E-Regbezirk	eRbez	60	\N	16
1261	12	MST-Beschr	mstBeschr	61	\N	1
1262	12	Deskriptor S2	desk2Beschr	62	\N	1
1401	14	interne PID	id	1	\N	4
1402	14	externe PID	externeProbeId	2	1	1
1403	14	Netz-ID	netzId	3	\N	18
1404	14	Netzbetreiber	netzbetreiber	4	13	18
1405	14	MST-ID	mstId	5	\N	10
1406	14	Datenbasis-ID	datenbasisId	6	\N	33
1407	14	Test	test	7	5	11
1408	14	Messregime-ID	baId	8	\N	33
1409	14	Probenart	probenartId	9	\N	19
1410	14	Solldatum Beginn	solldatumBeginn	10	20	2
1411	14	Solldatum Ende	solldatumEnde	11	21	2
1412	14	MPR-ID	mprId	12	92	33
1413	14	Deskriptoren	mediaDesk	13	\N	1
1414	14	Umw-ID	umwId	14	\N	12
1415	14	Messungen	mmt	15	\N	1
1416	14	E-Gem-ID	gemId	16	\N	16
1417	14	PRN-ID	probeNehmerId	17	\N	31
1418	14	Tags	tags	18	59	38
1501	15	interne MID	id	1	\N	5
1502	15	interne PID	probeId	2	\N	4
1503	15	HP-Nr	hpNr	3	2	1
1504	15	NP-Nr	npNr	4	22	1
1505	15	Status Datum	statusD	5	88	2
1506	15	Datenbasis	dBasis	6	8	17
1507	15	Netz-ID	netzId	7	\N	18
1508	15	MST/Labor-ID	mstLaborId	8	\N	10
1509	15	Umw-ID	umwId	9	\N	12
1510	15	Probenart	pArt	10	9	19
1511	15	Probenahme Beginn	peBegin	11	14	2
1512	15	Probenahme Ende	peEnd	12	15	2
1513	15	E-Gem-ID	eGemId	13	\N	16
1514	15	E-Gemeinde	eGem	14	10	16
1515	15	H-3	h3	15	\N	1
1516	15	K-40	k40	16	\N	1
1517	15	Co-60	co60	17	\N	1
1518	15	Sr-89	sr89	18	\N	1
1519	15	Sr-90	sr90	19	\N	1
1520	15	Ru-103	ru103	20	\N	1
1521	15	I-131	i131	21	\N	1
1522	15	Cs-134	cs134	22	\N	1
1523	15	Cs-137	cs137	23	\N	1
1524	15	Ce-144	ce144	24	\N	1
1525	15	U-234	u234	25	\N	1
1526	15	U-235	u235	26	\N	1
1527	15	U-238	u238	27	\N	1
1528	15	Pu-238	pu238	28	\N	1
1529	15	Pu-239	pu239	29	\N	1
1530	15	Pu-239/240	pu23940	30	\N	1
1531	15	Te-132	te132	31	\N	1
1532	15	Pb-212	pb212	32	\N	1
1533	15	Pb-214	pb214	33	\N	1
1534	15	Bi-212	bi212	34	\N	1
1535	15	Bi-214	bi214	35	\N	1
1536	15	MMT-ID	mmtId	36	\N	1
1537	15	externe PID	externeProbeId	37	1	1
1538	15	externe MID	externeMessungsId	38	\N	1
1539	15	Status	statusK	39	34	24
1540	15	Umweltbereich	umw	40	4	12
1541	15	Netzbetreiber	netzbetreiber	41	13	18
1542	15	E_GEOM	entnahmeGeom	42	\N	7
1543	15	MPR-ID	mprId	43	62	8
1544	15	MPL-ID	mplCode	44	\N	23
1545	15	Messprogramm-Land	mpl	45	42	27
1546	15	Test	test	46	5	11
1547	15	Messregime	messRegime	47	30	28
1548	15	MST/Labor	mstLabor	48	3	10
1549	15	geplant	geplant	49	43	11
1550	15	Solldatum Beginn	sollBegin	50	20	2
1551	15	Solldatum Ende	sollEnd	51	21	2
1552	15	Ort-ID	ortId	52	11	1
1553	15	Ort-Kurztext	ortKurztext	53	18	1
1554	15	Ort-Langtext	ortLangtext	54	18	1
1555	15	MMT	mmt	55	46	32
1556	15	Messbeginn	messbeginn	56	47	2
1557	15	Messdauer (s)	messdauer	57	\N	33
1558	15	Deskriptoren	deskriptoren	58	18	1
1559	15	Medium	medium	59	18	1
1560	15	OZ-ID	ozId	60	\N	1
1561	15	Ortszusatz	oz	61	\N	1
1562	15	E-LK-ID	eKreisId	62	53	34
1563	15	E-BL-ID	eBlId	63	54	35
1564	15	E-Staat	eStaat	64	55	20
1565	15	Fertig	fertig	65	56	11
1566	15	U-Staat	uStaat	66	58	20
1567	15	U-Ort-ID	uOrtId	67	87	1
1568	15	Ursprungszeit	uZeit	68	\N	2
1569	15	Tags	tags	69	59	38
1570	15	Proben Kommentare	pKommentar	70	\N	1
1571	15	Messung Kommentare	mKommentar	71	\N	1
1572	15	Deskriptor S0	desk0Beschr	72	\N	1
1573	15	Deskriptor S1	desk1Beschr	73	\N	1
1574	15	Deskriptor S2	desk2Beschr	74	\N	1
1575	15	Deskriptor S3	desk3Beschr	75	\N	1
1576	15	Deskriptor S4	desk4Beschr	76	\N	1
2101	21	MPR-ID	mpNr	1	63	8
2102	21	Netz-ID	netzId	2	\N	18
2103	21	MST/Labor-ID	mstLaborId	3	\N	10
2104	21	Datenbasis	dBasis	4	35	17
2105	21	Messregime	messRegime	5	30	28
2106	21	Probenart	pArt	6	36	19
2107	21	Umw-ID	umwId	7	\N	12
2108	21	Deskriptoren	deskriptoren	8	\N	1
2109	21	Probenintervall	intervall	9	\N	1
2110	21	Ort-ID	ortId	10	11	1
2111	21	E-Gem-ID	eGemId	11	\N	16
2112	21	E-Gemeinde	eGem	12	10	16
2113	21	MPL-ID	mplCode	13	\N	23
2114	21	Messprogramm-Land	mpl	14	42	27
2115	21	MST/Labor	mstLabor	15	27	10
2116	21	Aktiv	aktiv	16	31	11
2117	21	Netzbetreiber	netzbetreiber	17	13	18
2118	21	Umweltbereich	umw	18	37	12
2119	21	Anlage	anlage	19	38	25
2120	21	Anlage-Beschr	anlagebeschr	20	\N	25
2121	21	REI-Prog-GRP	reiproggrp	21	39	26
2122	21	REI-Prog-GRP-Beschr	reiproggrpbeschr	22	\N	26
2123	21	Test	test	23	40	11
2124	21	Ort-Kurztext	ortKurztext	24	18	1
2125	21	Ort-Langtext	ortLangtext	25	18	1
2126	21	U-Ort-ID	uOrtId	26	87	1
2127	21	MPR-Kommentar	mprKommentar	27	18	1
2128	21	Probenkommentar	mprPKommentar	28	18	1
2129	21	PRN-Bezeichnung	prnBezeichnung	29	\N	1
2130	21	PRN	prnId	30	45	31
2131	21	PRN-Kurzbezeichnung	prnKurzBezeichnung	31	\N	1
3101	31	ID	id	1	\N	6
3102	31	Netz-ID	netzId	2	\N	18
3103	31	Ort-ID	ortId	3	11	1
3104	31	Ortklassifizierung	ortTyp	4	\N	1
3105	31	Kurztext	kurztext	5	18	1
3106	31	Langtext	langtext	6	18	1
3107	31	Staat	staat	7	55	20
3108	31	Verwaltungseinheit	verwaltungseinheit	8	10	16
3109	31	NUTS-Code	nutsCode	9	\N	1
3110	31	OZ-ID	ozId	10	\N	1
3111	31	Anlage	anlage	11	38	25
3112	31	mpArt	mpArt	12	89	1
3113	31	Koordinatenart	koordinatenArt	13	\N	1
3114	31	X-Koordinate	koordXExtern	14	\N	1
3115	31	Y-Koordinate	koordYExtern	15	\N	1
3116	31	Longitude	longitude	16	\N	39
3117	31	Latitude	latitude	17	\N	39
3118	31	Höhe über NN	hoeheUeberNn	18	\N	39
3119	31	Höhe	hoeheLand	19	\N	39
3120	31	Aktiv (REI)	ortAktiv	20	52	11
3121	31	letzte Änderung	letzteAenderung	21	\N	2
3122	31	Zone	zone	22	\N	1
3123	31	Sektor	sektor	23	\N	1
3124	31	Zustaendigkeit	zustaendigkeit	24	\N	1
3125	31	Berichtstext	berichtstext	25	\N	1
3126	31	unscharf	unscharf	26	\N	11
3127	31	Netzbetreiber	netzbetreiber	27	23	18
3128	31	Anlage-Beschr	anlagebeschr	28	\N	25
3129	31	Verw-ID	verwid	29	\N	16
3201	32	ID	id	1	\N	21
3202	32	Netz-ID	netzId	2	\N	18
3203	32	PRN	prnId	3	41	1
3204	32	Bearbeiter	bearbeiter	4	\N	1
3205	32	Bemerkung	bemerkung	5	\N	1
3206	32	Betrieb	betrieb	6	\N	1
3207	32	Bezeichnung	bezeichnung	7	\N	1
3208	32	Kurzbezeichnung	kurzBezeichnung	8	\N	1
3209	32	Ort	ort	9	\N	1
3210	32	PLZ	plz	10	18	1
3211	32	Strasse	strasse	11	\N	1
3212	32	Telefon	telefon	12	\N	1
3213	32	Tourenplan	tourenplan	13	\N	1
3214	32	Typ	typ	14	\N	1
3215	32	letzte Änderung	letzteAenderung	15	\N	2
3216	32	Netzbetreiber	netzbetreiber	16	24	18
3301	33	ID	id	1	\N	22
3302	33	Netz-ID	netzId	2	\N	18
3303	33	Datensatzerzeuger	datensatzErzeugerId	3	\N	1
3304	33	MST-ID	mstId	4	\N	10
3305	33	Bezeichnung	bezeichnung	5	\N	1
3306	33	letzte Änderung	letzteAenderung	6	\N	2
3307	33	Netzbetreiber	netzbetreiber	7	25	18
3308	33	MST	mst	8	\N	10
3401	34	ID	id	1	\N	23
3402	34	Netz-ID	netzId	2	\N	18
3403	34	Code	code	3	\N	1
3404	34	Bezeichnung	bezeichnung	4	\N	1
3405	34	letzte Änderung	letzteAenderung	5	\N	2
3406	34	Netzbetreiber	netzbetreiber	6	26	18
3501	35	ID	mgrId	1	\N	33
3502	35	Bezeichnung	messgroesse	2	60	1
3503	35	Beschreibung	mgrBeschreibung	3	\N	1
3504	35	IDF Nuklid Key	idfNuklidKey	4	\N	1
3505	35	Leitnuklid	istLeitnuklid	5	\N	11
3506	35	EUDF Nuklid ID	eudfNuklidId	6	\N	33
3507	35	Kennung BVL	kennungBvl	7	\N	1
3601	36	ID	meId	1	\N	33
3602	36	Bezeichnung	me	2	64	1
3603	36	Beschreibung	meBeschreibung	3	\N	1
3604	36	EUDF Messeinheit	eudfMesseinheitId	4	\N	1
3605	36	EUDF Umrechnungsfaktor	umrEudf	5	\N	33
3701	37	ID	mmtId	1	\N	1
3702	37	Bezeichnung	mmt	2	65	1
3703	37	Beschreibung	mmtBeschreibung	3	\N	1
3801	38	ID	mstId	1	\N	1
3802	38	MST	mst	2	\N	10
3803	38	Netz-ID	netzId	3	\N	18
3804	38	Beschreibung	mstBeschreibung	4	\N	1
3805	38	Typ	mstTyp	5	\N	1
3806	38	Amtskennung	mstAmtskennung	6	\N	1
3807	38	Netzbetreiber	netzbetreiber	7	13	18
3901	39	ID	umwId	1	66	1
3902	39	Umweltbereich	umw	2	67	1
3903	39	Masseinheit	me	3	\N	1
3904	39	Masseinheit 2	me2	4	\N	1
3905	39	Beschreibung	umwBeschreibung	5	\N	1
4101	41	interne MwID	id	1	\N	42
4102	41	interne PID	probeId	2	\N	1
4103	41	HP-Nr	hpNr	3	2	1
4104	41	NP-Nr	npNr	4	22	1
4105	41	Status Datum	statusD	5	88	2
4106	41	Datenbasis	dBasis	6	8	17
4107	41	Netz-ID	netzId	7	\N	18
4108	41	MST/Labor-ID	mstLaborId	8	\N	10
4109	41	Umw-ID	umwId	9	\N	12
4110	41	Probenart	pArt	10	9	19
4111	41	Probenahme Beginn	peBegin	11	14	2
4112	41	Probenahme Ende	peEnd	12	15	2
4113	41	E-Gem-ID	eGemId	13	\N	16
4114	41	E-Gemeinde	eGem	14	10	16
4115	41	Messgröße	messgroesse	15	44	30
4116	41	< EG	nwg	16	\N	1
4117	41	Messwert	wert	17	49	15
4118	41	NWG zur Messung	nwgZuMesswert	18	50	15
4119	41	rel. Messunsicherh.(%)	fehler	19	\N	29
4120	41	Maßeinheit	einheit	20	\N	1
4121	41	MMT-ID	mmtId	21	\N	1
4122	41	externe PID	externeProbeId	22	1	1
4123	41	externe MID	externeMessungsId	23	\N	1
4124	41	Status	statusK	24	34	24
4125	41	Umweltbereich	umw	25	4	12
4126	41	Netzbetreiber	netzbetreiber	26	13	18
4127	41	E_GEOM	entnahmeGeom	27	\N	7
4128	41	Anlage	anlage	28	38	25
4129	41	Anlage-Beschr	anlagebeschr	29	\N	25
4130	41	REI-Prog-GRP	reiproggrp	30	39	26
4131	41	REI-Prog-GRP-Beschr	reiproggrpbeschr	31	\N	26
4132	41	MPR-ID	mprId	32	62	1
4133	41	MPL-ID	mplCode	33	\N	1
4134	41	Messprogramm-Land	mpl	34	42	27
4135	41	Test	test	35	5	11
4136	41	Messregime	messRegime	36	30	28
4137	41	MST/Labor	mstLabor	37	3	10
4138	41	geplant	geplant	38	43	11
4139	41	Solldatum Beginn	sollBegin	39	20	2
4140	41	Solldatum Ende	sollEnd	40	21	2
4141	41	PRN-Bezeichnung	prnBezeichnung	41	\N	1
4142	41	PRN	prnId	42	45	31
4143	41	PRN-Kurzbezeichnung	prnKurzBezeichnung	43	\N	1
4144	41	MMT	mmt	44	46	32
4145	41	Messbeginn	messbeginn	45	47	2
4146	41	Messdauer (s)	messdauer	46	\N	33
4147	41	Ort-Kurztext	ortKurztext	47	18	1
4148	41	Ort-Langtext	ortLangtext	48	18	1
4149	41	Deskriptoren	deskriptoren	49	\N	1
4150	41	Medium	medium	50	18	1
4151	41	OZ-ID	ozId	51	\N	1
4152	41	Ortszusatz	oz	52	\N	1
4153	41	E-LK-ID	eKreisId	53	53	34
4154	41	E-BL-ID	eBlId	54	54	35
4155	41	E-Staat	eStaat	55	55	20
4156	41	MW/NWG	wertNwg	56	\N	15
4157	41	Ort-ID	ortId	57	11	1
4158	41	U-Staat	uStaat	58	58	20
4159	41	U-Ort-ID	uOrtId	59	87	1
4160	41	Ursprungszeit	uZeit	60	\N	2
4161	41	Tags	tags	61	59	38
4201	42	interne ID	id	1	\N	42
4202	42	interne PID	probeId	2	\N	1
4203	42	HP-Nr	hpNr	3	2	1
4204	42	NP-Nr	npNr	4	22	1
4205	42	Status Datum	statusD	5	\N	2
4206	42	Datenbasis	dBasis	6	8	17
4207	42	Netz-ID	netzId	7	\N	18
4208	42	MST/Labor-ID	mstLaborId	8	\N	10
4209	42	Umw-ID	umwId	9	\N	12
4210	42	Ort-ID	ortId	10	11	1
4211	42	Probenahme Beginn	peBegin	11	14	2
4212	42	Probenahme Ende	peEnd	12	15	2
4213	42	E-Gem-ID	eGemId	13	\N	16
4214	42	E-Gemeinde	eGem	14	10	16
4215	42	Messgröße	messgroesse	15	86	30
4216	42	< EG	nwg	16	\N	1
4217	42	Messwert	wert	17	\N	15
4218	42	NWG zur Messung	nwgZuMesswert	18	\N	15
4219	42	rel. Messunsicherh.(%)	fehler	19	\N	29
4220	42	Maßeinheit	einheit	20	\N	1
4221	42	MMT-ID	mmtId	21	\N	1
4222	42	externe PID	externeProbeId	22	1	1
4223	42	externe MID	externeMessungsId	23	\N	1
4224	42	Status	statusK	24	34	24
4225	42	Umweltbereich	umw	25	4	12
4226	42	Netzbetreiber	netzbetreiber	26	13	18
4227	42	E_GEOM	entnahmeGeom	27	\N	7
4228	42	Anlage	anlage	28	38	25
4229	42	Anlage-Beschr	anlagebeschr	29	\N	25
4230	42	REI-Prog-GRP	reiproggrp	30	39	26
4231	42	REI-Prog-GRP-Beschr	reiproggrpbeschr	31	\N	26
4232	42	Berichtstext	berichtstext	32	\N	1
4233	42	Test	test	33	5	11
4234	42	Messregime	messRegime	34	30	28
4235	42	MST/Labor	mstLabor	35	3	10
4236	42	geplant	geplant	36	43	11
4237	42	Solldatum Beginn	sollBegin	37	20	2
4238	42	Solldatum Ende	sollEnd	38	21	2
4239	42	MMT	mmt	39	46	32
4240	42	Messbeginn	messbeginn	40	47	2
4241	42	Messdauer (s)	messdauer	41	\N	33
4242	42	Ort-Kurztext	ortKurztext	42	18	1
4243	42	Ort-Langtext	ortLangtext	43	18	1
4244	42	Medium	medium	44	18	1
4245	42	Mitte Sammelzeitraum	mitteSammelzeitraum	45	51	2
4246	42	Quartal Sammelzeitraum	qmitteSammelzeitraum	46	\N	1
4247	42	Jahr Sammelzeitraum	jmitteSammelzeitraum	47	\N	1
4248	42	PZB (Niederschlagshöhe)	pzs	48	\N	1
4249	42	Proben Kommentare	pKommentar	49	\N	1
4250	42	Messung Kommentare	mKommentar	50	\N	1
4251	42	Tags	tags	51	59	38
4252	42	Messstellen Adr.	messstellenadr	52	\N	1
4253	42	Messgröße-ID	mgrId	53	\N	33
4254	42	Messlabor Adr.	messlaboradr	54	\N	1
4255	42	MLabor-ID	laborMstId	55	\N	10
4256	42	MST-ID	MstId	56	\N	10
5101	51	Umw-ID	umwId	1	68	1
5102	51	Umweltbereich	umw	2	67	1
5103	51	S0	s00Sn	3	\N	33
5104	51	S0-Beschr	S00Beschr	4	\N	1
5105	51	S1	s01Sn	5	\N	33
5106	51	S1-Beschr	S01Beschr	6	\N	1
5107	51	S2	s02Sn	7	\N	33
5108	51	S2-Beschr	S02Beschr	8	\N	1
5109	51	S3	s03Sn	9	\N	33
5110	51	S3-Beschr	S03Beschr	10	\N	1
5111	51	S4	s04Sn	11	\N	33
5112	51	S4-Beschr	S04Beschr	12	\N	1
5113	51	S5	s05Sn	13	\N	33
5114	51	S5-Beschr	S05Beschr	14	\N	1
5115	51	S6	s06Sn	15	\N	33
5116	51	S6-Beschr	S06Beschr	16	\N	1
5117	51	S7	s07Sn	17	\N	33
5118	51	S7-Beschr	S07Beschr	18	\N	1
5119	51	S8	s08Sn	19	\N	33
5120	51	S8-Beschr	S08Beschr	20	\N	1
5121	51	S9	s09Sn	21	\N	33
5122	51	S9-Beschr	S09Beschr	22	\N	1
5123	51	S10	s10Sn	23	\N	33
5124	51	S10-Beschr	S10Beschr	24	\N	1
5125	51	S11	s11Sn	25	\N	33
5126	51	S11-Beschr	S11Beschr	26	\N	1
5201	52	ID	deskId	1	69	1
5202	52	Ebene	deskEbene	2	70	1
5203	52	Sn	deskSn	3	72	1
5204	52	Beschreibung	deskBeschr	4	75	1
5205	52	Bedeutung	deskBedeutung	5	\N	1
5206	52	Vorgänger-ID	deskVorgId	6	71	1
5207	52	Vorgänger-Ebene	deskVorgEbene	7	73	1
5208	52	Vorgänger-Sn	deskVorgSn	8	74	1
5209	52	Vorgänger-Beschreibung	deskVorgBeschr	9	\N	1
5301	53	ID	verwId	1	76	1
5302	53	Bezeichnung	verwBez	2	77	1
5303	53	PLZ	plz	3	\N	1
5304	53	ist_Gemeinde	isGem	4	78	11
5305	53	ist_Landkreis	isKreis	5	79	11
5306	53	ist_Reg.bezirk	isRbez	6	80	11
5307	53	ist_Bundesland	isBundesland	7	81	11
5308	53	NUTS	nuts	8	\N	1
5309	53	Bundesland-ID	bundeslandId	9	\N	1
5310	53	Bundesland	bundesland	10	\N	1
5311	53	Reg.bezirk-ID	rbezId	11	\N	1
5312	53	Reg.bezirk	rbez	12	\N	1
5313	53	Landkreis-ID	kreisId	13	\N	1
5314	53	Landkreis	kreis	14	\N	1
5401	54	ID	anlageId	1	\N	33
5402	54	Anlage	anlageBez	2	82	1
5403	54	Beschreibung	anlageBeschr	3	\N	1
5501	55	ID	staatId	1	\N	1
5502	55	HKL-ID	hklId	2	\N	1
5503	55	Staat	staatBez	3	83	1
5504	55	Autokennzeichen	staatKurz	4	\N	1
5505	55	ISO	staatIso	5	\N	1
5506	55	EU	staatEu	6	\N	11
5507	55	Koordinatenart	kda	7	\N	1
5508	55	X-Koordinate	staatKoordX	8	\N	1
5509	55	Y-Koordinate	staatKoordY	9	\N	1
5601	56	OZ-ID	ozId	1	84	1
5602	56	Ortszusatz	oz	2	85	1
10101	101	externe PID	externeProbeId	1	1	1
10102	101	Datenbasis	dBasis	2	8	17
10103	101	MMT-ID	mmtId	3	\N	1
10104	101	externe MID	externeMessungsId	4	\N	1
10105	101	Proben Kommentar	pKommentar	5	\N	1
10106	101	MPR Kommentar	mprKommentar	6	\N	1
10107	101	Messregime	messRegime	7	\N	28
10108	101	S00	s00	8	\N	1
10109	101	S01	s01	9	\N	1
10110	101	S02	s02	10	\N	1
10111	101	S03	s03	11	\N	1
10112	101	S04	s04	12	\N	1
10113	101	S05	s05	13	\N	1
10114	101	S06	s06	14	\N	1
10115	101	S07	s07	15	\N	1
10116	101	S08	s08	16	\N	1
10117	101	S09	s09	17	\N	1
10118	101	S10	s10	18	\N	1
10119	101	S11	s11	19	\N	1
10120	101	S12	s12	20	\N	1
10121	101	Umweltbereich	umw	21	\N	12
10122	101	X-Koordinate	koord_x_extern	22	\N	1
10123	101	Y-Koordinate	koord_y_extern	23	\N	1
10124	101	MPL-ID	mplCode	24	\N	1
10125	101	HKL-ID	hklId	25	\N	1
10126	101	U-Gem-ID	uGemId	26	\N	1
10127	101	E-Gem-ID	eGemId	27	\N	1
10128	101	OZS-ID	ozId	28	\N	1
10129	101	Solldatum Beginn	sollBegin	29	20	2
10130	101	Solldatum Ende	sollEnd	30	21	2
10131	101	Probenart	pArt	31	\N	1
10132	101	PRN-ID	prnId	32	\N	1
10133	101	MST/Labor-ID	mstLaborId	33	\N	10
10134	101	REI-Prog-GRP	reiproggrp	34	\N	1
10135	101	Ort-ID	ortId	35	\N	1
10136	101	Landkreis	landkreis	36	\N	1
10137	101	MPR-ID	mprId	37	62	1
10138	101	Medium	medium	38	\N	1
10139	101	Probemenge	probemenge	39	\N	1
10140	101	PRN-Bezeichnung	prnBezeichnung	40	\N	1
10141	101	E-Gemeinde	egem	41	\N	1
10142	101	Messprogramm-Land	mpl	42	42	27
10143	101	Koord-Art	kdaId	43	\N	1
10201	102	interne MID	id	1	\N	5
10202	102	interne PID	probeId	2	\N	4
10203	102	HP-Nr	hpNr	3	2	1
10204	102	NP-Nr	npNr	4	22	1
10205	102	Status Datum	statusD	5	88	2
10206	102	Datenbasis	dBasis	6	8	17
10207	102	Netz-ID	netzId	7	\N	18
10208	102	MST/Labor-ID	mstLaborId	8	\N	10
10209	102	Umw-ID	umwId	9	\N	12
10210	102	Probenart	pArt	10	9	19
10211	102	Probenahme Beginn	peBegin	11	14	2
10212	102	Probenahme Ende	peEnd	12	15	2
10213	102	K-40	k40	13	\N	1
10214	102	I-131	i131	14	\N	1
10215	102	Cs-134	cs134	15	\N	1
10216	102	Cs-137	cs137	16	\N	1
10217	102	Be-7	be7	17	\N	1
10218	102	Na-22	na22	18	\N	1
10219	102	Pb-210	pb210	19	\N	1
10220	102	Am-241	am241	20	\N	1
10221	102	MMT-ID	mmtId	21	\N	1
10222	102	externe PID	externeProbeId	22	1	1
10223	102	externe MID	externeMessungsId	23	\N	1
10224	102	Status	statusK	24	34	24
10225	102	Umweltbereich	umw	25	4	12
10226	102	Netzbetreiber	netzbetreiber	26	13	18
10227	102	E_GEOM	entnahmeGeom	27	\N	7
10228	102	Test	test	28	5	11
10229	102	Messregime	messRegime	29	30	28
10230	102	MST/Labor	mstLabor	30	3	10
10231	102	Ort-ID	ortId	31	11	1
10232	102	Ort-Kurztext	ortKurztext	32	18	1
10233	102	Ort-Langtext	ortLangtext	33	18	1
10234	102	MMT	mmt	34	46	32
10235	102	hat Rückfrage	hatRueckfrage	35	61	11
10236	102	K-40 NWG zur Messung	k407nwgZuMesswert	36	\N	15
10237	102	K-40 rel. Messunsicherh.(%)	k40fehler	37	\N	29
10238	102	I-131 NWG zur Messung	i131nwgZuMesswert	38	\N	15
10239	102	I-131 rel. Messunsicherh.(%)	i131fehler	39	\N	29
10240	102	Cs-134 NWG zur Messung	c134nwgZuMesswert	40	\N	15
10241	102	Cs-134 rel. Messunsicherh.(%)	c134fehler	41	\N	29
10242	102	Cs-137 NWG zur Messung	c137nwgZuMesswert	42	\N	15
10243	102	Cs-137 rel. Messunsicherh.(%)	c137fehler	43	\N	29
10244	102	Be-7 NWG zur Messung	be7nwgZuMesswert	44	\N	15
10245	102	Be-7 rel. Messunsicherh.(%)	be7fehler	45	\N	29
10246	102	Na-22 NWG zur Messung	na22nwgZuMesswert	46	\N	15
10247	102	Na-22 rel. Messunsicherh.(%)	na22fehler	47	\N	29
10248	102	Pb-210 NWG zur Messung	pb210nwgZuMesswert	48	\N	15
10249	102	Pb-210 rel. Messunsicherh.(%)	pb210fehler	49	\N	29
10250	102	Am-241 NWG zur Messung	am241nwgZuMesswert	50	\N	15
10251	102	Am-241 rel. Messunsicherh.(%)	am241fehler	51	\N	29
10301	103	interne MID	id	1	\N	5
10302	103	interne PID	probeId	2	\N	4
10303	103	HP-Nr	hpNr	3	2	1
10304	103	NP-Nr	npNr	4	22	1
10305	103	Status Datum	statusD	5	88	2
10306	103	Datenbasis	dBasis	6	8	17
10307	103	Netz-ID	netzId	7	\N	18
10308	103	MST/Labor-ID	mstLaborId	8	\N	10
10309	103	Umw-ID	umwId	9	\N	12
10310	103	Probenart	pArt	10	9	19
10311	103	Probenahme Beginn	peBegin	11	14	2
10312	103	Probenahme Ende	peEnd	12	15	2
10313	103	E-Gem-ID	eGemId	13	\N	16
10314	103	E-Gemeinde	eGem	14	10	16
10315	103	H-3	h3	15	\N	1
10316	103	K-40	k40	16	\N	1
10317	103	Co-60	co60	17	\N	1
10318	103	Sr-89	sr89	18	\N	1
10319	103	Sr-90	sr90	19	\N	1
10320	103	I-131	i131	20	\N	1
10321	103	Cs-134	cs134	21	\N	1
10322	103	Cs-137	cs137	22	\N	1
10323	103	U-234	u234	23	\N	1
10324	103	U-235	u235	24	\N	1
10325	103	U-238	u238	25	\N	1
10326	103	Pu-238	pu238	26	\N	1
10327	103	Pu-239	pu239	27	\N	1
10328	103	Pu-239/240	pu23940	28	\N	1
10329	103	Am-241	am241	29	\N	1
10330	103	Cm-242	cm242	30	\N	1
10331	103	Cm-244	cm244	31	\N	1
10332	103	Cm-243/244	cm243244	32	\N	1
10333	103	C-14	c14	33	\N	1
10334	103	Mn-54	mn54	34	\N	1
10335	103	Gamma-OD-Brutto	gammaodbrutto	35	\N	1
10336	103	Gamma-ODL-Brutto	gammaodlbrutto	36	\N	1
10337	103	Gamma-ODL-min	gammaodlmin	37	\N	1
10338	103	Gamma-ODL-max	gammaodlmax	38	\N	1
10339	103	Neutr-OD-Brutto	neutrodbrutto	39	\N	1
10340	103	Neutr-ODL-Brutto	neutrodlbrutto	40	\N	1
10341	103	Neutr-ODL-min	neutrodlmin	41	\N	1
10342	103	Neutr-ODL-max	neutrodlmax	42	\N	1
10343	103	MMT-ID	mmtId	43	\N	1
10344	103	externe PID	externeProbeId	44	1	1
10345	103	externe MID	externeMessungsId	45	\N	1
10346	103	Status	statusK	46	34	24
10347	103	Umweltbereich	umw	47	4	12
10348	103	Netzbetreiber	netzbetreiber	48	13	18
10349	103	E_GEOM	entnahmeGeom	49	\N	7
10350	103	Anlage	anlage	50	38	25
10351	103	Anlage-Beschr	anlagebeschr	51	\N	25
10352	103	REI-Prog-GRP	reiproggrp	52	39	26
10353	103	REI-Prog-GRP-Beschr	reiproggrpbeschr	53	\N	26
10354	103	Test	test	54	5	11
10355	103	Messregime	messRegime	55	30	28
10356	103	MST/Labor	mstLabor	56	3	10
10357	103	Solldatum Beginn	sollBegin	57	20	2
10358	103	Solldatum Ende	sollEnd	58	21	2
10359	103	Ort-ID	ortId	59	11	1
10360	103	Ort-Kurztext	ortKurztext	60	18	1
10361	103	Ort-Langtext	ortLangtext	61	18	1
10362	103	MMT	mmt	62	46	32
10363	103	Messbeginn	messbeginn	63	47	2
10364	103	Tags	tags	64	59	38
10365	103	Proben Kommentare	pKommentar	65	\N	1
10366	103	Messung Kommentare	mKommentar	66	\N	1
10367	103	MPR-ID	mprId	67	62	8
10401	104	interne MID	id	1	\N	5
10402	104	interne PID	probeId	2	\N	4
10403	104	HP-Nr	hpNr	3	2	1
10404	104	NP-Nr	npNr	4	22	1
10405	104	Datenbasis	dBasis	5	8	17
10406	104	Netz-ID	netzId	6	\N	18
10407	104	MST/Labor-ID	mstLaborId	7	\N	10
10408	104	Umw-ID	umwId	8	\N	12
10409	104	Probenart	pArt	9	9	19
10410	104	Probenahme Beginn	peBegin	10	14	2
10411	104	Probenahme Ende	peEnd	11	15	2
10412	104	MMT-ID	mmtId	12	\N	1
10413	104	externe PID	externeProbeId	13	1	1
10414	104	externe MID	externeMessungsId	14	\N	1
10415	104	Status	statusK	15	34	24
10416	104	Umweltbereich	umw	16	4	12
10417	104	Netzbetreiber	netzbetreiber	17	13	18
10418	104	E_GEOM	entnahmeGeom	18	\N	7
10419	104	Test	test	19	5	11
10420	104	Messregime	messRegime	20	30	28
10421	104	MST/Labor	mstLabor	21	3	10
10422	104	Ort-ID	ortId	22	11	1
10423	104	MMT	mmt	23	46	32
10424	104	Pu-238 < EG	pu238nwg	24	\N	1
10425	104	Pu-238 Messwert	pu238wert	25	\N	15
10426	104	Pu-238 NWG zur Messung	pu238nwgZuMesswert	26	\N	15
10427	104	Pu-238 Maßeinheit	pu238einheit	27	\N	1
10428	104	Pu-238 rel. Messunsicherh.(%)	pu238fehler	28	\N	29
10429	104	Pu-239/240 < EG	pu23940nwg	29	\N	1
10430	104	Pu-239/240 Messwert	pu23940wert	30	\N	15
10431	104	Pu-239/240 NWG zur Messung	pu23940nwgZuMesswert	31	\N	15
10432	104	Pu-239/240 Maßeinheit	pu23940einheit	32	\N	1
10433	104	Pu-239/240 rel. Messunsicherh.(%)	pu23940fehler	33	\N	29
10434	104	Am-241 < EG	am241nwg	34	\N	1
10435	104	Am-241 Messwert	am241wert	35	\N	15
10436	104	Am-241 NWG zur Messung	am241nwgZuMesswert	36	\N	15
10437	104	Am-241 Maßeinheit	am241einheit	37	\N	1
10438	104	Am-241 rel. Messunsicherh.(%)	am241fehler	38	\N	29
10439	104	Cm-244 < EG	cm244nwg	39	\N	1
10440	104	Cm-244 Messwert	cm244wert	40	\N	15
10441	104	Cm-244 NWG zur Messung	cm244nwgZuMesswert	41	\N	15
10442	104	Cm-244 Maßeinheit	cm244einheit	42	\N	1
10443	104	Cm-244 rel. Messunsicherh.(%)	cm244fehler	43	\N	29
10444	104	PZS A07 (m)	pzsA07wert	44	\N	1
10445	104	PZS A11 (m)	pzsA11wert	45	\N	1
\.


--
-- Data for Name: query_user; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.query_user (id, name, user_id, base_query, description) FROM stdin;
1	Proben	0	1	Vorlage für Probenselektion
2	Messungen	0	11	Vorlage für Messungsselektion\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
3	Messprogramme	0	21	Vorlage für Messprogrammselektion
4	Orte	0	31	Abfrage der Orte
5	Probenehmer	0	32	Abfrage der Probenehmer
6	Datensatzerzeuger	0	33	Abfrage der Datensatzerzeuger
7	Messprogrammkategorie	0	34	Abfrage der Messprogrammkategorien
8	PEP_BB	1	101	PEP-Proben für LIMS BB\n  Feste Bedingungen:\n  Netzbetreiber: 12 Brandenburg\n  Proben Kommentar: 'o_%' oder 'n_%'
9	Messwerte	0	41	Abfrage beliebiger Messwerte\nEs werden nur Messungen mit Messstellen-Status angezeigt!
10	Probenplanung/PEP	0	12	Vorlage für Selektion Probenplanung/PEP\nDie Abfrage enthält Spalten aus Probe, Messung und Messprogramm und dient der Erstellung von Übersichten und Unterlagen für Probenahme und Messung.
11	REI-Berichte	0	42	Abfrage für die Erstellung von REI-Berichten\nEs werden nur Messwerte mit Messstellen-Status in der jeweiligen Spalte angezeigt!
12	StammBund Messgröße	0	35	Abfrage Messgröße
13	StammBund Maßeinheit	0	36	Abfrage Maßeinheit
14	StammBund Messmethode	0	37	Abfrage Messmethode
15	StammBund Messstelle	0	38	Abfrage Messstelle
16	StammBund Umwelt	0	39	Abfrage Umwelt; Filter sind Textfilter
17	StammBund Umweltdeskriptoren	0	51	Abfrage Umweltdeskriptoren; Filter sind Textfilter
18	StammBund Deskriptoren	1	52	Abfrage Deskriptoren; Filter sind Textfilter
19	StammBund Verwaltungseinheit	0	53	Abfrage Verwaltungseinheit
20	StammBund Anlage	0	54	Abfrage Anlage (KTA-Gruppe)
21	StammBund Staat	0	55	Abfrage Staat
22	StammBund Ortszusatz	0	56	Abfrage Ortszusatz
24	Messungen RN6	1	102	Abfrage Messungen, abgestimmt auf Anforderungen RN6.\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
25	Messungen BY REI	1	103	Abfrage Messungen, abgestimmt auf Anforderungen Bayern REI.\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
26	Messungen BSH	1	104	Abfrage Messungen, abgestimmt auf Anforderungen BSH.\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
27	Ergebnis PEP-Generierung	0	14	Die Abfrage bildet die Anzeige der PEP-Proben nach ihrer Generierung aus einem Messprogramm nach. Die Formatierung der Ergebnistabelle ist auf den Export der Daten abgestimmt und weicht deshalb teilweise vom Standard in LADA ab, insbesondere bei der Auswahl und Benennung der Spalten.
31	Messungen mit Deskriptoren §162	1	15	Vorlage für Messungesselektion mit Deskriptoren, §162\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
\.


--
-- Data for Name: query_messstelle; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.query_messstelle (id, query, mess_stelle) FROM stdin;
1	8	12032
6	18	30021
7	18	30022
10	24	30021
11	24	30022
12	25	30021
13	25	30022
14	26	30021
15	26	30022
16	24	20120
17	25	09192
27	31	30021
28	31	30022
\.

--2	10	30022
--3	10	06010
--4	10	08010
--5	10	30021


--
-- Data for Name: grid_column_values; Type: TABLE DATA; Schema: stamm; Owner: postgres
--

COPY stamm.grid_column_values (id, user_id, grid_column, query_user, sort, sort_index, filter_value, filter_active, visible, column_index, width) FROM stdin;
8	0	103	1	\N	\N	\N	f	t	1	77
11	0	102	1	\N	\N	\N	f	t	0	92
19	0	118	1	\N	\N	\N	f	f	-1	150
22	0	117	1	\N	\N	\N	f	f	-1	55
28	0	128	1	\N	\N	\N	f	f	\N	200
40	0	1128	2	\N	\N	\N	f	f	-1	75
46	0	1123	2	\N	\N	\N	f	f	-1	75
63	0	1135	2	\N	\N	\N	f	f	-1	75
71	0	1104	2	\N	\N	\N	f	t	1	54
75	0	1152	2	\N	\N	\N	f	f	-1	164
107	0	2113	3	\N	\N	\N	f	f	-1	55
114	0	2120	3	\N	\N	\N	f	f	\N	120
129	0	3110	4	\N	\N	\N	f	f	-1	56
132	0	3103	4	\N	\N	\N	t	t	1	120
135	0	3113	4	\N	\N	\N	f	f	-1	120
146	0	3109	4	\N	\N	\N	f	f	-1	90
91	0	3306	6	\N	\N	\N	f	f	-1	125
94	0	3307	6	\N	\N	\N	t	f	-1	120
95	0	3308	6	\N	\N	\N	f	f	-1	164
123	0	3406	7	\N	\N	\N	t	f	-1	120
33	0	1110	2	\N	\N	\N	f	t	8	69
36	0	1146	2	\N	\N	\N	f	f	-1	200
43	0	1124	2	\N	\N	\N	f	f	-1	75
59	0	1121	2	\N	\N	\N	f	f	-1	75
72	0	1119	2	\N	\N	\N	f	f	-1	75
79	0	1140	2	\N	\N	\N	t	t	7	150
82	0	1105	2	\N	\N	\N	f	f	-1	125
84	0	1157	2	\N	\N	\N	f	f	\N	200
97	0	2102	3	\N	\N	\N	f	f	-1	52
101	0	2106	3	\N	\N	\N	f	t	6	69
104	0	2109	3	\N	\N	\N	f	t	9	102
111	0	2115	3	\N	\N	\N	t	t	3	164
137	0	3101	4	\N	\N	\N	f	f	-1	92
143	0	3106	4	\N	\N	\N	f	t	4	200
150	0	3116	4	\N	\N	\N	f	f	-1	75
154	0	3201	5	\N	\N	\N	f	f	-1	92
157	0	3204	5	\N	\N	\N	f	f	-1	120
160	0	3207	5	\N	\N	\N	f	t	2	300
163	0	3210	5	\N	\N	\N	f	f	-1	57
166	0	3213	5	\N	\N	\N	f	f	-1	50
169	0	3216	5	\N	\N	\N	t	f	-1	120
88	0	3301	6	\N	\N	\N	f	f	-1	92
122	0	3405	7	\N	\N	\N	f	f	-1	125
9	0	112	1	\N	\N	\N	f	f	-1	120
12	0	105	1	\N	\N	\N	f	t	3	92
20	0	101	1	\N	\N	\N	f	f	-1	92
26	0	110	1	\N	\N	\N	f	t	10	125
212	1	10141	8	\N	\N	\N	f	t	41	150
216	1	10132	8	\N	\N	\N	f	t	32	52
218	1	10136	8	\N	\N	\N	f	t	36	188
227	1	10123	8	\N	\N	\N	f	t	24	125
232	1	10111	8	\N	\N	\N	f	t	11	40
240	1	10112	8	\N	\N	\N	f	t	12	40
245	1	10131	8	\N	\N	\N	f	t	31	69
250	1	10101	8	asc	\N	\N	t	t	0	145
177	0	4123	9	\N	\N	\N	f	t	1	84
182	0	4103	9	\N	\N	\N	f	t	2	92
198	0	4112	9	\N	\N	\N	f	t	6	125
201	0	4139	9	\N	\N	\N	f	f	-1	125
204	0	4124	9	\N	\N	\N	t	t	10	145
258	0	1213	10	\N	\N	\N	f	f	\N	70
264	0	1227	10	\N	\N	\N	f	f	\N	150
267	0	1218	10	\N	\N	\N	f	f	\N	150
270	0	1217	10	\N	\N	\N	f	t	15	55
275	0	1212	10	\N	\N	\N	f	t	11	120
280	0	1240	10	\N	\N	\N	f	t	20	46
293	0	1226	10	\N	\N	true	t	f	\N	40
296	0	1238	10	\N	\N	\N	f	t	13	100
210	1	10116	8	\N	\N	\N	f	t	16	40
213	1	10138	8	\N	\N	\N	f	t	38	200
215	1	10102	8	\N	\N	\N	t	t	1	77
217	1	10135	8	\N	\N	\N	f	t	35	120
219	1	10134	8	\N	\N	\N	f	t	34	69
220	1	10108	8	\N	\N	\N	f	t	8	40
222	1	10120	8	\N	\N	\N	f	t	20	40
223	1	10118	8	\N	\N	\N	f	t	18	40
224	1	10133	8	\N	\N	\N	f	t	33	92
225	1	10125	8	\N	\N	\N	f	t	25	52
226	1	10114	8	\N	\N	\N	f	t	14	40
228	1	10119	8	\N	\N	\N	f	t	19	40
229	1	10130	8	\N	\N	\N	t	t	30	125
230	1	10105	8	\N	\N	\N	f	t	4	128
231	1	10142	8	\N	\N	\N	t	f	-1	150
233	1	10127	8	\N	\N	\N	f	t	27	70
235	1	10121	8	\N	\N	\N	f	t	21	150
236	1	10122	8	\N	\N	\N	f	t	23	125
237	1	10107	8	\N	\N	\N	f	t	6	150
239	1	10124	8	\N	\N	\N	f	t	7	55
242	1	10110	8	\N	\N	\N	f	t	10	40
243	1	10109	8	\N	\N	\N	f	t	9	40
244	1	10140	8	\N	\N	\N	f	t	40	250
246	1	10137	8	\N	\N	\N	f	t	37	55
247	1	10139	8	\N	\N	\N	f	t	39	80
249	1	10106	8	\N	\N	\N	f	t	5	112
251	1	10117	8	\N	\N	\N	f	t	17	40
252	1	10104	8	\N	\N	\N	f	t	3	81
178	0	4119	9	\N	\N	\N	f	t	17	141
179	0	4122	9	\N	\N	\N	f	t	0	145
180	0	4138	9	\N	\N	\N	f	f	-1	70
181	0	4101	9	\N	\N	\N	f	f	-1	92
183	0	4136	9	\N	\N	\N	f	f	-1	150
184	0	4121	9	\N	\N	\N	f	t	11	53
185	0	4134	9	\N	\N	\N	f	f	-1	150
187	0	4132	9	\N	\N	\N	f	f	-1	75
188	0	4137	9	\N	\N	\N	f	t	4	154
190	0	4107	9	\N	\N	\N	f	f	-1	52
191	0	4126	9	\N	\N	\N	t	f	-1	120
193	0	4110	9	\N	\N	\N	f	f	-1	69
194	0	4116	9	\N	\N	\N	f	t	13	40
196	0	4130	9	\N	\N	\N	f	f	-1	69
197	0	4111	9	\N	\N	\N	t	t	5	125
199	0	4102	9	\N	\N	\N	f	f	-1	92
200	0	4131	9	\N	\N	\N	f	f	-1	200
203	0	4105	9	\N	\N	\N	f	f	-1	125
211	1	10103	8	\N	\N	\N	f	t	2	53
214	1	10143	8	\N	\N	\N	f	t	22	80
221	1	10126	8	\N	\N	\N	f	t	26	70
234	1	10128	8	\N	\N	\N	f	t	28	80
238	1	10129	8	\N	\N	\N	t	t	29	125
241	1	10115	8	\N	\N	\N	f	t	15	40
248	1	10113	8	\N	\N	\N	f	t	13	40
186	0	4133	9	\N	\N	\N	f	f	-1	55
189	0	4108	9	\N	\N	\N	f	f	-1	92
192	0	4104	9	\N	\N	\N	f	t	3	53
195	0	4118	9	\N	\N	\N	f	t	15	120
202	0	4140	9	\N	\N	\N	f	f	-1	125
205	0	4135	9	\N	\N	true	t	f	-1	40
208	0	4125	9	\N	\N	\N	t	t	20	150
253	0	1222	10	\N	\N	\N	f	t	10	70
261	0	1202	10	\N	\N	\N	f	t	3	92
284	0	1241	10	\N	\N	\N	f	f	\N	125
287	0	1224	10	\N	\N	\N	f	t	14	69
290	0	1208	10	\N	\N	\N	t	t	6	125
1	0	122	1	\N	\N	\N	f	t	5	70
2	0	116	1	\N	\N	\N	f	f	-1	55
3	0	115	1	\N	\N	\N	f	f	-1	145
4	0	124	1	\N	\N	\N	f	f	-1	69
5	0	125	1	\N	\N	\N	f	f	-1	200
6	0	127	1	\N	\N	\N	f	f	-1	150
7	0	120	1	\N	\N	\N	t	t	2	120
10	0	109	1	\N	\N	\N	f	f	-1	125
13	0	123	1	\N	\N	\N	f	t	6	120
14	0	106	1	\N	\N	\N	f	t	7	56
15	0	113	1	\N	\N	\N	f	f	-1	70
16	0	104	1	\N	\N	\N	f	f	-1	52
17	0	108	1	\N	\N	\N	f	f	-1	125
18	0	114	1	\N	\N	\N	f	f	-1	150
21	0	126	1	\N	\N	true	t	f	-1	40
23	0	107	1	\N	\N	\N	f	t	9	69
24	0	111	1	\N	\N	\N	f	t	11	125
25	0	119	1	\N	\N	\N	t	t	8	150
27	0	121	1	\N	\N	\N	t	t	4	164
29	0	129	1	\N	\N	\N	f	f	\N	200
30	0	1109	2	\N	\N	\N	f	t	6	56
31	0	1116	2	\N	\N	\N	f	t	14	75
32	0	1131	2	\N	\N	\N	f	f	-1	75
34	0	1145	2	\N	\N	\N	f	f	-1	69
35	0	1112	2	\N	\N	\N	f	t	10	125
37	0	1102	2	\N	\N	\N	f	f	-1	92
38	0	1137	2	\N	\N	\N	f	f	-1	145
39	0	1143	2	\N	\N	\N	f	f	-1	70
41	0	1147	2	\N	\N	\N	f	f	-1	55
42	0	1134	2	\N	\N	\N	f	f	-1	75
44	0	1138	2	\N	\N	\N	f	f	-1	81
45	0	1141	2	\N	\N	\N	t	t	5	120
47	0	1117	2	\N	\N	\N	f	f	-1	75
48	0	1126	2	\N	\N	\N	f	f	-1	75
49	0	1115	2	\N	\N	\N	f	t	13	75
50	0	1120	2	\N	\N	\N	f	f	-1	75
51	0	1133	2	\N	\N	\N	f	f	-1	75
52	0	1127	2	\N	\N	\N	f	f	-1	75
53	0	1113	2	\N	\N	\N	f	t	11	70
54	0	1144	2	\N	\N	\N	f	f	-1	120
55	0	1136	2	\N	\N	\N	f	t	2	53
56	0	1148	2	\N	\N	\N	f	f	-1	55
57	0	1129	2	\N	\N	\N	f	f	-1	75
58	0	1149	2	\N	\N	\N	f	f	-1	150
60	0	1108	2	\N	\N	\N	f	f	-1	92
61	0	1139	2	\N	\N	\N	t	t	3	145
62	0	1103	2	\N	\N	\N	f	t	0	92
64	0	1107	2	\N	\N	\N	f	f	-1	52
65	0	1106	2	\N	\N	\N	f	t	4	77
66	0	1101	2	\N	\N	\N	f	f	-1	92
67	0	1150	2	\N	\N	true	t	f	-1	40
68	0	1122	2	\N	\N	\N	f	f	-1	75
69	0	1114	2	\N	\N	\N	f	t	12	150
70	0	1142	2	\N	\N	\N	f	f	-1	76
73	0	1130	2	\N	\N	\N	f	f	-1	75
74	0	1111	2	\N	\N	\N	t	t	9	125
76	0	1132	2	\N	\N	\N	f	f	-1	75
77	0	1125	2	\N	\N	\N	f	f	-1	75
78	0	1151	2	\N	\N	\N	f	f	-1	150
80	0	1118	2	\N	\N	\N	f	f	-1	75
81	0	1153	2	\N	\N	\N	f	f	\N	70
83	0	1156	2	\N	\N	\N	f	f	\N	120
85	0	1158	2	\N	\N	\N	f	f	\N	200
86	0	1154	2	\N	\N	\N	f	f	\N	125
87	0	1155	2	\N	\N	\N	f	f	\N	125
96	0	2101	3	\N	\N	\N	f	t	0	55
98	0	2105	3	\N	\N	\N	f	t	5	150
99	0	2103	3	\N	\N	\N	f	t	2	92
100	0	2107	3	\N	\N	\N	f	t	7	56
102	0	2104	3	\N	\N	\N	f	t	4	77
103	0	2108	3	\N	\N	\N	f	t	8	255
105	0	2110	3	\N	\N	\N	f	t	10	120
106	0	2112	3	\N	\N	\N	f	t	12	150
108	0	2111	3	\N	\N	\N	f	t	11	70
109	0	2114	3	\N	\N	\N	f	f	-1	150
110	0	2119	3	\N	\N	\N	f	f	\N	70
112	0	2116	3	\N	\N	\N	f	f	-1	55
113	0	2117	3	\N	\N	\N	t	t	1	120
115	0	2121	3	\N	\N	\N	f	f	\N	69
116	0	2122	3	\N	\N	\N	f	f	\N	200
117	0	2123	3	\N	\N	\N	f	f	\N	40
118	0	2118	3	\N	\N	\N	f	f	\N	150
125	0	3107	4	\N	\N	\N	f	t	5	120
126	0	3105	4	\N	\N	\N	f	t	3	200
127	0	3120	4	\N	\N	\N	f	f	-1	55
128	0	3125	4	\N	\N	\N	f	f	-1	200
130	0	3127	4	\N	\N	\N	t	t	0	120
131	0	3104	4	\N	\N	\N	f	t	2	70
133	0	3102	4	\N	\N	\N	f	f	-1	52
134	0	3122	4	\N	\N	\N	f	f	-1	40
136	0	3123	4	\N	\N	\N	f	f	-1	40
138	0	3118	4	\N	\N	\N	f	f	-1	75
139	0	3124	4	\N	\N	\N	f	f	-1	100
140	0	3117	4	\N	\N	\N	f	f	-1	75
141	0	3115	4	\N	\N	\N	f	f	-1	125
142	0	3121	4	\N	\N	\N	f	f	-1	125
144	0	3112	4	\N	\N	\N	f	f	-1	100
145	0	3114	4	\N	\N	\N	f	f	-1	125
147	0	3119	4	\N	\N	\N	f	f	-1	75
148	0	3108	4	\N	\N	\N	t	t	6	150
149	0	3126	4	\N	\N	\N	f	f	-1	70
151	0	3111	4	\N	\N	\N	f	f	-1	70
152	0	3128	4	\N	\N	\N	f	f	\N	120
153	0	3129	4	\N	\N	\N	f	f	\N	70
155	0	3202	5	\N	\N	\N	f	t	0	52
156	0	3203	5	\N	\N	\N	f	t	1	46
158	0	3205	5	\N	\N	\N	f	f	-1	200
159	0	3206	5	\N	\N	\N	f	f	-1	120
161	0	3208	5	\N	\N	\N	f	f	-1	200
162	0	3209	5	\N	\N	\N	f	f	-1	120
164	0	3211	5	\N	\N	\N	f	f	-1	200
165	0	3212	5	\N	\N	\N	f	f	-1	140
167	0	3214	5	\N	\N	\N	f	f	-1	50
168	0	3215	5	\N	\N	\N	f	f	-1	125
89	0	3302	6	\N	\N	\N	f	t	0	52
90	0	3303	6	\N	\N	\N	f	t	1	120
92	0	3305	6	\N	\N	\N	f	t	3	300
93	0	3304	6	\N	\N	\N	f	t	2	92
119	0	3401	7	\N	\N	\N	f	f	-1	92
120	0	3402	7	\N	\N	\N	f	t	0	52
121	0	3403	7	\N	\N	\N	f	t	1	50
124	0	3404	7	\N	\N	\N	f	t	2	300
170	0	4128	9	\N	\N	\N	f	f	-1	70
171	0	4129	9	\N	\N	\N	f	f	-1	120
172	0	4114	9	\N	\N	\N	f	t	8	150
173	0	4113	9	\N	\N	\N	f	f	-1	70
174	0	4120	9	\N	\N	\N	f	t	16	75
175	0	4106	9	\N	\N	\N	f	f	-1	77
176	0	4127	9	\N	\N	\N	f	t	18	76
206	0	4109	9	\N	\N	\N	f	t	9	56
207	0	4115	9	\N	\N	\N	f	t	12	76
209	0	4117	9	\N	\N	\N	f	t	14	81
254	0	1223	10	\N	\N	\N	f	f	\N	120
255	0	1233	10	\N	\N	\N	f	f	\N	255
256	0	1203	10	\N	\N	\N	t	t	0	77
257	0	1214	10	\N	\N	\N	f	t	12	150
259	0	1215	10	\N	\N	\N	f	t	1	145
260	0	1228	10	\N	\N	\N	f	t	2	70
262	0	1242	10	\N	\N	\N	f	f	\N	92
263	0	1234	10	\N	\N	\N	f	f	\N	150
265	0	1229	10	\N	\N	\N	f	f	\N	125
266	0	1231	10	\N	\N	\N	f	f	\N	75
268	0	1230	10	\N	\N	\N	f	f	\N	53
269	0	1216	10	\N	\N	\N	f	t	16	80
271	0	1221	10	\N	\N	\N	t	t	4	164
272	0	1205	10	\N	\N	\N	f	f	\N	92
273	0	1204	10	\N	\N	\N	f	f	\N	52
274	0	1220	10	\N	\N	\N	t	t	5	120
276	0	1237	10	\N	\N	\N	f	f	\N	56
277	0	1207	10	\N	\N	\N	f	f	\N	69
278	0	1210	10	\N	\N	\N	f	f	\N	125
279	0	1211	10	\N	\N	\N	f	f	\N	125
281	0	1201	10	\N	\N	\N	f	f	\N	92
282	0	1235	10	\N	\N	\N	f	t	19	128
283	0	1232	10	\N	\N	\N	f	t	21	250
285	0	1243	10	\N	\N	\N	f	t	17	120
286	0	1236	10	\N	\N	\N	f	t	18	250
289	0	1225	10	\N	\N	\N	f	f	\N	200
291	0	1209	10	\N	\N	\N	t	t	7	125
292	0	1239	10	\N	\N	\N	f	t	9	145
294	0	1219	10	\N	\N	\N	f	t	8	150
295	0	1206	10	\N	\N	\N	f	f	\N	56
299	0	130	1	\N	\N	\N	f	f	\N	255
300	0	131	1	\N	\N	\N	f	f	\N	150
301	0	132	1	\N	\N	\N	f	f	\N	250
302	0	133	1	\N	\N	\N	f	f	\N	46
303	0	134	1	\N	\N	\N	f	f	\N	125
304	0	1159	2	\N	\N	\N	f	f	\N	250
305	0	1160	2	\N	\N	\N	f	f	\N	46
306	0	1161	2	\N	\N	\N	f	f	\N	125
307	0	4141	9	\N	\N	\N	f	f	-1	250
308	0	4142	9	\N	\N	\N	f	f	-1	46
309	0	4143	9	\N	\N	\N	f	f	-1	125
310	0	1162	2	\N	\N	\N	f	f	\N	75
311	0	4144	9	\N	\N	\N	f	f	-1	75
312	0	1163	2	\N	\N	\N	f	f	\N	125
313	0	1164	2	\N	\N	\N	f	f	\N	75
314	0	4145	9	\N	\N	\N	f	f	-1	125
315	0	4146	9	\N	\N	\N	f	f	-1	75
316	0	1165	2	\N	\N	\N	f	f	\N	255
317	0	1166	2	\N	\N	\N	f	f	\N	150
318	0	4147	9	\N	\N	\N	f	f	-1	200
319	0	4148	9	\N	\N	\N	f	f	-1	200
320	0	4149	9	\N	\N	\N	f	f	-1	255
321	0	4150	9	\N	\N	\N	f	f	-1	150
322	0	135	1	\N	\N	\N	f	f	\N	120
323	0	2124	3	\N	\N	\N	f	f	\N	200
324	0	2125	3	\N	\N	\N	f	f	\N	200
325	0	4201	11	\N	\N	\N	f	f	-1	92
326	0	4202	11	\N	\N	\N	f	f	-1	92
327	0	4203	11	\N	\N	\N	f	f	-1	92
328	0	4204	11	\N	\N	\N	f	f	-1	53
329	0	4205	11	\N	\N	\N	f	f	-1	125
330	0	4206	11	\N	\N	\N	t	f	-1	77
331	0	4207	11	\N	\N	\N	f	f	-1	52
332	0	4208	11	\N	\N	\N	f	t	1	92
333	0	4209	11	\N	\N	\N	f	f	-1	56
334	0	4210	11	\N	\N	\N	f	f	-1	120
335	0	4211	11	asc	6	\N	f	t	9	125
336	0	4212	11	asc	7	\N	f	t	10	125
337	0	4213	11	\N	\N	\N	f	f	-1	70
338	0	4214	11	asc	5	\N	f	t	7	150
339	0	4215	11	\N	\N	\N	f	t	11	76
440	0	4216	11	\N	\N	\N	f	t	12	40
441	0	4217	11	\N	\N	\N	f	t	13	81
442	0	4218	11	\N	\N	\N	f	t	14	120
443	0	4219	11	\N	\N	\N	f	t	16	141
444	0	4220	11	\N	\N	\N	f	t	15	75
445	0	4221	11	\N	\N	\N	f	f	-1	53
446	0	4222	11	asc	8	\N	f	t	8	145
447	0	4223	11	\N	\N	\N	f	f	-1	84
448	0	4224	11	\N	\N	\N	t	f	-1	145
449	0	4225	11	\N	\N	\N	f	f	-1	150
450	0	4226	11	\N	\N	\N	t	f	-1	120
451	0	4227	11	\N	\N	\N	f	f	-1	76
452	0	4228	11	\N	\N	\N	t	f	-1	70
453	0	4229	11	asc	0	\N	f	t	0	120
454	0	4230	11	asc	2	\N	t	t	3	69
455	0	4231	11	\N	\N	\N	f	t	4	200
456	0	4232	11	asc	4	\N	f	t	6	200
457	0	4233	11	\N	\N	true	t	f	-1	40
458	0	4234	11	\N	\N	\N	f	f	-1	150
459	0	4235	11	\N	\N	\N	t	t	2	154
460	0	4236	11	\N	\N	\N	f	f	-1	70
461	0	4237	11	\N	\N	\N	f	f	-1	125
462	0	4238	11	\N	\N	\N	f	f	-1	125
463	0	4239	11	asc	3	\N	f	t	5	75
464	0	4240	11	\N	\N	\N	f	f	-1	125
465	0	4241	11	\N	\N	\N	f	f	-1	75
466	0	4242	11	\N	\N	\N	f	f	-1	200
467	0	4243	11	\N	\N	\N	f	f	-1	200
468	0	4244	11	\N	\N	\N	f	t	27	150
469	0	4245	11	\N	\N	\N	t	f	-1	125
470	0	4246	11	\N	\N	\N	f	t	20	40
471	0	4247	11	\N	\N	\N	f	t	21	55
472	0	4248	11	\N	\N	\N	f	t	19	220
473	0	4249	11	\N	\N	\N	f	t	17	220
474	0	4250	11	\N	\N	\N	f	t	18	220
475	0	136	1	\N	\N	\N	f	f	\N	80
476	0	1167	2	\N	\N	\N	f	f	\N	80
477	0	1168	2	\N	\N	\N	f	f	\N	100
478	0	4151	9	\N	\N	\N	f	f	-1	80
479	0	138	1	\N	\N	\N	f	f	\N	70
480	0	139	1	\N	\N	\N	f	f	\N	70
481	0	140	1	\N	\N	\N	f	f	\N	80
482	0	137	1	\N	\N	\N	f	f	\N	100
483	0	4152	9	\N	\N	\N	f	f	-1	100
484	0	1172	2	\N	\N	\N	f	f	-1	50
485	0	4156	9	\N	\N	\N	f	t	19	81
486	0	4157	9	\N	\N	\N	f	t	7	120
487	0	1169	2	\N	\N	\N	f	f	\N	70
488	0	1170	2	\N	\N	\N	f	f	\N	70
489	0	1171	2	\N	\N	\N	f	f	\N	80
490	0	4153	9	\N	\N	\N	f	f	-1	70
491	0	4154	9	\N	\N	\N	f	f	-1	70
492	0	4155	9	\N	\N	\N	f	f	-1	80
493	0	1245	10	\N	\N	\N	f	f	\N	70
494	0	1246	10	\N	\N	\N	f	f	\N	70
495	0	1247	10	\N	\N	\N	f	f	\N	80
496	0	1248	10	\N	\N	\N	f	f	\N	70
497	0	1249	10	\N	\N	\N	f	f	\N	81
498	0	1250	10	\N	\N	\N	f	f	\N	125
499	0	1251	10	\N	\N	\N	f	f	\N	100
500	0	1252	10	\N	\N	\N	f	f	\N	100
501	0	1253	10	\N	\N	\N	f	f	\N	125
502	0	1254	10	\N	\N	\N	f	f	\N	100
503	0	1255	10	\N	\N	\N	f	f	\N	200
504	0	1256	10	\N	\N	\N	f	f	\N	200
505	0	1257	10	\N	\N	\N	f	f	\N	200
506	0	1258	10	\N	\N	\N	f	f	\N	200
507	0	1259	10	\N	\N	\N	f	f	\N	150
508	0	1260	10	\N	\N	\N	f	f	\N	150
509	0	1261	10	\N	\N	\N	f	f	\N	200
510	0	4251	11	\N	\N	\N	f	f	\N	100
511	0	3501	12	\N	\N	\N	f	t	0	50
512	0	3502	12	asc	\N	\N	t	t	1	90
513	0	3503	12	\N	\N	\N	f	t	2	120
514	0	3504	12	\N	\N	\N	f	t	3	90
515	0	3505	12	\N	\N	\N	f	t	4	90
516	0	3506	12	\N	\N	\N	f	t	5	90
517	0	3507	12	\N	\N	\N	f	t	6	110
518	0	1173	2	\N	\N	\N	f	f	\N	80
519	0	4252	11	\N	\N	\N	f	t	23	100
521	0	3601	13	\N	\N	\N	f	t	0	50
522	0	3602	13	asc	\N	\N	t	t	1	100
523	0	3603	13	\N	\N	\N	f	t	2	350
524	0	3604	13	\N	\N	\N	f	t	3	100
525	0	3605	13	\N	\N	\N	f	f	\N	100
526	0	141	1	\N	\N	\N	f	f	\N	100
527	0	3701	14	asc	\N	\N	f	t	0	50
528	0	3702	14	\N	\N	\N	t	t	1	250
529	0	3703	14	\N	\N	\N	f	f	\N	250
530	0	142	1	\N	\N	\N	f	f	\N	125
531	0	3801	15	asc	\N	\N	f	t	0	80
532	0	3802	15	\N	\N	\N	f	t	1	180
533	0	3803	15	\N	\N	\N	f	f	\N	80
534	0	3804	15	\N	\N	\N	f	t	3	280
535	0	3805	15	\N	\N	\N	f	t	4	80
536	0	3806	15	\N	\N	\N	f	t	5	80
537	0	3807	15	\N	\N	\N	t	t	2	125
538	0	3901	16	asc	\N	\N	t	t	0	80
539	0	3902	16	\N	\N	\N	t	t	1	450
540	0	3903	16	\N	\N	\N	f	t	2	150
541	0	3904	16	\N	\N	\N	f	t	3	150
542	0	3905	16	\N	\N	\N	f	f	\N	200
543	0	5101	17	asc	\N	\N	t	t	0	70
544	0	5102	17	\N	\N	\N	t	t	1	200
545	0	5103	17	\N	\N	\N	f	t	2	40
546	0	5104	17	\N	\N	\N	f	t	3	200
547	0	5105	17	\N	\N	\N	f	t	4	40
548	0	5106	17	\N	\N	\N	f	t	5	200
549	0	5107	17	\N	\N	\N	f	t	6	40
550	0	5108	17	\N	\N	\N	f	t	7	200
551	0	5109	17	\N	\N	\N	f	t	8	40
552	0	5110	17	\N	\N	\N	f	t	9	200
553	0	5111	17	\N	\N	\N	f	t	10	40
554	0	5112	17	\N	\N	\N	f	t	11	200
555	0	5113	17	\N	\N	\N	f	t	12	40
556	0	5114	17	\N	\N	\N	f	t	13	200
557	0	5115	17	\N	\N	\N	f	t	14	40
558	0	5116	17	\N	\N	\N	f	t	15	200
559	0	5117	17	\N	\N	\N	f	t	16	40
560	0	5118	17	\N	\N	\N	f	t	17	200
561	0	5119	17	\N	\N	\N	f	t	18	40
562	0	5120	17	\N	\N	\N	f	t	19	200
563	0	5121	17	\N	\N	\N	f	t	20	40
564	0	5122	17	\N	\N	\N	f	t	21	200
565	0	5123	17	\N	\N	\N	f	t	22	40
566	0	5124	17	\N	\N	\N	f	t	23	200
567	0	5125	17	\N	\N	\N	f	t	24	40
568	0	5126	17	\N	\N	\N	f	t	25	200
569	0	5201	18	\N	\N	\N	t	t	0	70
570	0	5202	18	asc	\N	\N	t	t	1	60
571	0	5203	18	\N	\N	\N	t	t	2	60
572	0	5204	18	\N	\N	\N	t	t	3	300
573	0	5205	18	\N	\N	\N	f	t	4	170
574	0	5206	18	\N	\N	\N	f	f	\N	100
575	0	5207	18	\N	\N	\N	f	f	\N	100
576	0	5208	18	\N	\N	\N	f	f	\N	100
577	0	5209	18	\N	\N	\N	f	f	\N	300
578	0	5301	19	asc	\N	\N	t	t	0	100
579	0	5302	19	\N	\N	\N	t	t	1	200
580	0	5303	19	\N	\N	\N	f	t	2	100
581	0	5304	19	\N	\N	\N	t	t	3	100
582	0	5305	19	\N	\N	\N	t	t	4	100
583	0	5306	19	\N	\N	\N	t	t	5	100
584	0	5307	19	\N	\N	\N	t	t	6	100
585	0	5308	19	\N	\N	\N	f	t	7	100
586	0	5309	19	\N	\N	\N	f	t	8	100
587	0	5310	19	\N	\N	\N	f	t	9	100
588	0	5311	19	\N	\N	\N	f	t	10	100
589	0	5312	19	\N	\N	\N	f	t	11	100
590	0	5313	19	\N	\N	\N	f	t	12	100
591	0	5314	19	\N	\N	\N	f	t	13	100
592	0	4253	11	asc	9	\N	f	t	26	100
593	0	4254	11	\N	\N	\N	f	t	25	100
594	0	4255	11	asc	1	\N	f	t	24	80
595	0	4256	11	\N	\N	\N	f	t	22	80
596	0	1244	10	\N	\N	\N	f	f	\N	75
597	0	5401	20	\N	\N	\N	f	t	0	80
598	0	5402	20	asc	\N	\N	t	t	1	120
599	0	5403	20	\N	\N	\N	f	t	2	400
600	0	5501	21	asc	\N	\N	f	t	0	70
601	0	5502	21	\N	\N	\N	f	t	1	70
602	0	5503	21	\N	\N	\N	t	t	2	200
603	0	5504	21	\N	\N	\N	f	t	3	70
604	0	5505	21	\N	\N	\N	f	t	4	70
605	0	5506	21	\N	\N	\N	f	t	5	70
606	0	5507	21	\N	\N	\N	f	t	6	150
607	0	5508	21	\N	\N	\N	f	t	7	100
608	0	5509	21	\N	\N	\N	f	t	8	100
609	0	5601	22	asc	\N	\N	t	t	0	120
610	0	5602	22	\N	\N	\N	t	t	1	400
611	0	143	1	\N	\N	\N	f	f	\N	80
612	0	144	1	\N	\N	\N	f	f	\N	120
613	0	145	1	\N	\N	\N	f	f	\N	125
614	0	1174	2	\N	\N	\N	f	f	\N	80
615	0	1175	2	\N	\N	\N	f	f	\N	120
616	0	1176	2	\N	\N	\N	f	f	\N	125
617	0	4158	9	\N	\N	\N	f	f	\N	80
618	0	4159	9	\N	\N	\N	f	f	\N	120
619	0	4160	9	\N	\N	\N	f	f	\N	125
620	0	2126	3	\N	\N	\N	f	f	\N	120
621	0	1177	2	\N	\N	\N	f	f	\N	120
622	0	4161	9	\N	\N	\N	f	f	\N	120
643	1	10201	24	\N	\N	\N	f	f	\N	100
644	1	10202	24	\N	\N	\N	f	f	\N	100
645	1	10203	24	\N	\N	\N	f	f	\N	100
646	1	10204	24	\N	\N	\N	f	f	\N	100
647	1	10205	24	\N	\N	\N	f	f	\N	100
648	1	10206	24	\N	\N	\N	t	f	\N	100
649	1	10207	24	\N	\N	\N	f	f	\N	100
650	1	10208	24	\N	\N	\N	f	f	\N	100
651	1	10209	24	\N	\N	\N	f	t	10	80
652	1	10210	24	\N	\N	\N	f	f	\N	100
653	1	10211	24	\N	\N	\N	t	t	7	150
654	1	10212	24	\N	\N	\N	f	t	8	150
655	1	10213	24	\N	\N	\N	f	t	17	100
656	1	10214	24	\N	\N	\N	f	t	26	100
657	1	10215	24	\N	\N	\N	f	t	20	100
658	1	10216	24	\N	\N	\N	f	t	23	100
659	1	10217	24	\N	\N	\N	f	t	11	100
660	1	10218	24	\N	\N	\N	f	t	14	100
661	1	10219	24	\N	\N	\N	f	t	29	100
662	1	10220	24	\N	\N	\N	f	t	32	100
663	1	10221	24	\N	\N	\N	f	f	\N	100
664	1	10222	24	\N	\N	\N	f	t	0	150
665	1	10223	24	\N	\N	\N	f	t	1	90
666	1	10224	24	\N	\N	\N	t	t	9	150
667	1	10225	24	\N	\N	\N	t	f	\N	100
668	1	10226	24	\N	\N	\N	t	t	2	150
669	1	10227	24	\N	\N	\N	f	t	35	100
670	1	10228	24	\N	\N	true	t	f	\N	100
671	1	10229	24	\N	\N	\N	f	f	\N	100
672	1	10230	24	\N	\N	\N	f	t	3	150
673	1	10231	24	\N	\N	\N	f	t	4	100
674	1	10232	24	\N	\N	\N	f	t	5	100
675	1	10233	24	\N	\N	\N	f	t	6	200
676	1	10234	24	\N	\N	\N	f	f	\N	100
677	1	10235	24	\N	\N	\N	f	f	\N	100
678	1	10236	24	\N	\N	\N	f	t	18	100
679	1	10237	24	\N	\N	\N	f	t	19	100
680	1	10238	24	\N	\N	\N	f	t	27	100
681	1	10239	24	\N	\N	\N	f	t	28	100
682	1	10240	24	\N	\N	\N	f	t	21	100
683	1	10241	24	\N	\N	\N	f	t	22	100
684	1	10242	24	\N	\N	\N	f	t	24	100
685	1	10243	24	\N	\N	\N	f	t	25	100
686	1	10244	24	\N	\N	\N	f	t	12	100
687	1	10245	24	\N	\N	\N	f	t	13	100
688	1	10246	24	\N	\N	\N	f	t	15	100
689	1	10247	24	\N	\N	\N	f	t	16	100
690	1	10248	24	\N	\N	\N	f	t	30	100
691	1	10249	24	\N	\N	\N	f	t	31	100
692	1	10250	24	\N	\N	\N	f	t	33	100
693	1	10251	24	\N	\N	\N	f	t	34	100
696	1	10301	25	\N	\N	\N	f	f	\N	100
697	1	10302	25	\N	\N	\N	f	f	\N	100
698	1	10303	25	\N	\N	\N	f	f	\N	100
699	1	10304	25	\N	\N	\N	f	f	\N	100
700	1	10305	25	\N	\N	\N	f	t	4	150
701	1	10306	25	\N	\N	\N	t	f	\N	100
702	1	10307	25	\N	\N	\N	f	f	\N	100
703	1	10308	25	\N	\N	\N	f	f	\N	100
704	1	10309	25	\N	\N	\N	f	t	5	80
705	1	10310	25	\N	\N	\N	f	f	\N	100
706	1	10311	25	\N	\N	\N	f	t	13	150
707	1	10312	25	\N	\N	\N	f	t	14	150
708	1	10313	25	\N	\N	\N	f	t	9	100
709	1	10314	25	\N	\N	\N	f	t	10	150
710	1	10315	25	\N	\N	\N	f	t	22	100
711	1	10316	25	\N	\N	\N	f	t	17	100
712	1	10317	25	\N	\N	\N	f	t	18	100
713	1	10318	25	\N	\N	\N	f	t	23	100
714	1	10319	25	\N	\N	\N	f	t	24	100
715	1	10320	25	\N	\N	\N	f	t	21	100
716	1	10321	25	\N	\N	\N	f	t	19	100
717	1	10322	25	\N	\N	\N	f	t	20	100
718	1	10323	25	\N	\N	\N	f	f	\N	100
719	1	10324	25	\N	\N	\N	f	f	\N	100
720	1	10325	25	\N	\N	\N	f	f	\N	100
721	1	10326	25	\N	\N	\N	f	f	\N	100
722	1	10327	25	\N	\N	\N	f	f	\N	100
723	1	10328	25	\N	\N	\N	f	f	\N	100
724	1	10329	25	\N	\N	\N	f	t	25	100
725	1	10330	25	\N	\N	\N	f	t	26	100
726	1	10331	25	\N	\N	\N	f	t	27	100
727	1	10332	25	\N	\N	\N	f	t	28	100
728	1	10333	25	\N	\N	\N	f	t	29	100
729	1	10334	25	\N	\N	\N	f	t	30	100
730	1	10335	25	\N	\N	\N	f	t	31	100
731	1	10336	25	\N	\N	\N	f	t	32	100
732	1	10337	25	\N	\N	\N	f	t	33	100
733	1	10338	25	\N	\N	\N	f	t	34	100
734	1	10339	25	\N	\N	\N	f	t	35	100
735	1	10340	25	\N	\N	\N	f	t	36	100
736	1	10341	25	\N	\N	\N	f	t	37	100
737	1	10342	25	\N	\N	\N	f	t	38	100
738	1	10343	25	\N	\N	\N	f	f	\N	100
739	1	10344	25	\N	\N	\N	f	f	\N	100
740	1	10345	25	\N	\N	\N	f	f	\N	100
741	1	10346	25	\N	\N	\N	t	t	3	150
742	1	10347	25	\N	\N	\N	f	t	6	200
743	1	10348	25	\N	\N	\N	t	f	\N	100
744	1	10349	25	\N	\N	\N	f	f	\N	100
745	1	10350	25	\N	\N	\N	t	t	0	100
746	1	10351	25	\N	\N	\N	f	f	\N	100
747	1	10352	25	\N	\N	\N	t	t	1	100
748	1	10353	25	\N	\N	\N	f	f	\N	100
749	1	10354	25	\N	\N	true	t	f	\N	100
750	1	10355	25	\N	\N	\N	f	f	\N	100
751	1	10356	25	\N	\N	\N	f	f	\N	100
752	1	10357	25	\N	\N	\N	f	t	11	150
753	1	10358	25	\N	\N	\N	f	t	12	150
754	1	10359	25	\N	\N	\N	f	t	7	100
755	1	10360	25	\N	\N	\N	f	t	8	100
756	1	10361	25	\N	\N	\N	f	f	\N	100
757	1	10362	25	\N	\N	\N	f	f	\N	100
758	1	10363	25	\N	\N	\N	f	t	15	150
759	1	10364	25	\N	\N	\N	f	f	\N	100
760	1	10365	25	\N	\N	\N	f	t	39	200
761	1	10366	25	\N	\N	\N	f	t	40	200
694	1	10367	25	\N	\N	\N	f	t	2	100
762	1	10401	26	\N	\N	\N	f	f	\N	100
763	1	10402	26	\N	\N	\N	f	f	\N	100
764	1	10403	26	\N	\N	\N	f	t	0	100
765	1	10404	26	\N	\N	\N	f	t	1	100
766	1	10405	26	\N	\N	\N	t	f	\N	100
767	1	10406	26	\N	\N	\N	f	f	\N	100
768	1	10407	26	\N	\N	\N	f	f	\N	100
769	1	10408	26	\N	\N	\N	f	t	6	100
770	1	10409	26	\N	\N	\N	f	f	\N	100
771	1	10410	26	\N	\N	\N	t	t	3	100
772	1	10411	26	\N	\N	\N	f	t	4	100
773	1	10412	26	\N	\N	\N	f	t	9	100
774	1	10413	26	\N	\N	\N	f	f	\N	100
775	1	10414	26	\N	\N	\N	f	f	\N	100
776	1	10415	26	\N	\N	\N	t	t	30	100
777	1	10416	26	\N	\N	\N	t	f	\N	100
778	1	10417	26	\N	\N	\N	t	f	\N	100
779	1	10418	26	\N	\N	\N	f	t	31	100
780	1	10419	26	\N	\N	true	t	f	\N	100
781	1	10420	26	\N	\N	\N	f	f	\N	100
782	1	10421	26	\N	\N	\N	f	t	2	100
783	1	10422	26	\N	\N	\N	f	t	5	100
784	1	10423	26	\N	\N	\N	t	f	\N	100
785	1	10424	26	\N	\N	\N	f	t	15	100
786	1	10425	26	\N	\N	\N	f	t	16	100
787	1	10426	26	\N	\N	\N	f	t	17	100
788	1	10427	26	\N	\N	\N	f	t	18	100
789	1	10428	26	\N	\N	\N	f	t	19	100
790	1	10429	26	\N	\N	\N	f	t	10	100
791	1	10430	26	\N	\N	\N	f	t	11	100
792	1	10431	26	\N	\N	\N	f	t	12	100
793	1	10432	26	\N	\N	\N	f	t	13	100
794	1	10433	26	\N	\N	\N	f	t	14	100
795	1	10434	26	\N	\N	\N	f	t	20	100
796	1	10435	26	\N	\N	\N	f	t	21	100
797	1	10436	26	\N	\N	\N	f	t	22	100
798	1	10437	26	\N	\N	\N	f	t	23	100
799	1	10438	26	\N	\N	\N	f	t	24	100
800	1	10439	26	\N	\N	\N	f	t	25	100
801	1	10440	26	\N	\N	\N	f	t	26	100
802	1	10441	26	\N	\N	\N	f	t	27	100
803	1	10442	26	\N	\N	\N	f	t	28	100
804	1	10443	26	\N	\N	\N	f	t	29	100
805	1	10444	26	\N	\N	\N	f	t	7	100
806	1	10445	26	\N	\N	\N	f	t	8	100
807	0	2127	3	\N	\N	\N	f	f	\N	200
808	0	2128	3	\N	\N	\N	f	f	\N	200
809	0	1262	10	\N	\N	\N	f	f	\N	200
810	0	1401	27	\N	\N	\N	f	t	0	120
811	0	1402	27	\N	\N	\N	f	t	1	160
812	0	1403	27	\N	\N	\N	f	f	\N	200
813	0	1404	27	\N	\N	\N	t	f	\N	200
814	0	1405	27	\N	\N	\N	f	t	2	80
815	0	1406	27	\N	\N	\N	f	t	3	100
816	0	1407	27	\N	\N	true	t	f	\N	80
817	0	1408	27	\N	\N	\N	f	t	4	100
818	0	1409	27	\N	\N	\N	f	t	5	100
819	0	1410	27	\N	\N	\N	t	t	6	150
820	0	1411	27	\N	\N	\N	t	t	7	150
821	0	1412	27	\N	\N	\N	t	t	8	100
822	0	1413	27	\N	\N	\N	f	t	9	260
823	0	1414	27	\N	\N	\N	f	t	10	80
824	0	1415	27	\N	\N	\N	f	t	11	200
825	0	1416	27	\N	\N	\N	f	t	12	100
826	0	1417	27	\N	\N	\N	f	t	13	80
827	0	1418	27	\N	\N	\N	t	f	\N	200
844	1	1501	31	\N	\N	\N	f	f	\N	92
845	1	1502	31	\N	\N	\N	f	f	\N	92
846	1	1503	31	\N	\N	\N	f	f	\N	92
847	1	1504	31	\N	\N	\N	f	f	\N	54
848	1	1505	31	\N	\N	\N	f	f	\N	125
849	1	1506	31	\N	\N	\N	t	f	\N	77
850	1	1507	31	\N	\N	\N	f	f	\N	52
851	1	1508	31	\N	\N	\N	f	f	\N	92
852	1	1509	31	\N	\N	\N	f	t	2	56
853	1	1510	31	\N	\N	\N	f	f	\N	69
854	1	1511	31	\N	\N	\N	t	f	\N	125
855	1	1512	31	\N	\N	\N	f	f	\N	125
856	1	1513	31	\N	\N	\N	f	f	\N	70
857	1	1514	31	\N	\N	\N	f	f	\N	150
858	1	1515	31	\N	\N	\N	f	f	\N	75
859	1	1516	31	\N	\N	\N	f	f	\N	75
860	1	1517	31	\N	\N	\N	f	f	\N	75
861	1	1518	31	\N	\N	\N	f	f	\N	75
862	1	1519	31	\N	\N	\N	f	f	\N	75
863	1	1520	31	\N	\N	\N	f	f	\N	75
864	1	1521	31	\N	\N	\N	f	f	\N	75
865	1	1522	31	\N	\N	\N	f	f	\N	75
866	1	1523	31	\N	\N	\N	f	f	\N	75
867	1	1524	31	\N	\N	\N	f	f	\N	75
868	1	1525	31	\N	\N	\N	f	f	\N	75
869	1	1526	31	\N	\N	\N	f	f	\N	75
870	1	1527	31	\N	\N	\N	f	f	\N	75
871	1	1528	31	\N	\N	\N	f	f	\N	75
872	1	1529	31	\N	\N	\N	f	f	\N	75
873	1	1530	31	\N	\N	\N	f	f	\N	75
874	1	1531	31	\N	\N	\N	f	f	\N	75
875	1	1532	31	\N	\N	\N	f	f	\N	75
876	1	1533	31	\N	\N	\N	f	f	\N	75
877	1	1534	31	\N	\N	\N	f	f	\N	75
878	1	1535	31	\N	\N	\N	f	f	\N	75
879	1	1536	31	\N	\N	\N	f	f	\N	53
880	1	1537	31	\N	\N	\N	f	t	0	145
881	1	1538	31	\N	\N	\N	f	t	1	81
882	1	1539	31	\N	\N	\N	t	f	\N	145
883	1	1540	31	\N	\N	\N	t	t	3	150
884	1	1541	31	\N	\N	\N	t	f	\N	120
885	1	1542	31	\N	\N	\N	f	f	\N	76
886	1	1543	31	\N	\N	\N	f	f	\N	55
887	1	1544	31	\N	\N	\N	f	f	\N	55
888	1	1545	31	\N	\N	\N	f	f	\N	150
889	1	1546	31	\N	\N	true	t	f	\N	40
890	1	1547	31	\N	\N	\N	f	f	\N	150
891	1	1548	31	\N	\N	\N	f	f	\N	70
892	1	1549	31	\N	\N	\N	f	f	\N	164
893	1	1550	31	\N	\N	\N	f	f	\N	125
894	1	1551	31	\N	\N	\N	f	f	\N	125
895	1	1552	31	\N	\N	\N	f	f	\N	120
896	1	1553	31	\N	\N	\N	f	f	\N	200
897	1	1554	31	\N	\N	\N	f	f	\N	200
898	1	1555	31	\N	\N	\N	f	t	10	180
899	1	1556	31	\N	\N	\N	f	f	\N	125
900	1	1557	31	\N	\N	\N	f	f	\N	75
901	1	1558	31	\N	\N	D: %	t	t	4	255
902	1	1559	31	\N	\N	\N	f	f	\N	150
903	1	1560	31	\N	\N	\N	f	f	\N	80
904	1	1561	31	\N	\N	\N	f	f	\N	100
905	1	1562	31	\N	\N	\N	f	f	\N	70
906	1	1563	31	\N	\N	\N	f	f	\N	70
907	1	1564	31	\N	\N	\N	f	f	\N	80
908	1	1565	31	\N	\N	\N	f	f	\N	50
909	1	1566	31	\N	\N	\N	f	f	\N	80
910	1	1567	31	\N	\N	\N	f	f	\N	120
911	1	1568	31	\N	\N	\N	f	f	\N	125
912	1	1569	31	\N	\N	\N	f	f	\N	120
913	1	1570	31	\N	\N	\N	f	f	\N	200
914	1	1571	31	\N	\N	\N	f	f	\N	200
915	1	1572	31	\N	\N	\N	f	t	5	200
916	1	1573	31	\N	\N	\N	f	t	6	200
917	1	1574	31	\N	\N	\N	f	t	7	200
918	1	1575	31	\N	\N	\N	f	t	8	200
919	1	1576	31	\N	\N	\N	f	t	9	200
920	0	2129	3	\N	\N	\N	f	f	\N	250
921	0	2130	3	\N	\N	\N	f	f	\N	80
922	0	2131	3	\N	\N	\N	f	f	\N	125
\.


--
-- Name: base_query_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.base_query_id_seq', (SELECT max(id) FROM stamm.base_query), true);


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.filter_id_seq', (SELECT max(id) FROM stamm.filter), true);


--
-- Name: grid_column_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.grid_column_id_seq', (SELECT max(id) FROM stamm.grid_column), true);


--
-- Name: grid_column_values_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.grid_column_values_id_seq', (SELECT CASE WHEN max(id) < 10000 THEN 10000 ELSE max(id)+1 END FROM stamm.grid_column_values), true);
-- SELECT pg_catalog.setval('stamm.grid_column_values_id_seq', (SELECT max(id)+1 FROM stamm.grid_column_values), true);


--
-- Name: query_user_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.query_user_id_seq', (SELECT CASE WHEN max(id) < 100 THEN 100 ELSE max(id)+1 END FROM stamm.query_user), true);

--
-- Name: query_messstelle_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.query_messstelle_id_seq', (SELECT CASE WHEN max(id) < 100 THEN 100 ELSE max(id)+1 END FROM stamm.query_messstelle), true);


--
-- Name: result_type_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.result_type_id_seq', (SELECT max(id) FROM stamm.result_type), false);


--
-- Name: lada_user_id_seq; Type: SEQUENCE SET; Schema: stamm; Owner: postgres
--

SELECT pg_catalog.setval('stamm.lada_user_id_seq', (SELECT CASE WHEN max(id) < 100 THEN 100 ELSE max(id)+1 END FROM stamm.lada_user), true);

--
-- PostgreSQL database dump complete
--
