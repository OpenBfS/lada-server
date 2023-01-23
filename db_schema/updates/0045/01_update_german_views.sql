DROP VIEW stamm.query_messstelle;

CREATE VIEW stamm.query_messstelle AS SELECT
    query_id AS query,
    meas_facil_id AS mess_stelle
FROM master.query_meas_facil_mp;

DROP VIEW stamm.rei_progpunkt_grp_zuord;

CREATE VIEW stamm.rei_progpunkt_grp_zuord AS SELECT
    rei_ag_gr_id AS rei_progpunkt_grp_id,
    rei_ag_id AS rei_progpunkt_id,
    last_mod AS letzte_aenderung
FROM master.rei_ag_gr_mp;
