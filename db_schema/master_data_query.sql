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

TRUNCATE TABLE master.base_query, master.filter, master.disp CASCADE;
TRUNCATE TABLE master.lada_user CASCADE;

--
-- Data for Name: base_query; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.base_query (id, sql) FROM stdin;
1	SELECT probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  datenbasis.datenbasis AS dBasis,\n  stamm.mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.ort_id AS ortId,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  probe.ext_id AS externeProbeId,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.test,\n  betriebsart.name AS messRegime,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  probe.media_desk AS deskriptoren,\n  probe.media AS medium,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n  ortszuordnung.ortszusatztext,\n  ortszuordnung.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  array_to_string(tags.tags, ',', '') AS tags,\n  coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn) AS mitteSammelzeitraum,\n  staat_uo.staat AS uStaat,\n  ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit\n  FROM land.probe\nLEFT JOIN (\n  SELECT probe.id,\n    array_agg(tag.tag) AS tags\n  FROM land.probe\n  JOIN land.tagzuordnung ON probe.id = tagzuordnung.probe_id\n  JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n  GROUP BY probe.id\n  ) tags ON probe.id = tags.id\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz ON (ortszuordnung.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)
11	SELECT messung.id,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE\n   WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  CASE\n WHEN h3.messwert_nwg = '<'\n THEN h3.messwert_nwg || to_char(h3.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(h3.messwert, '0.99eeee')\n END AS h3,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n  CASE\n WHEN co60.messwert_nwg = '<'\n THEN co60.messwert_nwg || to_char(co60.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(co60.messwert, '0.99eeee')\n END AS co60,\n  CASE\n WHEN sr89.messwert_nwg = '<'\n THEN sr89.messwert_nwg || to_char(sr89.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr89.messwert, '0.99eeee')\n END AS sr89,\n CASE\n WHEN sr90.messwert_nwg = '<'\n THEN sr90.messwert_nwg || to_char(sr90.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr90.messwert, '0.99eeee')\n END AS sr90,  \n CASE\n WHEN ru103.messwert_nwg = '<'\n THEN ru103.messwert_nwg || to_char(ru103.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ru103.messwert, '0.99eeee')\n END AS ru103,\n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,  \n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN ce144.messwert_nwg = '<'\n THEN ce144.messwert_nwg || to_char(ce144.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ce144.messwert, '0.99eeee')\n END AS ce144,\n CASE\n WHEN u234.messwert_nwg = '<'\n THEN u234.messwert_nwg || to_char(u234.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u234.messwert, '0.99eeee')\n END AS u234,  \n CASE\n WHEN u235.messwert_nwg = '<'\n THEN u235.messwert_nwg || to_char(u235.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u235.messwert, '0.99eeee')\n END AS u235,\n CASE\n WHEN u238.messwert_nwg = '<'\n THEN u238.messwert_nwg || to_char(u238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u238.messwert, '0.99eeee')\n END AS u238,\n CASE\n WHEN pu238.messwert_nwg = '<'\n THEN pu238.messwert_nwg || to_char(pu238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu238.messwert, '0.99eeee')\n END AS pu238,  \n CASE\n WHEN pu239.messwert_nwg = '<'\n THEN pu239.messwert_nwg || to_char(pu239.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu239.messwert, '0.99eeee')\n END AS pu239,\n CASE\n WHEN pu23940.messwert_nwg = '<'\n THEN pu23940.messwert_nwg || to_char(pu23940.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu23940.messwert, '0.99eeee')\n END AS pu23940,\n CASE\n WHEN te132.messwert_nwg = '<'\n THEN te132.messwert_nwg || to_char(te132.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(te132.messwert, '0.99eeee')\n END AS te132,  \n CASE\n WHEN pb212.messwert_nwg = '<'\n THEN pb212.messwert_nwg || to_char(pb212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb212.messwert, '0.99eeee')\n END AS pb212,\n CASE\n WHEN pb214.messwert_nwg = '<'\n THEN pb214.messwert_nwg || to_char(pb214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb214.messwert, '0.99eeee')\n END AS pb214,\n CASE\n WHEN bi212.messwert_nwg = '<'\n THEN bi212.messwert_nwg || to_char(bi212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi212.messwert, '0.99eeee')\n END AS bi212,  \n CASE\n WHEN bi214.messwert_nwg = '<'\n THEN bi214.messwert_nwg || to_char(bi214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi214.messwert, '0.99eeee')\n END AS bi214,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE\n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n ortszuordnung.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  messung.fertig AS fertig,\n  (rueckfrage_messung.messungs_id IS NOT NULL) AS hatRueckfrage,\n     staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags\nFROM land.probe\nLEFT JOIN stamm.mess_stelle\n  ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nINNER JOIN land.messung\n  ON probe.id = messung.probe_id\nINNER JOIN land.status_protokoll\n  ON messung.STATUS = status_protokoll.id\nLEFT JOIN stamm.status_kombi\n  ON status_protokoll.status_kombi = stamm.status_kombi.id\nLEFT JOIN stamm.status_wert\n  ON stamm.status_wert.id = stamm.status_kombi.wert_id\nLEFT JOIN stamm.status_stufe\n  ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN land.rueckfrage_messung\n  ON rueckfrage_messung.messungs_id = messung.id\nLEFT JOIN stamm.datenbasis\n  ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart\n  ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung\n  ON (\n      probe.id = ortszuordnung.probe_id\n      AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n      )\nLEFT JOIN stamm.ort\n  ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit\n  ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz\n  ON (ortszuordnung.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe\n  ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe\n  ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.messprogramm_kategorie\n  ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.betriebsart\n  ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN public.lada_messwert h3\n  ON (h3.messungs_id = messung.id AND h3.messgroesse_id = 1)\nLEFT JOIN public.lada_messwert k40\n  ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\nLEFT JOIN public.lada_messwert co60\n  ON (co60.messungs_id = messung.id AND co60.messgroesse_id = 68)\nLEFT JOIN public.lada_messwert sr89\n  ON (sr89.messungs_id = messung.id AND sr89.messgroesse_id = 164)\nLEFT JOIN public.lada_messwert sr90\n  ON (sr90.messungs_id = messung.id AND sr90.messgroesse_id = 165)\nLEFT JOIN public.lada_messwert ru103\n  ON (ru103.messungs_id = messung.id AND ru103.messgroesse_id = 220)\nLEFT JOIN public.lada_messwert i131\n  ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\nLEFT JOIN public.lada_messwert cs134\n  ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\nLEFT JOIN public.lada_messwert cs137\n  ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\nLEFT JOIN public.lada_messwert ce144\n  ON (ce144.messungs_id = messung.id AND ce144.messgroesse_id = 404)\nLEFT JOIN public.lada_messwert u234\n  ON (u234.messungs_id = messung.id AND u234.messgroesse_id = 746)\nLEFT JOIN public.lada_messwert u235\n  ON (u235.messungs_id = messung.id AND u235.messgroesse_id = 747)\nLEFT JOIN public.lada_messwert u238\n  ON (u238.messungs_id = messung.id AND u238.messgroesse_id = 750)\nLEFT JOIN public.lada_messwert pu238\n  ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\nLEFT JOIN public.lada_messwert pu239\n  ON (pu239.messungs_id = messung.id AND pu239.messgroesse_id = 769)\nLEFT JOIN public.lada_messwert pu23940\n  ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\nLEFT JOIN public.lada_messwert te132\n  ON (te132.messungs_id = messung.id AND te132.messgroesse_id = 325)\nLEFT JOIN public.lada_messwert pb212\n  ON (pb212.messungs_id = messung.id AND pb212.messgroesse_id = 672)\nLEFT JOIN public.lada_messwert pb214\n  ON (pb214.messungs_id = messung.id AND pb214.messgroesse_id = 673)\nLEFT JOIN public.lada_messwert bi212\n  ON (bi212.messungs_id = messung.id AND bi212.messgroesse_id = 684)\nLEFT JOIN public.lada_messwert bi214  ON (bi214.messungs_id = messung.id AND bi214.messgroesse_id = 686) \nLEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\nLEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)
12	SELECT probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n datenbasis.datenbasis AS dBasis,\n stamm.mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.solldatum_beginn AS sollBegin,\n probe.solldatum_ende AS sollEnd,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n ort.ort_id AS ortId,\n ort.gem_id AS eGemId,\n verwaltungseinheit.bezeichnung AS eGem,\n probe.ext_id AS externeProbeId,\n probe.mpr_id AS mprId,\n messprogramm_kategorie.code AS mplCode,\n messprogramm_kategorie.bezeichnung AS mpl,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n kta_gruppe.kta_gruppe AS anlage,\n kta_gruppe.beschreibung AS anlagebeschr,\n rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n probe.test,\n betriebsart.name AS messRegime,\n land.messung.geplant,\n land.messung.messzeitpunkt AS messbeginn,\n land.messung.mmt_id AS mmtId,\n stamm.mess_methode.messmethode AS mmt,\n stamm.probenehmer.bezeichnung AS prnBezeichnung,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n pkommentar.pkommentar AS pKommentar,\n pzs.pzs,\n  ortszuordnung.oz_id AS ozId,\n staat_uo.staat AS uStaat,\n status_kombi.id AS statusK,\n stamm.probenehmer.prn_id AS prnId,\n stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n  land.messung.id,\n  land.messprogramm.probenahmemenge AS probemenge,\n land.messung.messdauer,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  verwaltungseinheit.regbezirk AS eRbezId,\n  messung.ext_id AS externeMessungsId,\n  stamm.probenehmer.betrieb AS prnBetrieb,\n stamm.probenehmer.plz AS prnPlz,\n stamm.probenehmer.ort AS prnOrt,\n stamm.probenehmer.strasse AS prnStrasse,\n stamm.probenehmer.telefon AS prnTelefon,\n stamm.probenehmer.bemerkung AS prnBemerkung,\n  messprogramm.kommentar AS mprKommentar,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  verwaltungseinheit_kreis.bezeichnung AS eKreis,\n  verwaltungseinheit_rbez.bezeichnung AS eRbez,\n  stamm.mess_stelle.beschreibung AS mstBeschr,\n stamm.get_desk_beschreibung(probe.media_desk, 2) AS desk2Beschr,\n array_to_string(tags.tags, ',', '') AS tags\n FROM land.probe\n LEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\n LEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\n LEFT JOIN stamm.verwaltungseinheit AS verwaltungseinheit_kreis ON (stamm.verwaltungseinheit.kreis = verwaltungseinheit_kreis.id)\n LEFT JOIN stamm.verwaltungseinheit AS verwaltungseinheit_rbez ON (stamm.verwaltungseinheit.regbezirk = verwaltungseinheit_rbez.id)\n LEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN stamm.messprogramm_kategorie\n ON (probe.mpl_id = messprogramm_kategorie.id)\n LEFT JOIN stamm.umwelt\n ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber\n ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.kta_gruppe\n ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\n LEFT JOIN stamm.rei_progpunkt_gruppe\n ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\n LEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\n INNER JOIN land.messung ON probe.id = messung.probe_id\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (\n probe.id = ortszuordnung_uo.probe_id\n AND ortszuordnung_uo.ortszuordnung_typ IN ('U')\n) \n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id) \n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN land.messprogramm ON (land.probe.mpr_id = land.messprogramm.id)\n LEFT JOIN (\n SELECT probe.id,\n array_to_string(array_agg(land.zusatz_wert.pzs_id || ' ' || stamm.proben_zusatz.beschreibung || ': ' ||  coalesce(land.zusatz_wert.kleiner_als,'') || ' ' || land.zusatz_wert.messwert_pzs || ' ' || coalesce(pzsmeh.einheit,'') || CASE WHEN land.zusatz_wert.messfehler IS NOT NULL THEN ' +/-' ELSE '' END || coalesce(to_char(land.zusatz_wert.messfehler,'99.9'),'') || CASE WHEN land.zusatz_wert.messfehler IS NOT NULL THEN ' %' ELSE '' END), ' # ', '') AS pzs\n FROM land.probe\n LEFT JOIN land.zusatz_wert ON (land.probe.id = land.zusatz_wert.probe_id)\n LEFT JOIN stamm.proben_zusatz ON (land.zusatz_wert.pzs_id = stamm.proben_zusatz.id)\n LEFT JOIN stamm.mess_einheit AS pzsmeh ON (stamm.proben_zusatz.meh_id = pzsmeh.id)\n GROUP BY probe.id\n) pzs ON probe.id = pzs.id\nLEFT JOIN (\n SELECT land.probe.id,\n string_agg(land.kommentar_p.text,' # ') AS pkommentar\n FROM land.probe\nLEFT JOIN land.kommentar_p ON land.probe.id = land.kommentar_p.probe_id GROUP BY probe.id) pkommentar ON land.probe.id = pkommentar.id\n LEFT JOIN ( SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)
14	SELECT probe.id AS id,\n probe.ext_id AS externeProbeId,\n stamm.mess_stelle.netzbetreiber_id AS netzId,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n probe.mst_id AS mstId,\n probe.datenbasis_id AS datenbasisId,\n probe.test,\n probe.ba_id AS baId,\n probenart.probenart AS probenartId,\n probe.solldatum_beginn AS solldatumBeginn,\n probe.solldatum_ende AS solldatumEnde,\n probe.mpr_id AS mprId,\n probe.media_desk AS mediaDesk,\n probe.umw_id AS umwId,\n array_to_string(mmtid.mmt, ', ', '') AS mmt,\n ort.gem_id AS gemId, \n stamm.probenehmer.id AS probeNehmerId, \n array_to_string(tags.tags, ',', '') AS tags\n FROM land.probe\n LEFT JOIN (\n SELECT probe.id,\n array_agg(tag.tag) AS tags\n FROM land.probe\n JOIN land.tagzuordnung ON probe.id = tagzuordnung.probe_id\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id\n) tags ON probe.id = tags.id\n LEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN (\n SELECT probe.id as pid,\n array_agg(messung.mmt_id) AS mmt\n FROM land.probe\n JOIN land.messung ON (probe.id = messung.probe_id)\n GROUP BY probe.id\n) mmtid ON probe.id = mmtid.pid
15	SELECT messung.id,\n probe.id AS probeId,\n probe.hauptproben_nr AS hpNr,\n messung.nebenproben_nr AS npNr,\n status_protokoll.datum AS statusD,\n datenbasis.datenbasis AS dBasis,\n mess_stelle.netzbetreiber_id AS netzId,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN probe.mst_id\n ELSE probe.mst_id || '-' || probe.labor_mst_id\n END AS mstLaborId,\n probe.umw_id AS umwId,\n probenart.probenart AS pArt,\n probe.probeentnahme_beginn AS peBegin,\n probe.probeentnahme_ende AS peEnd,\n ort.gem_id AS eGemId,\n verwaltungseinheit.bezeichnung AS eGem,\n CASE\n WHEN h3.messwert_nwg = '<'\n THEN h3.messwert_nwg || to_char(h3.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(h3.messwert, '0.99eeee')\n END AS h3,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n CASE\n WHEN co60.messwert_nwg = '<'\n THEN co60.messwert_nwg || to_char(co60.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(co60.messwert, '0.99eeee')\n END AS co60,\n CASE\n WHEN sr89.messwert_nwg = '<'\n THEN sr89.messwert_nwg || to_char(sr89.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr89.messwert, '0.99eeee')\n END AS sr89,\n CASE\n WHEN sr90.messwert_nwg = '<'\n THEN sr90.messwert_nwg || to_char(sr90.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr90.messwert, '0.99eeee')\n END AS sr90,  \n CASE\n WHEN ru103.messwert_nwg = '<'\n THEN ru103.messwert_nwg || to_char(ru103.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ru103.messwert, '0.99eeee')\n END AS ru103,\n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,  \n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN ce144.messwert_nwg = '<'\n THEN ce144.messwert_nwg || to_char(ce144.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ce144.messwert, '0.99eeee')\n END AS ce144,\n CASE\n WHEN u234.messwert_nwg = '<'\n THEN u234.messwert_nwg || to_char(u234.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u234.messwert, '0.99eeee')\n END AS u234,  \n CASE\n WHEN u235.messwert_nwg = '<'\n THEN u235.messwert_nwg || to_char(u235.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u235.messwert, '0.99eeee')\n END AS u235,\n CASE\n WHEN u238.messwert_nwg = '<'\n THEN u238.messwert_nwg || to_char(u238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u238.messwert, '0.99eeee')\n END AS u238,\n CASE\n WHEN pu238.messwert_nwg = '<'\n THEN pu238.messwert_nwg || to_char(pu238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu238.messwert, '0.99eeee')\n END AS pu238,  \n CASE\n WHEN pu239.messwert_nwg = '<'\n THEN pu239.messwert_nwg || to_char(pu239.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu239.messwert, '0.99eeee')\n END AS pu239,\n CASE\n WHEN pu23940.messwert_nwg = '<'\n THEN pu23940.messwert_nwg || to_char(pu23940.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu23940.messwert, '0.99eeee')\n END AS pu23940,\n CASE\n WHEN te132.messwert_nwg = '<'\n THEN te132.messwert_nwg || to_char(te132.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(te132.messwert, '0.99eeee')\n END AS te132,  \n CASE\n WHEN pb212.messwert_nwg = '<'\n THEN pb212.messwert_nwg || to_char(pb212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb212.messwert, '0.99eeee')\n END AS pb212,\n CASE\n WHEN pb214.messwert_nwg = '<'\n THEN pb214.messwert_nwg || to_char(pb214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb214.messwert, '0.99eeee')\n END AS pb214,\n CASE\n WHEN bi212.messwert_nwg = '<'\n THEN bi212.messwert_nwg || to_char(bi212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi212.messwert, '0.99eeee')\n END AS bi212,  \n CASE\n WHEN bi214.messwert_nwg = '<'\n THEN bi214.messwert_nwg || to_char(bi214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi214.messwert, '0.99eeee')\n END AS bi214,\n messung.mmt_id AS mmtId,\n probe.ext_id AS externeProbeId,\n messung.ext_id AS externeMessungsId,\n status_kombi.id AS statusK,\n umwelt.umwelt_bereich AS umw,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n public.st_asgeojson(ort.geom) AS entnahmeGeom,\n probe.mpr_id AS mprId,\n messprogramm_kategorie.code AS mplCode,\n messprogramm_kategorie.bezeichnung AS mpl,\n probe.test,\n betriebsart.name AS messRegime,\n CASE\n WHEN probe.mst_id = probe.labor_mst_id\n THEN stamm.mess_stelle.mess_stelle\n ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n END AS mstLabor,\n messung.geplant,\n probe.solldatum_beginn AS sollBegin,\n probe.solldatum_ende AS sollEnd,\n ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n land.messung.messdauer,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n ortszuordnung.oz_id AS ozId,\n stamm.ortszusatz.ortszusatz AS oz,\n verwaltungseinheit.kreis AS eKreisId,\n verwaltungseinheit.bundesland AS eBlId,\n stamm.staat.staat AS eStaat,\n messung.fertig AS fertig,\n staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags,\n pkommentar.pkommentar AS pKommentar,\n mkommentar.mkommentar AS mKommentar,\n stamm.get_desk_beschreibung(probe.media_desk, 0) AS desk0Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 1) AS desk1Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 2) AS desk2Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 3) AS desk3Beschr,\n stamm.get_desk_beschreibung(probe.media_desk, 4) AS desk4Beschr\n FROM land.probe\n LEFT JOIN stamm.mess_stelle\n ON (probe.mst_id = stamm.mess_stelle.id)\n LEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\n INNER JOIN land.messung\n ON probe.id = messung.probe_id\n INNER JOIN land.status_protokoll\n ON messung.STATUS = status_protokoll.id\n LEFT JOIN stamm.status_kombi\n ON status_protokoll.status_kombi = stamm.status_kombi.id\n LEFT JOIN stamm.status_wert\n ON stamm.status_wert.id = stamm.status_kombi.wert_id\n LEFT JOIN stamm.status_stufe\n ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\n LEFT JOIN stamm.datenbasis\n ON (probe.datenbasis_id = datenbasis.id)\n LEFT JOIN stamm.probenart\n ON (probe.probenart_id = probenart.id)\n LEFT JOIN land.ortszuordnung\n ON (\n probe.id = ortszuordnung.probe_id\n AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n)\n LEFT JOIN stamm.ort\n ON (ortszuordnung.ort_id = ort.id)\n LEFT JOIN stamm.verwaltungseinheit\n ON (ort.gem_id = verwaltungseinheit.id)\n LEFT JOIN stamm.ortszusatz\n ON (ortszuordnung.oz_id = ortszusatz.ozs_id)\n LEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\n LEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\n LEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\n LEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.messprogramm_kategorie\n ON (probe.mpl_id = messprogramm_kategorie.id)\n LEFT JOIN stamm.betriebsart\n ON (probe.ba_id = stamm.betriebsart.id)\n LEFT JOIN public.lada_messwert h3\n ON (h3.messungs_id = messung.id AND h3.messgroesse_id = 1)\n LEFT JOIN public.lada_messwert k40\n ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\n LEFT JOIN public.lada_messwert co60\n ON (co60.messungs_id = messung.id AND co60.messgroesse_id = 68)\n LEFT JOIN public.lada_messwert sr89\n ON (sr89.messungs_id = messung.id AND sr89.messgroesse_id = 164)\n LEFT JOIN public.lada_messwert sr90\n ON (sr90.messungs_id = messung.id AND sr90.messgroesse_id = 165)\n LEFT JOIN public.lada_messwert ru103\n ON (ru103.messungs_id = messung.id AND ru103.messgroesse_id = 220)\n LEFT JOIN public.lada_messwert i131\n ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\n LEFT JOIN public.lada_messwert cs134\n ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\n LEFT JOIN public.lada_messwert cs137\n ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\n LEFT JOIN public.lada_messwert ce144\n ON (ce144.messungs_id = messung.id AND ce144.messgroesse_id = 404)\n LEFT JOIN public.lada_messwert u234\n ON (u234.messungs_id = messung.id AND u234.messgroesse_id = 746)\n LEFT JOIN public.lada_messwert u235\n ON (u235.messungs_id = messung.id AND u235.messgroesse_id = 747)\n LEFT JOIN public.lada_messwert u238\n ON (u238.messungs_id = messung.id AND u238.messgroesse_id = 750)\n LEFT JOIN public.lada_messwert pu238\n ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\n LEFT JOIN public.lada_messwert pu239\n ON (pu239.messungs_id = messung.id AND pu239.messgroesse_id = 769)\n LEFT JOIN public.lada_messwert pu23940\n ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\n LEFT JOIN public.lada_messwert te132\n ON (te132.messungs_id = messung.id AND te132.messgroesse_id = 325)\n LEFT JOIN public.lada_messwert pb212\n ON (pb212.messungs_id = messung.id AND pb212.messgroesse_id = 672)\n LEFT JOIN public.lada_messwert pb214\n ON (pb214.messungs_id = messung.id AND pb214.messgroesse_id = 673)\n LEFT JOIN public.lada_messwert bi212\n ON (bi212.messungs_id = messung.id AND bi212.messgroesse_id = 684)\n LEFT JOIN public.lada_messwert bi214  \n ON (bi214.messungs_id = messung.id AND bi214.messgroesse_id = 686) \n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n LEFT JOIN (\n SELECT land.probe.id,\n string_agg(land.kommentar_p.text,' # ') AS pkommentar\n FROM land.probe\n LEFT JOIN land.kommentar_p \n ON land.probe.id = land.kommentar_p.probe_id GROUP BY probe.id) pkommentar \n ON land.probe.id = pkommentar.id\n LEFT JOIN (\n SELECT land.messung.id,\n string_agg(land.kommentar_m.TEXT, ' # ') AS mkommentar\n FROM land.messung\n LEFT JOIN land.kommentar_m \n ON land.messung.id = land.kommentar_m.messungs_id GROUP BY messung.id) mkommentar \n ON land.messung.id = mkommentar.id
16	SELECT messung.id,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE\n   WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  CASE\n WHEN h3.messwert_nwg = '<'\n THEN h3.messwert_nwg || to_char(h3.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(h3.messwert, '0.99eeee')\n END AS h3,\n CASE\n WHEN k40.messwert_nwg = '<'\n THEN k40.messwert_nwg || to_char(k40.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(k40.messwert, '0.99eeee')\n END AS k40,\n  CASE\n WHEN co60.messwert_nwg = '<'\n THEN co60.messwert_nwg || to_char(co60.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(co60.messwert, '0.99eeee')\n END AS co60,\n  CASE\n WHEN sr89.messwert_nwg = '<'\n THEN sr89.messwert_nwg || to_char(sr89.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr89.messwert, '0.99eeee')\n END AS sr89,\n CASE\n WHEN sr90.messwert_nwg = '<'\n THEN sr90.messwert_nwg || to_char(sr90.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(sr90.messwert, '0.99eeee')\n END AS sr90,  \n CASE\n WHEN ru103.messwert_nwg = '<'\n THEN ru103.messwert_nwg || to_char(ru103.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ru103.messwert, '0.99eeee')\n END AS ru103,\n CASE\n WHEN i131.messwert_nwg = '<'\n THEN i131.messwert_nwg || to_char(i131.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(i131.messwert, '0.99eeee')\n END AS i131,\n CASE\n WHEN cs134.messwert_nwg = '<'\n THEN cs134.messwert_nwg || to_char(cs134.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs134.messwert, '0.99eeee')\n END AS cs134,  \n CASE\n WHEN cs137.messwert_nwg = '<'\n THEN cs137.messwert_nwg || to_char(cs137.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(cs137.messwert, '0.99eeee')\n END AS cs137,\n CASE\n WHEN ce144.messwert_nwg = '<'\n THEN ce144.messwert_nwg || to_char(ce144.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(ce144.messwert, '0.99eeee')\n END AS ce144,\n CASE\n WHEN u234.messwert_nwg = '<'\n THEN u234.messwert_nwg || to_char(u234.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u234.messwert, '0.99eeee')\n END AS u234,  \n CASE\n WHEN u235.messwert_nwg = '<'\n THEN u235.messwert_nwg || to_char(u235.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u235.messwert, '0.99eeee')\n END AS u235,\n CASE\n WHEN u238.messwert_nwg = '<'\n THEN u238.messwert_nwg || to_char(u238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(u238.messwert, '0.99eeee')\n END AS u238,\n CASE\n WHEN pu238.messwert_nwg = '<'\n THEN pu238.messwert_nwg || to_char(pu238.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu238.messwert, '0.99eeee')\n END AS pu238,  \n CASE\n WHEN pu239.messwert_nwg = '<'\n THEN pu239.messwert_nwg || to_char(pu239.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu239.messwert, '0.99eeee')\n END AS pu239,\n CASE\n WHEN pu23940.messwert_nwg = '<'\n THEN pu23940.messwert_nwg || to_char(pu23940.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pu23940.messwert, '0.99eeee')\n END AS pu23940,\n CASE\n WHEN te132.messwert_nwg = '<'\n THEN te132.messwert_nwg || to_char(te132.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(te132.messwert, '0.99eeee')\n END AS te132,  \n CASE\n WHEN pb212.messwert_nwg = '<'\n THEN pb212.messwert_nwg || to_char(pb212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb212.messwert, '0.99eeee')\n END AS pb212,\n CASE\n WHEN pb214.messwert_nwg = '<'\n THEN pb214.messwert_nwg || to_char(pb214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(pb214.messwert, '0.99eeee')\n END AS pb214,\n CASE\n WHEN bi212.messwert_nwg = '<'\n THEN bi212.messwert_nwg || to_char(bi212.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi212.messwert, '0.99eeee')\n END AS bi212,  \n CASE\n WHEN bi214.messwert_nwg = '<'\n THEN bi214.messwert_nwg || to_char(bi214.nwg_zu_messwert, '0.99eeee')\n ELSE to_char(bi214.messwert, '0.99eeee')\n END AS bi214,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE\n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  ort.ort_id AS ortId,\n ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n ortszuordnung.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  messung.fertig AS fertig,\n  (rueckfrage_messung.messungs_id IS NOT NULL) AS hatRueckfrage,\n     staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags,\n  pzs.pzs\nFROM land.probe\nLEFT JOIN stamm.mess_stelle\n  ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nINNER JOIN land.messung\n  ON probe.id = messung.probe_id\nINNER JOIN land.status_protokoll\n  ON messung.STATUS = status_protokoll.id\nLEFT JOIN stamm.status_kombi\n  ON status_protokoll.status_kombi = stamm.status_kombi.id\nLEFT JOIN stamm.status_wert\n  ON stamm.status_wert.id = stamm.status_kombi.wert_id\nLEFT JOIN stamm.status_stufe\n  ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN land.rueckfrage_messung\n  ON rueckfrage_messung.messungs_id = messung.id\nLEFT JOIN stamm.datenbasis\n  ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart\n  ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung\n  ON (\n      probe.id = ortszuordnung.probe_id\n      AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n      )\nLEFT JOIN stamm.ort\n  ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit\n  ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz\n  ON (ortszuordnung.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe\n  ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe\n  ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.messprogramm_kategorie\n  ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.betriebsart\n  ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN public.lada_messwert h3\n  ON (h3.messungs_id = messung.id AND h3.messgroesse_id = 1)\nLEFT JOIN public.lada_messwert k40\n  ON (k40.messungs_id = messung.id AND k40.messgroesse_id = 28)\nLEFT JOIN public.lada_messwert co60\n  ON (co60.messungs_id = messung.id AND co60.messgroesse_id = 68)\nLEFT JOIN public.lada_messwert sr89\n  ON (sr89.messungs_id = messung.id AND sr89.messgroesse_id = 164)\nLEFT JOIN public.lada_messwert sr90\n  ON (sr90.messungs_id = messung.id AND sr90.messgroesse_id = 165)\nLEFT JOIN public.lada_messwert ru103\n  ON (ru103.messungs_id = messung.id AND ru103.messgroesse_id = 220)\nLEFT JOIN public.lada_messwert i131\n  ON (i131.messungs_id = messung.id AND i131.messgroesse_id = 340)\nLEFT JOIN public.lada_messwert cs134\n  ON (cs134.messungs_id = messung.id AND cs134.messgroesse_id = 369)\nLEFT JOIN public.lada_messwert cs137\n  ON (cs137.messungs_id = messung.id AND cs137.messgroesse_id = 373)\nLEFT JOIN public.lada_messwert ce144\n  ON (ce144.messungs_id = messung.id AND ce144.messgroesse_id = 404)\nLEFT JOIN public.lada_messwert u234\n  ON (u234.messungs_id = messung.id AND u234.messgroesse_id = 746)\nLEFT JOIN public.lada_messwert u235\n  ON (u235.messungs_id = messung.id AND u235.messgroesse_id = 747)\nLEFT JOIN public.lada_messwert u238\n  ON (u238.messungs_id = messung.id AND u238.messgroesse_id = 750)\nLEFT JOIN public.lada_messwert pu238\n  ON (pu238.messungs_id = messung.id AND pu238.messgroesse_id = 768)\nLEFT JOIN public.lada_messwert pu239\n  ON (pu239.messungs_id = messung.id AND pu239.messgroesse_id = 769)\nLEFT JOIN public.lada_messwert pu23940\n  ON (pu23940.messungs_id = messung.id AND pu23940.messgroesse_id = 850)\nLEFT JOIN public.lada_messwert te132\n  ON (te132.messungs_id = messung.id AND te132.messgroesse_id = 325)\nLEFT JOIN public.lada_messwert pb212\n  ON (pb212.messungs_id = messung.id AND pb212.messgroesse_id = 672)\nLEFT JOIN public.lada_messwert pb214\n  ON (pb214.messungs_id = messung.id AND pb214.messgroesse_id = 673)\nLEFT JOIN public.lada_messwert bi212\n  ON (bi212.messungs_id = messung.id AND bi212.messgroesse_id = 684)\nLEFT JOIN public.lada_messwert bi214  ON (bi214.messungs_id = messung.id AND bi214.messgroesse_id = 686) \nLEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\nLEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n LEFT JOIN ( SELECT probe.id,\n array_to_string(array_agg(land.zusatz_wert.pzs_id || ' ' || stamm.proben_zusatz.beschreibung || ': ' ||  coalesce(land.zusatz_wert.kleiner_als,'') || ' ' || land.zusatz_wert.messwert_pzs || ' ' || coalesce(pzsmeh.einheit,'') || CASE WHEN land.zusatz_wert.messfehler IS NOT NULL THEN ' +/-' ELSE '' END || coalesce(to_char(land.zusatz_wert.messfehler,'99.9'),'') || CASE WHEN land.zusatz_wert.messfehler IS NOT NULL THEN ' %' ELSE '' END), ' # ', '') AS pzs\n FROM land.probe\n LEFT JOIN land.zusatz_wert ON (land.probe.id = land.zusatz_wert.probe_id)\n LEFT JOIN stamm.proben_zusatz ON (land.zusatz_wert.pzs_id = stamm.proben_zusatz.id)\n LEFT JOIN stamm.mess_einheit AS pzsmeh ON (stamm.proben_zusatz.meh_id = pzsmeh.id)\n GROUP BY probe.id\n) pzs ON probe.id = pzs.id
21	SELECT messprogramm.id AS mpNr,\n  stamm.mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN messprogramm.mst_id = messprogramm.labor_mst_id\n      THEN messprogramm.mst_id\n    ELSE messprogramm.mst_id || '-' || messprogramm.labor_mst_id\n    END AS mstLaborId,\n  datenbasis.datenbasis AS dBasis,\n  betriebsart.name AS messRegime,\n  probenart.probenart AS pArt,\n  messprogramm.umw_id AS umwId,\n  messprogramm.media_desk AS deskriptoren,\n  messprogramm.probenintervall AS intervall,\n  ort.ort_id AS ortId,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  CASE \n    WHEN messprogramm.mst_id = messprogramm.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messprogramm.aktiv AS aktiv,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n umwelt.umwelt_bereich AS umw,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  messprogramm.test,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  ort_uo.ort_id AS uOrtId,\n messprogramm.kommentar AS mprKommentar,\n  messprogramm.probe_kommentar AS mprPKommentar,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n stamm.probenehmer.prn_id AS prnId,\n stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung\nFROM land.messprogramm\nLEFT JOIN stamm.mess_stelle\n  ON (messprogramm.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle as labormessstelle\n  ON (messprogramm.labor_mst_id = labormessstelle.id)\nLEFT JOIN stamm.datenbasis\n  ON (messprogramm.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart\n  ON (messprogramm.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung_mp\n  ON (\n      messprogramm.id = ortszuordnung_mp.messprogramm_id\n      AND ortszuordnung_mp.ortszuordnung_typ IN ('E', 'R')\n      )\nLEFT JOIN stamm.ort\n  ON (ortszuordnung_mp.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit\n  ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (messprogramm.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN land.ortszuordnung_mp AS ortszuordnung_mp_uo\n ON (\n messprogramm.id = ortszuordnung_mp_uo.messprogramm_id\n AND ortszuordnung_mp_uo.ortszuordnung_typ IN ('U')\n)\n LEFT JOIN stamm.ort AS ort_uo\n ON (ortszuordnung_mp_uo.ort_id = ort_uo.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.umwelt\n  ON (messprogramm.umw_id = umwelt.id)\nLEFT JOIN stamm.kta_gruppe\n  ON (messprogramm.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe\n  ON (messprogramm.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.betriebsart ON (messprogramm.ba_id = stamm.betriebsart.id)\nLEFT JOIN stamm.probenehmer ON (land.messprogramm.probe_nehmer_id = stamm.probenehmer.id)
31	SELECT ort.id,\n  ort.netzbetreiber_id AS netzId,\n  ort.ort_id AS ortId,\n  ort_typ.code AS ortTyp,\n  ort.kurztext,\n  ort.langtext,\n  staat.staat AS staat,\n  verwaltungseinheit.bezeichnung AS verwaltungseinheit,\n  ort.oz_id AS ozId,\n  kta_gruppe.kta_gruppe AS anlage,\n  ort.mp_art AS mpArt,\n  koordinaten_art.koordinatenart AS koordinatenArt,\n  ort.koord_x_extern AS koordXExtern,\n  ort.koord_y_extern AS koordYExtern,\n  PUBLIC.ST_X(ort.geom) AS longitude,\n  PUBLIC.ST_Y(ort.geom) AS latitude,\n  ort.hoehe_ueber_nn AS hoeheUeberNn,\n  ort.hoehe_land AS hoeheLand,\n  ort.aktiv,\n  ort.letzte_aenderung AS letzteAenderung,\n  ort.zone,\n  ort.sektor,\n  ort.zustaendigkeit,\n  ort.berichtstext,\n  ort.unscharf,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  ort.gem_id AS verwid,\n  public.st_asgeojson(ort.geom) AS geom\nFROM stamm.ort\nLEFT JOIN stamm.verwaltungseinheit\n  ON ort.gem_id = verwaltungseinheit.id\nLEFT JOIN stamm.staat\n  ON stamm.staat.id = ort.staat_id\nINNER JOIN stamm.koordinaten_art\n  ON stamm.koordinaten_art.id = ort.kda_id\nLEFT JOIN stamm.ort_typ\n  ON ort.ort_typ = ort_typ.id\nLEFT JOIN stamm.kta_gruppe\n  ON kta_gruppe.id = ort.kta_gruppe_id\nLEFT JOIN stamm.netz_betreiber\n  ON (ort.netzbetreiber_id = netz_betreiber.id)\n LEFT JOIN stamm.rei_progpunkt_gruppe ON (ort.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)
32	SELECT probenehmer.id, netzbetreiber_id AS netzId, prn_id AS prnId, bearbeiter, bemerkung, betrieb, bezeichnung, kurz_bezeichnung AS kurzBezeichnung, ort, plz, strasse, telefon, tourenplan, typ, probenehmer.letzte_aenderung AS letzteAenderung, netz_betreiber.netzbetreiber AS netzbetreiber FROM stamm.probenehmer LEFT JOIN stamm.netz_betreiber ON (stamm.probenehmer.netzbetreiber_id = netz_betreiber.id)
33	SELECT datensatz_erzeuger.id, datensatz_erzeuger.netzbetreiber_id AS netzId, datensatz_erzeuger_id AS datensatzErzeugerId, mst_id AS mstId, bezeichnung, datensatz_erzeuger.letzte_aenderung AS letzteAenderung, netz_betreiber.netzbetreiber AS netzbetreiber, stamm.mess_stelle.mess_stelle AS mst FROM stamm.datensatz_erzeuger LEFT JOIN stamm.netz_betreiber ON (stamm.datensatz_erzeuger.netzbetreiber_id = netz_betreiber.id) LEFT JOIN stamm.mess_stelle ON (stamm.datensatz_erzeuger.mst_id = stamm.mess_stelle.id)
34	SELECT messprogramm_kategorie.id, netzbetreiber_id AS netzId, code, bezeichnung, messprogramm_kategorie.letzte_aenderung AS letzteAenderung, netz_betreiber.netzbetreiber AS netzbetreiber FROM stamm.messprogramm_kategorie LEFT JOIN stamm.netz_betreiber ON (stamm.messprogramm_kategorie.netzbetreiber_id = netz_betreiber.id)
35	SELECT messgroesse.id AS mgrId,\n messgroesse,\n beschreibung AS mgrBeschreibung,\n idf_nuklid_key AS idfNuklidKey,\n ist_leitnuklid AS istLeitnuklid,\n eudf_nuklid_id AS eudfNuklidId,\n kennung_bvl AS kennungBvl\n FROM stamm.messgroesse
36	SELECT mess_einheit.id AS meId,\n einheit AS me,\n beschreibung AS meBeschreibung,\n eudf_messeinheit_id AS eudfMesseinheitId,\n umrechnungs_faktor_eudf AS umrEudf\n FROM stamm.mess_einheit
37	SELECT mess_methode.id AS mmtId,\n messmethode AS mmt,\n beschreibung AS mmtBeschreibung\n FROM stamm.mess_methode
38	SELECT mess_stelle.id AS mstId,\n mess_stelle AS mst,\n mess_stelle.netzbetreiber_id AS netzId,\n beschreibung AS mstBeschreibung,\n mst_typ AS mstTyp,\n amtskennung AS mstAmtskennung,\n netz_betreiber.netzbetreiber AS netzbetreiber\n  FROM stamm.mess_stelle\n LEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)
39	SELECT umwelt.id AS umwId,\n umwelt_bereich AS umw,\n mess_einheit.einheit AS me,\n mess_einheit_2.einheit AS me2,\n umwelt.beschreibung AS umwBeschreibung\n FROM stamm.umwelt\n LEFT JOIN stamm.mess_einheit ON (stamm.umwelt.meh_id = stamm.mess_einheit.id)\n LEFT JOIN stamm.mess_einheit AS mess_einheit_2 ON (stamm.umwelt.meh_id_2 = mess_einheit_2.id)
41	SELECT messung.id AS messungId,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messgroesse.messgroesse AS messgroesse,\n  coalesce(public.lada_messwert.messwert_nwg, ' ') AS nwg,\n  public.lada_messwert.messwert AS wert,\n  public.lada_messwert.nwg_zu_messwert AS nwgZuMesswert,\n  public.lada_messwert.messfehler AS fehler,\n  mess_einheit.einheit AS einheit,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n    ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n ortszuordnung.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n  coalesce(public.lada_messwert.messwert,public.lada_messwert.nwg_zu_messwert) AS wertNwg,\n  ort.ort_id AS ortId,\n  staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags,\n  lada_messwert.id\nFROM land.probe\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nJOIN land.messung ON probe.id = messung.probe_id\nJOIN land.status_protokoll ON messung.STATUS = status_protokoll.id\nJOIN stamm.status_kombi ON status_protokoll.status_kombi = stamm.status_kombi.id\nJOIN stamm.status_wert ON stamm.status_wert.id = stamm.status_kombi.wert_id\nJOIN stamm.status_stufe ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz\n  ON (ortszuordnung.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id\n)LEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nJOIN public.lada_messwert ON (public.lada_messwert.messungs_id = messung.id)\nJOIN stamm.messgroesse ON (messgroesse.id = public.lada_messwert.messgroesse_id)\nLEFT JOIN stamm.mess_einheit ON (mess_einheit.id = public.lada_messwert.meh_id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n
42	SELECT messung.id AS messungId,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  ort.ort_id AS ortId,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messgroesse.messgroesse AS messgroesse,\n  coalesce(public.lada_messwert.messwert_nwg, ' ') AS nwg,\n  public.lada_messwert.messwert AS wert,\n  public.lada_messwert.nwg_zu_messwert AS nwgZuMesswert,\n  public.lada_messwert.messfehler AS fehler,\n  mess_einheit.einheit AS einheit,\n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  ort.berichtstext AS berichtstext,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  stamm.mess_methode.messmethode AS mmt,\n  land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n  ort.kurztext AS ortKurztext,\n  ort.langtext AS ortLangtext,\n  probe.media AS medium,\n  coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn) AS mitteSammelzeitraum,\n  extract(quarter FROM (coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn))) AS qmitteSammelzeitraum,\n  extract(year FROM (coalesce((probe.probeentnahme_beginn + ((probe.probeentnahme_ende - probe.probeentnahme_beginn) / 2)), probe.probeentnahme_beginn))) AS jmitteSammelzeitraum,\n  pzs.pzs,\n  pkommentar.pkommentar AS pKommentar,\n  mkommentar.mkommentar AS mKommentar,\n  array_to_string(tags.tags, ', ', '') AS tags,\n  mess_stelle.beschreibung AS messstellenadr,\n  messgroesse.id AS mgrId,\n  labormessstelle.beschreibung AS messlaboradr,\n probe.labor_mst_id AS laborMstId,\n  probe.mst_id AS mstId,\n lada_messwert.id\n  FROM land.probe\nLEFT JOIN (\n  SELECT probe.id,\n    array_agg(tag.tag) AS tags\n  FROM land.probe\n  JOIN land.tagzuordnung ON probe.id = tagzuordnung.probe_id\n  JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n  GROUP BY probe.id\n  ) tags ON probe.id = tags.id\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nJOIN land.messung ON probe.id = messung.probe_id\nJOIN land.status_protokoll ON messung.STATUS = status_protokoll.id\nJOIN stamm.status_kombi ON status_protokoll.status_kombi = stamm.status_kombi.id\nJOIN stamm.status_wert ON stamm.status_wert.id = stamm.status_kombi.wert_id\nJOIN stamm.status_stufe ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nLEFT JOIN public.lada_messwert ON (public.lada_messwert.messungs_id = messung.id)\nLEFT JOIN stamm.messgroesse ON (messgroesse.id = public.lada_messwert.messgroesse_id)\nLEFT JOIN stamm.mess_einheit ON (mess_einheit.id = public.lada_messwert.meh_id)\nLEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\nLEFT JOIN (\n  SELECT probe.id,\n    array_to_string(array_agg(stamm.proben_zusatz.beschreibung || ': ' || translate(to_char(land.zusatz_wert.messwert_pzs,'9999.9'),'. ',',') || ' ' || pzsmeh.einheit), ' # ', '') AS pzs\n  FROM land.probe\n  LEFT JOIN land.zusatz_wert ON (\n      land.probe.id = land.zusatz_wert.probe_id\n      AND land.zusatz_wert.pzs_id = 'A25'\n      )\n  LEFT JOIN stamm.proben_zusatz ON (land.zusatz_wert.pzs_id = stamm.proben_zusatz.id)\n  LEFT JOIN stamm.mess_einheit AS pzsmeh ON (stamm.proben_zusatz.meh_id = pzsmeh.id)\n  GROUP BY probe.id\n  ) pzs ON probe.id = pzs.id\nLEFT JOIN (\n  SELECT land.probe.id,\n    string_agg(land.kommentar_p.TEXT, ' # ') AS pkommentar\n  FROM land.probe\n  LEFT JOIN land.kommentar_p ON land.probe.id = land.kommentar_p.probe_id\n  WHERE NOT kommentar_p.TEXT LIKE '(X)%'\n  GROUP BY probe.id\n  ) pkommentar ON land.probe.id = pkommentar.id\nLEFT JOIN (\n  SELECT land.messung.id,\n    string_agg(land.kommentar_m.TEXT, ' # ') AS mkommentar\n  FROM land.messung\n  LEFT JOIN land.kommentar_m ON land.messung.id = land.kommentar_m.messungs_id\n  WHERE NOT kommentar_m.TEXT LIKE '(X)%'\n  GROUP BY messung.id\n  ) mkommentar ON land.messung.id = mkommentar.id
43	SELECT messung.id AS messungId,\n  probe.id AS probeId,\n  probe.hauptproben_nr AS hpNr,\n  messung.nebenproben_nr AS npNr,\n  status_protokoll.datum AS statusD,\n  datenbasis.datenbasis AS dBasis,\n  mess_stelle.netzbetreiber_id AS netzId,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN probe.mst_id\n    ELSE probe.mst_id || '-' || probe.labor_mst_id\n    END AS mstLaborId,\n  probe.umw_id AS umwId,\n  probenart.probenart AS pArt,\n  probe.probeentnahme_beginn AS peBegin,\n  probe.probeentnahme_ende AS peEnd,\n  ort.gem_id AS eGemId,\n  verwaltungseinheit.bezeichnung AS eGem,\n  messgroesse.messgroesse AS messgroesse,\n    CASE\n WHEN (tm_fm_umrechnung.faktor IS NOT NULL) OR (lada_messwert.meh_id = stamm.umwelt.meh_id) THEN coalesce(public.lada_messwert.messwert_nwg, ' ')\n ELSE NULL\n END AS nwg,\n      CASE\n WHEN tm_fm_umrechnung.faktor IS NOT NULL THEN tm_fm_umrechnung.faktor*lada_messwert.messwert\n WHEN lada_messwert.meh_id = stamm.umwelt.meh_id THEN lada_messwert.messwert\n ELSE NULL\n END AS wert,\n      CASE\n WHEN tm_fm_umrechnung.faktor IS NOT NULL THEN tm_fm_umrechnung.faktor*lada_messwert.nwg_zu_messwert\n WHEN lada_messwert.meh_id = stamm.umwelt.meh_id THEN lada_messwert.nwg_zu_messwert\n ELSE NULL\n END AS nwgZuMesswert,\n        CASE WHEN (tm_fm_umrechnung.faktor IS NOT NULL) OR (lada_messwert.meh_id = stamm.umwelt.meh_id) THEN\n  public.lada_messwert.messfehler ELSE NULL\n  END AS fehler,\n     CASE \n WHEN tm_fm_umrechnung.faktor IS NOT NULL THEN mess_einheit_tm.einheit\n WHEN lada_messwert.meh_id = stamm.umwelt.meh_id THEN mess_einheit.einheit ELSE NULL\n END AS einheit, \n  messung.mmt_id AS mmtId,\n  probe.ext_id AS externeProbeId,\n  messung.ext_id AS externeMessungsId,\n  status_kombi.id AS statusK,\n  umwelt.umwelt_bereich AS umw,\n  netz_betreiber.netzbetreiber AS netzbetreiber,\n  public.st_asgeojson(ort.geom) AS entnahmeGeom,\n  kta_gruppe.kta_gruppe AS anlage,\n  kta_gruppe.beschreibung AS anlagebeschr,\n  rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n  rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr,\n  probe.mpr_id AS mprId,\n  messprogramm_kategorie.code AS mplCode,\n  messprogramm_kategorie.bezeichnung AS mpl,\n  probe.test,\n  betriebsart.name AS messRegime,\n  CASE \n    WHEN probe.mst_id = probe.labor_mst_id\n      THEN stamm.mess_stelle.mess_stelle\n    ELSE stamm.mess_stelle.mess_stelle || '-' || labormessstelle.mess_stelle\n    END AS mstLabor,\n  messung.geplant,\n  probe.solldatum_beginn AS sollBegin,\n  probe.solldatum_ende AS sollEnd,\n  stamm.probenehmer.bezeichnung AS prnBezeichnung,\n  stamm.probenehmer.prn_id AS prnId,\n  stamm.probenehmer.kurz_bezeichnung AS prnKurzBezeichnung,\n stamm.mess_methode.messmethode AS mmt,\n land.messung.messzeitpunkt AS messbeginn,\n  land.messung.messdauer,\n    ort.kurztext AS ortKurztext,\n ort.langtext AS ortLangtext,\n probe.media_desk AS deskriptoren,\n probe.media AS medium,\n ortszuordnung.oz_id AS ozId,\n  stamm.ortszusatz.ortszusatz AS oz,\n  verwaltungseinheit.kreis AS eKreisId,\n  verwaltungseinheit.bundesland AS eBlId,\n  stamm.staat.staat AS eStaat,\n      CASE \n WHEN tm_fm_umrechnung.faktor IS NOT NULL THEN tm_fm_umrechnung.faktor*coalesce(lada_messwert.messwert,lada_messwert.nwg_zu_messwert)\n WHEN lada_messwert.meh_id = stamm.umwelt.meh_id THEN coalesce(lada_messwert.messwert,lada_messwert.nwg_zu_messwert)\n ELSE NULL\n END AS wertNwg,\n  ort.ort_id AS ortId,\n  staat_uo.staat AS uStaat,\n ort_uo.ort_id AS uOrtId,\n probe.ursprungszeit AS uZeit,\n array_to_string(tags.tags, ',', '') AS tags,\n     CASE \n WHEN lada_messwert.meh_id = stamm.umwelt.meh_id_2\n THEN\n CASE\n WHEN lada_messwert.messwert_nwg = '<'\n THEN lada_messwert.messwert_nwg || to_char(lada_messwert.nwg_zu_messwert, '0.99eeee') || ' ' || mess_einheit.einheit\n ELSE to_char(lada_messwert.messwert, '0.99eeee') || ' ' || mess_einheit.einheit \n END\n ELSE ' '\n END AS kombiSekMeh,\n      CASE\n WHEN lada_messwert.meh_id = stamm.umwelt.meh_id_2\n THEN true\n ELSE false\n END AS booleanSekMeh,\n tm_fm_umrechnung.faktor AS faktorSekMeh,\n lada_messwert.id\n  FROM land.probe\nLEFT JOIN stamm.mess_stelle ON (probe.mst_id = stamm.mess_stelle.id)\nLEFT JOIN stamm.mess_stelle AS labormessstelle ON (probe.labor_mst_id = labormessstelle.id)\nJOIN land.messung ON probe.id = messung.probe_id\nJOIN land.status_protokoll ON messung.STATUS = status_protokoll.id\nJOIN stamm.status_kombi ON status_protokoll.status_kombi = stamm.status_kombi.id\nJOIN stamm.status_wert ON stamm.status_wert.id = stamm.status_kombi.wert_id\nJOIN stamm.status_stufe ON stamm.status_stufe.id = stamm.status_kombi.stufe_id\nLEFT JOIN stamm.datenbasis ON (probe.datenbasis_id = datenbasis.id)\nLEFT JOIN stamm.probenart ON (probe.probenart_id = probenart.id)\nLEFT JOIN land.ortszuordnung ON (\n    probe.id = ortszuordnung.probe_id\n    AND ortszuordnung.ortszuordnung_typ IN ('E', 'R')\n    )\nLEFT JOIN stamm.ort ON (ortszuordnung.ort_id = ort.id)\nLEFT JOIN stamm.verwaltungseinheit ON (ort.gem_id = verwaltungseinheit.id)\nLEFT JOIN stamm.ortszusatz\n  ON (ortszuordnung.oz_id = ortszusatz.ozs_id)\nLEFT JOIN stamm.staat ON (ort.staat_id = staat.id)\nLEFT JOIN land.ortszuordnung AS ortszuordnung_uo ON (probe.id = ortszuordnung_uo.probe_id AND ortszuordnung_uo.ortszuordnung_typ IN ('U'))\n LEFT JOIN stamm.ort AS ort_uo ON (ortszuordnung_uo.ort_id = ort_uo.id)\n LEFT JOIN stamm.staat AS staat_uo ON (ort_uo.staat_id = staat_uo.id\n)LEFT JOIN stamm.umwelt ON (probe.umw_id = umwelt.id)\nLEFT JOIN stamm.netz_betreiber ON (stamm.mess_stelle.netzbetreiber_id = netz_betreiber.id)\nLEFT JOIN stamm.kta_gruppe ON (probe.kta_gruppe_id = stamm.kta_gruppe.id)\nLEFT JOIN stamm.rei_progpunkt_gruppe ON (probe.rei_progpunkt_grp_id = stamm.rei_progpunkt_gruppe.id)\nLEFT JOIN stamm.messprogramm_kategorie ON (probe.mpl_id = messprogramm_kategorie.id)\nLEFT JOIN stamm.betriebsart ON (probe.ba_id = stamm.betriebsart.id)\nJOIN public.lada_messwert ON (public.lada_messwert.messungs_id = messung.id)\nJOIN stamm.messgroesse ON (messgroesse.id = public.lada_messwert.messgroesse_id)\nLEFT JOIN stamm.mess_einheit ON (mess_einheit.id = public.lada_messwert.meh_id)\n LEFT JOIN stamm.probenehmer ON (land.probe.probe_nehmer_id = stamm.probenehmer.id)\n LEFT JOIN stamm.mess_methode ON (land.messung.mmt_id = stamm.mess_methode.id)\n LEFT JOIN (\n SELECT probe.id AS pid, messung.id AS mid,\n array_agg(tag.tag) AS tags\n FROM land.probe\n INNER JOIN land.messung ON probe.id = messung.probe_id\n JOIN land.tagzuordnung ON (probe.id = tagzuordnung.probe_id or messung.id = tagzuordnung.messung_id)\n JOIN stamm.tag ON tagzuordnung.tag_id = tag.id\n GROUP BY probe.id, messung.id\n) tags ON (probe.id = tags.pid and messung.id = tags.mid)\n LEFT JOIN stamm.tm_fm_umrechnung ON (lada_messwert.meh_id = tm_fm_umrechnung.meh_id AND probe.umw_id = tm_fm_umrechnung.umw_id AND probe.media_desk LIKE tm_fm_umrechnung.media_desk_pattern)\n LEFT JOIN stamm.mess_einheit AS mess_einheit_tm ON (tm_fm_umrechnung.meh_id_nach = mess_einheit_tm.id)
51	SELECT deskriptor_umwelt.umw_id AS umwId,\n umwelt.umwelt_bereich AS umw,\n s00.sn AS s00Sn,\n s00.beschreibung AS s00Beschr,\n s01.sn AS s01Sn,\n s01.beschreibung AS s01Beschr,\n s02.sn AS s02Sn,\n s02.beschreibung AS s02Beschr,\n s03.sn AS s03Sn,\n s03.beschreibung AS s03Beschr,\n s04.sn AS s04Sn,\n s04.beschreibung AS s04Beschr,\n s05.sn AS s05Sn,\n s05.beschreibung AS s05Beschr,\n s06.sn AS s06Sn,\n s06.beschreibung AS s06Beschr,\n s07.sn AS s07Sn,\n s07.beschreibung AS s07Beschr,\n s08.sn AS s08Sn,\n s08.beschreibung AS s08Beschr,\n s09.sn AS s09Sn,\n s09.beschreibung AS s09Beschr,\n s10.sn AS s10Sn,\n s10.beschreibung AS s10Beschr,\n s11.sn AS s11Sn,\n s11.beschreibung AS s11Beschr\n FROM stamm.deskriptor_umwelt\n LEFT JOIN stamm.umwelt ON (stamm.deskriptor_umwelt.umw_id = stamm.umwelt.id)\n LEFT JOIN stamm.deskriptoren AS s00 ON (stamm.deskriptor_umwelt.s00 = s00.id)\n LEFT JOIN stamm.deskriptoren AS s01 ON (stamm.deskriptor_umwelt.s01 = s01.id)\n LEFT JOIN stamm.deskriptoren AS s02 ON (stamm.deskriptor_umwelt.s02 = s02.id)\n LEFT JOIN stamm.deskriptoren AS s03 ON (stamm.deskriptor_umwelt.s03 = s03.id)\n LEFT JOIN stamm.deskriptoren AS s04 ON (stamm.deskriptor_umwelt.s04 = s04.id)\n LEFT JOIN stamm.deskriptoren AS s05 ON (stamm.deskriptor_umwelt.s05 = s05.id)\n LEFT JOIN stamm.deskriptoren AS s06 ON (stamm.deskriptor_umwelt.s06 = s06.id)\n LEFT JOIN stamm.deskriptoren AS s07 ON (stamm.deskriptor_umwelt.s07 = s07.id)\n LEFT JOIN stamm.deskriptoren AS s08 ON (stamm.deskriptor_umwelt.s08 = s08.id)\n LEFT JOIN stamm.deskriptoren AS s09 ON (stamm.deskriptor_umwelt.s09 = s09.id)\n LEFT JOIN stamm.deskriptoren AS s10 ON (stamm.deskriptor_umwelt.s10 = s10.id)\n LEFT JOIN stamm.deskriptoren AS s11 ON (stamm.deskriptor_umwelt.s11 = s11.id)\n
52	SELECT deskriptoren.id AS deskId,\n deskriptoren.ebene AS deskEbene,\n deskriptoren.sn AS deskSn,\n deskriptoren.beschreibung AS deskBeschr,\n deskriptoren.bedeutung AS deskBedeutung,\n deskriptoren.vorgaenger AS deskVorgId,\n deskriptorenvorgaenger.ebene AS deskVorgEbene,\n deskriptorenvorgaenger.sn AS deskVorgSN,\n deskriptorenvorgaenger.beschreibung AS deskVorgBeschr\n FROM stamm.deskriptoren\n LEFT JOIN stamm.deskriptoren AS deskriptorenvorgaenger ON (deskriptoren.vorgaenger = deskriptorenvorgaenger.id)
53	SELECT verwaltungseinheit.id AS verwId,\n verwaltungseinheit.bezeichnung AS verwBez,\n verwaltungseinheit.plz AS plz,\n verwaltungseinheit.is_gemeinde AS isGem,\n verwaltungseinheit.is_landkreis AS isKreis,\n verwaltungseinheit.is_regbezirk AS isRbez,\n verwaltungseinheit.is_bundesland AS isBundesland,\n verwaltungseinheit.bundesland AS bundeslandId,\n bl.bezeichnung AS bundesland,\n verwaltungseinheit.regbezirk AS rbezId,\n rb.bezeichnung AS rbez,\n verwaltungseinheit.kreis AS kreisId,\n lk.bezeichnung AS kreis\n FROM stamm.verwaltungseinheit\n LEFT JOIN stamm.verwaltungseinheit AS bl ON (stamm.verwaltungseinheit.bundesland = bl.id)\n LEFT JOIN stamm.verwaltungseinheit AS rb ON (stamm.verwaltungseinheit.regbezirk = rb.id)\n LEFT JOIN stamm.verwaltungseinheit AS lk ON (stamm.verwaltungseinheit.kreis = lk.id)
54	SELECT kta_gruppe.id AS anlageId,\n kta_gruppe.kta_gruppe AS anlageBez,\n kta_gruppe.beschreibung AS anlageBeschr\n FROM stamm.kta_gruppe
55	SELECT staat.id AS staatId,\n  staat.hkl_id AS hklId, staat.staat AS staatBez,\n staat.staat_iso AS staatIso,\n staat.eu AS staatEu,\n koordinaten_art.koordinatenart AS kda,\n staat.koord_x_extern AS staatKoordX,\n staat.koord_y_extern AS staatKoordY\n FROM stamm.staat\n LEFT JOIN stamm.koordinaten_art ON (staat.kda_id = koordinaten_art.id)
56	SELECT ortszusatz.ozs_id AS ozId,\n ortszusatz.ortszusatz AS oz\n FROM stamm.ortszusatz
60	SELECT netz_betreiber.id AS netzId,\n netz_betreiber.netzbetreiber AS netzbetreiber,\n netz_betreiber.idf_netzbetreiber AS netzIdf,\n netz_betreiber.is_bmn AS netzBmn\n FROM stamm.netz_betreiber
61	SELECT proben_zusatz.id AS pzsId,\n proben_zusatz.zusatzwert AS pzsBez,\n proben_zusatz.beschreibung AS pzsBeschr,\n mess_einheit.einheit AS pzsEinheit,\n proben_zusatz.eudf_keyword AS pzsEudf\n FROM stamm.proben_zusatz\n LEFT JOIN stamm.mess_einheit ON (proben_zusatz.meh_id = mess_einheit.id)
62	SELECT rei_progpunkt_gruppe.id AS reiproggrpId,\n rei_progpunkt_gruppe.rei_prog_punkt_gruppe AS reiproggrp,\n rei_progpunkt_gruppe.beschreibung AS reiproggrpbeschr\n FROM stamm.rei_progpunkt_gruppe
63	SELECT tag.id AS tagId,\n tag.tag,\n tag.mst_id AS mstId,\n tag.netzbetreiber_id AS netzId,\n lada_user.name AS username,\n tag.tag_typ AS tagTyp,\n tag.gueltig_bis AS gueltigBis,\n tag.created_at AS createdAt\n FROM stamm.tag\n LEFT JOIN stamm.lada_user ON tag.user_id = lada_user.id
\.


--
-- Data for Name: filter; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.filter (id, sql, param, filter_type_id, name) FROM stdin;
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
97	stamm.netz_betreiber.netzbetreiber ~ :netzbetreiber	netzbetreiber	0	netzbetreiber_text
98	proben_zusatz.beschreibung ~ :pzsBeschr	pzsBeschr	0	pzsBeschr_text
99	rei_progpunkt_gruppe.beschreibung ~ :reiproggrpbeschr	reiproggrpbeschr	0	reiproggrpbeschr_text
100	probe.kta_gruppe_id IN ( :anlage )	anlage	5	kta_gruppe_probe
101	probe.rei_progpunkt_grp_id IN ( :reiproggrp )	reiproggrp	5	rei_prog_grp_probe
102	probe.ba_id IN ( :messRegime )	messRegime	5	messRegime_probe
103	messung.mmt_id IN ( :mmtId )	mmtId	4	mmt_id_messung
104	probe.ursprungszeit BETWEEN to_timestamp(cast(:fromUZeit AS DOUBLE PRECISION)) AND to_timestamp(cast(:toUZeit AS DOUBLE PRECISION))	fromUZeit,toUZeit	6	uZeit
105	status_protokoll.status_kombi IN ( :statusK )	statusK	5	statusK
106	probe.mpl_id IN ( :mplcode )	mplcode	5	mplcode
107	probe.probe_nehmer_id IN ( :prnId )	prnId	5	prn_id
108	messprogramm.kta_gruppe_id IN ( :anlage )	anlage	5	kta_gruppe_mpr
109	ort.kta_gruppe_id IN ( :anlage )	anlage	5	kta_gruppe_ort
110	messprogramm.ba_id IN ( :messRegime )	messRegime	5	messRegime_mpr
111	messprogramm.mpl_id IN ( :mplcode )	mplcode	5	mplcode_mpr
112	messprogramm.rei_progpunkt_grp_id IN ( :reiproggrp )	reiproggrp	5	rei_prog_grp_mpr
113	messprogramm.probe_nehmer_id IN ( :prnId )	prnId	5	prn_id_mpr
114	ort.kurztext ~ :ortKurztext	ortKurztext	0	ortKurztext
115	ort.Langtext ~ :ortLangtext	ortLangtext	0	ortLangtext
116	probe.media ~ :medium	medium	0	medium
117	probe.media_desk ~ :deskriptoren	deskriptoren	0	deskriptoren
118	messprogramm.kommentar ~ :mprKommentar	mprKommentar	0	mprKommentar
119	messprogramm.probe_kommentar ~ :mprPKommentar	mprPKommentar	0	mprPKommentar
120	probenehmer.plz ~ :plz	plz	0	plz
121	pzs ~ :pzs	pzs	0	pzs
130	tag.netzbetreiber IN ( :netzId )	netzId	4	Netzbetreiber
131	tag.mst_id IN ( :mstId )	mstId	4	Messstelle
132	tag.typ IN ( :typ )	typ	4	Typ
\.


--
-- Data for Name: result_type; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.disp (id, name, format) FROM stdin;
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
43	tagId	\N
44	tagTyp	\N
\.


--
-- Data for Name: grid_column; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.grid_col_mp (id, base_query_id, grid_col, data_index, "position", filter_id, disp_id) FROM stdin;
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
117	1	MPL-ID	mplCode	17	\N	1
118	1	Messprogramm-Land	mpl	18	106	27
119	1	Umweltbereich	umw	19	4	12
120	1	Netzbetreiber	netzbetreiber	20	13	18
121	1	MST/Labor	mstLabor	21	3	10
122	1	Anlage	anlage	22	100	25
123	1	Anlage-Beschr	anlagebeschr	23	\N	25
124	1	REI-Prog-GRP	reiproggrp	24	101	26
125	1	REI-Prog-GRP-Beschr	reiproggrpbeschr	25	\N	26
126	1	Test	test	26	5	11
127	1	Messregime	messRegime	27	102	28
128	1	Ort-Kurztext	ortKurztext	28	114	1
129	1	Ort-Langtext	ortLangtext	29	115	1
130	1	Deskriptoren	deskriptoren	30	\N	1
131	1	Medium	medium	31	116	1
132	1	PRN-Bezeichnung	prnBezeichnung	32	\N	1
133	1	PRN	prnId	33	107	31
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
145	1	Ursprungszeit	uZeit	45	104	2
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
1139	11	Status	statusK	39	105	24
1140	11	Umweltbereich	umw	40	4	12
1141	11	Netzbetreiber	netzbetreiber	41	13	18
1142	11	E_GEOM	entnahmeGeom	42	\N	7
1143	11	Anlage	anlage	43	100	25
1144	11	Anlage-Beschr	anlagebeschr	44	\N	25
1145	11	REI-Prog-GRP	reiproggrp	45	101	26
1146	11	REI-Prog-GRP-Beschr	reiproggrpbeschr	46	\N	26
1147	11	MPR-ID	mprId	47	62	8
1148	11	MPL-ID	mplCode	48	\N	1
1149	11	Messprogramm-Land	mpl	49	106	27
1150	11	Test	test	50	5	11
1151	11	Messregime	messRegime	51	102	28
1152	11	MST/Labor	mstLabor	52	3	10
1153	11	geplant	geplant	53	43	11
1154	11	Solldatum Beginn	sollBegin	54	20	2
1155	11	Solldatum Ende	sollEnd	55	21	2
1156	11	Ort-ID	ortId	56	11	1
1157	11	Ort-Kurztext	ortKurztext	57	114	1
1158	11	Ort-Langtext	ortLangtext	58	115	1
1159	11	PRN-Bezeichnung	prnBezeichnung	59	\N	1
1160	11	PRN	prnId	60	107	31
1161	11	PRN-Kurzbezeichnung	prnKurzBezeichnung	61	\N	1
1162	11	MMT	mmt	62	103	32
1163	11	Messbeginn	messbeginn	63	47	2
1164	11	Messdauer (s)	messdauer	64	\N	33
1165	11	Deskriptoren	deskriptoren	65	\N	1
1166	11	Medium	medium	66	116	1
1167	11	OZ-ID	ozId	67	\N	1
1168	11	Ortszusatz	oz	68	\N	1
1169	11	E-LK-ID	eKreisId	69	53	34
1170	11	E-BL-ID	eBlId	70	54	35
1171	11	E-Staat	eStaat	71	55	20
1172	11	Fertig	fertig	72	56	11
1173	11	hat Rückfrage	hatRueckfrage	73	61	11
1174	11	U-Staat	uStaat	74	58	20
1175	11	U-Ort-ID	uOrtId	75	87	1
1176	11	Ursprungszeit	uZeit	76	104	2
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
1218	12	Messprogramm-Land	mpl	18	106	27
1219	12	Umweltbereich	umw	19	4	12
1220	12	Netzbetreiber	netzbetreiber	20	13	18
1221	12	MST/Labor	mstLabor	21	3	10
1222	12	Anlage	anlage	22	100	25
1223	12	Anlage-Beschr	anlagebeschr	23	\N	25
1224	12	REI-Prog-GRP	reiproggrp	24	101	26
1225	12	REI-Prog-GRP-Beschr	reiproggrpbeschr	25	\N	26
1226	12	Test	test	26	5	11
1227	12	Messregime	messRegime	27	102	28
1228	12	geplant	geplant	28	43	11
1229	12	Messbeginn	messbeginn	29	47	2
1230	12	MMT-ID	mmtId	30	\N	32
1231	12	MMT	mmt	31	103	32
1232	12	PRN-Bezeichnung	prnBezeichnung	32	\N	1
1233	12	Deskriptoren	deskriptoren	33	\N	1
1234	12	Medium	medium	34	116	1
1235	12	Proben Kommentare	pKommentar	35	\N	1
1236	12	PZB	pzs	36	\N	1
1237	12	OZ-ID	ozId	37	\N	1
1238	12	U-Staat	uStaat	38	58	20
1239	12	Status	statusK	39	105	24
1240	12	PRN	prnId	40	107	31
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
1257	12	Ort-Kurztext	ortKurztext	57	114	1
1258	12	Ort-Langtext	ortLangtext	58	115	1
1259	12	E-Landkreis	eKreis	59	\N	16
1260	12	E-Regbezirk	eRbez	60	\N	16
1261	12	MST-Beschr	mstBeschr	61	\N	1
1262	12	Deskriptor S2	desk2Beschr	62	\N	1
1263	12	Tags	tags	63	59	38
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
1539	15	Status	statusK	39	105	24
1540	15	Umweltbereich	umw	40	4	12
1541	15	Netzbetreiber	netzbetreiber	41	13	18
1542	15	E_GEOM	entnahmeGeom	42	\N	7
1543	15	MPR-ID	mprId	43	62	8
1544	15	MPL-ID	mplCode	44	\N	1
1545	15	Messprogramm-Land	mpl	45	106	27
1546	15	Test	test	46	5	11
1547	15	Messregime	messRegime	47	102	28
1548	15	MST/Labor	mstLabor	48	3	10
1549	15	geplant	geplant	49	43	11
1550	15	Solldatum Beginn	sollBegin	50	20	2
1551	15	Solldatum Ende	sollEnd	51	21	2
1552	15	Ort-ID	ortId	52	11	1
1553	15	Ort-Kurztext	ortKurztext	53	114	1
1554	15	Ort-Langtext	ortLangtext	54	115	1
1555	15	MMT	mmt	55	103	32
1556	15	Messbeginn	messbeginn	56	47	2
1557	15	Messdauer (s)	messdauer	57	\N	33
1558	15	Deskriptoren	deskriptoren	58	117	1
1559	15	Medium	medium	59	116	1
1560	15	OZ-ID	ozId	60	\N	1
1561	15	Ortszusatz	oz	61	\N	1
1562	15	E-LK-ID	eKreisId	62	53	34
1563	15	E-BL-ID	eBlId	63	54	35
1564	15	E-Staat	eStaat	64	55	20
1565	15	Fertig	fertig	65	56	11
1566	15	U-Staat	uStaat	66	58	20
1567	15	U-Ort-ID	uOrtId	67	87	1
1568	15	Ursprungszeit	uZeit	68	104	2
1569	15	Tags	tags	69	59	38
1570	15	Proben Kommentare	pKommentar	70	\N	1
1571	15	Messung Kommentare	mKommentar	71	\N	1
1572	15	Deskriptor S0	desk0Beschr	72	\N	1
1573	15	Deskriptor S1	desk1Beschr	73	\N	1
1574	15	Deskriptor S2	desk2Beschr	74	\N	1
1575	15	Deskriptor S3	desk3Beschr	75	\N	1
1576	15	Deskriptor S4	desk4Beschr	76	\N	1
1601	16	interne MID	id	1	\N	5
1602	16	interne PID	probeId	2	\N	4
1603	16	HP-Nr	hpNr	3	2	1
1604	16	NP-Nr	npNr	4	22	1
1605	16	Status Datum	statusD	5	88	2
1606	16	Datenbasis	dBasis	6	8	17
1607	16	Netz-ID	netzId	7	\N	18
1608	16	MST/Labor-ID	mstLaborId	8	\N	10
1609	16	Umw-ID	umwId	9	\N	12
1610	16	Probenart	pArt	10	9	19
1611	16	Probenahme Beginn	peBegin	11	14	2
1612	16	Probenahme Ende	peEnd	12	15	2
1613	16	E-Gem-ID	eGemId	13	\N	16
1614	16	E-Gemeinde	eGem	14	10	16
1615	16	H-3	h3	15	\N	1
1616	16	K-40	k40	16	\N	1
1617	16	Co-60	co60	17	\N	1
1618	16	Sr-89	sr89	18	\N	1
1619	16	Sr-90	sr90	19	\N	1
1620	16	Ru-103	ru103	20	\N	1
1621	16	I-131	i131	21	\N	1
1622	16	Cs-134	cs134	22	\N	1
1623	16	Cs-137	cs137	23	\N	1
1624	16	Ce-144	ce144	24	\N	1
1625	16	U-234	u234	25	\N	1
1626	16	U-235	u235	26	\N	1
1627	16	U-238	u238	27	\N	1
1628	16	Pu-238	pu238	28	\N	1
1629	16	Pu-239	pu239	29	\N	1
1630	16	Pu-239/240	pu23940	30	\N	1
1631	16	Te-132	te132	31	\N	1
1632	16	Pb-212	pb212	32	\N	1
1633	16	Pb-214	pb214	33	\N	1
1634	16	Bi-212	bi212	34	\N	1
1635	16	Bi-214	bi214	35	\N	1
1636	16	MMT-ID	mmtId	36	\N	1
1637	16	externe PID	externeProbeId	37	1	1
1638	16	externe MID	externeMessungsId	38	\N	1
1639	16	Status	statusK	39	105	24
1640	16	Umweltbereich	umw	40	4	12
1641	16	Netzbetreiber	netzbetreiber	41	13	18
1642	16	E_GEOM	entnahmeGeom	42	\N	7
1643	16	Anlage	anlage	43	100	25
1644	16	Anlage-Beschr	anlagebeschr	44	\N	25
1645	16	REI-Prog-GRP	reiproggrp	45	101	26
1646	16	REI-Prog-GRP-Beschr	reiproggrpbeschr	46	\N	26
1647	16	MPR-ID	mprId	47	62	8
1648	16	MPL-ID	mplCode	48	\N	1
1649	16	Messprogramm-Land	mpl	49	106	27
1650	16	Test	test	50	5	11
1651	16	Messregime	messRegime	51	102	28
1652	16	MST/Labor	mstLabor	52	3	10
1653	16	geplant	geplant	53	43	11
1654	16	Solldatum Beginn	sollBegin	54	20	2
1655	16	Solldatum Ende	sollEnd	55	21	2
1656	16	Ort-ID	ortId	56	11	1
1657	16	Ort-Kurztext	ortKurztext	57	114	1
1658	16	Ort-Langtext	ortLangtext	58	115	1
1659	16	PRN-Bezeichnung	prnBezeichnung	59	\N	1
1660	16	PRN	prnId	60	107	31
1661	16	PRN-Kurzbezeichnung	prnKurzBezeichnung	61	\N	1
1662	16	MMT	mmt	62	103	32
1663	16	Messbeginn	messbeginn	63	47	2
1664	16	Messdauer (s)	messdauer	64	\N	33
1665	16	Deskriptoren	deskriptoren	65	\N	1
1666	16	Medium	medium	66	116	1
1667	16	OZ-ID	ozId	67	\N	1
1668	16	Ortszusatz	oz	68	\N	1
1669	16	E-LK-ID	eKreisId	69	53	34
1670	16	E-BL-ID	eBlId	70	54	35
1671	16	E-Staat	eStaat	71	55	20
1672	16	Fertig	fertig	72	56	11
1673	16	hat Rückfrage	hatRueckfrage	73	61	11
1674	16	U-Staat	uStaat	74	58	20
1675	16	U-Ort-ID	uOrtId	75	87	1
1676	16	Ursprungszeit	uZeit	76	104	2
1677	16	Tags	tags	77	59	38
1678	16	PZB	pzs	78	121	1
2101	21	MPR-ID	mpNr	1	63	8
2102	21	Netz-ID	netzId	2	\N	18
2103	21	MST/Labor-ID	mstLaborId	3	\N	10
2104	21	Datenbasis	dBasis	4	35	17
2105	21	Messregime	messRegime	5	110	28
2106	21	Probenart	pArt	6	36	19
2107	21	Umw-ID	umwId	7	\N	12
2108	21	Deskriptoren	deskriptoren	8	\N	1
2109	21	Probenintervall	intervall	9	\N	1
2110	21	Ort-ID	ortId	10	11	1
2111	21	E-Gem-ID	eGemId	11	\N	16
2112	21	E-Gemeinde	eGem	12	10	16
2113	21	MPL-ID	mplCode	13	\N	1
2114	21	Messprogramm-Land	mpl	14	111	27
2115	21	MST/Labor	mstLabor	15	27	10
2116	21	Aktiv	aktiv	16	31	11
2117	21	Netzbetreiber	netzbetreiber	17	13	18
2118	21	Umweltbereich	umw	18	37	12
2119	21	Anlage	anlage	19	108	25
2120	21	Anlage-Beschr	anlagebeschr	20	\N	25
2121	21	REI-Prog-GRP	reiproggrp	21	112	26
2122	21	REI-Prog-GRP-Beschr	reiproggrpbeschr	22	\N	26
2123	21	Test	test	23	40	11
2124	21	Ort-Kurztext	ortKurztext	24	114	1
2125	21	Ort-Langtext	ortLangtext	25	115	1
2126	21	U-Ort-ID	uOrtId	26	87	1
2127	21	MPR-Kommentar	mprKommentar	27	118	1
2128	21	Probenkommentar	mprPKommentar	28	119	1
2129	21	PRN-Bezeichnung	prnBezeichnung	29	\N	1
2130	21	PRN	prnId	30	113	31
2131	21	PRN-Kurzbezeichnung	prnKurzBezeichnung	31	\N	1
3101	31	ID	id	1	\N	6
3102	31	Netz-ID	netzId	2	\N	18
3103	31	Ort-ID	ortId	3	11	1
3104	31	Ortklassifizierung	ortTyp	4	\N	1
3105	31	Kurztext	kurztext	5	114	1
3106	31	Langtext	langtext	6	115	1
3107	31	Staat	staat	7	55	20
3108	31	Verwaltungseinheit	verwaltungseinheit	8	10	16
3110	31	OZ-ID	ozId	9	\N	1
3111	31	Anlage	anlage	10	109	25
3112	31	mpArt	mpArt	11	89	1
3113	31	Koordinatenart	koordinatenArt	12	\N	1
3114	31	X-Koordinate	koordXExtern	13	\N	1
3115	31	Y-Koordinate	koordYExtern	14	\N	1
3116	31	Longitude	longitude	15	\N	39
3117	31	Latitude	latitude	16	\N	39
3118	31	Höhe über NN	hoeheUeberNn	17	\N	39
3119	31	Höhe	hoeheLand	18	\N	39
3120	31	Aktiv (REI)	ortAktiv	19	52	11
3121	31	letzte Änderung	letzteAenderung	20	\N	2
3122	31	Zone	zone	21	\N	1
3123	31	Sektor	sektor	22	\N	1
3124	31	Zustaendigkeit	zustaendigkeit	23	\N	1
3125	31	Berichtstext	berichtstext	24	\N	1
3126	31	unscharf	unscharf	25	\N	11
3127	31	Netzbetreiber	netzbetreiber	26	23	18
3128	31	Anlage-Beschr	anlagebeschr	27	\N	25
3129	31	Verw-ID	verwid	28	\N	16
3130	31	GEOM	geom	29	\N	7
3201	32	ID	id	1	\N	21
3202	32	Netz-ID	netzId	2	\N	18
3203	32	PRN	prnId	3	41	1
3204	32	Bearbeiter	bearbeiter	4	\N	1
3205	32	Bemerkung	bemerkung	5	\N	1
3206	32	Betrieb	betrieb	6	\N	1
3207	32	Bezeichnung	bezeichnung	7	\N	1
3208	32	Kurzbezeichnung	kurzBezeichnung	8	\N	1
3209	32	Ort	ort	9	\N	1
3210	32	PLZ	plz	10	120	1
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
3802	38	Bezeichnung	mst	2	\N	10
3803	38	Netz-ID	netzId	3	\N	18
3804	38	Beschreibung	mstBeschreibung	4	\N	1
3805	38	Typ	mstTyp	5	\N	1
3806	38	Amtskennung	mstAmtskennung	6	\N	1
3807	38	Netzbetreiber	netzbetreiber	7	13	18
3901	39	ID	umwId	1	66	1
3902	39	Umweltbereich	umw	2	67	1
3903	39	Maßeinheit	me	3	\N	1
3904	39	Maßeinheit 2	me2	4	\N	1
3905	39	Beschreibung	umwBeschreibung	5	\N	1
4101	41	interne MID	messungId	1	\N	5
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
4124	41	Status	statusK	24	105	24
4125	41	Umweltbereich	umw	25	4	12
4126	41	Netzbetreiber	netzbetreiber	26	13	18
4127	41	E_GEOM	entnahmeGeom	27	\N	7
4128	41	Anlage	anlage	28	100	25
4129	41	Anlage-Beschr	anlagebeschr	29	\N	25
4130	41	REI-Prog-GRP	reiproggrp	30	39	26
4131	41	REI-Prog-GRP-Beschr	reiproggrpbeschr	31	\N	26
4132	41	MPR-ID	mprId	32	62	1
4133	41	MPL-ID	mplCode	33	\N	1
4134	41	Messprogramm-Land	mpl	34	106	27
4135	41	Test	test	35	5	11
4136	41	Messregime	messRegime	36	102	28
4137	41	MST/Labor	mstLabor	37	3	10
4138	41	geplant	geplant	38	43	11
4139	41	Solldatum Beginn	sollBegin	39	20	2
4140	41	Solldatum Ende	sollEnd	40	21	2
4141	41	PRN-Bezeichnung	prnBezeichnung	41	\N	1
4142	41	PRN	prnId	42	107	31
4143	41	PRN-Kurzbezeichnung	prnKurzBezeichnung	43	\N	1
4144	41	MMT	mmt	44	103	32
4145	41	Messbeginn	messbeginn	45	47	2
4146	41	Messdauer (s)	messdauer	46	\N	33
4147	41	Ort-Kurztext	ortKurztext	47	114	1
4148	41	Ort-Langtext	ortLangtext	48	115	1
4149	41	Deskriptoren	deskriptoren	49	\N	1
4150	41	Medium	medium	50	116	1
4151	41	OZ-ID	ozId	51	\N	1
4152	41	Ortszusatz	oz	52	\N	1
4153	41	E-LK-ID	eKreisId	53	53	34
4154	41	E-BL-ID	eBlId	54	54	35
4155	41	E-Staat	eStaat	55	55	20
4156	41	MW/NWG	wertNwg	56	\N	15
4157	41	Ort-ID	ortId	57	11	1
4158	41	U-Staat	uStaat	58	58	20
4159	41	U-Ort-ID	uOrtId	59	87	1
4160	41	Ursprungszeit	uZeit	60	104	2
4161	41	Tags	tags	61	59	38
4162	41	interne MWID	id	62	\N	42
4201	42	interne MID	messungId	1	\N	1
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
4228	42	Anlage	anlage	28	100	25
4229	42	Anlage-Beschr	anlagebeschr	29	\N	25
4230	42	REI-Prog-GRP	reiproggrp	30	101	26
4231	42	REI-Prog-GRP-Beschr	reiproggrpbeschr	31	\N	26
4232	42	Berichtstext	berichtstext	32	\N	1
4233	42	Test	test	33	5	11
4234	42	Messregime	messRegime	34	102	28
4235	42	MST/Labor	mstLabor	35	3	10
4236	42	geplant	geplant	36	43	11
4237	42	Solldatum Beginn	sollBegin	37	20	2
4238	42	Solldatum Ende	sollEnd	38	21	2
4239	42	MMT	mmt	39	103	32
4240	42	Messbeginn	messbeginn	40	47	2
4241	42	Messdauer (s)	messdauer	41	\N	33
4242	42	Ort-Kurztext	ortKurztext	42	114	1
4243	42	Ort-Langtext	ortLangtext	43	115	1
4244	42	Medium	medium	44	116	1
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
4257	42	interne MWID	id	57	\N	1
4301	43	interne MID	messungId	1	\N	5
4302	43	interne PID	probeId	2	\N	1
4303	43	HP-Nr	hpNr	3	2	1
4304	43	NP-Nr	npNr	4	22	1
4305	43	Status Datum	statusD	5	88	2
4306	43	Datenbasis	dBasis	6	8	17
4307	43	Netz-ID	netzId	7	\N	18
4308	43	MST/Labor-ID	mstLaborId	8	\N	10
4309	43	Umw-ID	umwId	9	\N	12
4310	43	Probenart	pArt	10	9	19
4311	43	Probenahme Beginn	peBegin	11	14	2
4312	43	Probenahme Ende	peEnd	12	15	2
4313	43	E-Gem-ID	eGemId	13	\N	16
4314	43	E-Gemeinde	eGem	14	10	16
4315	43	Messgröße	messgroesse	15	44	30
4316	43	< EG	nwg	16	\N	1
4317	43	Messwert	wert	17	\N	15
4318	43	NWG zur Messung	nwgZuMesswert	18	\N	15
4319	43	rel. Messunsicherh.(%)	fehler	19	\N	29
4320	43	Maßeinheit	einheit	20	\N	1
4321	43	MMT-ID	mmtId	21	\N	1
4322	43	externe PID	externeProbeId	22	1	1
4323	43	externe MID	externeMessungsId	23	\N	1
4324	43	Status	statusK	24	105	24
4325	43	Umweltbereich	umw	25	4	12
4326	43	Netzbetreiber	netzbetreiber	26	13	18
4327	43	E_GEOM	entnahmeGeom	27	\N	7
4328	43	Anlage	anlage	28	100	25
4329	43	Anlage-Beschr	anlagebeschr	29	\N	25
4330	43	REI-Prog-GRP	reiproggrp	30	101	26
4331	43	REI-Prog-GRP-Beschr	reiproggrpbeschr	31	\N	26
4332	43	MPR-ID	mprId	32	62	1
4333	43	MPL-ID	mplCode	33	\N	1
4334	43	Messprogramm-Land	mpl	34	106	27
4335	43	Test	test	35	5	11
4336	43	Messregime	messRegime	36	102	28
4337	43	MST/Labor	mstLabor	37	3	10
4338	43	geplant	geplant	38	43	11
4339	43	Solldatum Beginn	sollBegin	39	20	2
4340	43	Solldatum Ende	sollEnd	40	21	2
4341	43	PRN-Bezeichnung	prnBezeichnung	41	\N	1
4342	43	PRN	prnId	42	107	31
4343	43	PRN-Kurzbezeichnung	prnKurzBezeichnung	43	\N	1
4344	43	MMT	mmt	44	103	32
4345	43	Messbeginn	messbeginn	45	47	2
4346	43	Messdauer (s)	messdauer	46	\N	33
4347	43	Ort-Kurztext	ortKurztext	47	114	1
4348	43	Ort-Langtext	ortLangtext	48	115	1
4349	43	Deskriptoren	deskriptoren	49	\N	1
4350	43	Medium	medium	50	116	1
4351	43	OZ-ID	ozId	51	\N	1
4352	43	Ortszusatz	oz	52	\N	1
4353	43	E-LK-ID	eKreisId	53	53	34
4354	43	E-BL-ID	eBlId	54	54	35
4355	43	E-Staat	eStaat	55	55	20
4356	43	MW/NWG	wertNwg	56	\N	15
4357	43	Ort-ID	ortId	57	11	1
4358	43	U-Staat	uStaat	58	58	20
4359	43	U-Ort-ID	uOrtId	59	87	1
4360	43	Ursprungszeit	uZeit	60	104	2
4361	43	Tags	tags	61	59	38
4362	43	Sek. Maßeinheit erfasst	kombiSekMeh	62	\N	1
4363	43	Sek. Maßeinheit erfasst j/n	booleanSekMeh	63	\N	11
4364	43	Sek. Maßeinheit Faktor	faktorSekMeh	64	\N	33
4365	43	interne MWID	id	65	\N	1
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
5309	53	Bundesland-ID	bundeslandId	8	\N	1
5310	53	Bundesland	bundesland	9	\N	1
5311	53	Reg.bezirk-ID	rbezId	10	\N	1
5312	53	Reg.bezirk	rbez	11	\N	1
5313	53	Landkreis-ID	kreisId	12	\N	1
5314	53	Landkreis	kreis	13	\N	1
5401	54	ID	anlageId	1	\N	33
5402	54	Anlage	anlageBez	2	82	1
5403	54	Beschreibung	anlageBeschr	3	\N	1
5501	55	ID	staatId	1	\N	1
5502	55	HKL-ID	hklId	2	\N	1
5503	55	Staat	staatBez	3	83	1
5505	55	ISO	staatIso	5	\N	1
5506	55	EU	staatEu	6	\N	11
5507	55	Koordinatenart	kda	7	\N	1
5508	55	X-Koordinate	staatKoordX	8	\N	1
5509	55	Y-Koordinate	staatKoordY	9	\N	1
5601	56	OZ-ID	ozId	1	84	1
5602	56	Ortszusatz	oz	2	85	1
6001	60	ID	netzId	1	\N	18
6002	60	Bezeichnung	netzbetreiber	2	97	1
6003	60	IDF Netzbetreiber	netzIdf	3	\N	1
6004	60	BMN	netzBmn	4	\N	11
6101	61	ID	pzsId	1	\N	1
6102	61	Bezeichnung	pzsBez	2	\N	1
6103	61	Beschreibung	pzsBeschr	3	98	1
6104	61	Maßeinheit	pzsEinheit	4	\N	1
6105	61	EUDF Keyword	pzsEudf	5	\N	1
6201	62	ID	reiproggrpId	1	\N	1
6202	62	REI-Prog-GRP	reiproggrp	2	\N	1
6203	62	REI-Prog-GRP-Beschr	reiproggrpbeschr	3	99	1
6301	63	ID	tagId	1	\N	43
6302	63	Tag	tag	2	\N	1
6303	63	Messstelle	mstId	3	131	10
6304	63	Netzbetreiber	netzId	4	130	18
6305	63	Eigentümer	username	5	\N	1
6306	63	Typ	tagTyp	6	132	44
6307	63	Gültig bis	gueltigBis	7	\N	2
6308	63	Generiert	createdAt	8	\N	2
\.


--
-- Data for Name: lada_user; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.lada_user (id, name) FROM stdin;
0	Default
1	i_admin
\.


--
-- Data for Name: query_user; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.query_user (id, name, lada_user_id, base_query_id, descr) FROM stdin;
1	Proben	0	1	Vorlage für Probenselektion
2	Messungen	0	11	Vorlage für Messungsselektion\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
3	Messprogramme	0	21	Vorlage für Messprogrammselektion
4	Orte	0	31	Abfrage der Orte
5	Probenehmer	0	32	Abfrage der Probenehmer
6	Datensatzerzeuger	0	33	Abfrage der Datensatzerzeuger
7	Messprogrammkategorie	0	34	Abfrage der Messprogrammkategorien
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
27	Ergebnis PEP-Generierung	0	14	Die Abfrage bildet die Anzeige der PEP-Proben nach ihrer Generierung aus einem Messprogramm nach. Die Formatierung der Ergebnistabelle ist auf den Export der Daten abgestimmt und weicht deshalb teilweise vom Standard in LADA ab, insbesondere bei der Auswahl und Benennung der Spalten.
31	Messungen mit Deskriptoren	0	15	Vorlage für Messungesselektion mit Deskriptoren\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
32	StammBund Netzbetreiber	0	60	Abfrage Netzbetreiber
33	StammBund Probenzusatz	0	61	Abfrage Probenzusatz
34	StammBund REI-ProgrammpunktGruppe	0	62	Abfrage REI-ProgrammpunktGruppe
35	Messwerte mit Umrechnung	1	43	Es werden nur Messungen mit Messstellen-Status angezeigt!
36	Messungen mit Probenzusatzwerten	0	16	Vorlage für Messungesselektion mit Probenzusatzwerten\nIn den Spalten der Nuklide werden nur Messwerte mit Messstellen-Status angezeigt. In den Details richtet sich die Sichtbarkeit nach den persönlichen Berechtigungen.
37	Tags	0	63	Tags
\.


--
-- Data for Name: grid_column_values; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.grid_col_conf (id, lada_user_id, grid_col_mp_id, query_user_id, sort, sort_index, filter_val, is_filter_active, is_visible, col_index, width, is_filter_negate, is_filter_regex, is_filter_null) FROM stdin;
8	0	103	1	\N	\N	\N	f	t	1	77	f	f	f
11	0	102	1	\N	\N	\N	t	t	0	92	f	f	f
19	0	118	1	\N	\N	\N	f	f	-1	150	f	f	f
22	0	117	1	\N	\N	\N	f	f	-1	55	f	f	f
28	0	128	1	\N	\N	\N	f	f	\N	200	f	f	f
40	0	1128	2	\N	\N	\N	f	f	-1	75	f	f	f
46	0	1123	2	\N	\N	\N	f	f	-1	75	f	f	f
63	0	1135	2	\N	\N	\N	f	f	-1	75	f	f	f
71	0	1104	2	\N	\N	\N	f	t	1	54	f	f	f
75	0	1152	2	\N	\N	\N	f	f	-1	164	f	f	f
107	0	2113	3	\N	\N	\N	f	f	-1	55	f	f	f
114	0	2120	3	\N	\N	\N	f	f	\N	120	f	f	f
129	0	3110	4	\N	\N	\N	f	f	-1	56	f	f	f
132	0	3103	4	\N	\N	\N	t	t	1	120	f	f	f
135	0	3113	4	\N	\N	\N	f	f	-1	120	f	f	f
91	0	3306	6	\N	\N	\N	f	f	-1	125	f	f	f
94	0	3307	6	\N	\N	\N	t	f	-1	120	f	f	f
95	0	3308	6	\N	\N	\N	f	f	-1	164	f	f	f
123	0	3406	7	\N	\N	\N	t	f	-1	120	f	f	f
33	0	1110	2	\N	\N	\N	f	t	8	69	f	f	f
36	0	1146	2	\N	\N	\N	f	f	-1	200	f	f	f
43	0	1124	2	\N	\N	\N	f	f	-1	75	f	f	f
59	0	1121	2	\N	\N	\N	f	f	-1	75	f	f	f
72	0	1119	2	\N	\N	\N	f	f	-1	75	f	f	f
79	0	1140	2	\N	\N	\N	t	t	7	150	f	f	f
82	0	1105	2	\N	\N	\N	f	f	-1	125	f	f	f
84	0	1157	2	\N	\N	\N	f	f	\N	200	f	f	f
97	0	2102	3	\N	\N	\N	f	f	-1	52	f	f	f
101	0	2106	3	\N	\N	\N	f	t	6	69	f	f	f
104	0	2109	3	\N	\N	\N	f	t	9	102	f	f	f
111	0	2115	3	\N	\N	\N	t	t	3	164	f	f	f
137	0	3101	4	\N	\N	\N	f	f	-1	92	f	f	f
143	0	3106	4	\N	\N	\N	f	t	4	200	f	f	f
150	0	3116	4	\N	\N	\N	f	f	-1	75	f	f	f
154	0	3201	5	\N	\N	\N	f	f	-1	92	f	f	f
157	0	3204	5	\N	\N	\N	f	f	-1	120	f	f	f
160	0	3207	5	\N	\N	\N	f	t	3	300	f	f	f
163	0	3210	5	\N	\N	\N	f	f	-1	57	f	f	f
166	0	3213	5	\N	\N	\N	f	f	-1	50	f	f	f
169	0	3216	5	\N	\N	\N	t	f	-1	120	f	f	f
88	0	3301	6	\N	\N	\N	f	f	-1	92	f	f	f
122	0	3405	7	\N	\N	\N	f	f	-1	125	f	f	f
9	0	112	1	\N	\N	\N	f	f	-1	120	f	f	f
12	0	105	1	\N	\N	\N	f	t	3	92	f	f	f
20	0	101	1	\N	\N	\N	f	f	-1	92	f	f	f
26	0	110	1	\N	\N	\N	f	t	10	125	f	f	f
177	0	4123	9	\N	\N	\N	f	t	1	84	f	f	f
182	0	4103	9	\N	\N	\N	t	t	2	92	f	f	f
198	0	4112	9	\N	\N	\N	f	t	6	125	f	f	f
201	0	4139	9	\N	\N	\N	f	f	-1	125	f	f	f
204	0	4124	9	\N	\N	\N	t	t	10	145	f	f	f
258	0	1213	10	\N	\N	\N	f	f	\N	70	f	f	f
264	0	1227	10	\N	\N	\N	f	f	\N	150	f	f	f
267	0	1218	10	\N	\N	\N	f	f	\N	150	f	f	f
270	0	1217	10	\N	\N	\N	f	t	15	55	f	f	f
275	0	1212	10	\N	\N	\N	f	t	11	120	f	f	f
280	0	1240	10	\N	\N	\N	f	t	20	46	f	f	f
296	0	1238	10	\N	\N	\N	f	t	13	100	f	f	f
178	0	4119	9	\N	\N	\N	f	t	17	141	f	f	f
179	0	4122	9	\N	\N	\N	t	t	0	145	f	f	f
180	0	4138	9	\N	\N	\N	f	f	-1	70	f	f	f
181	0	4101	9	\N	\N	\N	f	f	-1	92	f	f	f
183	0	4136	9	\N	\N	\N	f	f	-1	150	f	f	f
184	0	4121	9	\N	\N	\N	f	t	11	53	f	f	f
185	0	4134	9	\N	\N	\N	f	f	-1	150	f	f	f
187	0	4132	9	\N	\N	\N	f	f	-1	100	f	f	f
188	0	4137	9	\N	\N	\N	f	t	4	154	f	f	f
190	0	4107	9	\N	\N	\N	f	f	-1	52	f	f	f
191	0	4126	9	\N	\N	\N	t	f	-1	120	f	f	f
193	0	4110	9	\N	\N	\N	f	f	-1	69	f	f	f
194	0	4116	9	\N	\N	\N	f	t	13	40	f	f	f
196	0	4130	9	\N	\N	\N	f	f	-1	69	f	f	f
197	0	4111	9	\N	\N	\N	t	t	5	125	f	f	f
199	0	4102	9	\N	\N	\N	f	f	-1	92	f	f	f
200	0	4131	9	\N	\N	\N	f	f	-1	200	f	f	f
203	0	4105	9	\N	\N	\N	f	f	-1	125	f	f	f
186	0	4133	9	\N	\N	\N	f	f	-1	55	f	f	f
189	0	4108	9	\N	\N	\N	f	f	-1	92	f	f	f
192	0	4104	9	\N	\N	\N	f	t	3	53	f	f	f
195	0	4118	9	\N	\N	\N	f	t	15	120	f	f	f
202	0	4140	9	\N	\N	\N	f	f	-1	125	f	f	f
208	0	4125	9	\N	\N	\N	t	t	20	150	f	f	f
253	0	1222	10	\N	\N	\N	f	t	10	70	f	f	f
261	0	1202	10	\N	\N	\N	t	t	3	92	f	f	f
284	0	1241	10	\N	\N	\N	f	f	\N	125	f	f	f
287	0	1224	10	\N	\N	\N	f	t	14	69	f	f	f
290	0	1208	10	\N	\N	\N	t	t	6	125	f	f	f
1	0	122	1	\N	\N	\N	f	t	5	70	f	f	f
2	0	116	1	\N	\N	\N	f	f	-1	100	f	f	f
3	0	115	1	\N	\N	\N	t	f	-1	145	f	f	f
4	0	124	1	\N	\N	\N	f	f	-1	69	f	f	f
5	0	125	1	\N	\N	\N	f	f	-1	200	f	f	f
6	0	127	1	\N	\N	\N	f	f	-1	150	f	f	f
7	0	120	1	\N	\N	\N	t	t	2	120	f	f	f
10	0	109	1	\N	\N	\N	f	f	-1	125	f	f	f
13	0	123	1	\N	\N	\N	f	t	6	120	f	f	f
14	0	106	1	\N	\N	\N	f	t	7	56	f	f	f
15	0	113	1	\N	\N	\N	f	f	-1	70	f	f	f
16	0	104	1	\N	\N	\N	f	f	-1	52	f	f	f
17	0	108	1	\N	\N	\N	f	f	-1	125	f	f	f
18	0	114	1	\N	\N	\N	f	f	-1	150	f	f	f
23	0	107	1	\N	\N	\N	f	t	9	69	f	f	f
24	0	111	1	\N	\N	\N	f	t	11	125	f	f	f
25	0	119	1	\N	\N	\N	t	t	8	150	f	f	f
27	0	121	1	\N	\N	\N	t	t	4	164	f	f	f
29	0	129	1	\N	\N	\N	f	f	\N	200	f	f	f
30	0	1109	2	\N	\N	\N	f	t	6	56	f	f	f
31	0	1116	2	\N	\N	\N	f	t	14	75	f	f	f
32	0	1131	2	\N	\N	\N	f	f	-1	75	f	f	f
34	0	1145	2	\N	\N	\N	f	f	-1	69	f	f	f
35	0	1112	2	\N	\N	\N	f	t	10	125	f	f	f
37	0	1102	2	\N	\N	\N	f	f	-1	92	f	f	f
38	0	1137	2	\N	\N	\N	t	f	-1	145	f	f	f
39	0	1143	2	\N	\N	\N	f	f	-1	70	f	f	f
41	0	1147	2	\N	\N	\N	f	f	-1	100	f	f	f
42	0	1134	2	\N	\N	\N	f	f	-1	75	f	f	f
44	0	1138	2	\N	\N	\N	f	f	-1	81	f	f	f
45	0	1141	2	\N	\N	\N	t	t	5	120	f	f	f
47	0	1117	2	\N	\N	\N	f	f	-1	75	f	f	f
48	0	1126	2	\N	\N	\N	f	f	-1	75	f	f	f
49	0	1115	2	\N	\N	\N	f	t	13	75	f	f	f
50	0	1120	2	\N	\N	\N	f	f	-1	75	f	f	f
51	0	1133	2	\N	\N	\N	f	f	-1	75	f	f	f
52	0	1127	2	\N	\N	\N	f	f	-1	75	f	f	f
53	0	1113	2	\N	\N	\N	f	t	11	70	f	f	f
54	0	1144	2	\N	\N	\N	f	f	-1	120	f	f	f
55	0	1136	2	\N	\N	\N	f	t	2	53	f	f	f
56	0	1148	2	\N	\N	\N	f	f	-1	55	f	f	f
57	0	1129	2	\N	\N	\N	f	f	-1	75	f	f	f
58	0	1149	2	\N	\N	\N	f	f	-1	150	f	f	f
60	0	1108	2	\N	\N	\N	f	f	-1	92	f	f	f
61	0	1139	2	\N	\N	\N	t	t	3	145	f	f	f
62	0	1103	2	\N	\N	\N	t	t	0	92	f	f	f
64	0	1107	2	\N	\N	\N	f	f	-1	52	f	f	f
65	0	1106	2	\N	\N	\N	f	t	4	77	f	f	f
66	0	1101	2	\N	\N	\N	f	f	-1	92	f	f	f
68	0	1122	2	\N	\N	\N	f	f	-1	75	f	f	f
69	0	1114	2	\N	\N	\N	f	t	12	150	f	f	f
70	0	1142	2	\N	\N	\N	f	f	-1	76	f	f	f
73	0	1130	2	\N	\N	\N	f	f	-1	75	f	f	f
74	0	1111	2	\N	\N	\N	t	t	9	125	f	f	f
76	0	1132	2	\N	\N	\N	f	f	-1	75	f	f	f
77	0	1125	2	\N	\N	\N	f	f	-1	75	f	f	f
78	0	1151	2	\N	\N	\N	f	f	-1	150	f	f	f
80	0	1118	2	\N	\N	\N	f	f	-1	75	f	f	f
81	0	1153	2	\N	\N	\N	f	f	\N	70	f	f	f
83	0	1156	2	\N	\N	\N	f	f	\N	120	f	f	f
85	0	1158	2	\N	\N	\N	f	f	\N	200	f	f	f
86	0	1154	2	\N	\N	\N	f	f	\N	125	f	f	f
87	0	1155	2	\N	\N	\N	f	f	\N	125	f	f	f
96	0	2101	3	\N	\N	\N	f	t	0	100	f	f	f
98	0	2105	3	\N	\N	\N	f	t	5	150	f	f	f
99	0	2103	3	\N	\N	\N	f	t	2	92	f	f	f
100	0	2107	3	\N	\N	\N	f	t	7	56	f	f	f
102	0	2104	3	\N	\N	\N	f	t	4	77	f	f	f
103	0	2108	3	\N	\N	\N	f	t	8	255	f	f	f
105	0	2110	3	\N	\N	\N	f	t	10	120	f	f	f
106	0	2112	3	\N	\N	\N	f	t	12	150	f	f	f
108	0	2111	3	\N	\N	\N	f	t	11	70	f	f	f
109	0	2114	3	\N	\N	\N	f	f	-1	150	f	f	f
110	0	2119	3	\N	\N	\N	f	f	\N	70	f	f	f
112	0	2116	3	\N	\N	\N	f	f	-1	55	f	f	f
113	0	2117	3	\N	\N	\N	t	t	1	120	f	f	f
115	0	2121	3	\N	\N	\N	f	f	\N	69	f	f	f
116	0	2122	3	\N	\N	\N	f	f	\N	200	f	f	f
118	0	2118	3	\N	\N	\N	f	f	\N	150	f	f	f
125	0	3107	4	\N	\N	\N	f	t	5	120	f	f	f
126	0	3105	4	\N	\N	\N	f	t	3	200	f	f	f
127	0	3120	4	\N	\N	\N	f	f	-1	55	f	f	f
128	0	3125	4	\N	\N	\N	f	f	-1	200	f	f	f
130	0	3127	4	\N	\N	\N	t	t	0	120	f	f	f
131	0	3104	4	\N	\N	\N	f	t	2	70	f	f	f
133	0	3102	4	\N	\N	\N	f	f	-1	52	f	f	f
134	0	3122	4	\N	\N	\N	f	f	-1	40	f	f	f
136	0	3123	4	\N	\N	\N	f	f	-1	40	f	f	f
138	0	3118	4	\N	\N	\N	f	f	-1	75	f	f	f
139	0	3124	4	\N	\N	\N	f	f	-1	100	f	f	f
140	0	3117	4	\N	\N	\N	f	f	-1	75	f	f	f
141	0	3115	4	\N	\N	\N	f	f	-1	125	f	f	f
142	0	3121	4	\N	\N	\N	f	f	-1	125	f	f	f
144	0	3112	4	\N	\N	\N	f	f	-1	100	f	f	f
145	0	3114	4	\N	\N	\N	f	f	-1	125	f	f	f
147	0	3119	4	\N	\N	\N	f	f	-1	75	f	f	f
148	0	3108	4	\N	\N	\N	t	t	6	150	f	f	f
149	0	3126	4	\N	\N	\N	f	f	-1	70	f	f	f
151	0	3111	4	\N	\N	\N	f	f	-1	70	f	f	f
152	0	3128	4	\N	\N	\N	f	f	\N	120	f	f	f
153	0	3129	4	\N	\N	\N	f	f	\N	70	f	f	f
155	0	3202	5	\N	\N	\N	f	t	0	52	f	f	f
156	0	3203	5	\N	\N	\N	f	t	1	70	f	f	f
158	0	3205	5	\N	\N	\N	f	f	-1	200	f	f	f
159	0	3206	5	\N	\N	\N	f	f	-1	120	f	f	f
161	0	3208	5	\N	\N	\N	f	t	2	150	f	f	f
162	0	3209	5	\N	\N	\N	f	f	-1	120	f	f	f
164	0	3211	5	\N	\N	\N	f	f	-1	200	f	f	f
165	0	3212	5	\N	\N	\N	f	f	-1	140	f	f	f
167	0	3214	5	\N	\N	\N	f	f	-1	50	f	f	f
168	0	3215	5	\N	\N	\N	f	f	-1	125	f	f	f
89	0	3302	6	\N	\N	\N	f	t	0	52	f	f	f
90	0	3303	6	\N	\N	\N	f	t	1	120	f	f	f
92	0	3305	6	\N	\N	\N	f	t	3	300	f	f	f
93	0	3304	6	\N	\N	\N	f	t	2	92	f	f	f
119	0	3401	7	\N	\N	\N	f	f	-1	92	f	f	f
120	0	3402	7	\N	\N	\N	f	t	0	52	f	f	f
121	0	3403	7	\N	\N	\N	f	t	1	50	f	f	f
124	0	3404	7	\N	\N	\N	f	t	2	300	f	f	f
170	0	4128	9	\N	\N	\N	f	f	-1	70	f	f	f
171	0	4129	9	\N	\N	\N	f	f	-1	120	f	f	f
172	0	4114	9	\N	\N	\N	f	t	8	150	f	f	f
173	0	4113	9	\N	\N	\N	f	f	-1	70	f	f	f
174	0	4120	9	\N	\N	\N	f	t	16	100	f	f	f
175	0	4106	9	\N	\N	\N	f	f	-1	77	f	f	f
176	0	4127	9	\N	\N	\N	f	t	18	76	f	f	f
206	0	4109	9	\N	\N	\N	f	t	9	56	f	f	f
207	0	4115	9	\N	\N	\N	f	t	12	76	f	f	f
209	0	4117	9	\N	\N	\N	f	t	14	81	f	f	f
254	0	1223	10	\N	\N	\N	f	f	\N	120	f	f	f
255	0	1233	10	\N	\N	\N	f	f	\N	255	f	f	f
256	0	1203	10	\N	\N	\N	t	t	0	77	f	f	f
257	0	1214	10	\N	\N	\N	f	t	12	150	f	f	f
259	0	1215	10	\N	\N	\N	t	t	1	145	f	f	f
260	0	1228	10	\N	\N	\N	f	t	2	70	f	f	f
262	0	1242	10	\N	\N	\N	f	f	\N	92	f	f	f
263	0	1234	10	\N	\N	\N	f	f	\N	150	f	f	f
265	0	1229	10	\N	\N	\N	f	f	\N	125	f	f	f
266	0	1231	10	\N	\N	\N	f	f	\N	75	f	f	f
268	0	1230	10	\N	\N	\N	f	f	\N	53	f	f	f
269	0	1216	10	\N	\N	\N	f	t	16	100	f	f	f
271	0	1221	10	\N	\N	\N	t	t	4	164	f	f	f
272	0	1205	10	\N	\N	\N	f	f	\N	92	f	f	f
273	0	1204	10	\N	\N	\N	f	f	\N	52	f	f	f
274	0	1220	10	\N	\N	\N	t	t	5	120	f	f	f
276	0	1237	10	\N	\N	\N	f	f	\N	56	f	f	f
277	0	1207	10	\N	\N	\N	f	f	\N	69	f	f	f
278	0	1210	10	\N	\N	\N	f	f	\N	125	f	f	f
279	0	1211	10	\N	\N	\N	f	f	\N	125	f	f	f
281	0	1201	10	\N	\N	\N	f	f	\N	92	f	f	f
282	0	1235	10	\N	\N	\N	f	t	19	128	f	f	f
283	0	1232	10	\N	\N	\N	f	t	21	250	f	f	f
285	0	1243	10	\N	\N	\N	f	t	17	120	f	f	f
286	0	1236	10	\N	\N	\N	f	t	18	250	f	f	f
289	0	1225	10	\N	\N	\N	f	f	\N	200	f	f	f
291	0	1209	10	\N	\N	\N	t	t	7	125	f	f	f
292	0	1239	10	\N	\N	\N	f	t	9	145	f	f	f
294	0	1219	10	\N	\N	\N	f	t	8	150	f	f	f
295	0	1206	10	\N	\N	\N	f	f	\N	56	f	f	f
299	0	130	1	\N	\N	\N	f	f	\N	255	f	f	f
300	0	131	1	\N	\N	\N	f	f	\N	150	f	f	f
301	0	132	1	\N	\N	\N	f	f	\N	250	f	f	f
302	0	133	1	\N	\N	\N	f	f	\N	46	f	f	f
303	0	134	1	\N	\N	\N	f	f	\N	125	f	f	f
304	0	1159	2	\N	\N	\N	f	f	\N	250	f	f	f
305	0	1160	2	\N	\N	\N	f	f	\N	46	f	f	f
306	0	1161	2	\N	\N	\N	f	f	\N	125	f	f	f
307	0	4141	9	\N	\N	\N	f	f	-1	250	f	f	f
308	0	4142	9	\N	\N	\N	f	f	-1	46	f	f	f
309	0	4143	9	\N	\N	\N	f	f	-1	125	f	f	f
310	0	1162	2	\N	\N	\N	f	f	\N	75	f	f	f
311	0	4144	9	\N	\N	\N	f	f	-1	75	f	f	f
312	0	1163	2	\N	\N	\N	f	f	\N	125	f	f	f
313	0	1164	2	\N	\N	\N	f	f	\N	75	f	f	f
314	0	4145	9	\N	\N	\N	f	f	-1	125	f	f	f
315	0	4146	9	\N	\N	\N	f	f	-1	75	f	f	f
316	0	1165	2	\N	\N	\N	f	f	\N	255	f	f	f
317	0	1166	2	\N	\N	\N	f	f	\N	150	f	f	f
318	0	4147	9	\N	\N	\N	f	f	-1	200	f	f	f
319	0	4148	9	\N	\N	\N	f	f	-1	200	f	f	f
320	0	4149	9	\N	\N	\N	f	f	-1	255	f	f	f
321	0	4150	9	\N	\N	\N	f	f	-1	150	f	f	f
322	0	135	1	\N	\N	\N	f	f	\N	120	f	f	f
323	0	2124	3	\N	\N	\N	f	f	\N	200	f	f	f
324	0	2125	3	\N	\N	\N	f	f	\N	200	f	f	f
325	0	4201	11	\N	\N	\N	f	f	-1	92	f	f	f
326	0	4202	11	\N	\N	\N	f	f	-1	92	f	f	f
327	0	4203	11	\N	\N	\N	f	f	-1	92	f	f	f
328	0	4204	11	\N	\N	\N	f	f	-1	53	f	f	f
329	0	4205	11	\N	\N	\N	f	f	-1	125	f	f	f
330	0	4206	11	\N	\N	\N	t	f	-1	77	f	f	f
331	0	4207	11	\N	\N	\N	f	f	-1	52	f	f	f
332	0	4208	11	\N	\N	\N	f	t	1	92	f	f	f
333	0	4209	11	\N	\N	\N	f	f	-1	56	f	f	f
334	0	4210	11	\N	\N	\N	f	f	-1	120	f	f	f
335	0	4211	11	asc	5	\N	f	t	9	125	f	f	f
336	0	4212	11	asc	6	\N	f	t	10	125	f	f	f
337	0	4213	11	\N	\N	\N	f	f	-1	70	f	f	f
338	0	4214	11	asc	4	\N	f	t	7	150	f	f	f
339	0	4215	11	\N	\N	\N	f	t	11	76	f	f	f
440	0	4216	11	\N	\N	\N	f	t	12	40	f	f	f
441	0	4217	11	\N	\N	\N	f	t	13	81	f	f	f
442	0	4218	11	\N	\N	\N	f	t	14	120	f	f	f
443	0	4219	11	\N	\N	\N	f	t	16	141	f	f	f
444	0	4220	11	\N	\N	\N	f	t	15	75	f	f	f
445	0	4221	11	\N	\N	\N	f	f	-1	53	f	f	f
446	0	4222	11	asc	7	\N	f	t	8	145	f	f	f
447	0	4223	11	\N	\N	\N	f	f	-1	84	f	f	f
448	0	4224	11	\N	\N	\N	t	f	-1	145	f	f	f
449	0	4225	11	\N	\N	\N	f	f	-1	150	f	f	f
450	0	4226	11	\N	\N	\N	t	f	-1	120	f	f	f
451	0	4227	11	\N	\N	\N	f	f	-1	76	f	f	f
452	0	4228	11	\N	\N	\N	t	f	-1	70	f	f	f
453	0	4229	11	asc	0	\N	f	t	0	120	f	f	f
454	0	4230	11	asc	1	\N	t	t	3	69	f	f	f
455	0	4231	11	\N	\N	\N	f	t	4	200	f	f	f
456	0	4232	11	asc	3	\N	f	t	6	200	f	f	f
458	0	4234	11	\N	\N	\N	f	f	-1	150	f	f	f
459	0	4235	11	\N	\N	\N	t	t	2	154	f	f	f
460	0	4236	11	\N	\N	\N	f	f	-1	70	f	f	f
461	0	4237	11	\N	\N	\N	f	f	-1	125	f	f	f
462	0	4238	11	\N	\N	\N	f	f	-1	125	f	f	f
463	0	4239	11	asc	2	\N	f	t	5	75	f	f	f
464	0	4240	11	\N	\N	\N	f	f	-1	125	f	f	f
465	0	4241	11	\N	\N	\N	f	f	-1	75	f	f	f
466	0	4242	11	\N	\N	\N	f	f	-1	200	f	f	f
467	0	4243	11	\N	\N	\N	f	f	-1	200	f	f	f
468	0	4244	11	\N	\N	\N	f	t	27	150	f	f	f
469	0	4245	11	\N	\N	\N	t	f	-1	125	f	f	f
470	0	4246	11	\N	\N	\N	f	t	20	40	f	f	f
471	0	4247	11	\N	\N	\N	f	t	21	55	f	f	f
472	0	4248	11	\N	\N	\N	f	t	19	220	f	f	f
473	0	4249	11	\N	\N	\N	f	t	17	220	f	f	f
474	0	4250	11	\N	\N	\N	f	t	18	220	f	f	f
475	0	136	1	\N	\N	\N	f	f	\N	80	f	f	f
476	0	1167	2	\N	\N	\N	f	f	\N	80	f	f	f
477	0	1168	2	\N	\N	\N	f	f	\N	100	f	f	f
478	0	4151	9	\N	\N	\N	f	f	-1	80	f	f	f
479	0	138	1	\N	\N	\N	f	f	\N	70	f	f	f
480	0	139	1	\N	\N	\N	f	f	\N	70	f	f	f
481	0	140	1	\N	\N	\N	f	f	\N	80	f	f	f
482	0	137	1	\N	\N	\N	f	f	\N	100	f	f	f
483	0	4152	9	\N	\N	\N	f	f	-1	100	f	f	f
484	0	1172	2	\N	\N	\N	f	f	-1	50	f	f	f
485	0	4156	9	\N	\N	\N	f	t	19	81	f	f	f
486	0	4157	9	\N	\N	\N	f	t	7	120	f	f	f
487	0	1169	2	\N	\N	\N	f	f	\N	70	f	f	f
488	0	1170	2	\N	\N	\N	f	f	\N	70	f	f	f
489	0	1171	2	\N	\N	\N	f	f	\N	80	f	f	f
490	0	4153	9	\N	\N	\N	f	f	-1	70	f	f	f
491	0	4154	9	\N	\N	\N	f	f	-1	70	f	f	f
492	0	4155	9	\N	\N	\N	f	f	-1	80	f	f	f
493	0	1245	10	\N	\N	\N	f	f	\N	70	f	f	f
494	0	1246	10	\N	\N	\N	f	f	\N	70	f	f	f
495	0	1247	10	\N	\N	\N	f	f	\N	80	f	f	f
496	0	1248	10	\N	\N	\N	f	f	\N	70	f	f	f
497	0	1249	10	\N	\N	\N	f	f	\N	81	f	f	f
498	0	1250	10	\N	\N	\N	f	f	\N	125	f	f	f
499	0	1251	10	\N	\N	\N	f	f	\N	100	f	f	f
500	0	1252	10	\N	\N	\N	f	f	\N	100	f	f	f
501	0	1253	10	\N	\N	\N	f	f	\N	125	f	f	f
502	0	1254	10	\N	\N	\N	f	f	\N	100	f	f	f
503	0	1255	10	\N	\N	\N	f	f	\N	200	f	f	f
504	0	1256	10	\N	\N	\N	f	f	\N	200	f	f	f
505	0	1257	10	\N	\N	\N	f	f	\N	200	f	f	f
506	0	1258	10	\N	\N	\N	f	f	\N	200	f	f	f
507	0	1259	10	\N	\N	\N	f	f	\N	150	f	f	f
508	0	1260	10	\N	\N	\N	f	f	\N	150	f	f	f
509	0	1261	10	\N	\N	\N	f	f	\N	200	f	f	f
510	0	4251	11	\N	\N	\N	f	f	\N	200	f	f	f
511	0	3501	12	\N	\N	\N	f	t	0	50	f	f	f
512	0	3502	12	asc	\N	\N	t	t	1	90	f	f	f
513	0	3503	12	\N	\N	\N	f	t	2	120	f	f	f
514	0	3504	12	\N	\N	\N	f	t	3	90	f	f	f
515	0	3505	12	\N	\N	\N	f	t	4	90	f	f	f
516	0	3506	12	\N	\N	\N	f	t	5	90	f	f	f
517	0	3507	12	\N	\N	\N	f	t	6	110	f	f	f
518	0	1173	2	\N	\N	\N	f	f	\N	80	f	f	f
519	0	4252	11	\N	\N	\N	f	t	23	100	f	f	f
521	0	3601	13	\N	\N	\N	f	t	0	50	f	f	f
522	0	3602	13	asc	\N	\N	t	t	1	100	f	f	f
523	0	3603	13	\N	\N	\N	f	t	2	350	f	f	f
524	0	3604	13	\N	\N	\N	f	t	3	100	f	f	f
525	0	3605	13	\N	\N	\N	f	f	\N	100	f	f	f
526	0	141	1	\N	\N	\N	f	f	\N	200	f	f	f
527	0	3701	14	asc	\N	\N	f	t	0	50	f	f	f
528	0	3702	14	\N	\N	\N	t	t	1	250	f	f	f
529	0	3703	14	\N	\N	\N	f	f	\N	250	f	f	f
530	0	142	1	\N	\N	\N	f	f	\N	125	f	f	f
531	0	3801	15	asc	\N	\N	f	t	0	80	f	f	f
532	0	3802	15	\N	\N	\N	f	t	1	180	f	f	f
533	0	3803	15	\N	\N	\N	f	f	\N	80	f	f	f
534	0	3804	15	\N	\N	\N	f	t	3	280	f	f	f
535	0	3805	15	\N	\N	\N	f	t	4	80	f	f	f
536	0	3806	15	\N	\N	\N	f	t	5	80	f	f	f
537	0	3807	15	\N	\N	\N	t	t	2	125	f	f	f
538	0	3901	16	asc	\N	\N	t	t	0	80	f	f	f
539	0	3902	16	\N	\N	\N	t	t	1	450	f	f	f
540	0	3903	16	\N	\N	\N	f	t	2	150	f	f	f
541	0	3904	16	\N	\N	\N	f	t	3	150	f	f	f
542	0	3905	16	\N	\N	\N	f	f	\N	200	f	f	f
543	0	5101	17	asc	\N	\N	t	t	0	70	f	f	f
544	0	5102	17	\N	\N	\N	t	t	1	200	f	f	f
545	0	5103	17	\N	\N	\N	f	t	2	40	f	f	f
546	0	5104	17	\N	\N	\N	f	t	3	200	f	f	f
547	0	5105	17	\N	\N	\N	f	t	4	40	f	f	f
548	0	5106	17	\N	\N	\N	f	t	5	200	f	f	f
549	0	5107	17	\N	\N	\N	f	t	6	40	f	f	f
550	0	5108	17	\N	\N	\N	f	t	7	200	f	f	f
551	0	5109	17	\N	\N	\N	f	t	8	40	f	f	f
552	0	5110	17	\N	\N	\N	f	t	9	200	f	f	f
553	0	5111	17	\N	\N	\N	f	t	10	40	f	f	f
554	0	5112	17	\N	\N	\N	f	t	11	200	f	f	f
555	0	5113	17	\N	\N	\N	f	t	12	40	f	f	f
556	0	5114	17	\N	\N	\N	f	t	13	200	f	f	f
557	0	5115	17	\N	\N	\N	f	t	14	40	f	f	f
558	0	5116	17	\N	\N	\N	f	t	15	200	f	f	f
559	0	5117	17	\N	\N	\N	f	t	16	40	f	f	f
560	0	5118	17	\N	\N	\N	f	t	17	200	f	f	f
561	0	5119	17	\N	\N	\N	f	t	18	40	f	f	f
562	0	5120	17	\N	\N	\N	f	t	19	200	f	f	f
563	0	5121	17	\N	\N	\N	f	t	20	40	f	f	f
564	0	5122	17	\N	\N	\N	f	t	21	200	f	f	f
565	0	5123	17	\N	\N	\N	f	t	22	40	f	f	f
566	0	5124	17	\N	\N	\N	f	t	23	200	f	f	f
567	0	5125	17	\N	\N	\N	f	t	24	40	f	f	f
568	0	5126	17	\N	\N	\N	f	t	25	200	f	f	f
569	0	5201	18	\N	\N	\N	t	t	0	70	f	f	f
570	0	5202	18	asc	\N	\N	t	t	1	60	f	f	f
571	0	5203	18	\N	\N	\N	t	t	2	60	f	f	f
572	0	5204	18	\N	\N	\N	t	t	3	300	f	f	f
573	0	5205	18	\N	\N	\N	f	t	4	170	f	f	f
574	0	5206	18	\N	\N	\N	f	f	\N	100	f	f	f
575	0	5207	18	\N	\N	\N	f	f	\N	100	f	f	f
576	0	5208	18	\N	\N	\N	f	f	\N	100	f	f	f
577	0	5209	18	\N	\N	\N	f	f	\N	300	f	f	f
578	0	5301	19	asc	\N	\N	t	t	0	100	f	f	f
579	0	5302	19	\N	\N	\N	t	t	1	200	f	f	f
580	0	5303	19	\N	\N	\N	f	t	2	100	f	f	f
581	0	5304	19	\N	\N	\N	t	t	3	100	f	f	f
582	0	5305	19	\N	\N	\N	t	t	4	100	f	f	f
583	0	5306	19	\N	\N	\N	t	t	5	100	f	f	f
584	0	5307	19	\N	\N	\N	t	t	6	100	f	f	f
586	0	5309	19	\N	\N	\N	f	t	8	100	f	f	f
587	0	5310	19	\N	\N	\N	f	t	9	100	f	f	f
588	0	5311	19	\N	\N	\N	f	t	10	100	f	f	f
589	0	5312	19	\N	\N	\N	f	t	11	100	f	f	f
590	0	5313	19	\N	\N	\N	f	t	12	100	f	f	f
591	0	5314	19	\N	\N	\N	f	t	13	100	f	f	f
592	0	4253	11	asc	8	\N	f	t	26	100	f	f	f
593	0	4254	11	\N	\N	\N	f	t	25	100	f	f	f
594	0	4255	11	\N	\N	\N	f	t	24	80	f	f	f
595	0	4256	11	\N	\N	\N	f	t	22	80	f	f	f
596	0	1244	10	\N	\N	\N	f	f	\N	75	f	f	f
597	0	5401	20	\N	\N	\N	f	t	0	80	f	f	f
598	0	5402	20	asc	\N	\N	t	t	1	120	f	f	f
599	0	5403	20	\N	\N	\N	f	t	2	400	f	f	f
600	0	5501	21	asc	\N	\N	f	t	0	70	f	f	f
601	0	5502	21	\N	\N	\N	f	t	1	70	f	f	f
602	0	5503	21	\N	\N	\N	t	t	2	200	f	f	f
604	0	5505	21	\N	\N	\N	f	t	3	70	f	f	f
605	0	5506	21	\N	\N	\N	f	t	4	70	f	f	f
606	0	5507	21	\N	\N	\N	f	t	5	150	f	f	f
607	0	5508	21	\N	\N	\N	f	t	6	100	f	f	f
608	0	5509	21	\N	\N	\N	f	t	7	100	f	f	f
609	0	5601	22	asc	\N	\N	t	t	0	120	f	f	f
610	0	5602	22	\N	\N	\N	t	t	1	400	f	f	f
611	0	143	1	\N	\N	\N	f	f	\N	80	f	f	f
612	0	144	1	\N	\N	\N	f	f	\N	120	f	f	f
613	0	145	1	\N	\N	\N	f	f	\N	125	f	f	f
614	0	1174	2	\N	\N	\N	f	f	\N	80	f	f	f
615	0	1175	2	\N	\N	\N	f	f	\N	120	f	f	f
616	0	1176	2	\N	\N	\N	f	f	\N	125	f	f	f
617	0	4158	9	\N	\N	\N	f	f	\N	80	f	f	f
618	0	4159	9	\N	\N	\N	f	f	\N	120	f	f	f
619	0	4160	9	\N	\N	\N	f	f	\N	125	f	f	f
620	0	2126	3	\N	\N	\N	f	f	\N	120	f	f	f
621	0	1177	2	\N	\N	\N	f	f	\N	200	f	f	f
622	0	4161	9	\N	\N	\N	f	f	\N	200	f	f	f
807	0	2127	3	\N	\N	\N	f	f	\N	200	f	f	f
808	0	2128	3	\N	\N	\N	f	f	\N	200	f	f	f
809	0	1262	10	\N	\N	\N	f	f	\N	200	f	f	f
810	0	1401	27	\N	\N	\N	f	t	0	120	f	f	f
811	0	1402	27	\N	\N	\N	t	t	1	160	f	f	f
812	0	1403	27	\N	\N	\N	f	f	\N	200	f	f	f
813	0	1404	27	\N	\N	\N	t	f	\N	200	f	f	f
814	0	1405	27	\N	\N	\N	f	t	2	80	f	f	f
815	0	1406	27	\N	\N	\N	f	t	3	100	f	f	f
817	0	1408	27	\N	\N	\N	f	t	4	100	f	f	f
818	0	1409	27	\N	\N	\N	f	t	5	100	f	f	f
819	0	1410	27	\N	\N	\N	t	t	6	150	f	f	f
820	0	1411	27	\N	\N	\N	t	t	7	150	f	f	f
821	0	1412	27	\N	\N	\N	t	t	8	100	f	f	f
822	0	1413	27	\N	\N	\N	f	t	9	260	f	f	f
823	0	1414	27	\N	\N	\N	f	t	10	80	f	f	f
824	0	1415	27	\N	\N	\N	f	t	11	200	f	f	f
825	0	1416	27	\N	\N	\N	f	t	12	100	f	f	f
826	0	1417	27	\N	\N	\N	f	t	13	80	f	f	f
827	0	1418	27	\N	\N	\N	t	f	\N	200	f	f	f
844	0	1501	31	\N	\N	\N	f	f	\N	92	f	f	f
845	0	1502	31	\N	\N	\N	f	f	\N	92	f	f	f
846	0	1503	31	\N	\N	\N	t	f	\N	92	f	f	f
847	0	1504	31	\N	\N	\N	f	f	\N	54	f	f	f
848	0	1505	31	\N	\N	\N	f	f	\N	125	f	f	f
849	0	1506	31	\N	\N	\N	t	f	\N	77	f	f	f
850	0	1507	31	\N	\N	\N	f	f	\N	52	f	f	f
851	0	1508	31	\N	\N	\N	f	f	\N	92	f	f	f
852	0	1509	31	\N	\N	\N	f	t	2	56	f	f	f
853	0	1510	31	\N	\N	\N	f	f	\N	69	f	f	f
854	0	1511	31	\N	\N	\N	t	f	\N	125	f	f	f
855	0	1512	31	\N	\N	\N	f	f	\N	125	f	f	f
856	0	1513	31	\N	\N	\N	f	f	\N	70	f	f	f
857	0	1514	31	\N	\N	\N	f	f	\N	150	f	f	f
858	0	1515	31	\N	\N	\N	f	f	\N	75	f	f	f
859	0	1516	31	\N	\N	\N	f	f	\N	75	f	f	f
860	0	1517	31	\N	\N	\N	f	f	\N	75	f	f	f
861	0	1518	31	\N	\N	\N	f	f	\N	75	f	f	f
862	0	1519	31	\N	\N	\N	f	f	\N	75	f	f	f
863	0	1520	31	\N	\N	\N	f	f	\N	75	f	f	f
864	0	1521	31	\N	\N	\N	f	f	\N	75	f	f	f
865	0	1522	31	\N	\N	\N	f	f	\N	75	f	f	f
866	0	1523	31	\N	\N	\N	f	f	\N	75	f	f	f
867	0	1524	31	\N	\N	\N	f	f	\N	75	f	f	f
868	0	1525	31	\N	\N	\N	f	f	\N	75	f	f	f
869	0	1526	31	\N	\N	\N	f	f	\N	75	f	f	f
870	0	1527	31	\N	\N	\N	f	f	\N	75	f	f	f
871	0	1528	31	\N	\N	\N	f	f	\N	75	f	f	f
872	0	1529	31	\N	\N	\N	f	f	\N	75	f	f	f
873	0	1530	31	\N	\N	\N	f	f	\N	75	f	f	f
874	0	1531	31	\N	\N	\N	f	f	\N	75	f	f	f
875	0	1532	31	\N	\N	\N	f	f	\N	75	f	f	f
876	0	1533	31	\N	\N	\N	f	f	\N	75	f	f	f
877	0	1534	31	\N	\N	\N	f	f	\N	75	f	f	f
878	0	1535	31	\N	\N	\N	f	f	\N	75	f	f	f
879	0	1536	31	\N	\N	\N	f	f	\N	53	f	f	f
880	0	1537	31	\N	\N	\N	t	t	0	145	f	f	f
881	0	1538	31	\N	\N	\N	f	t	1	81	f	f	f
882	0	1539	31	\N	\N	\N	t	f	\N	145	f	f	f
883	0	1540	31	\N	\N	\N	t	t	3	150	f	f	f
884	0	1541	31	\N	\N	\N	t	f	\N	120	f	f	f
885	0	1542	31	\N	\N	\N	f	f	\N	76	f	f	f
886	0	1543	31	\N	\N	\N	f	f	\N	100	f	f	f
887	0	1544	31	\N	\N	\N	f	f	\N	55	f	f	f
888	0	1545	31	\N	\N	\N	f	f	\N	150	f	f	f
890	0	1547	31	\N	\N	\N	f	f	\N	150	f	f	f
891	0	1548	31	\N	\N	\N	f	f	\N	70	f	f	f
892	0	1549	31	\N	\N	\N	f	f	\N	164	f	f	f
893	0	1550	31	\N	\N	\N	f	f	\N	125	f	f	f
894	0	1551	31	\N	\N	\N	f	f	\N	125	f	f	f
895	0	1552	31	\N	\N	\N	f	f	\N	120	f	f	f
896	0	1553	31	\N	\N	\N	f	f	\N	200	f	f	f
897	0	1554	31	\N	\N	\N	f	f	\N	200	f	f	f
898	0	1555	31	\N	\N	\N	f	t	10	180	f	f	f
899	0	1556	31	\N	\N	\N	f	f	\N	125	f	f	f
900	0	1557	31	\N	\N	\N	f	f	\N	75	f	f	f
901	0	1558	31	\N	\N	D: %	t	t	4	255	f	f	f
902	0	1559	31	\N	\N	\N	f	f	\N	150	f	f	f
903	0	1560	31	\N	\N	\N	f	f	\N	80	f	f	f
904	0	1561	31	\N	\N	\N	f	f	\N	100	f	f	f
905	0	1562	31	\N	\N	\N	f	f	\N	70	f	f	f
906	0	1563	31	\N	\N	\N	f	f	\N	70	f	f	f
907	0	1564	31	\N	\N	\N	f	f	\N	80	f	f	f
908	0	1565	31	\N	\N	\N	f	f	\N	50	f	f	f
909	0	1566	31	\N	\N	\N	f	f	\N	80	f	f	f
910	0	1567	31	\N	\N	\N	f	f	\N	120	f	f	f
911	0	1568	31	\N	\N	\N	f	f	\N	125	f	f	f
912	0	1569	31	\N	\N	\N	f	f	\N	200	f	f	f
913	0	1570	31	\N	\N	\N	f	f	\N	200	f	f	f
914	0	1571	31	\N	\N	\N	f	f	\N	200	f	f	f
915	0	1572	31	\N	\N	\N	f	t	5	200	f	f	f
916	0	1573	31	\N	\N	\N	f	t	6	200	f	f	f
917	0	1574	31	\N	\N	\N	f	t	7	200	f	f	f
918	0	1575	31	\N	\N	\N	f	t	8	200	f	f	f
919	0	1576	31	\N	\N	\N	f	t	9	200	f	f	f
920	0	2129	3	\N	\N	\N	f	f	\N	250	f	f	f
921	0	2130	3	\N	\N	\N	f	f	\N	80	f	f	f
922	0	2131	3	\N	\N	\N	f	f	\N	125	f	f	f
923	0	1263	10	\N	\N	\N	f	f	\N	200	f	f	f
924	0	6001	32	\N	\N	\N	f	t	0	100	f	f	f
925	0	6002	32	\N	\N	\N	t	t	1	250	f	f	f
926	0	6003	32	\N	\N	\N	f	t	2	150	f	f	f
927	0	6004	32	\N	\N	\N	f	t	3	100	f	f	f
928	0	6101	33	asc	\N	\N	f	t	0	100	f	f	f
929	0	6102	33	\N	\N	\N	f	t	1	150	f	f	f
930	0	6103	33	\N	\N	\N	t	t	2	250	f	f	f
931	0	6104	33	\N	\N	\N	f	t	3	100	f	f	f
932	0	6105	33	\N	\N	\N	f	t	4	100	f	f	f
933	0	6201	34	\N	\N	\N	f	t	0	70	f	f	f
934	0	6202	34	asc	\N	\N	f	t	1	150	f	f	f
935	0	6203	34	\N	\N	\N	t	t	2	550	f	f	f
937	1	4301	35	\N	\N	\N	f	f	-1	92	f	f	f
938	1	4302	35	\N	\N	\N	f	f	-1	92	f	f	f
939	1	4303	35	\N	\N	\N	t	t	2	92	f	f	f
940	1	4304	35	\N	\N	\N	f	t	3	53	f	f	f
941	1	4305	35	\N	\N	\N	f	f	-1	125	f	f	f
942	1	4306	35	\N	\N	\N	f	f	-1	77	f	f	f
943	1	4307	35	\N	\N	\N	f	f	-1	52	f	f	f
944	1	4308	35	\N	\N	\N	f	f	-1	92	f	f	f
945	1	4309	35	\N	\N	\N	f	t	9	56	f	f	f
946	1	4310	35	\N	\N	\N	f	f	-1	69	f	f	f
947	1	4311	35	\N	\N	\N	t	t	5	125	f	f	f
948	1	4312	35	\N	\N	\N	f	t	6	125	f	f	f
949	1	4313	35	\N	\N	\N	f	f	-1	70	f	f	f
950	1	4314	35	\N	\N	\N	f	t	8	150	f	f	f
951	1	4315	35	\N	\N	\N	f	t	12	76	f	f	f
952	1	4316	35	\N	\N	\N	f	t	13	40	f	f	f
953	1	4317	35	\N	\N	\N	f	t	14	81	f	f	f
954	1	4318	35	\N	\N	\N	f	t	15	120	f	f	f
955	1	4319	35	\N	\N	\N	f	t	17	141	f	f	f
956	1	4320	35	\N	\N	\N	f	t	16	100	f	f	f
957	1	4321	35	\N	\N	\N	f	t	11	53	f	f	f
958	1	4322	35	\N	\N	\N	t	t	0	145	f	f	f
959	1	4323	35	\N	\N	\N	f	t	1	84	f	f	f
960	1	4324	35	\N	\N	\N	t	t	10	145	f	f	f
961	1	4325	35	\N	\N	\N	t	t	20	150	f	f	f
962	1	4326	35	\N	\N	\N	t	f	-1	120	f	f	f
963	1	4327	35	\N	\N	\N	f	t	18	76	f	f	f
964	1	4328	35	\N	\N	\N	f	f	-1	70	f	f	f
965	1	4329	35	\N	\N	\N	f	f	-1	120	f	f	f
966	1	4330	35	\N	\N	\N	f	f	-1	69	f	f	f
967	1	4331	35	\N	\N	\N	f	f	-1	200	f	f	f
968	1	4332	35	\N	\N	\N	f	f	-1	100	f	f	f
969	1	4333	35	\N	\N	\N	f	f	-1	55	f	f	f
970	1	4334	35	\N	\N	\N	f	f	-1	150	f	f	f
972	1	4336	35	\N	\N	\N	f	f	-1	150	f	f	f
973	1	4337	35	\N	\N	\N	f	t	4	154	f	f	f
974	1	4338	35	\N	\N	\N	f	f	-1	70	f	f	f
975	1	4339	35	\N	\N	\N	f	f	-1	125	f	f	f
976	1	4340	35	\N	\N	\N	f	f	-1	125	f	f	f
977	1	4341	35	\N	\N	\N	f	f	-1	250	f	f	f
978	1	4342	35	\N	\N	\N	f	f	-1	46	f	f	f
979	1	4343	35	\N	\N	\N	f	f	-1	125	f	f	f
980	1	4344	35	\N	\N	\N	f	f	-1	75	f	f	f
981	1	4345	35	\N	\N	\N	f	f	-1	125	f	f	f
982	1	4346	35	\N	\N	\N	f	f	-1	75	f	f	f
983	1	4347	35	\N	\N	\N	f	f	-1	200	f	f	f
984	1	4348	35	\N	\N	\N	f	f	-1	200	f	f	f
985	1	4349	35	\N	\N	\N	f	f	-1	255	f	f	f
986	1	4350	35	\N	\N	\N	f	f	-1	150	f	f	f
987	1	4351	35	\N	\N	\N	f	f	-1	80	f	f	f
988	1	4352	35	\N	\N	\N	f	f	-1	100	f	f	f
989	1	4353	35	\N	\N	\N	f	f	-1	70	f	f	f
990	1	4354	35	\N	\N	\N	f	f	-1	70	f	f	f
991	1	4355	35	\N	\N	\N	f	f	-1	80	f	f	f
992	1	4356	35	\N	\N	\N	f	t	19	81	f	f	f
993	1	4357	35	\N	\N	\N	f	t	7	120	f	f	f
994	1	4358	35	\N	\N	\N	f	f	\N	80	f	f	f
995	1	4359	35	\N	\N	\N	f	f	\N	120	f	f	f
996	1	4360	35	\N	\N	\N	f	f	\N	125	f	f	f
997	1	4361	35	\N	\N	\N	f	f	\N	200	f	f	f
998	1	4362	35	\N	\N	\N	f	f	\N	180	f	f	f
999	0	1601	36	\N	\N	\N	f	f	-1	92	f	f	f
1000	0	1602	36	\N	\N	\N	f	f	-1	92	f	f	f
1001	0	1603	36	\N	\N	\N	t	t	0	92	f	f	f
1002	0	1604	36	\N	\N	\N	f	t	1	54	f	f	f
1003	0	1605	36	\N	\N	\N	f	f	-1	125	f	f	f
1004	0	1606	36	\N	\N	\N	f	t	4	77	f	f	f
1005	0	1607	36	\N	\N	\N	f	f	-1	52	f	f	f
1006	0	1608	36	\N	\N	\N	f	f	-1	92	f	f	f
1007	0	1609	36	\N	\N	\N	f	t	6	56	f	f	f
1008	0	1610	36	\N	\N	\N	f	t	8	69	f	f	f
1009	0	1611	36	\N	\N	\N	t	t	9	125	f	f	f
1010	0	1612	36	\N	\N	\N	f	t	10	125	f	f	f
1011	0	1613	36	\N	\N	\N	f	t	11	70	f	f	f
1012	0	1614	36	\N	\N	\N	f	t	12	150	f	f	f
1013	0	1615	36	\N	\N	\N	f	t	13	75	f	f	f
1014	0	1616	36	\N	\N	\N	f	t	14	75	f	f	f
1015	0	1617	36	\N	\N	\N	f	f	-1	75	f	f	f
1016	0	1618	36	\N	\N	\N	f	f	-1	75	f	f	f
1017	0	1619	36	\N	\N	\N	f	f	-1	75	f	f	f
1018	0	1620	36	\N	\N	\N	f	f	-1	75	f	f	f
1019	0	1621	36	\N	\N	\N	f	f	-1	75	f	f	f
1020	0	1622	36	\N	\N	\N	f	f	-1	75	f	f	f
1021	0	1623	36	\N	\N	\N	f	f	-1	75	f	f	f
1022	0	1624	36	\N	\N	\N	f	f	-1	75	f	f	f
1023	0	1625	36	\N	\N	\N	f	f	-1	75	f	f	f
1024	0	1626	36	\N	\N	\N	f	f	-1	75	f	f	f
1025	0	1627	36	\N	\N	\N	f	f	-1	75	f	f	f
1026	0	1628	36	\N	\N	\N	f	f	-1	75	f	f	f
1027	0	1629	36	\N	\N	\N	f	f	-1	75	f	f	f
1028	0	1630	36	\N	\N	\N	f	f	-1	75	f	f	f
1029	0	1631	36	\N	\N	\N	f	f	-1	75	f	f	f
1030	0	1632	36	\N	\N	\N	f	f	-1	75	f	f	f
1031	0	1633	36	\N	\N	\N	f	f	-1	75	f	f	f
1032	0	1634	36	\N	\N	\N	f	f	-1	75	f	f	f
1033	0	1635	36	\N	\N	\N	f	f	-1	75	f	f	f
1034	0	1636	36	\N	\N	\N	f	t	2	53	f	f	f
1035	0	1637	36	\N	\N	\N	t	f	-1	145	f	f	f
1036	0	1638	36	\N	\N	\N	f	f	-1	81	f	f	f
1037	0	1639	36	\N	\N	\N	t	t	3	145	f	f	f
1038	0	1640	36	\N	\N	\N	t	t	7	150	f	f	f
1039	0	1641	36	\N	\N	\N	t	t	5	120	f	f	f
1040	0	1642	36	\N	\N	\N	f	f	-1	76	f	f	f
1041	0	1643	36	\N	\N	\N	f	f	-1	70	f	f	f
1042	0	1644	36	\N	\N	\N	f	f	-1	120	f	f	f
1043	0	1645	36	\N	\N	\N	f	f	-1	69	f	f	f
1044	0	1646	36	\N	\N	\N	f	f	-1	200	f	f	f
1045	0	1647	36	\N	\N	\N	f	f	-1	100	f	f	f
1046	0	1648	36	\N	\N	\N	f	f	-1	55	f	f	f
1047	0	1649	36	\N	\N	\N	f	f	-1	150	f	f	f
1049	0	1651	36	\N	\N	\N	f	f	-1	150	f	f	f
1050	0	1652	36	\N	\N	\N	f	f	-1	164	f	f	f
1051	0	1653	36	\N	\N	\N	f	f	\N	70	f	f	f
1052	0	1654	36	\N	\N	\N	f	f	\N	125	f	f	f
1053	0	1655	36	\N	\N	\N	f	f	\N	125	f	f	f
1054	0	1656	36	\N	\N	\N	f	f	\N	120	f	f	f
1055	0	1657	36	\N	\N	\N	f	f	\N	200	f	f	f
1056	0	1658	36	\N	\N	\N	f	f	\N	200	f	f	f
1057	0	1659	36	\N	\N	\N	f	f	\N	250	f	f	f
1058	0	1660	36	\N	\N	\N	f	f	\N	46	f	f	f
1059	0	1661	36	\N	\N	\N	f	f	\N	125	f	f	f
1060	0	1662	36	\N	\N	\N	f	f	\N	75	f	f	f
1061	0	1663	36	\N	\N	\N	f	f	\N	125	f	f	f
1062	0	1664	36	\N	\N	\N	f	f	\N	75	f	f	f
1063	0	1665	36	\N	\N	\N	f	f	\N	255	f	f	f
1064	0	1666	36	\N	\N	\N	f	f	\N	150	f	f	f
1065	0	1667	36	\N	\N	\N	f	f	\N	80	f	f	f
1066	0	1668	36	\N	\N	\N	f	f	\N	100	f	f	f
1067	0	1669	36	\N	\N	\N	f	f	\N	70	f	f	f
1068	0	1670	36	\N	\N	\N	f	f	\N	70	f	f	f
1069	0	1671	36	\N	\N	\N	f	f	\N	80	f	f	f
1070	0	1672	36	\N	\N	\N	f	f	-1	50	f	f	f
1071	0	1673	36	\N	\N	\N	f	f	\N	80	f	f	f
1072	0	1674	36	\N	\N	\N	f	f	\N	80	f	f	f
1073	0	1675	36	\N	\N	\N	f	f	\N	120	f	f	f
1074	0	1676	36	\N	\N	\N	f	f	\N	125	f	f	f
1075	0	1677	36	\N	\N	\N	f	f	\N	200	f	f	f
1076	0	1678	36	\N	\N	\N	f	t	15	250	f	f	f
1077	1	4363	35	\N	\N	\N	f	f	\N	180	f	f	f
1078	1	4364	35	\N	\N	\N	f	f	\N	180	f	f	f
1079	0	4162	9	\N	\N	\N	f	f	\N	100	f	f	f
1080	0	4257	11	\N	\N	\N	f	f	-1	100	f	f	f
1081	1	4365	35	\N	\N	\N	f	f	-1	100	f	f	f
293	0	1226	10	\N	\N	\N	t	f	\N	40	f	f	f
205	0	4135	9	\N	\N	\N	t	f	-1	40	f	f	f
21	0	126	1	\N	\N	\N	t	f	-1	40	f	f	f
67	0	1150	2	\N	\N	\N	t	f	-1	40	f	f	f
117	0	2123	3	\N	\N	\N	f	f	\N	40	f	f	f
457	0	4233	11	\N	\N	\N	t	f	-1	40	f	f	f
816	0	1407	27	\N	\N	\N	t	f	\N	80	f	f	f
889	0	1546	31	\N	\N	\N	t	f	\N	40	f	f	f
971	1	4335	35	\N	\N	\N	t	f	-1	40	f	f	f
1048	0	1650	36	\N	\N	\N	t	f	-1	40	f	f	f
1090	0	6301	37	\N	\N	\N	f	t	1	40	f	f	f
1091	0	6302	37	\N	\N	\N	f	t	2	120	f	f	f
1092	0	6303	37	\N	\N	\N	t	t	3	120	f	f	f
1093	0	6304	37	\N	\N	\N	t	t	4	150	f	f	f
1094	0	6305	37	\N	\N	\N	f	t	5	150	f	f	f
1095	0	6306	37	\N	\N	\N	t	t	6	150	f	f	f
1096	0	6307	37	\N	\N	\N	f	t	7	170	f	f	f
1097	0	6308	37	\N	\N	\N	f	t	8	170	f	f	f
1100	0	3130	4	\N	\N	\N	f	t	\N	76	f	f	f
\.


--
-- Data for Name: query_messstelle; Type: TABLE DATA; Schema: master; Owner: postgres
--

COPY master.query_meas_facil_mp (id, query_user_id, meas_facil_id) FROM stdin;
6	18	30021
7	18	30022
18	35	30021
19	35	30022
20	35	20060
\.


--
-- Name: base_query_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.base_query_id_seq', 104, true);


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.filter_id_seq', 121, true);


--
-- Name: grid_col_mp_id_se Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.grid_col_mp_id_seq', 10445, true);


--
-- Name: grid_col_conf_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.grid_col_conf_id_seq', 10000, true);


--
-- Name: lada_user_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.lada_user_id_seq', 100, true);


--
-- Name: query_meas_facil_mp_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.query_meas_facil_mp_id_seq', 100, true);


--
-- Name: query_user_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.query_user_id_seq', 100, true);


--
-- Name: disp_id_seq; Type: SEQUENCE SET; Schema: master; Owner: postgres
--

SELECT pg_catalog.setval('master.disp_id_seq', 41, false);


--
-- PostgreSQL database dump complete
--

